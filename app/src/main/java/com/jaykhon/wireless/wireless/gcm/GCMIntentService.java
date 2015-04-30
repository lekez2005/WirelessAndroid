package com.jaykhon.wireless.wireless.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jaykhon.wireless.wireless.MainActivity;
import com.jaykhon.wireless.wireless.R;

/**
 * Created by lekez2005 on 4/3/15.
 */
public class GCMIntentService extends IntentService {

    private static final String MESSAGE_STATUS = "status";
    private static final String IDENTIFIER = "identifier";
    private static final String DEVICE = "device";
    private static final String MESSAGE = "message";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    String TAG = "GcmIntentService";

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                extras.putString(MESSAGE_STATUS, "Send Error");
                sendNotification(extras);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                extras.putString(MESSAGE_STATUS, "Deleted messages on server");
                sendNotification(extras);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                sendNotification(extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle extras) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.IDENTIFIER_KEY, extras.getString(IDENTIFIER, ""));
        intent.putExtra(MainActivity.DEVICE_KEY, extras.getString(DEVICE, ""));
        intent.setAction("com.jaykhon.wireless.wireless.MainActivity");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        long[] pattern = {500,500,500,500,500,500,500,500,500};

        String message = extras.getString(MESSAGE, "Detector " + extras.getString(IDENTIFIER, "") + " triggered");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_action_refresh)
                                .setContentTitle("Alert")
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(message))
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setAutoCancel(true)
                                .setVibrate(pattern)
        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        //Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
        Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mBuilder.setSound(alarmTone);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
