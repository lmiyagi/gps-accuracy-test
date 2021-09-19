package br.com.leonardomiyagi.gpsaccuracytest

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.OpenTopo)

        requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        mapView.setMultiTouchControls(true)

        mapController = mapView.controller

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /*fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_NO_POWER, object : CancellationToken() {
            override fun isCancellationRequested(): Boolean {
                return true
            }

            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                return this
            }
        }).addOnSuccessListener { asdf ->
            println("Lat: ${asdf.latitude} Lon: ${asdf.longitude} Acu: ${asdf.accuracy}")
        }.addOnFailureListener {
            println("LOCATION FAILED")
        }*/
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val request = LocationRequest.create()
            .setNumUpdates(10)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationClient.requestLocationUpdates(request, object: LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                println("LOCATION - Lat: ${location.latitude} Lon: ${location.longitude} Acu: ${location.accuracy}")
                val geoPoint = GeoPoint(location)
                mapController.setZoom(15.0)
                mapController.setCenter(geoPoint)

                val overlay = DirectedLocationOverlay(this@MainActivity)
                overlay.location = geoPoint
                overlay.setAccuracy(location.accuracy.toInt())
                overlay.setBearing(location.bearing)

                mapView.overlays.add(overlay)
            }
        }, Looper.getMainLooper())


        mapView.addOnFirstLayoutListener { v, left, top, right, bottom ->

            /*fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun isCancellationRequested(): Boolean {
                    return false
                }

                override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                    return this
                }
            }).addOnSuccessListener { location ->
                println("LOCATION - Lat: ${location.latitude} Lon: ${location.longitude} Acu: ${location.accuracy}")
                val geoPoint = GeoPoint(location)
                mapController.setZoom(15.0)
                mapController.setCenter(geoPoint)

                val overlay = DirectedLocationOverlay(this)
                overlay.location = geoPoint
                overlay.setAccuracy(location.accuracy.toInt())
                overlay.setBearing(location.bearing)

                mapView.overlays.add(overlay)
            }.addOnCanceledListener {
                println("LOCATION CANCELED")
            }.addOnFailureListener {
                println("LOCATION FAILED")
            }*/
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (element in grantResults) {
            permissionsToRequest.add(element.toString())
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}