package com.lab.lilytheexplorer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GpsSettings extends AppCompatActivity {
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_gps_settings);

        userName = getIntent().getStringExtra("user_name");

        final EditText latitudeEditText = (EditText) findViewById(R.id.latitudeEditText);
        final EditText longitudeEditText = (EditText) findViewById(R.id.longitudeEditText);
        Button setLocationBtn = (Button) findViewById(R.id.setLocationButton);

        setLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = latitudeEditText.getText().toString().trim();
                String lon = longitudeEditText.getText().toString().trim();

                if(checkValues(lat, lon)) {
                    if(lat.isEmpty() && lon.isEmpty()){
                        Intent mainAppIntent = new Intent(getApplicationContext(), MainApp.class);
                        mainAppIntent.putExtra("user_name", userName);
                        startActivity(mainAppIntent);
                    }else if(lat.isEmpty() || lon.isEmpty()){
                        Toast.makeText(GpsSettings.this, "Please enter the location correctly", Toast.LENGTH_LONG).show();
                    }else{
                        Intent mainAppIntent = new Intent(getApplicationContext(), MainApp.class);
                        mainAppIntent.putExtra("user_name", userName);
                        mainAppIntent.putExtra("lat", lat);
                        mainAppIntent.putExtra("lon", lon);
                        startActivity(mainAppIntent);
                    }
                }else{
                    Toast.makeText(GpsSettings.this, "Please enter the location correctly", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean checkValues(String lat, String lon){
        try{
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lon);
            if(latitude >= -180 && latitude<=180 && longitude >= -90 && longitude<=90)
                return true;
            else
                return false;
        }catch (Exception e){
            return true;
        }

    }
}
