package com.example.geofence20.model

import com.google.android.gms.maps.model.LatLng

class MapMarkerFactory {

	var casa: MapMarker? = null
	var trabalho: MapMarker? = null

	fun getMapMarker(codigo: Int, latLng: LatLng): MapMarker? {
		return when (codigo) {
			1 -> {
				return criarCasa(latLng)
			}
			2 -> {
				return criarTrabalho(latLng)
			}
			else -> null
		}
	}

	private fun criarTrabalho(latLng: LatLng): MapMarker? {
		if (trabalho == null) {
			trabalho = Trabalho(latLng)
		}
		return trabalho
	}

	private fun criarCasa(latLng: LatLng): MapMarker? {
		if (casa == null) {
			casa = Casa(latLng)
		}
		return casa
	}
}