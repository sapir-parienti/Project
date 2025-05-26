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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * LoginActivity handles user authentication and navigation based on user roles.
 *
 * This activity provides a login interface for users to authenticate using email, password,
 * and building code. It supports automatic login for already authenticated users and
 * redirects them to appropriate activities based on their manager status.
 *
 * Features:
 * - Firebase Authentication integration
 * - Building code verification
 * - Role-based navigation (Manager vs Regular User)
 * - Automatic login for authenticated users
 * - Password recovery and user registration navigation
 *
 * @author [Your Name]
 * @version 1.0
 * @since 1.0
 */
public class LoginActivity extends AppCompatActivity {

    /** Email input field for user authentication */
    private EditText editTextEmail;

    /** Password input field for user authentication */
    private EditText editTextPassword;

    /** Building code input field for user verification */
    private EditText editTextBuildingCode;

    /** Login button to initiate authentication process */
    private Button buttonLogin;

    /** Forgot password button to navigate to password recovery */
    private Button buttonForgotPassword;

    /** Signup button to navigate to user registration */
    private Button buttonSignup;

    /** Firebase Authentication instance for handling user authentication */
    private FirebaseAuth mAuth;

    /** Firebase Database reference for accessing user data */
    private DatabaseReference usersRef;

    /**
     * Called when the activity is first created. Handles automatic login for
     * authenticated users and sets up the login interface for new users.
     *
     * The method performs the following operations:
     * 1. Checks if a user is already authenticated
     * 2. If authenticated, verifies user data and redirects based on manager status
     * 3. If not authenticated, displays the login interface
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains
     *                          the data it most recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // בדיקה האם משתמש מחובר כבר
        if (currentUser != null) {
            // משתמש מחובר, קבל את ה-UID שלו ובדוק את סטטוס המנהל
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Boolean isManager = dataSnapshot.child("isManager").getValue(Boolean.class);
                        if (isManager != null && isManager) {
                            startManagerActivity();
                        } else {
                            startUserActivity();
                        }
                        finish(); // סגור את LoginActivity
                    } else {
                        // משתמש מחובר ב-Auth אבל לא קיים ב-DB (מקרה נדיר)
                        // כדאי לטפל במקרה הזה, למשל להציג מסך פרופיל להשלמה
                        Toast.makeText(LoginActivity.this, "שגיאה: פרטי משתמש לא נמצאו", Toast.LENGTH_LONG).show();
                        // אפשרות לנתק את המשתמש כאן: mAuth.signOut();
                        setContentView(R.layout.activity_login); // הצג את מסך ההתחברות
                        setupLoginButtons(); // הגדר את פעולות הכפתורים
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(LoginActivity.this, "שגיאה בקריאת נתוני משתמש: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    setContentView(R.layout.activity_login); // הצג את מסך ההתחברות
                    setupLoginButtons(); // הגדר את פעולות הכפתורים
                }
            });
            return; // אל תמשיך בהצגת הלייאוט של מסך ההתחברות כרגע
        }

        // אם המשתמש לא מחובר, הצג את לייאוט מסך ההתחברות והגדר את הכפתורים
        setContentView(R.layout.activity_login);
        setupLoginButtons();
    }

    /**
     * Sets up the login interface by initializing UI components and setting up event listeners.
     *
     * This method performs the following operations:
     * 1. Initializes Firebase database reference
     * 2. Binds UI components to their respective views
     * 3. Sets up click listeners for login, forgot password, and signup buttons
     * 4. Implements login validation and authentication logic
     *
     * The login process includes:
     * - Input validation for email, password, and building code
     * - Firebase authentication
     * - Building code verification against user data
     * - Role-based navigation after successful authentication
     */
    private void setupLoginButtons() {
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        editTextBuildingCode = findViewById(R.id.editTextBuildingCodeLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonForgotPassword = findViewById(R.id.buttonForgotPassword);
        buttonSignup = findViewById(R.id.buttonSignup);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                final String buildingCode = editTextBuildingCode.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("יש להזין אימייל");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("יש להזין סיסמה");
                    return;
                }

                if (TextUtils.isEmpty(buildingCode)) {
                    editTextBuildingCode.setError("יש להזין קוד בניין");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userId = mAuth.getCurrentUser().getUid();
                                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String userBuildingCode = dataSnapshot.child("buildingCode").getValue(String.class);
                                                Boolean isManager = dataSnapshot.child("isManager").getValue(Boolean.class);

                                                if (buildingCode.equals(userBuildingCode)) {
                                                    if (isManager != null && isManager) {
                                                        startManagerActivity();
                                                    } else {
                                                        startUserActivity();
                                                    }
                                                    finish();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "קוד בניין לא תואם לחשבון זה", Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "שגיאה: משתמש לא קיים במערכת", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(LoginActivity.this, "שגיאה בקריאת נתוני משתמש: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this, "התחברות נכשלה: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateUserActivity.class));
            }
        });
    }

    /**
     * Starts the ManagerActivity for users with manager privileges.
     *
     * This method creates an intent to launch the ManagerActivity and finishes
     * the current LoginActivity to prevent users from navigating back to the
     * login screen using the back button.
     */
    private void startManagerActivity() {
        Intent intent = new Intent(this, ManagerActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Starts the MainActivity for regular users without manager privileges.
     *
     * This method creates an intent to launch the MainActivity and finishes
     * the current LoginActivity to prevent users from navigating back to the
     * login screen using the back button.
     */
    private void startUserActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}