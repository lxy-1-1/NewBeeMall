package com.example.newbeemall;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.adapter.OrderAdapter;
import com.example.newbeemall.model.OrderItem;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends AppCompatActivity {
    private final List<OrderItem> orders = new ArrayList<>();
    private OrderAdapter adapter;
    private String currentStatus = "";
    private Button activeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        ListView lvOrders = findViewById(R.id.lvOrders);
        adapter = new OrderAdapter(this, orders);
        lvOrders.setAdapter(adapter);
        lvOrders.setOnItemClickListener((parent, view, position, id) -> showOrder(orders.get(position)));

        bindStatusButton(R.id.btnOrderAll, "");
        bindStatusButton(R.id.btnOrderWaitPay, "0");
        bindStatusButton(R.id.btnOrderWaitConfirm, "1");
        bindStatusButton(R.id.btnOrderWaitSend, "2");
        bindStatusButton(R.id.btnOrderSent, "3");
        bindStatusButton(R.id.btnOrderDone, "4");

        // 如果从"我的"页面传了status，则选中对应按钮
        int statusFromIntent = getIntent().getIntExtra("status", -1);
        if (statusFromIntent >= 0 && statusFromIntent <= 4) {
            currentStatus = String.valueOf(statusFromIntent);
            int[] statusBtnIds = {R.id.btnOrderWaitPay, R.id.btnOrderWaitConfirm,
                    R.id.btnOrderWaitSend, R.id.btnOrderSent, R.id.btnOrderDone};
            activeButton = findViewById(statusBtnIds[statusFromIntent]);
        } else {
            // 默认选中"全部"
            activeButton = findViewById(R.id.btnOrderAll);
        }
        highlightActiveButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void bindStatusButton(int id, String status) {
        Button button = findViewById(id);
        button.setOnClickListener(v -> {
            currentStatus = status;
            activeButton = button;
            highlightActiveButton();
            loadOrders();
        });
    }

    private void highlightActiveButton() {
        int[] buttonIds = {R.id.btnOrderAll, R.id.btnOrderWaitPay, R.id.btnOrderWaitConfirm,
                R.id.btnOrderWaitSend, R.id.btnOrderSent, R.id.btnOrderDone};
        for (int id : buttonIds) {
            Button btn = findViewById(id);
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_gray)));
            btn.setTextColor(getResources().getColor(R.color.black));
        }
        if (activeButton != null) {
            activeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            activeButton.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void loadOrders() {
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/order?pageNumber=1&status=" + currentStatus, this);
            runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(this, "订单加载失败，请确认已登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    // token失效自动跳转登录
                    if (JsonUtil.isTokenExpired(result)) {
                        HttpUtil.clearToken(this);
                        Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }
                    if (!JsonUtil.isSuccess(result)) {
                        Toast.makeText(this, JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    orders.clear();
                    orders.addAll(JsonUtil.parseOrders(result));
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(this, "订单解析失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showOrder(OrderItem item) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("orderNo", item.getOrderNo());
        intent.putExtra("orderStatus", item.getOrderStatus());
        intent.putExtra("createTime", item.getCreateTime());
        intent.putExtra("totalPrice", item.getTotalPrice());
        intent.putExtra("goodsList", new ArrayList<>(item.getGoodsList()));
        startActivity(intent);
    }
}
