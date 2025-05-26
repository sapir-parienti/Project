package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * PublishBuildingNotificationActivity allows users to publish notifications to their building community.
 *
 * This activity provides an interface for authenticated users to create and publish notifications
 * that will be visible to all residents of their building. The activity handles user authentication,
 * retrieves user details from Firebase, validates input, and publishes notifications with
 * comprehensive metadata including timestamps and publisher information.
 *
 * Features:
 * - User authentication verification
 * - Automatic retrieval of user details (building code, full name, apartment number)
 * - Input validation for notification content
 * - Timestamp generation with date and time formatting
 * - Progress indicators during database operations
 * - Error handling and user feedback
 * - Notification publishing to building-specific channels
 *
 * The notification data structure includes:
 * - Publisher ID and personal details
 * - Notification content
 * - Publication date and time
 * - Server timestamp for ordering
 *
 * @author [Your Name]
 * @version 1.0
 * @since 1.0
 */
public class PublishBuildingNotificationActivity extends AppCompatActivity {

    /** Text input field for notification content */
    private EditText editTextNotificationContent;

    /** Button to trigger notification publishing */
    private Button buttonPublishNotification;

    /** Progress indicator for loading states during database operations */
    private ProgressBar progressBar;

    /** Firebase Authentication instance for user verification */
    private FirebaseAuth mAuth;

    /** Firebase Database reference for accessing user data */
    private DatabaseReference usersRef;

    /** Firebase Database reference for publishing building notifications */
    private DatabaseReference buildingNotificationsRef;

    /** Current authenticated user's unique identifier */
    private String currentUserId;

    /** Building code associated with the current user */
    private String currentBuildingCode;

    /** Full name of the current user for notification attribution */
    private String currentUserFullName;

    /** Apartment number of the current user for identification */
    private String currentUserApartmentNumber;

    /**
     * Called when the activity is first created. Initializes the notification publishing interface
     * and sets up Firebase services for user authentication and data operations.
     *
     * This method performs the following operations:
     * 1. Sets up the activity layout and UI components
     * 2. Initializes Firebase Authentication and verifies user login status
     * 3. Retrieves current user details from Firebase Database
     * 4. Sets up click listeners for the publish button
     * 5. Handles cases where user is not authenticated
     *
     * If the user is not authenticated, the activity will display an error message
     * and terminate. Otherwise, it proceeds to fetch user details required for
     * notification publishing.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains
     *                          the data it most recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_building_notification);

        editTextNotificationContent = findViewById(R.id.editTextNotificationContent);
        buttonPublishNotification = findViewById(R.id.buttonPublishNotification);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            getUserDetails();
        } else {
            Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonPublishNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishNotification();
            }
        });
    }

    /**
     * Retrieves the current user's details from Firebase Realtime Database.
     *
     * This method fetches essential user information required for notification publishing:
     * - Building code: Used to determine which building's notification channel to publish to
     * - Full name: Used for notification attribution and identification
     * - Apartment number: Provides additional context for other residents
     *
     * The method includes comprehensive error handling:
     * - Shows progress indicator during database operation
     * - Handles cases where user data doesn't exist
     * - Manages database operation cancellations
     * - Terminates activity if critical data is missing
     *
     * Upon successful retrieval, initializes the building notifications reference
     * for the user's specific building.
     */
    private void getUserDetails() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    currentBuildingCode = dataSnapshot.child("buildingCode").getValue(String.class);
                    currentUserFullName = dataSnapshot.child("fullName").getValue(String.class);
                    currentUserApartmentNumber = dataSnapshot.child("apartmentNumber").getValue(String.class);
                    if (currentBuildingCode != null) {
                        buildingNotificationsRef = FirebaseDatabase.getInstance().getReference("building_notifications").child(currentBuildingCode);
                    } else {
                        Toast.makeText(PublishBuildingNotificationActivity.this, "קוד בניין לא נמצא.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(PublishBuildingNotificationActivity.this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PublishBuildingNotificationActivity.this, "שגיאה בקבלת פרטי משתמש: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Publishes a notification to the building's notification channel in Firebase.
     *
     * This method handles the complete notification publishing process:
     * 1. Validates that notification content is not empty
     * 2. Verifies that all required user details are available
     * 3. Generates a unique notification ID using Firebase's push() method
     * 4. Creates timestamp information (date, time, and server timestamp)
     * 5. Constructs a comprehensive notification data object
     * 6. Uploads the notification to Firebase Database
     * 7. Provides user feedback on success or failure
     * 8. Clears the input field after successful publication
     *
     * The notification data structure includes:
     * - publisherId: Unique identifier of the user publishing the notification
     * - fullName: Display name for notification attribution
     * - apartmentNumber: Additional context for other building residents
     * - content: The actual notification message
     * - date: Human-readable date in DD/MM/YYYY format
     * - time: Human-readable time in HH:MM format
     * - timestamp: Server-side timestamp for consistent ordering
     *
     * Error handling covers:
     * - Empty notification content validation
     * - Missing user details verification
     * - Database operation failures
     * - Network connectivity issues
     */
    private void publishNotification() {
        String notificationContent = editTextNotificationContent.getText().toString().trim();

        if (TextUtils.isEmpty(notificationContent)) {
            editTextNotificationContent.setError("יש להזין תוכן להודעה.");
            return;
        }

        if (buildingNotificationsRef != null && currentBuildingCode != null && currentUserId != null && currentUserFullName != null) {
            progressBar.setVisibility(View.VISIBLE);

            String notificationId = buildingNotificationsRef.push().getKey();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentDate = dateFormat.format(calendar.getTime());
            String currentTime = timeFormat.format(calendar.getTime());
            long timestamp = ServerValue.TIMESTAMP.size();

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("publisherId", currentUserId);
            notificationData.put("fullName", currentUserFullName);
            notificationData.put("apartmentNumber", currentUserApartmentNumber);
            notificationData.put("content", notificationContent);
            notificationData.put("date", currentDate);
            notificationData.put("time", currentTime);
            notificationData.put("timestamp", timestamp);

            buildingNotificationsRef.child(notificationId).setValue(notificationData)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(PublishBuildingNotificationActivity.this, "ההודעה פורסמה בהצלחה.", Toast.LENGTH_SHORT).show();
                            editTextNotificationContent.setText(""); // ניקוי שדה ההודעה לאחר הפרסום
                        } else {
                            Toast.makeText(PublishBuildingNotificationActivity.this, "שגיאה בפרסום ההודעה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "לא ניתן לפרסם הודעה כרגע.", Toast.LENGTH_SHORT).show();
        }
    }
}