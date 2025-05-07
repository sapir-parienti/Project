package com.example.project;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class RequestHelpActivity extends AppCompatActivity {

    private EditText fullNameEditText;
    private EditText apartmentNumberEditText;
    private EditText helpDetailsEditText;
    private Button requestHelpButton;
    private Button cancelHelpRequestButton;
    private DatabaseReference helpRequestsRef;
    private FirebaseUser currentUser;
    private String deviceToken;
    private String currentRequestId; // To store the ID of the active request

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_help);

        fullNameEditText = findViewById(R.id.fullNameEditText);
        apartmentNumberEditText = findViewById(R.id.apartmentNumberEditText);
        helpDetailsEditText = findViewById(R.id.helpDetailsEditText);
        requestHelpButton = findViewById(R.id.requestHelpButton);
        cancelHelpRequestButton = findViewById(R.id.cancelHelpRequestButton);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        helpRequestsRef = database.getReference("help_requests");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get FCM device token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        deviceToken = task.getResult();
                    }
                });

        // Check if there's an existing help request for the current user
        if (currentUser != null) {
            helpRequestsRef.orderByChild("userId").equalTo(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // User has an active request
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    currentRequestId = snapshot.getKey();
                                    helpDetailsEditText.setText(snapshot.child("details").getValue(String.class));
                                    fullNameEditText.setText(snapshot.child("fullName").getValue(String.class));
                                    String apartment = snapshot.child("apartmentNumber").getValue(String.class);
                                    if (apartment != null) {
                                        apartmentNumberEditText.setText(apartment);
                                    }
                                    requestHelpButton.setVisibility(View.GONE);
                                    cancelHelpRequestButton.setVisibility(View.VISIBLE);
                                }
                            } else {
                                // No active request
                                requestHelpButton.setVisibility(View.VISIBLE);
                                cancelHelpRequestButton.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
        }

        requestHelpButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String apartmentNumber = apartmentNumberEditText.getText().toString().trim();
            String helpDetails = helpDetailsEditText.getText().toString().trim();

            if (TextUtils.isEmpty(fullName)) {
                fullNameEditText.setError("יש להזין שם מלא");
                return;
            }

            if (TextUtils.isEmpty(helpDetails)) {
                helpDetailsEditText.setError("יש להזין פרטים על הבקשה");
                return;
            }

            if (currentUser != null && deviceToken != null) {
                String userId = currentUser.getUid();
                String requestId = helpRequestsRef.push().getKey();
                currentRequestId = requestId; // Store the request ID

                Map<String, Object> requestData = new HashMap<>();
                requestData.put("userId", userId);
                requestData.put("fullName", fullName);
                requestData.put("apartmentNumber", apartmentNumber);
                requestData.put("details", helpDetails);
                requestData.put("timestamp", System.currentTimeMillis());
                requestData.put("deviceToken", deviceToken);

                helpRequestsRef.child(requestId).setValue(requestData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RequestHelpActivity.this, "בקשת העזרה נשלחה!", Toast.LENGTH_SHORT).show();
                                requestHelpButton.setVisibility(View.GONE);
                                cancelHelpRequestButton.setVisibility(View.VISIBLE);
                                sendHelpRequestNotification(fullName, helpDetails);
                            } else {
                                Toast.makeText(RequestHelpActivity.this, "שליחת בקשת העזרה נכשלה.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(RequestHelpActivity.this, "משתמש לא מחובר או טוקן מכשיר לא זמין.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelHelpRequestButton.setOnClickListener(v -> {
            if (currentRequestId != null) {
                helpRequestsRef.child(currentRequestId).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RequestHelpActivity.this, "בקשת העזרה בוטלה.", Toast.LENGTH_SHORT).show();
                                requestHelpButton.setVisibility(View.VISIBLE);
                                cancelHelpRequestButton.setVisibility(View.GONE);
                                currentRequestId = null;
                                fullNameEditText.setText("");
                                apartmentNumberEditText.setText("");
                                helpDetailsEditText.setText("");
                            } else {
                                Toast.makeText(RequestHelpActivity.this, "ביטול בקשת העזרה נכשל.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(RequestHelpActivity.this, "לא קיימת בקשת עזרה פעילה לביטול.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendHelpRequestNotification(String senderName, String message) {
        // ... (אותו קוד כמו בפעם הקודמת לשליחת נוטיפיקיישן דרך שרת/Firebase Function)
        String notificationTitle = "בקשת עזרה חדשה!";
        String notificationBody = senderName + " זקוק/ה לעזרה: " + message;

        Map<String, String> notificationPayload = new HashMap<>();
        notificationPayload.put("title", notificationTitle);
        notificationPayload.put("body", notificationBody);
        notificationPayload.put("requestId", currentRequestId);

        android.util.Log.d("FCM Notification Payload", notificationPayload.toString());
        Toast.makeText(this, "הודעת בקשת עזרה נשלחה (Notification ישלח דרך שרת)", Toast.LENGTH_LONG).show();
    }
}