package com.example.newbeemall;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.model.Address;
import com.example.newbeemall.model.CartItem;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private long[] cartItemIds;
    private Address defaultAddress;
    private TextView tvCheckoutAddress;
    private TextView tvCheckoutGoods;
    private TextView tvCheckoutTotal;
    private RadioGroup rgPayType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartItemIds = getIntent().getLongArrayExtra("cartItemIds");
        tvCheckoutAddress = findViewById(R.id.tvCheckoutAddress);
        tvCheckoutGoods = findViewById(R.id.tvCheckoutGoods);
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        rgPayType = findViewById(R.id.rgPayType);
        Button btnManageAddress = findViewById(R.id.btnManageAddress);
        Button btnSubmitOrder = findViewById(R.id.btnSubmitOrder);

        btnManageAddress.setOnClickListener(v -> startActivity(new Intent(this, AddressListActivity.class)));
        btnSubmitOrder.setOnClickListener(v -> submitOrder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddress();
        loadSettle();
    }

    private void loadAddress() {
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/address/default", this);
            runOnUiThread(() -> {
                if (result == null) {
                    defaultAddress = null;
                    tvCheckoutAddress.setText("暂无默认地址，请先添加或选择地址");
                    return;
                }
                try {
                    defaultAddress = JsonUtil.parseAddress(JsonUtil.dataObject(result));
                    tvCheckoutAddress.setText(defaultAddress.getUserName() + "  " + defaultAddress.getUserPhone() + "\n" + defaultAddress.getFullAddress());
                } catch (Exception e) {
                    tvCheckoutAddress.setText("默认地址解析失败，请进入地址管理检查");
                }
            });
        }).start();
    }

    private void loadSettle() {
        if (cartItemIds == null || cartItemIds.length == 0) return;
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/shop-cart/settle?cartItemIds=" + idCsv(), this);
            runOnUiThread(() -> {
                if (result == null) {
                    tvCheckoutGoods.setText("待结算商品加载失败");
                    return;
                }
                try {
                    List<CartItem> items = JsonUtil.parseCartItems(result);
                    double total = 0;
                    StringBuilder goodsText = new StringBuilder();
                    for (CartItem item : items) {
                        goodsText.append(item.getGoodsName()).append(" x").append(item.getGoodsCount()).append("\n");
                        total += item.getTotalPrice();
                    }
                    JSONObject data = JsonUtil.dataObject(result);
                    if (data != null) {
                        total = data.optDouble("priceTotal", data.optDouble("totalPrice", total));
                    }
                    tvCheckoutGoods.setText(goodsText.toString());
                    tvCheckoutTotal.setText("合计：¥" + total);
                } catch (Exception e) {
                    tvCheckoutGoods.setText("待结算商品解析失败");
                }
            });
        }).start();
    }

    private void submitOrder() {
        if (defaultAddress == null || defaultAddress.getAddressId() == 0) {
            startActivity(new Intent(this, AddressListActivity.class));
            return;
        }
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("addressId", defaultAddress.getAddressId());
                JSONArray ids = new JSONArray();
                for (long id : cartItemIds) ids.put(id);
                body.put("cartItemIds", ids);
                String result = HttpUtil.post("/api/v1/saveOrder", body.toString(), this);
                runOnUiThread(() -> handleOrderResult(result));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "生成订单出错", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleOrderResult(String result) {
        if (result == null) {
            Toast.makeText(this, "生成订单失败", Toast.LENGTH_SHORT).show();
            return;
        }
        String orderNo = "";
        try {
            JSONObject data = JsonUtil.dataObject(result);
            if (data != null) orderNo = data.optString("orderNo");
            if (orderNo.isEmpty()) orderNo = new JSONObject(result).optString("data");
        } catch (Exception ignored) {
        }
        int payType = rgPayType.getCheckedRadioButtonId() == R.id.rbAliPay ? 1 : 2;
        payOrder(orderNo, payType);
    }

    private void payOrder(String orderNo, int payType) {
        new Thread(() -> {
            if (orderNo != null && !orderNo.isEmpty()) {
                HttpUtil.get("/api/v1/order/paySuccess?orderNo=" + orderNo + "&payType=" + payType, this);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "订单已生成", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, OrderListActivity.class));
                finish();
            });
        }).start();
    }

    private String idCsv() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cartItemIds.length; i++) {
            if (i > 0) builder.append(",");
            builder.append(cartItemIds[i]);
        }
        return builder.toString();
    }
}
