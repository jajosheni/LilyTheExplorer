package com.lab.lilytheexplorer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {
    public String URL;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private RequestQueue queue;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        final int[] myImageList = new int[]{R.drawable.user1, R.drawable.user2, R.drawable.user3, R.drawable.user4};
        i=1;
        final ImageView avatarImageView = (ImageView) findViewById(R.id.avatarImageView);
        avatarImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    i++;
                    i = i % 4;
                    avatarImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, myImageList[i]));
                }
                return true;
            }
        });

        URL = getResources().getString(R.string.URL);
        URL = URL.concat("/api/users/");

        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        final Button loginBtn = (Button) findViewById(R.id.loginButton);
        Button signUpBtn = (Button) findViewById(R.id.signUpButton);

        passwordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginBtn.performClick();
                    return true;
                }
                return false;
            }
        });

        queue = Volley.newRequestQueue(this);


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(signUpIntent);
            }
        });


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
