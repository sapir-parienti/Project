package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewAllNotificationsActivity extends AppCompatActivity {

    private RecyclerView allNotificationsRecyclerView;
    private TextView emptyAllNotificationsTextView;
    private ProgressBar allNotificationsProgressBar;
    private DatabaseReference allNotificationsRef;
    private String currentBuildingCode;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_notifications);

        allNotificationsRecyclerView = findViewById(R.id.allNotificationsRecyclerView);
        emptyAllNotificationsTextView = findViewById(R.id.emptyAllNotificationsTextView);
        allNotificationsProgressBar = findViewById(R.id.allNotificationsProgressBar);
        allNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        allNotificationsRecyclerView.setAdapter(adapter);

        allNotificationsProgressBar.setVisibility(View.VISIBLE);

        // קבל את קוד הבניין של המשתמש הנוכחי כדי להציג רק הודעות של הבניין שלו
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentBuildingCode = snapshot.child("buildingCode").getValue(String.class);
                        if (currentBuildingCode != null) {
                            allNotificationsRef = FirebaseDatabase.getInstance().getReference("building_notifications").child(currentBuildingCode);
                            fetchNotifications();
                        } else {
                            allNotificationsProgressBar.setVisibility(View.GONE);
                            emptyAllNotificationsTextView.setVisibility(View.VISIBLE);
                            emptyAllNotificationsTextView.setText("לא נמצא קוד בניין עבור משתמש זה.");
                        }
                    } else {
                        allNotificationsProgressBar.setVisibility(View.GONE);
                        emptyAllNotificationsTextView.setVisibility(View.VISIBLE);
                        emptyAllNotificationsTextView.setText("פרטי משתמש לא נמצאו.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    allNotificationsProgressBar.setVisibility(View.GONE);
                    Toast.makeText(ViewAllNotificationsActivity.this, "שגיאה בקבלת קוד בניין: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            allNotificationsProgressBar.setVisibility(View.GONE);
            emptyAllNotificationsTextView.setVisibility(View.VISIBLE);
            emptyAllNotificationsTextView.setText("משתמש לא מחובר.");
        }
    }

    private void fetchNotifications() {
        allNotificationsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(notification);
                    }
                }
                adapter.notifyDataSetChanged();
                allNotificationsProgressBar.setVisibility(View.GONE);
                if (notificationList.isEmpty()) {
                    emptyAllNotificationsTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyAllNotificationsTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                allNotificationsProgressBar.setVisibility(View.GONE);
                Toast.makeText(ViewAllNotificationsActivity.this, "שגיאה בטעינת הודעות: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ViewHolder עבור פריט הודעה
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationContentTextView;
        TextView notificationDateTimeTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationContentTextView = itemView.findViewById(R.id.notificationContentTextView);
            notificationDateTimeTextView = itemView.findViewById(R.id.notificationDateTimeTextView);
        }
    }

    // Adapter עבור RecyclerView
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {
        private List<Notification> notifications;

        public NotificationAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_notification, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Notification notification = notifications.get(position);
            holder.notificationContentTextView.setText(notification.getContent());
            String dateTime = notification.getDate() + " " + notification.getTime();
            holder.notificationDateTimeTextView.setText(dateTime);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }
    }

    // מודל נתונים עבור הודעה
    public static class Notification {
        private String content;
        private String date;
        private String time;
        private long timestamp;

        public Notification() {
            // דרוש עבור Firebase
        }

        public Notification(String content, String date, String time, long timestamp) {
            this.content = content;
            this.date = date;
            this.time = time;
            this.timestamp = timestamp;
        }

        public String getContent() {
            return content;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}