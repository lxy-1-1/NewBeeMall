package com.example.newbeemall;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import org.json.JSONObject;

/**
 * 商品详情页 + 生成订单
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    private ImageView ivCover;
    private TextView tvName, tvPrice, tvDesc;
    private Button btnAddCart, btnBuy;

    private long goodsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ivCover = findViewById(R.id.ivCover);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDesc = findViewById(R.id.tvDesc);
        btnAddCart = findViewById(R.id.btnAddCart);
        btnBuy = findViewById(R.id.btnBuy);

        // 获取商品 ID
        goodsId = getIntent().getLongExtra("goodsId", 0);

        loadGoodsDetail();

        btnAddCart.setOnClickListener(v -> addCart(false));
        btnBuy.setOnClickListener(v -> addCart(true));
    }

    private void loadGoodsDetail() {
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/goods/detail/" + goodsId, this);
            runOnUiThread(() -> {
                if (result != null) {
                    try {
                        if (!JsonUtil.isSuccess(result)) {
                            Toast.makeText(this, JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONObject data = JsonUtil.dataObject(result);
                        Goods goods = JsonUtil.parseGoods(data);

                        tvName.setText(goods.getGoodsName());
                        tvPrice.setText("¥" + goods.getSellingPrice());
                        tvDesc.setText(goods.getGoodsIntro());

                        // 加载商品图片
                        String img = goods.getGoodsCoverImg();
                        if (img != null && !img.isEmpty()) {
                            String imgUrl = img.startsWith("http") ? img : HttpUtil.BASE_URL + img;
                            Glide.with(this)
                                    .load(imgUrl)
                                    .placeholder(R.mipmap.ic_launcher)
                                    .error(R.mipmap.ic_launcher)
                                    .into(ivCover);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "解析数据失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }).start();
    }

    private void addCart(boolean goCart) {
        if (!HttpUtil.hasToken(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("goodsCount", 1);
                body.put("goodsId", goodsId);
                String result = HttpUtil.post("/api/v1/shop-cart", body.toString(), this);
                runOnUiThread(() -> {
                    if (result == null) {
                        Toast.makeText(this, "加入购物车失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!JsonUtil.isSuccess(result)) {
                        Toast.makeText(this, JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "已加入购物车", Toast.LENGTH_SHORT).show();
                    if (goCart) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("tabIndex", 2);
                        startActivity(intent);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加入购物车出错", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
