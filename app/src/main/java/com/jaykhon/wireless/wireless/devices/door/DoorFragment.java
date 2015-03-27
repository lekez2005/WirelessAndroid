package com.jaykhon.wireless.wireless.devices.door;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.devices.rfid.Card;
import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class DoorFragment extends Fragment implements Card.ReloadRfidFragment {

    private static final String identifierArg = "identifier";

    private String identifier;

    private TextView identifierView;
    private EditText prettyNameEdit;
    private EditText descriptionEdit;
    private Button updateButton;
    private Button unlockButton;
    private Button lockButton;
    private String rfid_identifier = "";

    private static final String ID_KEY = "identifier";
    private static final String PRETTY_KEY = "pretty_name";
    private static final String DESC_KEY = "description";
    private static final String RFID_KEY = "rfid";

    public static DoorFragment newInstance(String param1) {
        DoorFragment fragment = new DoorFragment();
        Bundle args = new Bundle();
        args.putString(identifierArg, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public DoorFragment() {
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
        outState.putString(ID_KEY, identifierView.getText().toString());
        outState.putString(PRETTY_KEY, prettyNameEdit.getText().toString());
        outState.putString(DESC_KEY, descriptionEdit.getText().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_door, container, false);
        identifierView =  (TextView) view.findViewById(R.id.identifierView);
        prettyNameEdit = (EditText) view.findViewById(R.id.prettyNameEdit);
        descriptionEdit = (EditText) view.findViewById(R.id.descriptionEdit);
        updateButton = (Button) view.findViewById(R.id.saveButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDoor();
            }
        });
        unlockButton = (Button) view.findViewById(R.id.unlockButton);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock(false);
            }
        });
        lockButton = (Button) view.findViewById(R.id.lockButton);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock(true);
            }
        });

        if (savedInstanceState!= null && savedInstanceState.containsKey(ID_KEY)){
            identifierView.setText(savedInstanceState.getString(ID_KEY));
            prettyNameEdit.setText(savedInstanceState.getString(PRETTY_KEY));
            descriptionEdit.setText(savedInstanceState.getString(DESC_KEY));
            rfid_identifier = savedInstanceState.getString(RFID_KEY);
        }else{
            reload();
        }

        return view;
    }

    private void updateDoor(){
        final String url = WirelessApp.getBaseUrl()+"door/update";
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put(ID_KEY, identifierView.getText().toString());
                    obj.put(PRETTY_KEY, prettyNameEdit.getText().toString());
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
                            Toast.makeText(getActivity(), "Door Updated", Toast.LENGTH_SHORT).show();
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

    private void lock(final Boolean l){
        String url;
        if (l){
            url = WirelessApp.getBaseUrl() + "door/lock/" + identifier;
        }else{
            url = WirelessApp.getBaseUrl() + "door/unlock/" + identifier;
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
                            Toast.makeText(getActivity(), l? "Locked":"Unlocked", Toast.LENGTH_SHORT).show();
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

    @Override
    public void reload() {
        final String url = WirelessApp.getBaseUrl() + "door/"+identifier;
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
                        rfid_identifier = result.getJSONObject(RFID_KEY).getString(ID_KEY);
                        identifierView.setText(result.getString(ID_KEY));
                        prettyNameEdit.setText(result.getString(PRETTY_KEY));
                        descriptionEdit.setText(result.getString(DESC_KEY));
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
