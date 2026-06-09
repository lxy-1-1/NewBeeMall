package com.example.newbeemall;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import org.json.JSONObject;

public class AddressEditActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etUserPhone;
    private EditText etProvince;
    private EditText etCity;
    private EditText etRegion;
    private EditText etDetail;
    private CheckBox cbDefault;
    private long addressId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_edit);

        etUserName = findViewById(R.id.etUserName);
        etUserPhone = findViewById(R.id.etUserPhone);
        etProvince = findViewById(R.id.etProvince);
        etCity = findViewById(R.id.etCity);
        etRegion = findViewById(R.id.etRegion);
        etDetail = findViewById(R.id.etDetailAddress);
        cbDefault = findViewById(R.id.cbDefault);
        Button btnSaveAddress = findViewById(R.id.btnSaveAddress);

        addressId = getIntent().getLongExtra("addressId", 0);
        etUserName.setText(getIntent().getStringExtra("userName"));
        etUserPhone.setText(getIntent().getStringExtra("userPhone"));
        etProvince.setText(valueOrDefault(getIntent().getStringExtra("provinceName"), "河南省"));
        etCity.setText(valueOrDefault(getIntent().getStringExtra("cityName"), "信阳市"));
        etRegion.setText(valueOrDefault(getIntent().getStringExtra("regionName"), "浉河区"));
        etDetail.setText(getIntent().getStringExtra("detailAddress"));
        cbDefault.setChecked(getIntent().getIntExtra("defaultFlag", 1) == 1);

        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private void saveAddress() {
        String userName = etUserName.getText().toString().trim();
        String userPhone = etUserPhone.getText().toString().trim();
        String detail = etDetail.getText().toString().trim();
        if (userName.isEmpty() || userPhone.isEmpty() || detail.isEmpty()) {
            Toast.makeText(this, "请填写收货人、手机号和详细地址", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                if (addressId > 0) body.put("addressId", addressId);
                body.put("userName", userName);
                body.put("userPhone", userPhone);
                body.put("provinceName", etProvince.getText().toString().trim());
                body.put("cityName", etCity.getText().toString().trim());
                body.put("regionName", etRegion.getText().toString().trim());
                body.put("detailAddress", detail);
                body.put("defaultFlag", cbDefault.isChecked() ? 1 : 0);
                String result = addressId > 0
                        ? HttpUtil.put("/api/v1/address", body.toString(), this)
                        : HttpUtil.post("/api/v1/address", body.toString(), this);
                runOnUiThread(() -> {
                    if (result == null || !JsonUtil.isSuccess(result)) {
                        Toast.makeText(this, result == null ? "保存失败" : JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "保存出错", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
