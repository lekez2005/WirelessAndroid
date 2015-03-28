package com.jaykhon.wireless.wireless.authorize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import com.jaykhon.wireless.wireless.MainActivity;
import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;

/**
 * A login screen that offers login via email/password.
 */
public class UserSelectActivity extends Activity {

    private Spinner usersSpinner;
    private ArrayAdapter<CharSequence> spinnerAdapter;
    private Button switchButton;
    private ToggleButton toggleButton;
    private LinearLayout newUserLayout;
    private EditText newUserIdView;
    private EditText newUserNameView;
    private CheckBox newUserAuthorized;

    private ArrayList<String> userIds;
    private ArrayList<String> userNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        usersSpinner = (Spinner) findViewById(R.id.users_spinner);
        spinnerAdapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usersSpinner.setAdapter(spinnerAdapter);

        switchButton = (Button) findViewById(R.id.switchButton);



        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    newUserLayout.setVisibility(View.VISIBLE);
                }else{
                    newUserLayout.setVisibility(View.GONE);
                }
            }
        });

        newUserLayout = (LinearLayout) findViewById(R.id.new_user_layout);
        newUserLayout.setVisibility(View.GONE);
        newUserIdView = (EditText) findViewById(R.id.new_user_id);
        newUserNameView = (EditText) findViewById(R.id.new_user_name);
        newUserAuthorized = (CheckBox) findViewById(R.id.new_user_authorized);

        getActionBar().setHomeButtonEnabled(true);

        reload();


    }

    private void reload(){
        final String url = WirelessApp.getBaseUrl() + "user/users";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}



