package com.example.myapplication.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "recipes")
public class RecipeEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String cuisine;

    @ColumnInfo(name = "calories")
    private int calories;

    @ColumnInfo(name = "cook_time_minutes")
    private int cookTimeMinutes;

    @NonNull
    private String allergens;

    @NonNull
    private List<RecipeIngredient> ingredients;

    @NonNull
    private List<String> instructions;

    @ColumnInfo(name = "servings")
    private int servings;

    @NonNull
    @ColumnInfo(name = "image_res_name")
    private String imageResName;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    public RecipeEntity(@NonNull String name,
                        @NonNull String cuisine,
                        int calories,
                        int cookTimeMinutes,
                        @NonNull String allergens,
                        @NonNull List<RecipeIngredient> ingredients,
                        @NonNull List<String> instructions,
                        int servings,
                        @NonNull String imageResName,
                        String imageUrl) {
        this.name = name;
        this.cuisine = cuisine;
        this.calories = calories;
        this.cookTimeMinutes = cookTimeMinutes;
        this.allergens = allergens;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.servings = servings;
        this.imageResName = imageResName;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getCuisine() {
        return cuisine;
    }

    public int getCalories() {
        return calories;
    }

    public int getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    @NonNull
    public String getAllergens() {
        return allergens;
    }

    @NonNull
    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    @NonNull
    public List<String> getInstructions() {
        return instructions;
    }

    public int getServings() {
        return servings;
    }

    @NonNull
    public String getImageResName() {
        return imageResName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
