package com.col.eventradar.utils

import android.content.Context
import com.col.eventradar.ui.views.MapFragment
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

object MapUtils {
    fun setupInitialCameraPosition(map: MapLibreMap, lat: Double, lon: Double, zoom: Double) {
        map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
            .target(LatLng(lat, lon))
            .zoom(zoom)
            .build()
    }

    fun addMapSourcesAndLayers(map: MapLibreMap, context: Context) {
        val style = map.style ?: return

        val rasterSource = RasterSource(
            RASTER_SOURCE_NAME,
            TileSet("2.1.0", "${DEFAULT_MAP_RASTER_URL}/{z}/{x}/{y}.png")
        )
        val rasterLayer = org.maplibre.android.style.layers.RasterLayer(
            RASTER_LAYER_ID,
            RASTER_SOURCE_NAME
        )

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

    const val SEARCH_RESULT_AREA_SOURCE_NAME = "search-result-area-source"
    const val SEARCH_RESULT_AREA_LAYER_NAME = "location-fill-layer"

    const val RASTER_SOURCE_NAME = "osm-raster-source"
    const val RASTER_LAYER_ID = "osm-raster-layer-id"

    const val DEFAULT_MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json"
    const val DEFAULT_MAP_RASTER_URL = "https://tile.openstreetmap.org"
}