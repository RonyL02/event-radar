import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import java.lang.ref.WeakReference

object MapManager {
    private var mapViewRef: WeakReference<MapView>? = null
    private var mapLibreMap: MapLibreMap? = null

    fun initialize(mapView: MapView) {
        mapViewRef = WeakReference(mapView)
        mapView.getMapAsync { map ->
            mapLibreMap = map
        }
    }

    fun getMapAsync(callback: OnMapReadyCallback) {
        mapViewRef?.get()?.getMapAsync(callback)
    }

    fun getMap(): MapLibreMap? = mapLibreMap
}
