package com.example.newbeemall.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.example.newbeemall.R;

/**
 * 轮播图容器 — 解决嵌套 ViewPager2 的手滑冲突。
 *
 * 轮播 ViewPager2 同时嵌套在 ScrollView 和主 tab ViewPager2 内，
 * 两层都会截走横向手势，导致用户无法手动翻轮播页。
 *
 * 本容器在 dispatchTouchEvent（手势最早到达的阶段）中：
 * 1. 手指碰到轮播区域 → 禁用主 ViewPager2 的用户滑动
 * 2. 沿父链往上 requestDisallowInterceptTouchEvent(true) → 禁止 ScrollView 拦截
 * 3. 手指离开 → 恢复主 ViewPager2 和 ScrollView 的拦截权限
 *
 * 注意：排除搜索栏区域，触摸搜索栏时不触发冲突处理。
 */
public class BannerContainer extends FrameLayout {

    private ViewPager2 mainViewPager;
    private Rect searchBarRect = new Rect();

    public BannerContainer(Context context) {
        super(context);
    }

    public BannerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BannerContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置主 tab ViewPager2 的引用，用于在触摸轮播时禁用其滑动
     */
    public void setMainViewPager(ViewPager2 viewPager) {
        this.mainViewPager = viewPager;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        // 检查触摸点是否在搜索栏区域内（搜索栏有自己的点击处理，不需要冲突处理）
        View searchBar = findViewById(R.id.tvSearch); // 这里只能用间接方式
        // 注意：在 dispatchTouchEvent 中调用 findViewById 会每次遍历子 View，
        // 性能不太好，但轮播区域只有几个子 View，影响可忽略
        boolean touchOnSearchBar = false;
        if (searchBar != null) {
            searchBar.getHitRect(searchBarRect);
            touchOnSearchBar = searchBarRect.contains((int) ev.getX(), (int) ev.getY());
        }

        if (!touchOnSearchBar) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                // 禁用主 tab ViewPager2 的用户滑动，防止横向手势被截走切换 tab
                if (mainViewPager != null) {
                    mainViewPager.setUserInputEnabled(false);
                }
                // 沿父链往上禁止 ScrollView 拦截，保证横向手势交给轮播
                ViewParent parent = getParent();
                while (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                    parent = parent.getParent();
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                // 手指离开，恢复主 ViewPager2 和 ScrollView 的拦截权限
                if (mainViewPager != null) {
                    mainViewPager.setUserInputEnabled(true);
                }
                ViewParent parent = getParent();
                while (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                    parent = parent.getParent();
                }
            }
        }

        return super.dispatchTouchEvent(ev);
    }
}
