package com.example.tourme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.PathParser;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tourme.Adapters.OglasAdapter;
import com.example.tourme.Glavni_ekran;
import com.example.tourme.IzmeniAccountActivity;
import com.example.tourme.Model.Gradovi;
import com.example.tourme.Model.Oglas;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.example.tourme.Notifications.Token;
import com.example.tourme.R;
import com.example.tourme.mapaGradovi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Pocetni extends Fragment implements AdapterView.OnItemSelectedListener {

    //View
    RecyclerView recyclerView;
    AutoCompleteTextView searchBar;
    Button searchButton;
    View viewNoInternet, viewThis, viewNoOglas;
    ProgressBar progressBar;
    Button tryAgainButton;
    Spinner spinnerForSorting;
    ProgressBar loadingBar;

    //Firebase
    DatabaseReference reference;

    //Variables
    OglasAdapter oglasAdapter;
    Gradovi g;
    List<String> items;
    Handler h = new Handler();
    int reasonForBadConnection = 1, sortingVariable = 0;
    ArrayAdapter<CharSequence> adapter;

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

    void resetSpinner(){
        spinnerForSorting.setSelection(adapter.getPosition("relevantnosti"));
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
                if(v1 != viewNoInternet && v1 != viewNoOglas)
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
                if(v1 != viewNoInternet && v1 != viewNoOglas)
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
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    public void hideNoOglas(int len) {
        if(len == 0)
            viewNoOglas.setVisibility(View.VISIBLE);
        else
            viewNoOglas.setVisibility(View.GONE);
    }

    public void search(){
        String inputText = searchBar.getText().toString().trim();
        if(TextUtils.isEmpty(inputText)){
            searchBar.setError("Unesite text");
        }else{
            if(IsConnectedToInternet()){
                reference.child("oglasi").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        List<Oglas> mOglas = new ArrayList<>();
                        List<String> newItems = g.Search(inputText);
                        items = newItems;
                        StaticVars.lastSearch = inputText;

                        for(DataSnapshot dataSnapshot : task.getResult().getChildren()){
                            Oglas oglas = dataSnapshot.getValue(Oglas.class);
                            String gradOglasaa = oglas.getGrad();
                            if(newItems.contains(gradOglasaa.toLowerCase()))
                                mOglas.add(oglas);
                        }
                        resetSpinner();
                        hideNoOglas(mOglas.size());

                        oglasAdapter = new OglasAdapter(getContext(), mOglas);
                        recyclerView.setAdapter(oglasAdapter);
                    }
                });
            }else{
                HideWithReason(1);
            }
        }
    }

    public void setupFirebase(){

        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful()) {
                        String token = task.getResult().toString();
                        Log.d("Token", "token:" + token);
                        updateToken(token);
                    }
                }
            });
        }

        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("oglasi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Oglas> mOglas = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Oglas oglas = dataSnapshot.getValue(Oglas.class);
                    String gradOglasaa = oglas.getGrad();
                    if(items.contains(gradOglasaa.toLowerCase()))
                        mOglas.add(oglas);
                }
                mOglas = sortByVariable(mOglas);
                hideNoOglas(mOglas.size());
                loadingBar.setVisibility(View.GONE);

                oglasAdapter = new OglasAdapter(getContext(), mOglas);
                recyclerView.setAdapter(oglasAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet())
            setupFirebase();
        else{
            HideWithReason(1);
            return false;
        }
        return true;
    }

    public void setupView(View view){
//        Log.e("grad","1");

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        viewThis = view.findViewById(R.id.pocetniFragment);
        viewNoInternet = (View) view.findViewById(R.id.nemaInternet);
        progressBar = viewNoInternet.findViewById(R.id.progressBar);
        loadingBar = view.findViewById(R.id.loadingBar);

        spinnerForSorting = view.findViewById(R.id.sortType);
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sortingType, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner_drop);
        spinnerForSorting.setAdapter(adapter);
        spinnerForSorting.setOnItemSelectedListener(this);

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


        List<Oglas> mOglas2 = new ArrayList<>();
        oglasAdapter = new OglasAdapter(getContext(), mOglas2);
        recyclerView.setAdapter(oglasAdapter);

        viewNoOglas = (View) view.findViewById(R.id.nema_oglasa);

        g = new Gradovi();
        items = g.getAllCities();

        searchBar = view.findViewById(R.id.searchBar);
        searchBar.setError(null);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, g.getAllCitiesC());
        searchBar.setAdapter(adapter2);

        tryToStart();

        searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                search();
            }
        });

        searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(FirebaseAuth.getInstance().getUid()).setValue(token1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pocetni, container, false);
        setupView(view);

        return view;
    }


    public void sortOglases(){
        if(IsConnectedToInternet()) {
            FirebaseDatabase.getInstance().getReference().child("oglasi").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    List<Oglas> mOglas = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        Oglas oglas = dataSnapshot.getValue(Oglas.class);
                        String gradOglasaa = oglas.getGrad();
                        if (items.contains(gradOglasaa.toLowerCase()))
                            mOglas.add(oglas);
                    }
                    hideNoOglas(mOglas.size());
                    mOglas = sortByVariable(mOglas);

                    oglasAdapter = new OglasAdapter(getContext(), mOglas);
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
    public void onResume() {
        super.onResume();
        StaticVars.listOfFragments.add(1);
        int len = StaticVars.listOfFragments.size();
        if(len >= 2 && StaticVars.listOfFragments.get(len - 2) == 1)
            StaticVars.lastSearch = "";

        if(!StaticVars.lastSearch.equals("")) {
            searchBar.setText(StaticVars.lastSearch);
            search();
            searchBar.dismissDropDown();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}