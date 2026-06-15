package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newbeemall.CheckoutActivity;
import com.example.newbeemall.LoginActivity;
import com.example.newbeemall.MainActivity;
import com.example.newbeemall.R;
import com.example.newbeemall.adapter.CartAdapter;
import com.example.newbeemall.model.CartItem;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车页
 */
public class CartFragment extends Fragment implements CartAdapter.OnCartActionListener {
    private final List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter adapter;
    private TextView tvCartSummary;
    private Button btnCheckout;
    private CheckBox cbSelectAll;
    private View llEmptyState;
    private View llBottomBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lvCart = view.findViewById(R.id.lvCart);
        tvCartSummary = view.findViewById(R.id.tvCartSummary);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        llBottomBar = view.findViewById(R.id.llBottomBar);

        // 空状态“前往选购”按钮
        view.findViewById(R.id.btnGoShopping).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(0); // 跳转首页
            }
        });

        adapter = new CartAdapter(requireContext(), cartItems);
        adapter.setOnCartActionListener(this);
        lvCart.setAdapter(adapter);

        btnCheckout.setOnClickListener(v -> checkout());

        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CartItem item : cartItems) {
                item.setSelected(isChecked);
            }
            adapter.notifyDataSetChanged();
            updateSummary();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        if (!HttpUtil.hasToken(requireContext())) {
            cartItems.clear();
            adapter.notifyDataSetChanged();
            tvCartSummary.setText("请先登录查看购物车");
            btnCheckout.setText("去登录");
            cbSelectAll.setVisibility(View.GONE);
            return;
        }
        cbSelectAll.setVisibility(View.VISIBLE);
        btnCheckout.setText("去结算");
        new Thread(() -> {
            HttpUtil.detectImageServer();
            String result = HttpUtil.get("/api/v1/shop-cart", requireContext());
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(requireContext(), "购物车加载失败", Toast.LENGTH_SHORT).show();
                    tvCartSummary.setText("购物车加载失败");
                    return;
                }
                try {
                    // token失效自动跳转登录
                    if (JsonUtil.isTokenExpired(result)) {
                        HttpUtil.clearToken(requireContext());
                        Toast.makeText(requireContext(), "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        return;
                    }
                    if (!JsonUtil.isSuccess(result)) {
                        cartItems.clear();
                        adapter.notifyDataSetChanged();
                        tvCartSummary.setText(JsonUtil.message(result));
                        return;
                    }
                    cartItems.clear();
                    cartItems.addAll(JsonUtil.parseCartItems(result));
                    // 默认全选
                    for (CartItem item : cartItems) {
                        item.setSelected(true);
                    }
                    adapter.notifyDataSetChanged();
                    updateSummary();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "购物车解析失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateSummary() {
        double total = 0;
        int count = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
                count += item.getGoodsCount();
            }
        }

        // 空状态处理
        boolean isEmpty = cartItems.isEmpty() && HttpUtil.hasToken(requireContext());
        llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        llBottomBar.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (cartItems.isEmpty()) {
            tvCartSummary.setText("购物车为空");
        } else {
            tvCartSummary.setText("已选 " + count + " 件  合计：¥" + String.format("%.2f", total));
        }
        // 更新全选状态
        boolean allSelected = !cartItems.isEmpty();
        for (CartItem item : cartItems) {
            if (!item.isSelected()) {
                allSelected = false;
                break;
            }
        }
        cbSelectAll.setOnCheckedChangeListener(null);
        cbSelectAll.setChecked(allSelected);
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CartItem item : cartItems) {
                item.setSelected(isChecked);
            }
            adapter.notifyDataSetChanged();
            updateSummary();
        });
    }

    @Override
    public void onQuantityChanged(CartItem item, int newCount) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("cartItemId", item.getCartItemId());
                body.put("goodsCount", newCount);
                String result = HttpUtil.put("/api/v1/shop-cart", body.toString(), requireContext());
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (result != null && JsonUtil.isSuccess(result)) {
                        item.setGoodsCount(newCount);
                        adapter.notifyDataSetChanged();
                        updateSummary();
                    } else {
                        Toast.makeText(requireContext(), "修改数量失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "修改数量出错", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onSelectionChanged() {
        updateSummary();
    }

    @Override
    public void onDeleteItem(CartItem item) {
        new Thread(() -> {
            String result = HttpUtil.delete("/api/v1/shop-cart/" + item.getCartItemId(), requireContext());
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (result != null && JsonUtil.isSuccess(result)) {
                    cartItems.remove(item);
                    adapter.notifyDataSetChanged();
                    updateSummary();
                    Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void checkout() {
        if (!HttpUtil.hasToken(requireContext())) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }
        // 只结算选中的商品
        List<Long> selectedIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedIds.add(item.getCartItemId());
            }
        }
        if (selectedIds.isEmpty()) {
            Toast.makeText(requireContext(), "请选择要结算的商品", Toast.LENGTH_SHORT).show();
            return;
        }
        long[] ids = new long[selectedIds.size()];
        for (int i = 0; i < selectedIds.size(); i++) {
            ids[i] = selectedIds.get(i);
        }
        Intent intent = new Intent(requireContext(), CheckoutActivity.class);
        intent.putExtra("cartItemIds", ids);
        startActivity(intent);
    }
}
