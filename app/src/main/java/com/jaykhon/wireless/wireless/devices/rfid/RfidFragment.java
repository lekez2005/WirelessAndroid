package com.jaykhon.wireless.wireless.devices.rfid;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.connect.Async;
import com.jaykhon.wireless.wireless.connect.Command;
import com.jaykhon.wireless.wireless.connect.ResultListener;
import com.jaykhon.wireless.wireless.connect.SendRequest;
import com.jaykhon.wireless.wireless.devices.ReloadFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RfidFragment extends Fragment implements ReloadFragment {

    private LinearLayout cardLayout;

    private static final String identifierArg = "identifier";

    private String identifier;

    public static RfidFragment newInstance(String param1) {
        RfidFragment fragment = new RfidFragment();
        Bundle args = new Bundle();
        args.putString(identifierArg, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public RfidFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            identifier = getArguments().getString(identifierArg);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rfid, container, false);
        cardLayout = (LinearLayout) view.findViewById(R.id.cards_linear_layout);
        reload();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void reload() {
        final String url = WirelessApp.getBaseUrl() + "rfid/"+identifier;
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
                    cardLayout.removeAllViews();
                    try {
                        JSONArray cards = result.getJSONArray("cards");
                        for (int i = 0; i < cards.length(); i++){
                            JSONObject card = cards.getJSONObject(i);
                            cardLayout.addView(Card.newInstance(getActivity(), card, RfidFragment.this));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(getActivity(), "Reload Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultsFail() {
            }
        }, getActivity()).execute();
    }
}
