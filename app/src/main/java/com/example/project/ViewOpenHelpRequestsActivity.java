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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewOpenHelpRequestsActivity extends AppCompatActivity {

    private RecyclerView allOpenHelpRequestsRecyclerView;
    private TextView emptyAllOpenHelpRequestsTextView;
    private ProgressBar allOpenHelpRequestsProgressBar;
    private DatabaseReference helpRequestsRootRef;
    private AllOpenHelpRequestAdapter adapter;
    private List<HelpRequest> allOpenHelpRequestsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_open_help_requests);

        allOpenHelpRequestsRecyclerView = findViewById(R.id.allOpenHelpRequestsRecyclerView);
        emptyAllOpenHelpRequestsTextView = findViewById(R.id.emptyAllOpenHelpRequestsTextView);
        allOpenHelpRequestsProgressBar = findViewById(R.id.allOpenHelpRequestsProgressBar);
        allOpenHelpRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        allOpenHelpRequestsList = new ArrayList<>();
        adapter = new AllOpenHelpRequestAdapter(allOpenHelpRequestsList);
        allOpenHelpRequestsRecyclerView.setAdapter(adapter);

        allOpenHelpRequestsProgressBar.setVisibility(View.VISIBLE);

        // נקרא מבסיס הצומת 'help_requests' כדי לקבל את כל הבניינים
        helpRequestsRootRef = FirebaseDatabase.getInstance().getReference("help_requests");
        fetchAllOpenHelpRequests();
    }

    private void fetchAllOpenHelpRequests() {
        Query query = helpRequestsRootRef; // קוראים את כל הצמתים תחת 'help_requests'
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allOpenHelpRequestsList.clear();
                for (DataSnapshot buildingSnapshot : dataSnapshot.getChildren()) {
                    // עבור כל קוד בניין, קרא את הבקשות הפתוחות
                    DatabaseReference buildingRequestsRef = helpRequestsRootRef.child(buildingSnapshot.getKey());
                    Query openRequestsQuery = buildingRequestsRef.orderByChild("isOpen").equalTo(true).orderByChild("timestamp");
                    for (DataSnapshot requestSnapshot : openRequestsQuery.get().getResult().getChildren()) {
                        HelpRequest request = requestSnapshot.getValue(HelpRequest.class);
                        if (request != null) {
                            allOpenHelpRequestsList.add(request);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                allOpenHelpRequestsProgressBar.setVisibility(View.GONE);
                emptyAllOpenHelpRequestsTextView.setVisibility(allOpenHelpRequestsList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewOpenHelpRequestsActivity.this, "שגיאה בטעינת בקשות עזרה: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                allOpenHelpRequestsProgressBar.setVisibility(View.GONE);
                emptyAllOpenHelpRequestsTextView.setVisibility(View.VISIBLE);
                emptyAllOpenHelpRequestsTextView.setText("שגיאה בטעינת בקשות עזרה.");
            }
        });
    }

    // Adapter עבור RecyclerView - ללא כפתור סגירה
    private static class AllOpenHelpRequestAdapter extends RecyclerView.Adapter<AllOpenHelpRequestViewHolder> {
        private List<HelpRequest> requests;

        public AllOpenHelpRequestAdapter(List<HelpRequest> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public AllOpenHelpRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_open_help_request, parent, false);
            return new AllOpenHelpRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AllOpenHelpRequestViewHolder holder, int position) {
            HelpRequest request = requests.get(position);
            holder.helpRequestContentTextView.setText(request.getContent());
            String dateTime = request.getDate() + " " + request.getTime();
            holder.helpRequestDateTimeTextView.setText(dateTime);
            holder.helpRequestPublisherTextView.setText(request.getFullName());
            holder.helpRequestApartmentNumberTextView.setText("דירה: " + request.getApartmentNumber());
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }
    }

    // ViewHolder עבור פריט בקשת עזרה - ללא כפתור סגירה
    public static class AllOpenHelpRequestViewHolder extends RecyclerView.ViewHolder {
        TextView helpRequestContentTextView;
        TextView helpRequestDateTimeTextView;
        TextView helpRequestPublisherTextView;
        TextView helpRequestApartmentNumberTextView;

        public AllOpenHelpRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            helpRequestContentTextView = itemView.findViewById(R.id.helpRequestContentTextView);
            helpRequestDateTimeTextView = itemView.findViewById(R.id.helpRequestDateTimeTextView);
            helpRequestPublisherTextView = itemView.findViewById(R.id.helpRequestPublisherTextView);
            helpRequestApartmentNumberTextView = itemView.findViewById(R.id.helpRequestApartmentNumberTextView);
        }
    }

    // מודל נתונים עבור בקשת עזרה (אותו מודל כמו קודם)
    public static class HelpRequest {
        private String content;
        private String date;
        private String time;
        private long timestamp;
        private boolean isOpen;
        private String publisherId;
        private String fullName;
        private String apartmentNumber;
        private String buildingCode;

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

        public String getBuildingCode() {
            return buildingCode;
        }

        public void setBuildingCode(String buildingCode) {
            this.buildingCode = buildingCode;
        }

        public void setOpen(boolean open) {
            isOpen = open;
        }
    }
}