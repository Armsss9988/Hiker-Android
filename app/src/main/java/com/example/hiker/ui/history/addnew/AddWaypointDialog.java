package com.example.hiker.ui.history.addnew;

import static android.content.ContentValues.TAG;
import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.hiker.databinding.AddWaypointDialogBinding;
import com.example.hiker.functions.MapAndLocation;
import com.example.hiker.ui.history.update.UpdateHikingViewModel;
import com.example.hiker.ui.map.SearchQueryListener;
import org.osmdroid.util.GeoPoint;
import java.io.IOException;

public class AddWaypointDialog extends DialogFragment {
    AddWaypointDialogBinding binding;
    AddHikingViewModel addHikingViewModel;
    UpdateHikingViewModel updateHikingViewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddWaypointDialogBinding.inflate(inflater,container,false);
        addHikingViewModel = new ViewModelProvider(this.requireActivity()).get(AddHikingViewModel.class);
        updateHikingViewModel = new ViewModelProvider(this.requireActivity()).get(UpdateHikingViewModel.class);
        binding.searchView.setOnQueryTextListener(new SearchQueryListener(requireActivity(),binding.searchView));
        binding.buttonCancel.setOnClickListener(view -> {
            AddWaypointDialog.this.getDialog().cancel();
        });
        binding.buttonDone.setOnClickListener(view -> {
            try {
                GeoPoint geoPoint = MapAndLocation.getInstance(requireActivity()).getMapLocation(binding.searchView.getQuery().toString());
                if(this.getTag() == "Update"){
                    updateHikingViewModel.addWaypoint(geoPoint);
                }
                else{
                    addHikingViewModel.addWaypoint(geoPoint);
                }
                AddWaypointDialog.this.getDialog().cancel();
            } catch (IOException e) {
                Toast.makeText(this.requireActivity(), "can't find waypoint", Toast.LENGTH_SHORT).show();
            }
        });
        return binding.getRoot();
    }
}
