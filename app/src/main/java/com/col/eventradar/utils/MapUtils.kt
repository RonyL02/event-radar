import android.content.Context
import android.util.Log
import android.view.View
import com.col.eventradar.api.locations.dto.LocationDetailsResultDTO
import com.col.eventradar.api.locations.dto.LocationSearchResult
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.api.locations.OpenStreetMapService
import com.col.eventradar.models.Event
import com.col.eventradar.models.EventType
import com.col.eventradar.models.Location
import com.col.eventradar.ui.bottom_sheets.EventDetailsBottomSheet
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.utils.ThemeUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MapUtils {
    const val DEFAULT_LAT = 32.0
    const val DEFAULT_LON = 35.0
    const val DEFAULT_ZOOM = 5.0

    private const val SEARCH_RESULT_AREA_SOURCE_NAME = "search-result-area-source"
    private const val SEARCH_RESULT_AREA_LAYER_NAME = "location-fill-layer"

    private const val RASTER_SOURCE_NAME = "osm-raster-source"
    private const val RASTER_LAYER_ID = "osm-raster-layer-id"

    const val EVENT_SOURCE_NAME = "events-source"
    private const val EVENTS_LAYER_ID = "events-layer-id"
    private const val EVENTS_BG_LAYER_ID = "events-background-layer-id"

    const val DEFAULT_MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json"
    private const val DEFAULT_MAP_RASTER_URL = "https://tile.openstreetmap.org"

    private const val TileJSON_VERSION = "2.1.0"
    private const val TAG = "MapUtils"


    fun addMapSourcesAndLayers(
        map: MapLibreMap,
        context: Context,
    ) {
        val style = map.style ?: return

        val rasterSource =
            RasterSource(
                RASTER_SOURCE_NAME,
                TileSet(TileJSON_VERSION, "${DEFAULT_MAP_RASTER_URL}/{z}/{x}/{y}.png"),
            )
        val rasterLayer =
            RasterLayer(
                RASTER_LAYER_ID,
                RASTER_SOURCE_NAME,
            ).withProperties(PropertyFactory.rasterSaturation(-1f))

        style.addSource(rasterSource)
        style.addLayer(rasterLayer)

        style.addSource(GeoJsonSource(SEARCH_RESULT_AREA_SOURCE_NAME))
        val themeColor = ThemeUtils.getThemeColor(context)

        val fillLayer =
            FillLayer(
                SEARCH_RESULT_AREA_LAYER_NAME,
                SEARCH_RESULT_AREA_SOURCE_NAME,
            ).apply {
                withProperties(
                    PropertyFactory.fillColor(themeColor),
                    PropertyFactory.fillOpacity(0.3f),
                )
            }
        style.addLayer(fillLayer)

        val eventSource = GeoJsonSource(
            EVENT_SOURCE_NAME,
            "{\"type\": \"FeatureCollection\", \"features\": []}"
        )

        style.addSource(eventSource)

        val eventLayer = SymbolLayer(EVENTS_LAYER_ID, EVENT_SOURCE_NAME)
            .withProperties(
                PropertyFactory.iconImage("{type}"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconSize(1.2f),
                PropertyFactory.iconColor(ThemeUtils.getThemeColor(context)),
                PropertyFactory.iconKeepUpright(true)
            )

        val eventBackgroundLayer = CircleLayer(EVENTS_BG_LAYER_ID, EVENT_SOURCE_NAME)
            .withProperties(
                PropertyFactory.circleRadius(30f),
                PropertyFactory.circleColor("#FFFFFF"),
                PropertyFactory.circleStrokeColor(ThemeUtils.getThemeColor(context)),
                PropertyFactory.circleStrokeWidth(4f),
            )

        style.addLayer(eventBackgroundLayer)
        style.addLayer(eventLayer)
    }

    fun setupInitialCameraPosition(
        map: MapLibreMap,
        lat: Double,
        lon: Double,
        zoom: Double,
    ) {
        map.cameraPosition =
            CameraPosition
                .Builder()
                .target(LatLng(lat, lon))
                .zoom(zoom)
                .build()
    }

    fun handleMapClick(
        map: MapLibreMap,
        point: LatLng,
        toastFragment: ToastFragment,
    ) {
        val features =
            map.queryRenderedFeatures(
                map.projection.toScreenLocation(point),
                EVENTS_LAYER_ID,
            )
        if (features.isNotEmpty()) {
            val feature = features.first()
            showFeatureContextMenu(toastFragment, feature)
        }
    }

    fun convertEventsToGeoJson(events: List<Event>): FeatureCollection {
        val features = events.mapNotNull { event ->
            event.location?.let { location ->
                Feature.fromGeometry(
                    Point.fromLngLat(location.longitude, location.latitude)
                ).apply {
                    addStringProperty("id", event.id)
                    addStringProperty("title", event.title)
                    addStringProperty("locationName",event.locationName)
                    addStringProperty("time", event.time.format(DateTimeFormatter.ISO_DATE_TIME))
                    addStringProperty("type", event.type.name)
                    addStringProperty("description", event.description)
                }
            }
        }
        return FeatureCollection.fromFeatures(features)
    }

    private fun showFeatureContextMenu(toastFragment: ToastFragment, feature: Feature) {
        val eventDetails = Event(
            title = feature.getStringProperty("title"),
            locationName = feature.getStringProperty("locationName"),
            description = feature.getStringProperty("description"),
            time = LocalDateTime.parse(feature.getStringProperty("time"), DateTimeFormatter.ISO_DATE_TIME),
            type = EventType.valueOf(feature.getStringProperty("type")),
            id = feature.getStringProperty("id"),
            location = Location(latitude= 0.0, longitude= 0.0)
        )

        val bottomSheet = EventDetailsBottomSheet(eventDetails)
        bottomSheet.show(toastFragment.parentFragmentManager, EventDetailsBottomSheet.TAG)
    }

    private fun toMapLibreFeature(result: LocationDetailsResultDTO): Feature {
        val geometry = result.geometry
        val feature =
            when (geometry.type) {
                "Polygon" -> {
                    val coordinates =
                        Gson().fromJson<List<List<List<Double>>>>(
                            geometry.coordinates,
                            object : TypeToken<List<List<List<Double>>>>() {}.type,
                        )

                    val polygonCoordinates =
                        coordinates[0].map { point -> Point.fromLngLat(point[0], point[1]) }

                    val polygon = Polygon.fromLngLats(listOf(polygonCoordinates))
                    Feature.fromGeometry(polygon)
                }

                "MultiPolygon" -> {
                    val coordinates =
                        Gson().fromJson<List<List<List<List<Double>>>>>(
                            geometry.coordinates,
                            object : TypeToken<List<List<List<List<Double>>>>>() {}.type,
                        )

                    val multiPolygonCoordinates =
                        coordinates.map { polygon ->
                            polygon.map { ring ->
                                ring.map { point -> Point.fromLngLat(point[0], point[1]) }
                            }
                        }

                    val multiPolygon =
                        org.maplibre.geojson.MultiPolygon
                            .fromLngLats(multiPolygonCoordinates)
                    Feature.fromGeometry(multiPolygon)
                }
                else -> {
                    val centroidCoordinates = result.centroid.coordinates
                    val point = Point.fromLngLat(centroidCoordinates[0], centroidCoordinates[1])
                    Feature.fromGeometry(point)
                }
            }

        feature.apply {
            addStringProperty("placeId", result.placeId.toString())
            addStringProperty("localname", result.localname)
            addStringProperty("category", result.category)
            addStringProperty("type", result.type)
            addStringProperty("countryCode", result.countryCode)
        }

        return feature
    }

    suspend fun handleLocationSelection(
        map: MapLibreMap,
        searchResult: LocationSearchResult,
        toastFragment: ToastFragment,
        binding: FragmentMapBinding,
    ) {
        try {
            val result = OpenStreetMapService.api.getLocationDetails(placeId = searchResult.placeId)
            val feature = toMapLibreFeature(result)

            map.style?.getSource(SEARCH_RESULT_AREA_SOURCE_NAME)?.apply {
                if (this is GeoJsonSource) {
                    setGeoJson(FeatureCollection.fromFeature(feature))
                }
            }

            binding.apply {
                mapAddLocationButton.visibility = View.VISIBLE
                mapAddLocationButton.setOnClickListener {
                    toastFragment("Added ${feature.getStringProperty("localname")} to User")
                    mapAddLocationButton.visibility = View.GONE
                }
            }

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Error body: $errorBody")
            toastFragment("Error fetching location: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "General error: ${e.message}")
        }

        val bounds =
            LatLngBounds
                .Builder()
                .include(LatLng(searchResult.southLat, searchResult.westLon))
                .include(LatLng(searchResult.northLat, searchResult.eastLon))
                .build()

        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250), 500)
    }
}
