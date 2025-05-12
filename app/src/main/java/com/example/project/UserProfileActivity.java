package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private EditText editTextFullName;
    private TextView textViewEmail;
    private Button buttonSaveProfile;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        editTextFullName = findViewById(R.id.editTextFullName);
        textViewEmail = findViewById(R.id.textViewEmail);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            loadUserProfile();
        } else {
            // אם אין משתמש מחובר, יש לטפל במצב הזה (למשל, לחזור למסך ההתחברות)
            Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String email = mAuth.getCurrentUser().getEmail();

                    editTextFullName.setText(fullName);
                    textViewEmail.setText(email);
                } else {
                    Toast.makeText(UserProfileActivity.this, "לא נמצאו פרטי משתמש.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserProfileActivity.this, "שגיאה בטעינת פרופיל: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        String newFullName = editTextFullName.getText().toString().trim();

        if (TextUtils.isEmpty(newFullName)) {
            editTextFullName.setError("יש להזין שם מלא.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newFullName);

        usersRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this, "הפרופיל עודכן בהצלחה.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "שגיאה בעדכון הפרופיל: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}