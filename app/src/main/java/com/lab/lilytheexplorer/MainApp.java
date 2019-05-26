package com.lab.lilytheexplorer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
import android.widget.AdapterView;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainApp extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String CHANNEL_ID = "lily2019";
    private ArrayList<String> oldList;
    private ArrayList<String> newList;
    private Boolean firstRun = true;

    private String URL;
    private String userName;

    private Boolean GPS;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Double latLocation;
    private Double lonLocation;
    private int radius;

    private TextView radiusTextView;
    private ListView resultsListView;

    private List<String> resultsList;
    private List<String> resultsLONGID;
    private ArrayAdapter<String> resultsAdapter;

    private Boolean seekbarTouchStarted;
    private int BackgroundTimer = 10000; //ms


    private void getLocationPermission(){
        if ( ContextCompat.checkSelfPermission(MainApp.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions(MainApp.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, BackgroundTimer, 0, locationListener);

        if ( ContextCompat.checkSelfPermission(MainApp.this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions(MainApp.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        else
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, BackgroundTimer, 0, locationListener);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

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
        createNotificationChannel();

        userName = getIntent().getStringExtra("user_name");

        try{
            GPS = false;
            String lat = getIntent().getStringExtra("lat");
            String lon = getIntent().getStringExtra("lon");
            if(!lat.isEmpty() && !lon.isEmpty()){
                latLocation = Double.parseDouble(lat);
                lonLocation = Double.parseDouble(lon);
                GPS = true;
            }
        } catch (Exception e){
            GPS = false;
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener(){
            public void onLocationChanged(Location location){ //This is called every 5 seconds
                if(!GPS){
                    latLocation = location.getLatitude();
                    lonLocation = location.getLongitude();
                }
                checkForNewCampaigns();
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

        getLocationPermission();

        URL = getResources().getString(R.string.URL);
        URL = URL.concat("/api/adverts/");

        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
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
        resultsLONGID = new ArrayList<String>();

        resultsList.add("Results: ");
        resultsLONGID.add("Results: ");

        resultsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultsList);
        resultsListView.setAdapter(resultsAdapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    Intent showAdvertIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    showAdvertIntent.putExtra("_id", resultsLONGID.get(position));
                    startActivity(showAdvertIntent);
                }
            }
        });
        oldList = new ArrayList<String>();
        newList = new ArrayList<String>();
    }

    private void checkForNewCampaigns() {
        if(latLocation!=null && lonLocation!=null){
            String userLocation = latLocation + ", " + lonLocation;
            String url = URL.concat("?userLocation=" + userLocation + "&searchQuery=" + "&radius=" + 500); // get campaigns under 500m radius

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try{
                        newList.clear();
                        for(int i = 0; i < response.length() ; i++){
                            JSONObject advert = response.getJSONObject(i);
                            String name = advert.getString("name");
                            String coordinates = (advert.getJSONObject("location").getJSONArray("coordinates")).toString();
                            String entry = name + ":" + coordinates;
                            if(i==0)
                                newList.add("My Location:["+latLocation+","+lonLocation+"]");
                            newList.add(entry);
                        }

                        if(firstRun){
                            oldList.clear();
                            for (String item : newList) oldList.add(item);
                            firstRun = false;
                        }else{
                            if(!oldList.equals(newList)){
                                showNotification(newList, new Random().nextInt());
                                oldList.clear();
                                for (String item : newList) oldList.add(item);
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error){
                            Log.i("Error", error.toString());
                        }
                    });

            requestQueue.add(jsonArrayRequest);
        }
    }

    private void showNotification(ArrayList<String> locations, int ID) {
        Toast.makeText(getApplicationContext(), "LOCATIONS: " + locations.size(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, NotificationMaps.class);
        intent.putExtra("locations", locations);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("New campaigns nearby!")
                .setContentText("Tap this notification to see more!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(ID, builder.build());
    }

    private void fetchResults(String query, int distance){
        if(resultsListView.getVisibility() == View.GONE)
            resultsListView.setVisibility(View.VISIBLE);

        if(latLocation==null || lonLocation==null){
            Toast.makeText(MainApp.this, "Getting GPS Location, please try again...", Toast.LENGTH_LONG).show();
            getLocationPermission();
        }else{
            String userLocation = latLocation + ", " + lonLocation;
            String url = URL.concat("?userLocation=" + userLocation + "&searchQuery=" + query + "&radius=" + distance);

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try{
                        resultsList.clear();
                        resultsLONGID.clear();
                        resultsList.add("Results: ");
                        resultsLONGID.add("Results: ");
                        for(int i = 0; i < response.length() ; i++){
                            JSONObject advert = response.getJSONObject(i);
                            String _id = advert.getString("_id");
                            String name = advert.getString("name");
                            String duration = advert.getString("campaignDuration").substring(0, 10).replace("-", "/");
                            String entry = name + "\nExpiring: " + duration;

                            resultsList.add(entry);
                            resultsLONGID.add(_id);
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
            Intent changePassIntent = new Intent(getApplicationContext(), AccountSettings.class);
            changePassIntent.putExtra("user_name", userName);
            startActivity(changePassIntent);
        }

        if (item.getItemId() == R.id.gps_tools) {
            Intent gpsIntent = new Intent(getApplicationContext(), GpsSettings.class);
            gpsIntent.putExtra("user_name", userName);
            startActivity(gpsIntent);
        }

        if (item.getItemId() == R.id.log_tools) {
            finish();
            Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(loginIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
