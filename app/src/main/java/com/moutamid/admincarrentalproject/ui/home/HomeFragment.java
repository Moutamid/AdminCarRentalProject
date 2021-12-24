package com.moutamid.admincarrentalproject.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.admincarrentalproject.R;
import com.moutamid.admincarrentalproject.Utils;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private Utils utils = new Utils();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private RelativeLayout addCarBtn;

    private ArrayList<CarModel> carsArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private View view;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        addCarBtn = view.findViewById(R.id.add_car_btn);
        addCarBtn.setOnClickListener(addCarBtnCLickLIstener());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("cars")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
//                    ProgressBar progressBar = view.findViewById(R.id.progress_bar_home);
//                    progressBar.setVisibility(View.GONE);
                            return;
                        }

                        carsArrayList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CarModel model = dataSnapshot.getValue(CarModel.class);
                            carsArrayList.add(model);
                        }

                        initRecyclerView();

//                if (getActivity()!=null) {
//
//
//                }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + error.getMessage());
                    }
                });

        return view;
    }

    private void initRecyclerView() {

        conversationRecyclerView = view.findViewById(R.id.home_recycler_views);
        conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapterMessages();
        //        LinearLayoutManager layoutManagerUserFriends = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //    int numberOfColumns = 3;
        //int mNoOfColumns = calculateNoOfColumns(getApplicationContext(), 50);
        //  recyclerView.setLayoutManager(new GridLayoutManager(this, mNoOfColumns));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
        conversationRecyclerView.setNestedScrollingEnabled(false);

        conversationRecyclerView.setAdapter(adapter);

        if (getActivity() != null) {

//            setDetailsOnFirstDeal();
//            setDetailsOnSecondDeal();
//            setDetailsOnThirdDeal();

        }

        if (adapter.getItemCount() != 0) {

//            ProgressBar progressBar = view.findViewById(R.id.progress_bar_home);
//            progressBar.setVisibility(View.GONE);

            //        noChatsLayout.setVisibility(View.GONE);
            //        chatsRecyclerView.setVisibility(View.VISIBLE);

        }


    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cars_home, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            CarModel carModel = carsArrayList.get(position);

//            ImageView carImage;
//            TextView name, transmission, model, seats, status, engine, price;
//            LinearLayout ac, gps;
            //ParentLayout

            holder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    utils.showDialog(getActivity(),
                            "Are you sure?",
                            "Do you really want to delete " + carModel.getName(),
                            "Yes",
                            "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    removeCarFromDatabase(position, carModel.getCarKey(), dialogInterface);
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }, true);

                    return false;
                }
            });

            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(getActivity(), carModel.getCarKey(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), AddCarActivity.class);
                    intent.putExtra("key", carModel.getCarKey());
                    startActivity(intent);
                }
            });

            if (carModel.isGPS())
                holder.gps.setVisibility(View.VISIBLE);

            if (carModel.isAC())
                holder.ac.setVisibility(View.VISIBLE);

            holder.price.setText("$" + carModel.getPerDayRate() + " /Mileage");

            holder.engine.setText(carModel.getEngine());

            holder.status.setText(carModel.getStatus());

            holder.seats.setText(carModel.getSeats() + " Seats");

            holder.model.setText(carModel.getModelYear());

            holder.transmission.setText(carModel.getTransmission());

            holder.name.setText(carModel.getName());

            Glide.with(getActivity())
                    .load(carModel.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.grey)
                            .error(R.color.grey)
                    )
                    .into(holder.carImage);

        }

        private void removeCarFromDatabase(int position, String carKey, DialogInterface dialogInterface) {
            dialogInterface.dismiss();

            carsArrayList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());

            databaseReference.child("cars").child(carKey).removeValue();
        }

        @Override
        public int getItemCount() {
            if (carsArrayList == null)
                return 0;
            return carsArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            ImageView carImage;
            TextView name, transmission, model, seats, status, engine, price;
            LinearLayout ac, gps;
            RelativeLayout parentLayout;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                carImage = v.findViewById(R.id.car_image_view_layout_home);
                name = v.findViewById(R.id.name_layout_car_home);
                transmission = v.findViewById(R.id.transmission_layout_car_home);
                model = v.findViewById(R.id.model_layout_car_home);
                seats = v.findViewById(R.id.seats_layout_car_home);
                status = v.findViewById(R.id.status_layout_car_home);
                engine = v.findViewById(R.id.engine_layout_car_home);
                price = v.findViewById(R.id.price_layout_car_home);
                ac = v.findViewById(R.id.ac_layout_car_home);
                gps = v.findViewById(R.id.gps_layout_car_home);
                parentLayout = v.findViewById(R.id.parent_layout_car_home);

            }
        }

    }

    private static class CarModel {

        private String name, modelYear, seats, transmission,
                status, perDayRate, engine, description, imageUrl, carKey;
        private boolean AC, GPS;

        public CarModel(String name, String modelYear, String seats, String transmission, String status, String perDayRate, String engine, String description, String imageUrl, String carKey, boolean AC, boolean GPS) {
            this.name = name;
            this.modelYear = modelYear;
            this.seats = seats;
            this.transmission = transmission;
            this.status = status;
            this.perDayRate = perDayRate;
            this.engine = engine;
            this.description = description;
            this.imageUrl = imageUrl;
            this.carKey = carKey;
            this.AC = AC;
            this.GPS = GPS;
        }

        public String getCarKey() {
            return carKey;
        }

        public void setCarKey(String carKey) {
            this.carKey = carKey;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getModelYear() {
            return modelYear;
        }

        public void setModelYear(String modelYear) {
            this.modelYear = modelYear;
        }

        public String getSeats() {
            return seats;
        }

        public void setSeats(String seats) {
            this.seats = seats;
        }

        public String getTransmission() {
            return transmission;
        }

        public void setTransmission(String transmission) {
            this.transmission = transmission;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPerDayRate() {
            return perDayRate;
        }

        public void setPerDayRate(String perDayRate) {
            this.perDayRate = perDayRate;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isAC() {
            return AC;
        }

        public void setAC(boolean AC) {
            this.AC = AC;
        }

        public boolean isGPS() {
            return GPS;
        }

        public void setGPS(boolean GPS) {
            this.GPS = GPS;
        }

        CarModel() {
        }
    }

    private View.OnClickListener addCarBtnCLickLIstener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddCarActivity.class));
            }
        };
    }
}