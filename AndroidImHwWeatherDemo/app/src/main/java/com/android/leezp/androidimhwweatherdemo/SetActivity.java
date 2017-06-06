package com.android.leezp.androidimhwweatherdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.leezp.androidimhwweatherdemo.View.SpinerAdapter;
import com.android.leezp.androidimhwweatherdemo.View.SpinerPopWindow;

import java.util.ArrayList;
import java.util.List;

public class SetActivity extends Activity implements View.OnClickListener,SpinerAdapter.IOnItemSelectListener,CompoundButton.OnCheckedChangeListener{

    //存储能选择的时间间隔
    private List<String> list = new ArrayList<>();
    private SpinerAdapter adapter;
    //自定义的Spinner
    private SpinerPopWindow spinner;

    //开启通知的按钮
    private Switch openNotify;
    //开启更新的按钮（此按钮打开，才可以选择时间间隔）
    private Switch openUpdate;
    //更新间隔字体
    private TextView updateSpace;
    //时间间隔
    private TextView timeSpace;
    //更新的下拉图片
    private ImageView updateSelectImg;
    //更新时间间隔点击有效区域
    private LinearLayout updateClickPart;

    //当打开通知时，就发送一个打开通知的本地广播，让MainActivity去处理
    //同理，当关闭通知时，也发送一个关闭通知的本地广播，让MainActivity去处理，所以需申明一个广播管理器
    private LocalBroadcastManager localBroadcastManager;

    //SharedPreferences存储设置按钮的数据
    private static final String prefName = "set_data";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //关于两个Switch按钮是打开还是关闭
    private boolean notify;
    private boolean update;

    //用于存储定时更新的时间
    private int timePart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set);
        
        initView();
        
        initEvent();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        openNotify = (Switch) findViewById(R.id.activity_set_openNotify);
        openUpdate = (Switch) findViewById(R.id.activity_set_openUpdate);
        updateSpace = (TextView) findViewById(R.id.activity_set_updateSpace);
        timeSpace = (TextView) findViewById(R.id.activity_set_timeSpace);
        updateSelectImg = (ImageView) findViewById(R.id.activity_set_updateSelectImg);
        updateClickPart = (LinearLayout) findViewById(R.id.activity_set_updateClickPart);
    }

    /**
     * 初始化控件相对应的事件以及一些资源
     */
    private void initEvent() {
        //初始化SharedPreferences对象进行存储数据
        preferences = getSharedPreferences(prefName, MODE_PRIVATE);
        editor = preferences.edit();
        //初始化两个switch按钮以及更新时间段
        notify = preferences.getBoolean("notify", false);
        update = preferences.getBoolean("update", false);
        timePart = preferences.getInt("timePart", 24);
        openNotify.setChecked(notify);
        openUpdate.setChecked(update);
        timeSpace.setText(timePart+"小时");

        //初始化Spinner中的数据源
        String[] timeSpaces = getResources().getStringArray(R.array.time_space);
        for (String timeStr:timeSpaces) {
            list.add(timeStr);
        }
        //初始化适配器
        adapter = new SpinerAdapter(this, list);

        //初始化PopWindow
        spinner = new SpinerPopWindow(this);
        spinner.setAdapter(adapter);
        spinner.setItemListener(this);

        //设置updateClickPart的点击事件
        updateClickPart.setOnClickListener(this);

        //开启通知的监听事件
        openNotify.setOnCheckedChangeListener(this);
        //开启更新的监听事件
        openUpdate.setOnCheckedChangeListener(this);

        //初始化更新部分，是开启更新，还是关闭更新
        if (update) {
            int white = Color.parseColor("#FFFFFF");
            updateSpace.setTextColor(white);
            timeSpace.setTextColor(white);
            updateSelectImg.setImageResource(R.drawable.setting_right_able);
            updateClickPart.setClickable(true);
        } else {
            int lightGray = Color.parseColor("#848484");
            updateSpace.setTextColor(lightGray);
            timeSpace.setTextColor(lightGray);
            updateSelectImg.setImageResource(R.drawable.setting_right_disable);
            updateClickPart.setClickable(false);
        }

        //获取广播管理的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    /**
     * 显示Spinner，进行选择
     */
    private void showSpinnerWindow() {
        updateSelectImg.setImageResource(R.drawable.setting_down_able);
        spinner.setWidth(timeSpace.getWidth()+2);
        spinner.showAsDropDown(timeSpace);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_set_updateClickPart:
                openUpdate.setEnabled(false);
                showSpinnerWindow();
                break;
        }
    }

    /**
     * 自定义Spinner的选择事件
     * @param pos   选择在Spinner中的位置
     */
    @Override
    public void onItemClick(int pos) {
        if (pos>=0 && pos<=list.size()) {
            String value = list.get(pos);
            updateSelectImg.setImageResource(R.drawable.setting_right_able);
            openUpdate.setEnabled(true);
            timeSpace.setText(value);
        }
    }

    /**
     * Switch的监听事件
     * @param buttonView        从哪一个按钮获取的事件
     * @param isChecked         判断是否已选择
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.activity_set_openNotify:
                notify = isChecked;
                //开启与关闭通知
                if (isChecked) {
                    Intent intent_openNotify = new Intent("com.android.leezp.androidimhwweatherdemo.openNotify");
                    //发送开启通知本地广播
                    localBroadcastManager.sendBroadcast(intent_openNotify);
                } else {
                    Intent intent_closeNotify = new Intent("com.android.leezp.androidimhwweatherdemo.closeNotify");
                    //发送关闭通知本地广播
                    localBroadcastManager.sendBroadcast(intent_closeNotify);
                }
                break;
            case R.id.activity_set_openUpdate:
                update = isChecked;
                //开启与关闭更新
                if (isChecked) {
                    int white = Color.parseColor("#FFFFFF");
                    updateSpace.setTextColor(white);
                    timeSpace.setTextColor(white);
                    updateSelectImg.setImageResource(R.drawable.setting_right_able);
                    updateClickPart.setClickable(true);
                } else {
                    int lightGray = Color.parseColor("#848484");
                    updateSpace.setTextColor(lightGray);
                    timeSpace.setTextColor(lightGray);
                    updateSelectImg.setImageResource(R.drawable.setting_right_disable);
                    updateClickPart.setClickable(false);
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor.putInt("timePart", timePart);
        editor.putBoolean("notify", notify);
        editor.putBoolean("update", update);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        String timeSpaceText = timeSpace.getText().toString();
        int index = timeSpaceText.indexOf("小");
        timePart = Integer.valueOf(timeSpaceText.substring(0, index));
        if (update) {
            Intent intent = new Intent();
            intent.putExtra("timePart", timePart);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }
}
