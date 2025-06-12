package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * CreateUserActivity handles new user registration for the building management system.
 *
 * This activity provides a registration interface where new users can create accounts
 * by providing their email, full name, building code, and password. The activity
 * validates the building code against the Firebase database before allowing registration
 * and automatically creates user records with default non-manager privileges.
 *
 * Features:
 * - User input validation for all required fields
 * - Building code verification against Firebase database
 * - Firebase Authentication account creation
 * - User profile creation in Firebase Realtime Database
 * - Automatic navigation to MainActivity after successful registration
 * - Error handling for registration failures
 *
 * @author [Your Name]
 * @version 1.0
 * @since 1.0
 */
public class CreateUserActivity extends AppCompatActivity {

    /** Email input field for user registration */
    private EditText editTextEmail;

    /** Full name input field for user profile */
    private EditText editTextFullName;

    /** Building code input field for building association */
    private EditText editTextBuildingCode;

    /** Password input field for account security */
    private EditText editTextPassword;

    /** Signup button to initiate registration process */
    private Button buttonSignup;

    /** Firebase Authentication instance for creating user accounts */
    private FirebaseAuth mAuth;

    /** Firebase Database reference for validating building codes */
    private DatabaseReference buildingsRef;

    /** Firebase Database reference for storing user data */
    private DatabaseReference usersRef;

    /**
     * Called when the activity is first created. Initializes the user registration interface
     * and sets up Firebase services for authentication and data storage.
     *
     * This method performs the following operations:
     * 1. Sets up the activity layout
     * 2. Initializes Firebase Authentication and Database references
     * 3. Binds UI components to their respective views
     * 4. Sets up the signup button click listener with comprehensive validation
     *
     * The registration process includes:
     * - Input validation for all required fields
     * - Building code verification against the database
     * - Firebase Authentication account creation
     * - User profile creation with default settings
     * - Navigation to MainActivity upon successful registration
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains
     *                          the data it most recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        mAuth = FirebaseAuth.getInstance();
        buildingsRef = FirebaseDatabase.getInstance().getReference().child("buildings");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        editTextEmail = findViewById(R.id.editTextEmailSignup);
        editTextFullName = findViewById(R.id.editTextFullNameSignup);
        editTextBuildingCode = findViewById(R.id.editTextBuildingCodeSignup);
        editTextPassword = findViewById(R.id.editTextPasswordSignup);
        buttonSignup = findViewById(R.id.buttonSignup);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editTextEmail.getText().toString().trim();
                final String fullName = editTextFullName.getText().toString().trim();
                final String buildingCode = editTextBuildingCode.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("יש להזין אימייל");
                    return;
                }
                if (TextUtils.isEmpty(fullName)) {
                    editTextFullName.setError("יש להזין שם מלא");
                    return;
                }
                if (TextUtils.isEmpty(buildingCode)) {
                    editTextBuildingCode.setError("יש להזין קוד בניין");
                    return;
                } if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("יש להזין סיסמה");
                    return;
                }

                if (password.length()<6){
                    editTextPassword.setError("סיסמה צריכה להכיל לפחות 6 תווים");
                    return;
                }

                buildingsRef.child(buildingCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(CreateUserActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                String userId = mAuth.getCurrentUser().getUid();
                                                usersRef.child(userId)
                                                        .setValue(new User(fullName, buildingCode, email, false)); // ברירת מחדל: לא מנהל
                                                Toast.makeText(CreateUserActivity.this, "הרשמה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(CreateUserActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(CreateUserActivity.this, "הרשמה נכשלה: " + task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            editTextBuildingCode.setError("קוד בניין לא תקין");
                            Toast.makeText(CreateUserActivity.this, "קוד בניין לא תקין", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(CreateUserActivity.this, "שגיאה באימות קוד בניין: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * User data model class for Firebase Realtime Database storage.
     *
     * This static nested class represents a user in the building management system.
     * It contains all necessary user information including personal details,
     * building association, and management privileges. The class is designed
     * to work seamlessly with Firebase's automatic serialization/deserialization.
     *
     * The class includes:
     * - Personal information (full name, email)
     * - Building association (building code)
     * - Authorization level (manager status)
     * - Firebase-compatible constructors
     *
     * @author [Your Name]
     * @version 1.0
     * @since 1.0
     */
    public static class User {

        /** The user's full name as provided during registration */
        public String fullName;

        /** The building code associating the user with a specific building */
        public String buildingCode;

        /** The user's email address used for authentication */
        public String email;

        /** Flag indicating whether the user has manager privileges (default: false) */
        public boolean isManager;

        /**
         * Default no-argument constructor required by Firebase for object deserialization.
         *
         * Firebase Realtime Database requires a public no-argument constructor
         * to automatically convert database snapshots into Java objects.
         */
        public User() {
            // דרוש עבור Firebase
        }

        /**
         * Parameterized constructor for creating User objects with specified values.
         *
         * This constructor is used when creating new user records during the
         * registration process. By default, new users are created with manager
         * privileges set to false for security purposes.
         *
         * @param fullName     The user's complete name
         * @param buildingCode The code identifying the user's associated building
         * @param email        The user's email address for authentication
         * @param isManager    Boolean flag indicating manager privileges
         */
        public User(String fullName, String buildingCode, String email, boolean isManager) {
            this.fullName = fullName;
            this.buildingCode = buildingCode;
            this.email = email;
            this.isManager = isManager;
        }
    }
}