package com.example.newbeemall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newbeemall.R;
import com.example.newbeemall.model.Address;

import java.util.List;

public class AddressAdapter extends BaseAdapter {
    private final Context context;
    private final List<Address> addresses;

    public AddressAdapter(Context context, List<Address> addresses) {
        this.context = context;
        this.addresses = addresses;
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public Address getItem(int position) {
        return addresses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return addresses.get(position).getAddressId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
            holder = new Holder();
            holder.name = convertView.findViewById(R.id.tvAddressName);
            holder.address = convertView.findViewById(R.id.tvAddressDetail);
            holder.defaultTag = convertView.findViewById(R.id.tvDefaultTag);
            holder.edit = convertView.findViewById(R.id.ivEdit);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        Address item = addresses.get(position);
        String label = item.getUserName() + "  " + item.getUserPhone();
        holder.defaultTag.setVisibility(item.getDefaultFlag() == 1 ? View.VISIBLE : View.GONE);
        holder.name.setText(label);
        holder.address.setText(item.getFullAddress());
        return convertView;
    }

    static class Holder {
        TextView name;
        TextView address;
        TextView defaultTag;
        ImageView edit;
    }
}
