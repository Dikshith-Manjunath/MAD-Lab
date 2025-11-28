package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.utils.IngredientLabelMapper;
import com.example.myapplication.utils.PermissionUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;

public class IngredientScannerActivity extends AppCompatActivity {

    public static final String EXTRA_INGREDIENTS = "scanned_ingredients";

    private static final int CAMERA_PERMISSION_REQUEST = 401;

    private ImageView previewImage;
    private MaterialButton captureButton;
    private MaterialButton useIngredientsButton;
    private TextView resultsText;
    private ProgressBar progressBar;

    private Bitmap capturedBitmap;
    private List<String> detectedIngredients = new ArrayList<>();

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            capturedBitmap = (Bitmap) extras.get("data");
                            if (capturedBitmap != null) {
                                previewImage.setImageBitmap(capturedBitmap);
                                analyzeImage();
                            } else {
                                showError("Unable to read captured image");
                            }
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_scanner);

        setupViews();
        captureButton.setOnClickListener(v -> ensureCameraPermissionThenCapture());
        useIngredientsButton.setOnClickListener(v -> returnResults());
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.scannerToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        previewImage = findViewById(R.id.previewImage);
        captureButton = findViewById(R.id.captureButton);
        useIngredientsButton = findViewById(R.id.useIngredientsButton);
        resultsText = findViewById(R.id.resultsText);
        progressBar = findViewById(R.id.scannerProgress);
    }

    private void ensureCameraPermissionThenCapture() {
        if (PermissionUtils.hasPermission(this, Manifest.permission.CAMERA)) {
            launchCamera();
        } else {
            PermissionUtils.requestPermissions(this, CAMERA_PERMISSION_REQUEST, Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            showError("Camera unavailable on this device");
        }
    }

    private void analyzeImage() {
        if (capturedBitmap == null) {
            showError("No image available for analysis");
            return;
        }
        showLoading(true);
        InputImage image = InputImage.fromBitmap(capturedBitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(this::handleLabels)
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Unable to process image: " + e.getMessage());
                });
    }

    private void handleLabels(List<ImageLabel> labels) {
        showLoading(false);
        detectedIngredients = IngredientLabelMapper.mapLabelsToIngredients(labels);
        if (detectedIngredients.isEmpty()) {
            resultsText.setText(R.string.scanner_no_results);
            useIngredientsButton.setEnabled(false);
        } else {
            StringBuilder builder = new StringBuilder();
            for (String ingredient : detectedIngredients) {
                builder.append("• ").append(ingredient).append("\n");
            }
            resultsText.setText(builder.toString().trim());
            useIngredientsButton.setEnabled(true);
        }
    }

    private void returnResults() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_INGREDIENTS, new ArrayList<>(detectedIngredients));
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        captureButton.setEnabled(!show);
        useIngredientsButton.setEnabled(!show && !detectedIngredients.isEmpty());
    }

    private void showError(String message) {
        Snackbar.make(captureButton, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (PermissionUtils.hasPermission(this, Manifest.permission.CAMERA)) {
                launchCamera();
            } else {
                showError(getString(R.string.scanner_permission_message));
            }
        }
    }
}
