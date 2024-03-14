package com.example.simplefer_android;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;

public class CameraPreviewActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2
{
    private static final String TAG = "CameraPreviewActivity";

    private Face curFace;
    private JavaCameraView cameraView;

    private int frameCount = 0;
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                cameraView.enableView();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        cameraView = findViewById(R.id.cameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);
        cameraView.setCameraPermissionGranted();
        //cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not found!");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(cameraView!=null){
            cameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }
    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat originMat = inputFrame.rgba();
        Mat rotatedMat = new Mat();
        Core.rotate(originMat, rotatedMat, Core.ROTATE_90_CLOCKWISE);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(rotatedMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        if(curFace != null){
            Rect rect = new Rect(curFace.x, curFace.y, curFace.width, curFace.height);
            Core.rotate(originMat, originMat, Core.ROTATE_90_CLOCKWISE);
            Imgproc.rectangle(originMat, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
            Imgproc.putText(originMat, ExpressionToString(curFace.expression), rect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Core.rotate(originMat, originMat, Core.ROTATE_90_COUNTERCLOCKWISE);
        }

        if(frameCount % 10 == 0)
        {
            frameCount = 0;
            File imageDirectory = new File(getExternalFilesDir(null), "camera_images");
            String fileName = "test" + ".jpg";
            File imageFile = new File(imageDirectory, fileName);
            Imgcodecs.imwrite(imageFile.getAbsolutePath(), grayMat);
            ImageUploadTask uploadTask = new ImageUploadTask();
            uploadTask.execute(imageFile);
        }

        frameCount++;
        return originMat;
    }

    private String ExpressionToString(String ex)
    {
        switch (ex)
        {
            case "0":
                return "angry";
            case "1":
                return "disgust";
            case "2":
                return "fear";
            case "3":
                return "happy";
            case "4":
                return "neutral";
            case "5":
                return "sad";
            case "6":
                return "surprise";
            default:
                return null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraView.setCameraPermissionGranted();
                Log.e(TAG, "Permission");

            } else {
                Toast.makeText(this, "相机权限被拒绝，无法使用相机功能", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "No Permission");
            }
        }
    }
    public class ImageUploadTask extends AsyncTask<File, Void, String> {
        private static final String SERVER_URL = "http://172.21.117.218:5050/upload";

        @Override
        protected String doInBackground(File... files) {
            if (files.length == 0 || files[0] == null) {
                return "No file to upload";
            }
            File imageFile = files[0];
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
                Request request = new Request.Builder()
                        .url(SERVER_URL)
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                return "Error during image upload: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                curFace = parseJsonResponse(response);
                if(curFace != null && Objects.equals(curFace.expression, "7")){ // No face detected
                    curFace = null;
                }
            } else {
                Log.w(TAG, "OnPostExecute: null response");
            }
        }

        private Face parseJsonResponse(String jsonResponse) {
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                String exp = jsonObject.getString("expression");
                int x = jsonObject.getInt("x");
                int y = jsonObject.getInt("y");
                int width = jsonObject.getInt("width");
                int height = jsonObject.getInt("height");
                return new Face(exp, x,y, width, height);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}




