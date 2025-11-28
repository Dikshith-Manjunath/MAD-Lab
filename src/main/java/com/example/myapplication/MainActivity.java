package com.example.myapplication;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.sync.RecipeSyncStatus;
import com.example.myapplication.models.Recipe;
import com.example.myapplication.sensors.LocationProvider;
import com.example.myapplication.sensors.LocationRecipeRecommender;
import com.example.myapplication.utils.PermissionUtils;
import com.example.myapplication.viewmodel.RecipeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_AUDIO_PERMISSION = 2001;

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextInputEditText searchEditText;
    private TextInputLayout searchLayout;
    private TextView emptyTextView;
    private TextView locationStatusText;
    private ExtendedFloatingActionButton aiFab;
    private RecipeViewModel recipeViewModel;
    private MaterialButton locationButton;
    private LinearLayout syncStatusContainer;
    private LinearProgressIndicator syncProgress;
    private TextView syncStatusText;
    private MaterialButton syncRetryButton;
    private LocationProvider locationProvider;
    private View rootView;
    private boolean locationFilterActive = false;
    private final androidx.activity.result.ActivityResultLauncher<Intent> voiceSearchLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    java.util.ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        searchEditText.setText(spokenText);
                        searchEditText.setSelection(spokenText.length());
                        recipeViewModel.setSearchQuery(spokenText);
                    } else {
                        Snackbar.make(rootView, R.string.voice_error, Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.all_recipes);
        }

        // Initialize views
        recyclerView = findViewById(R.id.recipesRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        searchLayout = findViewById(R.id.searchLayout);
        emptyTextView = findViewById(R.id.emptyTextView);
        aiFab = findViewById(R.id.aiFab);
        locationButton = findViewById(R.id.locationButton);
        locationStatusText = findViewById(R.id.locationStatusText);
        syncStatusContainer = findViewById(R.id.syncStatusContainer);
        syncProgress = findViewById(R.id.syncProgress);
        syncStatusText = findViewById(R.id.syncStatusText);
        syncRetryButton = findViewById(R.id.syncRetryButton);
        rootView = findViewById(android.R.id.content);
        locationProvider = new LocationProvider(this);

        // Setup FAB click listener
        aiFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AiRecipeActivity.class);
            startActivity(intent);
        });

        locationButton.setOnClickListener(v -> attemptLocationHighlight());
        syncRetryButton.setOnClickListener(v -> recipeViewModel.forceSync());
    searchLayout.setEndIconOnClickListener(v -> attemptVoiceSearch());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recyclerView.setAdapter(adapter);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        recipeViewModel.getRecipes().observe(this, recipes -> {
            adapter.submitList(recipes);
            updateEmptyView(recipes == null || recipes.isEmpty());
        });
        recipeViewModel.getSyncStatus().observe(this, this::updateSyncStatus);

        searchEditText.addTextChangedListener(new SimpleTextWatcher(text ->
                recipeViewModel.setSearchQuery(text)));

    }

    private void attemptLocationHighlight() {
        if (locationFilterActive) {
            clearLocationFilter();
            return;
        }
        if (hasAnyLocationPermission()) {
            fetchLocationCuisine();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(rootView, R.string.grant_location_permission, Snackbar.LENGTH_LONG).show();
            }
            PermissionUtils.requestPermissions(this, REQUEST_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    private void attemptVoiceSearch() {
        if (PermissionUtils.hasPermission(this, Manifest.permission.RECORD_AUDIO)) {
            launchVoiceSearch();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(rootView, R.string.voice_permission_message, Snackbar.LENGTH_LONG).show();
            }
            PermissionUtils.requestPermissions(this, REQUEST_AUDIO_PERMISSION, Manifest.permission.RECORD_AUDIO);
        }
    }

    private void launchVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));
        try {
            voiceSearchLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(rootView, R.string.voice_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private void fetchLocationCuisine() {
        setLocationLoading(true);
        locationProvider.requestAddress(new LocationProvider.Callback() {
            @Override
            public void onAddressResolved(Address address) {
                runOnUiThread(() -> handleResolvedAddress(address));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> handleLocationError(message));
            }
        });
    }

    private void handleResolvedAddress(Address address) {
        setLocationLoading(false);
        String cuisine = LocationRecipeRecommender.recommendCuisine(address);
        if (cuisine == null || cuisine.isEmpty()) {
            recipeViewModel.setHighlightedCuisine("");
            showLocationStatus(getString(R.string.location_unavailable));
            Snackbar.make(rootView, R.string.location_unavailable, Snackbar.LENGTH_LONG).show();
            return;
        }
        recipeViewModel.setHighlightedCuisine(cuisine);
        String message = getString(R.string.location_recommendation, cuisine);
        showLocationStatus(message);
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
        locationFilterActive = true;
        locationButton.setText(R.string.clear_location_filter);
    }

    private void handleLocationError(String message) {
        setLocationLoading(false);
        recipeViewModel.setHighlightedCuisine("");
        String fallback = message == null || message.isEmpty() ? getString(R.string.location_unavailable) : message;
        showLocationStatus(fallback);
        Snackbar.make(rootView, fallback, Snackbar.LENGTH_LONG).show();
        locationFilterActive = false;
        locationButton.setText(R.string.use_location);
    }

    private void setLocationLoading(boolean loading) {
        locationButton.setEnabled(!loading);
        locationStatusText.setVisibility(View.VISIBLE);
        locationStatusText.setText(loading ? getString(R.string.location_fetching) : "");
        if (!loading && (locationStatusText.getText() == null || locationStatusText.getText().length() == 0)) {
            locationStatusText.setVisibility(View.GONE);
        }
    }

    private void showLocationStatus(String message) {
        locationStatusText.setText(message);
        locationStatusText.setVisibility(View.VISIBLE);
    }

    private void clearLocationFilter() {
        recipeViewModel.setHighlightedCuisine("");
        locationFilterActive = false;
        locationButton.setText(R.string.use_location);
        locationStatusText.setVisibility(View.GONE);
        Snackbar.make(rootView, R.string.location_filter_cleared, Snackbar.LENGTH_SHORT).show();
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    private void updateSyncStatus(RecipeSyncStatus status) {
        if (status == null || status.getState() == RecipeSyncStatus.State.IDLE) {
            syncStatusContainer.setVisibility(View.GONE);
            return;
        }

        syncStatusContainer.setVisibility(View.VISIBLE);
        syncRetryButton.setVisibility(View.GONE);

        switch (status.getState()) {
            case RUNNING:
                if (status.getProgress() <= 0) {
                    syncProgress.setIndeterminate(true);
                    syncStatusText.setText(getString(R.string.sync_preparing));
                } else {
                    syncProgress.setIndeterminate(false);
                    int goal = Math.max(status.getGoal(), 1);
                    int percent = (int) ((status.getProgress() / (float) goal) * 100f);
                    syncProgress.setProgressCompat(percent, true);
                    syncStatusText.setText(getString(R.string.sync_progress, status.getProgress(), goal));
                }
                break;
            case SUCCEEDED:
                syncProgress.setIndeterminate(false);
                syncProgress.setProgressCompat(100, true);
                syncStatusText.setText(getString(R.string.sync_success, status.getInserted()));
                syncStatusContainer.postDelayed(() -> syncStatusContainer.setVisibility(View.GONE), 2000);
                break;
                case FAILED:
                syncProgress.setIndeterminate(false);
                syncProgress.setProgressCompat(0, false);
                String fallback = (status.getMessage() == null || status.getMessage().isEmpty())
                    ? "Please try again."
                    : status.getMessage();
                String message = getString(R.string.sync_failed, fallback);
                syncStatusText.setText(message);
                syncRetryButton.setVisibility(View.VISIBLE);
                break;
            default:
                syncStatusContainer.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (hasAnyLocationPermission()) {
                fetchLocationCuisine();
            } else {
                handleLocationError(getString(R.string.grant_location_permission));
            }
        } else if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (PermissionUtils.hasPermission(this, Manifest.permission.RECORD_AUDIO)) {
                launchVoiceSearch();
            } else {
                Snackbar.make(rootView, R.string.voice_permission_message, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private boolean hasAnyLocationPermission() {
        return PermissionUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || PermissionUtils.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final java.util.function.Consumer<String> consumer;

        SimpleTextWatcher(java.util.function.Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            consumer.accept(s == null ? "" : s.toString());
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }
}
