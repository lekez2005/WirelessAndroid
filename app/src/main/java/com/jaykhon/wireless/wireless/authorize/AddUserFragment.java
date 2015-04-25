package com.jaykhon.wireless.wireless.authorize;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jaykhon.wireless.wireless.R;

/**
 * Created by lekez2005 on 4/24/15.
 */
public class AddUserFragment extends Fragment {
    private EditText newUserIdView;
    private EditText newUserNameView;
    private CheckBox newUserAuthorized;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_user, container, false);

        newUserIdView = (EditText) rootView.findViewById(R.id.new_user_id);
        newUserNameView = (EditText) rootView.findViewById(R.id.new_user_name);
        newUserAuthorized = (CheckBox) rootView.findViewById(R.id.new_user_authorized);

        return rootView;

    }
}
