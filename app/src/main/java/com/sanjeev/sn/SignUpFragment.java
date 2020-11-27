package com.sanjeev.sn;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class SignUpFragment extends Fragment {


    public SignUpFragment() {
        // Required empty public constructor
    }

    FrameLayout parentFrameLayout;
    TextView back_to_login;
    Button register_account_btn;
    ImageView reg_profile_pic;

    TextInputEditText reg_fullname, reg_email, reg_password, reg_phone;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    Uri resultUri;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String currentUser;
    FirebaseUser user;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        back_to_login = view.findViewById(R.id.back_to_login);
        register_account_btn = view.findViewById(R.id.register_account_btn);
        parentFrameLayout = getActivity().findViewById(R.id.frame_layout);
        reg_email = view.findViewById(R.id.reg_email);

        reg_fullname = view.findViewById(R.id.reg_fullname);
        reg_password = view.findViewById(R.id.reg_password);
        reg_phone = view.findViewById(R.id.reg_phone);
        progressBar = view.findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("USERS");
        user = mAuth.getCurrentUser();






        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new SignInFragment());
            }
        });


        register_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkEmailandPassword();

            }
        });


        return view;
    }






    private void checkEmailandPassword() {

        final String email = reg_email.getText().toString();
        final String password = reg_password.getText().toString();
        final String full_name = reg_fullname.getText().toString();
        final String phone = reg_phone.getText().toString();



        if(full_name.equals(null))
        {
            reg_fullname.setError("Cant be left empty");
            reg_fullname.setFocusable(true);
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            reg_email.setError("Email not valid");
            reg_email.setFocusable(true);
        } else if(password.length() < 6)
        {
            reg_password.setError("Password length cant be less than 6");
            reg_password.setFocusable(true);
        }else if(phone.equals(null))
        {
            reg_phone.setError("Cant left blank");
        }
        else
        {

            progressBar.setVisibility(View.VISIBLE);
            register_account_btn.setEnabled(false);
            register_account_btn.setBackgroundColor(Color.parseColor("#C3BEBE"));


            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {


                            if (task.isSuccessful()) {
                                Map<Object, String> userdata = new HashMap<>();

                                String uid = mAuth.getCurrentUser().getUid();

                                userdata.put("name", full_name);
                                userdata.put("email", email);
                                userdata.put("phone", phone);
                                userdata.put("onlineStatus","online");
                                userdata.put("typingTo","noOne");
                                userdata.put("password", password);
                                userdata.put("uid", uid);

                                databaseReference.child(uid).setValue(userdata)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {


                                                if (task.isSuccessful()) {
                                                    startActivity(new Intent(getActivity(), UsernameActivity.class));
                                                    getActivity().finish();

                                                } else {

                                                    register_account_btn.setBackgroundColor(Color.parseColor("#7000ff00"));
                                                    register_account_btn.setEnabled(true);
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getContext(), "Auth Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });


                            }


                        }




                    });

        }
    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slideout_from_right);
        fragmentTransaction.replace(parentFrameLayout.getId(), fragment);
        fragmentTransaction.commit();

    }





}