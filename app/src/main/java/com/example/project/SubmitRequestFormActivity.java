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
 * An activity that allows users to submit help requests or general inquiries to the building manager.
 * Users can input the request content, their full name, and apartment number. The form handles
 * data validation and submission to a Firebase Realtime Database.
 */
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

    /**
     * Called when the activity is first created. This method initializes the UI components,
     * sets up Firebase authentication, and retrieves the current user's building code.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b>Note: Otherwise it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_request_form);

        // Initialize UI components
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
            getUserBuildingCode(); // Fetch building code for the current user
        } else {
            Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show(); // User not logged in.
            finish(); // Close the activity if no user is logged in
        }

        // Set up click listener for the submit button
        buttonSubmitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRequest();
            }
        });
    }

    /**
     * Retrieves the current user's building code from the Firebase Realtime Database.
     * This code is essential for storing help requests under the correct building.
     */
    private void getUserBuildingCode() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    currentBuildingCode = dataSnapshot.child("buildingCode").getValue(String.class);
                    if (currentBuildingCode != null) {
                        // Set up the database reference for requests specific to this building
                        requestsRef = FirebaseDatabase.getInstance().getReference("help_requests").child(currentBuildingCode);
                    } else {
                        Toast.makeText(SubmitRequestFormActivity.this, "קוד בניין לא נמצא.", Toast.LENGTH_SHORT).show(); // Building code not found.
                        finish();
                    }
                } else {
                    Toast.makeText(SubmitRequestFormActivity.this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show(); // User details not found.
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SubmitRequestFormActivity.this, "שגיאה בקבלת קוד בניין: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show(); // Error getting building code:
                finish();
            }
        });
    }

    /**
     * Handles the submission of a new help request.
     * It validates the input fields, retrieves user-specific details (if available),
     * and uploads the request data to the Firebase Realtime Database.
     */
    private void submitRequest() {
        String requestContent = editTextRequestContent.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String apartmentNumber = editTextApartmentNumber.getText().toString().trim();

        if (TextUtils.isEmpty(requestContent)) {
            editTextRequestContent.setError("יש להזין את תוכן הפנייה."); // Please enter the request content.
            return;
        }

        if (requestsRef != null && currentBuildingCode != null && currentUserId != null) {
            progressBar.setVisibility(View.VISIBLE);

            String requestId = requestsRef.push().getKey(); // Generate a unique key for the request
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentDate = dateFormat.format(calendar.getTime());
            String currentTime = timeFormat.format(calendar.getTime());

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("content", requestContent);
            // Default to "Anonymous" if full name is empty, or use the user's name from profile
            requestData.put("fullName", TextUtils.isEmpty(fullName) ? "אנונימי" : fullName);
            // Default to empty string if apartment number is empty, or use the user's apartment number from profile
            requestData.put("apartmentNumber", TextUtils.isEmpty(apartmentNumber) ? "" : apartmentNumber);
            requestData.put("date", currentDate);
            requestData.put("time", currentTime);
            requestData.put("timestamp", ServerValue.TIMESTAMP); // Firebase server timestamp
            requestData.put("isOpen", true); // All new requests are open by default
            requestData.put("publisherId", currentUserId);

            // Fetch publisher's full name and apartment number from their user profile
            // This ensures consistency and fallback if not provided in the form
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String publisherFullName = snapshot.child("fullName").getValue(String.class);
                    String publisherApartmentNumber = snapshot.child("apartmentNumber").getValue(String.class);

                    // Prioritize user input, then profile data, then defaults
                    requestData.put("fullName", TextUtils.isEmpty(fullName) ? (publisherFullName != null ? publisherFullName : "אנונימי") : fullName);
                    requestData.put("apartmentNumber", TextUtils.isEmpty(apartmentNumber) ? (publisherApartmentNumber != null ? publisherApartmentNumber : "") : apartmentNumber);

                    // Save the request to the database
                    requestsRef.child(requestId).setValue(requestData)
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(SubmitRequestFormActivity.this, "הפנייה הוגשה בהצלחה.", Toast.LENGTH_SHORT).show(); // Request submitted successfully.
                                    finish(); // Close the screen after successful submission
                                } else {
                                    Toast.makeText(SubmitRequestFormActivity.this, "שגיאה בהגשת הפנייה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Error submitting request:
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SubmitRequestFormActivity.this, "שגיאה בקבלת פרטי משתמש: " + error.getMessage(), Toast.LENGTH_SHORT).show(); // Error getting user details:
                }
            });
        } else {
            Toast.makeText(this, "לא ניתן להגיש את הפנייה כרגע.", Toast.LENGTH_SHORT).show(); // Cannot submit the request at the moment.
        }
    }
}