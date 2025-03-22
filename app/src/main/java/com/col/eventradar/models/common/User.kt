package com.col.eventradar.models.common

import com.col.eventradar.models.AreaEntity

data class User(
    var id: String = "",
    val username: String = "",
    val imageUri: String? = null,
    val areasOfInterest: List<AreaOfInterest> = emptyList(),
) {
    companion object {
        const val USERNAME_KEY = "username"
        const val IMAGE_URI_KEY = "imageUri"
    }

    val json: HashMap<String, String?>
        get() {
            return hashMapOf(
                USERNAME_KEY to username,
                IMAGE_URI_KEY to imageUri,
            )
        }
}

data class AreaOfInterest(
    val osmId: String = "",
    val osmType: String = "",
    val name: String = "",
    val country: String = "",
)

fun AreaOfInterest.toAreaEntity(geojson: String): AreaEntity =
    AreaEntity(this.osmId, this.osmType, this.name, this.country, geojson)
