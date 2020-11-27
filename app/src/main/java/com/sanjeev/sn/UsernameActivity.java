package com.sanjeev.sn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
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

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsernameActivity extends AppCompatActivity {

    CircleImageView reg_profile_pic;
    Button remove_btn, create_account_btn;
    TextInputEditText username;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    String currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    private Uri resultUri;
    public final static String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        init();

        reg_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withContext(UsernameActivity.this)
                        .withPermissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE

                        ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {
                            selectImage();
                        }
                        else
                        {
                            Toast.makeText(UsernameActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();


            }
        });

//        remove_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resultUri = null;
//                profile_image.setImageResource(R.drawable.default_pic);
//
//            }
//        });

        create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setError(null);
                if(username.getText().toString().isEmpty() || username.getText().toString().length()<3)
                {
                    username.setError("at least 3 characters required for username");
                    return;
                }
                else
                {
                    if(!username.getText().toString().matches(USERNAME_PATTERN))
                    {
                        username.setError("Only a-z / 0 -9 / and -,_  allowed");
                        return;
                    }
                    else
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        currentUser = mAuth.getCurrentUser().getUid();
                        String  user_name = username.getText().toString();
                       Map<String ,Object > username_data = new HashMap<>();
                       username_data.put("username",user_name);
                        databaseReference.child(currentUser)
                                .updateChildren(username_data)

                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                        if(task.isSuccessful())
                                        {
                                            startActivity(new Intent(UsernameActivity.this,MainActivity.class));
                                        }
                                        else
                                        {
                                            Toast.makeText(UsernameActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                    }


                }
            }
        });
    }

    private void init() {
        progressBar = findViewById(R.id.progressBar);
        username = findViewById(R.id.username);
        reg_profile_pic = findViewById(R.id.reg_profile_pic);
        create_account_btn = findViewById(R.id.create_account_btn);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("USERS");

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("profile_images");

        currentUser = mAuth.getCurrentUser().getUid();

    }




    void selectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityMenuIconColor(getResources().getColor(R.color.colorAccent))
                .setActivityTitle("Profile photo")
                .setAspectRatio(1, 1)
                .start(this);
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();

                System.out.println("data1"+resultUri);


                if(resultUri!=null)
                {
                    setImageInDatabase();
                }
                Glide
                        .with(this)
                        .load(resultUri)
                        .centerCrop()
                        .placeholder(R.drawable.default_pic)
                        .into(reg_profile_pic);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(UsernameActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setImageInDatabase() {

        StorageReference storageReference1 = storageReference.child(username.getText().toString()+"_"+currentUser);
        storageReference1.putFile(resultUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //check if uimage is uploaded or not
                        if(uriTask.isSuccessful())
                        {
                            HashMap<String,Object> results = new HashMap<>();
                            results.put("image",downloadUri.toString());
                            databaseReference.child(currentUser)
                                    .updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(UsernameActivity.this, "Image Upoloaded", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(UsernameActivity.this, "ErrorUpoloaded", Toast.LENGTH_SHORT).show();
                                }
                            });



                        }
                        else
                        {
                            Toast.makeText(UsernameActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



    }



}