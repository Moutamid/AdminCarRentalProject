package com.moutamid.admincarrentalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.admincarrentalproject.ui.requests.RequestViewerActivity;
import com.moutamid.admincarrentalproject.ui.requests.RequestsFragment;

import java.util.ArrayList;

import static android.view.LayoutInflater.from;

public class CarHistoryFragment extends Fragment {

    private ArrayList<String> carNamesList = new ArrayList<>();

    private ArrayList<RequestBookingModel> carBookingsArrayList = new ArrayList<>();

    public CarHistoryFragment() {
        // Required empty public constructor
    }

    private View view;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private String currentCarName;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_car_history, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        databaseReference.child("cars").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "No Cars exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                carNamesList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    carNamesList.add(dataSnapshot.child("name").getValue(String.class));
                }

                currentCarName = carNamesList.get(0);

                databaseReference.child("booking_history")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "No history exist", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                carBookingsArrayList.clear();

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                    carBookingsArrayList.add(dataSnapshot.getValue(RequestBookingModel.class));

                                }

                                initNamesRecyclerView();

//                                initRecyclerView();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), error.toException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private RecyclerView carBookingsRecyclerView;
    private RecyclerViewAdapterCarBookings carBookingsAdapter;

    private void initCarBookingsRecyclerView(String currentCarName) {

        ArrayList<RequestBookingModel> currentBookingsList = new ArrayList<>();

        currentBookingsList.clear();

        for (int i = 0; i <= carBookingsArrayList.size() - 1; i++) {

            if (carBookingsArrayList.get(i).getCarName().equals(currentCarName)) {
                currentBookingsList.add(carBookingsArrayList.get(i));
            }

        }

        carBookingsRecyclerView = view.findViewById(R.id.car_bookings_recycler_view);
        carBookingsRecyclerView.addItemDecoration(new DividerItemDecoration(carBookingsRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        carBookingsAdapter = new RecyclerViewAdapterCarBookings(currentBookingsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        carBookingsRecyclerView.setLayoutManager(linearLayoutManager);
        carBookingsRecyclerView.setHasFixedSize(true);
        carBookingsRecyclerView.setNestedScrollingEnabled(false);

        carBookingsRecyclerView.setAdapter(carBookingsAdapter);

        //    if (adapter.getItemCount() != 0) {

        //        noChatsLayout.setVisibility(View.GONE);
        //        chatsRecyclerView.setVisibility(View.VISIBLE);

        //    }

        progressDialog.dismiss();


    }

    private class RecyclerViewAdapterCarBookings extends Adapter
            <RecyclerViewAdapterCarBookings.ViewHolderRightMessage> {

        ArrayList<RequestBookingModel> currentBookingsList1;

        public RecyclerViewAdapterCarBookings(ArrayList<RequestBookingModel> currentBookingsList1) {
            this.currentBookingsList1 = currentBookingsList1;
        }

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_requests, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            RequestBookingModel model = currentBookingsList1.get(position);

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
                    intent.putExtra("uid", model.getPushKey());
                    intent.putExtra("history", "yes");
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (currentBookingsList1 == null)
                return 0;
            return currentBookingsList1.size();
        }

        public class ViewHolderRightMessage extends ViewHolder {

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

    //--------------------------------------------------------------------------------
    //-------------------------NAMES RECYCLER VIEW------------------------------------
    private RecyclerView carNamesRecyclerView;
    private RecyclerViewAdapterCarNames carNamesAdapter;

    private void initNamesRecyclerView() {

        carNamesRecyclerView = view.findViewById(R.id.car_names_recycler_view);
        carNamesAdapter = new RecyclerViewAdapterCarNames();
        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        carNamesRecyclerView.setLayoutManager(layoutManagerUserFriends);
        carNamesRecyclerView.setHasFixedSize(true);
        carNamesRecyclerView.setNestedScrollingEnabled(false);

        carNamesRecyclerView.setAdapter(carNamesAdapter);

        initCarBookingsRecyclerView(currentCarName);
    }

    private class RecyclerViewAdapterCarNames extends Adapter
            <RecyclerViewAdapterCarNames.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_car_names, parent, false);
            return new ViewHolderRightMessage(view);
        }

        boolean firstTime = true;
        ViewHolderRightMessage prevHolder;

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            if (firstTime) {
                setPressedState(holder);
                prevHolder = holder;
                firstTime = false;
            }
            holder.nameTxt.setText(carNamesList.get(position));
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removePrevPressedState();
                    setPressedState(holder);

                    initCarBookingsRecyclerView(carNamesList.get(position));
//
//                    Toast.makeText(getActivity(), carNamesList.get(position),
//                            Toast.LENGTH_SHORT).show();
                }
            });

        }

        private void setPressedState(ViewHolderRightMessage holder) {

            holder.parentLayout.setBackgroundResource(R.drawable.bg_get_started_btn);
            holder.nameTxt.setTextColor(Color.WHITE);
            prevHolder = holder;
        }

        private void removePrevPressedState() {
            prevHolder.parentLayout.setBackgroundResource(R.drawable.bg_get_started_btn_2);
            prevHolder.nameTxt.setTextColor(getActivity().getResources().getColor(R.color.goldenrodd));
        }

        @Override
        public int getItemCount() {
            if (carNamesList == null)
                return 0;
            return carNamesList.size();
        }

        public class ViewHolderRightMessage extends ViewHolder {

            TextView nameTxt;
            RelativeLayout parentLayout;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                nameTxt = v.findViewById(R.id.name_textview_layout_car_name);
                parentLayout = v.findViewById(R.id.parent_layout_car_name);

            }
        }

    }

    //---------------------------------------------------------------------------------

    private static class RequestBookingModel {

        private String start_date, end_date, booking_date, pushKey;
        private String carKey, carName, carImageUrl, myName, myUid,
                licenseNumber, status;
        private int totalMileages, totalCost;

        public RequestBookingModel(String start_date, String end_date, String booking_date, String pushKey, String carKey, String carName, String carImageUrl, String myName, String myUid, String licenseNumber, String status, int totalMileages, int totalCost) {
            this.start_date = start_date;
            this.end_date = end_date;
            this.booking_date = booking_date;
            this.pushKey = pushKey;
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

        public String getPushKey() {
            return pushKey;
        }

        public void setPushKey(String pushKey) {
            this.pushKey = pushKey;
        }

        public String getStart_date() {
            return start_date;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }

        public String getEnd_date() {
            return end_date;
        }

        public void setEnd_date(String end_date) {
            this.end_date = end_date;
        }

        public String getBooking_date() {
            return booking_date;
        }

        public void setBooking_date(String booking_date) {
            this.booking_date = booking_date;
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