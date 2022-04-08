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
import com.example.tourme.Model.Oglas;
import com.example.tourme.Model.User;
import com.example.tourme.R;
import com.example.tourme.pregledJednogOglasa;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OglasAdapter extends RecyclerView.Adapter<OglasAdapter.ViewHolder> {

    private Context mContext;
    private List<Oglas> mOglas;

    public OglasAdapter(Context mContext, List<Oglas> mOglas){
        this.mContext = mContext;
        this.mOglas = mOglas;
    }

    public OglasAdapter() {
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.oglas_item, parent, false);
        return new OglasAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Oglas oglas = mOglas.get(position);
        FirebaseDatabase.getInstance().getReference("users").child(oglas.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);
                //holder.username.setText(oglas.getUsername());
                holder.cena.setText(oglas.getCenaOglasa()+"");
                if(user.getIme().equals("") || user.getPrezime().equals("")){
                    holder.username.setVisibility(View.VISIBLE);
                    holder.username.setText(user.getUsername());
                    holder.ime_prezime.setVisibility(View.INVISIBLE);
                }
                else{
                    holder.ime_prezime.setText(user.getIme() + " " + user.getPrezime());
                    holder.username.setVisibility(View.GONE);
                }
                holder.grad.setText(oglas.getGrad());

                if(user.getImageurl().equals("default")){
                    holder.oglas_image.setImageResource(R.drawable.default_image);
                }else{
                    if(isValidContextForGlide(mContext))
                        Glide.with(mContext).load(user.getImageurl()).into(holder.oglas_image);
                }

                holder.ratingStars.setRating((float) oglas.getOcena());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isValidContextForGlide(mContext)){
                            Intent intent = new Intent(mContext, pregledJednogOglasa.class);
                            intent.putExtra("IDOglasa",oglas.getIdOglasa());
                            intent.putExtra("NazivGrada", oglas.getGrad());
                            intent.putExtra("IDUser", oglas.getUserId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mOglas.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{

        public TextView username;
        public ImageView oglas_image;
        public TextView cena;

        public TextView ime_prezime;
        public TextView grad;
        public RatingBar ratingStars;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            oglas_image = itemView.findViewById(R.id.oglas_image);
            cena = itemView.findViewById(R.id.cena);

            this.setIsRecyclable(false);

            ime_prezime = itemView.findViewById(R.id.ime_prezime);
            grad = itemView.findViewById(R.id.grad);
            ratingStars = itemView.findViewById(R.id.ratingStars);

        }
    }
}
