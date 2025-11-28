package com.example.myapplication.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.sync.AssetRecipeImporter;
import com.example.myapplication.data.sync.RecipeSyncStatus;
import com.example.myapplication.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecipeRepository {

    private static final String PREFS_NAME = "recipe_sync_prefs";
    private static final String KEY_LAST_SYNC = "last_sync_epoch";
    private static final long SYNC_INTERVAL = java.util.concurrent.TimeUnit.DAYS.toMillis(7);

    private final Context appContext;
    private final RecipeDao recipeDao;
    private final ExecutorService executor;
    private final SharedPreferences preferences;
    private final AssetRecipeImporter importer;
    private final AtomicBoolean syncing = new AtomicBoolean(false);
    private final MutableLiveData<RecipeSyncStatus> syncStatus = new MutableLiveData<>(RecipeSyncStatus.idle());

    public RecipeRepository(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.appContext = applicationContext;
        AppDatabase database = AppDatabase.getInstance(applicationContext);
        recipeDao = database.recipeDao();
        executor = AppDatabase.databaseExecutor;
        preferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        importer = new AssetRecipeImporter();
    }

    public LiveData<List<RecipeEntity>> getAllRecipes() {
        return recipeDao.getAll();
    }

    public void insertRecipe(Recipe recipe) {
        executor.execute(() -> recipeDao.insert(RecipeMapper.toEntity(recipe)));
    }

    public List<Recipe> getRecipesForCuisine(String cuisine) {
        List<RecipeEntity> entities = recipeDao.getRecipesForCuisine(cuisine);
        List<Recipe> output = new ArrayList<>();
        for (RecipeEntity entity : entities) {
            output.add(RecipeMapper.fromEntity(entity));
        }
        return output;
    }

    public LiveData<RecipeSyncStatus> getSyncStatus() {
        return syncStatus;
    }

    public void synchronize(boolean force) {
        if (syncing.get()) {
            return;
        }

        syncing.set(true);
        executor.execute(() -> {
            try {
                boolean shouldSync = force;
                if (!force) {
                    int count = recipeDao.getRecipeCount();
                    long lastSync = preferences.getLong(KEY_LAST_SYNC, 0L);
                    long now = System.currentTimeMillis();
                    shouldSync = count == 0 || (now - lastSync) > SYNC_INTERVAL;
                }

                if (!shouldSync) {
                    syncStatus.postValue(RecipeSyncStatus.idle());
                    return;
                }

                syncStatus.postValue(RecipeSyncStatus.running(0, 0));
                int inserted = importer.importRecipes(appContext, recipeDao, 0,
                    (completed, goal) -> syncStatus.postValue(RecipeSyncStatus.running(completed, goal)));
                preferences.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
                syncStatus.postValue(RecipeSyncStatus.success(inserted));
            } catch (Exception e) {
                String message = e.getMessage() == null ? "Synchronization failed" : e.getMessage();
                syncStatus.postValue(RecipeSyncStatus.failure(message));
            } finally {
                syncing.set(false);
            }
        });
    }
}
