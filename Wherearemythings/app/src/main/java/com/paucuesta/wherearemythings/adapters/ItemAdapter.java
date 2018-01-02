package com.paucuesta.wherearemythings.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.paucuesta.wherearemythings.R;
import com.paucuesta.wherearemythings.models.Item;
import com.squareup.picasso.Picasso;

import java.util.List;

import static java.lang.Integer.parseInt;

/**
 * Created by Pau on 30/12/2017.
 */

public class ItemAdapter extends BaseAdapter {

    private Context context;
    private List<Item> list;
    private int layout;

    public ItemAdapter(Context context, List<Item> items, int layout) {
        this.context = context;
        this.list = items;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder vh;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layout, null);
            vh = new ViewHolder();
            vh.image = (ImageView) convertView.findViewById(R.id.imageViewItem);
            vh.name = (TextView) convertView.findViewById(R.id.textViewItemName);
            vh.description = (TextView) convertView.findViewById(R.id.textViewItemDescription);
            vh.place = (TextView) convertView.findViewById(R.id.textViewItemPlace);
            vh.quantity = (TextView) convertView.findViewById(R.id.textViewItemQuantity);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        Item item = list.get(position);

        vh.name.setText(item.getName());
        vh.description.setText(item.getDescription());
        vh.place.setText(item.getPlace());
        vh.quantity.setText(String.valueOf(item.getQuantity()));
        Picasso.with(context).load("file:///"+ item.getPhoto()).placeholder(R.drawable.placeholder).fit().into(vh.image);

        return convertView;
    }

    public class ViewHolder{
        ImageView image;
        TextView name;
        TextView description;
        TextView place;
        TextView place2;
        TextView quantity;
    }
}
