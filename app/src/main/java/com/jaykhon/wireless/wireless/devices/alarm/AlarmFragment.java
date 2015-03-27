package com.jaykhon.wireless.wireless.devices.alarm;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class AlarmFragment extends Fragment{

    private static final String identifierArg = "identifier";

    private String identifier;

    private TextView identifierView;
    private EditText prettyNameEdit;
    private EditText descriptionEdit;
    private Button updateButton;
    private Button ringButton;
    private Button stopButton;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerManager;
    private RecycleAdapter recycleAdapter;

    private Spinner spinner;
    ArrayAdapter<CharSequence> spinnerAdapter;
    HashMap<String, String> pairedDetectors;
    HashMap<String, String> unpairedDetectors;

    private ImageButton addButton;

    private static final String ID_KEY = "identifier";
    private static final String PRETTY_KEY = "pretty_name";
    private static final String DESC_KEY = "description";
    private static final String DETECTOR_KEY = "detectors";

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
            outState.putString(ID_KEY, identifierView.getText().toString());
            outState.putString(PRETTY_KEY, prettyNameEdit.getText().toString());
            outState.putString(DESC_KEY, descriptionEdit.getText().toString());
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
                lock(false);
            }
        });
        stopButton = (Button) view.findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock(true);
            }
        });

        pairedDetectors = new HashMap<>();
        unpairedDetectors = new HashMap<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerManager);
        recycleAdapter = new RecycleAdapter(getActivity(), pairedDetectors);
        recyclerView.setAdapter(recycleAdapter);



        spinner = (Spinner) view.findViewById(R.id.spinner);
        spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        addButton = (ImageButton) view.findViewById(R.id.add_button);

        if (savedInstanceState!= null && savedInstanceState.containsKey(ID_KEY)){
            identifierView.setText(savedInstanceState.getString(ID_KEY));
            prettyNameEdit.setText(savedInstanceState.getString(PRETTY_KEY));
            descriptionEdit.setText(savedInstanceState.getString(DESC_KEY));
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
                    obj.put(ID_KEY, identifierView.getText().toString());
                    obj.put(PRETTY_KEY, prettyNameEdit.getText().toString());
                    obj.put(DESC_KEY, descriptionEdit.getText().toString());
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
                        identifierView.setText(result.getString(ID_KEY));
                        prettyNameEdit.setText(result.getString(PRETTY_KEY));
                        descriptionEdit.setText(result.getString(DESC_KEY));

                        parseDetectors(result.getJSONArray(DETECTOR_KEY));

                        recycleAdapter.setDetectors(pairedDetectors);
                        recycleAdapter.notifyDataSetChanged();
                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(unpairedDetectors.values());
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

    private void parseDetectors(JSONArray array){
        try {
            // map of connected
            pairedDetectors.clear();
            for (int i = 0; i< array.length(); i++){
                pairedDetectors.put(((JSONArray) array.get(i)).getString(0),
                        ((JSONArray) array.get(i)).getString(1));
            }

            JSONArray allDetectors = WirelessApp.getDevices().getJSONArray("detector");
            int len = allDetectors.length();
            unpairedDetectors.clear();
            for (int i=0; i<len; i++){
                String det_id = ((JSONArray) allDetectors.get(i)).getString(0);
                if (!pairedDetectors.containsKey(det_id)){
                    unpairedDetectors.put(det_id, ((JSONArray) allDetectors.get(i)).getString(1));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}