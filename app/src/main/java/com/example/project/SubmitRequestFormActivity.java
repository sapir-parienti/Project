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

public class SubmitRequestFormActivity extends AppCompatActivity {

    private EditText editTextRequestContent;
    private EditText editTextFullName;
    private EditText editTextApartmentNumber;
    private Button buttonSubmitRequest;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference requestsRef;
    private DatabaseReference usersRef;
    private String currentUserId;
    private String currentBuildingCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_request_form);

        editTextRequestContent = findViewById(R.id.editTextRequestContent);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextApartmentNumber = findViewById(R.id.editTextApartmentNumber);
        buttonSubmitRequest = findViewById(R.id.buttonSubmitRequest);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            getUserBuildingCode();
        } else {
            Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonSubmitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRequest();
            }
        });
    }

    private void getUserBuildingCode() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    currentBuildingCode = dataSnapshot.child("buildingCode").getValue(String.class);
                    if (currentBuildingCode != null) {
                        requestsRef = FirebaseDatabase.getInstance().getReference("help_requests").child(currentBuildingCode);
                    } else {
                        Toast.makeText(SubmitRequestFormActivity.this, "קוד בניין לא נמצא.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(SubmitRequestFormActivity.this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SubmitRequestFormActivity.this, "שגיאה בקבלת קוד בניין: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void submitRequest() {
        String requestContent = editTextRequestContent.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String apartmentNumber = editTextApartmentNumber.getText().toString().trim();

        if (TextUtils.isEmpty(requestContent)) {
            editTextRequestContent.setError("יש להזין את תוכן הפנייה.");
            return;
        }

        if (requestsRef != null && currentBuildingCode != null && currentUserId != null) {
            progressBar.setVisibility(View.VISIBLE);

            String requestId = requestsRef.push().getKey();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentDate = dateFormat.format(calendar.getTime());
            String currentTime = timeFormat.format(calendar.getTime());
            long timestamp = ServerValue.TIMESTAMP.size();

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("content", requestContent);
            requestData.put("fullName", TextUtils.isEmpty(fullName) ? "אנונימי" : fullName);
            requestData.put("apartmentNumber", TextUtils.isEmpty(apartmentNumber) ? "" : apartmentNumber);
            requestData.put("date", currentDate);
            requestData.put("time", currentTime);
            requestData.put("timestamp", timestamp);
            requestData.put("isOpen", true);
            requestData.put("publisherId", currentUserId);

            // קבלת שם מלא ומספר דירה מהמשתמש עצמו (אם קיים)
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String publisherFullName = snapshot.child("fullName").getValue(String.class);
                    String publisherApartmentNumber = snapshot.child("apartmentNumber").getValue(String.class);

                    requestData.put("fullName", TextUtils.isEmpty(fullName) ? (publisherFullName != null ? publisherFullName : "אנונימי") : fullName);
                    requestData.put("apartmentNumber", TextUtils.isEmpty(apartmentNumber) ? (publisherApartmentNumber != null ? publisherApartmentNumber : "") : apartmentNumber);

                    requestsRef.child(requestId).setValue(requestData)
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(SubmitRequestFormActivity.this, "הפנייה הוגשה בהצלחה.", Toast.LENGTH_SHORT).show();
                                    finish(); // סגור את המסך לאחר ההגשה
                                } else {
                                    Toast.makeText(SubmitRequestFormActivity.this, "שגיאה בהגשת הפנייה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SubmitRequestFormActivity.this, "שגיאה בקבלת פרטי משתמש: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "לא ניתן להגיש את הפנייה כרגע.", Toast.LENGTH_SHORT).show();
        }
    }
}