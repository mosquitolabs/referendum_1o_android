package com.referendum.uoctubre.gcm;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.utils.StringsManager;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "referendum")
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(StringsManager.getString("notification_title"))
                .setContentText(remoteMessage.getNotification().getBody());

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notificationBuilder.build());
        }
    }
}