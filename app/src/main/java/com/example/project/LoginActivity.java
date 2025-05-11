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

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextBuildingCode;
    private Button buttonLogin, buttonForgotPassword, buttonSignup; // הוספנו כפתור הרשמה
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // בדוק אם המשתמש כבר מחובר
        if (currentUser != null) {
            // משתמש מחובר, עבור ישירות ל-MainActivity
            startMainActivity();
            finish(); // סגור את LoginActivity כדי שלא יחזרו אליו בלחיצה על "חזור"
            return; // אל תמשיך בהצגת הלייאוט של מסך ההתחברות
        }

        // אם המשתמש לא מחובר, הצג את לייאוט מסך ההתחברות
        setContentView(R.layout.activity_login);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        editTextBuildingCode = findViewById(R.id.editTextBuildingCodeLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonForgotPassword = findViewById(R.id.buttonForgotPassword);
        buttonSignup = findViewById(R.id.buttonSignup); // קישור לכפתור הרשמה

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
                                    // בדוק אם קוד הבניין תואם לזה של המשתמש
                                    String userId = mAuth.getCurrentUser().getUid();
                                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String userBuildingCode = dataSnapshot.child("buildingCode").getValue(String.class);
                                                if (buildingCode.equals(userBuildingCode)) {
                                                    // התחברות מוצלחת, קוד בניין תואם
                                                    Toast.makeText(LoginActivity.this, "התחברות בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                                                    startMainActivity();
                                                    finish();
                                                } else {
                                                    // קוד בניין לא תואם
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
                                    // אם ההתחברות נכשלה, הצג הודעה למשתמש.
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

        // טיפול במעבר למסך הרשמה
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateUserActivity.class));
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}