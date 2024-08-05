package com.example.watueat.api

import android.util.Log
import java.lang.Exception
import com.example.watueat.data.Restaurant

private const val API_KEY = "1L9VMTnNJrpzROccKGRjcUkSjY8nwaSHUORjV8TvkBhM9A2dcXoErJ95-rNTlTP-mizOsTWId1ZDzd_UwUbvQCI39YDp2LGBAuB_V6bz70PjCiBRR5OTFUfDiEEgZnYx"

// Referenced: https://docs.developer.yelp.com/docs/fusion-authentication
class RestaurantRepository(private val api: YelpApi) {

    suspend fun fetchRestaurants(type: String, location: String): List<Restaurant> {
        try {
            return api.searchRestaurants("Bearer $API_KEY", type, location).restaurants
        } catch (e: Exception) {
            Log.d("Error occurred when fetching: ", e.message.toString())
            return emptyList()
        }
    }
}
