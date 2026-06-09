package com.example.newbeemall;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
            loadOrders();
        });
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
        new AlertDialog.Builder(this)
                .setTitle("订单详情")
                .setMessage("订单号：" + item.getOrderNo() + "\n状态：" + OrderAdapter.statusText(item.getOrderStatus()) + "\n金额：¥" + item.getTotalPrice() + "\n时间：" + item.getCreateTime())
                .setPositiveButton("确定", null)
                .show();
    }
}
