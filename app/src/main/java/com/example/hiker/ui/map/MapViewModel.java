package com.example.hiker.ui.map;

import android.app.Application;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class MapViewModel extends AndroidViewModel {
    MutableLiveData<ArrayList<GeoPoint>> waypointsRoute = new MutableLiveData<>();
    Dictionary<GeoPoint,String> locationStringData = new Hashtable<>();
    public MutableLiveData<GeoPoint> myLocation = new MutableLiveData<>();
    public MapViewModel(@NonNull Application application) {
        super(application);
    }

    public Dictionary<GeoPoint, String> getLocationStringData() {
        return locationStringData;
    }

    public void addLocationStringData(GeoPoint p, String locationString) {
        this.locationStringData.put(p,locationString);
    }

    public MutableLiveData<GeoPoint> getMyLocation(){
        return this.myLocation;
    }
    public MutableLiveData<ArrayList<GeoPoint>> getWaypointsRoute() {
        return this.waypointsRoute;
    }
    public ArrayList<GeoPoint> getWaypointRouteValue(){
        if(this.waypointsRoute.getValue() == null){
            return new ArrayList<>();
        }
        else return this.waypointsRoute.getValue();
    }

    public void setWaypointsRoute(ArrayList<GeoPoint> waypoints) {
        this.waypointsRoute.setValue(new ArrayList<>());
        this.waypointsRoute.setValue(waypoints);
    }
    public void addWaypointRoute(GeoPoint geoPoint){
        ArrayList<GeoPoint> currentList = this.waypointsRoute.getValue();
        if(currentList == null){
            currentList = new ArrayList<>();
        }
        currentList.add(geoPoint);
        this.setWaypointsRoute(currentList);
    }
    public void removeLastWaypoint(){
        ArrayList<GeoPoint> currentList = this.waypointsRoute.getValue();
        if(currentList != null){
            if(currentList.size() > 0){
                currentList.remove(currentList.size()-1);
                this.setWaypointsRoute(currentList);
            }
        }

    }
}
