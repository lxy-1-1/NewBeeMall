package com.example.newbeemall;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;
import com.example.newbeemall.util.RegionHelper;

import org.json.JSONObject;

import java.util.List;

public class AddressEditActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etUserPhone;
    private Spinner spProvince;
    private Spinner spCity;
    private Spinner spDistrict;
    private EditText etDetail;
    private CheckBox cbDefault;
    private long addressId;

    private ArrayAdapter<String> provinceAdapter;
    private ArrayAdapter<String> cityAdapter;
    private ArrayAdapter<String> districtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_edit);

        etUserName = findViewById(R.id.etUserName);
        etUserPhone = findViewById(R.id.etUserPhone);
        spProvince = findViewById(R.id.spProvince);
        spCity = findViewById(R.id.spCity);
        spDistrict = findViewById(R.id.spDistrict);
        etDetail = findViewById(R.id.etDetailAddress);
        cbDefault = findViewById(R.id.cbDefault);
        Button btnSaveAddress = findViewById(R.id.btnSaveAddress);
        Button btnDeleteAddress = findViewById(R.id.btnDeleteAddress);

        addressId = getIntent().getLongExtra("addressId", 0);
        etUserName.setText(getIntent().getStringExtra("userName"));
        etUserPhone.setText(getIntent().getStringExtra("userPhone"));
        etDetail.setText(getIntent().getStringExtra("detailAddress"));
        cbDefault.setChecked(getIntent().getIntExtra("defaultFlag", 1) == 1);

        // 编辑模式时显示删除按钮
        if (addressId > 0) {
            btnDeleteAddress.setVisibility(View.VISIBLE);
        }

        // 初始化省市区联动
        setupRegionSpinners();

        // 如果有已保存的地址，设置选中项
        String savedProvince = getIntent().getStringExtra("provinceName");
        String savedCity = getIntent().getStringExtra("cityName");
        String savedRegion = getIntent().getStringExtra("regionName");
        if (savedProvince != null && !savedProvince.isEmpty()) {
            selectSpinnerItem(spProvince, provinceAdapter, savedProvince);
            // 延迟设置城市和区
            spProvince.post(() -> {
                if (savedCity != null && !savedCity.isEmpty()) {
                    selectSpinnerItem(spCity, cityAdapter, savedCity);
                    spCity.post(() -> {
                        if (savedRegion != null && !savedRegion.isEmpty()) {
                            selectSpinnerItem(spDistrict, districtAdapter, savedRegion);
                        }
                    });
                }
            });
        }

        btnSaveAddress.setOnClickListener(v -> saveAddress());
        btnDeleteAddress.setOnClickListener(v -> deleteAddress());
    }

    private void setupRegionSpinners() {
        List<String> provinces = RegionHelper.getProvinces();
        provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProvince.setAdapter(provinceAdapter);

        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCity.setAdapter(cityAdapter);

        districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistrict.setAdapter(districtAdapter);

        // 省变化 → 更新城市
        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String province = provinceAdapter.getItem(position);
                List<String> cities = RegionHelper.getCities(province);
                cityAdapter.clear();
                cityAdapter.addAll(cities);
                cityAdapter.notifyDataSetChanged();
                districtAdapter.clear();
                districtAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 城市变化 → 更新区
        spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String province = (String) spProvince.getSelectedItem();
                String city = cityAdapter.getItem(position);
                List<String> districts = RegionHelper.getDistricts(province, city);
                districtAdapter.clear();
                districtAdapter.addAll(districts);
                districtAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 初始化第一个省的城市
        if (!provinces.isEmpty()) {
            List<String> cities = RegionHelper.getCities(provinces.get(0));
            cityAdapter.addAll(cities);
            if (!cities.isEmpty()) {
                List<String> districts = RegionHelper.getDistricts(provinces.get(0), cities.get(0));
                districtAdapter.addAll(districts);
            }
        }
    }

    private void selectSpinnerItem(Spinner spinner, ArrayAdapter<String> adapter, String value) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void saveAddress() {
        String userName = etUserName.getText().toString().trim();
        String userPhone = etUserPhone.getText().toString().trim();
        String detail = etDetail.getText().toString().trim();
        if (userName.isEmpty() || userPhone.isEmpty() || detail.isEmpty()) {
            Toast.makeText(this, "请填写姓名、电话和详细地址", Toast.LENGTH_SHORT).show();
            return;
        }

        String province = spProvince.getSelectedItem() != null ? spProvince.getSelectedItem().toString() : "";
        String city = spCity.getSelectedItem() != null ? spCity.getSelectedItem().toString() : "";
        String district = spDistrict.getSelectedItem() != null ? spDistrict.getSelectedItem().toString() : "";

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                if (addressId > 0) body.put("addressId", addressId);
                body.put("userName", userName);
                body.put("userPhone", userPhone);
                body.put("provinceName", province);
                body.put("cityName", city);
                body.put("regionName", district);
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

    private void deleteAddress() {
        if (addressId <= 0) return;
        new Thread(() -> {
            String result = HttpUtil.delete("/api/v1/address/" + addressId, this);
            runOnUiThread(() -> {
                if (result != null && JsonUtil.isSuccess(result)) {
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
