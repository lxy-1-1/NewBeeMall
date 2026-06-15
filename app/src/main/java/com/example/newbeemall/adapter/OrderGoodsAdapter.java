package com.example.newbeemall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newbeemall.R;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import java.util.List;

/**
 * 订单商品列表适配器
 */
public class OrderGoodsAdapter extends BaseAdapter {
    private final Context context;
    private final List<Goods> goodsList;

    public OrderGoodsAdapter(Context context, List<Goods> goodsList) {
        this.context = context;
        this.goodsList = goodsList;
    }

    @Override
    public int getCount() {
        return goodsList.size();
    }

    @Override
    public Object getItem(int position) {
        return goodsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order_goods, parent, false);
            holder = new Holder();
            holder.ivCover = convertView.findViewById(R.id.ivGoodsCover);
            holder.tvName = convertView.findViewById(R.id.tvGoodsName);
            holder.tvPrice = convertView.findViewById(R.id.tvGoodsPrice);
            holder.tvCount = convertView.findViewById(R.id.tvGoodsCount);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Goods goods = goodsList.get(position);
        holder.tvName.setText(goods.getGoodsName());
        holder.tvPrice.setText(JsonUtil.formatPrice(goods.getSellingPrice()));
        holder.tvCount.setText("x" + goods.getGoodsCount());

        HttpUtil.loadImage(context, goods.getGoodsCoverImg(), holder.ivCover);

        return convertView;
    }

    static class Holder {
        ImageView ivCover;
        TextView tvName;
        TextView tvPrice;
        TextView tvCount;
    }
}
