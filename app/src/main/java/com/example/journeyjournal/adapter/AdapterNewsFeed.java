package com.example.journeyjournal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class AdapterNewsFeed extends RecyclerView.Adapter<AdapterNewsFeed.MyHolder>{

    Context context;
    List<PastJourney> postList;
    String myUid;
    FirebaseFirestore firestore;


    public AdapterNewsFeed(Context context, List<PastJourney> postList) {
        this.context = context;
        this.postList = postList;

    }

    @NonNull
    @Override
    public AdapterNewsFeed.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterNewsFeed.MyHolder (LayoutInflater.from(context).inflate
                (R.layout.all_post,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        PastJourney pastJourney=postList.get(position);

        holder.title.setText(pastJourney.getTitle());
        holder.location.setText(pastJourney.getLocation());
        holder.description.setText(pastJourney.getDescription());
        try{
            Picasso.get().load(pastJourney.getTitle()).into(holder.image);

        }catch (Exception e){
            System.out.println("this Error "+e);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        TextView title,location,description;
        ImageView image;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.title_view);
            location=itemView.findViewById(R.id.location_view);
            description=itemView.findViewById(R.id.description_view);
            image=itemView.findViewById(R.id.image_view);
        }
    }
}
