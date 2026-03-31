package com.example.weather;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.weather.model.CityEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {CityEntity.class}, version = 2) // Version bumped to 2 for schema change
public abstract class AppDatabase extends RoomDatabase {

    public abstract CityDao cityDao();

    private static volatile AppDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "weather_db"
                            )
                            .fallbackToDestructiveMigration() // Simple way to handle schema change for dev
                            .build();
                }
            }
        }
        return instance;
    }
}
