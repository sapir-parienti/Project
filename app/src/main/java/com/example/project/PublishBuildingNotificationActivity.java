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

public class PublishBuildingNotificationActivity extends AppCompatActivity {

    private EditText editTextNotificationContent;
    private Button buttonPublishNotification;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private DatabaseReference buildingNotificationsRef;
    private String currentUserId;
    private String currentBuildingCode;
    private String currentUserFullName;
    private String currentUserApartmentNumber;

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

    private void publishNotification() {
        String notificationContent = editTextNotificationContent.getText().toString().trim();

        if (TextUtils.isEmpty(notificationContent)) {
            editTextNotificationContent.setError("יש להזין תוכן להודעה.");
            return;
        }

        if (buildingNotificationsRef != null && currentBuildingCode != null && currentUserId != null && currentUserFullName != null && currentUserApartmentNumber != null) {
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