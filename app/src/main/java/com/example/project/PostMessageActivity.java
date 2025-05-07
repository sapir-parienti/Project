package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PostMessageActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button postButton;
    private DatabaseReference bulletinBoardRef;
    private FirebaseUser currentUser;

    // Replace with the specific User ID that is allowed to post
    private final String ADMIN_USER_ID = "YOUR_ADMIN_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_message);

        messageEditText = findViewById(R.id.messageEditText);
        postButton = findViewById(R.id.postButton);

        // Initialize Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        bulletinBoardRef = database.getReference("bulletin_board");

        // Get the current logged-in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString().trim();

                if (messageText.isEmpty()) {
                    messageEditText.setError("הודעה לא יכולה להיות ריקה");
                    return;
                }

                if (currentUser != null && currentUser.getUid().equals(ADMIN_USER_ID)) {
                    String userId = currentUser.getUid();
                    String displayName = currentUser.getDisplayName();
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = "מנהל";
                    }

                    String messageId = bulletinBoardRef.push().getKey();
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("author", displayName);
                    messageData.put("text", messageText);
                    messageData.put("timestamp", System.currentTimeMillis());

                    bulletinBoardRef.child(messageId).setValue(messageData)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PostMessageActivity.this, "ההודעה פורסמה בהצלחה!", Toast.LENGTH_SHORT).show();
                                    messageEditText.setText("");
                                    finish();
                                } else {
                                    Toast.makeText(PostMessageActivity.this, "פרסום ההודעה נכשל.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(PostMessageActivity.this, "רק מנהל המערכת יכול לפרסם הודעות.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}