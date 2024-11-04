package com.maekotech.smartattendancesystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.LuminanceSource;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private PreviewView cameraPreview;
    private TextView textView;
    private boolean isScanning = false;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String CHECK_DEVICE_ID_URL = "http://192.168.29.18:8080/employee-actions/%s"; // Placeholder for Device ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPreview = findViewById(R.id.cameraPreview);
        textView = findViewById(R.id.textView);

        setupContactUsText();
        requestPermissionsIfNeeded();
        setOnClickListener();
    }

    private void setupContactUsText() {
        String contactUsText = " Contact Us";
        SpannableString spannableString = new SpannableString(contactUsText);
        spannableString.setSpan(new UnderlineSpan(), 0, contactUsText.length(), 0);
        textView.setText(spannableString);
        textView.setTextColor(Color.BLUE);
    }

    private void requestPermissionsIfNeeded() {
        if (allPermissionsGranted()) {
            startCamera();
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

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);
                startImageAnalysis(cameraProvider, cameraSelector);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startImageAnalysis(ProcessCameraProvider cameraProvider, CameraSelector cameraSelector) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> {
            if (isScanning) {
                analyzeImage(image);
            }
        });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
        isScanning = true; // Start automatic scanning
    }

    private void analyzeImage(ImageProxy image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            image.close();
            return; // Only process YUV_420_888 images
        }

        // Get the image data
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        // Decode the image to a BinaryBitmap
        LuminanceSource source = new MyLuminanceSource(data, image.getWidth(), image.getHeight());
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            String scanResult = result.getText();
            Toast.makeText(this, "Scan Result: " + scanResult, Toast.LENGTH_SHORT).show();

            // After a successful scan, check Device ID
            checkDeviceIdWithServer(scanResult);
            isScanning = false; // Stop further scans

        } catch (Exception e) {
            Log.e("CameraX", "Scan failed: " + e.getMessage());
        } finally {
            image.close(); // Always close the image
        }
    }

    private void checkDeviceIdWithServer(String scanResult) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
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
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
