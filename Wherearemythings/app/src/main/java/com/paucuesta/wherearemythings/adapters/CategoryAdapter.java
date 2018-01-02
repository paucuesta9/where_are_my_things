package com.paucuesta.wherearemythings.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.paucuesta.wherearemythings.R;
import com.paucuesta.wherearemythings.models.Category;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pau on 30/12/2017.
 */

public class CategoryAdapter extends BaseAdapter {

    private Context context;
    private List<Category> list;
    private int layout;

    public CategoryAdapter(Context context, List<Category> categories, int layout) {
        this.context = context;
        this.list = categories;
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
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layout, null);
            vh = new ViewHolder();
            vh.name = (TextView) convertView.findViewById(R.id.textViewCategoryName);
            vh.image = (ImageView) convertView.findViewById(R.id.imageViewCategory);
            vh.items = (TextView) convertView.findViewById(R.id.textViewItemsQuantity);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        Category category = list.get(position);
        vh.name.setText(category.getName());
        Picasso.with(context).load("file:///"+ category.getPhoto()).placeholder(R.drawable.placeholder).fit().into(vh.image);

        int numberOfItems = category.getItems().size();
        String textforItems = (numberOfItems == 1) ? numberOfItems + " Item" : numberOfItems + " Items";
        vh.items.setText(textforItems);

        return convertView;
    }

    public class ViewHolder {
        TextView name;
        ImageView image;
        TextView items;
    }
}
