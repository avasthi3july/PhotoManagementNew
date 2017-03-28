package com.photo.management.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.photo.management.R;
import com.photo.management.dao.Photo;
import com.photo.management.dao.User;

import java.util.ArrayList;

/**
 * Created by kavasthi on 12/29/2016.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<User> userList;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row, parent, false);
        return new ViewHolder(view);
    }

    public UserAdapter(Context mContext, ArrayList<User> list) {
        this.mContext = mContext;
        this.userList = list;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User mUser = userList.get(position);
        holder.email.setText(mUser.getEmail());

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView email;

        public ViewHolder(View itemView) {
            super(itemView);
            email = (TextView) itemView.findViewById(R.id.email);
        }
    }
}
