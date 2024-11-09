package com.maekotech.smartattendancesystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class EmployeeAttendanceActivity extends AppCompatActivity {

    private TextView employeeName, alfabet;
    private Button inButton, outButton;
    private boolean isCheckedIn = false; // Track check-in status
    private boolean isCheckedOut = false; // Track check-out status
    private String currentDate; // To store the current date for checking in/out

    private SharedPreferences sharedPreferences;

    interface AttendanceApi {
        @GET("/employee-actions/{deviceId}")
        Call<EmployeeNameResponse> getEmployeeName(@Path("deviceId") String deviceId);

        @POST("/employee-actions/saveAction")
        Call<EmployeeActionResponse> handleAttendance(@Body AttendanceRequest attendanceRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_attendance);

        employeeName = findViewById(R.id.employeeName);
        alfabet = findViewById(R.id.alfabet);
        inButton = findViewById(R.id.inbutton);
        outButton = findViewById(R.id.outbutton);

        sharedPreferences = getSharedPreferences("EmployeeAttendancePrefs", MODE_PRIVATE);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Device ID", deviceId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.29.18:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AttendanceApi attendanceApi = retrofit.create(AttendanceApi.class);

        // Fetch employee name first
        fetchEmployeeName(attendanceApi, deviceId);

        // Get current date
        currentDate = getCurrentDate();

        // Retrieve saved attendance state
        retrieveAttendanceState();

        // Set up button listeners
        setUpButtonListeners(attendanceApi, deviceId);
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date()); // Get the current date in "yyyy-MM-dd" format
    }

    private void fetchEmployeeName(AttendanceApi attendanceApi, String deviceId) {
        attendanceApi.getEmployeeName(deviceId).enqueue(new Callback<EmployeeNameResponse>() {
            @Override
            public void onResponse(Call<EmployeeNameResponse> call, Response<EmployeeNameResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String userName = response.body().getUserName();
                    employeeName.setText(userName);
                    setInitials(userName);
                } else {
                    Toast.makeText(EmployeeAttendanceActivity.this, "Failed to get employee name: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EmployeeNameResponse> call, Throwable t) {
                Toast.makeText(EmployeeAttendanceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setInitials(String userName) {
        if (userName != null && !userName.isEmpty()) {
            String[] nameParts = userName.split(" ");
            String firstInitial = nameParts.length > 0 ? String.valueOf(nameParts[0].charAt(0)).toUpperCase() : "";
            String lastInitial = nameParts.length > 1 ? String.valueOf(nameParts[nameParts.length - 1].charAt(0)).toUpperCase() : "";
            alfabet.setText(firstInitial + lastInitial);
        } else {
            alfabet.setText(""); // Clear if name is empty
        }
    }

    private void retrieveAttendanceState() {
        // Retrieve the saved check-in and check-out status from SharedPreferences
        String savedDate = sharedPreferences.getString("currentDate", "");
        isCheckedIn = sharedPreferences.getBoolean("isCheckedIn", false);
        isCheckedOut = sharedPreferences.getBoolean("isCheckedOut", false);

        // If today's date is different from the saved date, reset the status
        if (!currentDate.equals(savedDate)) {
            resetAttendanceState();
        }
    }

    private void resetAttendanceState() {
        // Reset check-in and check-out flags if the date has changed
        isCheckedIn = false;
        isCheckedOut = false;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isCheckedIn", false);
        editor.putBoolean("isCheckedOut", false);
        editor.putString("currentDate", currentDate); // Save the current date
        editor.apply();
    }

    private void saveAttendanceState() {
        // Save the current check-in/check-out state to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentDate", currentDate);
        editor.putBoolean("isCheckedIn", isCheckedIn);
        editor.putBoolean("isCheckedOut", isCheckedOut);
        editor.apply();
    }

    private void setUpButtonListeners(AttendanceApi attendanceApi, String deviceId) {
        inButton.setOnClickListener(v -> handleAttendance(attendanceApi, deviceId, "checkin"));
        outButton.setOnClickListener(v -> handleAttendance(attendanceApi, deviceId, "checkout"));
    }

    private void handleAttendance(AttendanceApi attendanceApi, String deviceId, String action) {
        // Check if check-in/check-out can proceed based on actions today
        if ("checkin".equals(action)) {
            if (isCheckedIn) {
                Toast.makeText(EmployeeAttendanceActivity.this, "You have already checked in today!", Toast.LENGTH_SHORT).show();
                return; // Prevent multiple check-ins in one day
            }
        } else if ("checkout".equals(action)) {
            if (isCheckedOut) {
                Toast.makeText(EmployeeAttendanceActivity.this, "You have already checked out today!", Toast.LENGTH_SHORT).show();
                return; // Prevent multiple check-outs in one day
            }
            if (!isCheckedIn) {
                Toast.makeText(EmployeeAttendanceActivity.this, "Please check in first!", Toast.LENGTH_SHORT).show();
                return; // Prevent checkout if not checked in
            }
        }

        AttendanceRequest attendanceRequest = new AttendanceRequest(deviceId, action);

        attendanceApi.handleAttendance(attendanceRequest).enqueue(new Callback<EmployeeActionResponse>() {
            @Override
            public void onResponse(Call<EmployeeActionResponse> call, Response<EmployeeActionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeActionResponse actionResponse = response.body();
                    String userName = actionResponse.getUserName();

                    // Set employee name
                    employeeName.setText(userName);

                    // Extract initials
                    setInitials(userName);

                    // Handle success message and update actions
                    if ("checkin".equals(action)) {
                        isCheckedIn = true;
                        Toast.makeText(EmployeeAttendanceActivity.this, "Checked in successfully!", Toast.LENGTH_SHORT).show();
                    } else if ("checkout".equals(action)) {
                        isCheckedOut = true;
                        Toast.makeText(EmployeeAttendanceActivity.this, "Checked out successfully!", Toast.LENGTH_SHORT).show();
                    }

                    // Save the updated attendance state to SharedPreferences
                    saveAttendanceState();
                } else {
                    Toast.makeText(EmployeeAttendanceActivity.this, "Attendance action failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EmployeeActionResponse> call, Throwable t) {
                Toast.makeText(EmployeeAttendanceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class AttendanceRequest {
        private String deviceId;
        private String action;

        public AttendanceRequest(String deviceId, String action) {
            this.deviceId = deviceId;
            this.action = action;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getAction() {
            return action;
        }
    }

    public static class EmployeeNameResponse {
        private String userName;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    public static class EmployeeActionResponse {
        private Long id;
        private String deviceId;
        private String action;
        private String actionTime;
        private String userName;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getActionTime() {
            return actionTime;
        }

        public void setActionTime(String actionTime) {
            this.actionTime = actionTime;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}
