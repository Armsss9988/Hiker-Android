package com.example.hiker.ui.history;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.hiker.MainActivity;
import com.example.hiker.database.HikerDatabase;
import com.example.hiker.database.dao.HikingHistoryDAO;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.databinding.FragmentHistoryBinding;
import com.example.hiker.ui.history.update.UpdateHikingViewModel;
import com.example.hiker.ui.map.SearchQueryListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    HistoryViewModel historyViewModel;
    RecyclerView historyView;
    HistoryAdapter historyAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel = new ViewModelProvider(this.requireActivity()).get(HistoryViewModel.class);
        UpdateHikingViewModel updateHikingViewModel = new ViewModelProvider(this.requireActivity()).get(UpdateHikingViewModel.class);
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        historyAdapter = new HistoryAdapter(new HistoryAdapter.HikingHistoryDiff(), root, historyViewModel, updateHikingViewModel);
        historyView = binding.historyList;
        historyView.setLayoutManager(new LinearLayoutManager(this.getActivity(),LinearLayoutManager.VERTICAL,false));
        historyView.setAdapter(historyAdapter);
        binding.searchHistories.setQueryHint("Search Hiking Here!");
        binding.searchHistories.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                historyViewModel.setSearchText(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                historyViewModel.setSearchText(newText);
                return false;
            }
        });
        binding.searchHistories.setOnCloseListener(() -> {
            historyViewModel.setSearchText("");
            return false;
        });
        DataObserve();
        return root;
    }
    void DataObserve(){
        historyViewModel.getSearchText().observe(getViewLifecycleOwner(), searchText -> {
            historyViewModel.getListHistoriesByName(searchText).observe(getViewLifecycleOwner(), listSearch -> {
                historyAdapter.submitList(listSearch);
            });
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}