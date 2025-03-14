package com.col.eventradar.models


data class User(
    var id: String = "",
    val username: String = "",
    val imageUri: String? = null,
    val areasOfInterest: Array<AreaOfInterest> = emptyArray()
) {

    companion object {
        const val USERNAME_KEY = "username"
        const val IMAGE_URI_KEY = "imageUri"
    }

    val json: HashMap<String, String?>
        get() {
            return hashMapOf(
                USERNAME_KEY to username,
                IMAGE_URI_KEY to imageUri
            )
        }
}

data class AreaOfInterest(
    val placeId: String,
    val name: String,
    val country: String
)