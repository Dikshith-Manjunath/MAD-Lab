package com.example.myapplication.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {RecipeEntity.class}, version = 5, exportSchema = false)
@TypeConverters(RecipeConverters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final int THREAD_COUNT = 4;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(THREAD_COUNT);

    public abstract RecipeDao recipeDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Context appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(
                                    appContext,
                                    AppDatabase.class,
                                    "pantrypilot.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                    
                    // Trigger sync/seed
                }
            }
        }
        return INSTANCE;
    }
}
