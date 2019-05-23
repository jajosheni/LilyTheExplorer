package com.lab.lilytheexplorer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainApp extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String URL;
    private Location myLocation;
    private TextView radiusTextView;
    private ListView resultsListView;
    private List<String> resultsList;
    private ArrayAdapter<String> resultsAdapter;
    private SeekBar seekBar;
    private String userName;
    private int radius;
    private Boolean seekbarTouchStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener(){
            public void onLocationChanged(Location location){
                myLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras){
                Log.i("**StatusChanged", provider + status);
            }
            public void onProviderEnabled(String provider){
                Log.i("**Provider Enabled", provider);
            }
            public void onProviderDisabled(String provider){
                Log.i("**Provider Disabled", provider);
            }
        };


        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, locationListener);

        URL = getResources().getString(R.string.URL);
        URL = URL.concat("/api/adverts/");

        userName = getIntent().getStringExtra("user_name");

        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        radiusTextView = (TextView) findViewById(R.id.radiusTextView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchResults(query, radius);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });


        radius = seekBar.getProgress();
        radiusTextView.setText("Radius: " + radius + "m");

        seekbarTouchStarted = false;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress;
                if(radius == 50000)
                    radiusTextView.setText("Radius: ∞");
                else{
                    if(radius/1000 < 1)
                        radiusTextView.setText("Radius: " + radius + "m");
                    else
                        radiusTextView.setText("Radius: " + radius/1000 + "km");
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarTouchStarted = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekbarTouchStarted) {
                    String query = searchView.getQuery().toString();
                    if (!query.isEmpty())
                        fetchResults(query, radius);
                    seekbarTouchStarted = false;
                }
            }
        });

        resultsListView = (ListView) findViewById(R.id.resultsListView);
        resultsList = new ArrayList<String>();
        resultsList.add("Results: ");

        resultsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultsList);
        resultsListView.setAdapter(resultsAdapter);
    }

    public void fetchResults(String query, int distance){
        if(resultsListView.getVisibility() == View.GONE)
            resultsListView.setVisibility(View.VISIBLE);

        String userLocation = myLocation.getLatitude() + ", " + myLocation.getLongitude();
        String url = URL.concat("?userLocation=" + userLocation + "&searchQuery=" + query + "&radius=" + distance);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    resultsList.clear();
                    resultsList.add("Results: ");
                    for(int i = 0; i < response.length() ; i++){
                        JSONObject advert = response.getJSONObject(i);
                        String name = advert.getString("name");
                        String duration = advert.getString("campaignDuration").substring(0, 10).replace("-", "/");
                        String entry = name + "\nExpiring: " + duration;

                        resultsList.add(entry);
                    }
                    resultsListView.setAdapter(resultsAdapter);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.i("ERRRRORRRR", error.toString());
                        Toast.makeText(MainApp.this, "Can't Connect. Are you offline?", Toast.LENGTH_SHORT).show();
                    }
                }
        );

// Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }

        @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setTitle("Lily the Explorer")
                    .setMessage("Are you leaving?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                            System.exit(0);
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_app, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        if (item.getItemId() == R.id.nav_tools) {
            Intent loginIntent = new Intent(getApplicationContext(), AccountSettings.class);
            loginIntent.putExtra("user_name", userName);
            startActivity(loginIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
