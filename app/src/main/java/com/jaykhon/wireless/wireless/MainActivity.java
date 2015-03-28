package com.jaykhon.wireless.wireless;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jaykhon.wireless.wireless.authorize.UserSelectActivity;
import com.jaykhon.wireless.wireless.devices.alarm.AlarmFragment;
import com.jaykhon.wireless.wireless.devices.door.DoorFragment;
import com.jaykhon.wireless.wireless.devices.rfid.RfidFragment;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.utils.Dialogs;
import com.jaykhon.wireless.wireless.utils.Json;

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

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int parentPosition, int childPosition) {

        // TODO fix reloading twice
        // update the main content by replacing fragments
        JSONObject devices = WirelessApp.getDevices();
        if (devices != null){
            String[] ids = Json.getIdentifiersFromJson(parentPosition, childPosition, devices);
            String deviceType = ids[0];
            if ("rfid".equals(deviceType)){
                //Dialogs.makeSingleButton(MainActivity.this, "devices null WHOA");
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, RfidFragment.newInstance(ids[1]))
                        .addToBackStack(null)
                        .commit();

            }else if ("door".equals(deviceType)){
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, DoorFragment.newInstance(ids[1]))
                        .addToBackStack(null)
                        .commit();

            }else if ("alarm".equals(deviceType)){
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, AlarmFragment.newInstance(ids[1]))
                        .addToBackStack(null)
                        .commit();
            }else{
                Dialogs.makeSingleButton(MainActivity.this,
                        String.format("Parent: %s Child: %s\n Implement other devices", ids[0], ids[1]));
                //Dialogs.makeSingleButton(MainActivity.this, "devices null WHOA");
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new MainActivityFragment())
                        .commit();
            }

        }else{
            //Dialogs.makeSingleButton(MainActivity.this, "devices null WHOA");
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MainActivityFragment())
                    .commit();
        }


    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainActivityFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public MainActivityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final EditText ipText = (EditText) rootView.findViewById(R.id.ipaddress);
            ipText.setText(WirelessApp.getPreferences().getServerIp());
            final EditText portText = (EditText) rootView.findViewById(R.id.portText);
            portText.setText(WirelessApp.getPreferences().getServerPort());
            final TextView resultView = (TextView) rootView.findViewById(R.id.resultView);

            Button button = (Button) rootView.findViewById(R.id.sendButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ip = ipText.getText().toString();
                    WirelessApp.setIp(ip);
                    String port = portText.getText().toString();
                    WirelessApp.setPort(port);
                    final String url = WirelessApp.getBaseUrl();
                    Log.d("Wireless: ", url);
                    new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
                        @Override
                        public JSONObject execute() {
                            return SendRequest.getJsonFromUrl(url, null);

                        }
                    }, new ResultListener<JSONObject>() {
                        @Override
                        public void onResultsSucceded(JSONObject result) {
                            if( result != null){
                                try {
                                    String f = result.toString(4);
                                    resultView.setText(f);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                resultView.setText("Invalid");
                            }

                        }

                        @Override
                        public void onResultsFail() {

                        }
                    }, getActivity()).execute();
                }
            });


            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }

}
