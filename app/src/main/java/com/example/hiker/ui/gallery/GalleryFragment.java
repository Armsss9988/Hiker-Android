package com.example.hiker.ui.gallery;

import static android.content.ContentValues.TAG;

import static androidx.navigation.Navigation.findNavController;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hiker.R;
import com.example.hiker.databinding.FragmentGalleryBinding;

import java.io.File;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        if (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED ) {
            createFolder();
        }
        else{
            requestPermissionsIfNecessary(new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    }
            );
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    void createFolder() {
        File fileImg = new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/HikerApp/");
        if(fileImg.listFiles() != null){
            if(fileImg.listFiles().length > 0){
                for(File file : fileImg.listFiles()){
                    LinearLayout linearLayout = new LinearLayout(this.requireContext());
                    linearLayout.setLayoutParams(new ViewGroup.LayoutParams(500, 350));
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setGravity(Gravity.CENTER);
                    ImageView imageView = new ImageView(this.requireContext());
                    imageView.setBackground(ResourcesCompat.getDrawable(this.getResources(),R.drawable.baseline_folder_24,null));
                    imageView.setForegroundGravity(Gravity.CENTER);
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    TextView textView = new TextView(this.requireContext());
                    textView.setText(file.getName());
                    textView.setTextSize(30);
                    Log.i(TAG, "onCreateView: " + file.getName());
                    textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    textView.setGravity(Gravity.CENTER);
                    linearLayout.addView(imageView);
                    linearLayout.addView(textView);
                    linearLayout.setOnClickListener(view -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("dir",file.getName());
                        findNavController(this.getView()).navigate(R.id.action_nav_gallery_to_nav_image_active, bundle);
                    });
                    binding.dirGallery.addView(linearLayout);
            }
        }
            else {
                Toast.makeText(requireContext(),"Nothing to show here!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this.requireActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            for (String permission : permissionsToRequest) {
                requestPermissionLauncher.launch(permission);
            }
        }
    }
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        createFolder();
                    } else {
                        Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                    }
                }
            }
    );
}