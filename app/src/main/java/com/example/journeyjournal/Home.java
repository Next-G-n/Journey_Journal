package com.example.journeyjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {
    private Button logout;
    private String myEmail, myUid;
    private FirebaseAuth auth;
    private BottomNavigationView navigationView;
    private ActionBar actionBar;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton floatingActionButton;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarAndStatusBar2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        actionBar = getSupportActionBar();
        navigationView = findViewById(R.id.bottom_menu);
        logout = findViewById(R.id.logout_btn);
        auth = FirebaseAuth.getInstance();
        bottomAppBar = findViewById(R.id.bottom_appBar);
        floatingActionButton=(FloatingActionButton) findViewById(R.id.floating_btn);

        navigationView.setBackground(null);
        navigationView.getMenu().getItem(3).setEnabled(false);

        getSupportFragmentManager().beginTransaction().replace(R.id.content,
                new NewsFeedFragment()).commit();


        MaterialShapeDrawable bottomBarBackground = (MaterialShapeDrawable) bottomAppBar.getBackground();
        bottomBarBackground.setShapeAppearanceModel(
                bottomBarBackground.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopRightCorner(CornerFamily.ROUNDED, 30)
                        .setTopLeftCorner(CornerFamily.ROUNDED, 40)
                        .setBottomRightCorner(CornerFamily.ROUNDED, 40)
                        .setBottomLeftCorner(CornerFamily.ROUNDED, 40)
                        .build());

        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                setNavigationView(id);
                return true;
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // bottomAppBar.getNavigationIcon();
               // bottomAppBar.replaceMenu(R.menu.replace_menu);
                loadDialog();
            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Home.this, "logout", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                //Menu menuNav = navigationView.getMenu();
                //MenuItem logoutItem = menuNav.findItem(R.menu.menu_bottom);

                finish();
                if (Home.this != null) {
                   startActivity(new Intent(Home.this, Login.class));
                }
            }
        });
    }

    private void loadDialog(){
        dialog= new Dialog(Home.this);
        dialog.setContentView(R.layout.add_daliog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dalog_white));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        Button previous_j=dialog.findViewById(R.id.previous_journey);
        Button current_j=dialog.findViewById(R.id.current_journey);
        previous_j.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, UploadImage.class));
                dialog.dismiss();
            }
        });
        current_j.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


    public void updateUI(FirebaseUser account) {
        if (account != null) {
            Toast.makeText(this, "You Signed In successfully", Toast.LENGTH_LONG).show();

        } else {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "You not signed in", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Login.class));
        }

    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        String userid = currentUser.getUid();
        System.out.println("userId :" + userid);
        updateUI(currentUser);
    }

    private boolean setNavigationView(int id) {
        Fragment selected = null;

        int word = 0;
        switch (id) {
            case R.id.home:
                selected = new NewsFeedFragment();
                word = 1;
                break;
            case R.id.bookmarks:
                selected = new BookmarkFragment();
                word = 1;
                break;
            case R.id.map:
                selected = new LocationFragment();
                word = 1;
                break;
            case R.id.placeholder:
                word = 0;
                break;


        }

        if (word == 1) {
            //Begin Transaction
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.content, selected).commit();
            return true;
        }
        return false;
    }
}