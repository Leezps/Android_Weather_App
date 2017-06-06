package com.android.leezp.androidimhwweatherdemo.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.leezp.androidimhwweatherdemo.R;

import java.util.List;

/**
 * Created by Leezp on 2017/4/23 0023.
 */

public class SpinerAdapter extends BaseAdapter{

    public static interface IOnItemSelectListener {
        public void onItemClick(int pos);
    }

    private List<String> objects;

    private LayoutInflater inflater;

    public SpinerAdapter(Context context, List<String> objects) {
        this.objects = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(List<String> objects) {

        this.objects = objects;

        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position).toString();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_set_custom_spinner,null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.activity_set_custom_spinner_timeSelect);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(objects.get(position));

        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
