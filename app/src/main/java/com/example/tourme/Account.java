package com.example.tourme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tourme.Adapters.OglasAdapter;
import com.example.tourme.Model.Oglas;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Account extends AppCompatActivity {

    //View
    ImageView imageView;
    TextView username, ime, prezime, opis, godine, average, ukupanBrojOcena;
    RatingBar averageBar;
    RecyclerView recyclerView;
    View viewNoInternet, viewThis;
    ProgressBar progressBar;
    Button tryAgainButton;

    //Firebase
    DatabaseReference reference;
    FirebaseUser firebaseUser;

    //Variables
    Handler h = new Handler();
    int reasonForBadConnection = 1, numberOfOglases;
    String userid1;
    OglasAdapter oglasAdapter;

    void hideProgressShowButton(){
        progressBar.setVisibility(View.GONE);
        tryAgainButton.setVisibility(View.VISIBLE);
    }
    void hideButtonShowProgress(){
        progressBar.setVisibility(View.VISIBLE);
        tryAgainButton.setVisibility(View.GONE);
    }

    private void HideEverythingRecursion(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i = 0 ;i < viewgroup.getChildCount(); i++) {
            View v1 = viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup){
                if(v1 != viewNoInternet)
                    HideEverythingRecursion(v1);
            }else
                v1.setVisibility(View.GONE);
        }
    }

    void HideEverything(){
        HideEverythingRecursion(viewThis);
        viewNoInternet.setVisibility(View.VISIBLE);
    }

    void HideWithReason(int reason){
        HideEverything();
        reasonForBadConnection = reason;
    }

    private void ShowEverythingRecursion(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i = 0 ;i < viewgroup.getChildCount(); i++) {
            View v1 = viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup){
                if(v1 != viewNoInternet)
                    ShowEverythingRecursion(v1);
            }else
                v1.setVisibility(View.VISIBLE);
        }
    }

    void ShowEverything(){
        ShowEverythingRecursion(viewThis);
        viewNoInternet.setVisibility(View.GONE);
    }

    Boolean IsConnectedToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    void recursion1ForMyOglases(int index, List<String> idsForMyOglas, List<Oglas> mOglas){
        if(index == numberOfOglases){
            oglasAdapter = new OglasAdapter(Account.this, mOglas);
            recyclerView.setAdapter(oglasAdapter);
        }else{
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();
            ref1.child("oglasi").child(idsForMyOglas.get(index)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    Oglas oglas = task.getResult().getValue(Oglas.class);
                    mOglas.add(oglas);
                    recursion1ForMyOglases(index + 1, idsForMyOglas, mOglas);
                }
            });
        }
    }

    public void updateUser(User user){
        username.setText(user.getUsername());
        ime.setText(user.getIme());
        prezime.setText(user.getPrezime());
        opis.setText(user.getOpis());
        average.setText(String.valueOf(user.getUkupnaProsecnaOcena()));
        averageBar.setRating((float) user.getUkupnaProsecnaOcena());
        ukupanBrojOcena.setText(String.valueOf(user.getBrojOcena()));
        String d1 = user.getDan();
        String m1 = user.getMesec();
        String g1 = user.getGodina();
        String d2 = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
        String m2 = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());
        String g2 = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        godine.setText(String.valueOf(StaticVars.numberOfYears(d1, StaticVars.convertMonth(m1), g1, d2, m2, g2)));
        if(ime.getText().toString().trim().equals("") && prezime.getText().toString().trim().equals(""))
            if(d1.equals("01") && m1.equals("Januar") && g1.equals("1900"))
                godine.setText("");
        if(user.getImageurl().equals("default")){
            imageView.setImageResource(R.drawable.default_image);
        }
        else{
            Glide.with(getApplicationContext()).load(user.getImageurl()).into(imageView);
        }
    }

    public void updateOglas(){
        FirebaseDatabase.getInstance().getReference().child("users").child(userid1).child("oglas").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                List<Oglas> mOglas = new ArrayList<>();
                List<String> idsForMyOglas = new ArrayList<>();
                for(DataSnapshot dataSnapshot : Objects.requireNonNull(task.getResult()).getChildren()){
                    String newIdOglasa = dataSnapshot.getValue(String.class);
                    idsForMyOglas.add(newIdOglasa);
                }

                numberOfOglases = idsForMyOglas.size();
                recursion1ForMyOglases(0, idsForMyOglas, mOglas);
            }
        });
    }

    public void setupFirebase(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userid1 = getIntent().getStringExtra("userid");

        reference = FirebaseDatabase.getInstance().getReference("users").child(userid1);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                updateUser(user);
                updateOglas();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("oglasi");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateOglas();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet()){
            setupFirebase();
        }else{
            HideWithReason(1);
            return false;
        }
        return true;
    }

    public void setupView(){
        StaticVars.listOfFragments.add(5);

        imageView = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        ime = findViewById(R.id.ime);
        prezime = findViewById(R.id.prezime);
        opis = findViewById(R.id.kratakOpis);
        godine = findViewById(R.id.godine);
        average = findViewById(R.id.ocena);
        averageBar = findViewById(R.id.averageRatubgBar);
        ukupanBrojOcena = findViewById(R.id.ukupanBrojOcena);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(Account.this));

        viewThis = findViewById(R.id.accountActivity);
        viewNoInternet = (View) findViewById(R.id.nemaInternet);
        progressBar = viewNoInternet.findViewById(R.id.progressBar);


        tryAgainButton = viewNoInternet.findViewById(R.id.TryAgainButton);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideButtonShowProgress();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressShowButton();
                        if(reasonForBadConnection == 1) {
                            if (tryToStart()) ShowEverything();
                        }else ShowEverything();
                    }
                }, 1000);
            }
        });

        tryToStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        setupView();
    }

    private void status(String status){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("status", status);

            reference.updateChildren(hashMap);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

}