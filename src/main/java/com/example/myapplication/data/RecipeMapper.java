package com.example.myapplication.data;

import android.content.Context;

import com.example.myapplication.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public final class RecipeMapper {

    private RecipeMapper() {
    }

    public static Recipe fromEntity(RecipeEntity entity) {
        return new Recipe(
                entity.getId(),
                entity.getName(),
                entity.getCuisine(),
                entity.getCalories(),
                entity.getCookTimeMinutes(),
                entity.getAllergens(),
                entity.getServings(),
                new ArrayList<>(entity.getIngredients()),
                new ArrayList<>(entity.getInstructions()),
                entity.getImageUrl(),
                entity.getImageResName()
        );
    }

    public static RecipeEntity toEntity(Recipe recipe) {
        return new RecipeEntity(
                recipe.getName(),
                recipe.getCuisine(),
                recipe.getCalories(),
                recipe.getCookingTime(),
                recipe.getAllergens(),
                new ArrayList<>(recipe.getIngredients()),
                new ArrayList<>(recipe.getInstructions()),
                recipe.getServings(),
                recipe.getImageResName(),
                recipe.getImageUrl()
        );
    }

    public static int resolveImageRes(Context context, String resName) {
        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        if (resId == 0) {
            resId = context.getResources().getIdentifier("ic_recipe_placeholder", "drawable", context.getPackageName());
        }
        return resId;
    }
}
