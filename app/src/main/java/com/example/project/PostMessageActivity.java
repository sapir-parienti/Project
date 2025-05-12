package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostMessageActivity extends AppCompatActivity {

    private EditText editTextNotificationContent;
    private Button buttonPublishNotification;
    private DatabaseReference notificationsRef;
    private String currentBuildingCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_message);

        editTextNotificationContent = findViewById(R.id.editTextNotificationContent);
        buttonPublishNotification = findViewById(R.id.buttonPublishNotification);

        // קבל את קוד הבניין של המשתמש הנוכחי
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentBuildingCode = snapshot.child("buildingCode").getValue(String.class);
                        if (currentBuildingCode != null) {
                            notificationsRef = FirebaseDatabase.getInstance().getReference("building_notifications").child(currentBuildingCode);
                        } else {
                            Toast.makeText(PostMessageActivity.this, "לא נמצא קוד בניין עבור משתמש זה.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(PostMessageActivity.this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PostMessageActivity.this, "שגיאה בקבלת קוד בניין: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(PostMessageActivity.this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonPublishNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editTextNotificationContent.getText().toString().trim();

                if (TextUtils.isEmpty(content)) {
                    editTextNotificationContent.setError("יש להזין תוכן להודעה");
                    return;
                }

                if (notificationsRef != null) {
                    String notificationId = notificationsRef.push().getKey();
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String date = dateFormat.format(calendar.getTime());
                    String time = timeFormat.format(calendar.getTime());
                    long timestamp = calendar.getTimeInMillis();

                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("content", content);
                    notificationData.put("date", date);
                    notificationData.put("time", time);
                    notificationData.put("timestamp", timestamp);

                    notificationsRef.child(notificationId).setValue(notificationData)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PostMessageActivity.this, "הודעה פורסמה בהצלחה", Toast.LENGTH_SHORT).show();
                                    editTextNotificationContent.setText("");
                                } else {
                                    Toast.makeText(PostMessageActivity.this, "שגיאה בפרסום ההודעה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(PostMessageActivity.this, "שגיאה: לא ניתן לפרסם הודעה כרגע.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}