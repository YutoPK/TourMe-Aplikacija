package com.example.tourme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.tourme.Adapters.OglasAdapter;
import com.example.tourme.Model.Oglas;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class sacuvaniOglasi extends AppCompatActivity {

    //View
    RecyclerView recyclerView;
    View viewNoInternet, viewThis, viewNotLoggedIn, viewNotSaved;
    ProgressBar progressBar;
    Button tryAgainButton, goToLoginButton;
    FirebaseAuth fAuth;

    //Firebase

    //Variables
    Handler h = new Handler();
    int reasonForBadConnection = 1, numberOfOglases;
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
                if(v1 != viewNoInternet && v1 != viewNotLoggedIn && v1 != viewNotSaved)
                    HideEverythingRecursion(v1);
            }else
                v1.setVisibility(View.GONE);
        }
    }

    void HideEverything(int option){
        HideEverythingRecursion(viewThis);
        if(option == 1)
            viewNoInternet.setVisibility(View.VISIBLE);
        else
            viewNotLoggedIn.setVisibility(View.VISIBLE);
    }

    void HideWithReason(int reason){
        HideEverything(1);
        reasonForBadConnection = reason;
    }

    private void ShowEverythingRecursion(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i = 0 ;i < viewgroup.getChildCount(); i++) {
            View v1 = viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup){
                if(v1 != viewNoInternet && v1 != viewNotLoggedIn && v1 != viewNotSaved){
                    ShowEverythingRecursion(v1);
                }
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

    public void manageNotSaved(int len){
        if(len == 0)
            viewNotSaved.setVisibility(View.VISIBLE);
        else
            viewNotSaved.setVisibility(View.GONE);
    }

    void recursion1ForMyOglases(int index, List<String> idsForMyOglas, List<Oglas> mOglas){
        if(index == numberOfOglases){
            oglasAdapter = new OglasAdapter(sacuvaniOglasi.this, mOglas);
            recyclerView.setAdapter(oglasAdapter);
        }else{
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();
            ref1.child("oglasi").child(idsForMyOglas.get(index)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    Oglas oglas = task.getResult().getValue(Oglas.class);
                    if(oglas != null)
                        mOglas.add(oglas);
                    recursion1ForMyOglases(index + 1, idsForMyOglas, mOglas);
                }
            });
        }
    }

    public void setupFirebase(){
        fAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().getReference().child("users").child(fAuth.getUid()).child("sacuvaniOglasi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> idsForMyOglas = new ArrayList<>();
                List<Oglas> mOglas = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String oglas = dataSnapshot.getValue(String.class);
                    idsForMyOglas.add(oglas);
                }

                numberOfOglases = idsForMyOglas.size();
                manageNotSaved(numberOfOglases);
                recursion1ForMyOglases(0, idsForMyOglas, mOglas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("oglasi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseDatabase.getInstance().getReference().child("users").child(fAuth.getUid()).child("sacuvaniOglasi").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        List<String> idsForMyOglas = new ArrayList<>();
                        List<Oglas> mOglas = new ArrayList<>();
                        for(DataSnapshot dataSnapshot : task.getResult().getChildren()){
                            String oglas = dataSnapshot.getValue(String.class);
                            idsForMyOglas.add(oglas);
                        }

                        numberOfOglases = idsForMyOglas.size();
                        manageNotSaved(numberOfOglases);
                        recursion1ForMyOglases(0, idsForMyOglas, mOglas);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet()) {
            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                setupFirebase();
            }else{
                HideEverything(2);
                return false;
            }
        }else{
            HideWithReason(1);
            return false;
        }
        return true;
    }

    public void setupView(){
        recyclerView = findViewById(R.id.recyclerViewSaved);
        recyclerView.setHasFixedSize(true);
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(sacuvaniOglasi.this));

        viewThis = findViewById(R.id.sacuvaniOglasiActivity);
        viewNoInternet = (View) findViewById(R.id.nemaInternet);
        viewNotLoggedIn = (View) findViewById(R.id.nijePrijavljen);
        viewNotSaved = (View) findViewById(R.id.nemaSacuvanih);
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
                        if (tryToStart()) ShowEverything();
                    }
                }, 1000);
            }
        });

        goToLoginButton = viewNotLoggedIn.findViewById(R.id.goToLoginIfDidnt);
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(sacuvaniOglasi.this, Login.class);
                startActivity(i);
            }
        });

        tryToStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sacuvani_oglasi);

        setupView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(IsConnectedToInternet()){
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                ShowEverything();
                viewNotLoggedIn.setVisibility(View.GONE);
                tryToStart();
            }
        }
    }
}