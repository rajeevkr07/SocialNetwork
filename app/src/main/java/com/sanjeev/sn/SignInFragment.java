package com.sanjeev.sn;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class SignInFragment extends Fragment {




    public SignInFragment() {
        // Required empty public constructor
    }

    private static final int RC_SIGN_IN = 100;

    GoogleSignInClient mGoogleSignInClient;

    TextInputEditText login_password,login_email;

    FrameLayout parentFrameLayout;
    FirebaseAuth mAuth;

    ProgressDialog progressDialog;
    TextView forgot_pasword;


    TextView reg_btn;
    Button login_btn;

    SignInButton google_login_btn;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    boolean ALREADY_EXISTS = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_in, container, false);
        login_btn = view.findViewById(R.id.login_btn);
        reg_btn = view.findViewById(R.id.reg_btn);
        login_email = view.findViewById(R.id.login_email);
        login_password = view.findViewById(R.id.login_password);
        parentFrameLayout = getActivity().findViewById(R.id.frame_layout);
        forgot_pasword=  view.findViewById(R.id.forgot_pasword);
        google_login_btn = view.findViewById(R.id.google_login_btn);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("USERS");









        progressDialog = new ProgressDialog(getActivity());

        //*************validation for google sign in api
// Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(),gso);

        //*****************validation for google sign in api

        mAuth = FirebaseAuth.getInstance();


        //******************validation for google sign in api
        google_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });
        //*************validation for google sign in api



        forgot_pasword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setforgotFragment(new ResetPasswordFragment());
            }
        });


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Patterns.EMAIL_ADDRESS.matcher(login_email.getText().toString()).matches())
                {
                    if(login_password.getText().toString().length() >5)
                    {
                        progressDialog.show();
                        checkValidation(login_email.getText().toString(),login_password.getText().toString());
                    }
                    else
                    {
                        login_password.setError("Password length must be 6");
                    }

                }else
                {
                    login_email.setError("Email not valid");
                }



            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setFragment(new SignUpFragment());

            }
        });






        return view;
    }



    //validation for google sign in api

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            final String uid = user.getUid();

                            String name = task.getResult().getAdditionalUserInfo().getProfile().get("name").toString();
                            String email = task.getResult().getAdditionalUserInfo().getProfile().get("email").toString();
                            String image  =task.getResult().getAdditionalUserInfo().getProfile().get("picture").toString();
                            String phone = "xxxxxxxxxx";

                            final Map<String ,Object> userdata = new HashMap<>();
                            userdata.put("name",name);
                            userdata.put("email",email);
                            userdata.put("image",image);
                            userdata.put("onlineStatus","online");
                            userdata.put("typingTo","noOne");
                            userdata.put("phone",phone);
                            userdata.put("uid",uid);



                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {


                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                                        Map<String, String> map = (Map<String, String>) postSnapshot.getValue();

                                        if (map.get("uid").equals(uid)) {

                                            ALREADY_EXISTS = true;

                                        }
                                    }

                                    if (ALREADY_EXISTS) {
                                        System.out.println("exists1");
                                        startActivity(new Intent(getActivity(), MainActivity.class));
                                        getActivity().finish();
                                    }
                                    else
                                    {
                                        databaseReference.child(uid).setValue(userdata)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(new Intent(getActivity(), UsernameActivity.class));
                                                        getActivity().finish();
                                                    }
                                                });
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

























                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(),"Login failes", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //validation for google sign in api






//    private void checkValidation(final String email, final String password)
//    {
//
//        progressDialog.setTitle("Validating");
//        progressDialog.setMessage("Plaese wait while we are validating your credentials...");
//        progressDialog.show();
//
//
//
//
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//
//                if(snapshot.child(login_email.getText().toString()).exists())
//                {
//                    System.out.println("data1");
//                }
//
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
////


    void checkValidation(String email,String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();


                        } else {
                            progressDialog.dismiss();
                            reg_btn.setEnabled(true);
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        reg_btn.setEnabled(true);
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_right,R.anim.slideout_from_left);
        fragmentTransaction.replace(parentFrameLayout.getId(),fragment);

        fragmentTransaction.commit();

    }

    private void setforgotFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.animate_shrink_enter,R.anim.animate_shrink_exit);
        fragmentTransaction.replace(parentFrameLayout.getId(),fragment);
        fragmentTransaction.commit();

    }
}