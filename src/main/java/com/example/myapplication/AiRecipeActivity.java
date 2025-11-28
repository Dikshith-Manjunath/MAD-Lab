package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.data.RecipeIngredient;
import com.example.myapplication.models.Recipe;
import com.example.myapplication.viewmodel.RecipeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class AiRecipeActivity extends AppCompatActivity {

    private TextInputEditText dishNameInput;
    private Button generateButton;
    private MaterialButton scanButton;
    private ProgressBar progressBar;
    private TextView resultTextView;
    private GeminiApiService geminiService;
    private RecipeViewModel recipeViewModel;
    
    // Replace with your actual Gemini API key
    private static final String GEMINI_API_KEY = "AIzaSyApgWQjl6wxmsVvrgXL2hgz2E4Z8c3WLAk";

    private final ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        List<String> ingredients = result.getData().getStringArrayListExtra(IngredientScannerActivity.EXTRA_INGREDIENTS);
                        if (ingredients != null && !ingredients.isEmpty()) {
                            String prompt = "Recipe using: " + String.join(", ", ingredients);
                            dishNameInput.setText(prompt);
                            generateRecipe(prompt);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_recipe);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AI Recipe Generator");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        dishNameInput = findViewById(R.id.dishNameInput);
        generateButton = findViewById(R.id.generateButton);
        scanButton = findViewById(R.id.scanButton);
        progressBar = findViewById(R.id.progressBar);
        resultTextView = findViewById(R.id.resultTextView);

        // Initialize Gemini service
        geminiService = new GeminiApiService(GEMINI_API_KEY);
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Set up button click listener
        generateButton.setOnClickListener(v -> {
             String dishName = dishNameInput.getText().toString().trim();
             if (dishName.isEmpty()) {
                Toast.makeText(this, "Please enter a dish name", Toast.LENGTH_SHORT).show();
                return;
             }
             generateRecipe(dishName);
        });
        
        scanButton.setOnClickListener(v -> {
            Intent intent = new Intent(AiRecipeActivity.this, IngredientScannerActivity.class);
            scannerLauncher.launch(intent);
        });
    }

    private void generateRecipe(String dishName) {
        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        scanButton.setEnabled(false);
        resultTextView.setText("Generating recipe for " + dishName + "...");

        // Call Gemini API
        geminiService.generateRecipe(dishName, new GeminiApiService.RecipeCallback() {
            @Override
            public void onSuccess(Recipe recipe) {
                progressBar.setVisibility(View.GONE);
                generateButton.setEnabled(true);
                scanButton.setEnabled(true);
                displayRecipe(recipe);
                recipeViewModel.addRecipe(recipe);
                Toast.makeText(AiRecipeActivity.this, "Recipe generated! Check 'All Recipes' to see it.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                generateButton.setEnabled(true);
                scanButton.setEnabled(true);
                if (error != null && error.contains("429")) {
                    Recipe fallback = recipeViewModel.findRecipeByName(dishName);
                    if (fallback != null) {
                        displayRecipe(fallback);
                        Snackbar.make(resultTextView, "Gemini rate limit reached. Showing a similar recipe from the catalog.", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }
                resultTextView.setText("Error: " + error + "\n\nPlease make sure:\n1. You have internet connection\n2. Your API key is correct\n3. The dish name is valid");
                Toast.makeText(AiRecipeActivity.this, "Failed to generate recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipe(Recipe recipe) {
        StringBuilder result = new StringBuilder();

        result.append(recipe.getName()).append("\n\n");
        result.append("Cuisine: ").append(recipe.getCuisine()).append("\n");
        result.append("Calories: ").append(recipe.getCalories()).append(" kcal\n");
        result.append("Cook Time: ").append(recipe.getCookingTime()).append(" minutes\n");
        result.append("Servings: ").append(recipe.getServings()).append("\n");
        result.append("Allergens: ").append(recipe.getAllergens()).append("\n\n");

        result.append("INGREDIENTS:\n");
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            result.append("• ").append(ingredient.getDisplayText()).append("\n");
        }

        result.append("\nINSTRUCTIONS:\n");
        int step = 1;
        for (String instruction : recipe.getInstructions()) {
            result.append(step++).append(". ").append(instruction).append("\n\n");
        }

        resultTextView.setText(result.toString().trim());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
