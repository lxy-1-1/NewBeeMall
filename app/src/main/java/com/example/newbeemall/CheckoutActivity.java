package com.example.newbeemall;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.adapter.OrderGoodsAdapter;
import com.example.newbeemall.model.Address;
import com.example.newbeemall.model.CartItem;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;
import com.example.newbeemall.widget.ExpandedListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private long[] cartItemIds;
    private Address defaultAddress;
    private TextView tvCheckoutAddress;
    private TextView tvCheckoutTotal;
    private ExpandedListView lvCheckoutGoods;
    private final List<Goods> checkoutGoods = new ArrayList<>();
    private OrderGoodsAdapter goodsAdapter;
    private AlertDialog paymentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartItemIds = getIntent().getLongArrayExtra("cartItemIds");
        if (cartItemIds == null) {
            cartItemIds = new long[0];
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        View llAddressCard = findViewById(R.id.llAddressCard);
        tvCheckoutAddress = findViewById(R.id.tvCheckoutAddress);
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        lvCheckoutGoods = findViewById(R.id.lvCheckoutGoods);
        Button btnSubmitOrder = findViewById(R.id.btnSubmitOrder);

        ivBack.setOnClickListener(v -> finish());
        llAddressCard.setOnClickListener(v ->
                startActivity(new Intent(this, AddressListActivity.class)));
        btnSubmitOrder.setOnClickListener(v -> submitOrder());

        goodsAdapter = new OrderGoodsAdapter(this, checkoutGoods);
        lvCheckoutGoods.setAdapter(goodsAdapter);
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
                if (JsonUtil.isTokenExpired(result)) {
                    HttpUtil.clearToken(this);
                    Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return;
                }
                if (!JsonUtil.isSuccess(result)) {
                    defaultAddress = null;
                    tvCheckoutAddress.setText(JsonUtil.message(result));
                    return;
                }
                try {
                    defaultAddress = JsonUtil.parseAddress(JsonUtil.dataObject(result));
                    if (defaultAddress.getAddressId() > 0) {
                        tvCheckoutAddress.setText(defaultAddress.getUserName() + "  " + defaultAddress.getUserPhone()
                                + "\n" + defaultAddress.getFullAddress());
                    } else {
                        tvCheckoutAddress.setText("暂无默认地址，请先添加或选择地址");
                    }
                } catch (Exception e) {
                    tvCheckoutAddress.setText("默认地址解析失败，请进入地址管理检查");
                }
            });
        }).start();
    }

    private void loadSettle() {
        if (cartItemIds.length == 0) {
            checkoutGoods.clear();
            goodsAdapter.notifyDataSetChanged();
            tvCheckoutTotal.setText("¥0");
            return;
        }
        new Thread(() -> {
            HttpUtil.detectImageServer();
            String result = HttpUtil.get("/api/v1/shop-cart/settle?cartItemIds=" + idCsv(), this);
            runOnUiThread(() -> {
                if (result == null) {
                    checkoutGoods.clear();
                    goodsAdapter.notifyDataSetChanged();
                    tvCheckoutTotal.setText("¥0");
                    Toast.makeText(this, "待结算商品加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (JsonUtil.isTokenExpired(result)) {
                    HttpUtil.clearToken(this);
                    Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return;
                }
                if (!JsonUtil.isSuccess(result)) {
                    checkoutGoods.clear();
                    goodsAdapter.notifyDataSetChanged();
                    tvCheckoutTotal.setText("¥0");
                    Toast.makeText(this, JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    List<CartItem> items = JsonUtil.parseCartItems(result);
                    checkoutGoods.clear();
                    double total = 0;
                    for (CartItem item : items) {
                        Goods goods = new Goods();
                        goods.setGoodsId(item.getGoodsId());
                        goods.setGoodsName(item.getGoodsName());
                        goods.setSellingPrice(item.getSellingPrice());
                        goods.setGoodsCoverImg(item.getGoodsCoverImg());
                        goods.setGoodsCount(item.getGoodsCount());
                        checkoutGoods.add(goods);
                        total += item.getTotalPrice();
                    }
                    JSONObject data = JsonUtil.dataObject(result);
                    if (data != null) {
                        total = data.optDouble("priceTotal", data.optDouble("totalPrice", total));
                    }
                    goodsAdapter.notifyDataSetChanged();
                    tvCheckoutTotal.setText(formatMoney(total));
                } catch (Exception e) {
                    checkoutGoods.clear();
                    goodsAdapter.notifyDataSetChanged();
                    tvCheckoutTotal.setText("¥0");
                    Toast.makeText(this, "待结算商品解析失败", Toast.LENGTH_SHORT).show();
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
                for (long id : cartItemIds) {
                    ids.put(id);
                }
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
        String orderNo = "";
        try {
            JSONObject data = JsonUtil.dataObject(result);
            if (data != null) {
                orderNo = data.optString("orderNo", "");
            }
            if (orderNo.isEmpty()) {
                orderNo = new JSONObject(result).optString("data", "");
            }
        } catch (Exception ignored) {
        }
        if (orderNo.isEmpty()) {
            Toast.makeText(this, "订单号获取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        showPayDialog(orderNo);
    }

    private void showPayDialog(String orderNo) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null, false);
        ImageView ivClose = view.findViewById(R.id.ivClosePay);
        Button btnAliPay = view.findViewById(R.id.btnAliPay);
        Button btnWechatPay = view.findViewById(R.id.btnWechatPay);

        paymentDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        ivClose.setOnClickListener(v -> paymentDialog.dismiss());
        btnAliPay.setOnClickListener(v -> {
            paymentDialog.dismiss();
            payOrder(orderNo, 1);
        });
        btnWechatPay.setOnClickListener(v -> {
            paymentDialog.dismiss();
            payOrder(orderNo, 2);
        });

        paymentDialog.show();
        Window window = paymentDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void payOrder(String orderNo, int payType) {
        new Thread(() -> {
            String result = null;
            if (orderNo != null && !orderNo.isEmpty()) {
                try {
                    result = HttpUtil.get("/api/v1/order/paySuccess?orderNo=" + orderNo + "&payType=" + payType, this);
                } catch (Exception ignored) {
                    result = null;
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, OrderListActivity.class));
                finish();
            });
        }).start();
    }

    private String idCsv() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cartItemIds.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(cartItemIds[i]);
        }
        return builder.toString();
    }

    private String formatMoney(double value) {
        return JsonUtil.formatPrice(value);
    }
}
