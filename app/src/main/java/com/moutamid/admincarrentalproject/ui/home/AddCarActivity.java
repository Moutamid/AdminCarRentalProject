package com.moutamid.admincarrentalproject.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.admincarrentalproject.R;
import com.moutamid.admincarrentalproject.Utils;

public class AddCarActivity extends AppCompatActivity {
    private static final String TAG = "AddCarActivity";
    private Utils utils = new Utils();
    private RelativeLayout addDoneCarBtn;

    private CarModel currentCarModel = new CarModel();

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private ProgressDialog progressDialog;

    private String carImageLink, nameStr, modelStr, seatsStr,
            rateStr, descriptionStr, transmissionStr = "Manual",
            statusStr = "Economy", engineStr = "Petrol";
    private static final int GALLERY_REQUEST = 1;
    private boolean gpsValue = false, acValue = false;

    private ImageView carImageView;
    private EditText nameEditText, modelEditText, seatsEditText,
            rateEditText, descriptionEditText;
    private SwitchCompat acSwitch, gpsSwitch;
    private RadioGroup transmissionRadioGroup, statusRadioGroup, engineRadioGroup;

    private String carKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        progressDialog = new ProgressDialog(AddCarActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        addDoneCarBtn = findViewById(R.id.add_done_car_btn);
        addDoneCarBtn.setOnClickListener(addDoneCarBtnClickListener());

        transmissionRadioGroup = findViewById(R.id.transmission_RadioGroup);
        statusRadioGroup = findViewById(R.id.status_RadioGroup);
        engineRadioGroup = findViewById(R.id.engine_RadioGroup);

        carImageView = findViewById(R.id.add_car_imageview);

        nameEditText = findViewById(R.id.name_et_add_car);
        modelEditText = findViewById(R.id.model_et_add_car);
        seatsEditText = findViewById(R.id.seats_et_add_car);
        rateEditText = findViewById(R.id.rate_et_add_car);
        descriptionEditText = findViewById(R.id.description_et_add_car);

        acSwitch = findViewById(R.id.acSwitchAddcar);
        gpsSwitch = findViewById(R.id.gpsSwitchAddCar);

        setCheckChangedListeners();

        setListenersOnRadioGroups();

        carImageView.setOnClickListener(carImageViewClickListener());

        if (getIntent().hasExtra("key")) {
            getDetailsOfCar();
        }

    }

