package com.example.motionalarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("using_location", false)) {
            Location location = intent.getParcelableExtra("location");
            makeNotification(intent.getStringExtra("title"), intent.getStringExtra("message") + "\nLatitude: " +
                    location.getLatitude() + "\nLongitude: " + location.getLongitude(), context, HomePage.NOTIFICATION_ID);
        } else {
            makeNotification(intent.getStringExtra("title"), intent.getStringExtra("message"), context, HomePage.NOTIFICATION_ID);
        }
    }

    private void makeNotification(String title, String text, Context context, String channel_id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(R.drawable.android)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setGroup(HomePage.GROUP_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat.from(context).notify(count++, builder.build());
    }
}
