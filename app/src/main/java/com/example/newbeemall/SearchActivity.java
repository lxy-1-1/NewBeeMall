package com.example.newbeemall;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.adapter.GoodsListAdapter;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品搜索页 - 支持关键字搜索、新品排序、价格排序（使用 TabLayout）
 */
public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private EditText etKeyword;
    private Button btnSearch;
    private TabLayout tlOrder;
    private ListView lvResult;
    private View llSearchEmpty;

    private List<Goods> goodsList = new ArrayList<>();
    private GoodsListAdapter adapter;

    private String currentKeyword = "";
    private long goodsCategoryId = 0;

    // 排序选项对应关系
    private final String[] orderValues = {"", "new", "price"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etKeyword = findViewById(R.id.etKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        tlOrder = findViewById(R.id.tlOrder);
        lvResult = findViewById(R.id.lvResult);
        llSearchEmpty = findViewById(R.id.llSearchEmpty);

        goodsCategoryId = getIntent().getLongExtra("goodsCategoryId", 0);
        String title = getIntent().getStringExtra("title");
        if (title != null && !title.isEmpty()) {
            currentKeyword = "";
            etKeyword.setHint(title);
            search("");
        }

        adapter = new GoodsListAdapter(this, goodsList);
        lvResult.setAdapter(adapter);

        // 添加 3 个 Tab：推荐、新品、价格
        tlOrder.addTab(tlOrder.newTab().setText("推荐"));
        tlOrder.addTab(tlOrder.newTab().setText("新品"));
        tlOrder.addTab(tlOrder.newTab().setText("价格"));

        // 默认选中第一个
        tlOrder.getTabAt(0).select();

        // Tab 切换监听
        tlOrder.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                search(orderValues[pos]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnSearch.setOnClickListener(v -> {
            currentKeyword = etKeyword.getText().toString().trim();
            if (currentKeyword.isEmpty()) {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                return;
            }
            // 搜索时保持当前选中的排序 Tab
            int pos = tlOrder.getSelectedTabPosition();
            search(orderValues[pos >= 0 ? pos : 0]);
        });
    }

    private void search(String orderBy) {
        if (currentKeyword.isEmpty() && goodsCategoryId == 0) {
            currentKeyword = etKeyword.getText().toString().trim();
            if (currentKeyword.isEmpty()) {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String path = buildSearchPath(orderBy);

        final String finalPath = path;
        new Thread(() -> {
            String result = HttpUtil.get(finalPath, this);
            runOnUiThread(() -> {
                if (result != null) {
                    if (!JsonUtil.isSuccess(result)) {
                        Toast.makeText(this, JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    parseResult(result);
                } else {
                    Toast.makeText(this, "搜索失败，请检查网络", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private String buildSearchPath(String orderBy) {
        try {
            String keyword = URLEncoder.encode(currentKeyword, "UTF-8");
            String path = "/api/v1/search?pageNumber=1&keyword=" + keyword + "&orderBy=" + orderBy;
            if (goodsCategoryId > 0) {
                path += "&goodsCategoryId=" + goodsCategoryId;
            }
            return path;
        } catch (Exception e) {
            return "/api/v1/search?pageNumber=1&keyword=&orderBy=" + orderBy;
        }
    }

    private void parseResult(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject data = root.getJSONObject("data");
            JSONArray list = data.getJSONArray("list");
            goodsList.clear();
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                Goods goods = new Goods();
                goods.setGoodsId(item.optLong("goodsId"));
                goods.setGoodsName(item.optString("goodsName"));
                goods.setSellingPrice(item.optDouble("sellingPrice"));
                goods.setGoodsCoverImg(item.optString("goodsCoverImg"));
                goods.setGoodsIntro(item.optString("goodsIntro", "新蜂精选"));
                goodsList.add(goods);
            }
            adapter.notifyDataSetChanged();
            // 隐藏空状态
            llSearchEmpty.setVisibility(goodsList.isEmpty() ? View.VISIBLE : View.GONE);
            Toast.makeText(this, goodsList.isEmpty() ? "没有找到相关商品" : "找到 " + goodsList.size() + " 条结果", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
            Toast.makeText(this, "搜索结果解析失败", Toast.LENGTH_SHORT).show();
        }
    }
}
