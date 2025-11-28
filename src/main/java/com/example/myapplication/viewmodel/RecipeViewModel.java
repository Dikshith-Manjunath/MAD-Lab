package com.example.myapplication.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.RecipeEntity;
import com.example.myapplication.data.RecipeMapper;
import com.example.myapplication.data.RecipeRepository;
import com.example.myapplication.data.RecipeIngredient;
import com.example.myapplication.data.sync.RecipeSyncStatus;
import com.example.myapplication.models.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RecipeViewModel extends AndroidViewModel {

    private final RecipeRepository repository;
    private final MediatorLiveData<List<Recipe>> filteredRecipes = new MediatorLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> highlightedCuisine = new MutableLiveData<>("");
    private final LiveData<RecipeSyncStatus> syncStatus;
    private List<Recipe> allRecipes = new ArrayList<>();

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
        syncStatus = repository.getSyncStatus();
        
        LiveData<List<RecipeEntity>> source = repository.getAllRecipes();
        filteredRecipes.addSource(source, entities -> {
            allRecipes = new ArrayList<>();
            if (entities != null) {
                for (RecipeEntity entity : entities) {
                    allRecipes.add(RecipeMapper.fromEntity(entity));
                }
            }
            applyFilters();
        });
        filteredRecipes.addSource(searchQuery, value -> applyFilters());
        filteredRecipes.addSource(highlightedCuisine, value -> applyFilters());

        // Ensure the on-device dataset seeds the database the first time the view model loads.
        repository.synchronize(false);
    }

    public LiveData<List<Recipe>> getRecipes() {
        return filteredRecipes;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query.trim());
    }

    public void setHighlightedCuisine(String cuisine) {
        highlightedCuisine.setValue(cuisine == null ? "" : cuisine);
    }

    public void addRecipe(Recipe recipe) {
        repository.insertRecipe(recipe);
    }

    public LiveData<RecipeSyncStatus> getSyncStatus() {
        return syncStatus;
    }

    public void forceSync() {
        repository.synchronize(true);
    }

    public Recipe findRecipeByName(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        Recipe partialMatch = null;
        int bestDelta = Integer.MAX_VALUE;

        for (Recipe recipe : allRecipes) {
            if (recipe == null || recipe.getName() == null) {
                continue;
            }
            String nameLower = recipe.getName().toLowerCase(Locale.ROOT);
            if (nameLower.equals(lower)) {
                return recipe;
            }
            if (nameLower.contains(lower) || lower.contains(nameLower)) {
                int delta = Math.abs(nameLower.length() - lower.length());
                if (partialMatch == null || delta < bestDelta) {
                    partialMatch = recipe;
                    bestDelta = delta;
                }
            }
        }

        return partialMatch;
    }

    private void applyFilters() {
        String query = searchQuery.getValue();
        String cuisine = highlightedCuisine.getValue();
        List<Recipe> working = new ArrayList<>(allRecipes);

        // 1. Apply Search Filter
        if (query != null && !query.isEmpty()) {
            String lower = query.toLowerCase();
            List<Recipe> filtered = new ArrayList<>();
            for (Recipe recipe : working) {
                if (recipe.getName().toLowerCase().contains(lower)
                        || recipe.getCuisine().toLowerCase().contains(lower)
                        || recipe.getAllergens().toLowerCase().contains(lower)) {
                    filtered.add(recipe);
                    continue;
                }
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    if (ingredient.getName().toLowerCase().contains(lower)) {
                        filtered.add(recipe);
                        break;
                    }
                }
            }
            working = filtered;
        }

        // 2. Apply Location (Cuisine) Filter
        if (cuisine != null && !cuisine.isEmpty()) {
            List<Recipe> matching = new ArrayList<>();
            String lowerCuisine = cuisine.toLowerCase();
            for (Recipe recipe : working) {
                if (recipe.getCuisine().toLowerCase().contains(lowerCuisine)) {
                    matching.add(recipe);
                }
            }
            // If we have matches for the cuisine, ONLY show those.
            // If no matches, we could show empty or show all. Let's show matching only.
            working = matching; 
        }

        // 3. Sort Alphabetically
        Collections.sort(working, Comparator.comparing(Recipe::getName));

        filteredRecipes.setValue(working);
    }
}
