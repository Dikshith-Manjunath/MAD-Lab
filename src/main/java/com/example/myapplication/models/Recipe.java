package com.example.myapplication.models;

import android.content.Context;

import com.example.myapplication.data.RecipeIngredient;
import com.example.myapplication.data.RecipeMapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {

    private int id;
    private String name;
    private String cuisine;
    private int calories;
    private int cookingTime;
    private String allergens;
    private int servings;
    private ArrayList<RecipeIngredient> ingredients;
    private ArrayList<String> instructions;
    private String imageUrl;
    private String imageResName;

    public Recipe(int id,
                  String name,
                  String cuisine,
                  int calories,
                  int cookingTime,
                  String allergens,
                  int servings,
                  List<RecipeIngredient> ingredients,
                  List<String> instructions,
                  String imageUrl,
                  String imageResName) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.calories = calories;
        this.cookingTime = cookingTime;
        this.allergens = allergens;
        this.servings = servings;
        this.ingredients = new ArrayList<>(ingredients);
        this.instructions = new ArrayList<>(instructions);
        this.imageUrl = imageUrl;
        this.imageResName = imageResName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public int getCalories() {
        return calories;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public String getAllergens() {
        return allergens;
    }

    public int getServings() {
        return servings;
    }

    public ArrayList<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public ArrayList<String> getInstructions() {
        return instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageResName() {
        return imageResName;
    }

    public int resolveImageResId(Context context) {
        return RecipeMapper.resolveImageRes(context, imageResName);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = new ArrayList<>(instructions);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageResName(String imageResName) {
        this.imageResName = imageResName;
    }
}