    private void getDetailsOfCar() {
        progressDialog.show();
        String key = getIntent().getStringExtra("key");

        databaseReference.child("cars").child(key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(AddCarActivity.this, "No car exist of this name", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        currentCarModel = snapshot.getValue(CarModel.class);

                        setDetailsOnStrings();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCarActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCancelled: " + error.getMessage());
                    }
                }
        );
    }

    private void setDetailsOnStrings() {
        nameStr = currentCarModel.getName();
        carImageLink = currentCarModel.getImageUrl();
        modelStr = currentCarModel.getModelYear();
        seatsStr = currentCarModel.getSeats();
        acValue = currentCarModel.isAC();
        gpsValue = currentCarModel.isGPS();
        transmissionStr = currentCarModel.getTransmission();
        statusStr = currentCarModel.getStatus();
        rateStr = currentCarModel.getPerDayRate();
        engineStr = currentCarModel.getEngine();
        carKey = currentCarModel.getCarKey();
        descriptionStr = currentCarModel.getDescription();

        if (transmissionStr.toLowerCase().equals("manual"))
            transmissionRadioGroup.check(R.id.radio_btn_manual);

        if (transmissionStr.toLowerCase().equals("automatic"))
            transmissionRadioGroup.check(R.id.radio_btn_automatic);

        if (statusStr.toLowerCase().equals("economy"))
            statusRadioGroup.check(R.id.radio_btn_economy);

        if (statusStr.toLowerCase().equals("luxury"))
            statusRadioGroup.check(R.id.radio_btn_sports);

        if (engineStr.toLowerCase().equals("petrol"))
            engineRadioGroup.check(R.id.radio_btn_petrol);

        if (engineStr.toLowerCase().equals("gas"))
            engineRadioGroup.check(R.id.radio_btn_gas);

        nameEditText.setText(nameStr);
        modelEditText.setText(modelStr);
        seatsEditText.setText(seatsStr);
        acSwitch.setChecked(acValue);
        gpsSwitch.setChecked(gpsValue);
        rateEditText.setText(rateStr);
        descriptionEditText.setText(descriptionStr);

        carImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(AddCarActivity.this)
                .load(carImageLink)
                .apply(new RequestOptions()
                        .placeholder(R.color.grey)
                        .error(R.color.grey)
                )
                .into(carImageView);

        progressDialog.dismiss();

    }

    private View.OnClickListener addDoneCarBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getEditTextValues();

                if (anyEditTextIsEmpty())
                    return;

                uploadNewCar();

            }
        };
    }

    private boolean anyEditTextIsEmpty() {
        if (carImageLink == null || carImageLink.isEmpty()) {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (nameStr == null || nameStr.isEmpty()) {
            Toast.makeText(this, "Please enter an name!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (modelStr == null || modelStr.isEmpty()) {
            Toast.makeText(this, "Please enter a model number!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (seatsStr == null || seatsStr.isEmpty()) {
            Toast.makeText(this, "Please enter seat numbers!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (rateStr == null || rateStr.isEmpty()) {
            Toast.makeText(this, "Please enter a rate!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (descriptionStr == null || descriptionStr.isEmpty()) {
            Toast.makeText(this, "Please enter a description!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (transmissionStr == null || transmissionStr.isEmpty()) {
            Toast.makeText(this, "Please select a transmission!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (statusStr == null || statusStr.isEmpty()) {
            Toast.makeText(this, "Please select a status!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (engineStr == null || engineStr.isEmpty()) {
            Toast.makeText(this, "Please select an engine!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void getEditTextValues() {
        nameStr = nameEditText.getText().toString();
        modelStr = modelEditText.getText().toString();
        seatsStr = seatsEditText.getText().toString();
        rateStr = rateEditText.getText().toString();
        descriptionStr = descriptionEditText.getText().toString();
    }

    private void setCheckChangedListeners() {
        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                gpsValue = b;
            }
        });

        acSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                acValue = b;
            }
        });
    }

    private View.OnClickListener carImageViewClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            //imageUri = data.getData();
            Uri imageUri = data.getData();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("carImages");

            progressDialog.show();

            final StorageReference filePath = storageReference
                    .child(imageUri.getLastPathSegment() + "_" + utils.getRandomNmbr(9999));
//            final StorageReference filePath = storageReference.child("sliders")
//                    .child(imageUri.getLastPathSegment());

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri photoUrl) {
                            progressDialog.dismiss();
                            carImageLink = photoUrl.toString();
                            carImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            carImageView.setImageURI(data.getData());


                        }
                    });
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();

                    Toast.makeText(AddCarActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    private void setListenersOnRadioGroups() {
        engineRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_btn_petrol)
                    engineStr = "Petrol";
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_btn_gas)
                    engineStr = "Gas";
            }
        });
        statusRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_btn_economy)
                    statusStr = "Economy";
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_btn_sports)
                    statusStr = "Luxury";

            }
        });
        transmissionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_btn_manual)
                    transmissionStr = "Manual";
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_btn_automatic)
                    transmissionStr = "Automatic";

            }
        });
    }

    private void uploadNewCar() {
        progressDialog.show();

        if (carKey == null || carKey.isEmpty()) {
            String pushKey1 = databaseReference.child("cars").push().getKey();
            currentCarModel.setCarKey(pushKey1);
        }

//        CarModel model = new CarModel();
        currentCarModel.setName(nameStr);
        currentCarModel.setImageUrl(carImageLink);
        currentCarModel.setModelYear(modelStr);
        currentCarModel.setSeats(seatsStr);
        currentCarModel.setAC(acValue);
        currentCarModel.setGPS(gpsValue);
        currentCarModel.setTransmission(transmissionStr);
        currentCarModel.setStatus(statusStr);
        currentCarModel.setPerDayRate(rateStr);
        currentCarModel.setEngine(engineStr);
        currentCarModel.setDescription(descriptionStr);

        databaseReference.child("cars").child(currentCarModel.getCarKey()).setValue(currentCarModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            Toast.makeText(AddCarActivity.this, "Done", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddCarActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


//        String pushKey1 = databaseReference.child("cars").push().getKey();
//
//        CarModel model = new CarModel();
//        model.setName("Audi A7 Sportback");
//        model.setImageUrl("https://www.ultimatespecs.com/cargallery/2/9867/Audi-25_02_2018__A7-1.jpg");
//        model.setModelYear("2020");
//        model.setSeats("4");
//        model.setAC(true);
//        model.setGPS(true);
//        model.setTransmission("Automatic");
//        model.setStatus("Economy");
//        model.setPerDayRate("190");
//        model.setEngine("Petrol");
//        model.setCarKey(pushKey1);
//        model.setDescription("With a fuel consumption of 6.2 litres/100km - 46 mpg UK - 38 mpg US (Average), 0 to 100 km/h (62mph) in 6.9 seconds, a maximum top speed of 155 mph (250 km/h), a curb weight of 3693 lbs (1675 kgs), the A7 Sportback (C8) 45 TFSI has a turbocharged Inline 4 cylinder engine, Petrol motor.");
//
//        databaseReference.child("cars").child(pushKey1).setValue(model);
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

}