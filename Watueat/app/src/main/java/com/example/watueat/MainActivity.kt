package com.example.watueat

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.watueat.databinding.ActivityMainBinding
import com.example.watueat.model.RestaurantViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import android.Manifest
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.example.watueat.auth.AuthUser
import com.example.watueat.auth.isInvalid
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : AppCompatActivity() {

    private lateinit var user: AuthUser
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: RestaurantViewModel by viewModels()
    private val currentLocation = MutableLiveData<String>()

    companion object { var locationPermissionGranted: Boolean = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        initMenu()

        // Referenced: FC9 checking for services and permissions
        checkGooglePlayServices()
        requestPermissionForMaps()

        currentLocation.observe(this) { location ->
            val message = "current location at $location"
            Log.d("Fetching restaurants at: ", message)
            viewModel.fetchRestaurants("", location)
        }

        navController = findNavController(R.id.mainFragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onStart() {
        super.onStart()
        Log.d("Starting app: ", "checking for user....")
        user = AuthUser(activityResultRegistry)
        lifecycle.addObserver(user)
        user.observeUser().observe(this) { user ->
            viewModel.setCurrentUser(user)
            // User is changed, fetch new restaurants
            if (!user.isInvalid()) {
                user?.let {
                    Log.d("New User, fetching list: ", it.toString())
                    viewModel.newUserFetchRestaurants()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.mainFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Referenced: HW3 menu navigation
    private fun initMenu() {
        addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.goToFavorites -> {
                        navController.navigate(R.id.favoritesFragment)
                        true
                    }
                    R.id.goToMaps -> {
                        navController.navigate(R.id.mapsFragment)
                        true
                    }
                    R.id.logoutOfApp -> {
                        user.logout()
                        true
                    }
                    else -> false
                }
            }
        })
    }

    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 257)?.show()
            } else {
                Log.i(javaClass.simpleName, "Please install Google Play Services.")
                finish()
            }
        }
    }

    private fun requestPermissionForMaps() {
        val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    locationPermissionGranted = true
                    findCurrentLocation()
                } else -> {
                    Toast.makeText(this, "Permission required: Unable to show location", Toast.LENGTH_LONG).show()
                }
            }
        }
        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    // Referenced:
    // https://developer.android.com/develop/sensors-and-location/location/retrieve-current
    // https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient#getCurrentLocation(int,%20com.google.android.gms.tasks.CancellationToken)
    // https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
    private fun findCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (locationPermissionGranted ) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        currentLocation.postValue("$latitude, $longitude")
                    }
                }
        }
    }
}
