package com.jaykhon.wireless.wireless.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by lekez2005 on 3/19/15.
 */
public class Preferences {
    private static final String SERVER_PORT = "port";
    private static final String SERVER_IP = "ip";

    private SharedPreferences sharedPreferences;

    public Preferences(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getServerIp(){
        return sharedPreferences.getString(SERVER_IP, "");
    }

    public void setServerIp(String ip){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SERVER_IP, ip);
        editor.commit();
    }

    public String getServerPort(){
        return sharedPreferences.getString(SERVER_PORT, "");
    }

    public void setServerPort(String port){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SERVER_PORT, port);
        editor.commit();
    }

    public String getBaseUrl(){
        String url = String.format("https://%s:%s/",
                getServerIp(), getServerPort());
        return url;
    }

}
