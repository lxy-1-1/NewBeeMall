package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.newbeemall.AccountManageActivity;
import com.example.newbeemall.AddressListActivity;
import com.example.newbeemall.OrderListActivity;
import com.example.newbeemall.R;
import com.example.newbeemall.util.HttpUtil;

/**
 * 我的 - 用户信息卡片 + 菜单列表（我的订单/账号管理/地址管理/关于我们）
 */
public class MyFragment extends Fragment {
    private TextView tvNickname;
    private TextView tvLoginName;
    private TextView tvSignature;

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

        tvNickname = view.findViewById(R.id.tvNickname);
        tvLoginName = view.findViewById(R.id.tvLoginName);
        tvSignature = view.findViewById(R.id.tvSignature);

        // 我的订单
        View menuOrders = view.findViewById(R.id.menuOrders);
        menuOrders.setOnClickListener(v -> {
            if (!HttpUtil.hasToken(requireContext())) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(requireContext(), OrderListActivity.class));
        });

        // 账号管理
        View menuAccount = view.findViewById(R.id.menuAccount);
        menuAccount.setOnClickListener(v -> {
            if (!HttpUtil.hasToken(requireContext())) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(requireContext(), AccountManageActivity.class));
        });

        // 地址管理
        View menuAddress = view.findViewById(R.id.menuAddress);
        menuAddress.setOnClickListener(v -> {
            if (!HttpUtil.hasToken(requireContext())) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(requireContext(), AddressListActivity.class));
        });

        // 关于我们 - 显示开发小组介绍
        View menuAbout = view.findViewById(R.id.menuAbout);
        menuAbout.setOnClickListener(v -> showAboutDialog());

        refreshLoginState();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLoginState();
    }

    private void refreshLoginState() {
        if (tvNickname == null) return;
        boolean loggedIn = HttpUtil.hasToken(requireContext());
        String phone = HttpUtil.getPhone(requireContext());
        
        if (loggedIn && !phone.isEmpty()) {
            tvNickname.setText("昵称：" + phone);
            tvLoginName.setText("登录名：" + phone);
        } else {
            tvNickname.setText("昵称：未登录");
            tvLoginName.setText("登录名：未登录");
        }
        String signature = HttpUtil.getSignature(requireContext());
        tvSignature.setText("个性签名：" + signature);
    }

    /**
     * 显示开发小组及人员介绍
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("关于我们")
                .setMessage("新蜂商城 Android 客户端\n\n" +
                        "【开发小组介绍】\n\n" +
                        "小组成员及分工：\n\n" +
                        "杨富鈞：首页、地址管理\n" +
                        "白奥涵：购物车、生成订单\n" +
                        "马浩：我的订单、商品搜索、账号管理、我的\n" +
                        "刘晨阳：分类、商品详情\n\n" +
                        "项目：新蜂商城 Android 实训")
                .setPositiveButton("确定", null)
                .show();
    }
}
