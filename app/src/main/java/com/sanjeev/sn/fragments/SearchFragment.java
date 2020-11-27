package com.sanjeev.sn.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanjeev.sn.R;
import com.sanjeev.sn.UserAdapter;
import com.sanjeev.sn.UsersModel;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    public SearchFragment() {
        // Required empty public constructor
    }

    ProgressDialog pd;

    RecyclerView users_recyclerView;
    UserAdapter userAdapter;
    List<UsersModel> usersModelsList;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String currentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_search, container, false);
        pd = new ProgressDialog(getContext());
        pd.setTitle("Getting Users List");
        pd.setMessage("Please wait while we are fetching users list");
        pd.setCancelable(false);
        pd.show();
        users_recyclerView = view.findViewById(R.id.users_recyclerView);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUser = firebaseUser.getUid();

        users_recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        users_recyclerView.setLayoutManager(layoutManager);

        usersModelsList = new ArrayList<>();


        getAllUsers();









        return view;
    }

    private void getAllUsers() {


        databaseReference = FirebaseDatabase.getInstance().getReference("USERS");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersModelsList.clear();


                for (DataSnapshot ds:snapshot.getChildren())
                {
                    UsersModel usersModel = ds.getValue(UsersModel.class);

                    if(!usersModel.getUid().equals(currentUser))
                    {
                        usersModelsList.add(usersModel);

                    }





                    userAdapter = new UserAdapter(getActivity(),usersModelsList);
                    users_recyclerView.setAdapter(userAdapter);

                }

                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}