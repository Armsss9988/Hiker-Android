package com.example.hiker.ui.imageview;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.content.ContentValues.TAG;

import static androidx.navigation.Navigation.findNavController;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiker.R;
import com.example.hiker.databinding.ImageViewFragmentBinding;

import java.io.File;

public class ImageViewFragment extends Fragment implements ImageAdapter.ItemClickListener{
    RecyclerView recyclerView;
    ImageViewFragmentBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ImageViewFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.rvNumbers;
        int numberOfColumns = 6;
        recyclerView.setLayoutManager(new GridLayoutManager(this.requireActivity(), numberOfColumns));
        String imgDirectory = getArguments().getString("dir");
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle(imgDirectory);
        File fileImg = new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/HikerApp/" + imgDirectory + "/");
        recyclerView.setAdapter(new ImageAdapter(this.requireContext(), fileImg.listFiles()));

        return root;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0){
            boolean accepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
            if(accepted){
            }else{
                Toast.makeText(this.requireActivity(),"You have dined the permission", Toast.LENGTH_LONG).show();
            }
        }else{

        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String imgDirectory = getArguments().getString("dir");
        Bundle bundle = new Bundle();
        File fileImg = new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/HikerApp/" + imgDirectory + "/");
        String filePath = fileImg.listFiles()[position].getPath();
        bundle.putString("img",filePath);
        findNavController(this.getView()).navigate(R.id.action_nav_image_active_to_nav_image_open, bundle);
    }
}
