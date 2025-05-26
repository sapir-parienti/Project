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

/**
 * The `ManagerActivity` class serves as the main dashboard for managers.
 * It provides navigation to various functionalities such as viewing building notifications,
 * managing user profiles, viewing submitted requests, and publishing new notifications.
 */
public class ManagerActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created. This is where you should do all of your normal static set up:
     * create views, bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b>Note: Otherwise it is null.</b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager);

        // Initialize buttons from the layout
        Button buttonBoard = findViewById(R.id.buttonBoard);
        Button buttonProfile = findViewById(R.id.buttonProfile);
        Button buttonForm = findViewById(R.id.buttonForm);
        Button buttonPublish = findViewById(R.id.buttonPublish);

        /**
         * Sets an {@link View.OnClickListener} for the 'Board' button.
         * When clicked, it navigates the user to the {@link ViewBuildingNotificationsActivity}.
         */
        buttonBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to switch to ViewBuildingNotificationsActivity
                Intent intent = new Intent(ManagerActivity.this, ViewBuildingNotificationsActivity.class);
                // Start the new activity
                startActivity(intent);
            }
        });

        /**
         * Sets an {@link View.OnClickListener} for the 'Profile' button.
         * When clicked, it navigates the user to the {@link UserProfileActivity}.
         */
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to switch to UserProfileActivity
                Intent intent = new Intent(ManagerActivity.this, UserProfileActivity.class);
                // Start the new activity
                startActivity(intent);
            }
        });

        /**
         * Sets an {@link View.OnClickListener} for the 'Form' button.
         * When clicked, it navigates the user to the {@link ViewSubmittedRequestsActivity}.
         */
        buttonForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to switch to ViewSubmittedRequestsActivity
                Intent intent = new Intent(ManagerActivity.this, ViewSubmittedRequestsActivity.class);
                // Start the new activity
                startActivity(intent);
            }
        });

        /**
         * Sets an {@link View.OnClickListener} for the 'Publish' button.
         * When clicked, it navigates the user to the {@link PublishBuildingNotificationActivity}.
         */
        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to switch to PublishBuildingNotificationActivity
                Intent intent = new Intent(ManagerActivity.this, PublishBuildingNotificationActivity.class);
                // Start the new activity
                startActivity(intent);
            }
        });
    }
}