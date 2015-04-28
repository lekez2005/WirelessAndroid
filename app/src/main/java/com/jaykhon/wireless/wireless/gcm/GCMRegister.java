package com.jaykhon.wireless.wireless.gcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.devices.alarm.AlarmFragment;
import com.jaykhon.wireless.wireless.utils.Json;
import com.jaykhon.wireless.wireless.utils.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by lekez2005 on 4/3/15.
 */
public class GCMRegister {
    private static final String TAG = "RegisterGCM";
    Context context;

    String SENDER_ID = "263165861177";
    GoogleCloudMessaging gcm;
    String regid;

    public GCMRegister(Context c){
        context = c;
    }

    public void register(){
        gcm = GoogleCloudMessaging.getInstance(context);
        registerInBackground();
//        regid = getRegistrationId(context);
//        if (regid.isEmpty()) {
//            registerInBackground();
//        }
    }

    public static String getRegistrationId(Context context){
        Preferences preferences = new Preferences(context);
        String registrationId = preferences.getGcmId();
        if (registrationId.isEmpty()){
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = preferences.getAppVersion();
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;

    }

    public void storeRegistrationId(String id){
        Preferences prefs = new Preferences(context);
        JSONObject obj = new JSONObject();
        String url = WirelessApp.getBaseUrl() + "user/update/gcm";
        try {
            obj.put(Json.IDENTIFIER, prefs.getUserId());
            obj.put("gcm_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            Log.d(TAG, SendRequest.postJsonToUrl(url, obj, null).toString());
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }


        prefs.setGcmId(id);
        prefs.setAppVersion(getAppVersion(context));
    }

    public void registerInBackground(){
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try{
                    if (gcm == null){
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    storeRegistrationId(regid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d(TAG, s);
            }
        }.execute();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
