package com.example.tourme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tourme.Account;
import com.example.tourme.Model.Comment;
import com.example.tourme.Model.User;
import com.example.tourme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private Context mContext;
    private List<Comment> mComment;

    public CommentAdapter(Context mContext, List<Comment> mComment) {
        this.mContext = mContext;
        this.mComment = mComment;
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null)
            return false;
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing())
                return false;
        }
        return true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Comment comment = mComment.get(position);
        FirebaseDatabase.getInstance().getReference("users").child(comment.getUserid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(user.getIme().equals("") || user.getPrezime().equals("")){
                    holder.username.setVisibility(View.VISIBLE);
                    holder.username.setText(user.getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isValidContextForGlide(mContext)){
                                Intent intent = new Intent(mContext, Account.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userid",user.getId());
                                mContext.startActivity(intent);
                            }
                        }
                    });
                    holder.ime_prezime.setVisibility(View.INVISIBLE);
                }
                else{
                    holder.ime_prezime.setText(user.getIme() + " " + user.getPrezime());
                    holder.username.setVisibility(View.GONE);
                    holder.ime_prezime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isValidContextForGlide(mContext)){
                                Intent intent = new Intent(mContext, Account.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userid",user.getId());
                                mContext.startActivity(intent);
                            }
                        }
                    });
                }
                if (user.getImageurl().equals("default")){
                    holder.comment_image.setImageResource(R.drawable.default_image);
                } else {
                    if(isValidContextForGlide(mContext))
                        Glide.with(mContext).load(user.getImageurl()).into(holder.comment_image);

                }
                holder.comment_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isValidContextForGlide(mContext)) {
                            Intent intent = new Intent(mContext, Account.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("userid", user.getId());
                            mContext.startActivity(intent);
                        }
                    }
                });
                holder.deskripcija.setText(comment.getRatingDescription());
                holder.ratingStars.setRating((float) comment.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView comment_image;
        public TextView deskripcija;
        public RatingBar ratingStars;
        public TextView ime_prezime;

        public ViewHolder(View itemView){
            super(itemView);

            this.setIsRecyclable(false);

            username = itemView.findViewById(R.id.username);
            comment_image = itemView.findViewById(R.id.comment_image);
            deskripcija = itemView.findViewById(R.id.deskripcija);
            ratingStars = itemView.findViewById(R.id.ratingStars);
            ime_prezime = itemView.findViewById(R.id.ime_prezime);
        }

    }

}
