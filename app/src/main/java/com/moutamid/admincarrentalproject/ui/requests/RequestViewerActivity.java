package com.moutamid.admincarrentalproject.ui.requests;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.admincarrentalproject.R;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.admincarrentalproject.R.color.lighterGrey;

public class RequestViewerActivity extends AppCompatActivity {
    private static final String TAG = "RequestViewerActivity";

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_viewer);

        initViews();

        String uid = getIntent().getStringExtra("uid");

        databaseReference.child("requests").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(RequestViewerActivity.this, "No data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RequestBookingModel requestBookingModel = snapshot.getValue(RequestBookingModel.class);

                        String startDateString = "25/04/21",
                                endDateString = "29/04/21",
                                currentMileagesStr = "not started";

                        if (snapshot.child("start_date").exists()) {
                            startDateString = snapshot.child("start_date").getValue(String.class);
                        }
                        if (snapshot.child("end_date").exists()) {
                            endDateString = snapshot.child("end_date").getValue(String.class);
                        }
                        if (snapshot.child("currentMileages").exists()) {
                            currentMileagesStr = String.valueOf(
                                    snapshot.child("currentMileages")
                                            .getValue(Double.class)
                            );
                        }

                        setValuesOnViews(requestBookingModel, startDateString, endDateString, currentMileagesStr);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(RequestViewerActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void initViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    private void setValuesOnViews(RequestBookingModel requestBookingModel, String startDateString, String endDateString, String currentMileagesStr) {
        TextView name = findViewById(R.id.user_name_layout_request_viewer);
        TextView carname = findViewById(R.id.car_name_layout_request_viewer);
        TextView status = findViewById(R.id.status_layout_car_request_viewer);
        TextView license = findViewById(R.id.license_textview_request_viewer);
        TextView startdate = findViewById(R.id.start_date_viewer);
        TextView enddate = findViewById(R.id.end_date_viewer);
        TextView totalcost = findViewById(R.id.total_cost_viewer);
        TextView currentmileages = findViewById(R.id.current_mileages_text_view_viewer);
        TextView totalmileages = findViewById(R.id.total_mileages_viewer);

        // SETTING VALUES ON TEXT VIEWS
        name.setText(requestBookingModel.getMyName());
        carname.setText(requestBookingModel.getCarName());
        status.setText(requestBookingModel.getStatus());
        license.setText(requestBookingModel.getLicenseNumber());
        startdate.setText(startDateString);
        enddate.setText(endDateString);
        totalcost.setText(requestBookingModel.getTotalCost() + "");
        currentmileages.setText(currentMileagesStr);
        totalmileages.setText(requestBookingModel.getTotalMileages() + "");

        // CHECK IF USER EXCEEDED HIS MILEAGE VALUES
        if (!currentMileagesStr.equals("not started")) {
            if (Double.parseDouble(currentMileagesStr) > requestBookingModel.getTotalMileages()) {
                findViewById(R.id.fine_cost_viewer).setVisibility(View.VISIBLE);
                findViewById(R.id.exceeded_mileage_viewer).setVisibility(View.VISIBLE);
            }
        }

        // LOAD CAR IMAGE

        ImageView imageView = findViewById(R.id.car_image_view_layout_request_viewer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!RequestViewerActivity.this.isDestroyed())
                with(RequestViewerActivity.this)
                        .asBitmap()
                        .load(requestBookingModel.getCarImageUrl())
                        .apply(new RequestOptions()
                                .placeholder(lighterGrey)
                                .error(lighterGrey)
                        )
                        .diskCacheStrategy(DATA)
                        .into(imageView);
        }

        if (getIntent().hasExtra("history")){
            // SHOWING ONLY HISTORY SO NO ON CLICK LISTENERS NEEDED
            progressDialog.dismiss();
            return;
        }

        findViewById(R.id.bottom_layout_request).setVisibility(View.VISIBLE);

        LinearLayout acceptRejectLayout = findViewById(R.id.acceptRejectLayout_request);
        RelativeLayout startStopLayout = findViewById(R.id.startStopLayout_request);

        // CHECK IF STATUS IS PENDING OR ACCEPTED
        if (requestBookingModel.getStatus().equals("pending")) {
            acceptRejectLayout.setVisibility(View.VISIBLE);
            startStopLayout.setVisibility(View.GONE);

        } else if (requestBookingModel.getStatus().equals("rejected")) {
            acceptRejectLayout.setVisibility(View.VISIBLE);
            startStopLayout.setVisibility(View.GONE);

        } else {
            acceptRejectLayout.setVisibility(View.GONE);
            startStopLayout.setVisibility(View.VISIBLE);
        }

        // CHECK IF TRACKING IS ALREADY STARTED OR NOT
        databaseReference.child("requests").child(requestBookingModel.getMyUid())
                .child("tracker_started")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            return;
                        }

                        final TextView startStopText = findViewById(R.id.start_driving_textview_viewer);
                        final ImageView gpsImageView = findViewById(R.id.tracker_image_gps_viewer);
                        boolean value = snapshot.getValue(Boolean.class);

                        if (value) {
                            // TRACKER_STARTED
                            startStopLayout.setBackgroundResource(R.drawable.bg_stop_tracker_btn);
                            startStopText.setText("STOP");
                            gpsAnimation = YoYo.with(Techniques.Flash).duration(4000).delay(1000).repeat(10000)
                                    .playOn(gpsImageView);
                            golden = false;

                        } else {
                            // TRACKER_STOPPED
                            startStopLayout.setBackgroundResource(R.drawable.bg_get_started_btn);
                            startStopText.setText("Start tracker");
                            gpsAnimation.stop();
                            golden = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RequestViewerActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // SET ON CLICK LISTENERS ON BOTTOM BUTTONS
        findViewById(R.id.acceptBtn_request).setOnClickListener(acceptBtnClickListeer(requestBookingModel.getMyUid()));
        findViewById(R.id.rejectBtn_request).setOnClickListener(rejectBtnClickListeer(requestBookingModel.getMyUid()));
        startStopLayout.setOnClickListener(startStopLayoutClickListener(requestBookingModel.getMyUid()));

        progressDialog.dismiss();

    }

    private boolean golden = true;
    private YoYo.YoYoString gpsAnimation;

    private View.OnClickListener startStopLayoutClickListener(String myUid) {
        final RelativeLayout startStopLayout = findViewById(R.id.startStopLayout_request);
        final TextView startStopText = findViewById(R.id.start_driving_textview_viewer);
        final ImageView gpsImageView = findViewById(R.id.tracker_image_gps_viewer);

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (golden) {
                    // TRACKER_STARTED
                    startStopLayout.setBackgroundResource(R.drawable.bg_stop_tracker_btn);
                    startStopText.setText("STOP");

                    gpsAnimation = YoYo.with(Techniques.Flash).duration(4000).delay(1000).repeat(10000)
                            .playOn(gpsImageView);

                    golden = false;

                    databaseReference.child("requests").child(myUid)
                            .child("tracker_started")
                            .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                startStopLayout.setBackgroundResource(R.drawable.bg_get_started_btn);
                                startStopText.setText("Start tracker");
                                gpsAnimation.stop();
                                golden = true;
                                Toast.makeText(RequestViewerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    // TRACKER_STOPPED
                    startStopLayout.setBackgroundResource(R.drawable.bg_get_started_btn);
                    startStopText.setText("Start tracker");

                    gpsAnimation.stop();

                    golden = true;

                    databaseReference.child("requests").child(myUid)
                            .child("tracker_started")
                            .setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                startStopLayout.setBackgroundResource(R.drawable.bg_stop_tracker_btn);
                                startStopText.setText("STOP");
                                gpsAnimation = YoYo.with(Techniques.Flash).duration(4000).delay(1000).repeat(10000)
                                        .playOn(gpsImageView);
                                golden = false;
                                Toast.makeText(RequestViewerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        };
    }

    private View.OnClickListener rejectBtnClickListeer(String myUid) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                databaseReference.child("requests").child(myUid)
                        .child("status")
                        .setValue("rejected").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if (!task.isSuccessful()) {
                            Toast.makeText(RequestViewerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
    }

    private View.OnClickListener acceptBtnClickListeer(String myUid) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                databaseReference.child("requests").child(myUid)
                        .child("currentMileages")
                        .setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(RequestViewerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            databaseReference.child("requests").child(myUid)
                                    .child("status")
                                    .setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();

                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RequestViewerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        };
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