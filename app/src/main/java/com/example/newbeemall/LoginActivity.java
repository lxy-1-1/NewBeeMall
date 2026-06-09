package com.example.newbeemall;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import org.json.JSONObject;

/**
 * 登录/注册页面
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etPhone, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    /**
     * 登录逻辑
     */
    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入手机号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() != 11) {
            Toast.makeText(this, "手机号必须是11位", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // 密码需要 MD5 加密（32位小写）
                String md5Password = MD5Util.md5(password);

                JSONObject body = new JSONObject();
                body.put("loginName", phone);
                body.put("passwordMd5", md5Password);

                String result = HttpUtil.post("/api/v1/user/login", body.toString(), this);

                runOnUiThread(() -> {
                    if (result != null) {
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.optInt("resultCode") != 200) {
                                Toast.makeText(this, json.optString("message", "登录失败"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String token = json.optString("data");
                            if (token != null && !token.isEmpty()) {
                                // 保存 token
                                HttpUtil.saveToken(this, token);
                                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Token: " + token);
                                finish();
                            } else {
                                Toast.makeText(this, "登录失败：token为空", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "登录失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "登录失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Login error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "登录出错：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    /**
     * 注册逻辑
     */
    private void register() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入手机号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() != 11) {
            Toast.makeText(this, "手机号必须是11位", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("loginName", phone);
                body.put("password", password);

                String result = HttpUtil.post("/api/v1/user/register", body.toString(), this);

                runOnUiThread(() -> {
                    if (result != null && JsonUtil.isSuccess(result)) {
                        Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, result == null ? "注册失败，请检查网络" : JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Register error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "注册出错：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
