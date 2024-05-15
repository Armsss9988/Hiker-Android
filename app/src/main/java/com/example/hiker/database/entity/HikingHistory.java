package com.example.hiker.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

@Entity(tableName = "Hiking Histories")
public class HikingHistory {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "Name of the hike")
    public String name;
    @ColumnInfo(name = "Location")
    public String location;
    @ColumnInfo(name = "Route")
    public ArrayList<GeoPoint> waypoints;
    @ColumnInfo(name = "Date of the hike")
    public String date;
    @ColumnInfo(name = "Parking available")
    public String haveParking;
    @ColumnInfo(name = "Length of the hike")
    public double length;
    @ColumnInfo(name = "Unit of Length")
    public String unit;
    @ColumnInfo(name = "Difficulty level")
    public String level;
    @ColumnInfo(name = "Description")
    public String description;
    @ColumnInfo(name = "Status")
    public String status;
}
