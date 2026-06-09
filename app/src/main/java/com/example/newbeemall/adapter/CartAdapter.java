package com.example.newbeemall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.newbeemall.R;
import com.example.newbeemall.model.CartItem;
import com.example.newbeemall.util.HttpUtil;

import java.util.List;

public class CartAdapter extends BaseAdapter {
    private final Context context;
    private final List<CartItem> items;

    public CartAdapter(Context context, List<CartItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CartItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getCartItemId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
            holder = new Holder();
            holder.cover = convertView.findViewById(R.id.ivCartCover);
            holder.name = convertView.findViewById(R.id.tvCartName);
            holder.price = convertView.findViewById(R.id.tvCartPrice);
            holder.count = convertView.findViewById(R.id.tvCartCount);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        CartItem item = items.get(position);
        holder.name.setText(item.getGoodsName());
        holder.price.setText("¥" + item.getSellingPrice());
        holder.count.setText("x" + item.getGoodsCount());
        String imgUrl = item.getGoodsCoverImg();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            if (!imgUrl.startsWith("http")) imgUrl = HttpUtil.BASE_URL + imgUrl;
            Glide.with(context).load(imgUrl).into(holder.cover);
        }
        return convertView;
    }

    static class Holder {
        ImageView cover;
        TextView name;
        TextView price;
        TextView count;
    }
}
