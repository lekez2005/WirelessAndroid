package com.jaykhon.wireless.wireless.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by lekez2005 on 3/19/15.
 */
public class Json {
    public static String[] getIdentifiersFromJson(int parentPosition, int childPosition, JSONObject devices){
        String deviceType = null;
        Iterator<String> it = devices.keys();
        for (int i = 0; i < devices.length(); i++){
            if (i == parentPosition){
                deviceType = it.next();
            }else{
                it.next();
            }
        }
        String childIdentifier = null;
        try {
            JSONArray childObjects = devices.getJSONArray(deviceType);
            JSONObject childObject = childObjects.getJSONObject(childPosition);
            childIdentifier = childObject.getString("identifier");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[]{deviceType, childIdentifier};
    }
}
