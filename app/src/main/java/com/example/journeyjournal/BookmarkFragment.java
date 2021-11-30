package com.example.journeyjournal;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookmarkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookmarkFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BookmarkFragment newInstance(String param1, String param2) {
        BookmarkFragment fragment = new BookmarkFragment();
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
        final OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            boolean doubleBackToExitPressedOnce = false;

            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
               /* if (doubleBackToExitPressedOnce) {
                    doubleBackToExitPressedOnce = true;
                    requireActivity().onBackPressed()
                    backpress = (backpress + 1);
                    System.exit(0);
                }else{

                }*/
                if (doubleBackToExitPressedOnce) {
                    // ActivityCompat.finishAffinity( getActivity() );
                    Log.e("Click", "double back");
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(getContext(), "Double click back to exit", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                        //System.exit(0);
                    }
                }, 2000);


            }
        };
        View view= inflater.inflate(R.layout.fragment_bookmark, container, false);
        return view;

    };
}