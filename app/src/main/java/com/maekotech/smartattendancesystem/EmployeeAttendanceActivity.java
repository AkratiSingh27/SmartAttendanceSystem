package com.maekotech.smartattendancesystem;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class EmployeeAttendanceActivity extends AppCompatActivity {

    private TextView employeeName;
    private ImageView employeeImage;
    private Button inButton;
    private Button outButton;

    // Retrofit API endpoint interface
    interface AttendanceApi {
        @POST("/employee-actions/")
        Call<ResponseBody> handleAttendance(@Body AttendanceRequest attendanceRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_attendance);

        // Initialize UI components
        employeeName = findViewById(R.id.employeeName);
        employeeImage = findViewById(R.id.employeeImage);
        inButton = findViewById(R.id.inButton);
        outButton = findViewById(R.id.outButton);

        // Retrofit setup with correct base URL
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.29.18:8080") // Adjust based on your server
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Creating the API instance
        AttendanceApi attendanceApi = retrofit.create(AttendanceApi.class);

        // Get employee details from Intent
        Long employeeId = getIntent().getLongExtra("EMPLOYEE_ID", 1L); // Change from DEVICE_ID to EMPLOYEE_ID
        String employeeDisplayName = getIntent().getStringExtra("EMPLOYEE_NAME");
        employeeName.setText(employeeDisplayName != null ? employeeDisplayName : "Employee Name");

        // Set up button listeners
        setUpButtonListeners(attendanceApi, employeeId);
    }

    private void setUpButtonListeners(AttendanceApi attendanceApi, Long employeeId) {
        inButton.setOnClickListener(v -> {
            handleAttendance(attendanceApi, employeeId, "checkin");
        });

        outButton.setOnClickListener(v -> {
            handleAttendance(attendanceApi, employeeId, "checkout");
        });
    }

    private void handleAttendance(AttendanceApi attendanceApi, Long employeeId, String action) {
        AttendanceRequest attendanceRequest = new AttendanceRequest(employeeId, action);
        attendanceApi.handleAttendance(attendanceRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Read the raw response
                        String responseBody = response.body().string();
                        Log.d("API Response", responseBody);

                        // Handle response based on action
                        String successMessage = "checkin".equals(action) ? "Checked in successfully!" : "Checked out successfully!";
                        Toast.makeText(EmployeeAttendanceActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("API Error", "Error parsing response", e);
                        Toast.makeText(EmployeeAttendanceActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EmployeeAttendanceActivity.this, "Attendance action failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EmployeeAttendanceActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // AttendanceRequest class definition
    public static class AttendanceRequest {
        private Long employeeId; // Changed from deviceId to employeeId
        private String action;

        public AttendanceRequest(Long employeeId, String action) {
            this.employeeId = employeeId; // Store employee ID
            this.action = action;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public String getAction() {
            return action;
        }
    }
}
