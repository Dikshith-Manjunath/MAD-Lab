package com.example.myapplication.data;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeConverters {
    private static final Gson gson = new Gson();
    private static final Type ingredientListType = new TypeToken<ArrayList<RecipeIngredient>>() {
    }.getType();
    private static final Type instructionListType = new TypeToken<ArrayList<String>>() {
    }.getType();

    @TypeConverter
    public static String fromIngredientList(List<RecipeIngredient> value) {
        if (value == null) {
            return "[]";
        }
        return gson.toJson(value, ingredientListType);
    }

    @TypeConverter
    public static List<RecipeIngredient> toIngredientList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return gson.fromJson(value, ingredientListType);
    }

    @TypeConverter
    public static String fromInstructionList(List<String> value) {
        if (value == null) {
            return "[]";
        }
        return gson.toJson(value, instructionListType);
    }

    @TypeConverter
    public static List<String> toInstructionList(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return gson.fromJson(value, instructionListType);
    }
}
