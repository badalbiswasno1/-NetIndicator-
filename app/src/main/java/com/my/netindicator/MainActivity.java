package com.my.netindicator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.graphics.Color;
import android.view.View;

public class MainActivity extends Activity {
    private TextView tvNetworkType, tvPing, tvSignal, tvTime, tvHistory;
    private Handler handler = new Handler();
    private Runnable updater;
    private StringBuilder history = new StringBuilder();
    private String lastNetwork = "";
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#111111"));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor("#111111"));
        main.setPadding(40, 80, 40, 40);
        main.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(main);

        TextView title = new TextView(this);
        title.setText("TRUE NETWORK");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 10);
        main.addView(title);

        View line1 = new View(this);
        line1.setBackgroundColor(Color.parseColor("#FFD700"));
        line1.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 3));
        main.addView(line1);

        tvNetworkType = new TextView(this);
        tvNetworkType.setText("...");
        tvNetworkType.setTextSize(72);
        tvNetworkType.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNetworkType.setGravity(Gravity.CENTER);
        tvNetworkType.setPadding(0, 20, 0, 0);
        main.addView(tvNetworkType);

        tvPing = new TextView(this);
        tvPing.setText("Ping: -- ms");
        tvPing.setTextSize(20);
        tvPing.setTypeface(null, android.graphics.Typeface.BOLD);
        tvPing.setGravity(Gravity.CENTER);
        tvPing.setPadding(0, 0, 0, 20);
        main.addView(tvPing);

        View line2 = new View(this);
        line2.setBackgroundColor(Color.parseColor("#FFD700"));
        line2.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 3));
        main.addView(line2);

        tvSignal = new TextView(this);
        tvSignal.setText("অপারেটর: --");
        tvSignal.setTextColor(Color.parseColor("#00CC44"));
        tvSignal.setTextSize(16);
        tvSignal.setGravity(Gravity.CENTER);
        tvSignal.setPadding(0, 20, 0, 8);
        main.addView(tvSignal);

        tvTime = new TextView(this);
        tvTime.setText("চলছে: 0 সেকেন্ড");
        tvTime.setTextColor(Color.parseColor("#AAAAAA"));
        tvTime.setTextSize(13);
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setPadding(0, 0, 0, 20);
        main.addView(tvTime);

        View line3 = new View(this);
        line3.setBackgroundColor(Color.parseColor("#E63329"));
        line3.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 3));
        main.addView(line3);

        TextView histTitle = new TextView(this);
        histTitle.setText("নেটওয়ার্ক ইতিহাস");
        histTitle.setTextColor(Color.parseColor("#FFD700"));
        histTitle.setTextSize(15);
        histTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        histTitle.setPadding(0, 15, 0, 5);
        main.addView(histTitle);

        tvHistory = new TextView(this);
        tvHistory.setText("এখনো কোনো পরিবর্তন হয়নি");
        tvHistory.setTextColor(Color.parseColor("#CCCCCC"));
        tvHistory.setTextSize(13);
        tvHistory.setPadding(0, 5, 0, 0);
        main.addView(tvHistory);

        setContentView(scroll);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        } else {
            startService(new Intent(this, FloatingService.class));
        }

        updater = new Runnable() {
            public void run() {
                try { updateUI(); } catch (Exception e) {}
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(updater);
    }

    private void updateUI() {
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        int type = tm.getDataNetworkType();
        String network = getNetworkName(type);
        int color = getNetworkColor(type);
        tvNetworkType.setText(network);
        tvNetworkType.setTextColor(color);
        tvPing.setTextColor(color);

        if (!network.equals(lastNetwork)) {
            String time = new java.text.SimpleDateFormat("HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date());
            history.insert(0, time + " → " + network + "\n");
            lastNetwork = network;
            tvHistory.setText(history.toString());
        }

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        tvTime.setText("চলছে: " + (elapsed/60) + " মিনিট " + (elapsed%60) + " সেকেন্ড");
        tvSignal.setText("অপারেটর: " + tm.getNetworkOperatorName());

        // Ping in background
        new Thread(new Runnable() {
            public void run() {
                long ping = measurePing();
                final String pingText = ping >= 0 ? "Ping: " + ping + " ms" : "Ping: timeout";
                final int pingColor = ping < 0 ? Color.RED :
                    ping < 100 ? Color.parseColor("#00CC44") :
                    ping < 300 ? Color.parseColor("#FFD700") : Color.parseColor("#E63329");
                runOnUiThread(new Runnable() {
                    public void run() {
                        tvPing.setText(pingText);
                        tvPing.setTextColor(pingColor);
                    }
                });
            }
        }).start();
    }

    private long measurePing() {
        try {
            long start = System.currentTimeMillis();
            Process p = Runtime.getRuntime().exec("ping -c 1 -W 2 8.8.8.8");
            int result = p.waitFor();
            if (result == 0) return System.currentTimeMillis() - start;
            return -1;
        } catch (Exception e) { return -1; }
    }

    private String getNetworkName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_NR: return "5G";
            case TelephonyManager.NETWORK_TYPE_LTE: return "4G";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS: return "3G";
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS: return "2G";
            default: return "?G";
        }
    }

    private int getNetworkColor(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_NR: return Color.parseColor("#00CC44");
            case TelephonyManager.NETWORK_TYPE_LTE: return Color.parseColor("#FFD700");
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS: return Color.parseColor("#FF8800");
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS: return Color.parseColor("#E63329");
            default: return Color.WHITE;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updater);
    }
}
