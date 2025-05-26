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

/**
 * An activity that displays a list of building notifications to the user.
 * It retrieves notifications from a Firebase Realtime Database based on the user's
 * building code and presents them in a RecyclerView.
 */
public class ViewBuildingNotificationsActivity extends AppCompatActivity {

    private RecyclerView buildingNotificationsRecyclerView;
    private TextView emptyBuildingNotificationsTextView;
    private ProgressBar buildingNotificationsProgressBar;
    private DatabaseReference buildingNotificationsRef;
    private String currentBuildingCode;
    private BuildingNotificationAdapter adapter;
    private List<BuildingNotification> buildingNotificationsList;

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up the RecyclerView, and initiates
     * the process of fetching the current user's building code.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b>Note: Otherwise it is null.</b>
     */
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

    /**
     * Retrieves the building code associated with the currently logged-in user from Firebase.
     * Once the building code is obtained, it proceeds to fetch building notifications.
     */
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
                    handleFirebaseError("שגיאה בקבלת קוד בניין: " + error.getMessage()); // Error getting building code:
                }
            });
        } else {
            handleUserNotLoggedIn();
        }
    }

    /**
     * Fetches building notifications from Firebase Realtime Database.
     * It orders notifications by timestamp (publication order) and updates the RecyclerView.
     * Displays a message if no notifications are found.
     */
    private void fetchBuildingNotifications() {
        if (buildingNotificationsRef != null) {
            Query query = buildingNotificationsRef.orderByChild("timestamp"); // Order by publication time
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
                    handleFirebaseError("שגיאה בטעינת הודעות בניין: " + databaseError.getMessage()); // Error loading building notifications:
                }
            });
        } else {
            handleFirebaseError("לא ניתן לגשת להודעות בניין כרגע."); // Cannot access building notifications at the moment.
        }
    }

    /**
     * Handles the scenario where the user is not logged in.
     * Hides the progress bar, displays an empty message, and shows a toast.
     */
    private void handleUserNotLoggedIn() {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        emptyBuildingNotificationsTextView.setVisibility(View.VISIBLE);
        emptyBuildingNotificationsTextView.setText("משתמש לא מחובר."); // User not logged in.
        Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show(); // User not logged in.
    }

    /**
     * Handles the scenario where the building code for the current user is not found.
     * Hides the progress bar, displays an empty message, and shows a toast.
     */
    private void handleBuildingCodeNotFound() {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        emptyBuildingNotificationsTextView.setVisibility(View.VISIBLE);
        emptyBuildingNotificationsTextView.setText("לא נמצא קוד בניין עבור משתמש זה."); // Building code not found for this user.
        Toast.makeText(this, "לא נמצא קוד בניין עבור משתמש זה.", Toast.LENGTH_SHORT).show(); // Building code not found for this user.
    }

    /**
     * Handles the scenario where user data is not found in Firebase.
     * Hides the progress bar, displays an empty message, and shows a toast.
     */
    private void handleUserDataNotFound() {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        emptyBuildingNotificationsTextView.setVisibility(View.VISIBLE);
        emptyBuildingNotificationsTextView.setText("פרטי משתמש לא נמצאו."); // User details not found.
        Toast.makeText(this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show(); // User details not found.
    }

    /**
     * Displays a Firebase-related error message as a toast.
     * Hides the progress bar.
     *
     * @param errorMessage The error message to display.
     */
    private void handleFirebaseError(String errorMessage) {
        buildingNotificationsProgressBar.setVisibility(View.GONE);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * RecyclerView Adapter for displaying building notifications.
     */
    private static class BuildingNotificationAdapter extends RecyclerView.Adapter<BuildingNotificationViewHolder> {
        private List<BuildingNotification> notifications;

        /**
         * Constructs a new {@code BuildingNotificationAdapter}.
         *
         * @param notifications The list of {@link BuildingNotification} objects to display.
         */
        public BuildingNotificationAdapter(List<BuildingNotification> notifications) {
            this.notifications = notifications;
        }

        /**
         * Called when RecyclerView needs a new {@link BuildingNotificationViewHolder} of the given type to represent
         * an item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         * an adapter position.
         * @param viewType The view type of the new View.
         * @return A new {@link BuildingNotificationViewHolder} that holds a View of the given view type.
         */
        @NonNull
        @Override
        public BuildingNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_building_notification, parent, false);
            return new BuildingNotificationViewHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method
         * updates the contents of the {@link BuildingNotificationViewHolder#itemView} to reflect the item at the given
         * position.
         *
         * @param holder The {@link BuildingNotificationViewHolder} which should be updated to represent the contents of the
         * item at the given {@code position} in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull BuildingNotificationViewHolder holder, int position) {
            BuildingNotification notification = notifications.get(position);
            holder.notificationContentTextView.setText(notification.getContent());
            String dateTime = notification.getDate() + " " + notification.getTime();
            holder.notificationDateTimeTextView.setText(dateTime);
            holder.notificationPublisherTextView.setText(notification.getFullName());
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return notifications.size();
        }
    }

    /**
     * ViewHolder for individual building notification items in the RecyclerView.
     * It holds the views for displaying notification content, date/time, and publisher.
     */
    public static class BuildingNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationContentTextView;
        TextView notificationDateTimeTextView;
        TextView notificationPublisherTextView;

        /**
         * Constructs a new {@code BuildingNotificationViewHolder}.
         *
         * @param itemView The view for a single list item.
         */
        public BuildingNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationContentTextView = itemView.findViewById(R.id.notificationContentTextView);
            notificationDateTimeTextView = itemView.findViewById(R.id.notificationDateTimeTextView);
            notificationPublisherTextView = itemView.findViewById(R.id.notificationPublisherTextView);
        }
    }

    /**
     * Data model for a building notification.
     * Represents the structure of a notification as stored in Firebase.
     */
    public static class BuildingNotification {
        private String publisherId;
        private String fullName;
        private String content;
        private String date;
        private String time;
        private long timestamp;

        /**
         * Default constructor required for Firebase.
         */
        public BuildingNotification() {
            // Required for Firebase
        }

        /**
         * Constructs a new {@code BuildingNotification} instance.
         *
         * @param publisherId The ID of the user who published the notification.
         * @param fullName The full name of the publisher.
         * @param apartmentNumber The apartment number of the publisher (though not used in this view directly,
         * it's part of the constructor for potential future use or consistency).
         * @param content The main content of the notification.
         * @param date The date the notification was published.
         * @param time The time the notification was published.
         * @param timestamp The server timestamp when the notification was published, used for ordering.
         */
        public BuildingNotification(String publisherId, String fullName, String apartmentNumber, String content, String date, String time, long timestamp) {
            this.publisherId = publisherId;
            this.fullName = fullName;
            this.content = content;
            this.date = date;
            this.time = time;
            this.timestamp = timestamp;
        }

        /**
         * Gets the ID of the publisher.
         * @return The publisher's user ID.
         */
        public String getPublisherId() {
            return publisherId;
        }

        /**
         * Gets the full name of the publisher.
         * @return The publisher's full name.
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * Gets the content of the notification.
         * @return The notification's text content.
         */
        public String getContent() {
            return content;
        }

        /**
         * Gets the date the notification was published.
         * @return The publication date string.
         */
        public String getDate() {
            return date;
        }

        /**
         * Gets the time the notification was published.
         * @return The publication time string.
         */
        public String getTime() {
            return time;
        }

        /**
         * Gets the timestamp of the notification's publication.
         * @return The timestamp as a long.
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}