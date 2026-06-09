package com.example.newbeemall;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newbeemall.adapter.GoodsGridAdapter;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品搜索页 - 支持关键字搜索、新品排序、价格排序
 */
public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private EditText etKeyword;
    private Button btnSearch, btnOrderDefault, btnOrderNew, btnOrderPrice;
    private GridView gvResult;

    private List<Goods> goodsList = new ArrayList<>();
    private GoodsGridAdapter adapter;

    private String currentKeyword = "";
    private long goodsCategoryId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etKeyword = findViewById(R.id.etKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        btnOrderDefault = findViewById(R.id.btnOrderDefault);
        btnOrderNew = findViewById(R.id.btnOrderNew);
        btnOrderPrice = findViewById(R.id.btnOrderPrice);
        gvResult = findViewById(R.id.gvResult);

        goodsCategoryId = getIntent().getLongExtra("goodsCategoryId", 0);
        String title = getIntent().getStringExtra("title");
        if (title != null && !title.isEmpty()) {
            currentKeyword = "";
            etKeyword.setHint(title);
            search("");
        }

        adapter = new GoodsGridAdapter(this, goodsList);
        gvResult.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            currentKeyword = etKeyword.getText().toString().trim();
            if (currentKeyword.isEmpty()) {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                return;
            }
            search("");  // 默认按推荐排序
        });

        btnOrderDefault.setOnClickListener(v -> search(""));
        btnOrderNew.setOnClickListener(v -> search("new"));
        btnOrderPrice.setOnClickListener(v -> search("price"));
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
                goodsList.add(goods);
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "找到 " + goodsList.size() + " 条结果", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
        }
    }
}
