package com.example.simplefer_android;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;


import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private String TAG = "ImageAdapter";
    private List<String> imageUrls;
    private Context context;

    public ImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Log.i(TAG, "onBindViewHolder: " + imageUrl);
        Picasso.get()
                .load(imageUrl)
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess: " + "成功");

                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: "+ e.getMessage());
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            // 添加下载逻辑
            Toast.makeText(context, "Downloading " + imageUrl, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
