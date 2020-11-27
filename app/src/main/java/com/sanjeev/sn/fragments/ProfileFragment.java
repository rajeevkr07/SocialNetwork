package com.sanjeev.sn.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sanjeev.sn.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }



    EditText editTextTextPersonName;


    Button change_password;
    FirebaseAuth mAuth;
    String currentUserId;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    ImageView profileImage,editName,setName;
    TextView profileName,profileEmail,profilePhone,profileUsername;
    Uri resultUri;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;

    ProgressDialog progressDialog,pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        profileEmail = view.findViewById(R.id.profile_email);
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profilePhone = view.findViewById(R.id.profile_phone);
        profileUsername = view.findViewById(R.id.profile_username);
        change_password = view.findViewById(R.id.change_password);
        editTextTextPersonName = view.findViewById(R.id.editTextTextPersonName);
        editName = view.findViewById(R.id.edit_name);
        setName = view.findViewById(R.id.setName);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        String name = mAuth.getCurrentUser().getDisplayName();
        System.out.println("data1"+name);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("USERS");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("profile_images");

        editTextTextPersonName.setVisibility(View.GONE);
        pd = new ProgressDialog(getContext());
        pd.setTitle("Getting user information");
        pd.setMessage("Please wait while we are fetching your info from our database ");
        pd.show();

        //getting data from database ,later have to reduce hitting direct firebase data
        getDataFromDatabase();



        //changing name from profile activity
        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editTextTextPersonName.setVisibility(View.VISIBLE);
                setName.setVisibility(View.VISIBLE);

                setName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Map<String ,Object > changeName = new HashMap<>();
                        changeName.put("name",editTextTextPersonName.getText().toString());
                        databaseReference.child(currentUserId)
                                .updateChildren(changeName)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if ((task.isSuccessful()))
                                        {
                                            editTextTextPersonName.setVisibility(View.GONE);
                                            setName.setVisibility(View.GONE);
                                        }
                                    }
                                });

                    }
                });
            }
        });

        //changing password
//        change_password.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                editTextTextPersonName.setVisibility(View.VISIBLE);
//                setName.setVisibility(View.VISIBLE);
//
//                setName.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        Map<String ,Object > changeName = new HashMap<>();
//                        changeName.put("password",editTextTextPersonName.getText().toString());
//                        databaseReference.child(currentUserId)
//                                .updateChildren(changeName)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if ((task.isSuccessful()))
//                                        {
//                                            editTextTextPersonName.setVisibility(View.GONE);
//                                            setName.setVisibility(View.GONE);
//                                        }
//                                    }
//                                });
//
//                    }
//                });
//            }
//        });


        //changing profile image


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Uploading...");
                progressDialog.setMessage("Please wait while we are uploading and updating your picture");


                progressDialog.show();

                Dexter.withContext(getActivity())
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
                            Toast.makeText(getActivity(), "Please allow permissions", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();

            }
        });

        return view;
    }





    private void getDataFromDatabase() {


            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                        Map<String, String> map = (Map<String, String>) postSnapshot.getValue();

                        if (map.get("uid").equals(currentUserId)) {
                            String name = map.get("name");
                            String email = map.get("email");

                            profileEmail.setText(email);
                            profileName.setText(name);
                            profileUsername.setText(map.get("username"));
                            profilePhone.setText("+91 " + map.get("phone"));

                            Glide.with(getContext())
                                    .load(map.get("image"))
                                    .into(profileImage);

                            pd.dismiss();


                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });








    }



    void selectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityMenuIconColor(getResources().getColor(R.color.colorAccent))
                .setActivityTitle("Profile photo")
                .setAspectRatio(1, 1)
                .start(getActivity());
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
                        .with(getContext())
                        .load(resultUri)
                        .centerCrop()
                        .placeholder(R.drawable.default_pic)
                        .into(profileImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setImageInDatabase() {

        StorageReference storageReference1 = storageReference.child(profileName.getText().toString()+"_"+currentUserId);
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
                            databaseReference.child(currentUserId)
                                    .updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            progressDialog.dismiss();

                                            Toast.makeText(getContext(), "Image Successfully Uploaded", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(getContext(), "Error Uploading Image "+e.getMessage() , Toast.LENGTH_SHORT).show();
                                }
                            });



                        }
                        else
                        {
                            Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
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
