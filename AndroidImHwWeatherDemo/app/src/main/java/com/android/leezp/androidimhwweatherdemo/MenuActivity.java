package com.android.leezp.androidimhwweatherdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Leezp on 2017/4/20 0020.
 */

public class MenuActivity extends Activity implements View.OnClickListener{

    private EditText cityWeather;
    private Button confirmBtn;
    private Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_menu);

        initView();

        initEvent();
    }

    /**
     * 初始化页面上的控件
     */
    private void initView() {
        cityWeather = (EditText) findViewById(R.id.activity_menu_cityWeather);
        confirmBtn = (Button) findViewById(R.id.activity_menu_confirmBtn);
        cancelBtn = (Button) findViewById(R.id.activity_menu_cancelBtn);
    }

    /**
     * 初始化对应控件的事件
     */
    private void initEvent() {
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    /**
     * 页面上存在的按钮，实现其点击事件
     * @param v     控件对象
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_menu_confirmBtn:
                String cityName = cityWeather.getText().toString();
                Intent intent = getIntent();
                intent.putExtra("cityName",cityName);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.activity_menu_cancelBtn:
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
        }
    }

    /**
     * 直接按返回键设置ResultCode为RESULT_CANCELED
     */
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

}
