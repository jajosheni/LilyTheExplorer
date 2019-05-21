package com.lab.lilytheexplorer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AccountSettings extends AppCompatActivity {
    public String URL = "http://10.0.2.2/api/users/";
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText rePasswordEditText;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_account_settings);

        queue = Volley.newRequestQueue(this);
        oldPasswordEditText = (EditText) findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = (EditText) findViewById(R.id.cp_newPasswordEditText);
        rePasswordEditText = (EditText) findViewById(R.id.cp_rePasswordEditText);

        TextView usernameTextView = (TextView) findViewById(R.id.userNameTextView);
        final String userName = getIntent().getStringExtra("user_name");
        usernameTextView.setText(userName);

        Button changePasswordBtn = (Button) findViewById(R.id.changePasswordBtn);
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String oldPassword = oldPasswordEditText.getText().toString();
                final String newPassword = newPasswordEditText.getText().toString();
                final String renewPassword = rePasswordEditText.getText().toString();

                if(newPassword.equals(renewPassword) && !newPassword.isEmpty() && !oldPassword.isEmpty()){
                    StringRequest putRequest = new StringRequest(Request.Method.PUT, URL,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    if(response.equals("true")){
                                        Toast.makeText(AccountSettings.this, "Password Changed!", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                    else
                                        Toast.makeText(AccountSettings.this, "Incorrect credentials", Toast.LENGTH_LONG).show();
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
                            params.put("user", userName);
                            params.put("oldPassword", oldPassword);
                            params.put("newPassword", newPassword);

                            return params;
                        }

                    };

                    queue.add(putRequest);
                }else
                    Toast.makeText(AccountSettings.this, "Fill in the fields correctly!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
