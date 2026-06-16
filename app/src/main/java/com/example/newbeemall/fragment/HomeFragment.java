package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newbeemall.AddressListActivity;
import com.example.newbeemall.DetailActivity;
import com.example.newbeemall.LoginActivity;
import com.example.newbeemall.MainActivity;
import com.example.newbeemall.OrderListActivity;
import com.example.newbeemall.R;
import com.example.newbeemall.SearchActivity;
import com.example.newbeemall.adapter.GoodsGridAdapter;
import com.example.newbeemall.adapter.NavAdapter;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;
import com.example.newbeemall.widget.BannerContainer;

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

    // 轮播图
    private ViewPager2 vpBanner;
    private LinearLayout llDots;
    private final List<String> bannerUrls = new ArrayList<>();
    private final List<Long> bannerGoodsIds = new ArrayList<>();
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private static final long AUTO_SCROLL_DELAY = 3000;
    private static final long RESUME_SCROLL_DELAY = 5000; // 手滑后恢复自动轮播的延迟
    private boolean isUserDragging = false; // 用户是否正在手动滑动
    private final int[] fallbackBanners = {
            R.drawable.banner_home_1,
            R.drawable.banner_home_2,
            R.drawable.banner_home_3
    };

    // 标题栏
    private View toolbar;
    private TextView tvToolbarTitle;

    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (vpBanner.getAdapter() != null && bannerUrls.size() > 1) {
                int next = (vpBanner.getCurrentItem() + 1) % bannerUrls.size();
                vpBanner.setCurrentItem(next, true);
            }
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
        }
    };

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

        // 标题栏
        toolbar = view.findViewById(R.id.toolbar);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        TextView tvToolbarLogin = view.findViewById(R.id.tvToolbarLogin);
        tvToolbarLogin.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), LoginActivity.class)));

        // 轮播图
        vpBanner = view.findViewById(R.id.vpBanner);
        llDots = view.findViewById(R.id.llDots);

        // 设置 BannerContainer 的主 ViewPager2 引用，让它能在 dispatchTouchEvent 阶段
        // 禁用主 tab ViewPager2 的滑动，解决嵌套 ViewPager2 的手滑冲突
        BannerContainer bannerContainer = view.findViewById(R.id.bannerContainer);
        if (getActivity() instanceof MainActivity) {
            bannerContainer.setMainViewPager(((MainActivity) getActivity()).getViewPager());
        }

        ScrollView scrollView = view.findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            // 将 200dp 转换为像素
            float density = getResources().getDisplayMetrics().density;
            int bannerHeightPx = (int) (200 * density);
            float alpha = Math.min(1f, scrollY / (float) bannerHeightPx);
            toolbar.setBackgroundColor(((int) (alpha * 255) << 24) | 0x001ABC9C);
            tvToolbarTitle.setAlpha(alpha);
        });

        TextView tvSearch = view.findViewById(R.id.tvSearch);
        GridView gvNav = view.findViewById(R.id.gvNav);
        GridView gvNewGoods = view.findViewById(R.id.gvNewGoods);
        GridView gvHotGoods = view.findViewById(R.id.gvHotGoods);
        GridView gvRecommendGoods = view.findViewById(R.id.gvRecommendGoods);

        // 导航使用自定义 NavAdapter（彩色圆形图标+文字标签）
        gvNav.setAdapter(new NavAdapter(requireContext()));
        gvNav.setOnItemClickListener((parent, itemView, position, id) -> {
        });

        newAdapter = new GoodsGridAdapter(requireContext(), newGoods);
        hotAdapter = new GoodsGridAdapter(requireContext(), hotGoods);
        recommendAdapter = new GoodsGridAdapter(requireContext(), recommendGoods);
        gvNewGoods.setAdapter(newAdapter);
        gvHotGoods.setAdapter(hotAdapter);
        gvRecommendGoods.setAdapter(recommendAdapter);

        tvSearch.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));
        loadHomeData();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    private void startAutoScroll() {
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
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
            // 先检测图片服务器（必须在后台线程执行）
            HttpUtil.detectImageServer();
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
        bannerUrls.clear();
        bannerGoodsIds.clear();
        if (carousels != null) {
            for (int i = 0; i < carousels.length(); i++) {
                JSONObject item = carousels.optJSONObject(i);
                if (item == null) continue;
                String img = item.optString("carouselUrl", "");
                if (img.isEmpty()) img = item.optString("goodsCoverImg", "");
                long goodsId = item.optLong("goodsId", 0);
                if (!img.isEmpty()) {
                    img = HttpUtil.buildImageUrl(img);
                    bannerUrls.add(img);
                    bannerGoodsIds.add(goodsId);
                }
            }
        }

        if (bannerUrls.size() < 3) {
            bannerUrls.clear();
            bannerGoodsIds.clear();
            for (int i = 0; i < fallbackBanners.length; i++) {
                bannerUrls.add("android.resource://" + requireContext().getPackageName() + "/" + fallbackBanners[i]);
                bannerGoodsIds.add(0L);
            }
        }

        if (bannerUrls.isEmpty()) return;

        // 设置 ViewPager2 适配器
        vpBanner.setAdapter(new BannerAdapter(bannerUrls));

        // 创建指示器圆点
        llDots.removeAllViews();
        for (int i = 0; i < bannerUrls.size(); i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0
                    ? android.R.drawable.presence_online
                    : android.R.drawable.presence_invisible);
            llDots.addView(dot);
        }

        // 监听页面切换和滑动状态，更新指示器 + 手滑暂停/恢复自动轮播
        vpBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < llDots.getChildCount(); i++) {
                    llDots.getChildAt(i).setBackgroundResource(
                            i == position
                                    ? android.R.drawable.presence_online
                                    : android.R.drawable.presence_invisible);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    // 用户手动滑动时暂停自动轮播
                    isUserDragging = true;
                    stopAutoScroll();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (isUserDragging) {
                        // 用户松手后，延迟恢复自动轮播
                        isUserDragging = false;
                        autoScrollHandler.postDelayed(autoScrollRunnable, RESUME_SCROLL_DELAY);
                    }
                }
            }
        });
    }

    /**
     * 轮播图适配器
     */
    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.VH> {
        private final List<String> urls;

        BannerAdapter(List<String> urls) {
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
            if (url.startsWith("android.resource://")) {
                try {
                    int resId = Integer.parseInt(url.substring(url.lastIndexOf('/') + 1));
                    holder.iv.setImageResource(resId);
                } catch (Exception e) {
                    HttpUtil.loadImage(holder.iv.getContext(), url, holder.iv);
                }
            } else {
                HttpUtil.loadImage(holder.iv.getContext(), url, holder.iv);
            }

            // 点击轮播跳转到对应商品详情
            holder.iv.setOnClickListener(v -> {
                if (position < bannerGoodsIds.size() && bannerGoodsIds.get(position) > 0) {
                    Intent intent = new Intent(requireContext(), DetailActivity.class);
                    intent.putExtra("goodsId", bannerGoodsIds.get(position));
                    startActivity(intent);
                }
            });
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
}
