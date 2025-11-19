package com.example.myapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {
    private int id;
    private String name;
    private String category;
    private String difficulty;
    private int cookingTime;
    private int servings;
    private ArrayList<String> ingredients;
    private ArrayList<String> instructions;
    private String imageEmoji;

    public Recipe(int id, String name, String category, String difficulty, 
                  int cookingTime, int servings, ArrayList<String> ingredients, 
                  ArrayList<String> instructions, String imageEmoji) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.difficulty = difficulty;
        this.cookingTime = cookingTime;
        this.servings = servings;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageEmoji = imageEmoji;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public int getCookingTime() { return cookingTime; }
    public int getServings() { return servings; }
    public ArrayList<String> getIngredients() { return ingredients; }
    public ArrayList<String> getInstructions() { return instructions; }
    public String getImageEmoji() { return imageEmoji; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public void setServings(int servings) { this.servings = servings; }
    public void setIngredients(ArrayList<String> ingredients) { this.ingredients = ingredients; }
    public void setInstructions(ArrayList<String> instructions) { this.instructions = instructions; }
    public void setImageEmoji(String imageEmoji) { this.imageEmoji = imageEmoji; }
}
