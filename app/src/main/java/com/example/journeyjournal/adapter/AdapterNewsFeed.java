package com.example.journeyjournal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.journeyjournal.R;
import com.example.journeyjournal.models.PastJourney;
import com.example.journeyjournal.models.SliderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class AdapterNewsFeed extends RecyclerView.Adapter<AdapterNewsFeed.MyHolder>{

    Context context;
    List<PastJourney> postList;
    String myUid;
    FirebaseFirestore firestore;

    public AdapterNewsFeed(Context context, List<PastJourney> postList) {
        this.context = context;
        this.postList = postList;
        firestore=FirebaseFirestore.getInstance();
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public AdapterNewsFeed.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterNewsFeed.MyHolder (LayoutInflater.from(context).inflate
                (R.layout.all_post,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        CollectionReference documentReference=firestore.collection(myUid);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                    }
                } else {

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        public MyHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
