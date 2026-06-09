package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newbeemall.CheckoutActivity;
import com.example.newbeemall.LoginActivity;
import com.example.newbeemall.R;
import com.example.newbeemall.adapter.CartAdapter;
import com.example.newbeemall.model.CartItem;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车页
 */
public class CartFragment extends Fragment {
    private final List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter adapter;
    private TextView tvCartSummary;
    private Button btnCheckout;

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
        adapter = new CartAdapter(requireContext(), cartItems);
        lvCart.setAdapter(adapter);
        btnCheckout.setOnClickListener(v -> checkout());
        lvCart.setOnItemLongClickListener((parent, itemView, position, id) -> {
            removeCartItem(cartItems.get(position).getCartItemId());
            return true;
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
            return;
        }
        btnCheckout.setText("去结算");
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/shop-cart", requireContext());
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(requireContext(), "购物车加载失败", Toast.LENGTH_SHORT).show();
                    tvCartSummary.setText("购物车加载失败");
                    return;
                }
                try {
                    if (!JsonUtil.isSuccess(result)) {
                        cartItems.clear();
                        adapter.notifyDataSetChanged();
                        tvCartSummary.setText(JsonUtil.message(result));
                        return;
                    }
                    cartItems.clear();
                    cartItems.addAll(JsonUtil.parseCartItems(result));
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
            total += item.getTotalPrice();
            count += item.getGoodsCount();
        }
        if (cartItems.isEmpty()) {
            tvCartSummary.setText("购物车为空，先去商品详情加入商品");
        } else {
            tvCartSummary.setText("共 " + count + " 件，合计 ¥" + total);
        }
    }

    private void checkout() {
        if (!HttpUtil.hasToken(requireContext())) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "购物车为空", Toast.LENGTH_SHORT).show();
            return;
        }
        long[] ids = new long[cartItems.size()];
        for (int i = 0; i < cartItems.size(); i++) {
            ids[i] = cartItems.get(i).getCartItemId();
        }
        Intent intent = new Intent(requireContext(), CheckoutActivity.class);
        intent.putExtra("cartItemIds", ids);
        startActivity(intent);
    }

    private void removeCartItem(long cartItemId) {
        new Thread(() -> {
            String result = HttpUtil.delete("/api/v1/shop-cart/" + cartItemId, requireContext());
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), result == null || !JsonUtil.isSuccess(result) ? "删除失败" : "已删除", Toast.LENGTH_SHORT).show();
                loadCart();
            });
        }).start();
    }
}
