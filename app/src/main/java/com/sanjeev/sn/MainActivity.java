package com.sanjeev.sn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sanjeev.sn.fragments.ChatListFragment;
import com.sanjeev.sn.fragments.HomeFragment;
import com.sanjeev.sn.fragments.NotificationsFragment;
import com.sanjeev.sn.fragments.ProfileFragment;
import com.sanjeev.sn.fragments.SearchFragment;
import com.sanjeev.sn.notifications.Token;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    FirebaseAuth firebaseAuth;

    String mUID;
    FirebaseUser currentUser;

    FrameLayout frameLayout;
    TabLayout tabLayout;
    ImageView log_out;
    List<Fragment> fragmentsList;

    int tabPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        fragmentsList = new ArrayList<>();

        fragmentsList.add(new HomeFragment());
        fragmentsList.add(new SearchFragment());
        fragmentsList.add(new ChatListFragment());
        fragmentsList.add(new NotificationsFragment());
        fragmentsList.add(new NotificationsFragment());
        fragmentsList.add(new ProfileFragment());




        tabLayout.getTabAt(3).getIcon().setTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() !=3) {
                    tabPosition = tab.getPosition();
                    tab.getIcon().setTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    setFragment(tab.getPosition());
                }
                else
                {
                    tabLayout.getTabAt(tabPosition).select();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() !=3) {
                    tab.getIcon().setTintList(ColorStateList.valueOf(Color.parseColor("#C3BCBC")));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        tabLayout.getTabAt(0).getIcon().setTintList(ColorStateList.valueOf(Color.parseColor("#C3BCBC")));
        setFragment(0);





        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.create();
                alertDialog.setCancelable(false);
                alertDialog.setMessage("Do you really want to log out?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        firebaseAuth.signOut();
                        startActivity(new Intent(MainActivity.this, UserActivity.class));
                        finish();
                    }
                });


                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();

            }
        });




        firebaseAuth = FirebaseAuth.getInstance();



        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void init() {
        log_out = findViewById(R.id.log_out);
        tabLayout = findViewById(R.id.tab_layout);
        frameLayout = findViewById(R.id.frame_layout);


    }


    @Override
    protected void onStart() {
        super.onStart();

        checkUserLoginStatus();

    }

    @Override
    protected void onResume() {
        checkUserLoginStatus();
        super.onResume();
    }

    private void setFragment(int position) {



        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slideout_from_right);
        fragmentTransaction.replace(frameLayout.getId(), fragmentsList.get(position));
        fragmentTransaction.commit();

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.log_out) {


            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, UserActivity.class));
            finish();




        }
        return true;
    }


    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }




    void checkUserLoginStatus() {
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, UserActivity.class));
        } else {
            // do nothing stay here

            mUID = currentUser.getUid();
            SharedPreferences sp  = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

        fragment.onActivityResult(requestCode,resultCode,data);
    }
}