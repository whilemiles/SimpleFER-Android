package com.example.simplefer_android;

import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (validateInput(username, password, confirmPassword)) {
                    registerUser(username, password);
                }
            }
        });
    }

    private boolean validateInput(String username, String password, String confirmPassword) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorMessage("请填写所有字段");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showErrorMessage("两次输入的密码不一致");
            return false;
        }

        return true;
    }

    private void registerUser(String username, String password){
        JSONObject json = new JSONObject();
        try {
            json.put("Name", username);
            json.put("Password", password);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("register/json"), String.valueOf(json));
        Request request = new Request.Builder()
                .url("http://172.21.117.218:5050/register")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showErrorMessage("处理请求失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> showSuccessMessage("注册成功"));
                } else {
                    if(response.code() == 409){
                        runOnUiThread(() -> showErrorMessage("用户名已存在"));
                    }
                    else if(response.code() == 500){
                        runOnUiThread(() -> showErrorMessage("未知错误"));
                    }
                }
            }
        });

        finish();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}