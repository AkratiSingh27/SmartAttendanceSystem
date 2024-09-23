package com.maekotech.smartattendancesystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
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

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView cameraPreview;
    private TextView textView;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPreview = findViewById(R.id.cameraPreview);
        textView = findViewById(R.id.textView);

        // Set up the "Contact Us" text with underline
        String contactUsText = " Contact Us";
        SpannableString spannableString = new SpannableString(contactUsText);
        spannableString.setSpan(new UnderlineSpan(), 0, contactUsText.length(), 0);
        textView.setText(spannableString);
        textView.setTextColor(Color.BLUE); // Set text color to blue

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // Set up click listener for the TextView
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Contact Us activity
                Intent intent = new Intent(MainActivity.this, FormActivity.class);
                startActivity(intent);
            }
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

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                if (isScanning) {
                    analyzeImage(image);
                }
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

            // Open the scanned result
            openScannedFile(scanResult);

        } catch (Exception e) {
            Log.e("CameraX", "Scan failed: " + e.getMessage());
        } finally {
            image.close(); // Always close the image
        }
    }

    private void openScannedFile(String scanResult) {
        // Check if the scanned result is a URL
        if (scanResult.startsWith("http://") || scanResult.startsWith("https://")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "Scanned result is not a valid URL: " + scanResult, Toast.LENGTH_SHORT).show();
        }
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
