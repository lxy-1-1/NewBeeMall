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
        btnRegister.setOnClickListener(v -> showRegisterInfo());
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
                            int resultCode = json.optInt("resultCode", -1);
                            if (resultCode != 200) {
                                // 服务器返回错误，显示具体原因
                                String msg = json.optString("message", "登录失败");
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String token = json.optString("data");
                            if (token != null && !token.isEmpty()) {
                                HttpUtil.saveToken(this, token);
                                HttpUtil.savePhone(this, phone);
                                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Token: " + token);
                                finish();
                            } else {
                                Toast.makeText(this, "登录失败：token为空", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            // 返回的不是 JSON，显示原始内容
                            Toast.makeText(this, "登录失败：" + result.substring(0, Math.min(result.length(), 80)), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
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
     * 注册功能 - 按实训文档要求暂不实现，提示联系管理员
     */
    private void register() {
        showRegisterInfo();
    }

    private void showRegisterInfo() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("注册说明")
                .setMessage("实训版账号通常由老师或组长统一分配。\n\n示例账号：16666666666\n示例密码：88888888")
                .setPositiveButton("知道了", null)
                .show();
    }
}
