package com.example.hiker.ui.history.addnew;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class AddHikingViewModel extends AndroidViewModel {
    MutableLiveData<String> day = new MutableLiveData<>();
    MutableLiveData<String> month = new MutableLiveData<>();
    MutableLiveData<String> year = new MutableLiveData<>();
    MutableLiveData<String> hour = new MutableLiveData<>();
    MutableLiveData<String> minute = new MutableLiveData<>();
    MutableLiveData<String> location = new MutableLiveData<>();
    MutableLiveData<String> name = new MutableLiveData<>();
    MutableLiveData<ArrayList<GeoPoint>> waypoints = new MutableLiveData<>();
    MutableLiveData<String> level = new MutableLiveData<>();
    MutableLiveData<String> length = new MutableLiveData<>();
    MutableLiveData<String> lengthUnit = new MutableLiveData<>();
    MutableLiveData<String> description = new MutableLiveData<>();
    MutableLiveData<Boolean> parking = new MutableLiveData<>(false);
    public AddHikingViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day.setValue(day);
    }

    public MutableLiveData<String> getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month.setValue(month);
    }

    public MutableLiveData<String> getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year.setValue(year);
    }

    public MutableLiveData<String> getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour.setValue(hour);
    }

    public MutableLiveData<String> getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute.setValue(minute);
    }

    public MutableLiveData<String> getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location.setValue(location);
    }

    public MutableLiveData<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.setValue(name);
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
    public void addWaypoint(GeoPoint geoPoint){
        ArrayList<GeoPoint> newArr = getWaypointValue();
        newArr.add(geoPoint);
        setWaypoints(newArr);
    }
    public void removeWaypoint(int index){
        ArrayList<GeoPoint> newArr = getWaypointValue();
        newArr.remove(index);
        setWaypoints(newArr);
    }

    public void setWaypoints(ArrayList<GeoPoint> waypoints) {
        this.waypoints.setValue(waypoints);
    }

    public MutableLiveData<String> getLevel() {
        return level;
    }

    public void setLevel(String  level) {
        this.level.setValue(level);
    }

    public MutableLiveData<String> getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length.setValue(length);
    }

    public MutableLiveData<String> getLengthUnit() {
        return lengthUnit;
    }

    public void setLengthUnit(String lengthUnit) {
        this.lengthUnit.setValue(lengthUnit);
    }

    public MutableLiveData<String> getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description.setValue(description);
    }

    public MutableLiveData<Boolean> getParking() {
        return parking;
    }
    public boolean getParkingValue(){
        return Boolean.TRUE.equals(parking.getValue());
    }

    public void setParking(Boolean parking) {
        this.parking.setValue(parking);
    }
    public void clearAll(){
        setDay("");
        setMonth("");
        setYear("");
        setHour("");
        setMinute("");
        setName("");
        setLocation("");
        setLength("");
        setLengthUnit("m");
        setWaypoints(new ArrayList<>());
        setParking(false);
        setLevel(null);
        setDescription("");
    }
}
