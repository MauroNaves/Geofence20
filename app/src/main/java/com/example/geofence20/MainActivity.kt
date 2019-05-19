package com.example.geofence20

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment
import com.example.geofence20.fingerprint.FingerPrintAuthCallback
import com.example.geofence20.fingerprint.FingerPrintAuthHelper
import com.example.geofence20.fingerprint.FingerPrintUtils
import com.example.geofence20.model.MapMarker
import com.example.geofence20.model.MapMarkerFactory
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
	OnMapReadyCallback, LocationListener, FingerPrintAuthCallback {

	companion object {
		val GEOFENCE_RADIUS = 100F
		private val CODIGO_MARKER_CASA = 1
		private val CODIGO_MARKER_TRABALHO = 2
		private val DEFAULT_ZOOM = 15f
	}

	private val LOCATION_PERMISSION_CODE = 1
	private var codigoSelecionado = 0

	private var map: GoogleMap? = null
	private var locationManager: LocationManager? = null
	private var myLocation: LatLng? = null
	private var mFingerPrintAuthHelper: FingerPrintAuthHelper? = null
	private var mGeofencingClient: GeofencingClient? = null
	private var factory: MapMarkerFactory? = null

	private val geofencePendingIntent: PendingIntent
		get() {
			val intent = Intent(this, LocationAlertIntentService::class.java)
			return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)
		configurarDrawerLayout()

		factory = MapMarkerFactory()

		val mapInfoWindowFragment =
			supportFragmentManager.findFragmentById(R.id.mapa) as MapInfoWindowFragment?
		mapInfoWindowFragment?.getMapAsync(this)

		locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
		// Here, thisActivity is the current activity
		if (ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(
					this,
					Manifest.permission.ACCESS_FINE_LOCATION
				)
			) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(
					this,
					arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
					LOCATION_PERMISSION_CODE
				)

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		} else {
			locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, this)
		}

		if (FingerPrintUtils.isSupportedHardware(applicationContext)) {
			mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(applicationContext, this)
		}

		mGeofencingClient = LocationServices.getGeofencingClient(this)
	}

	private fun configurarDrawerLayout() {
		val toggle = ActionBarDrawerToggle(
			this,
			drawer_layout,
			toolbar,
			R.string.navigation_drawer_open,
			R.string.navigation_drawer_close
		)
		drawer_layout.addDrawerListener(toggle)
		toggle.syncState()

		nav_view.setNavigationItemSelectedListener(this)
	}

	override fun onResume() {
		super.onResume()
		mFingerPrintAuthHelper?.startAuth()
	}

	override fun onPause() {
		super.onPause()
		mFingerPrintAuthHelper?.stopAuth()
	}

	override fun onBackPressed() {
		if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
			drawer_layout.closeDrawer(GravityCompat.START)
		} else {
			super.onBackPressed()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main, menu)
		menu.findItem(R.id.action_remove_casa).isVisible = factory?.casa != null
		menu.findItem(R.id.action_remove_trabalho).isVisible = factory?.trabalho != null
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_settings -> finish()
			R.id.action_remove_casa -> removerCasa()
			R.id.action_remove_trabalho -> removerTrabalho()
		}
		return super.onOptionsItemSelected(item)
	}

	private fun removerTrabalho() {
		removerGeofence(factory?.trabalho)
		factory?.trabalho = null
		rePopularMarkers()
	}

	private fun removerCasa() {
		removerGeofence(factory?.casa)
		factory?.casa = null
		rePopularMarkers()
	}

	private fun rePopularMarkers() {
		map?.clear()
		factory?.casa?.let {
			popularMarker(it, false)
		}
		factory?.trabalho?.let {
			popularMarker(it, false)
		}
	}

	private fun removerGeofence(mapMarker: MapMarker?) {
		mapMarker?.let {
			val geofenceIds = ArrayList<String>()
			geofenceIds.add(it.getKey())
			mGeofencingClient?.removeGeofences(geofenceIds)
		}
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.nav_home -> if (myLocation != null) {
				map?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM))
			}
			R.id.nav_localiza -> {
				//			map.animateCamera(CameraUpdateFactory.newLatLngZoom(carroLatLng, DEFAULT_ZOOM));
			}
			R.id.nav_casa -> casaAction()
			R.id.nav_trabalho -> trabalhoAction()
		}
		drawer_layout.closeDrawer(GravityCompat.START)
		return true
	}

	private fun trabalhoAction() {
		if (factory?.trabalho == null) {
			codigoSelecionado = CODIGO_MARKER_TRABALHO
		} else {
			map?.animateCamera(
				CameraUpdateFactory.newLatLngZoom(
					factory?.trabalho?.latLng,
					DEFAULT_ZOOM
				)
			)
		}
	}

	private fun casaAction() {
		if (factory?.casa == null) {
			codigoSelecionado = CODIGO_MARKER_CASA
		} else {
			map?.animateCamera(
				CameraUpdateFactory.newLatLngZoom(
					factory?.casa?.latLng,
					DEFAULT_ZOOM
				)
			)
		}
	}

	override fun onMapReady(googleMap: GoogleMap) {
		map = googleMap
		map?.setOnMapLongClickListener { latLng ->
			factory?.getMapMarker(codigoSelecionado, latLng)?.let {
				popularMarker(it, true)
			}
			codigoSelecionado = 0
		}
	}

	private fun popularMarker(mapMarker: MapMarker, inserindo: Boolean) {
		map?.let {
			it.addMarker(mapMarker.getMarkerOptions())
			it.addCircle(mapMarker.getCircleOptions())
			addLocationAlert(mapMarker, inserindo)
			invalidateOptionsMenu()
		}
	}

	@SuppressLint("MissingPermission")
	private fun addLocationAlert(mapMarker: MapMarker, inserindo: Boolean) {
		val geofence = mapMarker.getGeofence()
		mGeofencingClient!!.addGeofences(getGeofencingRequest(geofence), geofencePendingIntent)
			.addOnSuccessListener {
				if (inserindo) Toast.makeText(
					this@MainActivity,
					"Adicionou cerca",
					Toast.LENGTH_LONG
				).show()
			}
			.addOnFailureListener {
				Toast.makeText(
					this@MainActivity,
					"Falha ao adicionar cerca",
					Toast.LENGTH_LONG
				).show()
			}
	}

	private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
		val builder = GeofencingRequest.Builder()
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
		builder.addGeofence(geofence)
		return builder.build()
	}

	@SuppressLint("MissingPermission")
	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>,
		grantResults: IntArray
	) {
		if (requestCode == LOCATION_PERMISSION_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				locationManager!!.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					1000,
					10f,
					this
				)
			}
		}
	}

	override fun onLocationChanged(location: Location) {
		myLocation = LatLng(
			location.latitude,
			location.longitude
		)
		map?.let {
			it.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM))
			if (ActivityCompat.checkSelfPermission(
					this,
					Manifest.permission.ACCESS_FINE_LOCATION
				) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
					this,
					Manifest.permission.ACCESS_COARSE_LOCATION
				) != PackageManager.PERMISSION_GRANTED
			) {
				return
			}
			it.isMyLocationEnabled = true
		}
	}

	override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

	}

	override fun onProviderEnabled(s: String) {

	}

	override fun onProviderDisabled(s: String) {

	}

	/*
	 * FINGERPRINT
	 * */
	override fun onNoFingerPrintHardwareFound() {

	}

	override fun onNoFingerPrintRegistered() {

	}

	override fun onBelowMarshmallow() {

	}

	override fun onAuthSuccess(cryptoObject: FingerprintManager.CryptoObject) {
		Toast.makeText(this, "Digital", Toast.LENGTH_LONG).show()
	}

	override fun onAuthFailed(errorCode: Int, errorMessage: String) {

	}

	override fun onFingerPrintHardwareFound() {

	}
}