package com.sanjeev.sn;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    Context mContext;
    List<UsersModel> usersModelList;

    public UserAdapter(Context mContext, List<UsersModel> usersModelList) {
        this.mContext = mContext;
        this.usersModelList = usersModelList;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ViewHolder holder, int position) {

        holder.name.setText(usersModelList.get(position).getName());
        holder.email.setText(usersModelList.get(position).getEmail());
        final String uid = usersModelList.get(position).getUid();

        Glide.with(mContext)
                .load(usersModelList.get(position).getImage())
                .into(holder.profileImage);


        holder.showusers_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(holder.itemView.getContext(),ChatActivity.class);
                in.putExtra("uid",uid);
                holder.itemView.getContext().startActivity(in);
            }
        });


    }

    @Override
    public int getItemCount() {
        return usersModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView name, email;
        CardView showusers_cardview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showusers_cardview = itemView.findViewById(R.id.showusers_cardview);
            profileImage = itemView.findViewById(R.id.showusers_pic);
            email = itemView.findViewById(R.id.showusers_email);
            name = itemView.findViewById(R.id.showusers_name);
        }
    }
}
