package com.example.geofence20;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class LocationAlertIntentService extends IntentService {

	private static final String IDENTIFIER = "LocationAlertIS";

	public LocationAlertIntentService() {
		super(IDENTIFIER);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

		if (geofencingEvent.hasError()) {
			Log.e(IDENTIFIER, "erro");
		}

		String requestId = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();

		Log.i(requestId, geofencingEvent.toString());

		int geofenceTransition = geofencingEvent.getGeofenceTransition();

		if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
				geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
			//setar no firebase
			Toast.makeText(getApplicationContext(), "Entrou na cerca", Toast.LENGTH_LONG).show();
		} else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
			//setar no firebase
			Toast.makeText(getApplicationContext(), "Saiu da cerca", Toast.LENGTH_LONG).show();
		}
	}
}
