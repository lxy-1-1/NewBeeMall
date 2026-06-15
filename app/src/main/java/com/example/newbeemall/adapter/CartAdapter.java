package com.example.newbeemall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newbeemall.R;
import com.example.newbeemall.model.CartItem;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import java.util.List;

public class CartAdapter extends BaseAdapter {
    private final Context context;
    private final List<CartItem> items;
    private OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, int newCount);
        void onSelectionChanged();
        void onDeleteItem(CartItem item);
    }

    public void setOnCartActionListener(OnCartActionListener listener) {
        this.listener = listener;
    }

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
            holder.cbSelect = convertView.findViewById(R.id.cbSelect);
            holder.cover = convertView.findViewById(R.id.ivCartCover);
            holder.name = convertView.findViewById(R.id.tvCartName);
            holder.price = convertView.findViewById(R.id.tvCartPrice);
            holder.count = convertView.findViewById(R.id.tvCartCount);
            holder.btnMinus = convertView.findViewById(R.id.btnMinus);
            holder.btnPlus = convertView.findViewById(R.id.btnPlus);
            holder.tvDelete = convertView.findViewById(R.id.tvDelete);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        CartItem item = items.get(position);

        // 防止 CheckBox 监听重复触发
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(item.isSelected());
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (listener != null) listener.onSelectionChanged();
        });

        holder.name.setText(item.getGoodsName());
        holder.price.setText(JsonUtil.formatPrice(item.getSellingPrice()));
        holder.count.setText(String.valueOf(item.getGoodsCount()));

        // 加载图片
        HttpUtil.loadImage(context, item.getGoodsCoverImg(), holder.cover);

        // 数量减少
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getGoodsCount() > 1) {
                int newCount = item.getGoodsCount() - 1;
                if (listener != null) listener.onQuantityChanged(item, newCount);
            }
        });

        // 数量增加
        holder.btnPlus.setOnClickListener(v -> {
            int newCount = item.getGoodsCount() + 1;
            if (listener != null) listener.onQuantityChanged(item, newCount);
        });

        // 删除商品
        holder.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteItem(item);
        });

        return convertView;
    }

    static class Holder {
        CheckBox cbSelect;
        ImageView cover;
        TextView name;
        TextView price;
        TextView count;
        Button btnMinus;
        Button btnPlus;
        TextView tvDelete;
    }
}
