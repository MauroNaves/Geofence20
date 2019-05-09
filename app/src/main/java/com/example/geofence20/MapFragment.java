package com.example.geofence20;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MapFragment extends Fragment implements OnMapReadyCallback {

	private MapInfoWindowFragment mapInfoWindowFragment;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.map_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mapInfoWindowFragment = (MapInfoWindowFragment) getChildFragmentManager().findFragmentById(R.id.mapa);
		mapInfoWindowFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

	}
}
