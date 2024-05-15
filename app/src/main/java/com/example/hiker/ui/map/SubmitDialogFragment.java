package com.example.hiker.ui.map;

import static android.content.ContentValues.TAG;
import static androidx.navigation.Navigation.findNavController;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.hiker.R;
import com.example.hiker.databinding.FragmentAddNewBinding;
import com.example.hiker.databinding.SubmitRouteDialogBinding;
import com.example.hiker.functions.MapAndLocation;
import com.example.hiker.ui.history.addnew.AddHikingViewModel;
import com.example.hiker.ui.history.update.UpdateHikingViewModel;

import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class SubmitDialogFragment extends DialogFragment {
    SubmitRouteDialogBinding binding;
    MapViewModel mapViewModel;
    AddHikingViewModel addHikingViewModel;
    UpdateHikingViewModel updateHikingViewModel;
    SubmitDialogViewModel submitDialogViewModel;
    LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SubmitRouteDialogBinding.inflate(inflater,container,false);
        mapViewModel = new ViewModelProvider(this.requireActivity()).get(MapViewModel.class);
        updateHikingViewModel = new ViewModelProvider(this.requireActivity()).get(UpdateHikingViewModel.class);
        addHikingViewModel = new ViewModelProvider(this.requireActivity()).get(AddHikingViewModel.class);
        submitDialogViewModel = new ViewModelProvider(this.requireActivity()).get(SubmitDialogViewModel.class);
        linearLayout = binding.checkPanel;
        submitDialogViewModel.setWaypoints(new ArrayList<>(mapViewModel.getWaypointRouteValue()));
        Dictionary<GeoPoint,String> dataLocation = mapViewModel.getLocationStringData();
        for(GeoPoint p : mapViewModel.getWaypointRouteValue()){
            String location = dataLocation.get(p);
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(location);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            checkBox.setChecked(true);
            checkBox.setOnCheckedChangeListener((compoundButton, b) -> handleCheckBox());
            linearLayout.addView(checkBox);
        }
        binding.locationCheck.setChecked(true);
        binding.buttonCancel.setOnClickListener(view -> {
            SubmitDialogFragment.this.getDialog().cancel();
        });
        binding.buttonDone.setOnClickListener(view -> {
            onSubmit();

        });
        onLiveDateChange();
        return binding.getRoot();
    }



    void handleCheckBox(){
        ArrayList<GeoPoint> newWaypoint = new ArrayList<>();
        for(int i = 0; i < linearLayout.getChildCount(); i++){
            View child = linearLayout.getChildAt(i);
            if(child instanceof CheckBox){
                if (((CheckBox) child).isChecked()){
                    newWaypoint.add(mapViewModel.getWaypointRouteValue().get(i));
                }
            }
        }
        submitDialogViewModel.setWaypoints(newWaypoint);
    }
    void onSubmit(){
        if(Objects.equals(this.getTag(), "Update")){
            updateHikingViewModel.setwaypointsUpdate(submitDialogViewModel.getWaypointValue());
            if(binding.locationCheck.isChecked()){
                updateHikingViewModel.setLocation(binding.locationCheck.getText().toString());
            }
            if(binding.lengthCheck.isChecked()){
                updateHikingViewModel.setLength(binding.lengthCheck.getText().toString());
                updateHikingViewModel.setLengthUnit("km");
            }
        }
        else {
            addHikingViewModel.setWaypoints(submitDialogViewModel.getWaypointValue());
            if(binding.locationCheck.isChecked()){
                addHikingViewModel.setLocation(binding.locationCheck.getText().toString());
            }
            if(binding.lengthCheck.isChecked()){
                addHikingViewModel.setLength(binding.lengthCheck.getText().toString());
                addHikingViewModel.setLengthUnit("km");
            }
        }
        SubmitDialogFragment.this.getDialog().cancel();
        findNavController(getParentFragment().getView()).popBackStack();
    }
    void onLiveDateChange(){
       submitDialogViewModel.getWaypoints().observe(getViewLifecycleOwner(), waypoints -> {
               Executor executor = Executors.newSingleThreadExecutor();
               Handler handler = new Handler(Looper.getMainLooper());
               executor.execute(() -> {
                   String location, length;
                   try {
                       location = MapAndLocation.getInstance(requireActivity()).findCenterLocationOfWaypoints(waypoints);
                       length = String.valueOf(MapAndLocation.getInstance(requireActivity()).getLengthOfRoad(waypoints));
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
                   handler.post(() -> {
                       binding.locationCheck.setText(location);
                       binding.lengthCheck.setText(length);
                   });
               });
       });
    }
}
