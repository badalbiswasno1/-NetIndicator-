package com.my.netindicator;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class NetworkLogger {
    private static final String PREF_NAME="network_log";
    private static final String KEY_LOG="log";
    private SharedPreferences prefs;
    public NetworkLogger(Context ctx){
        prefs=ctx.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
    }
    public void log(String network,long ping,long dataKB){
        try{
            JSONArray arr=getLogs();
            JSONObject obj=new JSONObject();
            obj.put("time",new SimpleDateFormat("HH:mm:ss dd/MM",Locale.getDefault()).format(new Date()));
            obj.put("network",network);
            obj.put("ping",ping);
            obj.put("data",dataKB);
            arr.put(obj);
            if(arr.length()>500){
                JSONArray newArr=new JSONArray();
                for(int i=arr.length()-500;i<arr.length();i++)
                    newArr.put(arr.get(i));
                arr=newArr;
            }
            prefs.edit().putString(KEY_LOG,arr.toString()).apply();
        }catch(Exception e){}
    }
    public JSONArray getLogs(){
        try{
            String s=prefs.getString(KEY_LOG,"[]");
            return new JSONArray(s);
        }catch(Exception e){return new JSONArray();}
    }
    public void clear(){
        prefs.edit().remove(KEY_LOG).apply();
    }
}
