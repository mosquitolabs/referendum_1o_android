package com.referendum.uoctubre;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.referendum.uoctubre.main.Constants;
import com.referendum.uoctubre.utils.StringsManager;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import io.fabric.sdk.android.Fabric;


public class UOctubreApplication extends Application {

    private static UOctubreApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        Fabric.with(this, new Crashlytics());

        StringsManager.initialize();

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);

        //Create Notification channel in Android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel mChannel = new NotificationChannel("referendum",
                        StringsManager.getString("notification_channel_name"), NotificationManager.IMPORTANCE_HIGH);
                mChannel.setDescription(StringsManager.getString("notification_channel_description"));
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.RED);
                notificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    public static UOctubreApplication getInstance() {
        return sInstance;
    }
}
