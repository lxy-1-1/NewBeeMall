package com.example.newbeemall;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情页 - 轮播图 + 标题/运费/价格 + 商品介绍 + 固定底栏(加入购物车/立即购买)
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    private ViewPager2 vpGoodsImages;
    private LinearLayout llImageDots;
    private TextView tvName, tvPrice, tvDesc;
    private Button btnAddCart, btnBuy;
    private View ivBack;
    private FrameLayout flCartIcon;
    private TextView tvCartBadge;

    private long goodsId;
    private final List<String> imageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        vpGoodsImages = findViewById(R.id.vpGoodsImages);
        llImageDots = findViewById(R.id.llImageDots);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDesc = findViewById(R.id.tvDesc);
        btnAddCart = findViewById(R.id.btnAddCart);
        btnBuy = findViewById(R.id.btnBuy);
        ivBack = findViewById(R.id.ivBack);
        flCartIcon = findViewById(R.id.flCartIcon);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        // 获取商品 ID
        goodsId = getIntent().getLongExtra("goodsId", 0);

        // 返回按钮
        ivBack.setOnClickListener(v -> finish());

        // 购物车图标 - 跳转购物车页
        flCartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("tabIndex", 2);
            startActivity(intent);
        });

        loadGoodsDetail();

        btnAddCart.setOnClickListener(v -> addCart(false));
        btnBuy.setOnClickListener(v -> addCart(true));
    }

    private void loadGoodsDetail() {
        new Thread(() -> {
            HttpUtil.detectImageServer();
            String result = HttpUtil.get("/api/v1/goods/detail/" + goodsId, this);
            runOnUiThread(() -> {
                if (result != null) {
                    try {
                        if (!JsonUtil.isSuccess(result)) {
                            // token失效检测
                            if (JsonUtil.isTokenExpired(result)) {
                                HttpUtil.clearToken(this);
                                Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                return;
                            }
                            Toast.makeText(this, JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONObject data = JsonUtil.dataObject(result);
                        Goods goods = JsonUtil.parseGoods(data);

                        tvName.setText(goods.getGoodsName());
                        tvPrice.setText(JsonUtil.formatPrice(goods.getSellingPrice()));
                        tvDesc.setText(goods.getGoodsIntro());

                        // 解析轮播图图片列表
                        imageUrls.clear();
                        // goodsCarousel 可能是 JSON 数组字符串，也可能是逗号分隔字符串
                        String carouselStr = data != null ? data.optString("goodsCarousel", "") : "";
                        if (!carouselStr.isEmpty()) {
                            try {
                                JSONArray carouselArray = new JSONArray(carouselStr);
                                for (int i = 0; i < carouselArray.length(); i++) {
                                    String url = carouselArray.optString(i);
                                    if (!url.isEmpty()) {
                                        imageUrls.add(HttpUtil.buildImageUrl(url));
                                    }
                                }
                            } catch (Exception e) {
                                // 不是 JSON 数组，尝试逗号分隔
                                String[] parts = carouselStr.split(",");
                                for (String part : parts) {
                                    if (!part.trim().isEmpty()) {
                                        imageUrls.add(HttpUtil.buildImageUrl(part.trim()));
                                    }
                                }
                            }
                        }
                        // 如果没有轮播图，使用封面图
                        if (imageUrls.isEmpty() && !goods.getGoodsCoverImg().isEmpty()) {
                            imageUrls.add(HttpUtil.buildImageUrl(goods.getGoodsCoverImg()));
                        }
                        setupImageCarousel();

                    } catch (Exception e) {
                        Toast.makeText(this, "解析数据失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }).start();
    }

    private void setupImageCarousel() {
        if (imageUrls.isEmpty()) return;

        vpGoodsImages.setAdapter(new ImageAdapter(imageUrls));

        // 创建指示器圆点
        llImageDots.removeAllViews();
        for (int i = 0; i < imageUrls.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0
                    ? android.R.drawable.presence_online
                    : android.R.drawable.presence_invisible);
            llImageDots.addView(dot);
        }

        vpGoodsImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < llImageDots.getChildCount(); i++) {
                    llImageDots.getChildAt(i).setBackgroundResource(
                            i == position
                                    ? android.R.drawable.presence_online
                                    : android.R.drawable.presence_invisible);
                }
            }
        });
    }

    /**
     * 商品图片轮播适配器
     */
    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {
        private final List<String> urls;

        ImageAdapter(List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_banner, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String url = urls.get(position);
            HttpUtil.loadImage(holder.iv.getContext(), url, holder.iv);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView iv;

            VH(@NonNull View itemView) {
                super(itemView);
                iv = itemView.findViewById(R.id.ivBanner);
            }
        }
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
                    // 更新购物车徽章
                    tvCartBadge.setVisibility(View.VISIBLE);
                    int current = 0;
                    try {
                        current = Integer.parseInt(tvCartBadge.getText().toString());
                    } catch (Exception ignored) {}
                    tvCartBadge.setText(String.valueOf(current + 1));

                    if (goCart) {
                        // 立即购买：加入购物车后跳转结算页
                        try {
                            JSONObject cartResult = new JSONObject(result);
                            JSONObject data = cartResult.optJSONObject("data");
                            long cartItemId = data != null ? data.optLong("cartItemId", 0) : 0;
                            if (cartItemId == 0) {
                                // 如果返回中没有 cartItemId，跳转到购物车 Tab
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra("tabIndex", 2);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(this, CheckoutActivity.class);
                                intent.putExtra("cartItemIds", new long[]{cartItemId});
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            // 解析失败则跳转购物车
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("tabIndex", 2);
                            startActivity(intent);
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加入购物车出错", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
