package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

public class AiRecipeActivity extends AppCompatActivity {

    private TextInputEditText dishNameInput;
    private Button generateButton;
    private ProgressBar progressBar;
    private TextView resultTextView;
    private GeminiApiService geminiService;
    
    // Replace with your actual Gemini API key
    private static final String GEMINI_API_KEY = "AIzaSyApgWQjl6wxmsVvrgXL2hgz2E4Z8c3WLAk";

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
        progressBar = findViewById(R.id.progressBar);
        resultTextView = findViewById(R.id.resultTextView);

        // Initialize Gemini service
        geminiService = new GeminiApiService(GEMINI_API_KEY);

        // Set up button click listener
        generateButton.setOnClickListener(v -> generateRecipe());
    }

    private void generateRecipe() {
        String dishName = dishNameInput.getText().toString().trim();
        
        if (dishName.isEmpty()) {
            Toast.makeText(this, "Please enter a dish name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        resultTextView.setText("Generating recipe for " + dishName + "...");

        // Call Gemini API
        geminiService.generateRecipe(dishName, new GeminiApiService.RecipeCallback() {
            @Override
            public void onSuccess(Recipe recipe) {
                progressBar.setVisibility(View.GONE);
                generateButton.setEnabled(true);
                displayRecipe(recipe);
                
                // Add to database
                RecipeDatabase.addAiRecipe(recipe);
                Toast.makeText(AiRecipeActivity.this, "Recipe generated! Check 'All Recipes' to see it.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                generateButton.setEnabled(true);
                resultTextView.setText("Error: " + error + "\n\nPlease make sure:\n1. You have internet connection\n2. Your API key is correct\n3. The dish name is valid");
                Toast.makeText(AiRecipeActivity.this, "Failed to generate recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipe(Recipe recipe) {
        StringBuilder result = new StringBuilder();
        
        result.append(recipe.getImageEmoji()).append(" ").append(recipe.getName()).append("\n\n");
        result.append("Category: ").append(recipe.getCategory()).append("\n");
        result.append("Difficulty: ").append(recipe.getDifficulty()).append("\n");
        result.append("Time: ").append(recipe.getCookingTime()).append(" minutes\n");
        result.append("Servings: ").append(recipe.getServings()).append("\n\n");
        
        result.append("INGREDIENTS:\n");
        for (String ingredient : recipe.getIngredients()) {
            result.append("• ").append(ingredient).append("\n");
        }
        
        result.append("\nINSTRUCTIONS:\n");
        int step = 1;
        for (String instruction : recipe.getInstructions()) {
            result.append(step++).append(". ").append(instruction).append("\n\n");
        }
        
        resultTextView.setText(result.toString());
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
