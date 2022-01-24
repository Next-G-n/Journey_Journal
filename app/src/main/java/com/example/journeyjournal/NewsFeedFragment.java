package com.example.journeyjournal;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.journeyjournal.adapter.AdapterImageViewWithLike;
import com.example.journeyjournal.adapter.AdapterNewsFeed;
import com.example.journeyjournal.models.PastJourney;
import com.example.journeyjournal.models.SliderItem;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFeedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    RecyclerView recyclerView;
    List<String> arrayList;
    AdapterNewsFeed adapterNewsFeed;
    FirebaseFirestore firestore;
    FirebaseUser userID;
    FirebaseAuth auth;
    Dialog dialog;
    FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    CardView clickProfile;
    String uid;
    StorageReference mStorageRef;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewsFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFeedFragment newInstance(String param1, String param2) {
        NewsFeedFragment fragment = new NewsFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_news_feed, container, false);
        recyclerView=view.findViewById(R.id.recycleView_post);
        auth= FirebaseAuth.getInstance();
        clickProfile=view.findViewById(R.id.clickProfile);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //linearLayoutManager=new LinearLayoutManager();

        clickProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDialog();
            }
        });
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        loading();

        firestore=FirebaseFirestore.getInstance();
        userID=auth.getCurrentUser();
        uid=userID.getUid();
        Query query=firestore.collection("Post Entries").whereNotEqualTo("Title",null).whereEqualTo("UserId",uid).orderBy("Title");


        FirestoreRecyclerOptions<PastJourney> options = new FirestoreRecyclerOptions.Builder<PastJourney>()
                .setQuery(query, PastJourney.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PastJourney, ViewHolder>(options) {
            @Override
            public void onBindViewHolder(ViewHolder holder, int position, PastJourney model) {
                holder.bind(model);

                holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getContext(), Journey_Entry_Details.class);
                        Toast.makeText(getContext(), "this is fig_ "+model.getEntryId(), Toast.LENGTH_SHORT).show();
                        i.putExtra("Entry_Id",model.getEntryId());
                        startActivity(i);
                    }
                });

            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.all_post, group, false);

                return new ViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        dialog.dismiss();


        return view;
    }

    private void loadDialog(){
        dialog= new Dialog(getContext());
        dialog.setContentView(R.layout.user_profile);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dalog_white));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        TextView user_name=dialog.findViewById(R.id.user_name);
        TextView user_number=dialog.findViewById(R.id.user_number);
        TextView user_email=dialog.findViewById(R.id.user_email);
        Button edit_btn=dialog.findViewById(R.id.edit_info);
        Button Logout=dialog.findViewById(R.id.logout_btn2);
        CardView image=dialog.findViewById(R.id.image_view1);
        DocumentReference docRef = firestore.collection("Users_Information").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String first_name=document.getString("First Name");
                        String last_name=document.getString("Last Name");
                        String user_number1=document.getString("Phone Number");
                        String user_email1=document.getString("email");

                        user_name.setText(first_name+" "+last_name);
                        user_number.setText(user_number1);
                        user_email.setText(user_email1);
                    } else {
                        System.out.println("Error 2");
                    }
                } else {
                    System.out.println("Error 1");
                }
            }
        });

        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "logout", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                //Menu menuNav = navigationView.getMenu();
                //MenuItem logoutItem = menuNav.findItem(R.menu.menu_bottom);


                if (getContext() != null) {
                    startActivity(new Intent(getContext(), Login.class));
                }
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView title,location,description;
        ImageView image;
        RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.title_view);
            location=itemView.findViewById(R.id.location_view);
            description=itemView.findViewById(R.id.description_view);
            image=itemView.findViewById(R.id.image_view);
            relativeLayout=itemView.findViewById(R.id.all_entries);
        }

        public void bind(PastJourney pastJourney){
           title.setText(pastJourney.getTitle());
            location.setText(pastJourney.getLocation());
            description.setText(pastJourney.getDescription());

            try{
                Picasso.get().load(pastJourney.getImage0()).fit().centerCrop().into(image);
            }catch (Exception e){
                System.out.println("this Error "+e);
            }


        }

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void loading(){
        dialog= new Dialog(getContext());
        dialog.setContentView(R.layout.custom_dialog_loading);
        dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.background_dialog));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

    }


}