package com.android.leezp.androidimhwweatherdemo.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.leezp.androidimhwweatherdemo.Http.HttpCallbackListener;
import com.android.leezp.androidimhwweatherdemo.Http.HttpWeather;


/**
 * Created by Leezp on 2017/5/2 0002.
 */

public class TimeUpdateService extends Service{
    private int time = 0;
    public static String urlContent;
    private String weatherUrl;
    //当定时获取数据获取完成时广播
    private LocalBroadcastManager localBroadcastManager;


    @Override
    public void onCreate() {
        super.onCreate();
        //获取广播管理的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        time = intent.getIntExtra("time", 0);
        Log.e("ServiceTag", ""+time);
        if (time != 0) {
            weatherUrl = intent.getStringExtra("weatherUrl");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new HttpWeather().sendHttpRequest(weatherUrl, new HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {
                            urlContent = response;
                            Intent intent_openNotify = new Intent("com.android.leezp.androidimhwweatherdemo.weatherSuccess");
                            //发送开启通知本地广播
                            localBroadcastManager.sendBroadcast(intent_openNotify);
                        }

                        @Override
                        public void onError(Exception e) {
                            urlContent = null;
                            Intent intent_openNotify = new Intent("com.android.leezp.androidimhwweatherdemo.weatherFail");
                            //发送开启通知本地广播
                            localBroadcastManager.sendBroadcast(intent_openNotify);
                        }
                    });
                }
            }).start();
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            int updateTime = time*60*60*1000;
            long updateDate = System.currentTimeMillis()+updateTime;
            Intent in = new Intent(this, TimeUpdateService.class);
            PendingIntent pi = PendingIntent.getService(this, 0, in, 0);
            manager.set(AlarmManager.RTC_WAKEUP, updateDate, pi);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}