package com.lab.lilytheexplorer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {
    public String URL = "http://10.0.2.2/api/users/";
    private EditText usernameEditText;
    private EditText passwordEditText;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        queue = Volley.newRequestQueue(this);

        Button signUpBtn = (Button) findViewById(R.id.signUpButton);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(signUpIntent);
            }
        });

        Button loginBtn = (Button) findViewById(R.id.loginButton);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = usernameEditText.getText().toString();
                final String passWord = passwordEditText.getText().toString();
                if(userName.isEmpty() || passWord.isEmpty())
                    Toast.makeText(MainActivity.this, "Please enter your credentials!", Toast.LENGTH_SHORT).show();
                else
                    try {
                        String url = URL + "?user=" + userName + "&password=" + passWord;

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if(response.contains("true")){
                                            Intent mainAppIntent = new Intent(getApplicationContext(), MainApp.class);
                                            mainAppIntent.putExtra("user_name", userName);
                                            startActivity(mainAppIntent);
                                        }else
                                            Toast.makeText(MainActivity.this, "Incorrect credentials!", Toast.LENGTH_SHORT).show();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("ERROR", error.toString());
                            }
                        });

                        // Add the request to the RequestQueue.
                        queue.add(stringRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }
}
