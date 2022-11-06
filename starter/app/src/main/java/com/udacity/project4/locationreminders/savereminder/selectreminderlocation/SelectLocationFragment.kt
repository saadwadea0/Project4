package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.Log.v
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var map: GoogleMap? = null
    var mPoi: PointOfInterest? = null
    private var isLocationSelected = false
    private var ltd: Double = 0.0
    private var lng: Double = 0.0
    private var poiName: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        /** map setup implementation */
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        onLocationSelected()
        return binding.root

    }

    private fun onLocationSelected() {
        // When the user confirms on the selected location,
        binding.btnSave.setOnClickListener {
            if (isLocationSelected) {
                _viewModel.latitude.value = ltd
                _viewModel.longitude.value = lng
                /**
                Send back the selected location details to the view model
                 */
                _viewModel.reminderSelectedLocationStr.value = poiName

                /**
                Navigate back to the previous fragment to save the reminder
                 */
                _viewModel.navigationCommand.postValue(NavigationCommand.Back)

                /**
                If user not selected the location
                 */
            } else {
                Toast.makeText(context, getString(R.string.select_location), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        /**
        Change the map type based on the user's selection.
         */
        R.id.normal_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_HYBRID

            true
        }
        R.id.satellite_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE

            true
        }
        R.id.terrain_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_TERRAIN

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    /**
    zoom to the user location after taking his permission
     */
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map?.isMyLocationEnabled = true

        } else {
            Log.d(AuthenticationActivity.TAG, "Request foreground only location permission")
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        }
        map?.moveCamera(CameraUpdateFactory.zoomIn())

    }

    /** Check self permission */
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity!!, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Complete Your Address */
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                v(TAG, "My Current location address $strReturnedAddress")
            } else {
                v(TAG, "No Address returned!")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            v(TAG, "Canont get Address!")
        }
        return strAdd
    }

    /** map setup implementation */
    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        enableMyLocation()
        val sydney = LatLng(-34.0, 151.0)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))
        setMapLongClick(map!!)
        setMapStyle(map!!)
        mapPoiClick(map!!)
    }

    /** map zooming with long click implementation */
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            val location = getCompleteAddressString(latLng.latitude, latLng.longitude)
            val marker = map.addMarker(
                MarkerOptions().position(latLng).title(location)
            )

            val zoomLevel = 15f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
            isLocationSelected = true
            ltd = latLng.latitude
            lng = latLng.longitude
            poiName = location
            marker.showInfoWindow()
        }
    }

    /** marker to location that the user selected */
    private fun mapPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )
            val zoomLevel = 15f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, zoomLevel))
            poiMarker.showInfoWindow()
            mPoi = poi
            isLocationSelected = true
            ltd = poi.latLng.latitude
            lng = poi.latLng.longitude
            poiName = poi.name
        }
    }

    /** Map style */
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context, R.raw.map_style
                )
            )

            if (!success) {
                Log.e(AuthenticationActivity.TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(AuthenticationActivity.TAG, "Can't find style. Error: ", e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (isPermissionGranted() && map != null) map?.isMyLocationEnabled = true

    }

}

private const val REQUEST_LOCATION_PERMISSION = 115
private const val TAG = "SelectLocationFragment"