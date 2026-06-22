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
import org.json.JSONArray;
import org.json.JSONObject;
public class MainActivity extends Activity {
    private TextView tvNetworkType,tvPing,tvSignal,tvTime,tvDbm,tvData,tvHistory;
    private Handler handler=new Handler();
    private Runnable updater;
    private String lastNetwork="";
    private long startTime;
    private NetworkLogger logger;
    private long lastPing=0;
    private SeekBar timeSeek;
    private TextView tvTimeLabel;
    private int[] timeOptions={15,30,60,1440};
    private String[] timeLabels={"১৫ মিনিট","৩০ মিনিট","১ ঘন্টা","সারাদিন"};
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        startTime=System.currentTimeMillis();
        logger=new NetworkLogger(this);
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
        tvTimeLabel=new TextView(this);
        tvTimeLabel.setText("সময়: ১৫ মিনিট");
        tvTimeLabel.setTextColor(Color.parseColor("#AAAAAA"));
        tvTimeLabel.setTextSize(13);
        tvTimeLabel.setGravity(Gravity.CENTER);
        main.addView(tvTimeLabel);
        timeSeek=new SeekBar(this);
        timeSeek.setMax(3);
        timeSeek.setProgress(0);
        timeSeek.setPadding(0,10,0,10);
        timeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar sb,int p,boolean u){
                tvTimeLabel.setText("সময়: "+timeLabels[p]);
                updateHistory(p);
            }
            public void onStartTrackingTouch(SeekBar sb){}
            public void onStopTrackingTouch(SeekBar sb){}
        });
        main.addView(timeSeek);
        tvHistory=new TextView(this);
        tvHistory.setTextColor(Color.parseColor("#CCCCCC"));
        tvHistory.setTextSize(11);
        tvHistory.setPadding(0,5,0,20);
        tvHistory.setTypeface(android.graphics.Typeface.MONOSPACE);
        main.addView(tvHistory);
        Button clearBtn=new Button(this);
        clearBtn.setText("ইতিহাস মুছো");
        clearBtn.setBackgroundColor(Color.parseColor("#E63329"));
        clearBtn.setTextColor(Color.WHITE);
        clearBtn.setTextSize(12);
        clearBtn.setOnClickListener(v->{
            logger.clear();
            tvHistory.setText("ইতিহাস মুছে গেছে");
        });
        main.addView(clearBtn);
        addLine(main,"#333333");
        Button settingsBtn=new Button(this);
        settingsBtn.setText("⚙ সেটিংস");
        settingsBtn.setBackgroundColor(Color.parseColor("#333333"));
        settingsBtn.setTextColor(Color.WHITE);
        settingsBtn.setTextSize(14);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0,20,0,0);
        settingsBtn.setLayoutParams(bp);
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
        updateHistory(0);
    }
    private void addLine(LinearLayout parent,String color){
        View line=new View(this);
        line.setBackgroundColor(Color.parseColor(color));
        line.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,3));
        parent.addView(line);
    }
    private void updateHistory(int timeIndex){
        try{
            JSONArray logs=logger.getLogs();
            long cutoff=System.currentTimeMillis()-((long)timeOptions[timeIndex]*60*1000);
            StringBuilder sb=new StringBuilder();
            sb.append(String.format("%-8s %-4s %-6s %-8s\n","সময়","Net","Ping","Data"));
            sb.append("--------------------------------\n");
            long totalData=0;
            long minPing=Long.MAX_VALUE,maxPing=0;
            int count=0;
            for(int i=logs.length()-1;i>=0;i--){
                JSONObject obj=logs.getJSONObject(i);
                long ping=obj.getLong("ping");
                long data=obj.getLong("data");
                totalData+=data;
                if(ping>0){
                    if(ping<minPing)minPing=ping;
                    if(ping>maxPing)maxPing=ping;
                }
                count++;
                if(count<=20){
                    sb.append(String.format("%-8s %-4s %-6s %-8s\n",
                        obj.getString("time").substring(0,8),
                        obj.getString("network"),
                        ping>0?ping+"ms":"--",
                        data+"KB"));
                }
            }
            if(count>0){
                sb.append("--------------------------------\n");
                sb.append("মোট: "+count+" রেকর্ড\n");
                sb.append("Data: "+(totalData/1024)+" MB\n");
                if(minPing<Long.MAX_VALUE)
                    sb.append("Best: "+minPing+"ms | Worst: "+maxPing+"ms");
            }else{
                sb.append("এখনো কোনো ডেটা নেই");
            }
            tvHistory.setText(sb.toString());
        }catch(Exception e){tvHistory.setText("ইতিহাস লোড হয়নি");}
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
            lastNetwork=network;
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
            long totalKB=(rx+tx)/1024;
            String dataText=totalKB>1024?"App চলার পর: "+(totalKB/1024)+" MB":"App চলার পর: "+totalKB+" KB";
            tvData.setText(dataText);
        }catch(Exception e){}
        new Thread(()->{
            final long ping=measurePing();
            lastPing=ping;
            try{
                long rx=android.net.TrafficStats.getMobileRxBytes();
                long tx=android.net.TrafficStats.getMobileTxBytes();
                long dataKB=(rx+tx)/1024;
                logger.log(network,ping,dataKB);
            }catch(Exception e){}
            final String pingText=ping>=0?"Ping: "+ping+" ms":"Ping: timeout";
            final int pingColor=ping<0?Color.RED:
                ping<100?Color.parseColor("#00CC44"):
                ping<300?Color.parseColor("#FFD700"):Color.parseColor("#E63329");
            runOnUiThread(()->{
                tvPing.setText(pingText);
                tvPing.setTextColor(pingColor);
                updateHistory(timeSeek.getProgress());
            });
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
