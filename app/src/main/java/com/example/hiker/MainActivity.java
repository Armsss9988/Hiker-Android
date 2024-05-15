package com.example.hiker;

import static android.content.ContentValues.TAG;

import android.icu.text.SimpleDateFormat;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.view.Menu;

import com.example.hiker.database.HikerDatabase;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.ui.history.HistoryViewModel;
import com.example.hiker.ui.home.HomeFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.hiker.databinding.ActivityMainBinding;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    NavController navController;
    Thread updateTimeThread;
    HistoryViewModel historyViewModel;
    SimpleDateFormat formatter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        updateStatusByTime();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,R.id.nav_history, R.id.OSMMapFragment2, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
    //Add item for action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    private void updateStatusByTime() {
        updateTimeThread = new Thread( new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        update();
                        Thread.sleep(1000);
                    } catch (InterruptedException | ParseException e) {
                        Log.i(TAG, "run: ");
                    }
                }
            }

            public void update() throws ParseException {
                if(historyViewModel.getRoomHistory().getValue() != null){
                    for(HikingHistory hikingHistory : historyViewModel.getRoomHistory().getValue()){
                        if(!Objects.equals(hikingHistory.status, "Active") && !Objects.equals(hikingHistory.status, "Close")){
                            if(!Objects.equals(hikingHistory.status, "Waiting") && formatter.parse(hikingHistory.date).compareTo(new Date()) > 0){
                                hikingHistory.status = "Waiting";
                                historyViewModel.updateHistory(hikingHistory);
                            }
                            else if (!Objects.equals(hikingHistory.status, "Expired" )
                                    &&  formatter.parse(hikingHistory.date).compareTo(new Date()) < 0){
                                hikingHistory.status = "Expired";
                                historyViewModel.updateHistory(hikingHistory);
                            }
                        }
                    }
                }

            }

        });
        updateTimeThread.start();
    }


}