package com.example.newbeemall;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.util.HttpUtil;

/**
 * 账号管理页面 - 昵称、个性签名、接口环境、退出登录
 */
public class AccountManageActivity extends AppCompatActivity {

    private TextView tvNickname;
    private EditText etSignature;
    private TextView tvApiEnv;

    private final String[] envNames = {"校内服务器", "校外服务器"};
    private final String[] envUrls = {HttpUtil.DEFAULT_INTERNAL_BASE_URL, HttpUtil.DEFAULT_EXTERNAL_BASE_URL};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        tvNickname = findViewById(R.id.tvNickname);
        etSignature = findViewById(R.id.etSignature);
        tvApiEnv = findViewById(R.id.tvApiEnv);

        findViewById(R.id.llApiEnv).setOnClickListener(v -> showApiEnvDialog());
        findViewById(R.id.llChangePassword).setOnClickListener(v ->
                Toast.makeText(this, "实训版可在此扩展修改密码", Toast.LENGTH_SHORT).show());

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveSettings());

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定退出登录吗？")
                    .setPositiveButton("确定", (d, w) -> {
                        HttpUtil.clearToken(this);
                        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        refreshUserInfo();
    }

    private void refreshUserInfo() {
        String phone = HttpUtil.getPhone(this);
        tvNickname.setText(phone.isEmpty() ? "未登录" : phone);
        etSignature.setText(HttpUtil.getSignature(this));
        tvApiEnv.setText(HttpUtil.getBaseUrlLabel(this));
    }

    private void saveSettings() {
        HttpUtil.saveSignature(this, etSignature.getText().toString());
        Toast.makeText(this, "已保存本地设置", Toast.LENGTH_SHORT).show();
        refreshUserInfo();
    }

    private void showApiEnvDialog() {
        int currentIndex = HttpUtil.DEFAULT_EXTERNAL_BASE_URL.equals(HttpUtil.getBaseUrl(this)) ? 1 : 0;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("选择接口环境")
                .setSingleChoiceItems(envNames, currentIndex, (dialog, which) -> {
                    HttpUtil.saveBaseUrl(this, envUrls[which]);
                    Toast.makeText(this, "已切换为" + envNames[which], Toast.LENGTH_SHORT).show();
                    refreshUserInfo();
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
