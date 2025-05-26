package com.example.project;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

/**
 * MainActivity serves as the main entry point for the application.
 * This activity provides navigation to three main sections of the app:
 * building notifications, user profile, and request submission form.
 * It also sets up a daily notification alarm system.
 *
 * @author [Author Name]
 * @version 1.0
 * @since [Version]
 */
public class MainActivity extends AppCompatActivity {

    /** Tag used for logging purposes */
    private static final String TAG = "MainActivity";

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up button click listeners for navigation,
     * and configures the daily alarm notification system.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down then this Bundle contains
     *                          the data it most recently supplied in
     *                          {@link #onSaveInstanceState(Bundle)}.
     *                          <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        Button buttonBoard = findViewById(R.id.buttonBoard);
        Button buttonProfile = findViewById(R.id.buttonProfile);
        Button buttonForm = findViewById(R.id.buttonForm);

        /**
         * Button click listener for navigation to building notifications view.
         * Opens ViewBuildingNotificationsActivity when clicked.
         */
        buttonBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(MainActivity.this, ViewBuildingNotificationsActivity.class);
                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        /**
         * Button click listener for navigation to user profile.
         * Opens UserProfileActivity when clicked.
         */
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        /**
         * Button click listener for navigation to request submission form.
         * Opens SubmitRequestFormActivity when clicked.
         */
        buttonForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(MainActivity.this, SubmitRequestFormActivity.class);
                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        // Set up daily notification alarm
        setDailyAlarm();
    }

    /**
     * Sets up a daily repeating alarm that triggers at 4:00 PM every day.
     * The alarm uses AlarmManager.setInexactRepeating() for battery optimization
     * and triggers a NotificationReceiver broadcast receiver.
     *
     * <p><b>Note:</b> This alarm will not persist across device reboots.
     * For more reliable scheduling, consider using WorkManager.</p>
     *
     * <p><b>Warning:</b> If the current time is already past 4:00 PM when this method
     * is called, the alarm will trigger immediately rather than waiting for the next day.</p>
     *
     * @see AlarmManager#setInexactRepeating(int, long, long, PendingIntent)
     * @see NotificationReceiver
     */
    private void setDailyAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set start time to 4:00 PM (16:00)
        int hour = 16;
        int minute = 0;

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Log.d(TAG, "Setting alarm for: " + calendar.getTime().toString());

        // Repeat the alarm every day (INTERVAL_DAY represents 24 hours in milliseconds)
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        Log.d(TAG, "Daily alarm scheduled");
    }
}