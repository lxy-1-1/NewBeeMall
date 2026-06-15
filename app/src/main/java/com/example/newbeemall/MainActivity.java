package com.example.newbeemall;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.newbeemall.adapter.MainPagerAdapter;
import com.example.newbeemall.fragment.CartFragment;
import com.example.newbeemall.fragment.CategoryFragment;
import com.example.newbeemall.fragment.HomeFragment;
import com.example.newbeemall.fragment.MyFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private final String[] tabTitles = {"首页", "分类", "购物车", "我的"};
    private final int[] tabIcons = {R.drawable.ic_tab_home, R.drawable.ic_tab_category, R.drawable.ic_tab_cart, R.drawable.ic_tab_my};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // 准备 Fragment 列表
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new CategoryFragment());
        fragments.add(new CartFragment());
        fragments.add(new MyFragment());

        // 设置 ViewPager 适配器
        MainPagerAdapter adapter = new MainPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);
        // 预加载所有页面，避免切换时重建
        viewPager.setOffscreenPageLimit(3);

        // 绑定 TabLayout 和 ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
            tab.setIcon(tabIcons[position]);
        }).attach();

        int tabIndex = getIntent().getIntExtra("tabIndex", 0);
        selectTab(tabIndex);
    }

    public void selectTab(int index) {
        if (viewPager != null && index >= 0 && index < tabTitles.length) {
            viewPager.setCurrentItem(index, false);
        }
    }
}
