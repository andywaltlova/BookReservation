package com.andy.bookreservations.utils;


import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.andy.bookreservations.MainActivity;
import com.andy.bookreservations.R;

public class Notifications extends Application {
    private static final String CHANNEL_ID = "channel_1";
    private NotificationManager notificationManager;
    private Context activityContex;
    private boolean shouldNotify;
    private boolean lateNotif;
    private boolean workDoneNotif;
    private boolean workDoneNotifSend = false;


    public Notifications(Context activityContext, boolean shouldNotify, boolean workDoneNotif) {
        this.activityContex = activityContext;
        this.notificationManager = activityContex.getSystemService(NotificationManager.class);
        this.shouldNotify = shouldNotify;
        this.workDoneNotif = workDoneNotif;
        createNotificationChannel();
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "ÚK FF MU - Book requests", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifications on new book requests");
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(MODE_APPEND);
        notificationManager.createNotificationChannel(channel);
    }

    public void sendNotification(String textTitle, String textMessage) {
        if (shouldNotify) {
            final Intent intent = new Intent(activityContex, MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(activityContex, 0, intent, 0);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(activityContex, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_import_contacts_black_24dp)
                    .setContentTitle(textTitle)
                    .setContentText(textMessage)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            notificationManager.notify(1, notification.build());
        }
    }

    public void setShouldNotify(boolean shouldNotify) {
        this.shouldNotify = shouldNotify;
    }

    public void setLateNotif(boolean lateNotif) {
        this.lateNotif = lateNotif;
    }

    public void setWorkDoneNotif(boolean donedNotif) {
        this.workDoneNotif = donedNotif;
    }

    public boolean isLateNotif() {
        return lateNotif;
    }

    public boolean isWorkDoneNotif() {
        return workDoneNotif;
    }

    public boolean isWorkDoneNotifSend() {
        return workDoneNotifSend;
    }

    public void setWorkDoneNotifSend(boolean workDoneNotifSend) {
        this.workDoneNotifSend = workDoneNotifSend;
    }
}

