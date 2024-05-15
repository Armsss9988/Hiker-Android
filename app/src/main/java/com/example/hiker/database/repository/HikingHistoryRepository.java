package com.example.hiker.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hiker.database.HikerDatabase;
import com.example.hiker.database.dao.HikingHistoryDAO;
import com.example.hiker.database.entity.HikingHistory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HikingHistoryRepository {
    private LiveData<List<HikingHistory>> listHistory;
    private HikingHistoryDAO hikingHistoryDAO;
    private LiveData<List<HikingHistory>> listHistoriesByID;
    public HikingHistoryRepository(Application application) {
        HikerDatabase hikerDatabase = HikerDatabase.getDatabase(application);
        hikingHistoryDAO = hikerDatabase.hikingHistoryDAO();
        listHistory = hikingHistoryDAO.getAll();
        listHistoriesByID = new MutableLiveData<>();
    }
    public LiveData<List<HikingHistory>> getAllHistory(){
        return this.listHistory;
    }


    public LiveData<List<HikingHistory>> getHistoryByID(int[] hikingIDs) {
        listHistoriesByID = hikingHistoryDAO.getListHistoriesByIds(hikingIDs);

        return listHistoriesByID;
    }
    public void insertNew(HikingHistory hikingHistory) {
        HikerDatabase.databaseWriteExecutor.execute(() -> hikingHistoryDAO.insertNew(hikingHistory));
    }
    public void updateHistory(HikingHistory hikingHistory) {
        HikerDatabase.databaseWriteExecutor.execute(() -> hikingHistoryDAO.updateHistory(hikingHistory));
    }
    public void deleteHiking(HikingHistory hikingHistory){
        HikerDatabase.databaseWriteExecutor.execute(() -> hikingHistoryDAO.delete(hikingHistory));
    }
    public LiveData<List<HikingHistory>> getListHistoriesByName(String text){
        return hikingHistoryDAO.getListHistoriesByName(text);
    }

}
