package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newbeemall.AddressListActivity;
import com.example.newbeemall.LoginActivity;
import com.example.newbeemall.MainActivity;
import com.example.newbeemall.OrderListActivity;
import com.example.newbeemall.R;
import com.example.newbeemall.SearchActivity;
import com.example.newbeemall.adapter.GoodsGridAdapter;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页 - 轮播图、导航、新品、热门、推荐
 */
public class HomeFragment extends Fragment {
    private final List<Goods> newGoods = new ArrayList<>();
    private final List<Goods> hotGoods = new ArrayList<>();
    private final List<Goods> recommendGoods = new ArrayList<>();
    private GoodsGridAdapter newAdapter;
    private GoodsGridAdapter hotAdapter;
    private GoodsGridAdapter recommendAdapter;
    private ImageView ivBanner;
    private TextView tvBannerTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivBanner = view.findViewById(R.id.ivBanner);
        tvBannerTitle = view.findViewById(R.id.tvBannerTitle);
        TextView tvSearch = view.findViewById(R.id.tvSearch);
        GridView gvNav = view.findViewById(R.id.gvNav);
        GridView gvNewGoods = view.findViewById(R.id.gvNewGoods);
        GridView gvHotGoods = view.findViewById(R.id.gvHotGoods);
        GridView gvRecommendGoods = view.findViewById(R.id.gvRecommendGoods);

        String[] navItems = {"分类", "搜索", "新品", "热门", "推荐", "购物车", "订单", "地址", "登录", "我的"};
        gvNav.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, navItems));
        gvNav.setOnItemClickListener((parent, itemView, position, id) -> handleNavClick(position));

        newAdapter = new GoodsGridAdapter(requireContext(), newGoods);
        hotAdapter = new GoodsGridAdapter(requireContext(), hotGoods);
        recommendAdapter = new GoodsGridAdapter(requireContext(), recommendGoods);
        gvNewGoods.setAdapter(newAdapter);
        gvHotGoods.setAdapter(hotAdapter);
        gvRecommendGoods.setAdapter(recommendAdapter);

        tvSearch.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));
        loadHomeData();
    }

    private void handleNavClick(int position) {
        if (!(getActivity() instanceof MainActivity)) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        switch (position) {
            case 0:
                mainActivity.selectTab(1);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                startActivity(new Intent(requireContext(), SearchActivity.class));
                break;
            case 5:
                mainActivity.selectTab(2);
                break;
            case 6:
                startActivity(new Intent(requireContext(), OrderListActivity.class));
                break;
            case 7:
                startActivity(new Intent(requireContext(), AddressListActivity.class));
                break;
            case 8:
                startActivity(new Intent(requireContext(), LoginActivity.class));
                break;
            case 9:
                mainActivity.selectTab(3);
                break;
            default:
                break;
        }
    }

    private void loadHomeData() {
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/index-infos", requireContext());
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
            if (result == null) {
                Toast.makeText(requireContext(), "首页数据加载失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!JsonUtil.isSuccess(result)) {
                Toast.makeText(requireContext(), JsonUtil.message(result), Toast.LENGTH_SHORT).show();
                return;
            }
            parseHomeData(result);
        });
        }).start();
    }

    private void parseHomeData(String json) {
        try {
            JSONObject data = JsonUtil.dataObject(json);
            if (data == null) return;
            replaceGoods(newGoods, data.optJSONArray("newGoodses"), newAdapter);
            replaceGoods(hotGoods, data.optJSONArray("hotGoodses"), hotAdapter);
            replaceGoods(recommendGoods, data.optJSONArray("recommendGoodses"), recommendAdapter);
            loadBanner(data.optJSONArray("carousels"));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "首页数据解析失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void replaceGoods(List<Goods> target, JSONArray source, GoodsGridAdapter adapter) {
        target.clear();
        target.addAll(JsonUtil.parseGoodsArray(source));
        adapter.notifyDataSetChanged();
    }

    private void loadBanner(JSONArray carousels) {
        if (carousels == null || carousels.length() == 0) return;
        JSONObject first = carousels.optJSONObject(0);
        if (first == null) return;
        String img = first.optString("carouselUrl", first.optString("goodsCoverImg"));
        if ((img == null || img.isEmpty()) && !newGoods.isEmpty()) {
            img = newGoods.get(0).getGoodsCoverImg();
        }
        if (img == null || img.isEmpty()) return;
        String imgUrl = img.startsWith("http") ? img : HttpUtil.BASE_URL + img;
        Glide.with(this)
                .load(imgUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(ivBanner);
        tvBannerTitle.setBackgroundColor(0x00000000);
    }
}
