package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class test1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test1);
       //יש בעיה עם המסך הזה ואני שוקלת לוותר עליו כי דרישות החובה של פיירבייס עובדות גם בלעדיו
        Button buttonViewHelp = findViewById(R.id.buttonViewHelp);

        buttonViewHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent כדי לעבור למסך הבא
                Intent intent = new Intent(test1.this, ViewOpenHelpRequestsActivity.class);

                // הפעלת המסך הבא באמצעות ה-Intent
                startActivity(intent);
            }
        });

    }
}