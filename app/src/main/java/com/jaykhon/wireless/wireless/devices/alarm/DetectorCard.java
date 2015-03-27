package com.jaykhon.wireless.wireless.devices.alarm;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jaykhon.wireless.wireless.R;

/**
 * Created by lekez2005 on 3/26/15.
 */
public class DetectorCard extends CardView {

    public TextView prettyNameView;
    private ImageButton deleteButton;

    public DetectorCard(final Context context){
        super(context);
        LayoutInflater.from(context).inflate(R.layout.alarm_detector_card, this);
        prettyNameView = (TextView) findViewById(R.id.detector_view);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Testing click", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
