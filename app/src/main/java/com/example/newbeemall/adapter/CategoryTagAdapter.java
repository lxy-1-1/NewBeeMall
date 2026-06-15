package com.example.newbeemall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.newbeemall.R;
import com.example.newbeemall.model.CategoryItem;

import java.util.List;

/**
 * 分类子项标签适配器（teal 标签样式）
 */
public class CategoryTagAdapter extends BaseAdapter {
    private final Context context;
    private final List<CategoryItem> items;

    public CategoryTagAdapter(Context context, List<CategoryItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

    @Override
    public long getItemId(int position) { return items.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_category_tag, parent, false);
        }
        TextView tv = convertView.findViewById(R.id.tvCategoryTag);
        tv.setText(items.get(position).getName());
        return convertView;
    }
}
