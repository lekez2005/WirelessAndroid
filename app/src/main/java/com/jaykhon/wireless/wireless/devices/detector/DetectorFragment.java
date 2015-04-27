package com.jaykhon.wireless.wireless.devices.detector;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.MainActivity;
import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.devices.ReloadFragment;

import org.json.JSONException;
import org.json.JSONObject;


public class DetectorFragment extends Fragment implements ReloadFragment {

    private static final String identifierArg = "identifier";

    private String identifier;

    private TextView identifierView;
    private EditText prettyNameEdit;
    private EditText descriptionEdit;
    private EditText messageEdit;
    private Button updateButton;
    private Button deleteButton;

    private Switch activeSwitch;

    private static final String ID_KEY = "identifier";
    private static final String PRETTY_KEY = "pretty_name";
    private static final String DESC_KEY = "description";
    private static final String ACTIVE_KEY = "active";
    private static final String MESSAGE_KEY = "alarm_message";

    public static DetectorFragment newInstance(String param1) {
        DetectorFragment fragment = new DetectorFragment();
        Bundle args = new Bundle();
        args.putString(identifierArg, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public DetectorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            identifier = getArguments().getString(identifierArg);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ID_KEY, identifierView.getText().toString());
        outState.putString(PRETTY_KEY, prettyNameEdit.getText().toString());
        outState.putString(DESC_KEY, descriptionEdit.getText().toString());
        outState.putString(MESSAGE_KEY, messageEdit.getText().toString());
        outState.putBoolean(ACTIVE_KEY, activeSwitch.isChecked());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detector, container, false);
        identifierView =  (TextView) view.findViewById(R.id.identifierView);
        prettyNameEdit = (EditText) view.findViewById(R.id.prettyNameEdit);
        descriptionEdit = (EditText) view.findViewById(R.id.descriptionEdit);
        messageEdit = (EditText) view.findViewById(R.id.message_edit);
        updateButton = (Button) view.findViewById(R.id.saveButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetector();
            }
        });
        deleteButton = (Button) view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDetector();
            }
        });

        activeSwitch = (Switch) view.findViewById(R.id.active_switch);


        if (savedInstanceState!= null && savedInstanceState.containsKey(ID_KEY)){
            identifierView.setText(savedInstanceState.getString(ID_KEY));
            prettyNameEdit.setText(savedInstanceState.getString(PRETTY_KEY));
            descriptionEdit.setText(savedInstanceState.getString(DESC_KEY));
            activeSwitch.setChecked(savedInstanceState.getBoolean(ACTIVE_KEY));
        }else{
            reload();
        }

        return view;
    }

    private void updateDetector(){
        final String url = WirelessApp.getBaseUrl()+"detector/update";
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put(ID_KEY, identifierView.getText().toString());
                    obj.put(PRETTY_KEY, prettyNameEdit.getText().toString());
                    obj.put(DESC_KEY, descriptionEdit.getText().toString());
                    obj.put(MESSAGE_KEY, messageEdit.getText().toString());
                    obj.put(ACTIVE_KEY, activeSwitch.isChecked());
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
                            Toast.makeText(getActivity(), "Detector Updated", Toast.LENGTH_SHORT).show();
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

    private void deleteDetector(){
        final String u = WirelessApp.getBaseUrl() + "detector/delete/" + identifier;
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
                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
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
        final String url = WirelessApp.getBaseUrl() + "detector/"+identifier;
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
                        identifierView.setText(result.getString(ID_KEY));
                        prettyNameEdit.setText(result.getString(PRETTY_KEY));
                        descriptionEdit.setText(result.getString(DESC_KEY));
                        messageEdit.setText(result.getString(MESSAGE_KEY));
                        activeSwitch.setChecked(result.getBoolean(ACTIVE_KEY));
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
