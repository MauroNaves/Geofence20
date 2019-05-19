package com.example.geofence20.model

import android.graphics.Color
import com.example.geofence20.MainActivity
import com.example.geofence20.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Trabalho(latLng: LatLng) : MapMarker(latLng) {
	override fun getKey(): String {
		return "TRABALHO_GEOFENCE"
	}

	override fun getMarkerOptions(): MarkerOptions {
		return MarkerOptions().position(latLng)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_trabalho_marker))
	}

	override fun getCircleOptions(): CircleOptions {
		return CircleOptions().center(latLng).radius(100.0).fillColor(Color.BLACK)
	}

	override fun getGeofence(): Geofence {
		return Geofence.Builder()
			.setRequestId(getKey())
			.setCircularRegion(latLng.latitude, latLng.longitude, MainActivity.GEOFENCE_RADIUS)
			.setExpirationDuration(Geofence.NEVER_EXPIRE)
			.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
			.setLoiteringDelay(10000)
			.build()
	}
}
