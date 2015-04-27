package com.jaykhon.wireless.wireless.devices.rfid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.devices.ReloadFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lekez2005 on 3/19/15.
 */
public class Card extends LinearLayout {

    private Context mContext;
    ReloadFragment reloader;

    private TextView identifierView;
    private EditText prettyNameEdit;
    private EditText descriptionEdit;
    private Switch activeSwitch;
    private Button updateButton;
    private Button deleteButton;
    private boolean activeChecked = false;
    private String rfid_identifier = "";

    private final String ID_BUNDLE = "id";
    private final String PRETTY_BUNDLE = "pretty";
    private final String ACTIVE_BUNDLE = "active";
    private final String INSTANCE_BUNDLE = "instanceState";
    private final String DESC_BUNDLE = "description";

    private static final String ID_KEY = "identifier";
    private static final String PRETTY_KEY = "pretty_name";
    private static final String VALID_KEY = "valid";
    private static final String DESC_KEY = "description";
    private static final String RFID_KEY = "rfid_identifier";

    public static Card newInstance(Context context, JSONObject obj, RfidFragment f){
        Card c = new Card(context);
        c.mContext = context;
        c.reloader = f;
        try {
            c.identifierView.setText(obj.getString(ID_KEY));
            c.prettyNameEdit.setText(obj.getString(PRETTY_KEY));
            c.descriptionEdit.setText(obj.getString(DESC_KEY));
            c.activeChecked = obj.getBoolean(VALID_KEY);
            c.rfid_identifier = obj.getString(RFID_KEY);
            c.activeSwitch.setChecked(c.activeChecked);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return c;
    }
    public Card(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.rfid_card, this);

        identifierView =  (TextView) findViewById(R.id.identifierView);
        prettyNameEdit = (EditText) findViewById(R.id.prettyNameEdit);
        descriptionEdit = (EditText) findViewById(R.id.descriptionEdit);
        activeSwitch = (Switch) findViewById(R.id.activeSwitch);
        updateButton = (Button) findViewById(R.id.saveButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);

        activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (activeChecked != isChecked){
                    activeChecked = isChecked;
                    //enableCard(isChecked);
                }
            }
        });

        updateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCard();
            }
        });

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Delete Card " + identifierView.getText().toString())
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteCard();

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
    }

    private void enableCard(final boolean isChecked){
        String url = null;

        if (isChecked){
            url = WirelessApp.getBaseUrl()+"rfid/enable/"+identifierView.getText().toString();
        }else{
            url = WirelessApp.getBaseUrl()+"rfid/disable/"+identifierView.getText().toString();
        }
        final String u = url;

        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    return SendRequest.getJsonFromUrl(u, null);
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
                            Toast.makeText(mContext, isChecked? "Enabled":"Disabled", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext, result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(mContext, "Delete Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, mContext).execute();

    }

    private void deleteCard(){
        final String url = WirelessApp.getBaseUrl()+"rfid/remove/"+identifierView.getText().toString();

        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    return SendRequest.getJsonFromUrl(url, null);
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
                            reloader.reload();
                        }else{
                            Toast.makeText(mContext, result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(mContext, "Delete Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, mContext).execute();

    }

    private void updateCard(){
        final String url = WirelessApp.getBaseUrl()+"rfid/update";

        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put(ID_KEY, identifierView.getText().toString());
                    obj.put(PRETTY_KEY, prettyNameEdit.getText().toString());
                    obj.put(VALID_KEY, activeChecked);
                    obj.put(DESC_KEY, descriptionEdit.getText().toString());
                    obj.put(RFID_KEY, rfid_identifier);
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
                            Toast.makeText(mContext, "Card Updated", Toast.LENGTH_SHORT).show();
                            reloader.reload();
                        }else{
                            Toast.makeText(mContext, result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(mContext, "Update Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, mContext).execute();
    }

    @Override
    public Parcelable onSaveInstanceState(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_BUNDLE, super.onSaveInstanceState());
        bundle.putString(ID_BUNDLE, identifierView.getText().toString());
        bundle.putString(PRETTY_BUNDLE, prettyNameEdit.getText().toString());
        bundle.putString(DESC_BUNDLE, descriptionEdit.getText().toString());
        bundle.putBoolean(ACTIVE_BUNDLE, activeChecked);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            identifierView.setText(bundle.getString(ID_BUNDLE));
            prettyNameEdit.setText(bundle.getString(PRETTY_BUNDLE));
            descriptionEdit.setText(bundle.getString(DESC_BUNDLE));
            activeChecked = bundle.getBoolean(ACTIVE_BUNDLE);
            activeSwitch.setChecked(activeChecked);
            state = bundle.getParcelable(INSTANCE_BUNDLE);
        }
        super.onRestoreInstanceState(state);
    }

}
