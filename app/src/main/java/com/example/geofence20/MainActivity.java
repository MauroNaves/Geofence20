package com.example.geofence20;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.example.geofence20.fingerprint.FingerPrintAuthCallback;
import com.example.geofence20.fingerprint.FingerPrintAuthHelper;
import com.example.geofence20.fingerprint.FingerPrintUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener, FingerPrintAuthCallback {

	private int LOCATION_PERMISSION_CODE = 1;

	private LatLng carroLatLng;
	private LatLng casaLatLng;
	private LatLng trabalhoLatLng;
	private GoogleMap map;
	private LocationManager locationManager;
	private static final float DEFAULT_ZOOM = 15f;
	private LatLng myLocation = null;
	private FingerPrintAuthHelper mFingerPrintAuthHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		carroLatLng = new LatLng(-16.678951, -49.244458);
		casaLatLng = new LatLng(-16.681270, -49.258656);
		trabalhoLatLng = new LatLng(-16.685723, -49.254700);

		MapInfoWindowFragment mapInfoWindowFragment = (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
		mapInfoWindowFragment.getMapAsync(this);

		locationManager = (LocationManager)
				getSystemService(Context.LOCATION_SERVICE);
		// Here, thisActivity is the current activity
		if (ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_FINE_LOCATION
		)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(
					this,
					Manifest.permission.ACCESS_FINE_LOCATION
			)) {

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(
						this,
						new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
						LOCATION_PERMISSION_CODE
				);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		} else {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
		}

		if (FingerPrintUtils.isSupportedHardware(getApplicationContext())) {
			mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(getApplicationContext(), this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mFingerPrintAuthHelper != null) {
			mFingerPrintAuthHelper.startAuth();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mFingerPrintAuthHelper != null) {
			mFingerPrintAuthHelper.stopAuth();
		}
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_home) {
			if (myLocation!= null) {
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM));
			}
		} else if (id == R.id.nav_localiza) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(carroLatLng, DEFAULT_ZOOM));
		} else if (id == R.id.nav_casa) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(casaLatLng, DEFAULT_ZOOM));
		} else if (id == R.id.nav_trabalho) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(trabalhoLatLng, DEFAULT_ZOOM));
		}
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		map.addMarker(new MarkerOptions().position(carroLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_carro_marker)));
		map.addMarker(new MarkerOptions().position(casaLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_marker)));
		map.addMarker(new MarkerOptions().position(trabalhoLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_trabalho_marker)));
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(carroLatLng);
		builder.include(casaLatLng);
		builder.include(trabalhoLatLng);
		map.addCircle(new CircleOptions().center(carroLatLng).radius(100).fillColor(Color.GREEN));
		map.addCircle(new CircleOptions().center(casaLatLng).radius(100).fillColor(Color.RED));
		map.addCircle(new CircleOptions().center(trabalhoLatLng).radius(100).fillColor(Color.BLACK));
		int width = getResources().getDisplayMetrics().widthPixels;
		int height = getResources().getDisplayMetrics().heightPixels;
		int padding = (int) (width * 0.10);
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding);
		map.animateCamera(cameraUpdate);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == LOCATION_PERMISSION_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		myLocation = new LatLng(
				location.getLatitude(),
				location.getLongitude()
		);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM));
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		map.setMyLocationEnabled(true);
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	@Override
	public void onProviderEnabled(String s) {

	}

	@Override
	public void onProviderDisabled(String s) {

	}

	/*
	* FINGERPRINT
	* */
	@Override
	public void onNoFingerPrintHardwareFound() {

	}

	@Override
	public void onNoFingerPrintRegistered() {

	}

	@Override
	public void onBelowMarshmallow() {

	}

	@Override
	public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
		Toast.makeText(this, "Digital", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onAuthFailed(int errorCode, String errorMessage) {

	}

	@Override
	public void onFingerPrintHardwareFound() {

	}
}
