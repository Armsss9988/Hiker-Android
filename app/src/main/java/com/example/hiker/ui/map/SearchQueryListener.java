package com.example.hiker.ui.map;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;

import com.example.hiker.functions.MapAndLocation;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchQueryListener implements SearchView.OnQueryTextListener {
    SearchView searchView;
    WeakReference<Context> context;
    MapView mapView;
    ExecutorService searchExecutor;
    public SearchQueryListener(Context context,SearchView searchView, MapView mapView) {
        this.context = new WeakReference<>(context);
        this.searchView = searchView;
        this.mapView = mapView;
        searchExecutor = Executors.newSingleThreadExecutor();
    }
    public SearchQueryListener(Context context,SearchView searchView) {
        this.context = new WeakReference<>(context);
        this.searchView = searchView;
        searchExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        String location = searchView.getQuery().toString();
        try {
            GeoPoint point = MapAndLocation.getInstance(context.get()).getMapLocation(location);
            if(point != null && mapView != null){
                mapView.getController().animateTo(point);
            }
        } catch (IOException e) {
            Log.i(TAG, "onQueryTextSubmit: " + location + " not found");
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

}
