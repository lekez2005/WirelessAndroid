package com.jaykhon.wireless.wireless.authorize;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import com.jaykhon.wireless.wireless.AssociationActivity;
import com.jaykhon.wireless.wireless.MainActivity;
import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.ServerConfigActivity;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.gcm.GCMRegister;
import com.jaykhon.wireless.wireless.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class UserSelectActivity extends Activity implements OnSwitchFragmentListener {

    public static final String SELECT_USER = "select_user";
    public static final String ADD_USER = "add_user";
    public static final String EDIT_USER = "edit_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setHomeButtonEnabled(true);
        setContentView(R.layout.activity_user);

        if (savedInstanceState != null)
            return;

        SelectUserFragment selectUserFragment = new SelectUserFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, selectUserFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_user, menu);
        restoreActionBar();
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Users");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.config_server:
                Intent i = new Intent(this, ServerConfigActivity.class);
                startActivity(i);
                return true;
            case R.id.associate:
                Intent i2 = new Intent(this, AssociationActivity.class);
                startActivity(i2);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSwitchFragment(String newFrag, String user) {
        switch(newFrag){
            case SELECT_USER:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SelectUserFragment())
                        .addToBackStack(null)
                        .commit();
                break;
            case ADD_USER:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddUserFragment())
                        .addToBackStack(null)
                        .commit();
                break;
            case EDIT_USER:
                Bundle args = new Bundle();
                args.putString(EditUserFragment.IDENTIFIER_ARG, user);
                EditUserFragment frag = new EditUserFragment();
                frag.setArguments(args);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag)
                        .addToBackStack(null)
                        .commit();

        }

    }
}



