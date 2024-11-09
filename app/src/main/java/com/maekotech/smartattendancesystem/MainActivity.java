package com.maekotech.smartattendancesystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeCallback;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private BarcodeView barcodeView;
    private TextView textView;
    private boolean isScanning = false;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String CHECK_DEVICE_ID_URL = "http://192.168.29.18:8080/employee-actions/%s";
    private static final String TARGET_URL = "https://www.smartattendancesystem.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barcodeView = findViewById(R.id.barcode_scanner);
        textView = findViewById(R.id.textView6);

        setupContactUsText();
        requestPermissionsIfNeeded();
        setOnClickListener();
    }

    private void setupContactUsText() {
        String contactUsText = " Facing Any Issues?\nContact Us";
        SpannableString spannableString = new SpannableString(contactUsText);
        spannableString.setSpan(new UnderlineSpan(), 0, contactUsText.length(), 0);
        textView.setText(spannableString);
        textView.setTextColor(Color.BLUE);
    }

    private void requestPermissionsIfNeeded() {
        if (allPermissionsGranted()) {
            startScanner();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void setOnClickListener() {
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormActivity.class);
            startActivity(intent);
        });
    }

    private void startScanner() {
        if (barcodeView != null) {
            barcodeView.decodeContinuous(new BarcodeCallback() {
                @Override
                public void barcodeResult(BarcodeResult result) {
                    // This method will be called when a barcode is scanned
                    if (isScanning) {
                        String scanResult = result.getText();

                        // Check if the scan result matches the specific URL
                        if (scanResult.equals(TARGET_URL)) {
                            Toast.makeText(MainActivity.this, "Scan Result: " + scanResult, Toast.LENGTH_SHORT).show();

                            // After a successful scan, check Device ID
                            checkDeviceIdWithServer(scanResult);
                            isScanning = false; // Stop further scans
                        } else {
                            // If the scanned result doesn't match the URL, ignore and continue scanning
                            Log.d("Scanner", "Scanned result is not the target URL. Continuing to scan...");
                        }
                    }
                }

                @Override
                public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                    // Handle possible result points if needed (optional)
                }
            });

            // No need for setStatusText(), just resume scanning
            barcodeView.resume(); // Start the scanning process
            isScanning = true;
        }
    }

    private void checkDeviceIdWithServer(String scanResult) {
        String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        String url = String.format(CHECK_DEVICE_ID_URL, deviceId); // Insert Device ID into the URL

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url) // Use the formatted URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("NetworkError", "Failed to check Device ID: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error connecting to server.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, TampDeviceIdActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Assuming the server responds with a valid confirmation
                    Log.d("ServerResponse", "Device ID is valid.");
                    runOnUiThread(() -> openEmployeeAttendanceActivity());
                } else {
                    Log.e("ServerError", "Invalid Device ID: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Device ID is invalid. Redirecting.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, TampDeviceIdActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }

    private void openEmployeeAttendanceActivity() {
        Intent intent = new Intent(MainActivity.this, EmployeeAttendanceActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startScanner(); // Permission granted, start the scanner
            } else {
                // Check if the user has denied the permission permanently (clicked "Don't ask again")
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    // The user has denied the permission, but not permanently (they can still allow it)
                    Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                } else {
                    // The user has denied the permission permanently, prompt them to go to settings
                    Toast.makeText(this, "Camera permission is required for this app. Please enable it in settings.", Toast.LENGTH_LONG).show();
                    openAppSettings();
                }
                finish(); // Close the activity if permissions are not granted
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause(); // Pause the scanner when the activity is paused
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume(); // Resume the scanner when the activity is resumed
    }
}
