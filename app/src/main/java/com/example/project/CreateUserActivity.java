package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class CreateUserActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(CreateUserActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(CreateUserActivity.this, "הרשמה מוצלחת!", Toast.LENGTH_SHORT).show();
                                    // כאן תוסיף את הלוגיקה למעבר למסך הבא
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(CreateUserActivity.this, "הרשמה נכשלה: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(CreateUserActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(CreateUserActivity.this, "הרשמה מוצלחת!", Toast.LENGTH_SHORT).show();
                                    // כאן תוסיף את הלוגיקה למעבר למסך הבא
                                } else {
                                    // If sign in fails, display a message to the user.
                                    String errorMessage = "הרשמה נכשלה.";
                                    if (task.getException() instanceof FirebaseAuthException) {
                                        FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                                        errorMessage = getErrorMessage(firebaseAuthException.getErrorCode());
                                    }
                                    Toast.makeText(CreateUserActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private String getErrorMessage(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                return "אימייל לא תקין.";
            case "ERROR_WEAK_PASSWORD":
                return "סיסמה חלשה.";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "אימייל כבר בשימוש.";
            default:
                return "הרשמה נכשלה. נסה שוב מאוחר יותר.";
        }
    }
}
