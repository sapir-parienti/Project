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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity for managers to view submitted help requests from residents.
 * It retrieves requests from Firebase Realtime Database based on the manager's
 * building code and displays them in a RecyclerView.
 */
public class ViewSubmittedRequestsActivity extends AppCompatActivity {

    private RecyclerView submittedRequestsRecyclerView;
    private TextView emptyRequestsTextView;
    private ProgressBar progressBar;
    private DatabaseReference requestsRef;
    private String currentBuildingCode;
    private SubmittedRequestAdapter adapter;
    private List<SubmittedRequest> submittedRequestsList;

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up the RecyclerView, and begins the process
     * of fetching the current user's building code to load relevant requests.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b>Note: Otherwise it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_submitted_requests);

        submittedRequestsRecyclerView = findViewById(R.id.submittedRequestsRecyclerView);
        emptyRequestsTextView = findViewById(R.id.emptyRequestsTextView);
        progressBar = findViewById(R.id.progressBar);
        submittedRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        submittedRequestsList = new ArrayList<>();
        // Initialize the adapter with just the list (no close callback needed)
        adapter = new SubmittedRequestAdapter(submittedRequestsList);
        submittedRequestsRecyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);

        getCurrentBuildingCode();
    }

    /**
     * Retrieves the building code associated with the currently logged-in manager from Firebase.
     * This code is crucial for fetching help requests specific to their building.
     */
    private void getCurrentBuildingCode() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentBuildingCode = snapshot.child("buildingCode").getValue(String.class);
                        if (currentBuildingCode != null) {
                            // Set up the database reference for requests under this building code
                            requestsRef = FirebaseDatabase.getInstance().getReference("help_requests").child(currentBuildingCode);
                            fetchSubmittedRequests();
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
     * Fetches submitted help requests from the Firebase Realtime Database.
     * It orders requests by timestamp and updates the RecyclerView.
     * Displays a message if no requests are found.
     */
    private void fetchSubmittedRequests() {
        if (requestsRef != null) {
            Query query = requestsRef.orderByChild("timestamp"); // Display in publication order
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    submittedRequestsList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        SubmittedRequest request = snapshot.getValue(SubmittedRequest.class);
                        if (request != null) {
                            submittedRequestsList.add(request);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    emptyRequestsTextView.setVisibility(submittedRequestsList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleFirebaseError("שגיאה בטעינת פניות: " + databaseError.getMessage()); // Error loading requests:
                }
            });
        } else {
            handleFirebaseError("לא ניתן לגשת לפניות כרגע."); // Cannot access requests at the moment.
        }
    }

    /**
     * Handles the scenario where the user is not logged in.
     * Hides the progress bar, displays an empty message, and shows a toast.
     */
    private void handleUserNotLoggedIn() {
        progressBar.setVisibility(View.GONE);
        emptyRequestsTextView.setVisibility(View.VISIBLE);
        emptyRequestsTextView.setText("משתמש לא מחובר."); // User not logged in.
        Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show(); // User not logged in.
    }

    /**
     * Handles the scenario where the building code for the current user is not found.
     * Hides the progress bar, displays an empty message, and shows a toast.
     */
    private void handleBuildingCodeNotFound() {
        progressBar.setVisibility(View.GONE);
        emptyRequestsTextView.setVisibility(View.VISIBLE);
        emptyRequestsTextView.setText("לא נמצא קוד בניין עבור משתמש זה."); // Building code not found for this user.
        Toast.makeText(this, "לא נמצא קוד בניין עבור משתמש זה.", Toast.LENGTH_SHORT).show(); // Building code not found for this user.
    }

    /**
     * Handles the scenario where user data is not found in Firebase.
     * Hides the progress bar, displays an empty message, and shows a toast.
     */
    private void handleUserDataNotFound() {
        progressBar.setVisibility(View.GONE);
        emptyRequestsTextView.setVisibility(View.VISIBLE);
        emptyRequestsTextView.setText("פרטי משתמש לא נמצאו."); // User details not found.
        Toast.makeText(this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show(); // User details not found.
    }

    /**
     * Displays a Firebase-related error message as a toast.
     * Hides the progress bar.
     *
     * @param errorMessage The error message to display.
     */
    private void handleFirebaseError(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * RecyclerView Adapter for displaying submitted help requests.
     */
    private static class SubmittedRequestAdapter extends RecyclerView.Adapter<SubmittedRequestViewHolder> {
        private List<SubmittedRequest> requests;

        /**
         * Constructs a new {@code SubmittedRequestAdapter}.
         *
         * @param requests The list of {@link SubmittedRequest} objects to display.
         */
        public SubmittedRequestAdapter(List<SubmittedRequest> requests) {
            this.requests = requests;
        }

        /**
         * Called when RecyclerView needs a new {@link SubmittedRequestViewHolder} of the given type to represent
         * an item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         * an adapter position.
         * @param viewType The view type of the new View.
         * @return A new {@link SubmittedRequestViewHolder} that holds a View of the given view type.
         */
        @NonNull
        @Override
        public SubmittedRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_submitted_request, parent, false);
            return new SubmittedRequestViewHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method
         * updates the contents of the {@link SubmittedRequestViewHolder#itemView} to reflect the item at the given
         * position.
         *
         * @param holder The {@link SubmittedRequestViewHolder} which should be updated to represent the contents of the
         * item at the given {@code position} in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull SubmittedRequestViewHolder holder, int position) {
            SubmittedRequest request = requests.get(position);
            holder.requestContentTextView.setText(request.getContent());
            String dateTime = request.getDate() + " " + request.getTime();
            holder.requestDateTimeTextView.setText(dateTime);
            holder.requestPublisherTextView.setText(request.getFullName());
            holder.requestApartmentNumberTextView.setText("דירה: " + request.getApartmentNumber()); // Apartment:

            // Hide the close button completely
            holder.buttonCloseRequest.setVisibility(View.GONE);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return requests.size();
        }
    }

    /**
     * ViewHolder for individual submitted request items in the RecyclerView.
     * It holds the views for displaying request content, date/time, publisher,
     * and apartment number.
     */
    public static class SubmittedRequestViewHolder extends RecyclerView.ViewHolder {
        TextView requestContentTextView;
        TextView requestDateTimeTextView;
        TextView requestPublisherTextView;
        TextView requestApartmentNumberTextView;
        TextView buttonCloseRequest; // Changed to TextView since it's now just hidden

        /**
         * Constructs a new {@code SubmittedRequestViewHolder}.
         *
         * @param itemView The view for a single list item.
         */
        public SubmittedRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestContentTextView = itemView.findViewById(R.id.requestContentTextView);
            requestDateTimeTextView = itemView.findViewById(R.id.requestDateTimeTextView);
            requestPublisherTextView = itemView.findViewById(R.id.requestPublisherTextView);
            requestApartmentNumberTextView = itemView.findViewById(R.id.requestApartmentNumberTextView);
            buttonCloseRequest = itemView.findViewById(R.id.buttonCloseRequest);
        }
    }

    /**
     * Data model for a submitted help request.
     * Represents the structure of a request as stored in Firebase.
     */
    public static class SubmittedRequest {
        private String content;
        private String fullName;
        private String apartmentNumber;
        private String date;
        private String time;
        private long timestamp;
        private boolean isOpen;
        private String publisherId;
        private String buildingCode; // Added building code

        /**
         * Default constructor required for Firebase.
         */
        public SubmittedRequest() {
            // Required for Firebase
        }

        /**
         * Constructs a new {@code SubmittedRequest} instance.
         *
         * @param content The main content of the request.
         * @param fullName The full name of the requester.
         * @param apartmentNumber The apartment number of the requester.
         * @param date The date the request was submitted.
         * @param time The time the request was submitted.
         * @param timestamp The server timestamp when the request was submitted, used for ordering.
         * @param isOpen A boolean indicating if the request is still open (true) or closed (false).
         * @param publisherId The ID of the user who submitted the request.
         * @param buildingCode The building code associated with this request.
         */
        public SubmittedRequest(String content, String fullName, String apartmentNumber, String date, String time, long timestamp, boolean isOpen, String publisherId, String buildingCode) {
            this.content = content;
            this.fullName = fullName;
            this.apartmentNumber = apartmentNumber;
            this.date = date;
            this.time = time;
            this.timestamp = timestamp;
            this.isOpen = isOpen;
            this.publisherId = publisherId;
            this.buildingCode = buildingCode;
        }

        /**
         * Gets the content of the request.
         * @return The request's text content.
         */
        public String getContent() {
            return content;
        }

        /**
         * Gets the full name of the requester.
         * @return The requester's full name.
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * Gets the apartment number of the requester.
         * @return The requester's apartment number.
         */
        public String getApartmentNumber() {
            return apartmentNumber;
        }

        /**
         * Gets the date the request was submitted.
         * @return The submission date string.
         */
        public String getDate() {
            return date;
        }

        /**
         * Gets the time the request was submitted.
         * @return The submission time string.
         */
        public String getTime() {
            return time;
        }

        /**
         * Gets the timestamp of the request's submission.
         * @return The timestamp as a long.
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Checks if the request is open.
         * @return True if the request is open, false otherwise.
         */
        public boolean isOpen() {
            return isOpen;
        }

        /**
         * Gets the ID of the user who submitted the request.
         * @return The publisher's user ID.
         */
        public String getPublisherId() {
            return publisherId;
        }

        /**
         * Gets the building code associated with this request.
         * @return The building code string.
         */
        public String getBuildingCode() {
            return buildingCode;
        }
    }
}