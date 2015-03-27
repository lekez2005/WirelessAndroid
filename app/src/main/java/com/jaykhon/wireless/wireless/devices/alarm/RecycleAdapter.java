package com.jaykhon.wireless.wireless.devices.alarm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lekez2005 on 3/26/15.
 */
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    private HashMap<String, String> detectors;
    private ArrayList<String> identifiers;
    private ArrayList<String> names;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public DetectorCard card;
        public ViewHolder(DetectorCard v){
            super(v);
            card = v;
        }
    }

    public RecycleAdapter(Context mContext, HashMap<String, String> pairedDetectors){
        identifiers = new ArrayList<>();
        names = new ArrayList<>();
        setDetectors(pairedDetectors);
        context = mContext;
    }

    public void setDetectors(HashMap<String, String> d){
        detectors = d;
        identifiers.clear();
        names.clear();
        for (Map.Entry<String, String> entry: detectors.entrySet()){
            identifiers.add(entry.getKey());
            names.add(entry.getValue());
        }
    }

    @Override
    public RecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        DetectorCard c = new DetectorCard(context);
        ViewHolder vh = new ViewHolder(c);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position){
        viewHolder.card.prettyNameView.setText(names.get(position));
    }

    @Override
    public int getItemCount(){
        return detectors.size();
    }
}
