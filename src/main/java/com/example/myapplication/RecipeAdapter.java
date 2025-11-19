package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private ArrayList<Recipe> recipes;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(ArrayList<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        
        holder.recipeEmoji.setText(recipe.getImageEmoji());
        holder.recipeName.setText(recipe.getName());
        holder.recipeCategory.setText(recipe.getCategory());
        holder.recipeTime.setText(recipe.getCookingTime() + " min");
        holder.recipeServings.setText(recipe.getServings() + " servings");
        holder.recipeDifficulty.setText(recipe.getDifficulty());
        
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
        holder.recipeDifficulty.setTextColor(difficultyColor);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(ArrayList<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeEmoji;
        TextView recipeName;
        TextView recipeCategory;
        TextView recipeTime;
        TextView recipeServings;
        TextView recipeDifficulty;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeEmoji = itemView.findViewById(R.id.recipeEmoji);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            recipeServings = itemView.findViewById(R.id.recipeServings);
            recipeDifficulty = itemView.findViewById(R.id.recipeDifficulty);
        }
    }
}
