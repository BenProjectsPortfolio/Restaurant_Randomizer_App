package com.example.watueat.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.watueat.api.YelpApi
import com.example.watueat.api.RestaurantRepository
import com.example.watueat.auth.User
import com.example.watueat.auth.invalidUser
import com.example.watueat.data.Restaurant
import com.example.watueat.db.DatabaseHelper
import java.util.UUID

class RestaurantViewModel: ViewModel() {

    private val api = YelpApi.create()
    private val repo = RestaurantRepository(api)
    private val dbHelper = DatabaseHelper()

    private var currentUser = MutableLiveData<User>().apply { value = invalidUser }

    private val restaurantSearch = "restaurants "
    private var typeOfFoodSearched = ""
    private var locationSearched = ""

    // this triggers the search, status of boolean does not matter
    private var searchRestaurantsTrigger = MutableLiveData<Boolean>().apply { value = false }

    private var listOfAllUsersFavoriteRestaurants = MutableLiveData<List<Restaurant>>()
    private var listOfFavoriteRestaurants = MutableLiveData<List<Restaurant>>()

    // Referenced: HW4 using MediatorLiveData and launching coroutine
    private var listOfRestaurants = MediatorLiveData<List<Restaurant>>().apply {
        addSource(searchRestaurantsTrigger) {  searchingRestaurants ->
            viewModelScope.launch(viewModelScope.coroutineContext + Dispatchers.IO) {
                postValue(repo.fetchRestaurants(restaurantSearch + typeOfFoodSearched, locationSearched))
            }
        }
    }


    ////////////////////////
    // Network Functions //
    ///////////////////////

    private fun netFetchRestaurants() {
        searchRestaurantsTrigger.value = !(searchRestaurantsTrigger.value)!!
    }

    fun newUserFetchRestaurants() {
        listOfRestaurants.postValue(emptyList())
        fetchRestaurants(typeOfFoodSearched, locationSearched)
    }

    fun fetchRestaurants(typeOfFoodSearch: String, locationOfRestaurantSearch: String) {

        if (typeOfFoodSearch.isNullOrEmpty()) typeOfFoodSearched = "" else typeOfFoodSearched = typeOfFoodSearch

        if (locationOfRestaurantSearch.isNotEmpty()) { locationSearched = locationOfRestaurantSearch }

        fetchRestaurantMeta {  }
        netFetchRestaurants()
    }


    /////////////////////////
    // Observer Functions //
    ////////////////////////

    fun observeListOfRestaurants(): LiveData<List<Restaurant>> {
        dbHelper.listenForRestaurantMeta(currentUser.value!!.uid) {
            listOfFavoriteRestaurants.postValue(it)
        }
        return listOfRestaurants
    }

    fun observeAllUserFavorites(): LiveData<List<Restaurant>> {
        dbHelper.listenForAllUsersRestaurantMeta() {
            listOfAllUsersFavoriteRestaurants.postValue(it)
        }
        return listOfAllUsersFavoriteRestaurants
    }

    fun observeListOfFavoriteRestaurants(): LiveData<List<Restaurant>> {
        dbHelper.listenForRestaurantMeta(currentUser.value!!.uid) {
            listOfFavoriteRestaurants.postValue(it)
        }
        return listOfFavoriteRestaurants
    }


    ///////////////////////
    // Helper Functions //
    //////////////////////

    fun setCurrentUser(user: User) {
        currentUser.value = user
    }

    fun getLocationOfRestaurantsSearched(): String {
        return locationSearched
    }

    fun isFavorite(restaurant: Restaurant): Boolean {
        val list = listOfFavoriteRestaurants.value
        return list?.contains(restaurant) ?: false
    }

    fun addFavorite(restaurant: Restaurant) {
        if (!isFavorite(restaurant)) {
            createRestaurantMeta(restaurant, UUID.randomUUID().toString())
        }
    }

    fun removeFavorite(restaurant: Restaurant) {
        if (isFavorite(restaurant)) {
            val list = listOfFavoriteRestaurants.value.orEmpty().toMutableList()
            // Note: Restaurants from MainFragment come from Retrofit's fetch
            // Hence, they do not have firestoreIds
            // However, we are only comparing the content of restaurant (look at hashCode() in Restaurant.kt)
            // Check for position of Restaurant in favorites list then call db to remove that Restaurant
            val position = list?.indexOf(restaurant)
            removeRestaurantMeta(list.get(position!!))
        }
    }

    fun updateComments(restaurant: Restaurant){
        updateCommentsRestaurantMeta(restaurant)
    }


    ////////////////////////
    // Database Functions //
    ////////////////////////

    private fun fetchRestaurantMeta(resultListener:() -> Unit) {
        dbHelper.fetchRestaurantMeta(currentUser.value!!.uid) {
            listOfFavoriteRestaurants.postValue(it)
            resultListener.invoke()
        }
    }

    private fun updateCommentsRestaurantMeta(restaurant: Restaurant) {
        dbHelper.updateCommentsRestaurantMeta(restaurant) {
            listOfFavoriteRestaurants.postValue(it)
        }
    }

    private fun removeRestaurantMeta(restaurant: Restaurant) {
        dbHelper.removeRestaurantMeta(restaurant) {
            listOfFavoriteRestaurants.postValue(it)
        }
    }

    private fun createRestaurantMeta(restaurant: Restaurant, uuid: String) {
        restaurant.ownerName = currentUser.value!!.name
        restaurant.ownerUid = currentUser.value!!.uid
        restaurant.uuid = uuid
        restaurant.comments = "\u2764" // default comment

        dbHelper.createRestaurantMeta(restaurant) {
            listOfFavoriteRestaurants.postValue(it)
        }
    }
}
