package com.example.hiker.ui.home;
import static android.content.ContentValues.TAG;
import static androidx.navigation.Navigation.findNavController;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.hiker.R;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.databinding.FragmentHomeBinding;
import com.example.hiker.ui.history.HistoryViewModel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment {
    Button addNewBtn;

    private FragmentHomeBinding binding;
    HistoryViewModel historyViewModel;
    SimpleDateFormat formatter;
    HikingHistory currentHiking;
    HikingHistory nearbyHiking;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: create");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        historyViewModel = new ViewModelProvider(this.requireActivity()).get(HistoryViewModel.class);
        View root = binding.getRoot();
        nearbyHiking = null;
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        addNewBtn = binding.addNew;
        addNewBtn.setOnClickListener(view -> {
            findNavController(root).navigate(R.id.action_nav_home_to_fragment_add_new);
        });
        DataObserve();
        return root;
    }
    void DataObserve(){
        historyViewModel.getRoomHistory().observe(getViewLifecycleOwner(),listAll ->{
            ArrayList<Date> listOfDates = new ArrayList<>();
            boolean haveActive = false;
            for(int i = 0; i < listAll.size(); i++) {
                if(Objects.equals(listAll.get(i).status, "Active")){
                    currentHiking = listAll.get(i);
                    haveActive = true;
                    Log.i(TAG, "DataObserve: " + currentHiking.name);
                    binding.currentHiking.setVisibility(View.VISIBLE);
                    binding.currentNameValue.setText(currentHiking.name);
                    binding.currentLocationValue.setText(currentHiking.location);
                    binding.currentButtonShowMap.setOnClickListener(view -> {
                        Bundle bundle = new Bundle();
                        bundle.putLong("id",currentHiking.id);
                        findNavController(this.getView()).navigate(R.id.action_nav_home_to_nav_active, bundle);
                    });
                }
                if(!haveActive){
                    binding.currentHiking.setVisibility(View.GONE);
                }
                try {
                    if (formatter.parse(listAll.get(i).date).compareTo(new Date()) > 0) {
                        if(Objects.equals(listAll.get(i).status, "Waiting")){
                            Date dateCheck = formatter.parse(listAll.get(i).date);
                            listOfDates.add(dateCheck);
                        }
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            if(listOfDates.size() >0){
                Date minDate = Collections.min(listOfDates);
                binding.nextHiking.setVisibility(View.VISIBLE);
                for( HikingHistory hikingHistory : listAll){
                    try {
                        if(formatter.parse(hikingHistory.date).compareTo(minDate) == 0){
                            nearbyHiking = hikingHistory;
                            binding.nameValue.setText(nearbyHiking.name);
                            binding.locationValue.setText(nearbyHiking.location);
                            binding.buttonShowMap.setOnClickListener(view -> {
                                new AlertDialog.Builder(this.requireContext())
                                        .setTitle("Start Hiking!")
                                        .setMessage("This will close your current Hiking. Are you sure you want to start this for new hiking?")
                                        .setPositiveButton("Start", (dialog, which) -> {
                                            if(currentHiking != null) {
                                                currentHiking.status = "Close";
                                                historyViewModel.updateHistory(currentHiking);
                                            }
                                            Bundle bundle = new Bundle();
                                            bundle.putLong("id",hikingHistory.id);
                                            hikingHistory.status = "Active";
                                            historyViewModel.updateHistory(hikingHistory);
                                            Toast.makeText(this.requireActivity(), hikingHistory.name + " is started!", Toast.LENGTH_SHORT).show();

                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();

                            });
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else{
                binding.nextHiking.setVisibility(View.GONE);
            }


        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}