package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private ArrayList<Recipe> allRecipes;
    private ArrayList<Recipe> filteredRecipes;
    private TextInputEditText searchEditText;
    private TextView emptyTextView;
    private ExtendedFloatingActionButton aiFab;

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
        emptyTextView = findViewById(R.id.emptyTextView);
        aiFab = findViewById(R.id.aiFab);

        // Setup FAB click listener
        aiFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AiRecipeActivity.class);
            startActivity(intent);
        });

        // Load recipes
        allRecipes = RecipeDatabase.getRecipes();
        filteredRecipes = new ArrayList<>(allRecipes);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(filteredRecipes, this);
        recyclerView.setAdapter(adapter);

        // Setup search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateEmptyView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recipes when returning from AI activity
        allRecipes = RecipeDatabase.getRecipes();
        filterRecipes(searchEditText.getText().toString());
    }

    private void filterRecipes(String query) {
        filteredRecipes.clear();

        if (query.isEmpty()) {
            filteredRecipes.addAll(allRecipes);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Recipe recipe : allRecipes) {
                if (recipe.getName().toLowerCase().contains(lowerCaseQuery) ||
                    recipe.getCategory().toLowerCase().contains(lowerCaseQuery) ||
                    recipe.getDifficulty().toLowerCase().contains(lowerCaseQuery)) {
                    filteredRecipes.add(recipe);
                }
            }
        }

        adapter.updateRecipes(filteredRecipes);
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (filteredRecipes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }
}
