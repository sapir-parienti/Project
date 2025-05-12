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
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class ViewBuildingNotificationsActivity extends AppCompatActivity {

    private RecyclerView buildingNotificationsRecyclerView;
    private TextView emptyBuildingNotificationsTextView;
    private ProgressBar buildingNotificationsProgressBar;
    private DatabaseReference buildingNotificationsRef;
    private String currentBuildingCode;
    private BuildingNotificationAdapter adapter;
    private List<BuildingNotification> buildingNotificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_building_notifications);

        buildingNotificationsRecyclerView = findViewById(R.id.buildingNotificationsRecyclerView);
        emptyBuildingNotificationsTextView = findViewById(R.id.emptyBuildingNotificationsTextView);
        buildingNotificationsProgressBar = findViewById(R.id.buildingNotificationsProgressBar);
        buildingNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        buildingNotificationsList = new ArrayList<>();
        adapter = new BuildingNotificationAdapter(buildingNotificationsList);
        buildingNotificationsRecyclerView.setAdapter(adapter);

        buildingNotificationsProgressBar.setVisibility(View.VISIBLE);

        getCurrentBuildingCode();
    }

    private void getCurrentBuildingCode() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentBuildingCode = snapshot.child("buildingCode").getValue(String.class);
                        if (currentBuildingCode != null) {
                            buildingNotificationsRef = FirebaseDatabase.getInstance().getReference("building_notifications").child(currentBuildingCode);
                            fetchBuildingNotifications();
                        } else {
                            handleBuildingCodeNotFound();
                        }
                    } else {
                        handleUserDataNotFound();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleFirebaseError("שגיאה בקבלת קוד בניין: " + error.getMessage());
                }
            });
        } else {
            handleUserNotLoggedIn();
        }
    }

    private void fetchBuildingNotifications() {
        if (buildingNotificationsRef != null) {
            Query query = buildingNotificationsRef.orderByChild("timestamp"); // הצג לפי סדר הפרסום
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    buildingNotificationsList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BuildingNotification notification = snapshot.getValue(BuildingNotification.class);
                        if (notification != null) {
                            buildingNotificationsList.add(notification);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    buildingNotificationsProgressBar.setVisibility(View.GONE);
                    emptyBuildingNotificationsTextView.setVisibility(buildingNotificationsList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleFirebaseError("שגיאה בטעינת הודעות בניין: " + databaseError.getMessage());
                }
            });
        } else {
            handleFirebaseError("לא ניתן לגשת להודעות בניין כרגע.");
        }
    }

    private void handleUserNotLoggedIn() {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        emptyBuildingNotificationsTextView.setVisibility(View.VISIBLE);
        emptyBuildingNotificationsTextView.setText("משתמש לא מחובר.");
        Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
    }

    private void handleBuildingCodeNotFound() {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        emptyBuildingNotificationsTextView.setVisibility(View.VISIBLE);
        emptyBuildingNotificationsTextView.setText("לא נמצא קוד בניין עבור משתמש זה.");
        Toast.makeText(this, "לא נמצא קוד בניין עבור משתמש זה.", Toast.LENGTH_SHORT).show();
    }

    private void handleUserDataNotFound() {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        emptyBuildingNotificationsTextView.setVisibility(View.VISIBLE);
        emptyBuildingNotificationsTextView.setText("פרטי משתמש לא נמצאו.");
        Toast.makeText(this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show();
    }

    private void handleFirebaseError(String errorMessage) {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Adapter עבור RecyclerView
    private static class BuildingNotificationAdapter extends RecyclerView.Adapter<BuildingNotificationViewHolder> {
        private List<BuildingNotification> notifications;

        public BuildingNotificationAdapter(List<BuildingNotification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public BuildingNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_building_notification, parent, false);
            return new BuildingNotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BuildingNotificationViewHolder holder, int position) {
            BuildingNotification notification = notifications.get(position);
            holder.notificationContentTextView.setText(notification.getContent());
            String dateTime = notification.getDate() + " " + notification.getTime();
            holder.notificationDateTimeTextView.setText(dateTime);
            holder.notificationPublisherTextView.setText(notification.getFullName());
            holder.notificationApartmentNumberTextView.setText("דירה: " + notification.getApartmentNumber());
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }
    }

    // ViewHolder עבור פריט הודעת בניין
    public static class BuildingNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationContentTextView;
        TextView notificationDateTimeTextView;
        TextView notificationPublisherTextView;
        TextView notificationApartmentNumberTextView;

        public BuildingNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationContentTextView = itemView.findViewById(R.id.notificationContentTextView);
            notificationDateTimeTextView = itemView.findViewById(R.id.notificationDateTimeTextView);
            notificationPublisherTextView = itemView.findViewById(R.id.notificationPublisherTextView);
            notificationApartmentNumberTextView = itemView.findViewById(R.id.notificationApartmentNumberTextView);
        }
    }

    // מודל נתונים עבור הודעת בניין
    public static class BuildingNotification {
        private String publisherId;
        private String fullName;
        private String apartmentNumber;
        private String content;
        private String date;
        private String time;
        private long timestamp;

        public BuildingNotification() {
            // דרוש עבור Firebase
        }

        public BuildingNotification(String publisherId, String fullName, String apartmentNumber, String content, String date, String time, long timestamp) {
            this.publisherId = publisherId;
            this.fullName = fullName;
            this.apartmentNumber = apartmentNumber;
            this.content = content;
            this.date = date;
            this.time = time;
            this.timestamp = timestamp;
        }

        public String getPublisherId() {
            return publisherId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getApartmentNumber() {
            return apartmentNumber;
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