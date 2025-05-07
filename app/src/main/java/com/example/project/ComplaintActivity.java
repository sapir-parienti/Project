package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ComplaintActivity extends AppCompatActivity {

    private EditText subjectEditText;
    private EditText descriptionEditText;
    private Button sendComplaintButton;
    private DatabaseReference complaintsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        subjectEditText = findViewById(R.id.subjectEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        sendComplaintButton = findViewById(R.id.sendComplaintButton);

        // Initialize Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        complaintsRef = database.getReference("complaints"); // Creates a node named "complaints"

        sendComplaintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = subjectEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();

                if (subject.isEmpty()) {
                    subjectEditText.setError("יש להזין נושא");
                    return;
                }

                if (description.isEmpty()) {
                    descriptionEditText.setError("יש להזין תיאור");
                    return;
                }

                // Create a unique key for the complaint
                String complaintId = complaintsRef.push().getKey();

                // Create a map to store the complaint data
                Map<String, String> complaintData = new HashMap<>();
                complaintData.put("subject", subject);
                complaintData.put("description", description);

                // Save the complaint data to Firebase
                complaintsRef.child(complaintId).setValue(complaintData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ComplaintActivity.this, "התלונה נשלחה בהצלחה!", Toast.LENGTH_SHORT).show();
                                // Clear the input fields
                                subjectEditText.setText("");
                                descriptionEditText.setText("");
                            } else {
                                Toast.makeText(ComplaintActivity.this, "שליחת התלונה נכשלה.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}