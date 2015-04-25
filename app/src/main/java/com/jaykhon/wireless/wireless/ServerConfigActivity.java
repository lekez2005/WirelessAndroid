package com.jaykhon.wireless.wireless;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jaykhon.wireless.wireless.authorize.UserSelectActivity;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lekez2005 on 4/24/15.
 */
public class ServerConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.fragment_server_config);
        final EditText ipText = (EditText) findViewById(R.id.ipaddress);
        ipText.setText(WirelessApp.getPreferences().getServerIp());
        final EditText portText = (EditText) findViewById(R.id.portText);
        portText.setText(WirelessApp.getPreferences().getServerPort());
        final TextView resultView = (TextView) findViewById(R.id.resultView);

        Button button = (Button) findViewById(R.id.sendButton);
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
                        if (result != null) {
                            try {
                                String f = result.toString(4);
                                resultView.setText(f);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            resultView.setText("Invalid");
                        }

                    }

                    @Override
                    public void onResultsFail() {

                    }
                }, getApplicationContext()).execute();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config_server, menu);
        restoreActionBar();
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Server Config");
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
        }
        return super.onOptionsItemSelected(item);
    }
}
