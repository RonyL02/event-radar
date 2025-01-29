import android.content.Context
import android.util.Log
import android.view.View
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.network.OpenStreetMapService
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.utils.ThemeUtils
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

object MapUtils {
    const val DEFAULT_LAT = 32.0
    const val DEFAULT_LON = 35.0
    const val DEFAULT_ZOOM = 5.0

    const val SEARCH_RESULT_AREA_SOURCE_NAME = "search-result-area-source"
    const val SEARCH_RESULT_AREA_LAYER_NAME = "location-fill-layer"

    const val RASTER_SOURCE_NAME = "osm-raster-source"
    const val RASTER_LAYER_ID = "osm-raster-layer-id"

    const val DEFAULT_MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json"
    const val DEFAULT_MAP_RASTER_URL = "https://tile.openstreetmap.org"

    const val TileJSON_VERSION = "2.1.0"

    val TAG = "MapUtils"

    fun initMap(map: MapLibreMap, context: Context, toastFragment: ToastFragment) {

        map.setStyle(DEFAULT_MAP_STYLE_URL) { style ->
            setupInitialCameraPosition(map, DEFAULT_LAT, DEFAULT_LON, DEFAULT_ZOOM)
            addMapSourcesAndLayers(map, context)
//            enableLocationComponent(style)
        }

        map.addOnMapClickListener { point ->
            handleMapClick(map, point, toastFragment)
            true
        }
    }

    fun addMapSourcesAndLayers(map: MapLibreMap, context: Context) {
        val style = map.style ?: return

        val rasterSource = RasterSource(
            RASTER_SOURCE_NAME,
            TileSet(TileJSON_VERSION, "${DEFAULT_MAP_RASTER_URL}/{z}/{x}/{y}.png")
        )
        val rasterLayer = RasterLayer(
            RASTER_LAYER_ID,
            RASTER_SOURCE_NAME
        ).withProperties(PropertyFactory.rasterSaturation(-1f))

        style.addSource(rasterSource)
        style.addLayer(rasterLayer)

        style.addSource(GeoJsonSource(SEARCH_RESULT_AREA_SOURCE_NAME))
        val themeColor = ThemeUtils.getThemeColor(context)

        val fillLayer = FillLayer(
            SEARCH_RESULT_AREA_LAYER_NAME,
            SEARCH_RESULT_AREA_SOURCE_NAME
        ).apply {
            withProperties(
                PropertyFactory.fillColor(themeColor),
                PropertyFactory.fillOpacity(0.3f)
            )
        }
        style.addLayer(fillLayer)
    }

    fun setupInitialCameraPosition(map: MapLibreMap, lat: Double, lon: Double, zoom: Double) {
        map.cameraPosition = CameraPosition.Builder()
            .target(LatLng(lat, lon))
            .zoom(zoom)
            .build()
    }

    fun handleMapClick(map: MapLibreMap, point: LatLng, toastFragment: ToastFragment) {
        val features = map.queryRenderedFeatures(map.projection.toScreenLocation(point), SEARCH_RESULT_AREA_LAYER_NAME)
        if (features.isNotEmpty()) {
            val feature = features.first()
            showFeatureContextMenu(toastFragment, feature)
        }
    }

    private fun showFeatureContextMenu(toastFragment: ToastFragment, feature: Feature) {
        toastFragment.showToast("Clicked on: ${feature.getStringProperty("localname")}")
    }

    suspend fun handleLocationSelection(
        map: MapLibreMap,
        searchResult: LocationSearchResult,
        toastFragment: ToastFragment,
        binding: FragmentMapBinding
    ) {
        try {
            val result = OpenStreetMapService.api.getLocationDetails(osmId = searchResult.osmId)
            val feature = result.toMapLibreFeature()

            map.style?.getSource(SEARCH_RESULT_AREA_SOURCE_NAME)?.let { source ->
                if (source is GeoJsonSource) {
                    source.setGeoJson(FeatureCollection.fromFeature(feature))
                }
            }

            binding.mapAddLocationButton.visibility = View.VISIBLE
            binding.mapAddLocationButton.setOnClickListener {
                toastFragment.showToast("Added ${feature.getStringProperty("localname")} to User")
                binding.mapAddLocationButton.visibility = View.GONE
            }

        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Error body: $errorBody")
            toastFragment.showToast("Error fetching location: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "General error: ${e.message}")
        }

        val bounds = LatLngBounds.Builder()
            .include(LatLng(searchResult.southLat, searchResult.westLon))
            .include(LatLng(searchResult.northLat, searchResult.eastLon))
            .build()

        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 500)
    }
}
