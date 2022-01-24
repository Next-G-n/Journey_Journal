package com.example.journeyjournal;

import static android.content.ContentValues.TAG;
import java.net.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.journeyjournal.adapter.AdapterImageView;
import com.example.journeyjournal.adapter.AdapterImageViewWithLike;
import com.example.journeyjournal.models.SliderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Add_New_Journey_Entry extends AppCompatActivity {

    private ViewPager2 viewPager2;
    Button next_btn;
    FloatingActionButton upload_image_btn;
    private Handler sliderHandler=new Handler();
    private int IMAGE_CODE=1;
    List<SliderItem> sliderItems;
    AdapterImageView adapterImageView;
    RecyclerView recyclerView;
    LinearLayout linearLayout_Btn;
    private StorageReference mStorageRef;
    private FirebaseAuth auth;
    private Dialog dialog;
    private FirebaseFirestore firestore;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    Uri imge_uri=null;
    private DatabaseReference UsersRef;
    String [] cameraPermission;
    String[] storagePermission;
    String timeStampPost="Nothing";
    String downloadUri;
    String actions,entry_Id;
    int imageCount;
    private TextView header_images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarAndStatusBar2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_journey_entry);

        viewPager2=findViewById(R.id.viewPageImageSlider);
        next_btn=findViewById(R.id.next_btn);
        upload_image_btn=findViewById(R.id.upload_Images_float);
        header_images=findViewById(R.id.header_images);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Intent intent=getIntent();
        entry_Id=intent.getStringExtra("Entry_Id");
        actions=intent.getStringExtra("Action");

        if(actions.equals("Updating_entry")){
            next_btn.setText("Update Images");
            loadImages();
        }


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
                sliderHandler.postDelayed(sliderRunner, 8000);
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sliderItems.isEmpty()){
                    Toast.makeText(Add_New_Journey_Entry.this,"Please Upload images",Toast.LENGTH_SHORT).show();
                }else{
                    if (actions.equals("Updating_entry")){
                        UpdateImages();
                    }else {
                        uploadImagesInfo();
                    }

                }
                System.out.println("this error : "+sliderItems.size());


            }
        });
    }

    private void UpdateImages() {
        FirebaseUser userID=auth.getCurrentUser();
        String uid=userID.getUid();
        DocumentReference documentReference=firestore.collection("Post Entries").document(entry_Id);
        Map<String,Object> post_info=new HashMap<>();
        post_info.put("imageCount",sliderItems.size());

        int i;
        for(i=0;i<sliderItems.size();++i){

            if(sliderItems.get(i).getImageName().equals("Nothing")){
                post_info.put("image"+i,sliderItems.get(i).getFromDatabase());
            }else{
                Toast.makeText(Add_New_Journey_Entry.this, "this add an image", Toast.LENGTH_SHORT).show();
                uploadImageToDb(sliderItems.get(i).getImageName(),sliderItems.get(i).getImage(),uid,i);
            }
        }
        documentReference.set(post_info, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent i = new Intent(Add_New_Journey_Entry.this, Journey_Entry_Details.class);
                i.putExtra("Entry_Id",entry_Id);
                startActivity(i);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"Error adding document" +e);
            }
        });
    }

    private void loadImages() {
        DocumentReference docRef = firestore.collection("Post Entries").document(entry_Id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String title1=document.getString("Title");
                        imageCount= Objects.requireNonNull(document.getLong("imageCount")).intValue();
                        for(int i=0;i<imageCount;++i) {
                            SliderItem modalClass = new SliderItem(document.getString("image" + i),"Nothing");
                            sliderItems.add(modalClass);
                        }
                        adapterImageView = new AdapterImageView(Add_New_Journey_Entry.this, sliderItems,viewPager2);

                        viewPager2.setAdapter(new AdapterImageView(Add_New_Journey_Entry.this,sliderItems ,viewPager2));

                        header_images.setText(title1);
                    } else {
                        System.out.println("Error 2");
                    }
                } else {
                    System.out.println("Error 1");
                }
            }
        });
    }



    private void uploadImagesInfo() {
        if(timeStampPost.equals("Nothing")){
            timeStampPost= String.valueOf(System.currentTimeMillis());
        }
        FirebaseUser userID=auth.getCurrentUser();
        String uid=userID.getUid();
        DocumentReference documentReference=firestore.collection("Post Entries").document("Past Journey_"+timeStampPost);
        Map<String,Object> post_info=new HashMap<>();
        post_info.put("Title",null);
        post_info.put("Location",null);
        post_info.put("Date",null);
        post_info.put("Description",null);
        post_info.put("UserId",uid);
        post_info.put("imageCount",sliderItems.size());
        post_info.put("EntryId","Past Journey_"+timeStampPost);
        int i;
        for(i=0;i<sliderItems.size();++i){
            if(sliderItems.get(i).getImageName().equals("Nothing")){
                post_info.put("image"+i,sliderItems.get(i).getFromDatabase());
            }else{
                uploadImageToDb(sliderItems.get(i).getImageName(),sliderItems.get(i).getImage(),uid,i);
            }
            //post_info.put("image"+i,downloadUri);
        }
        documentReference.set(post_info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent i = new Intent(Add_New_Journey_Entry.this, PastJourneyForm.class);
                i.putExtra("Action","add_new_entry");
                i.putExtra("timeStampPost", timeStampPost);
                startActivity(i);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"Error adding document" +e);
            }
        });
    }

    private String uploadImageToDb(String imageName, Uri image, String uid,int i) {
        StorageReference mRef = mStorageRef.child(uid).child(imageName);

        mRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadUri2=uriTask.getResult().toString();
                DocumentReference documentReference=null;
                if(actions.equals("Updating_entry")) {
                    documentReference = firestore.collection("Post Entries").document(entry_Id);
                }else{
                    documentReference = firestore.collection("Post Entries").document("Past Journey_" + timeStampPost);
                }
                Map<String,Object> post_info=new HashMap<>();
                post_info.put("image"+i,downloadUri2);
                Toast.makeText(Add_New_Journey_Entry.this,"works well2 "+downloadUri2,Toast.LENGTH_SHORT).show();
                documentReference.update(post_info);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        System.out.println("All Okay :"+downloadUri);
        return downloadUri;
    }

    public void removeItem(int position){
        sliderItems.remove(position);
        adapterImageView.notifyItemRemoved(position);
    }

    private void showImagePickDialog() {

        dialog= new Dialog(Add_New_Journey_Entry.this);
        dialog.setContentView(R.layout.image_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dalog_white));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        Button camera=dialog.findViewById(R.id.camera);
        Button gallery=dialog.findViewById(R.id.gallery);



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

        gallery.setOnClickListener(view -> {
            //gallery clicked
            if (!checkStoragePermission()){
                requestStoragePermission();
            }else {
                pickFromGallery();
                dialog.dismiss();
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
        ActivityCompat.requestPermissions(Add_New_Journey_Entry.this,storagePermission,STORAGE_REQUEST_CODE
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
        ActivityCompat.requestPermissions(Add_New_Journey_Entry.this,cameraPermission,CAMERA_REQUEST_CODE
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
                        uploadImageStore(imageUri,i);

                    }


                } else if (data.getData() != null) {
                    uploadImageStore(imge_uri, 1);
                }

            }


        }else if(requestCode==IMAGE_PICK_CAMERA_CODE){

            uploadImageStore(imge_uri, 1);

        }







    }

    private  void uploadImageStore(Uri imge_uri, int i) {

        final String timeStamp= String.valueOf(System.currentTimeMillis());

        SliderItem modalClass = new SliderItem(imge_uri,timeStamp);
        sliderItems.add(modalClass);

        adapterImageView = new AdapterImageView(Add_New_Journey_Entry.this, sliderItems,viewPager2);

        viewPager2.setAdapter(new AdapterImageView(Add_New_Journey_Entry.this,sliderItems ,viewPager2));

        FirebaseUser userID=auth.getCurrentUser();
        String uid=userID.getUid();
        // uploadImage(timeStamp,imge_uri,uid,i);
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