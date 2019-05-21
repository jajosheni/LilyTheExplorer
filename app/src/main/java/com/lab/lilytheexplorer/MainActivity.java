package com.lab.lilytheexplorer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public TextView textView;
    public RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        Button signUpBtn = (Button) findViewById(R.id.signUpButton);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(signUpIntent);
            }
        });

        textView = (TextView) findViewById(R.id.headerTextView);
        queue = Volley.newRequestQueue(this);

        Button loginBtn = (Button) findViewById(R.id.loginButton);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate the RequestQueue.

                String url = "http://10.0.2.2/api/users/all";

                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                            @Override
                            public void onResponse(JSONArray response) {
                                textView.setText("Response: " + response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                textView.setText(error.toString());
                            }
                        });

                // Access the RequestQueue through your singleton class.
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);


                /*Intent mainAppIntent = new Intent(getApplicationContext(), MainApp.class);
                startActivity(mainAppIntent);*/
            }
        });
    }
}
