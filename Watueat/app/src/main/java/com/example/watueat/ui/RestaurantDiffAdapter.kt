package com.example.watueat.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.watueat.R
import com.example.watueat.databinding.RestaurantCardBinding
import com.example.watueat.model.RestaurantViewModel
import com.example.watueat.data.Restaurant
import com.example.watueat.glide.Glide

class RestaurantDiffAdapter(val context: Context, val viewModel: RestaurantViewModel, val navigateToSelectedRestaurant: (Restaurant) -> Unit)
    : ListAdapter<Restaurant, RestaurantDiffAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(val restaurantCardBinding: RestaurantCardBinding): RecyclerView.ViewHolder(restaurantCardBinding.root) {

        fun bind(restaurant: Restaurant){

            // set up the card
            Glide.glideFetch(restaurant.image, restaurantCardBinding.cardPicture)
            restaurantCardBinding.name.text = restaurant.name
            restaurantCardBinding.address.text = restaurant.location.address
            val cityAndZip = StringBuilder().append(restaurant.location.city).append(" ").append(restaurant.location.zipCode)
            restaurantCardBinding.cityAndZip.text = cityAndZip

            val gettingCategories = StringBuilder()
            for (category in restaurant.category) {
                gettingCategories.append(category.alias)
                gettingCategories.append(", ")
            }
            val category = gettingCategories.substring(0, gettingCategories.length - 2)
            restaurantCardBinding.category.text = category

            restaurantCardBinding.priceRating.text = restaurant.price
            restaurantCardBinding.ratingBar.rating = restaurant.rating.toFloat()

            restaurantCardBinding.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
            if (viewModel.isFavorite(restaurant)){
                restaurantCardBinding.favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RestaurantCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val restaurantItem = getItem(position)
        holder.bind(restaurantItem)

        holder.restaurantCardBinding.cardPicture.setOnClickListener { navigateToSelectedRestaurant(restaurantItem) }

        holder.restaurantCardBinding.favoriteButton.setOnClickListener {
            if (viewModel.isFavorite(restaurantItem)) {
                viewModel.removeFavorite(restaurantItem)
                holder.restaurantCardBinding.favoriteButton.setImageResource(android.R.color.transparent)
                holder.restaurantCardBinding.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
            } else {
                viewModel.addFavorite(restaurantItem)
                holder.restaurantCardBinding.favoriteButton.setImageResource(android.R.color.transparent)
                holder.restaurantCardBinding.favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
            }
        }
    }

    class Diff: DiffUtil.ItemCallback<Restaurant>() {

        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
            return oldItem.coordinates.latitude == newItem.coordinates.latitude
                    && oldItem.coordinates.longitude == newItem.coordinates.longitude
                    && oldItem.name == newItem.name
                    && oldItem.location == newItem.location
                    && oldItem.phoneNumber == newItem.phoneNumber
                    && oldItem.displayPhoneNumber == newItem.displayPhoneNumber
                    && oldItem.website == newItem.website
                    && oldItem.rating == newItem.rating
                    && oldItem.reviewCount == newItem.reviewCount
                    && oldItem.price == newItem.price
        }
    }
}
