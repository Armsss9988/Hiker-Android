package com.example.hiker.ui.map;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import static androidx.navigation.Navigation.findNavController;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.hiker.R;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.databinding.FragmentActiveHikingBinding;
import com.example.hiker.databinding.FragmentMapsBinding;
import com.example.hiker.functions.MapAndLocation;
import com.example.hiker.ui.history.HistoryViewModel;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActiveMapFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_PERMISSION_CODE = 99;
    FragmentActiveHikingBinding binding;
    HikingHistory hikingHistory;
    HistoryViewModel historyViewModel;
    MyLocationNewOverlay myLocationNewOverlay;
    ExecutorService renderMapExecutor;
    IMapController mapController;
    String imgDirectory;
    private MapView map = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActiveHikingBinding.inflate(inflater, container, false);
        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        map = binding.map;
        mapController = map.getController();
        assert getArguments() != null;
        int id = (int) getArguments().getLong("id") ;
        int[] idArr = {id};
        historyViewModel.GetHistoriesByID(idArr).observe(getViewLifecycleOwner(),hikingHistoryFound -> {
            hikingHistory = hikingHistoryFound.get(0);
            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setTitle(hikingHistory.name.toUpperCase());
            imgDirectory = hikingHistory.name;
            MapAndLocation.getInstance(requireActivity()).buildRoadOverlay(map, hikingHistory.waypoints);
            mapController.animateTo(hikingHistory.waypoints.get(0));
        });
        renderMapExecutor = Executors.newSingleThreadExecutor();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context ctx = this.requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getTileProvider().getTileCache().getProtectedTileComputers().clear();
        requestPermissionsIfNecessary(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
                }
        );
        if (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED){
            GpsMyLocationProvider provider = new GpsMyLocationProvider(this.requireContext());
            provider.addLocationSource(LocationManager.NETWORK_PROVIDER);
            myLocationNewOverlay = new MyLocationNewOverlay(provider, map);
            map.getOverlayManager().add(myLocationNewOverlay);
        }
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapController = map.getController();
        mapController.setZoom(15.0);
        binding.gps.setOnClickListener(view ->{
            GeoPoint myLocation = myLocationNewOverlay.getMyLocation();
            Log.i(TAG, "onCreateView: location: " + myLocation);
            mapController.animateTo(myLocation);
        });
        binding.doneButton.setOnClickListener(view -> {
            hikingHistory.status = "Close";
            historyViewModel.updateHistory(hikingHistory);
            findNavController(this.getView()).popBackStack();
        });
        binding.photo.setOnClickListener(view ->{
            Bundle bundle = new Bundle();
            bundle.putString("dir",imgDirectory);
            findNavController(this.getView()).navigate(R.id.action_nav_active_to_nav_image_active, bundle);
        });
        binding.camera.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    this.requireActivity(),
                    Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this.requireActivity(),new String[]{Manifest.permission.CAMERA},REQUEST_IMAGE_PERMISSION_CODE);

            }
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri imgPath = createImg();
            cam.putExtra(MediaStore.EXTRA_OUTPUT, imgPath);
            startActivityForResult(cam,REQUEST_IMAGE_CAPTURE);
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(this.requireActivity().getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this.requireActivity().getApplicationContext()));
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
                    Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
                } else {
                    Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                }
            }
    );

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

        }
    }
    Uri createImg(){
        Uri uri = null;
        ContentResolver contentResolver = requireActivity().getContentResolver();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        else {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        String imgName = String.valueOf(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imgName + ".jpg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/HikerApp/" + imgDirectory + "/");
        Log.i(TAG, "createImg: dir: " + imgDirectory);
        Uri resUri = contentResolver.insert(uri,contentValues);
        return resUri;
    }
}
