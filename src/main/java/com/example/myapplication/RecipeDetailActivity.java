package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView detailEmoji;
    private TextView detailName;
    private TextView detailCategory;
    private TextView detailTime;
    private TextView detailServings;
    private TextView detailDifficulty;
    private TextView detailIngredients;
    private TextView detailInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        detailEmoji = findViewById(R.id.detailEmoji);
        detailName = findViewById(R.id.detailName);
        detailCategory = findViewById(R.id.detailCategory);
        detailTime = findViewById(R.id.detailTime);
        detailServings = findViewById(R.id.detailServings);
        detailDifficulty = findViewById(R.id.detailDifficulty);
        detailIngredients = findViewById(R.id.detailIngredients);
        detailInstructions = findViewById(R.id.detailInstructions);

        // Get recipe from intent
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (recipe != null) {
            displayRecipe(recipe);
        }
    }

    private void displayRecipe(Recipe recipe) {
        // Set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.getName());
        }

        // Display recipe details
        detailEmoji.setText(recipe.getImageEmoji());
        detailName.setText(recipe.getName());
        detailCategory.setText(recipe.getCategory());
        detailTime.setText(recipe.getCookingTime() + " min");
        detailServings.setText(String.valueOf(recipe.getServings()));
        detailDifficulty.setText(recipe.getDifficulty());

        // Set difficulty color
        int difficultyColor;
        switch (recipe.getDifficulty().toLowerCase()) {
            case "easy":
                difficultyColor = Color.parseColor("#4CAF50");
                break;
            case "medium":
                difficultyColor = Color.parseColor("#FF9800");
                break;
            case "hard":
                difficultyColor = Color.parseColor("#F44336");
                break;
            default:
                difficultyColor = Color.parseColor("#757575");
        }
        detailDifficulty.setTextColor(difficultyColor);

        // Display ingredients
        StringBuilder ingredientsText = new StringBuilder();
        ArrayList<String> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientsText.append("• ").append(ingredients.get(i));
            if (i < ingredients.size() - 1) {
                ingredientsText.append("\n\n");
            }
        }
        detailIngredients.setText(ingredientsText.toString());

        // Display instructions
        StringBuilder instructionsText = new StringBuilder();
        ArrayList<String> instructions = recipe.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            instructionsText.append((i + 1)).append(". ").append(instructions.get(i));
            if (i < instructions.size() - 1) {
                instructionsText.append("\n\n");
            }
        }
        detailInstructions.setText(instructionsText.toString());
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
