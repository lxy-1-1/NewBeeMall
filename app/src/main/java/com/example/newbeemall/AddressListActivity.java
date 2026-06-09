package com.example.newbeemall;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.adapter.AddressAdapter;
import com.example.newbeemall.model.Address;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class AddressListActivity extends AppCompatActivity {
    private final List<Address> addresses = new ArrayList<>();
    private AddressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        ListView lvAddress = findViewById(R.id.lvAddress);
        Button btnAddAddress = findViewById(R.id.btnAddAddress);
        adapter = new AddressAdapter(this, addresses);
        lvAddress.setAdapter(adapter);
        btnAddAddress.setOnClickListener(v -> startActivity(new Intent(this, AddressEditActivity.class)));
        lvAddress.setOnItemClickListener((parent, view, position, id) -> {
            Address address = addresses.get(position);
            Intent intent = new Intent(this, AddressEditActivity.class);
            intent.putExtra("addressId", address.getAddressId());
            intent.putExtra("userName", address.getUserName());
            intent.putExtra("userPhone", address.getUserPhone());
            intent.putExtra("provinceName", address.getProvinceName());
            intent.putExtra("cityName", address.getCityName());
            intent.putExtra("regionName", address.getRegionName());
            intent.putExtra("detailAddress", address.getDetailAddress());
            intent.putExtra("defaultFlag", address.getDefaultFlag());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!HttpUtil.hasToken(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        loadAddresses();
    }

    private void loadAddresses() {
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/address", this);
            runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(this, "地址加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    addresses.clear();
                    addresses.addAll(JsonUtil.parseAddresses(result));
                    adapter.notifyDataSetChanged();
                    if (addresses.isEmpty()) {
                        Toast.makeText(this, "请先添加收货地址", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "地址解析失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
