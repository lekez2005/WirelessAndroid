package com.jaykhon.wireless.wireless.devices.alarm;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.utils.Json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lekez2005 on 4/29/15.
 */
public class AlarmCard extends CardView {

    Context mContext;

    JSONObject obj;

    TextView nameView;
    Button stopButton;
    Button ringButton;
    Switch activeSwitch;
    String identifier;

    public AlarmCard(final Context context) {
        super(context);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.fragment_main_alarms_card, this, true);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        nameView = (TextView) findViewById(R.id.pretty_name_view);
        stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(WirelessApp.getBaseUrl() + "alarm/stop/" + identifier, null);
            }
        });
        ringButton = (Button) findViewById(R.id.ring_button);
        ringButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(WirelessApp.getBaseUrl() + "alarm/ring/" + identifier, null);
            }
        });

        activeSwitch = (Switch) findViewById(R.id.active_switch);
        activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    JSONObject data = new JSONObject();
                    data.put(Json.IDENTIFIER, identifier);
                    data.put(Json.ACTIVE, isChecked);
                    if (!obj.getBoolean(Json.ACTIVE) == isChecked) {
                        loadUrl(WirelessApp.getBaseUrl() +"alarm/update", data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void loadUrl(final String url, final JSONObject data) {
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                if (data == null){
                    return SendRequest.getJsonFromUrl(url, null);
                }else{
                    return SendRequest.postJsonToUrl(url, data, null);
                }

            }
        }, new ResultListener<JSONObject>() {
            @Override
            public void onResultsSucceded(JSONObject result) {
                if (result != null) {
                    try {
                        String status = result.getString("Status");
                        if (status.equals("OK")) {
                            Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(mContext, "Refresh Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResultsFail() {
            }
        }, mContext).execute();
    }

    public void setCard(JSONObject object) {
        obj = object;
        try {
            identifier = object.getString(Json.IDENTIFIER);
            nameView.setText(object.getString(Json.PRETTY_NAME));
            activeSwitch.setChecked(obj.getBoolean(Json.ACTIVE));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}