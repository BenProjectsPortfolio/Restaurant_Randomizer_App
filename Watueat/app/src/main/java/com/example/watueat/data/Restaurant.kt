package com.example.watueat.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import com.google.firebase.firestore.DocumentId

// Handling the result query of api
data class ResultQuery( @SerializedName("total") val total: Int, @SerializedName("businesses") val restaurants: List<Restaurant>)

data class Restaurant(

    @SerializedName("name")
    val name: String = "",

    @SerializedName("location")
    val location: Location = Location("", "", "", "", "", listOf()),

    @SerializedName("phone")
    val phoneNumber: String = "",

    @SerializedName("display_phone")
    val displayPhoneNumber: String = "",

    @SerializedName("categories")
    val category: List<Categories> = listOf(),

    @SerializedName("url")
    val website: String = "",

    @SerializedName("rating")
    val rating: Double = 0.0,

    @SerializedName("review_count")
    val reviewCount: Int = 0,

    @SerializedName("price")
    val price: String = "",

    @SerializedName("image_url")
    val image: String = "",

    @SerializedName("coordinates")
    val coordinates: Coordinates = Coordinates(),

    // For comment section in favorites
    var comments: String = "",

    // Auth Information for Metadata
    var ownerName: String = "",
    var ownerUid: String = "",
    var uuid: String = "",

    @DocumentId
    var firestoreId: String = ""

): Serializable {

    override fun equals(other: Any?): Boolean {
        if (other is Restaurant) {
            return name == other.name
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + coordinates.latitude.hashCode()
        result = 31 * result + coordinates.longitude.hashCode()
        return result
    }
}

// Handling Categories, Location, and Coordinates as they are lists, getting data we will use in Restaurants

data class Categories(

    @SerializedName("alias")
    val alias: String = "",

    @SerializedName("title")
    val title: String = ""
)

data class Location(

    @SerializedName("address1")
    val address: String = "",

    @SerializedName("city")
    val city: String = "",

    @SerializedName("state")
    val state: String = "",

    @SerializedName("zip_code")
    val zipCode: String = "",

    @SerializedName("country")
    val country: String = "",

    @SerializedName("display_address")
    val fullAddress: List<String> = listOf()
)

data class Coordinates(

    @SerializedName("latitude")
    val latitude: Float = 0f,

    @SerializedName("longitude")
    val longitude: Float = 0f
)
