package com.example.geofence20

import android.Manifest
import android.annotation.SuppressLint
import android.app.IntentService
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment
import com.example.geofence20.fingerprint.FingerPrintAuthCallback
import com.example.geofence20.fingerprint.FingerPrintAuthHelper
import com.example.geofence20.fingerprint.FingerPrintUtils
import com.example.geofence20.model.Casa
import com.example.geofence20.model.MapMarker
import com.example.geofence20.model.MapMarkerFactory
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
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
		val FILTRO_KEY = "ServiceTest_KEY"
		val MENSAGEM_KEY = "ServiceTest_MENSAGEM_KEY"
	}

	private val LOCATION_PERMISSION_CODE = 1
	private var codigoSelecionado = 0

	private var map: GoogleMap? = null
	private var locationManager: LocationManager? = null
	private var myLocation: LatLng? = null
	private var firstLocation = true
	private var carroLocation: LatLng? = null
	private var mFingerPrintAuthHelper: FingerPrintAuthHelper? = null
	private var mGeofencingClient: GeofencingClient? = null
	private var factory: MapMarkerFactory? = null
	private var carroPareado: Boolean = false
	private var broadcast: LocalBroadcastServiceTest? = null

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
			if (ActivityCompat.shouldShowRequestPermissionRationale(
					this,
					Manifest.permission.ACCESS_FINE_LOCATION
				)
			) {

			} else {
				ActivityCompat.requestPermissions(
					this,
					arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
					LOCATION_PERMISSION_CODE
				)
			}
		} else {
			locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
		}

		initFingerPrint()

		registerReceiver()

		mGeofencingClient = LocationServices.getGeofencingClient(this)

		broadcast = LocalBroadcastServiceTest()
        val intentFilter = IntentFilter(FILTRO_KEY)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcast!!, intentFilter)
	}

	private fun registerReceiver() {
		val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
		registerReceiver(mReceiver, filter)
	}

	private fun initFingerPrint() {
		if (FingerPrintUtils.isSupportedHardware(applicationContext)) {
			mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(applicationContext, this)
		}
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

	override fun onDestroy() {
		super.onDestroy()
		unregisterReceiver(mReceiver)
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcast!!)
		locationManager?.removeUpdates(this)
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
		desenharCarro()
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
			R.id.nav_home -> centralizarLocalizacaoAtual()
			R.id.nav_localiza -> localizarCarro()
			R.id.nav_casa -> casaAction()
			R.id.nav_trabalho -> trabalhoAction()
		}
		drawer_layout.closeDrawer(GravityCompat.START)
		return true
	}

	private fun centralizarLocalizacaoAtual() {
		if (myLocation != null)
			map?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM))
	}

	private fun localizarCarro() {
		if (carroLocation != null)
			map?.animateCamera(
				CameraUpdateFactory.newLatLngZoom(
					carroLocation,
					DEFAULT_ZOOM
				)
			)
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

	private fun desenharCarro() {
		if (!carroPareado) {
			return
		}
		if (carroLocation == null) {
			return
		}
		map?.addMarker(
			MarkerOptions().position(carroLocation!!).icon(
				BitmapDescriptorFactory.fromResource(
					R.drawable.ic_carro_marker
				)
			)
		)
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
					0,
					0f,
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
			if (firstLocation) {
				it.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM))
				firstLocation = false
			}
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
			if (carroPareado) {
				carroLocation = LatLng(
					location.latitude,
					location.longitude
				)
				rePopularMarkers()
			}
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
		val casa = factory?.casa
		if (casa != null && (casa as Casa).dentroCerca) {
			val db = FirebaseDatabase.getInstance().reference
			db.child("casaDigital").setValue(1)
			Toast.makeText(this, "Abriu a porta", Toast.LENGTH_LONG).show()
		} else {
			Toast.makeText(this, "É necessário estár dentro da cerca da casa.", Toast.LENGTH_LONG).show()
		}
		mFingerPrintAuthHelper?.startAuth()
	}

	override fun onAuthFailed(errorCode: Int, errorMessage: String) {

	}

	override fun onFingerPrintHardwareFound() {

	}

	private val mReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val action = intent?.action
			if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
				val mDevice =
					intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
				when (mDevice.bondState) {
					BluetoothDevice.BOND_BONDED -> {
						Log.e("BroadcastReceiver", "BONDED")
						carroPareado = true
					}
					BluetoothDevice.BOND_BONDING -> {
						Log.e("BroadcastReceiver", "BONDING")
					}
					BluetoothDevice.BOND_NONE -> {
						Log.e("BroadcastReceiver", "NONE")
						carroPareado = false
					}
				}
			}
		}
	}

	inner class LocalBroadcastServiceTest : BroadcastReceiver() {

		override fun onReceive(context: Context, intent: Intent) {
			val status = intent.getBooleanExtra(MENSAGEM_KEY, false)
			factory?.casa?.let {
				(it as Casa).dentroCerca = status
			}
		}
	}
}
