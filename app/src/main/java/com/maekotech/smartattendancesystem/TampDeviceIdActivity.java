package com.maekotech.smartattendancesystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.os.Bundle;
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

public class TampDeviceIdActivity extends AppCompatActivity {

    private EditText deviceNameEditText;
    private Button submitButton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tamp_device_id); // Adjust to your actual layout file name

        deviceNameEditText = findViewById(R.id.deviceNameEditText);
        submitButton = findViewById(R.id.submitButton);

        requestQueue = Volley.newRequestQueue(this);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDeviceId();
            }
        });
    }

    private void submitDeviceId() {
        String deviceId = deviceNameEditText.getText().toString().trim();

        if (deviceId.isEmpty()) {
            Toast.makeText(this, "Please enter a device ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.29.18:8080/DeviceID-information/"; // Replace with your API URL

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(TampDeviceIdActivity.this, "Device ID submitted successfully!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TampDeviceIdActivity.this, "Error submitting device ID: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}
