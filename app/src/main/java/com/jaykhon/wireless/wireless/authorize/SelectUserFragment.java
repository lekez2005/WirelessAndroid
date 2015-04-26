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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.MainActivity;
import com.jaykhon.wireless.wireless.R;
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

import java.util.ArrayList;

/**
 * Created by lekez2005 on 4/24/15.
 */
public class SelectUserFragment  extends Fragment{

    OnSwitchFragmentListener mCallback;

    private Spinner usersSpinner;
    private ArrayAdapter<CharSequence> spinnerAdapter;
    private Button switchButton;
    private Button addUserButton;
    private Button editUserButton;


    private ArrayList<String> userIds;
    private ArrayList<String> userNames;

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
        View rootView = inflater.inflate(R.layout.fragment_select_user, container, false);

        usersSpinner = (Spinner) rootView.findViewById(R.id.users_spinner);
        spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usersSpinner.setAdapter(spinnerAdapter);

        userIds = new ArrayList<>();
        userNames = new ArrayList<>();

        switchButton = (Button) rootView.findViewById(R.id.switchButton);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchUser();
            }
        });

        editUserButton = (Button) rootView.findViewById(R.id.edit_user_button);
        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! spinnerAdapter.isEmpty()){
                    int pos = usersSpinner.getSelectedItemPosition();
                    String user = userIds.get(pos);
                    mCallback.onSwitchFragment(UserSelectActivity.EDIT_USER, user);
                }
            }
        });



        addUserButton = (Button) rootView.findViewById(R.id.add_user_button);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSwitchFragment(UserSelectActivity.ADD_USER, null);
            }
        });

        reload();

        return rootView;

    }

    private void switchUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Switch user? This will log out any other user with this user id")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                switchUserAtServer();

                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void switchUserAtServer(){
        final String token = new SessionIdentifierGenerator().nextSessionId();
        final String url = WirelessApp.getBaseUrl() + "user/update/token";
        final String identifier = userIds.get(usersSpinner.getSelectedItemPosition());


        final String gcm_id = GCMRegister.getRegistrationId(getActivity());
        if (gcm_id.isEmpty()){
            GCMRegister g = new GCMRegister(getActivity());
            g.register();
        }


        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("identifier", identifier);
                    obj.put("token", token);
                    obj.put("gcm_id", gcm_id);
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
                            Preferences p = new Preferences(getActivity());
                            p.setUserId(identifier);
                            p.setUserToken(token);
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

    private void reload(){
        final String url = WirelessApp.getBaseUrl() + "user/users";

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
                            getUsersFromJson(result);
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


    private void getUsersFromJson(JSONObject result){
        try {
            JSONArray users = result.getJSONArray("users");
            userIds.clear();
            userNames.clear();
            for (int i = 0; i< users.length(); i++){
                JSONObject user = users.getJSONObject(i);
                userIds.add(user.getString("identifier"));
                userNames.add(user.getString("name"));
            }
            spinnerAdapter.clear();
            spinnerAdapter.addAll(userNames);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
