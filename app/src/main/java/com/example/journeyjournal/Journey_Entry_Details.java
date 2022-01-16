package com.example.journeyjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Journey_Entry_Details extends AppCompatActivity {

    LinearLayout edit,share,delete;
    TextView title,location,date,description;
    String entry_Id,fileUri,title1,location1,date1,description1;
    FirebaseFirestore firestore;
    int imageCount;
    List<SliderItem> sliderItems;
    AdapterImageViewWithLike adapterImageViewWithLike;
    ViewPager2 viewPager2;
    private Dialog dialog;
    private Handler sliderHandler=new Handler();
    Uri uri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarAndStatusBar2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_entry_details);
        edit=findViewById(R.id.edit_details);
        share=findViewById(R.id.share_details);
        delete=findViewById(R.id.delete_details);
        title=findViewById(R.id.title_view_details);
        location=findViewById(R.id.location_view_details);
        description=findViewById(R.id.description_view_details);
        date=findViewById(R.id.date_view_details);
        firestore= FirebaseFirestore.getInstance();
        Intent intent=getIntent();
        entry_Id=intent.getStringExtra("Entry_Id");
        viewPager2=findViewById(R.id.viewPageImageSlider_details);

        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        sliderItems=new ArrayList<>();

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


        loadEntryDetails();

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEntry();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDialog();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareJourneyEntry();
            }

        });

    }


    private void shareJourneyEntry(){
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (int i=0;i<sliderItems.size();++i){

            Picasso.get().load(sliderItems.get(i).getFromDatabase()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        File mydir = new File(Environment.getExternalStorageDirectory() + "/11zon");
                        if (!mydir.exists()) {
                            mydir.mkdirs();
                        }

                        fileUri = mydir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
                        FileOutputStream outputStream = new FileOutputStream(fileUri);
                        Toast.makeText(Journey_Entry_Details.this, "Nshathis "+fileUri, Toast.LENGTH_SHORT).show();

                        System.out.println("Nshathisi buy "+fileUri);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    if(fileUri== null) {
                    uri= Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
                            BitmapFactory.decodeFile(fileUri),null,null));
                    }
                    uri=getloacalBitmaUri(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
            imageUris.add(uri);
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Title :"+title1);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Date :"+date1);
        shareIntent.putExtra(Intent.EXTRA_TEXT,"Location :"+location1+ "\n"+"Description :"+description1);
        startActivity(Intent.createChooser(shareIntent, null));
    }
    public Uri getloacalBitmaUri(Bitmap bmp)
    {
        Uri bmpuri = null;
        try{
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"shareimage"+ System.currentTimeMillis()+".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG,90,out);
            out.close();

            bmpuri = FileProvider.getUriForFile(getApplicationContext(),"studio.harpreet.mybrowser.provider",file);


        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return bmpuri;
    }

    private void loadDialog(){
        dialog= new Dialog(Journey_Entry_Details.this);
        dialog.setContentView(R.layout.edit_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dalog_white));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        Button edit_images=dialog.findViewById(R.id.edit_Images);
        Button edit_info=dialog.findViewById(R.id.edit_info);
        edit_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Journey_Entry_Details.this, Add_New_Journey_Entry.class);
                i.putExtra("Entry_Id",entry_Id);
                i.putExtra("Action","Updating_entry");
                startActivity(i);
                dialog.dismiss();
            }
        });
        edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Journey_Entry_Details.this, PastJourneyForm.class);
                i.putExtra("Entry_Id",entry_Id);
                i.putExtra("Action","Updating_entry");
                startActivity(i);
                dialog.dismiss();
            }
        });
    }

    private void loadEntryDetails() {
        DocumentReference docRef = firestore.collection("Post Entries").document(entry_Id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                         title1=document.getString("Title");
                         location1=document.getString("Location");
                         date1=document.getString("Date");
                         description1=document.getString("Description");
                        imageCount= Objects.requireNonNull(document.getLong("imageCount")).intValue();
                        for(int i=0;i<imageCount;++i) {
                            SliderItem modalClass = new SliderItem(document.getString("image" + i),"Nothing");
                            sliderItems.add(modalClass);
                        }
                        adapterImageViewWithLike = new AdapterImageViewWithLike(Journey_Entry_Details.this, sliderItems,viewPager2);

                        viewPager2.setAdapter(new AdapterImageViewWithLike(Journey_Entry_Details.this,sliderItems ,viewPager2));

                        title.setText(title1);
                        description.setText(description1);
                        location.setText(location1);
                        date.setText(date1);
                    } else {
                        System.out.println("Error 2");
                    }
                } else {
                    System.out.println("Error 1");
                }
            }
        });
    }

    private void deleteEntry(){
        firestore.collection("Post Entries").document(entry_Id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Journey_Entry_Details.this, "Entry Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Journey_Entry_Details.this,Home.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Journey_Entry_Details.this, "Failed Entry Deleted", Toast.LENGTH_SHORT).show();
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