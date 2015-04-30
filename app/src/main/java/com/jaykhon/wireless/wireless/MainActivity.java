package com.jaykhon.wireless.wireless;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.authorize.UserSelectActivity;
import com.jaykhon.wireless.wireless.devices.alarm.AlarmAdapter;
import com.jaykhon.wireless.wireless.devices.alarm.AlarmFragment;
import com.jaykhon.wireless.wireless.devices.detector.DetectorFragment;
import com.jaykhon.wireless.wireless.devices.door.DoorAdapter;
import com.jaykhon.wireless.wireless.devices.door.DoorFragment;
import com.jaykhon.wireless.wireless.devices.rfid.RfidFragment;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.utils.Dialogs;
import com.jaykhon.wireless.wireless.utils.Json;

import org.apache.http.impl.conn.Wire;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


public class MainActivity extends Activity  implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    public static final String DEVICE_KEY = "device";
    public static final String IDENTIFIER_KEY = "identifier";


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public static CharSequence mTitle = "Wireless";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        //mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        updateDeviceList();

        Intent i = getIntent();
        replaceFragment(i.getStringExtra(DEVICE_KEY), i.getStringExtra(IDENTIFIER_KEY));
        }

    @Override
    public void onNavigationDrawerItemSelected(int parentPosition, int childPosition) {
        JSONObject devices = WirelessApp.getDevices();
        if (devices != null) {
            String[] ids = Json.getIdentifiersFromJson(parentPosition, childPosition, devices);
            replaceFragment(ids[0], ids[1]);
        }else {
            replaceFragment(null, null);
        }
    }

    private  void replaceFragment(String deviceType, String identifier){
        if (deviceType == null || identifier == null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MainActivityFragment())
                    .commit();
        }else if ("rfid".equals(deviceType)){
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, RfidFragment.newInstance(identifier))
                    .commit();

        }else if ("door".equals(deviceType)){
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, DoorFragment.newInstance(identifier))
                    .commit();

        }else if ("alarm".equals(deviceType)){
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, AlarmFragment.newInstance(identifier))
                    .commit();
        }else if("detector".equals(deviceType)){
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, DetectorFragment.newInstance(identifier))
                    .commit();
        }
        else{
            Dialogs.makeSingleButton(MainActivity.this,
                    String.format("Parent: %s Child: %s\n Implement other devices", deviceType, identifier));
            //Dialogs.makeSingleButton(MainActivity.this, "devices null WHOA");
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MainActivityFragment())
                    .commit();
        }
        invalidateOptionsMenu();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_with_home, menu);
        restoreActionBar();
        return true;
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
        }else if(id == R.id.action_home){
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MainActivityFragment())
                    .commit();
        }else if (id == R.id.config_server){
            Intent intent = new Intent(this, ServerConfigActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.refresh_server){
            Toast.makeText(this, "Refreshing Server", Toast.LENGTH_SHORT).show();
            updateDeviceList();
            return true;
        }else if (id == R.id.associate){
            Intent intent = new Intent(this, AssociationActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateDeviceList() {

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
                    Toast.makeText(getApplicationContext(), "Refreshed!", Toast.LENGTH_SHORT).show();
                    mNavigationDrawerFragment.onReload();
                    Fragment f = getFragmentManager().findFragmentById(R.id.container);
                    if (f instanceof MainActivityFragment){
                        ((MainActivityFragment) f).reloadFragment();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Refresh Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResultsFail() {
            }
        }, getApplicationContext()).execute();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainActivityFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private Switch activatedSwitch;
        View indicatorView;

        RecyclerView doorRecycler;
        DoorAdapter doorAdapter;
        RecyclerView.LayoutManager doorManager;

        RecyclerView alarmRecycler;
        AlarmAdapter alarmAdapter;
        RecyclerView.LayoutManager alarmManager;

        public MainActivityFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.clear();
            inflater.inflate(R.menu.main, menu);
            ActionBar actionBar = getActivity().getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(MainActivity.mTitle);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //reloadFragment();

            activatedSwitch = (Switch) rootView.findViewById(R.id.activated_switch);
            activatedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    JSONObject data = new JSONObject();
                    try {
                        if (WirelessApp.getDevices()!=null &&
                                WirelessApp.getDevices().getBoolean(Json.ACTIVATED) != isChecked){
                            goToUrl(WirelessApp.getBaseUrl() + "activate", data);
                            data.put(Json.STATE, isChecked);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
            indicatorView = rootView.findViewById(R.id.indicator_view);

//            doorRecycler = (RecyclerView) rootView.findViewById(R.id.door_recycler);
//            doorManager = new LinearLayoutManager(getActivity());
//            doorRecycler.setLayoutManager(doorManager);
//            doorAdapter = new DoorAdapter(getActivity());
//            doorRecycler.setAdapter(doorAdapter);

            alarmRecycler = (RecyclerView) rootView.findViewById(R.id.alarm_recycler);
            alarmManager = new LinearLayoutManager(getActivity());
            alarmRecycler.setLayoutManager(alarmManager);
            alarmAdapter = new AlarmAdapter(getActivity());
            alarmRecycler.setAdapter(alarmAdapter);


            return rootView;
        }

        private void reloadFragment(){
            JSONObject devices = WirelessApp.getDevices();
            if (devices != null && this.isVisible()){
                try {
                    activatedSwitch.setChecked(devices.getBoolean(Json.ACTIVATED));
                    if (devices.getBoolean(Json.ACTIVATED)){
                        indicatorView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    }else {
                        indicatorView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                    }
                    //doorAdapter.notifyDataSetChanged();
                    alarmAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void goToUrl(final String url, final JSONObject data){
            new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
                @Override
                public JSONObject execute() {
                    if (data == null){
                        return SendRequest.getJsonFromUrl(url, null);
                    }else {
                        return SendRequest.postJsonToUrl(url, data, null);
                    }

                }
            }, new ResultListener<JSONObject>() {
                @Override
                public void onResultsSucceded(JSONObject result) {
                    if (result != null) {
                        try {
                            String status = result.getString("Status");
                            if (status.equals("OK")){
                                Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                                ((MainActivity) getActivity()).updateDeviceList();
                            }else{
                                Toast.makeText(getActivity(), result.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(getActivity(), "Refresh Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onResultsFail() {
                }
            }, getActivity()).execute();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }









}
