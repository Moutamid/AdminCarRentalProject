package com.moutamid.admincarrentalproject.ui.requests;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.admincarrentalproject.R;

import java.util.ArrayList;

public class RequestsFragment extends Fragment {
    private static final String TAG = "RequestsFragment";


    private ArrayList<RequestBookingModel> tasksArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private View root;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//    private

    private ProgressDialog progressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_requests, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        databaseReference.child("requests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "No data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        tasksArrayList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            tasksArrayList.add(dataSnapshot.getValue(RequestBookingModel.class));

                        }

                        initRecyclerView();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

//        initRecyclerView();

        return root;
    }

    private void initRecyclerView() {

        conversationRecyclerView = root.findViewById(R.id.requests_recyclerview);
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        progressDialog.dismiss();

        if (adapter.getItemCount() != 0) {
            //        noChatsLayout.setVisibility(View.GONE);
            //        chatsRecyclerView.setVisibility(View.VISIBLE);

        }

    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_requests, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            RequestBookingModel model = tasksArrayList.get(position);

            Glide.with(getActivity())
                    .asBitmap()
                    .load(model.getCarImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.lighterGrey)
                            .error(R.color.lighterGrey)
                    )
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(holder.imageViewH);

            holder.statusH.setText(model.getStatus());

            holder.carNameH.setText(model.getCarName());

            holder.userNameH.setText(model.getMyName());

            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), RequestViewerActivity.class);
                    intent.putExtra("uid", model.getMyUid());
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            if (tasksArrayList == null)
                return 0;
            return tasksArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView userNameH, carNameH, statusH;
            ImageView imageViewH;
            RelativeLayout parentLayout;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                userNameH = v.findViewById(R.id.user_name_layout_request);
                carNameH = v.findViewById(R.id.car_name_layout_request);
                statusH = v.findViewById(R.id.status_layout_car_request);
                imageViewH = v.findViewById(R.id.car_image_view_layout_request);
                parentLayout = v.findViewById(R.id.parent_layout_request);

            }
        }

    }

    private static class RequestBookingModel {

        private String carKey, carName, carImageUrl, myName, myUid,
                licenseNumber, status;
        private int totalMileages, totalCost;

        public RequestBookingModel(String carKey, String carName, String carImageUrl, String myName, String myUid, String licenseNumber, String status, int totalMileages, int totalCost) {
            this.carKey = carKey;
            this.carName = carName;
            this.carImageUrl = carImageUrl;
            this.myName = myName;
            this.myUid = myUid;
            this.licenseNumber = licenseNumber;
            this.status = status;
            this.totalMileages = totalMileages;
            this.totalCost = totalCost;
        }

        public String getCarName() {
            return carName;
        }

        public void setCarName(String carName) {
            this.carName = carName;
        }

        public String getCarImageUrl() {
            return carImageUrl;
        }

        public void setCarImageUrl(String carImageUrl) {
            this.carImageUrl = carImageUrl;
        }

        public int getTotalMileages() {
            return totalMileages;
        }

        public void setTotalMileages(int totalMileages) {
            this.totalMileages = totalMileages;
        }

        public int getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(int totalCost) {
            this.totalCost = totalCost;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLicenseNumber() {
            return licenseNumber;
        }

        public void setLicenseNumber(String licenseNumber) {
            this.licenseNumber = licenseNumber;
        }

        public String getCarKey() {
            return carKey;
        }

        public void setCarKey(String carKey) {
            this.carKey = carKey;
        }

        public String getMyName() {
            return myName;
        }

        public void setMyName(String myName) {
            this.myName = myName;
        }

        public String getMyUid() {
            return myUid;
        }

        public void setMyUid(String myUid) {
            this.myUid = myUid;
        }

        RequestBookingModel() {
        }
    }

}