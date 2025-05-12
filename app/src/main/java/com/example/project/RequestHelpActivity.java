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

public class RequestHelpActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextApartmentNumber, editTextHelpRequestContent;
    private Button buttonPublishHelpRequest;
    private DatabaseReference helpRequestsRef;
    private String currentBuildingCode;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_help);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextApartmentNumber = findViewById(R.id.editTextApartmentNumber);
        editTextHelpRequestContent = findViewById(R.id.editTextHelpRequestContent);
        buttonPublishHelpRequest = findViewById(R.id.buttonPublishHelpRequest);

        // קבל את מזהה המשתמש הנוכחי
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // קבל את קוד הבניין של המשתמש הנוכחי
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentBuildingCode = snapshot.child("buildingCode").getValue(String.class);
                        if (currentBuildingCode != null) {
                            helpRequestsRef = FirebaseDatabase.getInstance().getReference("help_requests").child(currentBuildingCode);
                        } else {
                            Toast.makeText(RequestHelpActivity.this, "לא נמצא קוד בניין עבור משתמש זה.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(RequestHelpActivity.this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RequestHelpActivity.this, "שגיאה בקבלת קוד בניין: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(RequestHelpActivity.this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonPublishHelpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = editTextFullName.getText().toString().trim();
                String apartmentNumber = editTextApartmentNumber.getText().toString().trim();
                String content = editTextHelpRequestContent.getText().toString().trim();

                if (TextUtils.isEmpty(fullName)) {
                    editTextFullName.setError("יש להזין שם מלא");
                    return;
                }

                if (TextUtils.isEmpty(apartmentNumber)) {
                    editTextApartmentNumber.setError("יש להזין מספר דירה");
                    return;
                }

                if (TextUtils.isEmpty(content)) {
                    editTextHelpRequestContent.setError("יש להזין תיאור לבקשת העזרה");
                    return;
                }

                if (helpRequestsRef != null) {
                    String requestId = helpRequestsRef.push().getKey();
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String date = dateFormat.format(calendar.getTime());
                    String time = timeFormat.format(calendar.getTime());
                    long timestamp = calendar.getTimeInMillis();

                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("fullName", fullName);
                    requestData.put("apartmentNumber", apartmentNumber);
                    requestData.put("content", content);
                    requestData.put("date", date);
                    requestData.put("time", time);
                    requestData.put("timestamp", timestamp);
                    requestData.put("isOpen", true);
                    requestData.put("publisherId", currentUserId); // שמירת מזהה המפרסם

                    helpRequestsRef.child(requestId).setValue(requestData)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RequestHelpActivity.this, "בקשת עזרה פורסמה בהצלחה", Toast.LENGTH_SHORT).show();
                                    editTextFullName.setText("");
                                    editTextApartmentNumber.setText("");
                                    editTextHelpRequestContent.setText("");
                                    // כאן צריך להוסיף קוד לשליחת התראה FCM
                                } else {
                                    Toast.makeText(RequestHelpActivity.this, "שגיאה בפרסום בקשת העזרה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(RequestHelpActivity.this, "שגיאה: לא ניתן לפרסם בקשת עזרה כרגע.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}