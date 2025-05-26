package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

/**
 * An activity for managing user profiles. Users can view their email,
 * update their full name, and log out of the application.
 * This activity interacts with Firebase Authentication and Realtime Database
 * to load and save user-specific data.
 */
public class UserProfileActivity extends AppCompatActivity {

    private EditText editTextFullName;
    private TextView textViewEmail;
    private Button buttonSaveProfile;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;
    private Button logoutButton;

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up Firebase authentication,
     * and handles user login status to load profile data or redirect.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b>Note: Otherwise it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize UI components
        editTextFullName = findViewById(R.id.editTextFullName);
        textViewEmail = findViewById(R.id.textViewEmail);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        progressBar = findViewById(R.id.progressBar);
        logoutButton = findViewById(R.id.logoutButton);

        // Set up click listener for the logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user from Firebase
                mAuth.signOut();

                // After logout, navigate to the Login screen
                Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                // Clear all previous activities from the stack
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // Get a reference to the current user's data in the Firebase Realtime Database
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            loadUserProfile(); // Load the user's profile data
        } else {
            // If no user is logged in, handle this situation (e.g., redirect to login screen)
            Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show(); // User not logged in.
            finish(); // Close the activity
        }

        // Set up click listener for the save profile button
        buttonSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile(); // Save the updated user profile
            }
        });
    }

    /**
     * Loads the current user's profile data (full name and email) from Firebase.
     * Displays a progress bar while loading and a toast message if data is not found or an error occurs.
     */
    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    // The email is directly from FirebaseUser, not from the database
                    String email = mAuth.getCurrentUser().getEmail();

                    editTextFullName.setText(fullName);
                    textViewEmail.setText(email);
                } else {
                    Toast.makeText(UserProfileActivity.this, "לא נמצאו פרטי משתמש.", Toast.LENGTH_SHORT).show(); // User details not found.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserProfileActivity.this, "שגיאה בטעינת פרופיל: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show(); // Error loading profile:
            }
        });
    }

    /**
     * Saves the updated full name to the current user's profile in Firebase.
     * Validates the input field and displays a progress bar during the save operation.
     * Shows a toast message indicating success or failure of the update.
     */
    private void saveUserProfile() {
        String newFullName = editTextFullName.getText().toString().trim();

        if (TextUtils.isEmpty(newFullName)) {
            editTextFullName.setError("יש להזין שם מלא."); // Please enter full name.
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newFullName);

        // Update the 'fullName' field in the user's database entry
        usersRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this, "הפרופיל עודכן בהצלחה.", Toast.LENGTH_SHORT).show(); // Profile updated successfully.
                    } else {
                        Toast.makeText(UserProfileActivity.this, "שגיאה בעדכון הפרופיל: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Error updating profile:
                    }
                });
    }
}