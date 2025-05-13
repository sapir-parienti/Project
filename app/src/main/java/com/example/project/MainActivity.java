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


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonBoard = findViewById(R.id.buttonBoard);
        Button buttonProfile = findViewById(R.id.buttonProfile);
        Button buttonHelp = findViewById(R.id.buttonHelp);
        Button buttonForm = findViewById(R.id.buttonForm);


        buttonBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(MainActivity.this, ViewBuildingNotificationsActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(MainActivity.this, RequestHelpActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        buttonForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(MainActivity.this, SubmitRequestFormActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });




        setDailyAlarm();
    }

    private void setDailyAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // הגדר שעת התחלה 16:00
        int hour = 16;
        int minute = 0;

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Log.d(TAG, "Setting alarm for: " + calendar.getTime().toString());


        // חזור על האזעקה כל יום (INTERVAL_DAY הוא קבוע שמייצג 24 שעות במילישניות)
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        Log.d(TAG, "Daily alarm scheduled");
    }


}