package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class ViewSubmittedRequestsActivity extends AppCompatActivity {

    private RecyclerView submittedRequestsRecyclerView;
    private TextView emptyRequestsTextView;
    private ProgressBar progressBar;
    private DatabaseReference requestsRef;
    private String currentBuildingCode;
    private SubmittedRequestAdapter adapter;
    private List<SubmittedRequest> submittedRequestsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_submitted_requests);

        submittedRequestsRecyclerView = findViewById(R.id.submittedRequestsRecyclerView);
        emptyRequestsTextView = findViewById(R.id.emptyRequestsTextView);
        progressBar = findViewById(R.id.progressBar);
        submittedRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        submittedRequestsList = new ArrayList<>();
        adapter = new SubmittedRequestAdapter(submittedRequestsList, this::closeRequest);
        submittedRequestsRecyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);

        getCurrentBuildingCode();
    }

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
                    handleFirebaseError("שגיאה בקבלת קוד בניין: " + error.getMessage());
                }
            });
        } else {
            handleUserNotLoggedIn();
        }
    }

    private void fetchSubmittedRequests() {
        if (requestsRef != null) {
            Query query = requestsRef.orderByChild("timestamp"); // הצג לפי סדר הפרסום
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
                    handleFirebaseError("שגיאה בטעינת פניות: " + databaseError.getMessage());
                }
            });
        } else {
            handleFirebaseError("לא ניתן לגשת לפניות כרגע.");
        }
    }

    private void closeRequest(String requestId) {
        if (requestsRef != null) {
            requestsRef.child(requestId).child("isOpen").setValue(false)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "הפנייה נסגרה בהצלחה.", Toast.LENGTH_SHORT).show();
                            // רענן את הרשימה לאחר הסגירה
                            fetchSubmittedRequests();
                        } else {
                            handleFirebaseError("שגיאה בסגירת הפנייה: " + task.getException().getMessage());
                        }
                    });
        } else {
            handleFirebaseError("לא ניתן לגשת לפניות כרגע.");
        }
    }

    private void handleUserNotLoggedIn() {
        progressBar.setVisibility(View.GONE);
        emptyRequestsTextView.setVisibility(View.VISIBLE);
        emptyRequestsTextView.setText("משתמש לא מחובר.");
        Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
    }

    private void handleBuildingCodeNotFound() {
        progressBar.setVisibility(View.GONE);
        emptyRequestsTextView.setVisibility(View.VISIBLE);
        emptyRequestsTextView.setText("לא נמצא קוד בניין עבור משתמש זה.");
        Toast.makeText(this, "לא נמצא קוד בניין עבור משתמש זה.", Toast.LENGTH_SHORT).show();
    }

    private void handleUserDataNotFound() {
        progressBar.setVisibility(View.GONE);
        emptyRequestsTextView.setVisibility(View.VISIBLE);
        emptyRequestsTextView.setText("פרטי משתמש לא נמצאו.");
        Toast.makeText(this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show();
    }

    private void handleFirebaseError(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Adapter עבור RecyclerView
    private static class SubmittedRequestAdapter extends RecyclerView.Adapter<SubmittedRequestViewHolder> {
        private List<SubmittedRequest> requests;
        private final OnCloseClickListener onCloseClickListener;

        public SubmittedRequestAdapter(List<SubmittedRequest> requests, OnCloseClickListener onCloseClickListener) {
            this.requests = requests;
            this.onCloseClickListener = onCloseClickListener;
        }

        @NonNull
        @Override
        public SubmittedRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_submitted_request, parent, false);
            return new SubmittedRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SubmittedRequestViewHolder holder, int position) {
            SubmittedRequest request = requests.get(position);
            holder.requestContentTextView.setText(request.getContent());
            String dateTime = request.getDate() + " " + request.getTime();
            holder.requestDateTimeTextView.setText(dateTime);
            holder.requestPublisherTextView.setText(request.getFullName());
            holder.requestApartmentNumberTextView.setText("דירה: " + request.getApartmentNumber());

            holder.buttonCloseRequest.setVisibility(View.VISIBLE);
            holder.buttonCloseRequest.setOnClickListener(v -> {
                // כדי לקבל את ה-key של הפריט, אנחנו צריכים למצוא את מיקום הפריט ב-Firebase
                DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("help_requests").child(request.getBuildingCode());
                Query query = requestsRef.orderByChild("timestamp").equalTo(request.getTimestamp());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            onCloseClickListener.onCloseClick(snapshot.getKey());
                            return; // מצאנו את הפריט, יוצאים מהלולאה
                        }
                        Toast.makeText(holder.itemView.getContext(), "שגיאה: לא ניתן לסגור בקשה זו.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(holder.itemView.getContext(), "שגיאה באיתור מזהה בקשה: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }
    }

    // ViewHolder עבור פריט פנייה שהוגשה
    public static class SubmittedRequestViewHolder extends RecyclerView.ViewHolder {
        TextView requestContentTextView;
        TextView requestDateTimeTextView;
        TextView requestPublisherTextView;
        TextView requestApartmentNumberTextView;
        Button buttonCloseRequest;

        public SubmittedRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestContentTextView = itemView.findViewById(R.id.requestContentTextView);
            requestDateTimeTextView = itemView.findViewById(R.id.requestDateTimeTextView);
            requestPublisherTextView = itemView.findViewById(R.id.requestPublisherTextView);
            requestApartmentNumberTextView = itemView.findViewById(R.id.requestApartmentNumberTextView);
            buttonCloseRequest = itemView.findViewById(R.id.buttonCloseRequest);
        }
    }

    // ממשק עבור אירוע לחיצה על כפתור סגירה
    interface OnCloseClickListener {
        void onCloseClick(String requestId);
    }

    // מודל נתונים עבור פנייה שהוגשה
    public static class SubmittedRequest {
        private String content;
        private String fullName;
        private String apartmentNumber;
        private String date;
        private String time;
        private long timestamp;
        private boolean isOpen;
        private String publisherId;
        private String buildingCode; // הוספת קוד בניין

        public SubmittedRequest() {
            // דרוש עבור Firebase
        }

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

        public String getContent() {
            return content;
        }

        public String getFullName() {
            return fullName;
        }

        public String getApartmentNumber() {
            return apartmentNumber;
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

        public boolean isOpen() {
            return isOpen;
        }

        public String getPublisherId() {
            return publisherId;
        }

        public String getBuildingCode() {
            return buildingCode;
        }
    }
}