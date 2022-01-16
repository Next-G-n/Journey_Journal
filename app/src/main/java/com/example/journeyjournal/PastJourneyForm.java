package com.example.journeyjournal;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.journeyjournal.adapter.AdapterImageViewWithLike;
import com.example.journeyjournal.models.SliderItem;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PastJourneyForm extends AppCompatActivity implements LocationListener{
    private static final Object REQUEST_CODE = 1;
    private TextView date_picker, maptest;
    private TextInputLayout date_picker2;
    int PLACE_PICKER_REQUEST = 1;
    String[] locationPermission;
    PlacesClient placesClient;
    LocationManager locationManager;
    TextInputEditText location_Edit,description_edit,title_edit;
    TextInputLayout location_layout,description_layout,title_layout;
    private Button submit_btn;
    Dialog dialog;
    List<SliderItem> sliderItems;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser userID;
    private StorageReference mStorageRef;
    private SliderItem sliderItem2;
    String actions,entry_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarAndStatusBar2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_journey_form);
        location_Edit=findViewById(R.id.location_edit);
        description_edit=findViewById(R.id.description_edit);
        title_edit=findViewById(R.id.title_edit);
        submit_btn=findViewById(R.id.submit_post);
        location_layout=findViewById(R.id.location_layout);
        description_layout=findViewById(R.id.description_layout);
        title_layout=findViewById(R.id.title_layout);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firestore=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();

        sliderItems=new ArrayList<>();




        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationEnabled();
        getLocation();

        Intent intent=getIntent();
        entry_Id=intent.getStringExtra("Entry_Id");
        actions=intent.getStringExtra("Action");

        if(actions.equals("Updating_entry")){
            submit_btn.setText("Update Entry");
            loadEntryInfoDetails();
        }


        //
        // PASTE THE LINES BELOW THIS COMMENT
        //



        locationPermission = new String[]{ACCESS_FINE_LOCATION, Manifest.permission.LOCATION_HARDWARE};

        date_picker = findViewById(R.id.date_picker_actions2);



        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select A Date");

        final MaterialDatePicker<Pair<Long, Long>> materialDatePicker = builder.build();

        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                date_picker.setText(materialDatePicker.getHeaderText());
            }
        });
       // materialDatePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener) selection -> date_picker.setText(materialDatePicker.getHeaderText()));

        submit_btn.setOnClickListener(view -> {
                UploadInfo();

        });




    }


    private void loadEntryInfoDetails() {
        DocumentReference docRef = firestore.collection("Post Entries").document(entry_Id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String title1=document.getString("Title");
                        String location1=document.getString("Location");
                        String date1=document.getString("Date");
                        String description1=document.getString("Description");

                        title_edit.setText(title1);
                        description_edit.setText(description1);
                        location_Edit.setText(location1);
                        date_picker.setText(date1);
                    } else {
                        System.out.println("Error 2");
                    }
                } else {
                    System.out.println("Error 1");
                }
            }
        });
    }


    private void UploadInfo() {
        loading();
        final String title=Objects.requireNonNull(title_edit.getText()).toString().trim();
        final String date=date_picker.getText().toString().trim();
        final String desc=Objects.requireNonNull(description_edit.getText()).toString().trim();
        final String location= Objects.requireNonNull(location_Edit.getText()).toString().trim();

        Intent intent = this.getIntent();
        String timeStampPost = intent.getStringExtra("timeStampPost");
        if(TextUtils.isEmpty(title)) {
            clearError();
            title_layout.setError("Please fill this field");
            dialog.dismiss();
        }else if(TextUtils.isEmpty(location)){
            clearError();
            location_layout.setError("Please fill this field");
            dialog.dismiss();
        }else if(date.equals("Date")){
            clearError();
            date_picker.setBackgroundResource(R.drawable.text_field_error);
            Toast.makeText(this,"Choose Start date and End Date",Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }else if(TextUtils.isEmpty(desc)){
            clearError();
            description_layout.setError("Please fill this field");
            dialog.dismiss();
        }else {


            //Store information in FireStore
            userID=auth.getCurrentUser();
            String uid=userID.getUid();

            DocumentReference documentReference=null;
            if(actions.equals("Updating_entry")) {
                documentReference = firestore.collection("Post Entries").document(entry_Id);
            }else {
                documentReference = firestore.collection("Post Entries").document("Past Journey_" + timeStampPost);
            }
            Map<String,Object> post_info=new HashMap<>();
            post_info.put("Title",title);
            post_info.put("Location",location);
            post_info.put("Date",date);
            post_info.put("Description",desc);

            documentReference.update(post_info).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    dialog.dismiss();
                    startActivity(new Intent(PastJourneyForm.this,Home.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Log.w(TAG,"Error adding document" +e);
                }
            });

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {

        }
    }

    private void clearError(){
        title_layout.setError(null);
        description_layout.setError(null);
        location_layout.setError(null);
        date_picker.setBackgroundResource(R.drawable.new_text_field);
    }
    private void loading(){
        dialog= new Dialog(PastJourneyForm.this);
        dialog.setContentView(R.layout.custom_dialog_loading);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dialog));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();
    }


    private void locationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(PastJourneyForm.this)
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show Near Places around you.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            location_Edit.setText(addresses.get(0).getCountryName()+", "+addresses.get(0).getLocality());
            System.out.println("this man");


        } catch (Exception e) {
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



}