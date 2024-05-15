package com.example.hiker.ui.map;

import static android.content.ContentValues.TAG;

import static androidx.navigation.Navigation.findNavController;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hiker.databinding.FragmentMapsBinding;
import com.example.hiker.functions.MapAndLocation;
import com.example.hiker.ui.history.update.UpdateHikingViewModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OSMMapFragment extends Fragment {

    FragmentMapsBinding binding;
    Marker myLocationMarker;
    MyLocationNewOverlay myLocationNewOverlay = null;
    ExecutorService renderMapExecutor;
    MapViewModel mapViewModel;
    IMapController mapController;
    UpdateHikingViewModel updateHikingViewModel;
    private MapView map = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        map = binding.map;
        renderMapExecutor = Executors.newSingleThreadExecutor();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context ctx = this.requireActivity().getApplicationContext();
        mapViewModel = new ViewModelProvider(this.requireActivity()).get(MapViewModel.class);
        updateHikingViewModel = new ViewModelProvider(this.requireActivity()).get(UpdateHikingViewModel.class);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getTileProvider().getTileCache().getProtectedTileComputers().clear();
        requestPermissionsIfNecessary(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
                }
        );

        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapController = map.getController();
        mapController.setZoom(15.0);

        if (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED){
            GpsMyLocationProvider provider = new GpsMyLocationProvider(this.requireContext());
            provider.addLocationSource(LocationManager.NETWORK_PROVIDER);
            myLocationNewOverlay = new MyLocationNewOverlay(provider, map);
            map.getOverlayManager().add(myLocationNewOverlay);
        }
        if (mapViewModel.getWaypointRouteValue().size() != 0) {
            mapController.setCenter(mapViewModel.getWaypointRouteValue().get(0));
        }
        else {
            mapController.setCenter(new GeoPoint(16.0545d,108.0717d));
        }
        binding.myLocation.setOnClickListener(view ->{
            if(myLocationNewOverlay != null){
                GeoPoint myLocation = myLocationNewOverlay.getMyLocation();
                mapController.animateTo(myLocation);
            }
            else {
                Toast.makeText(requireContext(),"Require access location permission!", Toast.LENGTH_SHORT).show();
            }
        });
        map.getOverlays().add(new MapEventsOverlay(new AppMapEventReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                mapViewModel.addWaypointRoute(p);
                return false;
            }
        }));
        binding.searchView.setOnQueryTextListener(new SearchQueryListener(requireActivity(), binding.searchView, map));
        binding.clearButton.setOnClickListener(view -> {
            MapAndLocation.getInstance(requireActivity()).clearMap(map);
        });
        binding.backButton.setOnClickListener(view -> MapAndLocation.getInstance(requireActivity()).revertBack());
        binding.submitButton.setOnClickListener(view -> {
            if(renderMapExecutor.isTerminated()){
                showNoticeDialog();
            }
            else{
                if(mapViewModel.getWaypointsRoute().getValue() == null || mapViewModel.getWaypointsRoute().getValue().size() <= 1){
                    Toast.makeText(requireContext(),"Please choose your Hiking road", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(requireContext(),"Waiting for finish map render!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        onLiveDataChange();
        return binding.getRoot();
    }
    void onLiveDataChange() {
        mapViewModel.getWaypointsRoute().observe(getViewLifecycleOwner(), waypoints -> {
            if (waypoints.size() == 1) {
                Marker marker = new Marker(map);
                marker.setPosition(waypoints.get(0));
                map.getOverlays().add(marker);
            } else {
                renderMapExecutor.shutdownNow();
                renderMapExecutor = Executors.newSingleThreadExecutor();
                renderMapExecutor.submit(() -> MapAndLocation.getInstance(requireActivity()).buildRoadOverlay(map,waypoints));
                renderMapExecutor.shutdown();
            }
        });
        mapViewModel.getMyLocation().observe(getViewLifecycleOwner(), myLocation -> {
            myLocationMarker.setPosition(myLocation);
            myLocationMarker.setTitle("You are here");
            map.invalidate();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(this.requireActivity().getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this.requireActivity().getApplicationContext()));
        //add
        if(myLocationNewOverlay != null){
            myLocationNewOverlay.enableMyLocation();
        }
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().load(this.requireActivity().getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this.requireActivity().getApplicationContext()));

        if(myLocationNewOverlay != null){
            myLocationNewOverlay.disableMyLocation();
        }
        map.onPause();
    }
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this.requireActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            for (String permission : permissionsToRequest) {
                requestPermissionLauncher.launch(permission);
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    if (ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED){
                        GpsMyLocationProvider provider = new GpsMyLocationProvider(requireContext());
                        provider.addLocationSource(LocationManager.NETWORK_PROVIDER);
                        myLocationNewOverlay = new MyLocationNewOverlay(provider, map);
                        map.getOverlayManager().add(myLocationNewOverlay);
                    }
                } else {
                    Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                }
            }
    );

    public void showNoticeDialog() {
        if(Objects.equals(getArguments().getString("key"), "New")){
            DialogFragment dialog = new SubmitDialogFragment();
            dialog.show(getParentFragmentManager(), "New");
        }
        else{
            DialogFragment dialog = new SubmitDialogFragment();
            dialog.show(getParentFragmentManager(), "Update");
        }

    }


}
