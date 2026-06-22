package com.my.netindicator;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
public class FloatingService extends Service {
    private WindowManager wm;
    private TextView tv;
    private Handler handler;
    private Runnable runnable;
    private WindowManager.LayoutParams params;
    private int initialX,initialY;
    private float initialTouchX,initialTouchY;
    @Override
    public void onCreate(){
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        tv=new TextView(this);
        tv.setText("4G");
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setPadding(20,10,20,10);
        tv.setBackgroundColor(Color.RED);
        params=new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.END;
        params.x=10;
        params.y=150;
        wm.addView(tv,params);
        tv.setOnTouchListener((v,event)->{
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    initialX=params.x;
                    initialY=params.y;
                    initialTouchX=event.getRawX();
                    initialTouchY=event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x=initialX-(int)(event.getRawX()-initialTouchX);
                    params.y=initialY+(int)(event.getRawY()-initialTouchY);
                    wm.updateViewLayout(tv,params);
                    return true;
            }
            return false;
        });
        handler=new Handler();
        runnable=new Runnable(){
            public void run(){
                updateNetwork();
                handler.postDelayed(this,2000);
            }
        };
        handler.post(runnable);
    }
    private void updateNetwork(){
        try{
            TelephonyManager tm=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            int type=tm.getDataNetworkType();
            String network;
            int color;
            switch(type){
                case TelephonyManager.NETWORK_TYPE_NR:
                    network="5G";color=Color.parseColor("#00CC44");break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    network="4G";color=Color.parseColor("#FFD700");break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    network="3G";color=Color.parseColor("#FF8800");break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    network="2G";color=Color.parseColor("#E63329");break;
                default:network="?G";color=Color.WHITE;
            }
            tv.setText(network);
            tv.setBackgroundColor(color);
        }catch(Exception e){}
    }
    @Override
    public IBinder onBind(Intent intent){return null;}
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(tv!=null)wm.removeView(tv);
        if(handler!=null)handler.removeCallbacks(runnable);
    }
}
