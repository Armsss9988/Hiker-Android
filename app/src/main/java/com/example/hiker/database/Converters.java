package com.example.hiker.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<GeoPoint> fromString(String value) {
        Type listType = new TypeToken<ArrayList<GeoPoint>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<GeoPoint> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
