package com.example.hiker.ui.map;

import android.app.Application;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class SubmitDialogViewModel extends AndroidViewModel {
    MutableLiveData<ArrayList<GeoPoint>> waypoints;
    public SubmitDialogViewModel(@NonNull Application application) {
        super(application);
        waypoints = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<GeoPoint>> getWaypoints() {
        return waypoints;
    }
    public ArrayList<GeoPoint> getWaypointValue(){
        if(waypoints.getValue() == null){
            return new ArrayList<>();
        }
        else return waypoints.getValue();
    }
    public void setWaypoints(ArrayList<GeoPoint> waypoints) {
        this.waypoints.setValue(waypoints);
    }
}
