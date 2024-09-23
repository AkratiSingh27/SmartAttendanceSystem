package com.maekotech.smartattendancesystem;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EmployeeAttendance extends AppCompatActivity {

    private Button inButton;
    private Button outButton;
    private ImageView employeeImage;
    private TextView employeeName;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_attendance); // Make sure this matches your XML filename

        // Initialize views
        inButton = findViewById(R.id.inButton);
        outButton = findViewById(R.id.outButton);
        employeeImage = findViewById(R.id.employeeImage);
        employeeName = findViewById(R.id.employeeName);
        welcomeText = findViewById(R.id.textView4);

        // Set up button click listeners
        inButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "In" button click
                Toast.makeText(EmployeeAttendance.this, "Checked In", Toast.LENGTH_SHORT).show();
            }
        });

        outButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "Out" button click
                Toast.makeText(EmployeeAttendance.this, "Checked Out", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
