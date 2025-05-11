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

public class CreateUserActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextFullName, editTextBuildingCode, editTextPassword;
    private Button buttonSignup;
    private FirebaseAuth mAuth;
    private DatabaseReference buildingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        mAuth = FirebaseAuth.getInstance();
        buildingsRef = FirebaseDatabase.getInstance().getReference().child("buildings");

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
                }

                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("יש להזין סיסמה");
                    return;
                }

                // אימות קוד בניין מול Firebase
                buildingsRef.child(buildingCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // קוד הבניין קיים, המשך בהרשמה
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(CreateUserActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // הרשמה מוצלחת, שמור פרטים נוספים ב-Firebase Database
                                                String userId = mAuth.getCurrentUser().getUid();
                                                FirebaseDatabase.getInstance().getReference().child("users").child(userId)
                                                        .setValue(new User(fullName, buildingCode, email));
                                                Toast.makeText(CreateUserActivity.this, "הרשמה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(CreateUserActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                // אם ההרשמה נכשלה, הצג הודעה למשתמש.
                                                Toast.makeText(CreateUserActivity.this, "הרשמה נכשלה: " + task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // קוד הבניין לא קיים
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

    public static class User {
        public String fullName;
        public String buildingCode;
        public String email;

        public User() {
            // דרוש עבור Firebase
        }

        public User(String fullName, String buildingCode, String email) {
            this.fullName = fullName;
            this.buildingCode = buildingCode;
            this.email = email;
        }
    }
}