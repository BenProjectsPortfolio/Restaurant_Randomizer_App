package com.example.watueat.ui.view

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.watueat.databinding.MapsFragmentBinding
import com.example.watueat.model.RestaurantViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.watueat.MainActivity
import com.example.watueat.R
import com.example.watueat.data.Restaurant
import com.example.watueat.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private val viewModel: RestaurantViewModel by activityViewModels()
    private var _binding: MapsFragmentBinding? = null
    private val binding get() = _binding!!

    private val colorHuesForIcon: HashMap<Int, Float> = hashMapOf(
        0 to BitmapDescriptorFactory.HUE_RED,    // last icon color before iterating again
        1 to BitmapDescriptorFactory.HUE_ORANGE, // starting icon color
        2 to BitmapDescriptorFactory.HUE_ROSE
        // add as many as desired
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingBar.visibility = View.VISIBLE
        initMenu()

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
    }


    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        if ( MainActivity.locationPermissionGranted ) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = true
        }

        viewModel.observeAllUserFavorites().observe(viewLifecycleOwner) {
            googleMap.clear()
            val mapRestaurantHeartsAndComments = HashMap<Restaurant, Pair<Int, MutableList<String>>>()

            // map all equal restaurant to contain a Pair of numberOfFavorites
            // and a list that contains all user comments that favorite that restaurant
            for (restaurant in it) {
                if (mapRestaurantHeartsAndComments[restaurant] == null) {
                    mapRestaurantHeartsAndComments[restaurant] = Pair(1, mutableListOf("${restaurant.ownerName}: ${restaurant.comments}"))
                } else {
                    mapRestaurantHeartsAndComments[restaurant]!!.second.add("${restaurant.ownerName}: ${restaurant.comments}")
                    mapRestaurantHeartsAndComments[restaurant] = Pair(
                        mapRestaurantHeartsAndComments[restaurant]!!.first + 1,
                        mapRestaurantHeartsAndComments[restaurant]!!.second
                    )
                }
            }

            // iterate map to set markers for each location
            // restaurant.value.first -> number of favorites
            // restaurant.value.second -> list of comments
            for (restaurant in mapRestaurantHeartsAndComments) {
                val latitude = restaurant.key.coordinates.latitude.toDouble()
                val longitude = restaurant.key.coordinates.longitude.toDouble()

                // icon color is based on the number of favorites a location has
                val iconColor = colorHuesForIcon[restaurant.value.first % (colorHuesForIcon.size)]

                // set icon
                val marker = MarkerOptions().position(LatLng(latitude, longitude))
                marker.icon(BitmapDescriptorFactory.defaultMarker(iconColor!!))

                // get restaurant from map info
                val title = restaurant.key.name
                val hearted = restaurant.value.first.toString() + " hearts"
                val image = restaurant.key.image

                // in order to pass more argument to a marker, which only has title and snippet:
                // a list is created containing [title, numberOfFavorites, image]
                // then that list is joined to pass it as a string
                // once its passed, InfoWindowAdapter will separate the data
                val listForPopupTitle = listOf(title, hearted, image)
                val stringListForPopupTitle = listForPopupTitle.joinToString("|")

                // a string of all the comments is created
                val listOfCommentsInString = StringBuilder()
                for (comment in restaurant.value.second) {
                    listOfCommentsInString.append(comment)
                    listOfCommentsInString.append("\n")
                }

                marker.title(stringListForPopupTitle)
                marker.snippet(listOfCommentsInString.toString())

                googleMap.addMarker(marker)
            }
        }

        // Referenced:
        // https://developers.google.com/maps/documentation/android-sdk/marker#maps_android_markers_custom_marker_color-kotlin
        // https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap.InfoWindowAdapter
        googleMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(p0: Marker): View? { return null }

            override fun getInfoContents(p0: Marker): View? {
                val marker = p0
                val infoWindow = layoutInflater.inflate(R.layout.location_info_popup, null)

                // scrap all data from title, string was organized in the following order:
                // title | numOfHearts | imageOfRestaurant
                val contentFromTitle = marker.title?.split("|")
                val title = contentFromTitle?.get(0)
                val numOfHearts = contentFromTitle?.get(1)
                val imageOfRestaurant = contentFromTitle?.get(2)!!
                val contentFromSnippet = marker.snippet  // snippet contains the string of comments

                // find all window views
                val nameOfRestaurant = infoWindow.findViewById<TextView>(R.id.popupName)
                val numberOfHearts = infoWindow.findViewById<TextView>(R.id.popupHearts)
                val image = infoWindow.findViewById<ImageView>(R.id.popupImage)
                val comments = infoWindow.findViewById<TextView>(R.id.popupComments)

                // bind content from title and snippet to views
                nameOfRestaurant.text = title
                numberOfHearts.text = numOfHearts
                Glide.glideFetch(imageOfRestaurant, image)
                comments.text = contentFromSnippet

                return infoWindow
            }
        })

        findLocation()
        binding.loadingBar.visibility = View.GONE
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun findLocation() {
        if (Build.VERSION.SDK_INT >= 33) {
            geocoder.getFromLocationName(viewModel.getLocationOfRestaurantsSearched(), 1, Geocoder.GeocodeListener {
                MainScope().launch {
                    setLocationOnMap(it.toList())
                }
            })
        } else {
            MainScope().launch {
                setLocationOnMap(geocoder.getFromLocationName(viewModel.getLocationOfRestaurantsSearched(),1)!!.toList())
            }
        }
    }

    private suspend fun setLocationOnMap(location: List<Address>) {
        withContext(Dispatchers.Main) {
            for (address in location) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(address.latitude, address.longitude), 12.0f))
            }
        }
    }
}
