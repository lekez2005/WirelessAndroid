package com.jaykhon.wireless.wireless.authorize;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.utils.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lekez2005 on 4/25/15.
 */
public class EditUserFragment extends Fragment {

    public static final String IDENTIFIER_ARG = "identifier";

    OnSwitchFragmentListener mCallback;

    String mIdentifier;

    EditText nameEditText;
    Switch authorizeSwitch;
    Switch notifySwitch;

    Button saveButton;
    Button deleteButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mCallback = (OnSwitchFragmentListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mIdentifier = getArguments().getString(IDENTIFIER_ARG);
        View rootView = inflater.inflate(R.layout.fragment_edit_user, container, false);

        nameEditText = (EditText) rootView.findViewById(R.id.name_edit_text);
        authorizeSwitch = (Switch) rootView.findViewById(R.id.user_authorized_switch);
        notifySwitch = (Switch) rootView.findViewById(R.id.user_notification_switch);
        saveButton = (Button) rootView.findViewById(R.id.save_user_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();
            }
        });

        deleteButton = (Button) rootView.findViewById(R.id.delete_user_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        populateViews();
        return rootView;
    }

    private void populateViews(){
        final String url = WirelessApp.getBaseUrl() + "user/user/"+mIdentifier;

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
                        String status = result.getString("Status");
                        if (status.equals("OK")){
                            JSONObject user = result.getJSONObject("user");
                            nameEditText.setText(user.getString("name"));
                            authorizeSwitch.setChecked(user.getBoolean("authorized"));
                            notifySwitch.setChecked(user.getBoolean("notify"));
                        }else{
                            Toast.makeText(getActivity(), "Could not retrieve user", Toast.LENGTH_SHORT).show();
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

    private void saveUser(){
        final String url = WirelessApp.getBaseUrl() + "user/edit";
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("identifier", mIdentifier);
                    obj.put("name", nameEditText.getText().toString());
                    obj.put("authorized", authorizeSwitch.isChecked());
                    obj.put("notify", notifySwitch.isChecked());
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
                            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
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

    private void deleteUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete user? This will log out any other user with this user id")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteUserAtServer();

                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteUserAtServer(){
        final String url = WirelessApp.getBaseUrl() + "user/delete/"+mIdentifier;

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
                        String status = result.getString("Status");
                        if (status.equals("OK")){
                            Toast.makeText(getActivity(), "User deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), UserSelectActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getActivity(), "Delete failed", Toast.LENGTH_SHORT).show();
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
}
