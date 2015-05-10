package com.jaykhon.wireless.wireless.devices.alarm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.jaykhon.wireless.wireless.WirelessApp;
import com.jaykhon.wireless.wireless.utils.Json;

import org.json.JSONException;

/**
 * Created by lekez2005 on 4/29/15.
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder>{

    public Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public AlarmCard card;
        public ViewHolder(AlarmCard v){
            super(v);
            card = v;
        }
    }

    public AlarmAdapter(Context context){
        mContext = context;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AlarmCard card = new AlarmCard(mContext);
        ViewHolder vh = new ViewHolder(card);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (WirelessApp.getDevices() != null){
            try {
                holder.card.setCard(WirelessApp.getDevices().getJSONArray(Json.ALARM).getJSONObject(position));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int getItemCount() {
        if (WirelessApp.getDevices() != null){
            try {
                return WirelessApp.getDevices().getJSONArray(Json.ALARM).length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

}