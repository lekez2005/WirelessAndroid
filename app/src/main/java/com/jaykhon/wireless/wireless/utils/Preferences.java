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
    private static final String USER_ID = "id";
    private static final String USER_TOKEN = "token";

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

    public void setUserId(String id){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID, id);
        editor.commit();
    }

    public String getUserId(){
        return sharedPreferences.getString(USER_ID, "");
    }

    public void setUserToken(String token){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_TOKEN, token);
        editor.commit();
    }

    public String getUserToken(){
        return sharedPreferences.getString(USER_TOKEN, "");
    }

    public String getBaseUrl(){
        String url = String.format("https://%s:%s/",
                getServerIp(), getServerPort());
        return url;
    }

}
