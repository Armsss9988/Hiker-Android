package com.example.hiker.functions;
import static android.content.ContentValues.TAG;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hiker.R;
import com.example.hiker.ui.map.MapViewModel;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapAndLocation {
    private static MapAndLocation INSTANCE = null;
    private final WeakReference<Context> context;
    ArrayList<Marker> markers;
    Polyline roadOverlay;
    MapViewModel mapViewModel;
    Drawable nodeIcon;
    RoadManager roadManager;

    public MapAndLocation(Context context) {
        this.context = new WeakReference<>(context);
        markers = new ArrayList<>();
        mapViewModel = new ViewModelProvider((FragmentActivity)context).get(MapViewModel.class);
        nodeIcon = ResourcesCompat.getDrawable(context.getResources(),R.drawable.marker_node,null);
        roadManager = new OSRMRoadManager(context, context.getApplicationContext().getPackageName());
        ((OSRMRoadManager)roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);
    }

    public static MapAndLocation getInstance(Context context) {
        if (INSTANCE == null || INSTANCE.context.get() != context) {
            INSTANCE = new MapAndLocation(context);
        }
        return INSTANCE;
    }
    public Double getLengthOfRoad(ArrayList<GeoPoint> waypoints){
        Road road = roadManager.getRoad(waypoints);
        DecimalFormat df = new DecimalFormat("0.000000");
        return Double.parseDouble(df.format(road.mLength));
    }

    public String findCenterLocationOfWaypoints(ArrayList<GeoPoint> waypoints) throws IOException {
        Double latX = 0d, longY = 0d;
        for(GeoPoint geoPoint : waypoints){
            latX += geoPoint.getLatitude();
            longY += geoPoint.getLongitude();
        }
        Geocoder geocoder = new Geocoder(context.get().getApplicationContext());
        List<Address> centerLocation = geocoder.getFromLocation(latX/waypoints.size(),longY/waypoints.size(),1);
        if(centerLocation != null){
            Address address = centerLocation.get(0);
            String subAdminArea = (address.getSubAdminArea() != null ) ? address.getSubAdminArea() : "";
            String adminArea = (address.getAdminArea() != null ) ? ", " + address.getAdminArea() : "";
            String countryName = (address.getCountryName() != null ) ? ", " + address.getCountryName() : "";
            return subAdminArea + adminArea + countryName;
        }
        else{
            return "";
        }
    }
    public GeoPoint getMapLocation(String location) throws IOException {
        Geocoder geocoder = new Geocoder(context.get().getApplicationContext());
        List<Address> listAddress = geocoder.getFromLocationName(location,1);
        if(listAddress != null && listAddress.size() > 0){
            Address address = listAddress.get(0);
            return new GeoPoint(address.getLatitude(),address.getLongitude());
        }
        else{
            Toast.makeText(context.get(), "Can't find location!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
    public Address getLocationAddress(GeoPoint geoPoint){
        Geocoder geocoder = new Geocoder(context.get().getApplicationContext());
        try {
            return geocoder.getFromLocation(geoPoint.getLatitude(),geoPoint.getLongitude(),1).get(0);
        }
        catch (Exception e){
            Log.i(TAG, "getLocationName: ");
            return null;
        }
    }
    public void buildRoadOverlay(MapView map , ArrayList<GeoPoint> waypoints){
        map.getOverlays().removeAll(markers);
        Road road = roadManager.getRoad(waypoints);
        map.getOverlays().remove(roadOverlay);
        roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);
        roadOverlay.getOutlinePaint().setStrokeWidth(20);
        markers = new ArrayList<>();
        for (int i=0; i< waypoints.size(); i++){
            String location = getLocationAddress(waypoints.get(i)).getAddressLine(0);
            mapViewModel.addLocationStringData(waypoints.get(i),location);
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(waypoints.get(i));
            nodeMarker.setIcon(nodeIcon);
            if(getLocationAddress(waypoints.get(i)) != null){
                nodeMarker.setTitle("Turn way: " + location);
            }
            else nodeMarker.setTitle("Turn way: " + waypoints);
            Drawable icon = ResourcesCompat.getDrawable(context.get().getResources(), R.drawable.baseline_arrow_upward_24,null);
            nodeMarker.setImage(icon);
            markers.add(nodeMarker);
            map.getOverlays().add(nodeMarker);
        }
        map.invalidate();

    }
    public void clearMap(MapView map){
        map.getOverlays().remove(roadOverlay);
        mapViewModel.setWaypointsRoute(new ArrayList<>());
        map.getOverlays().removeIf(overlay -> overlay instanceof Marker);
    }
    public void revertBack() {
        mapViewModel.removeLastWaypoint();
    }
    public ArrayList<String> waypointsToListLocation(ArrayList<GeoPoint> waypoints){
        ArrayList<String> waypointsLocation = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context.get().getApplicationContext());
        for (GeoPoint waypoint : waypoints) {
            try {
                waypointsLocation.add(geocoder.getFromLocation(waypoint.getLatitude()
                        ,waypoint.getLongitude(),1).get(0).getAddressLine(0));
            } catch (IOException e) {
                waypointsLocation.add(waypoint.toString());
                Log.i(TAG, "onWaypointChange: cant find location");
            }
        }
        return waypointsLocation;
    }
}
