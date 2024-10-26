package com.maekotech.smartattendancesystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import Handler
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class TampDeviceIdActivity extends AppCompatActivity {

    private EditText deviceNameEditText;
    private Button submitButton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tamp_device_id); // Ensure this matches your actual layout file name

        deviceNameEditText = findViewById(R.id.deviceNameEditText);
        submitButton = findViewById(R.id.submitButton);

        // Initialize the RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Set up the button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDeviceInfo();
            }
        });
    }

    private void submitDeviceInfo() {
        String name = deviceNameEditText.getText().toString().trim();

        // Check if the input is empty
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the Android device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DeviceID", "Retrieved Device ID: " + deviceId);

        // Check if the device ID is valid
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "Device ID is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.29.18:8080/DeviceID-information/save"; // API URL

        // Create the JSON object to send
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", 14); // Adjust this ID as needed
            jsonObject.put("deviceId", deviceId); // Use Android ID as device ID
            jsonObject.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON object.", Toast.LENGTH_SHORT).show();
            return; // Exit if JSON creation fails
        }

        Log.d("JSONPayload", jsonObject.toString());

        // Create a JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Show success message
                        Toast.makeText(TampDeviceIdActivity.this, "Device info submitted successfully!", Toast.LENGTH_SHORT).show();

                        // Delay the transition to MainActivity
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(TampDeviceIdActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Optional: finish current activity
                            }
                        }, 2000); // Delay for 2 seconds (2000 milliseconds)
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error submitting device info: " + error.getMessage();
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorData = new String(error.networkResponse.data);
                            errorMessage += "\nServer Response: " + errorData;
                        }
                        Toast.makeText(TampDeviceIdActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("VolleyError", errorMessage);
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
}
