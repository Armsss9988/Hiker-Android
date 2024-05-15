package com.example.hiker.ui.history.addnew;
import static android.content.ContentValues.TAG;
import static androidx.navigation.Navigation.findNavController;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.hiker.MainActivity;
import com.example.hiker.R;
import com.example.hiker.database.entity.HikingHistory;
import com.example.hiker.databinding.FragmentAddNewBinding;
import com.example.hiker.functions.MapAndLocation;
import com.example.hiker.functions.MyDragShadowBuilder;
import com.example.hiker.ui.history.HistoryViewModel;
import com.example.hiker.ui.map.MapViewModel;
import com.google.android.material.textfield.TextInputEditText;
import org.osmdroid.util.GeoPoint;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddHikingFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentAddNewBinding binding;
    HistoryViewModel historyViewModel;
    AddHikingViewModel addHikingViewModel;
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
        binding = FragmentAddNewBinding.inflate(inflater,container,false);
        historyViewModel =
                new ViewModelProvider(this.getActivity()).get(HistoryViewModel.class);
        addHikingViewModel = new ViewModelProvider(this.getActivity()).get(AddHikingViewModel.class);
        mapViewModel = new ViewModelProvider(this.getActivity()).get(MapViewModel.class);
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        View root = binding.getRoot();
        setInputElement();
        setSpinnerElement();
        setButton(root);
        onDataCreate();
        binding.chooseWithMap.setOnClickListener(view -> {
            mapViewModel.setWaypointsRoute(new ArrayList<>(addHikingViewModel.getWaypointValue()));
            Bundle bundle = new Bundle();
            bundle.putString("key","New");
            findNavController(binding.getRoot()).navigate(R.id.action_nav_add_new_to_OSMMapFragment,bundle);
        });
        onLiveDataChange();
        binding.buttonPlus.setOnClickListener(view -> {
            DialogFragment dialog = new AddWaypointDialog();
            dialog.show(getParentFragmentManager(),"New");
        });
        return root;
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
        addHikingViewModel.getWaypoints().observe(getViewLifecycleOwner(),waypoints->{
            linearLayout.removeAllViews();
            List<String> listWaypointLocation = MapAndLocation.getInstance(requireActivity()).waypointsToListLocation(waypoints);
            for (String location : listWaypointLocation){
                TextView textView = new TextView(requireContext());
                textView.setText(location);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.location_and_route_bg,null));
                textView.setPadding(5,5,5,5);
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
                                        Collections.swap(waypoints, indexDragged, indexChange);
                                        addHikingViewModel.setWaypoints(waypoints);
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
                    ImageView img;
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
                            addHikingViewModel.removeWaypoint(indexDragged);
                            binding.remove.setBackgroundColor(Color.WHITE);
                            return true;
                        default:
                            Log.e("DragDrop Example","Unknown action type received by View.OnDragListener.");
                            break;
                    }
                    return false;
                });
        addHikingViewModel.getLocation().observe(getViewLifecycleOwner(),locationName -> location.setText(locationName));
        addHikingViewModel.getLength().observe(getViewLifecycleOwner(),lengthValue -> length.setText(lengthValue));
        addHikingViewModel.getLengthUnit().observe(getViewLifecycleOwner(),lengthUnit -> unitSpinner.setText(lengthUnit));
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
    void setButton(View rootView){
        Button chooseDateByCalendar = binding.calendar;
        chooseDateByCalendar.setOnClickListener(view -> openDialog());
        submitButton = binding.submitBtn;
        submitButton.setText("Add");
        submitButton.setOnClickListener(view -> {
            try {
                if(checkInput()){
                    findNavController(rootView).navigate(R.id.action_nav_add_new_to_nav_home);
                }
            } catch (ParseException e) {
                Toast.makeText(this.getActivity(),"Invalid date input",Toast.LENGTH_SHORT).show();
            }
        });
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
            Date dateCheck = formatter.parse(datetimeText);
            HikingHistory history = new HikingHistory();
            history.name = this.name.getText().toString();
            history.location = this.location.getText().toString();
            history.date = datetimeText;
            history.haveParking = this.haveParking.isChecked() ? "Yes" : "No";
            history.length = Double.parseDouble(this.length.getText().toString());
            history.level = this.levelSpinner.getText().toString();
            history.unit = (this.unitSpinner.getText().toString().equals("")) ? "m" : this.unitSpinner.getText().toString();
            history.description = this.description.getText().toString();
            history.waypoints = addHikingViewModel.getWaypointValue();
            if(dateCheck.compareTo(new Date()) > 0){
                history.status = "Waiting";
            } else if (dateCheck.compareTo(new Date()) <= 0) {
                history.status = "Expired";
            }
            historyViewModel.insertNew(history);
            addHikingViewModel.clearAll();
            Toast.makeText(this.getActivity(),"New hiking have created!", Toast.LENGTH_SHORT).show();
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
            addHikingViewModel.setDay((date.getText() != null) ? date.getText().toString() : "");
            addHikingViewModel.setMonth((month.getText() != null) ? month.getText().toString() : "");
            addHikingViewModel.setYear((year.getText() != null) ? year.getText().toString() : "");
            addHikingViewModel.setHour((hour.getText() != null) ? hour.getText().toString() : "");
            addHikingViewModel.setMinute((minute.getText() != null) ? minute.getText().toString() : "");
            addHikingViewModel.setName((name.getText() != null) ? name.getText().toString() : "");
            addHikingViewModel.setLocation((location.getText() != null) ? location.getText().toString() : "");
            addHikingViewModel.setDescription((description.getText() != null) ? description.getText().toString() : "");
            addHikingViewModel.setLength((length.getText() != null) ? length.getText().toString() : "");
            addHikingViewModel.setLengthUnit((this.unitSpinner.getText().toString().equals("")) ? "m" : this.unitSpinner.getText().toString());
            addHikingViewModel.setLevel(this.levelSpinner.getText().toString());
            addHikingViewModel.setParking(this.haveParking.isChecked());
    }
    void onDataCreate(){
            date.setText(addHikingViewModel.getDay().getValue());
            month.setText(addHikingViewModel.getMonth().getValue());
            year.setText(addHikingViewModel.getYear().getValue());
            hour.setText(addHikingViewModel.getHour().getValue());
            minute.setText(addHikingViewModel.getMinute().getValue());
            location.setText(addHikingViewModel.getLocation().getValue());
            length.setText(addHikingViewModel.getLength().getValue());
            name.setText(addHikingViewModel.getName().getValue());
            levelSpinner.setText(addHikingViewModel.getLevel().getValue());
            unitSpinner.setText(addHikingViewModel.getLengthUnit().getValue());
            description.setText(addHikingViewModel.getDescription().getValue());
            haveParking.setChecked(addHikingViewModel.getParkingValue());
    }
}
