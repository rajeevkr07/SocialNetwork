package com.sanjeev.sn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder> {

    public  static  final  int MSG_TYPE_LEFT = 0;
    public  static  final  int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ChatModel> chatModelList;
    String imageUrl;


    FirebaseUser firebaseUser;

    public ChatAdapter(Context context, List<ChatModel> chatModelList, String imageUrl) {
        this.context = context;
        this.chatModelList = chatModelList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {


        String message = chatModelList.get(position).getMessage();
        String timeStamp = chatModelList.get(position).getTimeStamp();

        //convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String  dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();


        if (message.equals("This message was deleted")){
            holder.message_tv.setTextColor(Color.parseColor("#8C7C7C"));
            holder.message_tv.setText(message);
        }else{
            holder.message_tv.setTextColor(Color.parseColor("#000000"));
            holder.message_tv.setText(message);
        }

        holder.time_tv.setText(dateTime);

        Glide.with(context)
                .load(imageUrl)
                .into(holder.profile_iv);


        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure want to dlete this message");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });

        if(position == chatModelList.size()-1)
        {
            if (chatModelList.get(position).isSeen())
                holder.is_seen_tv.setText("Seen");
            else {
                holder.is_seen_tv.setText("Delivered");
            }
        }else
        {

            holder.is_seen_tv.setVisibility(View.GONE);

        }


    }

    private void deleteMessage(int position) {


        final String  myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp = chatModelList.get(position).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()
                     ) {

                    if (ds.child("sender").getValue().equals(myUID)){
//                        ds.getRef().removeValue();

                        //set the val to message was deleted

                        HashMap<String ,Object> hashMap = new HashMap<>();
                        hashMap.put("message","This message was deleted");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "message deleted", Toast.LENGTH_SHORT).show();
                    }

                    else{
                        Toast.makeText(context, "You can delete only your message", Toast.LENGTH_SHORT).show();

                    }





                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }


    @Override
    public int getItemViewType(int position) {
        //get curremnt signedinb user

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatModelList.get(position).getSender().equals(firebaseUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }

    }

    class  MyHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profile_iv;
        TextView message_tv,time_tv,is_seen_tv;
        LinearLayout messageLayout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profile_iv = itemView.findViewById(R.id.profile_iv);
            message_tv  =itemView.findViewById(R.id.message_tv);
            time_tv  =itemView.findViewById(R.id.time_tv);
            is_seen_tv  =itemView.findViewById(R.id.is_seen_tv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
