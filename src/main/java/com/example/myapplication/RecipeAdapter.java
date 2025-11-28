package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.myapplication.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Recipe> recipes) {
        this.recipes = recipes == null ? new ArrayList<>() : new ArrayList<>(recipes);
        notifyDataSetChanged();
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
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(holder.recipeImage.getContext())
                    .load(recipe.getImageUrl())
                    .placeholder(recipe.resolveImageResId(holder.itemView.getContext()))
                    .error(recipe.resolveImageResId(holder.itemView.getContext()))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(recipe.resolveImageResId(holder.itemView.getContext()));
        }
        holder.recipeName.setText(recipe.getName());
        holder.recipeCuisine.setText(recipe.getCuisine());
        holder.recipeCalories.setText(holder.itemView.getContext().getString(R.string.format_calories, recipe.getCalories()));
        holder.recipeCookTime.setText(holder.itemView.getContext().getString(R.string.format_minutes, recipe.getCookingTime()));
        holder.recipeAllergens.setText(recipe.getAllergens());

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

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName;
        TextView recipeCuisine;
        TextView recipeCalories;
        TextView recipeCookTime;
        TextView recipeAllergens;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeCuisine = itemView.findViewById(R.id.recipeCuisine);
            recipeCalories = itemView.findViewById(R.id.recipeCalories);
            recipeCookTime = itemView.findViewById(R.id.recipeCookTime);
            recipeAllergens = itemView.findViewById(R.id.recipeAllergens);
        }
    }
}
