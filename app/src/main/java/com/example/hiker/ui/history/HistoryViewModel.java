package com.example.hiker.ui.history;
import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.database.repository.HikingHistoryRepository;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {


    private LiveData<List<HikingHistory>> listHistory;
    private MutableLiveData<String> searchText;
    private HikingHistoryRepository hikingHistoryRepository;
    private Dictionary<HikingHistory,Boolean> expandedItem;

    public HistoryViewModel(Application application) {
        super(application);
        this.hikingHistoryRepository = new HikingHistoryRepository(application);
        this.listHistory = hikingHistoryRepository.getAllHistory();
        this.expandedItem = new Hashtable<>();
        this.searchText = new MutableLiveData<>();
        this.searchText.setValue("");
    }

    public Dictionary<HikingHistory, Boolean> getExpandedItem() {
        return expandedItem;
    }

    public LiveData<List<HikingHistory>> getRoomHistory(){
        return this.listHistory;
    }
    public void insertNew(HikingHistory hikingHistory){
        hikingHistoryRepository.insertNew(hikingHistory);
    }
    public LiveData<List<HikingHistory>> GetHistoriesByID(int[] Id){
        return hikingHistoryRepository.getHistoryByID(Id);
    }
    public void updateHistory(HikingHistory hikingHistory){
        hikingHistoryRepository.updateHistory(hikingHistory);
    }
    public void deleteHiking(HikingHistory hikingHistory){
        hikingHistoryRepository.deleteHiking(hikingHistory);
    }

    public MutableLiveData<String> getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.setValue(searchText);
    }

    public LiveData<List<HikingHistory>> getListHistoriesByName(String searchText){
        return hikingHistoryRepository.getListHistoriesByName(searchText);
    }
}