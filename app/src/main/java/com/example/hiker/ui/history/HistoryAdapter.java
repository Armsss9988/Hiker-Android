package com.example.hiker.ui.history;

import static androidx.navigation.Navigation.findNavController;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiker.R;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.functions.MapAndLocation;
import com.example.hiker.ui.history.update.UpdateHikingViewModel;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.util.GeoPoint;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryAdapter extends ListAdapter<HikingHistory, HistoryAdapter.HistoryViewHolder> {
    private final View fragmentView;
    private final HistoryViewModel historyViewModel;
    private final UpdateHikingViewModel updateHikingViewModel;

    protected HistoryAdapter(@NonNull DiffUtil.ItemCallback<HikingHistory> diffCallback, View view, HistoryViewModel historyViewModel,UpdateHikingViewModel updateViewModel) {
        super(diffCallback);
        this.fragmentView = view;
        this.historyViewModel = historyViewModel;
        this.updateHikingViewModel = updateViewModel;
    }


    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_history,parent,false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        if(historyViewModel.getExpandedItem().get(getCurrentList().get(position)) == null){
            historyViewModel.getExpandedItem().put(getCurrentList().get(position), false);
        }
        HikingHistory history = getItem(position);
        holder.hikingName.setText(history.name);
        holder.hikingDate.setText(history.date);
        holder.hikingLocation.setText(history.location);
        holder.hikingStatus.setText(history.status);
        holder.statusLayout.removeAllViews();
        if(Objects.equals(history.status, "Close")){
            TextView buttonChangeStatus = new TextView(holder.itemView.getContext());
            buttonChangeStatus.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            buttonChangeStatus.setBackground(ResourcesCompat.getDrawable(holder.itemView.getRootView().getContext().getResources(),R.drawable.button_change_status,null));
            buttonChangeStatus.setText(R.string.re_new);
            buttonChangeStatus.setTextColor(ColorUtils.blendARGB(Color.parseColor("#FF990000"), Color.parseColor("#FF009900"), 0.5f));
            buttonChangeStatus.setPadding(20,0,20,0);
            buttonChangeStatus.setGravity(Gravity.CENTER);
            buttonChangeStatus.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            buttonChangeStatus.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setMessage("Are you sure to re-new this Hiking?")
                        .setPositiveButton("Change", (dialog, id) -> {
                            history.status = "Waiting";
                            historyViewModel.updateHistory(history);
                        })
                        .setNegativeButton("Cancel", (dialog, id) -> {
                            // User cancelled the dialog
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            });
            holder.statusLayout.addView(buttonChangeStatus);
        }
        if(historyViewModel.getExpandedItem().get(getCurrentList().get(position))){
            holder.hikingDescription.setText(history.description);
            holder.hikingPark.setText(history.haveParking);
            holder.hikingLevel.setText(history.level);
            holder.hikingLength.setText(String.valueOf(history.length));
            holder.hiddenLayout.setVisibility(View.VISIBLE);
        }
        else {
            holder.hiddenLayout.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view->{
            historyViewModel.getExpandedItem().put(getCurrentList().get(position), !historyViewModel.getExpandedItem().get(getCurrentList().get(position)));
            notifyItemChanged(position);
        });
        holder.editBtn.setOnClickListener(view -> {
            updateHikingViewModel.dataWriteExecutor.execute(()->{
                InputData(getItem(position));
            });
            findNavController(fragmentView).navigate(R.id.action_nav_history_to_nav_update);
        });
        holder.deleteBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(this.fragmentView.getContext())
                    .setTitle("Delete Hiking")
                    .setMessage("Are you sure you want to delete this hiking?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        historyViewModel.getExpandedItem().remove(getCurrentList().get(position));
                        historyViewModel.deleteHiking(history);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });
    }
    void InputData(HikingHistory updateHiking){
        String timeHiking = updateHiking.date;
        String[] timeSet = timeHiking.split("-|:| ");
        updateHikingViewModel.setHikingUpdate(updateHiking);
        updateHikingViewModel.setDay(timeSet[0]);
        updateHikingViewModel.setMonth(timeSet[1]);
        updateHikingViewModel.setYear(timeSet[2]);
        updateHikingViewModel.setHour(timeSet[3]);
        updateHikingViewModel.setMinute(timeSet[4]);
        updateHikingViewModel.setName(updateHiking.name);
        updateHikingViewModel.setLocation(updateHiking.location);
        updateHikingViewModel.setDescription(updateHiking.description);
        updateHikingViewModel.setLength(String.valueOf(updateHiking.length));
        updateHikingViewModel.setLengthUnit(updateHiking.unit);
        updateHikingViewModel.setLevel(updateHiking.level);
        updateHikingViewModel.setParking((Objects.equals(updateHiking.haveParking, "Yes")));
        ArrayList<GeoPoint> passArr = new ArrayList<>(updateHiking.waypoints);
        updateHikingViewModel.setwaypointsUpdate(passArr);
    }
    static class HikingHistoryDiff extends DiffUtil.ItemCallback<HikingHistory>{


        @Override
        public boolean areItemsTheSame(@NonNull HikingHistory oldItem, @NonNull HikingHistory newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull HikingHistory oldItem, @NonNull HikingHistory newItem) {
            return false;
        }
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{
        TextView hikingName, hikingDate, hikingLocation, hikingLength, hikingPark, hikingLevel, hikingDescription, hikingStatus;
        View hiddenLayout;
        LinearLayout statusLayout;
        MaterialButton deleteBtn, editBtn;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            hikingStatus = itemView.findViewById(R.id.hiking_status);
            hikingName = itemView.findViewById(R.id.hiking_name);
            hikingDate = itemView.findViewById(R.id.hiking_date);
            hikingLocation = itemView.findViewById(R.id.hiking_location);
            hikingPark = itemView.findViewById(R.id.hiking_park);
            hikingLength = itemView.findViewById(R.id.hiking_length);
            hikingLevel = itemView.findViewById(R.id.hiking_level);
            hikingDescription = itemView.findViewById(R.id.hiking_description);
            hiddenLayout = itemView.findViewById(R.id.hiding_panel);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            editBtn = itemView.findViewById(R.id.edit_btn);
            statusLayout = itemView.findViewById(R.id.status);
        }
    }
}
