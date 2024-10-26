package com.maekotech.smartattendancesystem;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Device ID", deviceId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.29.18:8080") // Replace with your API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AttendanceApi attendanceApi = retrofit.create(AttendanceApi.class);

        // Fetch employee name first
        fetchEmployeeName(attendanceApi, deviceId);

        // Set up button listeners
        setUpButtonListeners(attendanceApi, deviceId);
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

    private void setUpButtonListeners(AttendanceApi attendanceApi, String deviceId) {
        inButton.setOnClickListener(v -> handleAttendance(attendanceApi, deviceId, "checkin"));
        outButton.setOnClickListener(v -> handleAttendance(attendanceApi, deviceId, "checkout"));
    }

    private void handleAttendance(AttendanceApi attendanceApi, String deviceId, String action) {
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

                    String successMessage = "checkin".equals(action) ? "Checked in successfully!" : "Checked out successfully!";
                    Toast.makeText(EmployeeAttendanceActivity.this, successMessage, Toast.LENGTH_SHORT).show();
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
