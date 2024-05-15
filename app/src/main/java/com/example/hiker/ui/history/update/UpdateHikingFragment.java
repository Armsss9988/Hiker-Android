package com.example.hiker.ui.history.update;

import static android.content.ContentValues.TAG;
import static androidx.navigation.Navigation.findNavController;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hiker.MainActivity;
import com.example.hiker.R;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.databinding.FragmentAddNewBinding;
import com.example.hiker.databinding.FragmentUpdateBinding;
import com.example.hiker.functions.MapAndLocation;
import com.example.hiker.functions.MyDragShadowBuilder;
import com.example.hiker.ui.history.HistoryViewModel;
import com.example.hiker.ui.history.addnew.AddHikingViewModel;
import com.example.hiker.ui.history.addnew.AddWaypointDialog;
import com.example.hiker.ui.history.addnew.AppTextWatcher;
import com.example.hiker.ui.history.addnew.DropdownAdapter;
import com.example.hiker.ui.history.addnew.InputRule;
import com.example.hiker.ui.map.MapViewModel;
import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UpdateHikingFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    private HikingHistory updateHiking = null;
    private FragmentUpdateBinding binding;
    HistoryViewModel historyViewModel;
    UpdateHikingViewModel updateHikingViewModel;
    MapViewModel mapViewModel;
    LinearLayout linearLayout;
    SimpleDateFormat formatter;
    Button submitButton;
    TextInputEditText date, month, year, hour, minute , length,location, name, description;
    AutoCompleteTextView levelSpinner,unitSpinner;
    SwitchCompat haveParking;
    String[] levelSelection = new String[]{"Low","Medium","High","Very high"};
    String[] unitSelection = new String[]{"m","km","foot","yard"};
    ArrayAdapter<String> levelAdapter, unitAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_update, container, false);
        historyViewModel = new ViewModelProvider(this.requireActivity()).get(HistoryViewModel.class);
        updateHikingViewModel = new ViewModelProvider(this.requireActivity()).get(UpdateHikingViewModel.class);
        binding.setUpdateHikingViewModel(updateHikingViewModel);
        binding.setLifecycleOwner(this);
        mapViewModel = new ViewModelProvider(this.requireActivity()).get(MapViewModel.class);
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        View root = binding.getRoot();
        setInputElement();
        setSpinnerElement();
        setButton(root);
        binding.chooseWithMap.setOnClickListener(view -> {
            if(updateHikingViewModel.getWaypointsUpdate().getValue() != null){
                ArrayList<GeoPoint> passArr = new ArrayList<>(updateHikingViewModel.getWaypointsUpdate().getValue());
                mapViewModel.setWaypointsRoute(passArr);
            }
            Bundle bundle = new Bundle();
            bundle.putString("key","Update");
            findNavController(binding.getRoot()).navigate(R.id.action_nav_update_to_OSMMapFragment, bundle);
        });
        onLiveDataChange();
        binding.buttonPlus.setOnClickListener(view -> {
            DialogFragment dialog = new AddWaypointDialog();
            dialog.show(getParentFragmentManager(),"Update");
        });
        return root;
    }
    void setButton(View rootView){
        Button chooseDateByCalendar = binding.calendar;
        chooseDateByCalendar.setOnClickListener(view -> openDialog());
        submitButton = binding.submitBtn;
        submitButton.setOnClickListener(view -> {
            try {
                if(checkInput()){
                    findNavController(rootView).navigate(R.id.action_nav_update_to_nav_history);
                }
            } catch (ParseException e) {
                Toast.makeText(this.getActivity(),"Invalid date input",Toast.LENGTH_SHORT).show();
            }
        });
    }

    void setInputElement(){
        location = binding.locationInput;
        linearLayout = binding.waypointRoute;
        name = binding.nameInput;
        length = binding.lengthInput;
        description = binding.descriptionInput;
        haveParking = binding.parkingInput;
        year = binding.timeSet.yearInput;
        hour = binding.timeSet.hourInput;
        hour.setFilters(new InputFilter[]{new InputRule(0,24)});
        date = binding.timeSet.dateInput;
        date.setFilters(new InputFilter[]{new InputRule(0,31)});
        month = binding.timeSet.monthInput;
        month.setFilters(new InputFilter[]{new InputRule(0,12)});
        minute =  binding.timeSet.minuteInput;
        minute.setFilters(new InputFilter[]{new InputRule(0,60)});
        SetTimeInputEven();
    }

    void onLiveDataChange(){
        updateHikingViewModel.getWaypointsUpdate().observe(getViewLifecycleOwner(),waypointsUpdate->{
            linearLayout.removeAllViews();
            List<String> listWaypointLocation = MapAndLocation.getInstance(this.requireActivity()).waypointsToListLocation(waypointsUpdate);
            for (String location : listWaypointLocation){
                TextView textView = new TextView(requireContext());
                textView.setText(location);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.location_and_route_bg,null));
                textView.setPadding(15,5,5,5);
                linearLayout.addView(textView);
                textView.setTag(location);
                textView.setOnLongClickListener( view ->{
                    View.DragShadowBuilder myShadow = new MyDragShadowBuilder(textView);
                    ClipData.Item item = new ClipData.Item((CharSequence) textView.getTag());
                    String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                    ClipData dragData = new ClipData(textView.getTag().toString(), mimeTypes, item);
                    view.startDragAndDrop(dragData,  // The data to be dragged.
                            myShadow,  // The drag shadow builder.
                            view,      // No need to use local data.
                            0          // Flags. Not currently used, set to 0.
                    );
                    return true;
                });
                textView.setOnDragListener(
                        (v, e) -> {
                            switch(e.getAction()) {
                                case DragEvent.ACTION_DRAG_STARTED:
                                    v.setBackgroundColor(Color.GRAY);
                                    v.invalidate();
                                    return true;

                                case DragEvent.ACTION_DRAG_ENTERED:
                                    v.setBackgroundColor(0x55555555);
                                    v.invalidate();
                                    return true;

                                case DragEvent.ACTION_DRAG_LOCATION:
                                    return true;

                                case DragEvent.ACTION_DRAG_EXITED:
                                    v.setBackgroundColor(Color.GRAY);
                                    return true;

                                case DragEvent.ACTION_DRAG_ENDED:
                                    for(int i = 0 ; i < linearLayout.getChildCount(); i++){
                                        linearLayout.getChildAt(i).setBackground(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.location_and_route_bg,null));
                                        linearLayout.getChildAt(i).invalidate();
                                    }
                                    return true;

                                case DragEvent.ACTION_DROP:
                                    int indexDragged = linearLayout.indexOfChild((View) e.getLocalState());
                                    try {
                                        int indexChange = linearLayout.indexOfChild(textView);
                                        Collections.swap(waypointsUpdate, indexDragged, indexChange);
                                        updateHikingViewModel.setwaypointsUpdate(waypointsUpdate);
                                        v.setBackgroundColor(Color.WHITE);
                                        ((GradientDrawable)v.getBackground()).setStroke(1,Color.BLACK);
                                        v.invalidate();
                                    } catch (Exception ecep){};
                                    return true;
                                default:
                                    Log.e("DragDrop Example","Unknown action type received by View.OnDragListener.");
                                    break;
                            }

                            return false;
                        });
            }
        });
        binding.remove.setOnDragListener(
                (v,e) ->{
                    switch(e.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:

                        case DragEvent.ACTION_DRAG_EXITED:
                            v.setBackgroundColor(Color.GRAY);
                            v.invalidate();
                            return true;

                        case DragEvent.ACTION_DRAG_ENTERED:
                            v.setBackgroundColor(Color.DKGRAY);
                            v.invalidate();
                            return true;

                        case DragEvent.ACTION_DRAG_LOCATION:
                            return true;

                        case DragEvent.ACTION_DRAG_ENDED:
                            binding.remove.setBackgroundColor(Color.WHITE);
                            return true;

                        case DragEvent.ACTION_DROP:
                            Toast.makeText(requireActivity(),"entered", Toast.LENGTH_SHORT).show();
                            int indexDragged = linearLayout.indexOfChild((View) e.getLocalState());
                            updateHikingViewModel.removeWaypoint(indexDragged);
                            binding.remove.setBackgroundColor(Color.WHITE);
                            return true;
                        default:
                            Log.e("DragDrop Example","Unknown action type received by View.OnDragListener.");
                            break;
                    }
                    return false;
                });
       /* updateHikingViewModel.getLocation().observe(getViewLifecycleOwner(),locationName -> location.setText(locationName));
        updateHikingViewModel.getLength().observe(getViewLifecycleOwner(),lengthValue -> length.setText(lengthValue));
        updateHikingViewModel.getLengthUnit().observe(getViewLifecycleOwner(),lengthUnit -> unitSpinner.setText(lengthUnit,false));*/
        updateHikingViewModel.getDay().observe(getViewLifecycleOwner(),day -> date.setText(day));
        updateHikingViewModel.getMonth().observe(getViewLifecycleOwner(), monthData -> month.setText(monthData));
        updateHikingViewModel.getYear().observe(getViewLifecycleOwner(), yearData -> year.setText(yearData));
        updateHikingViewModel.getHour().observe(getViewLifecycleOwner(), hourData -> hour.setText(hourData));
        updateHikingViewModel.getMinute().observe(getViewLifecycleOwner(), minuteData -> minute.setText(minuteData));
