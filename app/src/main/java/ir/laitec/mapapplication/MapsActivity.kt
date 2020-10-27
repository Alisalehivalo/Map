package ir.laitec.mapapplication

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback  , GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    // 1
    private lateinit var locationCallback: LocationCallback
    // 2
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Here you update lastLocation with the new location
        // and update the map with the new location coordinates.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        createLocationRequest()
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            loadPlacePicker()
        }


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        /*

        // Add a marker in Sydney and move the camera
        val tehran = LatLng(35.7, 51.4)
        map.addMarker(MarkerOptions().position(tehran).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(tehran))

         */
        //Here you enable the zoom controls on the map and declare MapsActivity as the callback triggered when the user clicks a marker on this map.
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)
        setUpMap()
        /*
        //set Zoom level (0 ..20)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(tehran, 12.0f))

         */


        /*
        mMap.isTrafficEnabled=true
        mMap.isBuildingsEnabled=true
        mMap.isIndoorEnabled=true
        mMap.uiSettings.isMyLocationButtonEnabled=true
        mMap.uiSettings.isMapToolbarEnabled=true
        mMap.uiSettings.isRotateGesturesEnabled=true
        mMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom=true

         */

    }



    override fun onMarkerClick(p0: Marker?)=false
    //The code checks if the app has been granted the ACCESS_FINE_LOCATION permission. If it hasn’t, then request it from the user.
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        // isMyLocationEnabled = true enables the my-location layer
        // which draws a light blue dot on the user’s location.
        // It also adds a button to the map that, when tapped,
        // centers the map on the user’s location.
        map.isMyLocationEnabled = true
//for change map type
        //GoogleMap.MAP_TYPE_TERRAIN displays a more detailed view of the area,
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN


// fusedLocationClient.getLastLocation()
// gives you the most recent location currently available.
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //for show the marker
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }

    }
    //a companion object with the code to request location permission

    //Create a MarkerOptions object and sets the user’s
    // current location as the position for the marker
    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        //to Here you added a call to getAddress()
        // and added this address as the marker title.
        val titleStr = getAddress(location)  // add these two lines
        markerOptions.title(titleStr)
        // 2
        map.addMarker(markerOptions)
    }

    //for Implementing Geocoding
    // 1.Creates a Geocoder object to turn a latitude and longitude
    // coordinate into an address and vice versa.
    //2. Asks the geocoder to get the address from the location
    // passed to the method.
    // 3.If the response contains any address, then append
    // it to a string and return.

    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }


    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        // 3
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3

    }
    // 1.In startLocationUpdates(), if the ACCESS_FINE_LOCATION permission
    // has not been granted, request it now and return.
    //2. If there is permission, request for location updates.

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    //Here’s what’s going on in createLocationRequest():
    //
    //    You create an instance of LocationRequest, add it to an instance of LocationSettingsRequest.Builder and retrieve and handle any changes to be made based on the current state of the user’s location settings.
    //    interval specifies the rate at which your app will like to receive updates.
    //    fastestInterval specifies the fastest rate at which the app can handle updates. Setting the fastestInterval rate places a limit on how fast updates will be sent to your app.
    //
    //    Before you start requesting for location updates, you need to check the state of the user’s location settings.
    //    You create a settings client and a task to check location settings.
    //    A task success means all is well and you can go ahead and initiate a location request.
    //    A task failure means the location settings have some issues which can be fixed. This could be as a result of the user’s location settings turned off.

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
    //Here’s what’s going on:
    //
    //    Override AppCompatActivity’s onActivityResult() method and start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    //    Override onPause() to stop location update request
    //    Override onResume() to restart the location update request.

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        //Here you retrieve details about the selected place if it has a RESULT_OK result for a PLACE_PICKER_REQUEST request,
        // and then place a marker on that position on the map.
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }

    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    //This method creates a new builder for an intent to start
    // the Place Picker UI and then starts the PlacePicker intent.
    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }



}