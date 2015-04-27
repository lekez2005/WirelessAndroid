package com.jaykhon.wireless.wireless;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    HashMap<String, String> allDevices;
    HashMap<String, String> pairedDevices;
    HashMap<String, String> unpairedDevices;
    ArrayList<String> unpairedIdentifiers;
    ArrayList<String> unpairedNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_association);

        allDevices = new HashMap<>();
        pairedDevices = new HashMap<>();
        unpairedDevices = new HashMap<>();
        unpairedIdentifiers = new ArrayList<>();
        unpairedNames = new ArrayList<>();

        fromDevice = (Spinner) findViewById(R.id.from_device);
        fromDeviceAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item);
        fromDeviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromDeviceAdapter.addAll(ALARM, DETECTOR);
        fromDevice.setAdapter(fromDeviceAdapter);
        fromDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == parent.getSelectedItemPosition() && ! allDevices.isEmpty()){
                    return;
                }else {
                    String selected = parent.getItemAtPosition(position).toString();
                    if (selected == ALARM){
                        Toast.makeText(getApplicationContext(), "Alarm", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Detector", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fromIdentifier = (Spinner) findViewById(R.id.from_identifier);
        toIdentifier = (Spinner) findViewById(R.id.to_identifier);

        associateButton = (Button) findViewById(R.id.associate_button);



        associated = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerManager = new LinearLayoutManager(this);
        associated.setLayoutManager(recyclerManager);
        recycleAdapter = new RecycleAdapter(this, pairedDevices);
        associated.setAdapter(recycleAdapter);

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

        private HashMap<String, String> devices;
        private ArrayList<String> identifiers;
        private ArrayList<String> names;
        private Context context;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public DeviceCard card;
            public ViewHolder(DeviceCard v){
                super(v);
                card = v;
            }
        }

        public RecycleAdapter(Context mContext, HashMap<String, String> pairedDevices){
            identifiers = new ArrayList<>();
            names = new ArrayList<>();
            setDevices(pairedDevices);
            context = mContext;
        }

        public void setDevices(HashMap<String, String> d){
            devices = d;
            identifiers.clear();
            names.clear();
            for (Map.Entry<String, String> entry: devices.entrySet()){
                identifiers.add(entry.getKey());
                names.add(entry.getValue());
            }
        }

        @Override
        public RecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            DeviceCard c = new DeviceCard(context);
            ViewHolder vh = new ViewHolder(c);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position){
            viewHolder.card.updateDevice(identifiers.get(position), names.get(position));
        }

        @Override
        public int getItemCount(){
            return devices.size();
        }
    }

    public class DeviceCard extends CardView {

        public TextView prettyNameView;
        private ImageButton deleteButton;

        private String detectorIdentifier;
        private String detectorName;

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
            detectorIdentifier = identifier;
            detectorName = name;
            prettyNameView.setText(detectorName);
        }

        private void removeDevice(){
            final String url = WirelessApp.getBaseUrl() + "alarmds/remove/detector";
            new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
                @Override
                public JSONObject execute() {
                    try { //TODO fix this
                        JSONObject obj = new JSONObject();
                        obj.put("identifier", "identifier");
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
                                Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
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
