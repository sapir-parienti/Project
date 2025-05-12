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

public class ViewOpenHelpRequestsActivity extends AppCompatActivity {

    private RecyclerView openHelpRequestsRecyclerView;
    private TextView emptyOpenHelpRequestsTextView;
    private ProgressBar openHelpRequestsProgressBar;
    private DatabaseReference helpRequestsRef;
    private String currentBuildingCode;
    private String currentUserId;
    private OpenHelpRequestAdapter adapter;
    private List<HelpRequest> openHelpRequestsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_open_help_requests);

        openHelpRequestsRecyclerView = findViewById(R.id.openHelpRequestsRecyclerView);
        emptyOpenHelpRequestsTextView = findViewById(R.id.emptyOpenHelpRequestsTextView);
        openHelpRequestsProgressBar = findViewById(R.id.openHelpRequestsProgressBar);
        openHelpRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        openHelpRequestsList = new ArrayList<>();
        adapter = new OpenHelpRequestAdapter(openHelpRequestsList, this::closeHelpRequest);
        openHelpRequestsRecyclerView.setAdapter(adapter);

        openHelpRequestsProgressBar.setVisibility(View.VISIBLE);

        getCurrentBuildingCode();
    }

    private void getCurrentBuildingCode() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentBuildingCode = snapshot.child("buildingCode").getValue(String.class);
                        if (currentBuildingCode != null) {
                            helpRequestsRef = FirebaseDatabase.getInstance().getReference("help_requests").child(currentBuildingCode);
                            fetchOpenHelpRequests();
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

    private void fetchOpenHelpRequests() {
        if (helpRequestsRef != null) {
            Query query = helpRequestsRef.orderByChild("isOpen").equalTo(true).orderByChild("timestamp");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    openHelpRequestsList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HelpRequest request = snapshot.getValue(HelpRequest.class);
                        if (request != null) {
                            openHelpRequestsList.add(request);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    openHelpRequestsProgressBar.setVisibility(View.GONE);
                    emptyOpenHelpRequestsTextView.setVisibility(openHelpRequestsList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleFirebaseError("שגיאה בטעינת בקשות עזרה: " + databaseError.getMessage());
                }
            });
        } else {
            handleFirebaseError("לא ניתן לגשת לבקשות עזרה כרגע.");
        }
    }

    private void closeHelpRequest(String requestId) {
        if (helpRequestsRef != null) {
            helpRequestsRef.child(requestId).child("isOpen").setValue(false)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "בקשת העזרה נסגרה", Toast.LENGTH_SHORT).show();
                        } else {
                            handleFirebaseError("שגיאה בסגירת הבקשה: " + task.getException().getMessage());
                        }
                    });
        } else {
            handleFirebaseError("לא ניתן לגשת לבקשות עזרה כרגע.");
        }
    }

    private void handleUserNotLoggedIn() {
        openHelpRequestsProgressBar.setVisibility(View.GONE);
        emptyOpenHelpRequestsTextView.setVisibility(View.VISIBLE);
        emptyOpenHelpRequestsTextView.setText("משתמש לא מחובר.");
        Toast.makeText(this, "משתמש לא מחובר.", Toast.LENGTH_SHORT).show();
    }

    private void handleBuildingCodeNotFound() {
        openHelpRequestsProgressBar.setVisibility(View.GONE);
        emptyOpenHelpRequestsTextView.setVisibility(View.VISIBLE);
        emptyOpenHelpRequestsTextView.setText("לא נמצא קוד בניין עבור משתמש זה.");
        Toast.makeText(this, "לא נמצא קוד בניין עבור משתמש זה.", Toast.LENGTH_SHORT).show();
    }

    private void handleUserDataNotFound() {
        openHelpRequestsProgressBar.setVisibility(View.GONE);
        emptyOpenHelpRequestsTextView.setVisibility(View.VISIBLE);
        emptyOpenHelpRequestsTextView.setText("פרטי משתמש לא נמצאו.");
        Toast.makeText(this, "פרטי משתמש לא נמצאו.", Toast.LENGTH_SHORT).show();
    }

    private void handleFirebaseError(String errorMessage) {
        openHelpRequestsProgressBar.setVisibility(View.GONE);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Adapter עבור RecyclerView
    private class OpenHelpRequestAdapter extends RecyclerView.Adapter<OpenHelpRequestViewHolder> {
        private List<HelpRequest> requests;
        private final OnCloseClickListener onCloseClickListener;

        public OpenHelpRequestAdapter(List<HelpRequest> requests, OnCloseClickListener onCloseClickListener) {
            this.requests = requests;
            this.onCloseClickListener = onCloseClickListener;
        }

        @NonNull
        @Override
        public OpenHelpRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_open_help_request, parent, false);
            return new OpenHelpRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OpenHelpRequestViewHolder holder, int position) {
            HelpRequest request = requests.get(position);
            holder.helpRequestContentTextView.setText(request.getContent());
            String dateTime = request.getDate() + " " + request.getTime();
            holder.helpRequestDateTimeTextView.setText(dateTime);
            holder.helpRequestPublisherTextView.setText(request.getFullName());
            holder.helpRequestApartmentNumberTextView.setText("דירה: " + request.getApartmentNumber());

            if (request.getPublisherId() != null && request.getPublisherId().equals(currentUserId)) {
                holder.buttonCloseRequest.setVisibility(View.VISIBLE);
                holder.buttonCloseRequest.setOnClickListener(v -> {
                    // כדי לקבל את ה-key של הפריט, אנחנו צריכים למצוא את מיקום הפריט ב-Firebase
                    Query query = helpRequestsRef.orderByChild("timestamp").equalTo(request.getTimestamp());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                onCloseClickListener.onCloseClick(snapshot.getKey());
                                return; // מצאנו את הפריט, יוצאים מהלולאה
                            }
                            Toast.makeText(ViewOpenHelpRequestsActivity.this, "שגיאה: לא ניתן לסגור בקשה זו.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            handleFirebaseError("שגיאה באיתור מזהה בקשה: " + databaseError.getMessage());
                        }
                    });
                });
            } else {
                holder.buttonCloseRequest.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }
    }

    // ViewHolder עבור פריט בקשת עזרה
    public static class OpenHelpRequestViewHolder extends RecyclerView.ViewHolder {
        TextView helpRequestContentTextView;
        TextView helpRequestDateTimeTextView;
        TextView helpRequestPublisherTextView;
        TextView helpRequestApartmentNumberTextView;
        Button buttonCloseRequest;

        public OpenHelpRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            helpRequestContentTextView = itemView.findViewById(R.id.helpRequestContentTextView);
            helpRequestDateTimeTextView = itemView.findViewById(R.id.helpRequestDateTimeTextView);
            helpRequestPublisherTextView = itemView.findViewById(R.id.helpRequestPublisherTextView);
            helpRequestApartmentNumberTextView = itemView.findViewById(R.id.helpRequestApartmentNumberTextView);
            buttonCloseRequest = itemView.findViewById(R.id.buttonCloseRequest);
        }
    }

    // ממשק עבור אירוע לחיצה על כפתור סגירה
    interface OnCloseClickListener {
        void onCloseClick(String requestId);
    }

    // מודל נתונים עבור בקשת עזרה
    public static class HelpRequest {
        private String content;
        private String date;
        private String time;
        private long timestamp;
        private boolean isOpen;
        private String publisherId;
        private String fullName;
        private String apartmentNumber;

        public HelpRequest() {
            // דרוש עבור Firebase
        }

        public HelpRequest(String content, String date, String time, long timestamp, boolean isOpen, String publisherId, String fullName, String apartmentNumber) {
            this.content = content;
            this.date = date;
            this.time = time;
            this.timestamp = timestamp;
            this.isOpen = isOpen;
            this.publisherId = publisherId;
            this.fullName = fullName;
            this.apartmentNumber = apartmentNumber;
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

        public boolean isOpen() {
            return isOpen;
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

        public void setOpen(boolean open) {
            isOpen = open;
        }
    }
}