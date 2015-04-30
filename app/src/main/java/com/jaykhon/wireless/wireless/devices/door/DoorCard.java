package com.jaykhon.wireless.wireless.devices.door;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
public class DoorCard extends CardView {

    Context mContext;

    TextView nameView;
    Button unlockButton;
    Button lockButton;
    String identifier;

    public DoorCard(final Context context) {
        super(context);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.fragment_main_door_card, this, true);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        nameView = (TextView) findViewById(R.id.pretty_name_view);
        unlockButton = (Button) findViewById(R.id.ring_button);
        unlockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(WirelessApp.getBaseUrl() + "door/unlock/" + identifier);
            }
        });
        lockButton = (Button) findViewById(R.id.stop_button);
        lockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(WirelessApp.getBaseUrl() + "door/lock/" + identifier);
            }
        });


    }

    private void loadUrl(final String url) {
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                return SendRequest.getJsonFromUrl(url, null);
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
        try {
            identifier = object.getString(Json.IDENTIFIER);
            nameView.setText(object.getString(Json.PRETTY_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}