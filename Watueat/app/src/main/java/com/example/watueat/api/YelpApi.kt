package com.example.watueat.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.example.watueat.data.*

private const val BASE_URL = "https://api.yelp.com/v3/"

// Referenced:
// https://docs.developer.yelp.com/docs/fusion-intro
// https://docs.developer.yelp.com/reference/v3_business_search
interface YelpApi {

    @GET("businesses/search")
    suspend fun searchRestaurants( @Header("Authorization") key: String,
                                   @Query("term") searchTerm: String,
                                   @Query("location") location: String): ResultQuery

    companion object Factory {
        fun create(): YelpApi {
            val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
            val yelpApi = retrofit.create(YelpApi::class.java)
            return yelpApi
        }
    }
}
