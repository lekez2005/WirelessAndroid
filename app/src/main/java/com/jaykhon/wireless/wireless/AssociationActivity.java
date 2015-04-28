package com.jaykhon.wireless.wireless;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.authorize.UserSelectActivity;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lekez2005 on 4/27/15.
 */
public class AssociationActivity extends Activity {

    private static final String ALARM = "Alarm";
    private static final String DETECTOR = "Detector";

    Spinner fromDevice;
    ArrayAdapter<CharSequence> fromDeviceAdapter;
    Spinner fromIdentifier;
    ArrayAdapter<CharSequence> fromIdentifierAdapter;
    Spinner toIdentifier;
    ArrayAdapter<CharSequence> toIdentifierAdapter;
    Button associateButton;

    RecyclerView associated;
    RecycleAdapter recycleAdapter;
    private RecyclerView.LayoutManager recyclerManager;
    ArrayList<JSONObject> pairedDevices;
    ArrayList<String> unpairedIdentifiers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_association);

        pairedDevices = new ArrayList<>();
        unpairedIdentifiers = new ArrayList<>();

        fromDevice = (Spinner) findViewById(R.id.from_device);
        fromDeviceAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item);
        fromDeviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromDeviceAdapter.addAll(ALARM, DETECTOR);
        fromDevice.setAdapter(fromDeviceAdapter);
        fromDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                selectDevice(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fromIdentifier = (Spinner) findViewById(R.id.from_identifier);
        fromIdentifierAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item);
        fromIdentifierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromIdentifier.setAdapter(fromIdentifierAdapter);
        fromIdentifier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateUnpaired();
                populatePaired();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        toIdentifier = (Spinner) findViewById(R.id.to_identifier);
        toIdentifierAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item);
        toIdentifierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toIdentifier.setAdapter(toIdentifierAdapter);


        associateButton = (Button) findViewById(R.id.associate_button);
        associateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                associate();
            }
        });



        associated = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerManager = new LinearLayoutManager(this);
        associated.setLayoutManager(recyclerManager);
        recycleAdapter = new RecycleAdapter(this, pairedDevices);
        associated.setAdapter(recycleAdapter);

    }

    private void selectDevice(String device){
        JSONObject devices = WirelessApp.getDevices();
        if (devices == null)
            return;
        try {
            JSONArray selected = new JSONArray();
            if ("Detector".equals(device)){
                selected = devices.getJSONArray(Json.DETECTOR);
            }else if ("Alarm".equals(device)){
                selected = devices.getJSONArray(Json.ALARM);
            }
            fromIdentifierAdapter.clear();
            for (int i = 0; i< selected.length(); i++){
                fromIdentifierAdapter.add((selected.getJSONObject(i)).getString(Json.PRETTY_NAME));
            }
            populateUnpaired();
            populatePaired();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateUnpaired(){
        JSONObject devices = WirelessApp.getDevices();
        if (devices == null)
            return;
        String selectedDeviceType = fromDevice.getSelectedItem().toString();
        int selectedIdentifierPosition = fromIdentifier.getSelectedItemPosition();

        try {
            JSONArray allPairable = new JSONArray();
            JSONArray paired = new JSONArray();
            if ("Detector".equals(selectedDeviceType)){
                allPairable = devices.getJSONArray(Json.ALARM);
                JSONObject selectedDevice = (devices.getJSONArray(Json.DETECTOR))
                        .getJSONObject(selectedIdentifierPosition);
                paired = selectedDevice.getJSONArray(Json.ALARMS);

            }else if ("Alarm".equals(selectedDeviceType)){
                allPairable = devices.getJSONArray(Json.DETECTOR);
                JSONObject selectedDevice = (devices.getJSONArray(Json.ALARM))
                        .getJSONObject(selectedIdentifierPosition);
                paired = selectedDevice.getJSONArray(Json.DETECTORS);
            }
            toIdentifierAdapter.clear();
            unpairedIdentifiers.clear();
            for (int i = 0; i < allPairable.length(); i++){
                boolean alreadyPaired = false;
                JSONObject potentialPair = allPairable.getJSONObject(i);
                for (int j = 0; j< paired.length(); j++){
                    if (potentialPair.getString(Json.IDENTIFIER)
                            .equals(paired.getString(j))){
                        alreadyPaired = true;
                    }
                }
                 if (! alreadyPaired){
                     toIdentifierAdapter.add(potentialPair.getString(Json.PRETTY_NAME));
                     unpairedIdentifiers.add(potentialPair.getString(Json.IDENTIFIER));
                 }
            }
            toIdentifierAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void populatePaired(){
        JSONObject devices = WirelessApp.getDevices();
        if (devices == null)
            return;
        String selectedDeviceType = fromDevice.getSelectedItem().toString();
        int selectedIdentifierPosition = fromIdentifier.getSelectedItemPosition();

        try {
            JSONArray allPairable = new JSONArray();
            JSONArray paired = new JSONArray();
            if ("Detector".equals(selectedDeviceType)){
                allPairable = devices.getJSONArray(Json.ALARM);
                JSONObject selectedDevice = (devices.getJSONArray(Json.DETECTOR))
                        .getJSONObject(selectedIdentifierPosition);
                paired = selectedDevice.getJSONArray(Json.ALARMS);

            }else if ("Alarm".equals(selectedDeviceType)){
                allPairable = devices.getJSONArray(Json.DETECTOR);
                JSONObject selectedDevice = (devices.getJSONArray(Json.ALARM))
                        .getJSONObject(selectedIdentifierPosition);
                paired = selectedDevice.getJSONArray(Json.DETECTORS);
            }
            pairedDevices.clear();

            for (int i = 0; i < allPairable.length(); i++){

                if (! unpairedIdentifiers.contains(allPairable.getJSONObject(i).getString(Json.IDENTIFIER))){
                    pairedDevices.add(allPairable.getJSONObject(i));
                }
            }
            recycleAdapter.setPairedDevices(pairedDevices);
            recycleAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void associate(){
        JSONObject devices = WirelessApp.getDevices();
        if (devices == null || fromIdentifier.getSelectedItemPosition() == AdapterView.INVALID_POSITION ||
                toIdentifier.getSelectedItemPosition() == AdapterView.INVALID_POSITION)
            return;
        String selected = fromDevice.getSelectedItem().toString();
        final JSONObject data = new JSONObject();
        String url = WirelessApp.getBaseUrl();
        try {

            if ("Detector".equals(selected)){
                url+="detector/add/alarm";
                JSONObject detector = devices.getJSONArray(Json.DETECTOR)
                        .getJSONObject(fromIdentifier.getSelectedItemPosition());
                data.put(Json.IDENTIFIER, detector.getString(Json.IDENTIFIER));
                data.put(Json.ALARM, unpairedIdentifiers.get(toIdentifier.getSelectedItemPosition()));
            } else if ("Alarm".equals(selected)){
                url+="alarm/add/detector";
                JSONObject alarm = devices.getJSONArray(Json.ALARM)
                        .getJSONObject(fromIdentifier.getSelectedItemPosition());
                data.put(Json.IDENTIFIER, alarm.getString(Json.IDENTIFIER));
                data.put(Json.DETECTOR, unpairedIdentifiers.get(toIdentifier.getSelectedItemPosition()));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String u = url;
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                return SendRequest.postJsonToUrl(u, data, null);
            }
        }, new ResultListener<JSONObject>() {
            @Override
            public void onResultsSucceded(JSONObject result) {
                if (result != null) {
                    reload();
                }else {
                    Toast.makeText(getApplicationContext(), "Association failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResultsFail() {
            }
        }, getApplicationContext()).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_associate_activity, menu);
        restoreActionBar();
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Associations");
    }

    public void reload(){
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                String url = WirelessApp.getBaseUrl() + "modules";
                return SendRequest.getJsonFromUrl(url, null);
            }
        }, new ResultListener<JSONObject>() {
            @Override
            public void onResultsSucceded(JSONObject result) {
                if (result != null) {
                    WirelessApp.setmDevices(result);
                    WirelessApp.setLastConnectionSuccess(true);
                    populateUnpaired();
                    populatePaired();
                }else {
                    Toast.makeText(getApplicationContext(), "Refresh Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResultsFail() {
            }
        }, getApplicationContext()).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.select_user) {
            Intent intent = new Intent(this, UserSelectActivity.class);
            startActivity(intent);
            return true; //TODO put settings here
        }else if(id == android.R.id.home){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.config_server){
            Intent i = new Intent(this, ServerConfigActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

        private ArrayList<JSONObject> paired;
        private Context context;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public DeviceCard card;
            public ViewHolder(DeviceCard v){
                super(v);
                card = v;
            }
        }

        public void setPairedDevices(ArrayList<JSONObject> pairedDevices){
            this.paired = pairedDevices;
        }

        public RecycleAdapter(Context mContext, ArrayList<JSONObject> pairedDevices){
            this.paired = pairedDevices;
            context = mContext;
        }

        @Override
        public RecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            DeviceCard c = new DeviceCard(context);
            ViewHolder vh = new ViewHolder(c);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position){
            try {
                viewHolder.card.updateDevice(paired.get(position).getString(Json.IDENTIFIER),
                        paired.get(position).getString(Json.PRETTY_NAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount(){
            return paired.size();
        }
    }

    public class DeviceCard extends CardView {

        public TextView prettyNameView;
        private ImageButton deleteButton;

        private String deviceIdentifier;
        private String deviceName;

        public DeviceCard(final Context context){
            super(context);
            LayoutInflater.from(context).inflate(R.layout.association_card, this);
            prettyNameView = (TextView) findViewById(R.id.name_view);
            deleteButton = (ImageButton) findViewById(R.id.delete_button);

            deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeDevice();
                }
            });
        }

        public void updateDevice(String identifier, String name){
            deviceIdentifier = identifier;
            deviceName = name;
            prettyNameView.setText(deviceName);
        }

        private void removeDevice(){

            JSONObject devices = WirelessApp.getDevices();
            if (devices == null || fromIdentifier.getSelectedItemPosition() == AdapterView.INVALID_POSITION)
                return;
            String selected = fromDevice.getSelectedItem().toString();
            final JSONObject data = new JSONObject();
            String url = WirelessApp.getBaseUrl();
            try {

                if ("Detector".equals(selected)){
                    url+="detector/remove/alarm";
                    JSONObject detector = devices.getJSONArray(Json.DETECTOR)
                            .getJSONObject(fromIdentifier.getSelectedItemPosition());
                    data.put(Json.IDENTIFIER, detector.getString(Json.IDENTIFIER));
                    data.put(Json.ALARM, deviceIdentifier);
                } else if ("Alarm".equals(selected)){
                    url+="alarm/remove/detector";
                    JSONObject alarm = devices.getJSONArray(Json.ALARM)
                            .getJSONObject(fromIdentifier.getSelectedItemPosition());
                    data.put(Json.IDENTIFIER, alarm.getString(Json.IDENTIFIER));
                    data.put(Json.DETECTOR, deviceIdentifier);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            final String u = url;
            new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
                @Override
                public JSONObject execute() {
                    try {
                        return SendRequest.postJsonToUrl(u, data, null);
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
                                Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
                                reload();
                            }else{
                                Toast.makeText(getApplicationContext(), result.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                    reload();

                }

                @Override
                public void onResultsFail() {
                }
            }, getApplicationContext()).execute();
        }


    }
}
