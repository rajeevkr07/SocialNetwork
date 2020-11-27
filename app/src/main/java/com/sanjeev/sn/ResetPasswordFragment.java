package com.sanjeev.sn;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;


public class ResetPasswordFragment extends Fragment {


    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    TextInputEditText reset_email;
    Button reset_password_btn;
    ProgressBar pb;
    FirebaseAuth mAuth;
    TextView tv4;

    FrameLayout parentFrameLayout;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_reset_password, container, false);
        parentFrameLayout = getActivity().findViewById(R.id.frame_layout);
        reset_password_btn = view.findViewById(R.id.reset_password_btn);
        reset_email = view.findViewById(R.id.reset_email);
        pb = view.findViewById(R.id.pb);
        tv4 = view.findViewById(R.id.tv4);
        mAuth = FirebaseAuth.getInstance();


        tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new SignInFragment());
            }
        });

        reset_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);


                if(Patterns.EMAIL_ADDRESS.matcher(reset_email.getText().toString()).matches())
                {
                    mAuth.sendPasswordResetEmail(reset_email.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful())
                                    {
                                        pb.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(), "Email Successfully sent!! Please check toyr email", Toast.LENGTH_SHORT).show();
                                        setFragment(new SignInFragment());
                                    }else
                                    {
                                        pb.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {


                                    pb.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });



                }
                else
                {
                    pb.setVisibility(View.GONE);
                    reset_email.setError("Email not valid...Please input correct email.");
                }

            }
        });






        return  view;

    }


    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.animate_shrink_enter,R.anim.animate_shrink_exit);
        fragmentTransaction.replace(parentFrameLayout.getId(),fragment);
        fragmentTransaction.commit();

    }
}