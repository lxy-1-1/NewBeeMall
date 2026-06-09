package com.example.newbeemall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.newbeemall.R;
import com.example.newbeemall.model.OrderItem;

import java.util.List;

public class OrderAdapter extends BaseAdapter {
    private final Context context;
    private final List<OrderItem> orders;

    public OrderAdapter(Context context, List<OrderItem> orders) {
        this.context = context;
        this.orders = orders;
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public OrderItem getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
            holder = new Holder();
            holder.no = convertView.findViewById(R.id.tvOrderNo);
            holder.status = convertView.findViewById(R.id.tvOrderStatus);
            holder.price = convertView.findViewById(R.id.tvOrderPrice);
            holder.time = convertView.findViewById(R.id.tvOrderTime);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        OrderItem item = orders.get(position);
        holder.no.setText("订单号：" + item.getOrderNo());
        holder.status.setText(statusText(item.getOrderStatus()));
        holder.price.setText("¥" + item.getTotalPrice());
        holder.time.setText(item.getCreateTime());
        return convertView;
    }

    public static String statusText(int status) {
        switch (status) {
            case 0:
                return "待付款";
            case 1:
                return "待确认";
            case 2:
                return "待发货";
            case 3:
                return "已发货";
            case 4:
                return "交易完成";
            default:
                return "全部";
        }
    }

    static class Holder {
        TextView no;
        TextView status;
        TextView price;
        TextView time;
    }
}
