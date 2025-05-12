package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager);


        Button buttonBoard = findViewById(R.id.buttonBoard);
        Button buttonProfile = findViewById(R.id.buttonProfile);
        Button buttonHelp = findViewById(R.id.buttonHelp);
        Button buttonForm = findViewById(R.id.buttonForm);
        Button buttonViewHelp = findViewById(R.id.buttonViewHelp);
        Button buttonPublish = findViewById(R.id.buttonPublish);


        buttonBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(ManagerActivity.this, ViewBuildingNotificationsActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(ManagerActivity.this, UserProfileActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(ManagerActivity.this, RequestHelpActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        buttonForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(ManagerActivity.this, ViewSubmittedRequestsActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });


        buttonViewHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(ManagerActivity.this, ViewOpenHelpRequestsActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(ManagerActivity.this, PublishBuildingNotificationActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });
    }
}