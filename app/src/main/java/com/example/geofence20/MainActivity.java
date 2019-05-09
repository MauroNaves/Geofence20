package com.example.geofence20;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
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
		implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

	private LatLng carroLatLng;
	private LatLng casaLatLng;
	private LatLng trabalhoLatLng;
	private GoogleMap map;

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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings ("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_home) {

		} else if (id == R.id.nav_localiza) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(carroLatLng, 15));
		} else if (id == R.id.nav_casa) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(casaLatLng, 15));
		} else if (id == R.id.nav_trabalho) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(trabalhoLatLng, 15));
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
}
