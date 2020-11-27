package com.sanjeev.sn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    TextView username, userstatus;
    CircleImageView profile_pic;

    EditText messageEt;
    ImageButton sendBtn;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    String hisImage;
    TextView chat_user_status;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ChatModel> chatModelList;
    ChatAdapter chatAdapter;
    RecyclerView chat_recyclerView;


    String currentUserID, hisUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        chat_recyclerView.setHasFixedSize(true);
        chat_recyclerView.setLayoutManager(linearLayoutManager);


        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        currentUserID = mAuth.getCurrentUser().getUid();
        hisUserID = getIntent().getStringExtra("uid");

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("USERS");

        // we will fire a query to search the details of the uID
        Query userQuery = usersRef.orderByChild("uid").equalTo(hisUserID);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();


                    //check typing status
                    if(typingStatus.equals(currentUserID)){

                        chat_user_status.setText("typing...");
                        chat_user_status.setTextColor(Color.parseColor("#000000"));

                    }
                    else{

                        chat_user_status.setTextColor(Color.parseColor("#ffffff"));

                        String onlineStatus =  "" + ds.child("onlineStatus").getValue();
                        if(onlineStatus.equals("online"))
                            chat_user_status.setText(onlineStatus);
                        else {
                            //convert timestamp to peoper time
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String  dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            chat_user_status.setText("Last seen at:"+dateTime);


                        }
                    }

                    username.setText(name);

                    Glide.with(ChatActivity.this).load(hisImage).placeholder(R.drawable.default_pic)
                            .into(profile_pic);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageEt.getText().toString();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Chat cant send empty message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }

            }
        });


        //check edit text change lsitemner
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().trim().length()==0){
                checkTypingStatus("noOne");
            }else {
                checkTypingStatus(hisUserID);  //uid of receiver
            }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        readMessage();
        seenMessage();


    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren())
                {
                    ChatModel chat = ds.getValue(ChatModel.class);

                    if (chat.getReceiver().equals(currentUserID)&& chat.getSender().equals(hisUserID))
                    {
                        HashMap<String ,Object> hasSeen = new HashMap<>();
                        hasSeen.put("isSeen",true);

                        ds.getRef().updateChildren(hasSeen);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        //gettimestamp
        String timeStamp = String.valueOf(System.currentTimeMillis());

        //set offline with last seen stime stamp
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");

        userRefForSeen.removeEventListener(seenListener);

    }


    @Override
    protected void onResume() {

        checkOnlineStatus("online");
        super.onResume();
    }

    private void readMessage() {
        chatModelList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatModelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatModel chatModel = ds.getValue(ChatModel.class);
                    if (chatModel.getReceiver().equals(currentUserID) && chatModel.getSender().equals(hisUserID) || chatModel.getReceiver().equals(hisUserID) && chatModel.getSender().equals(currentUserID)) {

                        chatModelList.add(chatModel);
                    }
                    chatAdapter = new ChatAdapter(ChatActivity.this,chatModelList,hisImage);
                    chatAdapter.notifyDataSetChanged();

                    chat_recyclerView.setAdapter(chatAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> chats = new HashMap<>();
        chats.put("receiver", hisUserID);
        chats.put("sender", currentUserID);
        chats.put("message", message);
        chats.put("timeStamp", timeStamp);
        chats.put("isSeen", false);
        chatRef.child("Chats").push().setValue(chats);
//        chatRef.child("Chats").child(currentUserID).push().setValue(chats);
//        chatRef.child("Chats").child(hisUserID).push().setValue(chats);


        messageEt.setText("");


    }

    void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("USERS").child(currentUserID);
        HashMap<String ,Object> map = new HashMap<>();
        map.put("onlineStatus",status);
        //update valure of curen user
        dbRef.updateChildren(map);
    }

    void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("USERS").child(currentUserID);
        HashMap<String ,Object> map = new HashMap<>();
        map.put("typingTo",typing);
        //update valure of curen user
        dbRef.updateChildren(map);
    }

    void init() {
        username = findViewById(R.id.chat_username);
        userstatus = findViewById(R.id.chat_user_status);
        profile_pic = findViewById(R.id.chat_user_profile_image);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        mAuth = FirebaseAuth.getInstance();
        chat_recyclerView = findViewById(R.id.chat_recyclerView);
        chat_user_status= findViewById(R.id.chat_user_status);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onStart() {
        checkUserLoginStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.chat_log_out) {
            mAuth.signOut();
            checkUserLoginStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    void checkUserLoginStatus() {
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(ChatActivity.this, UserActivity.class));
        } else {
            // do nothing stay here

        }


    }
}