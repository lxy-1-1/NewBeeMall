package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newbeemall.AddressListActivity;
import com.example.newbeemall.LoginActivity;
import com.example.newbeemall.OrderListActivity;
import com.example.newbeemall.R;
import com.example.newbeemall.util.HttpUtil;

/**
 * 我的 - 小组介绍
 */
public class MyFragment extends Fragment {
    private Button btnLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvGroupInfo = view.findViewById(R.id.tvGroupInfo);
        btnLogin = view.findViewById(R.id.btnLogin);
        Button btnOrders = view.findViewById(R.id.btnOrders);
        Button btnAddress = view.findViewById(R.id.btnAddress);

        tvGroupInfo.setText("新蜂商城 Android 客户端\n\n已实现：首页、分类、搜索、详情、购物车、地址管理、生成订单、我的订单");
        btnLogin.setOnClickListener(v -> startActivity(new Intent(requireContext(), LoginActivity.class)));
        btnOrders.setOnClickListener(v -> startActivity(new Intent(requireContext(), OrderListActivity.class)));
        btnAddress.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddressListActivity.class)));
        refreshLoginState();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLoginState();
    }

    private void refreshLoginState() {
        if (btnLogin == null) return;
        btnLogin.setText(HttpUtil.hasToken(requireContext()) ? "重新登录" : "登录");
    }
}
