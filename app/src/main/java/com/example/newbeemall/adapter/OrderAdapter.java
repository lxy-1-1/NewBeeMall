package com.example.newbeemall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.newbeemall.R;
import com.example.newbeemall.model.OrderItem;
import com.example.newbeemall.util.JsonUtil;

import java.util.List;

public class OrderAdapter extends BaseAdapter {
    private final Context context;
    private final List<OrderItem> orders;

    public OrderAdapter(Context context, List<OrderItem> orders) {
        this.context = context;
        this.orders = orders;
    }

    @Override
    public int getCount() { return orders.size(); }

    @Override
    public OrderItem getItem(int position) { return orders.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
            holder = new Holder();
            holder.tvTime = convertView.findViewById(R.id.tvOrderTime);
            holder.tvStatus = convertView.findViewById(R.id.tvOrderStatus);
            holder.tvNo = convertView.findViewById(R.id.tvOrderNo);
            holder.tvPrice = convertView.findViewById(R.id.tvOrderPrice);
            holder.lvGoods = convertView.findViewById(R.id.lvOrderGoods);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        OrderItem item = orders.get(position);
        holder.tvTime.setText(item.getCreateTime());
        holder.tvStatus.setText(statusText(item.getOrderStatus()));
        holder.tvNo.setText("订单号：" + item.getOrderNo());
        holder.tvPrice.setText(JsonUtil.formatPrice(item.getTotalPrice()));

        // 嵌套商品列表
        if (item.getGoodsList() != null && !item.getGoodsList().isEmpty()) {
            OrderGoodsAdapter goodsAdapter = new OrderGoodsAdapter(context, item.getGoodsList());
            holder.lvGoods.setAdapter(goodsAdapter);
            holder.lvGoods.setVisibility(View.VISIBLE);
        } else {
            holder.lvGoods.setVisibility(View.GONE);
        }

        return convertView;
    }

    public static String statusText(int status) {
        switch (status) {
            case 0: return "待付款";
            case 1: return "待确认";
            case 2: return "待发货";
            case 3: return "已发货";
            case 4: return "交易完成";
            default: return "已支付";
        }
    }

    static class Holder {
        TextView tvTime, tvStatus, tvNo, tvPrice;
        ListView lvGoods;
    }
}
