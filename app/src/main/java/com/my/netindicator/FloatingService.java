package com.my.netindicator;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
public class FloatingService extends Service {
    private WindowManager wm;
    private TextView tv;
    private Handler handler;
    private Runnable runnable;
    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        tv = new TextView(this);
        tv.setText("4G");
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(12);
        tv.setPadding(16,8,16,8);
        tv.setBackgroundColor(Color.RED);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.END;
        p.x = 10; p.y = 150;
        wm.addView(tv, p);
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                try {
                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    int type = tm.getDataNetworkType();
                    if (type == TelephonyManager.NETWORK_TYPE_NR) {
                        tv.setText("5G");
                        tv.setBackgroundColor(Color.parseColor("#00AA00"));
                    } else {
                        tv.setText("4G");
                        tv.setBackgroundColor(Color.RED);
                    }
                } catch (Exception e) {}
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(runnable);
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tv != null) wm.removeView(tv);
        if (handler != null) handler.removeCallbacks(runnable);
    }
}
