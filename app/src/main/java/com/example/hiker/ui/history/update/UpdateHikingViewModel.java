package com.example.hiker.ui.history.update;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.hiker.database.entity.HikingHistory;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateHikingViewModel extends AndroidViewModel {
    MutableLiveData<HikingHistory> hikingUpdate = new MutableLiveData<>();
    MutableLiveData<String> day = new MutableLiveData<>();
    MutableLiveData<String> month = new MutableLiveData<>();
    MutableLiveData<String> year = new MutableLiveData<>();
    MutableLiveData<String> hour = new MutableLiveData<>();
    MutableLiveData<String> minute = new MutableLiveData<>();
    MutableLiveData<String> location = new MutableLiveData<>();
    MutableLiveData<String> name = new MutableLiveData<>();
    MutableLiveData<ArrayList<GeoPoint>> waypointsUpdate = new MutableLiveData<>();
    MutableLiveData<String> level = new MutableLiveData<>();
    MutableLiveData<String> length = new MutableLiveData<>();
    MutableLiveData<String> lengthUnit = new MutableLiveData<>();
    MutableLiveData<String> description = new MutableLiveData<>();
    MutableLiveData<Boolean> parking = new MutableLiveData<>(false);
    public final ExecutorService dataWriteExecutor =
            Executors.newSingleThreadExecutor();
    public UpdateHikingViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<HikingHistory> getHikingUpdate() {
        return hikingUpdate;
    }

    public void setHikingUpdate(HikingHistory hikingUpdate) {
        this.hikingUpdate.postValue(hikingUpdate);
    }

    public MutableLiveData<String> getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day.postValue(day);
    }

    public MutableLiveData<String> getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month.postValue(month);
    }

    public MutableLiveData<String> getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year.postValue(year);
    }

    public MutableLiveData<String> getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour.postValue(hour);
    }

    public MutableLiveData<String> getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute.postValue(minute);
    }

    public MutableLiveData<String> getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location.postValue(location);
    }

    public MutableLiveData<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.postValue(name);
    }

    public MutableLiveData<ArrayList<GeoPoint>> getWaypointsUpdate() {
        return waypointsUpdate;
    }
    public ArrayList<GeoPoint> getWaypointValue(){
        if(waypointsUpdate.getValue() == null){
            return new ArrayList<>();
        }
        else return waypointsUpdate.getValue();
    }
    public void addWaypoint(GeoPoint geoPoint){
        ArrayList<GeoPoint> newArr = getWaypointValue();
        newArr.add(geoPoint);
        setwaypointsUpdate(newArr);
    }
    public void removeWaypoint(int index){
        ArrayList<GeoPoint> newArr = getWaypointValue();
        newArr.remove(index);
        setwaypointsUpdate(newArr);
    }

    public void setwaypointsUpdate(ArrayList<GeoPoint> waypointsUpdate) {
        this.waypointsUpdate.postValue(waypointsUpdate);
    }

    public MutableLiveData<String> getLevel() {
        return level;
    }

    public void setLevel(String  level) {
        this.level.postValue(level);
    }

    public MutableLiveData<String> getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length.postValue(length);
    }

    public MutableLiveData<String> getLengthUnit() {
        return lengthUnit;
    }

    public void setLengthUnit(String lengthUnit) {
        this.lengthUnit.postValue(lengthUnit);
    }

    public MutableLiveData<String> getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description.postValue(description);
    }

    public MutableLiveData<Boolean> getParking() {
        return parking;
    }
    public boolean getParkingValue(){
        return Boolean.TRUE.equals(parking.getValue());
    }

    public void setParking(Boolean parking) {
        this.parking.postValue(parking);
    }
}
