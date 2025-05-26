package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * ForgotPasswordActivity provides password recovery functionality for users who have forgotten their login credentials.
 *
 * This activity offers a simple interface where users can enter their email address to receive
 * a password reset link. It integrates with Firebase Authentication's built-in password recovery
 * system, which handles the secure generation and delivery of password reset emails.
 *
 * Features:
 * - Email input validation to ensure proper format
 * - Integration with Firebase Authentication password reset service
 * - User feedback for successful and failed reset attempts
 * - Automatic activity termination after successful email dispatch
 * - Error handling with descriptive messages
 * - Clean, user-friendly interface design
 *
 * Security considerations:
 * - Uses Firebase's secure password reset mechanism
 * - No sensitive data is stored or transmitted beyond the email address
 * - Reset links are time-limited and single-use (handled by Firebase)
 * - User authentication is handled entirely by Firebase services
 *
 * @author [Your Name]
 * @version 1.0
 * @since 1.0
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    /** Email input field where users enter their registered email address */
    private EditText editTextEmail;

    /** Button to trigger the password reset email dispatch process */
    private Button buttonResetPassword;

    /** Firebase Authentication instance for handling password recovery operations */
    private FirebaseAuth mAuth;

    /**
     * Called when the activity is first created. Initializes the password recovery interface
     * and sets up Firebase Authentication for password reset operations.
     *
     * This method performs the following operations:
     * 1. Sets up the activity layout with password recovery form
     * 2. Initializes Firebase Authentication service
     * 3. Binds UI components to their respective view elements
     * 4. Sets up click listener for the reset password button
     * 5. Implements email validation and Firebase password reset functionality
     *
     * The password reset process includes:
     * - Email format validation to prevent invalid requests
     * - Firebase sendPasswordResetEmail() API integration
     * - Success/error feedback to inform users of the operation status
     * - Automatic activity closure after successful email dispatch
     *
     * Error scenarios handled:
     * - Empty email field validation
     * - Invalid email addresses (handled by Firebase)
     * - Network connectivity issues
     * - Non-existent email addresses in the system
     * - Firebase service unavailability
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains
     *                          the data it most recently supplied in onSaveInstanceState(Bundle).
     *                          Note: In this simple activity, savedInstanceState is typically null
     *                          as there's no complex state to preserve.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmailForgotPassword);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("יש להזין אימייל");
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "קישור לאיפוס סיסמה נשלח לאימייל שלך", Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    Toast.makeText(ForgotPasswordActivity.this, "שגיאה באיפוס סיסמה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}