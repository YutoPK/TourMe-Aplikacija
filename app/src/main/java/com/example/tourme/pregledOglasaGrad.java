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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.tourme.Adapters.OglasAdapter;
import com.example.tourme.Model.Gradovi;
import com.example.tourme.Model.Oglas;
import com.example.tourme.Model.StaticVars;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class pregledOglasaGrad extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //View
    RecyclerView recyclerView;
    View viewNoInternet, viewThis;
    ProgressBar progressBar;
    Button tryAgainButton;
    Spinner spinnerForSorting;

    //Firebase
    DatabaseReference reference;

    //Variables
    OglasAdapter oglasAdapter;
    List<String> items;
    Handler h = new Handler();
    int reasonForBadConnection = 1, sortingVariable = 0;
    String grad;
    ArrayAdapter<CharSequence> adapter;


    void resetSpinner(){
        spinnerForSorting.setSelection(adapter.getPosition("Po Relevantnosti"));
        sortingVariable = 0;
    }

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

    public boolean tryToStart(){
        if(IsConnectedToInternet()){
            reference = FirebaseDatabase.getInstance().getReference();
            reference.child("oglasi").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Oglas> mOglas = new ArrayList<>();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Oglas oglas = dataSnapshot.getValue(Oglas.class);
                        if(oglas.getGrad().equals(grad))
                            mOglas.add(oglas);
                    }

                    mOglas = sortByVariable(mOglas);
                    oglasAdapter = new OglasAdapter(pregledOglasaGrad.this, mOglas);
                    recyclerView.setAdapter(oglasAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            HideWithReason(1);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregled_oglasa_grad);

        StaticVars.listOfFragments.add(13);

        grad = getIntent().getStringExtra("grad");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        spinnerForSorting = findViewById(R.id.sortType2);
        adapter = ArrayAdapter.createFromResource(this, R.array.sortingType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerForSorting.setAdapter(adapter);
        spinnerForSorting.setOnItemSelectedListener(this);

        viewThis = findViewById(R.id.pregledOglasaGradActivity);
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

    }

    public List<Oglas> sortByVariable(List<Oglas> mOglas){
        if(sortingVariable == 1) {
            Comparator<Oglas> cmp1 = (Oglas a, Oglas b) -> {
                if ((b.getCenaOglasa()) < ((a.getCenaOglasa()))) return 1;
                else if ((b.getCenaOglasa()) == ((a.getCenaOglasa()))) return 0;
                return -1;
            };
            Collections.sort(mOglas, cmp1);
        }else if(sortingVariable == 2){
            Comparator<Oglas> cmp2 = (Oglas a, Oglas b) -> {
                if ((b.getCenaOglasa()) > ((a.getCenaOglasa()))) return 1;
                else if ((b.getCenaOglasa()) == ((a.getCenaOglasa()))) return 0;
                return -1;
            };
            Collections.sort(mOglas, cmp2);
        }else if(sortingVariable == 3){
            Comparator<Oglas> cmp3 = (Oglas a, Oglas b) -> {
                if ((b.getOcena()) < ((a.getOcena()))) return 1;
                else if ((b.getOcena()) == ((a.getOcena()))) return 0;
                return -1;
            };
            Collections.sort(mOglas, cmp3);
        }else if(sortingVariable == 4){
            Comparator<Oglas> cmp4 = (Oglas a, Oglas b) -> {
                if ((b.getOcena()) > ((a.getOcena()))) return 1;
                else if ((b.getOcena()) == ((a.getOcena()))) return 0;
                return -1;
            };
            Collections.sort(mOglas, cmp4);
        }

        return mOglas;
    }

    public void sortOglases(){
        if(IsConnectedToInternet()) {
            FirebaseDatabase.getInstance().getReference().child("oglasi").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    List<Oglas> mOglas = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        Oglas oglas = dataSnapshot.getValue(Oglas.class);
                        if(oglas.getGrad().equals(grad))
                            mOglas.add(oglas);
                    }
                    mOglas = sortByVariable(mOglas);

                    oglasAdapter = new OglasAdapter(pregledOglasaGrad.this, mOglas);
                    recyclerView.setAdapter(oglasAdapter);
                }
            });
        }else{
            HideWithReason(1);
            resetSpinner();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String textFromSpinner = adapterView.getItemAtPosition(i).toString();
        switch (textFromSpinner) {
            case "ceni opadajuće":
                sortingVariable = 1;
                sortOglases();
                break;
            case "ceni rastuće":
                sortingVariable = 2;
                sortOglases();
                break;
            case "oceni opadajuće":
                sortingVariable = 3;
                sortOglases();
                break;
            case "oceni rastuće":
                sortingVariable = 4;
                sortOglases();
                break;
            default:
                sortingVariable = 0;
                sortOglases();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}