package com.example.geofence20.model

import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

abstract class MapMarker(var latLng: LatLng) {
	abstract fun getMarkerOptions(): MarkerOptions
	abstract fun getCircleOptions(): CircleOptions
	abstract fun getGeofence(): Geofence
	abstract fun getKey(): String
}