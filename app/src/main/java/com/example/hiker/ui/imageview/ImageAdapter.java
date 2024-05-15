package com.example.hiker.ui.imageview;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiker.R;

import java.io.File;

public class ImageAdapter  extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private File[] mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    ImageAdapter(Context context, File[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }


    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filePath = mData[position].getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        holder.imgView.setImageBitmap(bitmap);
        Bundle bundle = new Bundle();
        bundle.putString("img",filePath);
        holder.imgView.setOnClickListener(view -> {
            findNavController(holder.itemView).navigate(R.id.action_nav_image_active_to_nav_image_open, bundle);
        });
    }

    // total number of cells
    @Override
    public int getItemCount() {
        if(mData != null){
            return mData.length;
        }
        else{
            return 0;
        }
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgView;

        ViewHolder(View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.img_view);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
