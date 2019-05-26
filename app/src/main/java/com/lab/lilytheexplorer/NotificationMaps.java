package com.lab.lilytheexplorer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.util.ArrayList;

public class NotificationMaps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<String> locations;
    private LatLng myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locations = getIntent().getStringArrayListExtra("locations");
        String[] coords = getIntent().getStringExtra("myCoordinates").split(",");
        myLocation = new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for (int i = 0; i < locations.size(); i++) {
            String[] parts = locations.get(i).split(":");
            String name = parts[0];
            String location = parts[1].replace(" ", "");
            location = location.replace("[", "");
            location = location.replace("]", "");
            String[] coords = location.split(",");

            Double lat = Double.parseDouble(coords[0]);
            Double lon = Double.parseDouble(coords[1]);

            LatLng item = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(item).title(name));
        }

        mMap.addCircle(new CircleOptions()
                .center(myLocation)
                .radius(500) //m
                .strokeColor(Color.argb(5, 255, 255, 0))
                .fillColor(Color.argb(20, 0, 136, 255)));

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.0f)); // zoom Level
    }
        @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(getApplicationContext(), MainApp.class));
    }
}
