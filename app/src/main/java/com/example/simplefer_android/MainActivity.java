package com.example.simplefer_android;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.simplefer_android.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.opencv.video.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 0;

    private static final int REQUEST_VIDEO_PICK = 1;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    private com.example.simplefer_android.databinding.ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private boolean isPieMenuOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.pieMenu.setOnClickListener(
            view -> {
                if (isPieMenuOpen)
                {
                    closePieMenu();
                } else
                {
                    openPieMenu();
                }
            }
        );
        binding.cameraButton.setOnClickListener(
            view -> {
                try {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        Intent intent = new Intent(this, CameraPreviewActivity.class);
                        startActivity(intent);
                        Toast toast = Toast.makeText(MainActivity.this, "开始预览！通信中", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                catch (Error e){
                    e.printStackTrace();
                }
            }
        );

        binding.playButton.setOnClickListener(
            view -> {
                try {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_MEDIA_VIDEO},
                                REQUEST_CODE_STORAGE_PERMISSION);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("video/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_VIDEO_PICK);
                    }
                }
                catch (Error e){
                    Log.e("playButton", "play"+ e.getMessage());
                    e.printStackTrace();
                }
            }
        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                String fileName = getRealPathFromURI(videoUri);
                String actualPath = "/storage/emulated/0/Movies/" + fileName;
                File videoFile = new File(String.valueOf(actualPath));
                VideoUploadTask uploadTask = new VideoUploadTask();
                uploadTask.execute(videoFile);
            }
        }

    }

    public String getRealPathFromURI(Uri uri) {
        String filePath = "";
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columnIndex != -1) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        return filePath;
    }

    public class VideoUploadTask extends AsyncTask<File, Void, String>
    {
        private final String TAG = "VideoUploadTask";
        private static final String SERVER_URL = "http://172.21.117.218:5050/upload";

        @Override
        protected String doInBackground(File... files) {
            if (files.length == 0 || files[0] == null) {
                Log.e(TAG , "No file to upload");
                return "No file to upload";
            }
            File file = files[0];
            file = file.getAbsoluteFile();
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(MediaType.parse("video/mp4"), file);
                Request request = new Request.Builder()
                        .url(SERVER_URL)
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                if(response.isSuccessful()){
                    return "Success";
                }
                else{
                    return  "Fail";
                }
            } catch (IOException e) {
                Log.e(TAG , "Error during video upload: " + e.getMessage());
                return "Error during video upload: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (Objects.equals(response, "Success")) {
                Log.i(TAG , "upload succeeded");
                Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG , "upload failed");
                Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void openPieMenu()
    {
    // 展开动画
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.show_pie_menu);
        binding.pieMenuChildren.startAnimation(animation);
        binding.pieMenuChildren.setVisibility(View.VISIBLE);

        isPieMenuOpen = true;
    }

    private void closePieMenu() {
        // 收缩动画
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.hide_pie_menu);
        binding.pieMenuChildren.startAnimation(animation);
        binding.pieMenuChildren.setVisibility(View.GONE);

        isPieMenuOpen = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("playButton", "granted");
            } else {
                Log.e("playButton", "not granted");
            }
        }
    }
}
