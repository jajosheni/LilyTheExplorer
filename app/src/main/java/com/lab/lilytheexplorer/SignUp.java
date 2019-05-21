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

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    public String URL = "http://10.0.2.2/api/users/";
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText rePasswordEditText;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up);

        queue = Volley.newRequestQueue(this);
        usernameEditText = (EditText) findViewById(R.id.su_usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.su_passwordEditText);
        rePasswordEditText = (EditText) findViewById(R.id.su_rePasswordEditText);

        Button createAccountBtn = (Button) findViewById(R.id.createAccountBtn);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEditText.getText().toString();
                final String password = passwordEditText.getText().toString();
                final String rePassword = rePasswordEditText.getText().toString();

                if(password.equals(rePassword) && !password.isEmpty() && !username.isEmpty()){
                    StringRequest postRequest = new StringRequest(Request.Method.POST, URL,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    if(response.equals("true")){
                                        Toast.makeText(SignUp.this, "User created!", Toast.LENGTH_LONG).show();
                                        Intent mainAppIntent = new Intent(getApplicationContext(), MainApp.class);
                                        mainAppIntent.putExtra("user_name", username);
                                        startActivity(mainAppIntent);
                                    }else
                                        Toast.makeText(SignUp.this, "Username is taken!", Toast.LENGTH_LONG).show();
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    Log.d("Error.Response", error.toString());
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("user", username);
                            params.put("password", password);

                            return params;
                        }
                    };
                    queue.add(postRequest);
                }else{
                    Toast.makeText(SignUp.this, "Fill in the fields correctly!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
