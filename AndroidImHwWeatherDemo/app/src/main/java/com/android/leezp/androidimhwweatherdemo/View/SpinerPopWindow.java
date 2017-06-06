package com.android.leezp.androidimhwweatherdemo.View;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.android.leezp.androidimhwweatherdemo.R;

import java.util.List;

/**
 * function:    自定义Spinner布局
 * Created by Leezp on 2017/4/23 0023.
 */

public class SpinerPopWindow extends PopupWindow implements AdapterView.OnItemClickListener{
    private Context context;
    private ListView listView;
    private SpinerAdapter adapter;
    private SpinerAdapter.IOnItemSelectListener mItemSelectListener;

    public SpinerPopWindow(Context context) {
        super(context);
        
        this.context = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_set_custom_spinner_window,null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        ColorDrawable colorDrawable = new ColorDrawable(0x00);
        setBackgroundDrawable(colorDrawable);

        listView = (ListView) view.findViewById(R.id.activity_set_spinner_window_list);
        listView.setOnItemClickListener(this);
    }

    public void setItemListener(SpinerAdapter.IOnItemSelectListener listener) {
        mItemSelectListener = listener;
    }

    public void setAdapter(SpinerAdapter adapter) {
        this.adapter = adapter;
        listView.setAdapter(adapter);
    }

    public void refreshData(List<String> list, int index) {
        if (list != null && index != -1) {
            if (adapter != null) {
                adapter.refreshData(list);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        if (mItemSelectListener != null) {
            mItemSelectListener.onItemClick(position);
        }
    }
}
