package com.example.project;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * A {@link BroadcastReceiver} that handles daily notification reminders.
 * This receiver is triggered by an alarm and displays a notification to the user,
 * reminding them to check for new messages from the building committee.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "daily_reminder_channel";
    private static final int NOTIFICATION_ID = 123;

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * This method is triggered when the daily reminder alarm goes off.
     * It logs the alarm trigger and then calls {@link #showNotification(Context)} to display the notification.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm triggered, showing notification");
        showNotification(context);
    }

    /**
     * Displays a daily reminder notification to the user.
     * This method first ensures that a notification channel is created (for Android Oreo and above),
     * then builds and displays the notification.
     *
     * @param context The Context in which the notification should be shown.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void showNotification(Context context) {
        createNotificationChannel(context);

        // Intent to open the main application activity when the notification is clicked
        Intent appIntent = new Intent(context, MainActivity.class); // Replace with your main Activity
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a suitable icon
                .setContentTitle("תזכורת יומית!") // Daily Reminder!
                .setContentText("אל תשכחו לבדוק הודעות חדשות מועד הבית!") // Don't forget to check for new messages from the building committee!
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Notification will disappear when the user clicks on it

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.d(TAG, "Notification shown");
    }

    /**
     * Creates a notification channel for daily reminders.
     * This is required for notifications on Android Oreo (API level 26) and higher.
     *
     * @param context The Context used to get the NotificationManager.
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "תזכורת יומית"; // Daily Reminder
            String description = "ערוץ עבור תזכורות יומיות לאפליקציה"; // Channel for daily reminders for the app
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }
}