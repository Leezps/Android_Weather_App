package com.android.leezp.androidimhwweatherdemo;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.leezp.androidimhwweatherdemo.Http.HttpCallbackListener;
import com.android.leezp.androidimhwweatherdemo.Http.HttpWeather;
import com.android.leezp.androidimhwweatherdemo.Service.TimeUpdateService;
import com.android.leezp.androidimhwweatherdemo.View.CircleView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{
    //百度提供的定位接口的类
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    //地图需要的权限的请求码
    private static final int REQUEST_CODE_LOCATION_PERMISSIONS = 10001;

    //请求天气的接口API___url
    private String weatherUrl = "http://wthrcdn.etouch.cn/weather_mini?city=";
    //Http天气的类
    private HttpWeather httpWeather = new HttpWeather();

    //定位的地址
    private String location;
    //当前风的级别
    private String windScale;
    //当前温度
    private int nowTemp;
    //最低温度
    private int []lowTemp = new int [5];
    //最高温度
    private int []highTemp = new int[5];
    //天气种类
    private String []weatherType = new String[5];
    //日期
    private String []dates = new String[5];
    //友情提示
    private String notifyContent;

    //中间天气的自定义布局
    private CircleView circleWeather;
    //设置按钮
    private ImageButton setBtn;
    //菜单按钮
    private ImageButton menuBtn;
    //中间城市的显示
    private TextView city;
    //错误信息的显示
    private TextView errorInfo;
    //当前日期星期
    private TextView week;
    //当前天气
    private TextView nowWeather;
    //未来一、二、三、四天的天气种类、温度、星期
    private ImageView one_weather;
    private ImageView  two_weather;
    private ImageView  three_weather;
    private ImageView  four_weather;
    private TextView  one_temp;
    private TextView  two_temp;
    private TextView  three_temp;
    private TextView  four_temp;
    private TextView  one_week;
    private TextView  two_week;
    private TextView  three_week;
    private TextView  four_week;

    //广播接收器，用于接收广播的数据，并进行相应的处理
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    //通知显示天气
    private NotificationManager notificationManager;
    private Notification notification;
    //通知的id
    private static final int notifyId = 1001;

    //进入MenuActivity的请求码
    private static final int menuRequestCode = 10002;
    //进入SetActivity的请求码
    private static final int setRequestCode = 10003;

    //SharedPreferences存储天气数据
    private static final String prefName = "main_data";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //初始化界面以及响应事件
        initView();
        initEvent();
        //获取地址
        List<String> locationPermissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            locationPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
            locationPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            locationPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!locationPermissions.isEmpty()) {
            String [] permissions = locationPermissions.toArray(new String[locationPermissions.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE_LOCATION_PERMISSIONS);
        } else {
            requestLocation();
        }
    }

    /**
     * 初始化界面上按钮的事件
     */
    private void initEvent() {
        setBtn.setOnClickListener(this);
        menuBtn.setOnClickListener(this);

        //实例化SharedPreferences用于存储数据
        preferences = getSharedPreferences(prefName, MODE_PRIVATE);
        editor = preferences.edit();
        showLocalData();

        //本地广播  初始化本地广播相关事件
        //1.打开通知广播
        intentFilter = new IntentFilter();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter.addAction("com.android.leezp.androidimhwweatherdemo.openNotify");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        //关闭通知广播
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.leezp.androidimhwweatherdemo.closeNotify");
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        //后台服务数据获取完成
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.leezp.androidimhwweatherdemo.weatherSuccess");
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        //后台服务数据获取失败
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.leezp.androidimhwweatherdemo.weatherFail");
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

        //初始化通知的管理
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * 显示本地存储的天气状况
     */
    private void showLocalData() {
        location = preferences.getString("location", "");
        if (location.equals("")) {
            location = "成都";
            return;
        } else {
            windScale = preferences.getString("windScale", "");
            nowTemp = preferences.getInt("nowTemp", 0);
            lowTemp[0] = preferences.getInt("lowTemp_0", 0);
            lowTemp[1] = preferences.getInt("lowTemp_1", 0);
            lowTemp[2] = preferences.getInt("lowTemp_2", 0);
            lowTemp[3] = preferences.getInt("lowTemp_3", 0);
            lowTemp[4] = preferences.getInt("lowTemp_4", 0);
            highTemp[0] = preferences.getInt("highTemp_0", 0);
            highTemp[1] = preferences.getInt("highTemp_1", 0);
            highTemp[2] = preferences.getInt("highTemp_2", 0);
            highTemp[3] = preferences.getInt("highTemp_3", 0);
            highTemp[4] = preferences.getInt("highTemp_4", 0);
            weatherType[0] = preferences.getString("weatherType_0", "");
            weatherType[1] = preferences.getString("weatherType_1", "");
            weatherType[2] = preferences.getString("weatherType_2", "");
            weatherType[3] = preferences.getString("weatherType_3", "");
            weatherType[4] = preferences.getString("weatherType_4", "");
            dates[0] = preferences.getString("dates_0", "");
            dates[1] = preferences.getString("dates_1", "");
            dates[2] = preferences.getString("dates_2", "");
            dates[3] = preferences.getString("dates_3", "");
            dates[4] = preferences.getString("dates_4", "");
            notifyContent = preferences.getString("notifyContent", "");
            interUpdate(location);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        errorInfo.setVisibility(View.INVISIBLE);
    }

    /**
     * 初始化界面上的控件
     */
    private void initView() {
        circleWeather  = (CircleView) findViewById(R.id.activity_main_weather);
        setBtn = (ImageButton) findViewById(R.id.activity_main_setting);
        city = (TextView) findViewById(R.id.activity_main_place);
        menuBtn = (ImageButton) findViewById(R.id.activity_main_more);
        errorInfo = (TextView) findViewById(R.id.activity_main_errorInfo);
        week = (TextView) findViewById(R.id.activity_main_week);
        nowWeather = (TextView) findViewById(R.id.activity_main_nowWeather);
        one_weather = (ImageView) findViewById(R.id.activity_main_future_one_weather);
        two_weather = (ImageView) findViewById(R.id.activity_main_future_two_weather);
        three_weather = (ImageView) findViewById(R.id.activity_main_future_three_weather);
        four_weather = (ImageView) findViewById(R.id.activity_main_future_four_weather);
        one_temp = (TextView) findViewById(R.id.activity_main_future_one_temp);
        two_temp = (TextView) findViewById(R.id.activity_main_future_two_temp);
        three_temp = (TextView) findViewById(R.id.activity_main_future_three_temp);
        four_temp = (TextView) findViewById(R.id.activity_main_future_four_temp);
        one_week = (TextView) findViewById(R.id.activity_main_future_one_week);
        two_week = (TextView) findViewById(R.id.activity_main_future_two_week);
        three_week = (TextView) findViewById(R.id.activity_main_future_three_week);
        four_week = (TextView) findViewById(R.id.activity_main_future_four_week);
    }

    /**
     * 调用百度定位手机地址
     */
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    /**
     * function: 设置从百度获取地址的选项
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //设置为以手机条件为主，如果打开了GPS，就GPS定位，如果只打开的网络，那就网络定位
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //设置为可以获取地址信息
        option.setIsNeedAddress(true);
        //将选择设置到获取地址的对象中
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSIONS:
                if (grantResults.length > 0) {
                    for (int result:grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * function     继承点击事件的接口
     * @param v     点击事件的view控件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_setting:
                Intent intent_set = new Intent(this, SetActivity.class);
                startActivityForResult(intent_set, setRequestCode);
                break;
            case R.id.activity_main_more:
                Intent intent_menu = new Intent(this, MenuActivity.class);
                startActivityForResult(intent_menu, menuRequestCode);
                break;
            default:
        }
    }

    /**
     *function       百度地图的监听事件
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //先将请求天气的URL字符串拼接好，按照utf-8编码将中文拼接到url后面，好后面直接使用
            StringBuilder url = new StringBuilder(weatherUrl);
            try {
                location = bdLocation.getCity();
                if (location == null||location.equals("")) {
                    location = "成都";
                }
                url.append(URLEncoder.encode(location,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                String error = new String("城市名转码出现问题");
                errorShow(error);
            }
            //调用请求天气的方法
            httpWeather.sendHttpRequest(url.toString(), new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {
                    mLocationClient.stop();
                    resolveJson(response);
                    rememberWeather();
                }

                @Override
                public void onError(Exception e) {
                    String error = new String("没有网络");
                    errorShow(error);
                }
            });

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) { }
    }

    /**
     * 用来存储从网上获取的数据，当没网时显示本地存储的数据
     */
    private void rememberWeather() {
        editor.putString("location", location);
        editor.putString("windScale", windScale);
        editor.putInt("nowTemp", nowTemp);
        editor.putInt("lowTemp_0", lowTemp[0]);
        editor.putInt("lowTemp_1", lowTemp[1]);
        editor.putInt("lowTemp_2", lowTemp[2]);
        editor.putInt("lowTemp_3", lowTemp[3]);
        editor.putInt("lowTemp_4", lowTemp[4]);
        editor.putInt("highTemp_0", highTemp[0]);
        editor.putInt("highTemp_1", highTemp[1]);
        editor.putInt("highTemp_2", highTemp[2]);
        editor.putInt("highTemp_3", highTemp[3]);
        editor.putInt("highTemp_4", highTemp[4]);
        editor.putString("weatherType_0", weatherType[0]);
        editor.putString("weatherType_1", weatherType[1]);
        editor.putString("weatherType_2", weatherType[2]);
        editor.putString("weatherType_3", weatherType[3]);
        editor.putString("weatherType_4", weatherType[4]);
        editor.putString("dates_0", dates[0]);
        editor.putString("dates_1", dates[1]);
        editor.putString("dates_2", dates[2]);
        editor.putString("dates_3", dates[3]);
        editor.putString("dates_4", dates[4]);
        editor.putString("notifyContent", notifyContent);
        editor.apply();
    }

    /**
     * function:    解析JSON字符串
     * @param response  传过来的json字符串
     */
    private void resolveJson(String response) {
        try {
            JSONObject weatherJson = new JSONObject(response);
            int status = weatherJson.getInt("status");
            //当状态为1002时，这说明没有该城市的天气
            if (status!=1000) {
                String error = new String("没有该城市的天气");
                errorShow(error);
                return;
            }
            JSONObject weatherData = weatherJson.getJSONObject("data");
            nowTemp = weatherData.getInt("wendu");
            notifyContent = weatherData.getString("ganmao");
            String cityName = weatherData.getString("city");
            JSONArray weathersJson = weatherData.getJSONArray("forecast");
            for (int i=0;i<weathersJson.length();++i) {
                JSONObject json = weathersJson.getJSONObject(i);
                if (i==0) {
                    windScale = json.getString("fengli");
                }
                String low = json.getString("low");
                low = low.substring(low.indexOf(" ")+1,low.lastIndexOf("℃"));
                String high = json.getString("high");
                high = high.substring(high.indexOf(" ")+1,high.lastIndexOf("℃"));
                String type = json.getString("type");
                String date = json.getString("date");
                date = "周"+date.substring(date.length()-1);
                lowTemp[i] = Integer.valueOf(low);
                highTemp[i] = Integer.valueOf(high);
                weatherType[i] = type;
                dates[i] = date;
            }
            interUpdate(cityName);
        } catch (JSONException e) {
            String error = new String("Json数据解析有误");
            errorShow(error);
        }
    }

    /**
     * function:    错误信息的显示
     * @param error     错误信息
     */
    private void errorShow(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorInfo.getVisibility() != View.VISIBLE) {
                    errorInfo.setVisibility(View.VISIBLE);
                    errorInfo.setText(error);
                }
            }
        });
    }

    /**
     *function:      获取了数据，更新页面
     * @param cityName      //城市名
     */
    private void interUpdate(final String cityName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                city.setText(cityName);
                circleWeather.setAngle(lowTemp[0],highTemp[0]);
                circleWeather.setLowHighTem(lowTemp[0],highTemp[0]);
                circleWeather.setNowTem(nowTemp);
                circleWeather.setWeaBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), typeRetImage(weatherType[0])));
                week.setText(dates[0]);
                nowWeather.setText(weatherType[0]+"|"+windScale);
                one_weather.setImageResource(typeRetImage(weatherType[1]));
                one_temp.setText(lowTemp[1]+"º/"+highTemp[1]+"º");
                one_week.setText(dates[1]);
                two_weather.setImageResource(typeRetImage(weatherType[2]));
                two_temp.setText(lowTemp[2]+"º/"+highTemp[2]+"º");
                two_week.setText(dates[2]);
                three_weather.setImageResource(typeRetImage(weatherType[3]));
                three_temp.setText(lowTemp[3]+"º/"+highTemp[3]+"º");
                three_week.setText(dates[3]);
                four_weather.setImageResource(typeRetImage(weatherType[4]));
                four_temp.setText(lowTemp[4]+"º/"+highTemp[4]+"º");
                four_week.setText(dates[4]);
            }
        });
    }

    /**
     * function     根据天气返回应该用的图片
     * @param type      //天气种类
     * @return          //返回图片
     */
    private int typeRetImage(String type) {
        if (type.equals("多云")) {
            return R.drawable.weather_cloudy;
        } else if (type.equals("大雨")) {
            return R.drawable.weather_heavy_rain;
        } else if (type.equals("小雨")) {
            return R.drawable.weather_light_rain;
        } else if (type.equals("小雨转中雨")) {
            return R.drawable.weather_ltom_rain;
        } else if (type.equals("中雨")) {
            return R.drawable.weather_mid_rain;
        } else if (type.equals("雪")) {
            return R.drawable.weather_snow;
        } else if (type.equals("阵雨")) {
            return R.drawable.weather_shower;
        } else if (type.equals("晴")) {
            return R.drawable.weather_sunny;
        } else if (type.equals("雷阵雨")) {
            return R.drawable.weather_thunder_shower;
        } else if (type.equals("阴")) {
            return R.drawable.weather_yin;
        } else {
            return R.drawable.weather_snow;
        }
    }

    /**
     * 继承广播接收器的类，并实现其中的方法，收到相应的的信息进行相应
     */
    private class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("com.android.leezp.androidimhwweatherdemo.openNotify")) {
                //点击通知进入APP主页面
                Intent intent_main = new Intent(MainActivity.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent_main,0);
                //当广播的是打开通知
                notification = new NotificationCompat.Builder(getApplicationContext())
                               .setContentTitle(nowTemp+"℃"+"   "+weatherType[0])
                               .setWhen(System.currentTimeMillis())
                               .setSmallIcon(R.mipmap.weather_label)
                               .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.weather_label))
                               .setContentIntent(pendingIntent)
                               .setStyle(new NotificationCompat.BigTextStyle().bigText(notifyContent))
                               .build();
                notificationManager.notify(notifyId, notification);
            } else if(action.equals("com.android.leezp.androidimhwweatherdemo.closeNotify")) {
                //当广播的是关闭通知
                notificationManager.cancel(notifyId);
            } else if (action.equals("com.android.leezp.androidimhwweatherdemo.weatherSuccess")) {
                resolveJson(TimeUpdateService.urlContent);
            } else if (action.equals("com.android.leezp.androidimhwweatherdemo.weatherFail")) {
                String error = new String("没有网络");
                errorShow(error);
            }
        }
    }

    /**
     * Activity的返回数据并根据情况在MainActivity中显示
     * @param requestCode       请求码
     * @param resultCode        返回码
     * @param data          返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //MenuActivity中返回数据进行显示
            case menuRequestCode:
                if (resultCode == RESULT_OK) {
                    String cityName = data.getStringExtra("cityName");
                    StringBuilder url = new StringBuilder(weatherUrl);
                    try {
                        url.append(URLEncoder.encode(cityName,"utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        String error = new String("城市名转码出现问题");
                        errorShow(error);
                    }
                    //调用请求天气的方法
                    httpWeather.sendHttpRequest(url.toString(), new HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {
                            resolveJson(response);
                        }

                        @Override
                        public void onError(Exception e) {
                            String error = new String("没有网络");
                            errorShow(error);
                        }
                    });
                }
                break;
            //SetActivity中返回数据进行显示
            case setRequestCode:
                Intent intent_service = new Intent(this, TimeUpdateService.class);
                if (resultCode == RESULT_OK) {
                    int time = data.getIntExtra("timePart", 0);
                    Log.e("MainActivity", ""+time);
                    intent_service.putExtra("time", time);
                    StringBuilder url = new StringBuilder(weatherUrl);
                    try {
                        url.append(URLEncoder.encode(location,"utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        String error = new String("城市名转码出现问题");
                        errorShow(error);
                    }
                    intent_service.putExtra("weatherUrl", url.toString());
                    startService(intent_service);
                } else {
                    intent_service.putExtra("time", 0);
                    startService(intent_service);
                }
                break;
            default:
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Activity销毁之后，广播接收器也要销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消本地广播注册
        localBroadcastManager.unregisterReceiver(localReceiver);
        //注销掉服务
        Intent intent_service = new Intent(this, TimeUpdateService.class);
        stopService(intent_service);
    }
}