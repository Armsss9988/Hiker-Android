package com.example.hiker.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.hiker.database.entity.HikingHistory;

import java.util.List;

@Dao
public interface HikingHistoryDAO {
    @Query("SELECT * FROM `Hiking Histories`")
    LiveData<List<HikingHistory>> getAll();

    @Query("SELECT * FROM `Hiking Histories` WHERE id IN (:hikingIDs)")
    LiveData<List<HikingHistory>> getListHistoriesByIds(int[] hikingIDs);
    @Query("SELECT * FROM `Hiking Histories` WHERE `Name of the hike` LIKE '%' || :text || '%'")
    LiveData<List<HikingHistory>> getListHistoriesByName(String text);

    @Insert
    void insertNew(HikingHistory hikingHistory);
    @Update
    void updateHistory(HikingHistory hikingHistory);

    @Delete
    void delete(HikingHistory hikingHistory);
}
