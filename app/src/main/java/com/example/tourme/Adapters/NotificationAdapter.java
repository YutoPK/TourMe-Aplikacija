package com.example.tourme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tourme.Model.Notification;
import com.example.tourme.Model.Oglas;
import com.example.tourme.R;
import com.example.tourme.pregledJednogOglasa;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    public NotificationAdapter(){

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notification notification = mNotification.get(position);

        holder.title.setText(notification.getTitle());
        holder.text.setText(notification.getBody());

    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView logo;
        public TextView title;
        public TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.setIsRecyclable(false);

            logo = itemView.findViewById(R.id.logo);
            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);

        }
    }

}
