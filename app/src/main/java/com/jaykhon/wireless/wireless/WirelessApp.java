package com.jaykhon.wireless.wireless;

import android.app.Application;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Created by lekez2005 on 3/19/15.
 */
public class WirelessApp extends Application {

    private static WirelessApp sInstance;
    private static Preferences mPreferences;
    private static boolean lastConnection = false;

    private static JSONObject mDevices;

    private String IP_address;
    private String PORT;

    public static WirelessApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mPreferences = new Preferences(this);
        SendRequest.mContext = getApplicationContext();
    }

    public static void setIp(String ip) {
        mPreferences.setServerIp(ip);
    }

    public static void setPort(String port) {
        mPreferences.setServerPort(port);
    }

    public static boolean lastConnectionSuccessful(){
        return lastConnection;
    }

    public static void setLastConnectionSuccess(boolean res){
        lastConnection = res;
    }

    public static boolean networkConnected(){
        return true; //TODO check network connection
    }

    public static JSONObject getDevices(){
        return mDevices;
    }

    public static void setmDevices(JSONObject obj){
        mDevices = obj;
    }



    public static boolean pingServer() {
        return false;
    }

    public static String getBaseUrl() {
        String url = String.format("https://%s:%s/",
                mPreferences.getServerIp(), mPreferences.getServerPort());
        return url;
    }

    public static Preferences getPreferences() {
        return sInstance.mPreferences;
    }

    public static void setPreferences(Preferences preferences) {
        sInstance.mPreferences = preferences;
    }
}
