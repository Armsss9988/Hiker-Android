package com.example.hiker.ui.imageview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hiker.databinding.OpenImageViewBinding;
import com.github.chrisbanes.photoview.PhotoView;

public class OpenImageFragment extends Fragment {
    OpenImageViewBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = OpenImageViewBinding.inflate(inflater, container, false);
        String path = getArguments().getString("img");
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle(path.split("/")[path.split("/").length-1]);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        PhotoView photoView = binding.imageOpen;
        photoView.setImageBitmap(bitmap);
        return binding.getRoot();
    }
}
