package com.example.journeyjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.journeyjournal.adapter.AdapterImageView;
import com.example.journeyjournal.models.SliderItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class UploadPostInfo extends AppCompatActivity {

    private ViewPager2 viewPager2;
    Button next_btn,upload_image_btn;
    private Handler sliderHandler=new Handler();
    private int IMAGE_CODE=1;
    List<SliderItem> sliderItems;
    AdapterImageView adapterImageView;
    RecyclerView recyclerView;
    LinearLayout linearLayout_Btn;
    private StorageReference mStorageRef;
    private FirebaseAuth auth;
    private Dialog dialog;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    Uri imge_uri=null;
    String [] cameraPermission;
    String[] storagePermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post_info);
        viewPager2=findViewById(R.id.viewPageImageSlider);
        next_btn=findViewById(R.id.next_btn);
        upload_image_btn=findViewById(R.id.upload_Images_btn);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        auth=FirebaseAuth.getInstance();


        upload_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        //init permission array
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        sliderItems=new ArrayList<>();
       // sliderItems.add(new SliderItem(R.drawable.com_facebook_auth_dialog_background));



        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer =new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r=1 -Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        viewPager2.setPageTransformer(compositePageTransformer);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunner);
                sliderHandler.postDelayed(sliderRunner, 3000);
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UploadPostInfo.this,PastJourneyForm.class));

            }
        });

    }

    public void removeItem(int position){
        sliderItems.remove(position);
        adapterImageView.notifyItemRemoved(position);
    }

    private void showImagePickDialog() {

        dialog= new Dialog(UploadPostInfo.this);
        dialog.setContentView(R.layout.image_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dalog_white));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        TextView camera=dialog.findViewById(R.id.camera);
        TextView gallery=dialog.findViewById(R.id.gallery);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //camera clicked
                if (!checkCameraPermission()){
                    requestCameraPermission();
                }else {
                    pickFromCamera();
                    dialog.dismiss();
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gallery clicked
                if (!checkStoragePermission()){
                    requestStoragePermission();
                }else {
                    pickFromGallery();
                    dialog.dismiss();
                }
            }
        });

    }

    private boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.
                permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromCamera() {
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        imge_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);


        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imge_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(UploadPostInfo.this,storagePermission,STORAGE_REQUEST_CODE
        );
    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.
                permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result2= ContextCompat.checkSelfPermission(this,Manifest.
                permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result2;
    }

    private void requestCameraPermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(UploadPostInfo.this,cameraPermission,CAMERA_REQUEST_CODE
        );
    }
    //handle permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Camera and Storage both permission are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if ( storageAccepted){
                        pickFromGallery();
                    }else {
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==IMAGE_PICK_GALLERY_CODE ){

            if (resultCode == RESULT_OK) {

                if (data.getClipData() != null) {


                    int totalitem = data.getClipData().getItemCount();

                    for (int i = 0; i < totalitem; i++) {

                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        viewPager2.setBackground(getDrawable(R.color.colorPrimaryLDark));
                        uploadImageStore(imageUri);

                    }


                } else if (data.getData() != null) {
                    uploadImageStore(imge_uri);
                }

            }


        }else if(requestCode==IMAGE_PICK_CAMERA_CODE){

            uploadImageStore(imge_uri);

            }







    }

    private  void uploadImageStore(Uri imge_uri){

        final String timeStamp= String.valueOf(System.currentTimeMillis());

        SliderItem modalClass = new SliderItem(imge_uri,timeStamp);
        sliderItems.add(modalClass);

        adapterImageView = new AdapterImageView(UploadPostInfo.this, sliderItems);

        viewPager2.setAdapter(new AdapterImageView(UploadPostInfo.this,sliderItems ));

        FirebaseUser user=auth.getCurrentUser();
        String userId= user.getUid();

        StorageReference mRef = mStorageRef.child(userId).child(timeStamp);

        mRef.putFile(imge_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(UploadPostInfo.this, "Upload Images", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadPostInfo.this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private Runnable sliderRunner =new Runnable() {
        @Override
        public void run() {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem()+1);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunner,3000);
    }
}