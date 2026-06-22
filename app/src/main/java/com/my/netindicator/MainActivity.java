package com.my.netindicator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoGsm;
import android.telephony.TelephonyManager;
import android.widget.*;
import android.view.*;
import android.graphics.Color;
import java.util.List;
public class MainActivity extends Activity {
    private TextView tvNetworkType,tvPing,tvSignal,tvTime,tvHistory,tvDbm,tvData;
    private Handler handler=new Handler();
    private Runnable updater;
    private StringBuilder history=new StringBuilder();
    private String lastNetwork="";
    private long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        startTime=System.currentTimeMillis();
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#111111"));
        LinearLayout main=new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor("#111111"));
        main.setPadding(40,80,40,40);
        main.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(main);
        TextView title=new TextView(this);
        title.setText("TRUE NETWORK");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(null,android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0,0,0,10);
        main.addView(title);
        addLine(main,"#FFD700");
        tvNetworkType=new TextView(this);
        tvNetworkType.setText("...");
        tvNetworkType.setTextSize(72);
        tvNetworkType.setTypeface(null,android.graphics.Typeface.BOLD);
        tvNetworkType.setGravity(Gravity.CENTER);
        tvNetworkType.setPadding(0,20,0,0);
        main.addView(tvNetworkType);
        tvPing=new TextView(this);
        tvPing.setText("Ping: -- ms");
        tvPing.setTextSize(20);
        tvPing.setTypeface(null,android.graphics.Typeface.BOLD);
        tvPing.setGravity(Gravity.CENTER);
        tvPing.setPadding(0,0,0,5);
        main.addView(tvPing);
        tvDbm=new TextView(this);
        tvDbm.setText("Signal: -- dBm");
        tvDbm.setTextSize(16);
        tvDbm.setGravity(Gravity.CENTER);
        tvDbm.setTextColor(Color.parseColor("#AAAAAA"));
        tvDbm.setPadding(0,0,0,20);
        main.addView(tvDbm);
        addLine(main,"#FFD700");
        tvSignal=new TextView(this);
        tvSignal.setText("অপারেটর: --");
        tvSignal.setTextColor(Color.parseColor("#00CC44"));
        tvSignal.setTextSize(16);
        tvSignal.setGravity(Gravity.CENTER);
        tvSignal.setPadding(0,20,0,8);
        main.addView(tvSignal);
        tvTime=new TextView(this);
        tvTime.setText("চলছে: 0 সেকেন্ড");
        tvTime.setTextColor(Color.parseColor("#AAAAAA"));
        tvTime.setTextSize(13);
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setPadding(0,0,0,10);
        main.addView(tvTime);
        tvData=new TextView(this);
        tvData.setText("Data: -- MB");
        tvData.setTextColor(Color.parseColor("#FFD700"));
        tvData.setTextSize(14);
        tvData.setGravity(Gravity.CENTER);
        tvData.setPadding(0,0,0,20);
        main.addView(tvData);
        addLine(main,"#E63329");
        TextView histTitle=new TextView(this);
        histTitle.setText("নেটওয়ার্ক ইতিহাস");
        histTitle.setTextColor(Color.parseColor("#FFD700"));
        histTitle.setTextSize(15);
        histTitle.setTypeface(null,android.graphics.Typeface.BOLD);
        histTitle.setPadding(0,15,0,5);
        main.addView(histTitle);
        tvHistory=new TextView(this);
        tvHistory.setText("এখনো কোনো পরিবর্তন হয়নি");
        tvHistory.setTextColor(Color.parseColor("#CCCCCC"));
        tvHistory.setTextSize(13);
        tvHistory.setPadding(0,5,0,20);
        main.addView(tvHistory);
        addLine(main,"#E63329");
        TextView pingTitle=new TextView(this);
        pingTitle.setText("পিং রেকর্ড");
        pingTitle.setTextColor(Color.parseColor("#FFD700"));
        pingTitle.setTextSize(15);
        pingTitle.setTypeface(null,android.graphics.Typeface.BOLD);
        pingTitle.setPadding(0,15,0,20);
        main.addView(pingTitle);
        addLine(main,"#333333");
        Button settingsBtn=new Button(this);
        settingsBtn.setText("⚙ সেটিংস");
        settingsBtn.setBackgroundColor(Color.parseColor("#333333"));
        settingsBtn.setTextColor(Color.WHITE);
        settingsBtn.setTextSize(14);
        settingsBtn.setPadding(0,20,0,20);
        LinearLayout.LayoutParams btnParams=new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0,20,0,0);
        settingsBtn.setLayoutParams(btnParams);
        settingsBtn.setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class)));
        main.addView(settingsBtn);
        setContentView(scroll);
        if(!Settings.canDrawOverlays(this)){
            Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:"+getPackageName()));
            startActivityForResult(intent,0);
        }else{
            startService(new Intent(this,FloatingService.class));
        }
        updater=new Runnable(){
            public void run(){
                try{updateUI();}catch(Exception e){}
                handler.postDelayed(this,2000);
            }
        };
        handler.post(updater);
    }
    private void addLine(LinearLayout parent,String color){
        View line=new View(this);
        line.setBackgroundColor(Color.parseColor(color));
        line.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,3));
        parent.addView(line);
    }
    private void updateUI(){
        TelephonyManager tm=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        int type=tm.getDataNetworkType();
        String network=getNetworkName(type);
        int color=getNetworkColor(type);
        tvNetworkType.setText(network);
        tvNetworkType.setTextColor(color);
        tvPing.setTextColor(color);
        if(!network.equals(lastNetwork)){
            String time=new java.text.SimpleDateFormat("HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date());
            history.insert(0,time+" -> "+network+"\n");
            lastNetwork=network;
            tvHistory.setText(history.toString());
        }
        long elapsed=(System.currentTimeMillis()-startTime)/1000;
        tvTime.setText("চলছে: "+(elapsed/60)+" মিনিট "+(elapsed%60)+" সেকেন্ড");
        tvSignal.setText("অপারেটর: "+tm.getNetworkOperatorName());
        try{
            List<CellInfo> cells=tm.getAllCellInfo();
            if(cells!=null&&!cells.isEmpty()){
                for(CellInfo cell:cells){
                    if(cell.isRegistered()){
                        int dbm=0;
                        if(cell instanceof CellInfoLte)
                            dbm=((CellInfoLte)cell).getCellSignalStrength().getDbm();
                        else if(cell instanceof CellInfoWcdma)
                            dbm=((CellInfoWcdma)cell).getCellSignalStrength().getDbm();
                        else if(cell instanceof CellInfoGsm)
                            dbm=((CellInfoGsm)cell).getCellSignalStrength().getDbm();
                        String sigLevel=dbm>-80?"ভালো":dbm>-100?"মাঝারি":"দুর্বল";
                        tvDbm.setText("Signal: "+dbm+" dBm ("+sigLevel+")");
                        break;
                    }
                }
            }
        }catch(Exception e){}
        try{
            long rx=android.net.TrafficStats.getMobileRxBytes();
            long tx=android.net.TrafficStats.getMobileTxBytes();
            long total=(rx+tx)/(1024*1024);
            tvData.setText("Data ব্যবহার: "+total+" MB");
        }catch(Exception e){}
        new Thread(new Runnable(){
            public void run(){
                final long ping=measurePing();
                final String pingText=ping>=0?"Ping: "+ping+" ms":"Ping: timeout";
                final int pingColor=ping<0?Color.RED:
                    ping<100?Color.parseColor("#00CC44"):
                    ping<300?Color.parseColor("#FFD700"):Color.parseColor("#E63329");
                runOnUiThread(new Runnable(){
                    public void run(){
                        tvPing.setText(pingText);
                        tvPing.setTextColor(pingColor);
                    }
                });
            }
        }).start();
    }
    private long measurePing(){
        try{
            long start=System.currentTimeMillis();
            Process p=Runtime.getRuntime().exec("ping -c 1 -W 2 8.8.8.8");
            int result=p.waitFor();
            if(result==0)return System.currentTimeMillis()-start;
            return -1;
        }catch(Exception e){return -1;}
    }
    private String getNetworkName(int type){
        switch(type){
            case TelephonyManager.NETWORK_TYPE_NR:return "5G";
            case TelephonyManager.NETWORK_TYPE_LTE:return "4G";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:return "3G";
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:return "2G";
            default:return "?G";
        }
    }
    private int getNetworkColor(int type){
        switch(type){
            case TelephonyManager.NETWORK_TYPE_NR:return Color.parseColor("#00CC44");
            case TelephonyManager.NETWORK_TYPE_LTE:return Color.parseColor("#FFD700");
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:return Color.parseColor("#FF8800");
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:return Color.parseColor("#E63329");
            default:return Color.WHITE;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(updater);
    }
}
