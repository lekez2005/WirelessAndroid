package com.jaykhon.wireless.wireless.devices.alarm;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lekez2005 on 3/26/15.
 */
public class DetectorCard extends CardView {

    public TextView prettyNameView;
    private ImageButton deleteButton;

    private String detectorIdentifier;
    private String detectorName;
    private AlarmFragment fragment;

    public DetectorCard(final Context context, AlarmFragment f){
        super(context);
        fragment = f;
        LayoutInflater.from(context).inflate(R.layout.alarm_detector_card, this);
        prettyNameView = (TextView) findViewById(R.id.detector_view);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDetector();
            }
        });
    }

    public void updateDetector(String identifier, String name){
        detectorIdentifier = identifier;
        detectorName = name;
        prettyNameView.setText(detectorName);
    }

    private void removeDetector(){
        final String url = WirelessApp.getBaseUrl() + "alarm/remove/detector";
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put(AlarmFragment.ID_KEY, fragment.identifier);
                    obj.put("detector", detectorIdentifier);
                    return SendRequest.postJsonToUrl(url, obj, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, new ResultListener<JSONObject>() {
            @Override
            public void onResultsSucceded(JSONObject result) {
                if( result != null){
                    try {
                        String status = result.getString("Status");
                        if (status.equals("OK")){
                            Toast.makeText(fragment.getActivity(),"Removed", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(fragment.getActivity(), result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(fragment.getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                }
                fragment.reload();

            }

            @Override
            public void onResultsFail() {
            }
        }, fragment.getActivity()).execute();
    }


}
