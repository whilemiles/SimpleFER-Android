package com.example.simplefer_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                validateCredentials(username, password);

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validateCredentials(String username, String password) {
        if(username.isEmpty() || password.isEmpty()){
            runOnUiThread(() -> showErrorMessage("用户名或密码为空"));
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("Name", username);
            json.put("Password", password);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("login/json"), String.valueOf(json));
        Request request = new Request.Builder()
                .url("http://172.21.117.218:5050/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("validate", e.getMessage());
                runOnUiThread(() -> showErrorMessage("处理请求失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> showSuccessMessage("登陆成功"));
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userName", username);
                    startActivity(intent);
                    finish();
                } else {
                    if(response.code() == 401){
                        runOnUiThread(() -> showErrorMessage("用户名或密码错误"));
                    }
                    else if(response.code() == 500){
                        runOnUiThread(() -> showErrorMessage("未知错误"));
                    }
                }
            }
        });
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}