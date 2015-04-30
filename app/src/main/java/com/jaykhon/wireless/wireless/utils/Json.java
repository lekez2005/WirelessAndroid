package com.jaykhon.wireless.wireless.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by lekez2005 on 3/19/15.
 */
public class Json {

    public static final String IDENTIFIER = "identifier";
    public static final String PRETTY_NAME = "pretty_name";
    public static final String ADDRESS = "address";
    public static final String ACTIVE = "active";
    public static final String DESCRIPTION = "description";
    public static final String ALARM = "alarm";
    public static final String ALARMS = "alarms";
    public static final String DOOR = "door";
    public static final String DETECTOR = "detector";
    public static final String DETECTORS = "detectors";
    public static final String ACTIVATED = "activated";
    public static final String STATE = "state";


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
            childIdentifier = childObject.getString(IDENTIFIER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[]{deviceType, childIdentifier};
    }
}
