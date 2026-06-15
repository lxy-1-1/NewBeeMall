package com.example.newbeemall.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newbeemall.DetailActivity;
import com.example.newbeemall.R;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.util.HttpUtil;

import java.util.List;

/**
 * 商品列表适配器（用于搜索结果 ListView，每行一个商品）
 */
public class GoodsListAdapter extends BaseAdapter {

    private final Context context;
    private final List<Goods> goodsList;

    public GoodsListAdapter(Context context, List<Goods> goodsList) {
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
        return goodsList.get(position).getGoodsId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_search_goods, parent, false);
            holder = new ViewHolder();
            holder.ivCover = convertView.findViewById(R.id.ivGoodsCover);
            holder.tvName = convertView.findViewById(R.id.tvGoodsName);
            holder.tvIntro = convertView.findViewById(R.id.tvGoodsIntro);
            holder.tvPrice = convertView.findViewById(R.id.tvGoodsPrice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Goods goods = goodsList.get(position);

        holder.tvName.setText(goods.getGoodsName());
        holder.tvIntro.setText(goods.getGoodsIntro() != null ? goods.getGoodsIntro() : "新蜂精选");
        holder.tvPrice.setText("¥ " + goods.getSellingPrice());

        // 加载图片
        HttpUtil.loadImage(context, goods.getGoodsCoverImg(), holder.ivCover);

        // 点击跳转详情
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("goodsId", goods.getGoodsId());
            context.startActivity(intent);
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView ivCover;
        TextView tvName, tvIntro, tvPrice;
    }
}
