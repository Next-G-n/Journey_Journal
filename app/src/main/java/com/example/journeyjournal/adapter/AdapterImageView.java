package com.example.journeyjournal.adapter;

import static com.google.firebase.firestore.FieldValue.delete;
import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.journeyjournal.R;
import com.example.journeyjournal.models.SliderItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterImageView extends RecyclerView.Adapter<AdapterImageView.MyHolder> {
    private Context context;
    private List<SliderItem> sliderItems;
    private ViewPager2 viewPager2;
    private OnItemClickListLister mListLister;

    public interface OnItemClickListLister{
        void onItemClick(int position);
    }

    public  void  setOnItemLister(OnItemClickListLister listLister){
        mListLister=listLister;

    }

    private void removeItem(int position){
        sliderItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, sliderItems.size());
    }


    public AdapterImageView(Context context, List<SliderItem> sliderItems) {
        this.context = context;
        this.sliderItems = sliderItems;
    }

    @NonNull
    @Override
    public AdapterImageView.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new  AdapterImageView.MyHolder (LayoutInflater.from(context).inflate
                (R.layout.slider_item_container,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterImageView.MyHolder holder, int position) {
        holder.setImage(sliderItems.get(position));
        int i=position;
        System.out.println("yes me 2"+i);

        holder.delete_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    removeItem(i);
            }
        });

        try{
            Picasso.get().load(sliderItems.get(position).getImage()).into(holder.imageView);

        }catch (Exception e){
            System.out.println("this Error "+e);
        }



    }

    private void deleteImage(String deleteImageName) {


    }

    @Override
    public int getItemCount() {
        return  sliderItems.size();
    }



    public class MyHolder extends RecyclerView.ViewHolder{
        private RoundedImageView imageView;
        private String imageString;
        private ImageButton delete_info;


        public MyHolder(View inflate) {
            super(inflate);
            imageView=itemView.findViewById(R.id.imageSlide);
            delete_info=itemView.findViewById(R.id.deleted);
            delete_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListLister!= null){
                        int position=getAdapterPosition();
                        mListLister.onItemClick(position);
                        sliderItems.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            });

        }


        void  setImage(SliderItem sliderItem){
           // imageView.setImageResource(sliderItem.getImage());
         }
    }

    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            sliderItems.addAll(sliderItems);
            notifyDataSetChanged();
        }
    };
}
