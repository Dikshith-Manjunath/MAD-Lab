package com.example.myapplication.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecipeEntity> recipes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecipeEntity recipe);

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    LiveData<List<RecipeEntity>> getAll();

    @Query("SELECT * FROM recipes WHERE id = :id")
    LiveData<RecipeEntity> getById(int id);

    @Query("DELETE FROM recipes")
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM recipes WHERE cuisine LIKE '%' || :cuisine || '%' ORDER BY name ASC")
    List<RecipeEntity> getRecipesForCuisine(String cuisine);

    @Query("SELECT COUNT(*) FROM recipes")
    int getRecipeCount();
}
