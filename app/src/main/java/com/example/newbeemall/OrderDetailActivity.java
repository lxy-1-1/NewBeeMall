package com.example.newbeemall;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.adapter.OrderAdapter;
import com.example.newbeemall.adapter.OrderGoodsAdapter;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import java.util.List;

/**
 * 订单详情页 - 显示订单信息 + 商品列表
 */
public class OrderDetailActivity extends AppCompatActivity {

    private String orderNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        TextView tvStatus = findViewById(R.id.tvOrderStatus);
        TextView tvOrderNo = findViewById(R.id.tvOrderNo);
        TextView tvTime = findViewById(R.id.tvOrderTime);
        TextView tvTotal = findViewById(R.id.tvOrderTotal);
        ListView lvGoods = findViewById(R.id.lvOrderGoods);
        Button btnCancelOrder = findViewById(R.id.btnCancelOrder);

        // 从 Intent 获取订单数据
        orderNo = getIntent().getStringExtra("orderNo");
        int orderStatus = getIntent().getIntExtra("orderStatus", 0);
        String createTime = getIntent().getStringExtra("createTime");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0);
        @SuppressWarnings("unchecked")
        List<Goods> goodsList = (List<Goods>) getIntent().getSerializableExtra("goodsList");

        tvStatus.setText(OrderAdapter.statusText(orderStatus));
        tvOrderNo.setText("订单号：" + (orderNo != null ? orderNo : ""));
        tvTime.setText("订单时间：" + (createTime != null ? createTime : ""));
        tvTotal.setText(JsonUtil.formatPrice(totalPrice));

        if (goodsList != null && !goodsList.isEmpty()) {
            OrderGoodsAdapter adapter = new OrderGoodsAdapter(this, goodsList);
            lvGoods.setAdapter(adapter);
        }

        // 只有待付款/待确认状态才显示取消按钮
        if (orderStatus > 1) {
            btnCancelOrder.setVisibility(android.view.View.GONE);
        }

        btnCancelOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定取消此订单吗？")
                    .setPositiveButton("确定", (d, w) -> cancelOrder())
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void cancelOrder() {
        if (orderNo == null || orderNo.isEmpty()) return;
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/order/" + orderNo + "/cancel", this);
            runOnUiThread(() -> {
                if (result != null && JsonUtil.isSuccess(result)) {
                    Toast.makeText(this, "订单已取消", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "取消失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
