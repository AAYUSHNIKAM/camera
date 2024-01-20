package com.example.healthcare5;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.camera.view.PreviewView;


import com.google.common.util.concurrent.ListenableFuture;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;


import com.google.common.util.concurrent.ListenableFuture;


public class two extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ExecutorService cameraExecutor;

    private PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION
            );
        }

        // Other initialization code...

        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(view -> takePhoto());
    }

    private void startCamera() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(640, 480))
                .build();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        ProcessCameraProvider cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));







    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void takePhoto() {
        ImageCapture imageCapture = new ImageCapture.Builder().build();

        File photoFile = createFile(getOutputDirectory(this), "JPEG_", ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String msg = "Photo captured successfully: " + photoFile.getAbsolutePath();
                        Toast.makeText(two.this, msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraActivity", "Photo capture failed: " + exception.getMessage(), exception);
                    }
                }
        );
    }

    private File getOutputDirectory(Context context) {
        File mediaDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "CameraX"
        );

        if (!mediaDir.exists() && !mediaDir.mkdirs()) {
            Log.e("CameraActivity", "Failed to create directory");
        }

        return mediaDir;
    }

    private File createFile(File baseFolder, String format, String extension) {
        return new File(
                baseFolder,
                new SimpleDateFormat(format, Locale.US)
                        .format(System.currentTimeMillis()) + extension
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
