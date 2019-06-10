package com.example.geofence20

import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.FirebaseDatabase
import android.support.v4.content.LocalBroadcastManager



class LocationAlertIntentService : IntentService(IDENTIFIER) {

	companion object {
		private val IDENTIFIER = "LocationAlertIS"
	}

	override fun onHandleIntent(intent: Intent?) {
		val geofencingEvent = GeofencingEvent.fromIntent(intent)

		if (geofencingEvent.hasError()) {
			Log.e(IDENTIFIER, "erro")
		}

		val requestId = geofencingEvent.triggeringGeofences[0].requestId

		Log.i(requestId, geofencingEvent.toString())

		val geofenceTransition = geofencingEvent.geofenceTransition

		val db = FirebaseDatabase.getInstance().reference

		if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
			db.child(requestId).setValue(1)
			Toast.makeText(applicationContext, "Entrou na cerca", Toast.LENGTH_LONG).show()
			if (requestId == "validacaoCasa") {
				db.child("casaDigital").setValue(0)
				val msg = Intent(MainActivity.FILTRO_KEY)
				msg.putExtra(MainActivity.MENSAGEM_KEY, true)
				LocalBroadcastManager.getInstance(this).sendBroadcast(msg)
			}
		} else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
			db.child(requestId).setValue(0)
			Toast.makeText(applicationContext, "Saiu da cerca", Toast.LENGTH_LONG).show()
		}
	}

}
