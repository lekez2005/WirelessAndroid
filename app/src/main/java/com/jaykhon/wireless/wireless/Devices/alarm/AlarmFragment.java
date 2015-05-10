package com.jaykhon.wireless.wireless.devices.alarm;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class AlarmFragment extends Fragment {

    private static final String identifierArg = "identifier";

    public String identifier;

    private TextView identifierView;
    private EditText prettyNameEdit;
    private EditText descriptionEdit;
    private Button updateButton;
    private Button ringButton;
    private Button stopButton;
    private Switch activeSwitch;


    public static AlarmFragment newInstance(String param1) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        args.putString(identifierArg, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public AlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            identifier = getArguments().getString(identifierArg);
            reload();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null){
            outState.putString(Json.IDENTIFIER, identifierView.getText().toString());
            outState.putString(Json.PRETTY_NAME, prettyNameEdit.getText().toString());
            outState.putString(Json.DESCRIPTION, descriptionEdit.getText().toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        identifierView =  (TextView) view.findViewById(R.id.identifierView);
        prettyNameEdit = (EditText) view.findViewById(R.id.prettyNameEdit);
        descriptionEdit = (EditText) view.findViewById(R.id.descriptionEdit);
        updateButton = (Button) view.findViewById(R.id.saveButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAlarm();
            }
        });
        ringButton = (Button) view.findViewById(R.id.ringButton);
        ringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ring(true);
            }
        });
        stopButton = (Button) view.findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ring(false);
            }
        });
        activeSwitch = (Switch) view.findViewById(R.id.active_switch);


        if (savedInstanceState!= null && savedInstanceState.containsKey(Json.IDENTIFIER)){
            identifierView.setText(savedInstanceState.getString(Json.IDENTIFIER));
            prettyNameEdit.setText(savedInstanceState.getString(Json.PRETTY_NAME));
            descriptionEdit.setText(savedInstanceState.getString(Json.DESCRIPTION));
        }else{
            reload();
        }

        return view;
    }

    private void updateAlarm(){
        final String url = WirelessApp.getBaseUrl()+"alarm/update";
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put(Json.IDENTIFIER, identifierView.getText().toString());
                    obj.put(Json.PRETTY_NAME, prettyNameEdit.getText().toString());
                    obj.put(Json.DESCRIPTION, descriptionEdit.getText().toString());
                    obj.put(Json.ACTIVE, activeSwitch.isChecked());
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
                            Toast.makeText(getActivity(), "Alarm Updated", Toast.LENGTH_SHORT).show();
                            reload();
                        }else{
                            Toast.makeText(getActivity(), result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getActivity(), "Update Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, getActivity()).execute();
    }

    private void ring(final Boolean ring){
        String url;
        if (ring){
            url = WirelessApp.getBaseUrl() + "alarm/ring/" + identifier;
        }else{
            url = WirelessApp.getBaseUrl() + "alarm/stop/" + identifier;
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
                            Toast.makeText(getActivity(), ring? "Alarm ringing":"Alarm stopped", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, getActivity()).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void reload() {
        final String url = WirelessApp.getBaseUrl() + "alarm/"+identifier;
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
                        identifierView.setText(result.getString(Json.IDENTIFIER));
                        prettyNameEdit.setText(result.getString(Json.PRETTY_NAME));
                        descriptionEdit.setText(result.getString(Json.DESCRIPTION));
                        activeSwitch.setChecked(result.getBoolean(Json.ACTIVE));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(getActivity(), "Reload Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, getActivity()).execute();
    }

}
