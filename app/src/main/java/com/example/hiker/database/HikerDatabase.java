package com.example.hiker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.database.dao.HikingHistoryDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {HikingHistory.class}, version = 12)
@TypeConverters({Converters.class})
public abstract class HikerDatabase extends RoomDatabase {
    public abstract HikingHistoryDAO hikingHistoryDAO();
    private static volatile HikerDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static HikerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (HikerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                                    HikerDatabase.class, "Hiker Database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
