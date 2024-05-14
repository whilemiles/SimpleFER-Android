package com.example.simplefer_android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowResultsActivity extends AppCompatActivity implements FetchImageUrlsTask.OnUrlsFetchedListener{

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;

    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        userName = getIntent().getStringExtra("userName");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FetchImageUrlsTask fetchImageUrlsTask = new FetchImageUrlsTask(this);
        fetchImageUrlsTask.execute(userName);

        //List<String> imageUrls = new ArrayList<>();
        // 添加图片URL到列表中
        //imageUrls.add("https://www.princeton.edu/sites/default/files/styles/full_2x_crop/public/images/2022/02/KOA_Nassau_2697x1517.jpg?itok=-AEkIq8B");
        //imageUrls.add("https://th-thumbnailer.cdn-si-edu.com/SdKYWifCKfE2g8O-po_SO99hQ-Y=/1000x750/filters:no_upscale():focal(3126x2084:3127x2085)/https://tf-cmsv2-smithsonianmag-media.s3.amazonaws.com/filer_public/ec/e6/ece69181-708a-496e-b2b7-eaf7078b99e0/gettyimages-1310156391.jpg");
        // 添加更多图片URL...

        //imageAdapter = new ImageAdapter(this, imageUrls);
        //recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public void onUrlsFetched(String urls) {
        if (urls != null) {
            List<String> imageUrls = new ArrayList<>(Arrays.asList(urls.split(" ")));
            imageAdapter = new ImageAdapter(this, imageUrls);
            recyclerView.setAdapter(imageAdapter);
            Log.d("URLs", urls);
        } else {
            Log.e("URLs", "Failed to fetch URLs");
        }
    }
}

class FetchImageUrlsTask extends AsyncTask<String, Void, String> {

    private OkHttpClient client;
    private OnUrlsFetchedListener listener;

    public FetchImageUrlsTask(OnUrlsFetchedListener listener) {
        this.client = new OkHttpClient();
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... str) {
        String userName = str[0];
        String url = "http://172.21.117.218:5050/image-urls?user_name=" + userName;
        Log.i("FetchImageUrlsTask", "doInBackground: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            listener.onUrlsFetched(result);
        }
    }

    public interface OnUrlsFetchedListener {
        void onUrlsFetched(String urls);
    }
}

