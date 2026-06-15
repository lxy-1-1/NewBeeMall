package com.example.newbeemall.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.newbeemall.R;

/**
 * 首页导航图标适配器（彩色圆形 + 文字图标 + 标签）
 * 严格按照参考图 image5 的布局和颜色
 */
public class NavAdapter extends BaseAdapter {

    private final Context context;

    // 导航项数据：{标签, 圆形内图标文字, 背景颜色}
    // 严格按照参考图：第一行5个 + 第二行5个
    private static final String[][] NAV_ITEMS = {
            // 第一行
            {"新蜂超市", "🛒", "#FF6D00"},
            {"新蜂服饰", "👕", "#9C27B0"},
            {"全球购",   "✈", "#2196F3"},
            {"新蜂生鲜", "🥬", "#4CAF50"},
            {"新蜂到家", "🧑", "#03A9F4"},
            // 第二行
            {"充值缴费", "充", "#FF9800"},
            {"9.9元拼",  "拼", "#BF360C"},
            {"领券",     "¥", "#E91E63"},
            {"省钱",     "💰", "#FF9800"},
            {"全部",     "▦", "#1ABC9C"},
    };

    public NavAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() { return NAV_ITEMS.length; }

    @Override
    public Object getItem(int position) { return NAV_ITEMS[position]; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_nav, parent, false);
            holder = new Holder();
            holder.vCircle = convertView.findViewById(R.id.vCircle);
            holder.tvIcon = convertView.findViewById(R.id.tvIcon);
            holder.tvLabel = convertView.findViewById(R.id.tvLabel);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        String label = NAV_ITEMS[position][0];
        String icon = NAV_ITEMS[position][1];
        int color = Color.parseColor(NAV_ITEMS[position][2]);

        // 动态设置圆形背景颜色
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        holder.vCircle.setBackground(circle);

        holder.tvIcon.setText(icon);
        holder.tvLabel.setText(label);

        return convertView;
    }

    static class Holder {
        View vCircle;
        TextView tvIcon;
        TextView tvLabel;
    }
}
