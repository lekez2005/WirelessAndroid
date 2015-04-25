package com.jaykhon.wireless.wireless.authorize;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lekez2005 on 4/24/15.
 */
public class AddUserFragment extends Fragment {

    OnSwitchFragmentListener mCallback;

    private static final String IDENTIFIER = "identifier";
    private static final String TOKEN = "token";
    private static final String NAME = "name";

    private EditText newUserIdView;
    private EditText newUserNameView;
    private CheckBox newUserAuthorized;
    private Button saveUserButton;
    private Button selectUserButton;

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

        View rootView = inflater.inflate(R.layout.fragment_add_user, container, false);

        newUserIdView = (EditText) rootView.findViewById(R.id.new_user_id);
        newUserNameView = (EditText) rootView.findViewById(R.id.new_user_name);
        newUserAuthorized = (CheckBox) rootView.findViewById(R.id.new_user_authorized);

        saveUserButton = (Button) rootView.findViewById(R.id.save_user_button);
        saveUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();
            }
        });

        selectUserButton = (Button) rootView.findViewById(R.id.select_user_button);
        selectUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSwitchFragment(UserSelectActivity.SELECT_USER);
            }
        });

        return rootView;

    }

    private void saveUser(){
        final String url = WirelessApp.getBaseUrl()+"user/register";
        new Async<Void, Void, JSONObject>(new Command<JSONObject>() {
            @Override
            public JSONObject execute() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put(IDENTIFIER, newUserIdView.getText().toString());
                    obj.put(NAME, newUserNameView.getText().toString());
                    obj.put(TOKEN, new SessionIdentifierGenerator().nextSessionId());
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
                            Toast.makeText(getActivity(), "User Created", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), result.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getActivity(), "User creation Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, getActivity()).execute();
    }
}