/*        updateHikingViewModel.getName().observe(getViewLifecycleOwner(), nameData -> name.setText(nameData));
        updateHikingViewModel.getParking().observe(getViewLifecycleOwner(), parkingData -> haveParking.setChecked(parkingData));
        updateHikingViewModel.getLevel().observe(getViewLifecycleOwner(), levelData -> levelSpinner.setText(levelData,false));
        updateHikingViewModel.getDescription().observe(getViewLifecycleOwner(), descriptionData -> description.setText(descriptionData));*/
    }
    void setSpinnerElement(){
        levelSpinner = binding.difficultySpinner;
        unitSpinner = binding.lengthTypeInput;
        levelAdapter = new DropdownAdapter<>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, levelSelection);
        unitAdapter = new DropdownAdapter<>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, unitSelection);
        levelAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        unitAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelAdapter);
        levelSpinner.setOnItemSelectedListener(this);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setOnItemSelectedListener(this);
    }

    void SetTimeInputEven(){
        List<TextInputEditText> editTextList  = new ArrayList<>();
        editTextList.add(date);
        editTextList.add(month);
        editTextList.add(year);
        editTextList.add(hour);
        editTextList.add(minute);
        for (TextInputEditText editText : editTextList) {
            editText.getHint();
            editText.addTextChangedListener(new AppTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // add a condition to check length here - you can give here length according to your requirement to go to next EditTexts.
                    if(editText.getText().toString().trim().length() >= ((editText.getHint().toString().equals("Year"))? 4 : 2)){

                        if(editText != editTextList.get(editTextList.size()-1)){
                            editText.clearFocus();
                            editTextList.get(editTextList.indexOf(editText)+1).requestFocus();
                        }
                    }
                }
            });
            editText.setOnFocusChangeListener((v, hasFocus) -> editText.setSelectAllOnFocus(true));
        }
    }
    public boolean checkInput() throws ParseException {
        if (date.getText().toString().equals("")
                ||this.month.getText().toString().equals("")
                ||this.year.getText().toString().equals("")
                ||this.hour.getText().toString().equals("")
                ||this.minute.getText().toString().equals("")
                || this.location.getText().toString().equals("")
                || this.name.getText().toString().equals("")
                || this.length.getText().toString().equals("")
                || this.levelSpinner.getText().toString().equals("")
                || this.description.getText().toString().equals("")){
            Toast.makeText(this.getActivity(), "Please fill all require field", Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            String datetimeText = date.getText().toString() + "-" + month.getText().toString() + "-" + year.getText().toString()
                    + " " + hour.getText().toString() + ":" + minute.getText().toString();
            Log.i(TAG, "checkInput: " + datetimeText);
            Log.i(TAG, "checkInput: " + levelSpinner.getText().toString());
            Log.i(TAG, "checkInput: " + unitSpinner.getText().toString());
            updateHiking = updateHikingViewModel.getHikingUpdate().getValue();
            Date dateCheck = formatter.parse(datetimeText);
            updateHiking.name = this.name.getText().toString();
            updateHiking.location = this.location.getText().toString();
            updateHiking.date = datetimeText;
            updateHiking.haveParking = this.haveParking.isChecked() ? "Yes" : "No";
            updateHiking.length = Double.parseDouble(this.length.getText().toString());
            updateHiking.level = this.levelSpinner.getText().toString();
            updateHiking.unit = (this.unitSpinner.getText().toString().equals("")) ? "m" : this.unitSpinner.getText().toString();
            updateHiking.description = this.description.getText().toString();
            updateHiking.waypoints = updateHikingViewModel.getWaypointValue();
            if(!Objects.equals(updateHiking.status, "Close")){
                if(dateCheck.compareTo(new Date()) > 0){
                    updateHiking.status = "Waiting";
                } else if (dateCheck.compareTo(new Date()) <= 0) {
                    updateHiking.status = "Expired";
                }
            }
            historyViewModel.updateHistory(updateHiking);
            Toast.makeText(this.getActivity(),"Change saved!", Toast.LENGTH_SHORT).show();
            return true;

        }
    }
    void openDialog(){
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this.getActivity(),
                (view, yearPicked, monthOfYear, dayOfMonth) -> {
                    date.setText(String.valueOf(dayOfMonth));
                    month.setText(String.valueOf(monthOfYear + 1));
                    year.setText(String.valueOf(yearPicked));
                }, currentYear, currentMonth, currentDay);
        datePickerDialog.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: destroyed");
        binding = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: stop");
        updateHikingViewModel.setDay((date.getText() != null) ? date.getText().toString() : "");
        updateHikingViewModel.setMonth((month.getText() != null) ? month.getText().toString() : "");
        updateHikingViewModel.setYear((year.getText() != null) ? year.getText().toString() : "");
        updateHikingViewModel.setHour((hour.getText() != null) ? hour.getText().toString() : "");
        updateHikingViewModel.setMinute((minute.getText() != null) ? minute.getText().toString() : "");
        updateHikingViewModel.setName((name.getText() != null) ? name.getText().toString() : "");
        updateHikingViewModel.setLocation((location.getText() != null) ? location.getText().toString() : "");
        updateHikingViewModel.setDescription((description.getText() != null) ? description.getText().toString() : "");
        updateHikingViewModel.setLength((length.getText() != null) ? length.getText().toString() : "");
        updateHikingViewModel.setLengthUnit((this.unitSpinner.getText().toString().equals("")) ? "m" : this.unitSpinner.getText().toString());
        updateHikingViewModel.setLevel(this.levelSpinner.getText().toString());
        updateHikingViewModel.setParking(this.haveParking.isChecked());
    }
}
