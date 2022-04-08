package com.example.tourme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tourme.Model.Oglas;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class dodajOglas extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //View
    Button addOglas, tryAgainButton, goToLoginButton;
    EditText editTextForDescribe, editTextForPrice;
    View viewNoInternet, viewThis, viewNotLoggedIn;
    Spinner city;
    ProgressBar progressBar;

    //Firebase
    private DatabaseReference mDatabase;
    FirebaseAuth fAuth;

    //Variables
    boolean isGood = true;
    String textForDescribe, textForPrice, cityText;
    Handler h = new Handler();
    int reasonForBadConnection = 1;

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
        for (int i = 0 ; i < viewgroup.getChildCount(); i++) {
            View v1 = viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup){
                if(v1 != viewNoInternet && v1 != viewNotLoggedIn)
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
        for (int i = 0 ; i < viewgroup.getChildCount(); i++) {
            View v1 = viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup){
                if(v1 != viewNoInternet && v1 != viewNotLoggedIn) {
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

    void setDescribeError(String errorText){
        editTextForDescribe.setError(errorText);
        isGood = false;
    }

    void setPriceError(String errorText){
        editTextForPrice.setError(errorText);
        isGood = false;
    }

    void createToast(String toastText){
        Toast.makeText(dodajOglas.this, toastText, Toast.LENGTH_LONG).show();
    }

    public void finishAddingOglas(String userId, String imageurl, String username){
        if(IsConnectedToInternet()){
            mDatabase.child("users").child(userId).child("brojOglasa").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful())
                        HideWithReason(2);
                    else {
                        String numberOfOglas = String.valueOf(task.getResult().getValue());

                        if(!numberOfOglas.equals("null")){
                            DatabaseReference ref = mDatabase.child("oglasi").push();
                            String idOglasa = ref.getKey();
                            Integer priceForOglas = Integer.parseInt(textForPrice);
                            Oglas oglas = new Oglas(idOglasa, cityText, 0.0, 0, priceForOglas, userId, textForDescribe);

                            Integer intNumOfOglas = Integer.parseInt(numberOfOglas) + 1;
                            String newNumberForOglas = intNumOfOglas.toString();

                            mDatabase.child("oglasi").child(idOglasa).setValue(oglas);
                            mDatabase.child("users").child(userId).child("brojOglasa").setValue(newNumberForOglas);
                            mDatabase.child("users").child(userId).child("oglas").child(cityText).setValue(idOglasa);
                            createToast("Uspešno ste postavili oglas");
                            finish();
                        }else
                            HideWithReason(2);
                    }
                }
            });
        }else
            HideWithReason(2);
    }

    public void continueAddingOglas(String userId, String imageurl, String username){
        if(IsConnectedToInternet()){
            mDatabase.child("users").child(userId).child("oglas").child(cityText).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful())
                        HideWithReason(2);
                    else {
                        String existsOglas = String.valueOf(task.getResult().getValue());
                        if(existsOglas.equals("null"))
                            finishAddingOglas(userId, imageurl, username);
                        else
                            createToast("Već postoji oglas za ovaj grad");
                    }
                }
            });
        }else
            HideWithReason(2);
    }

    public void startAddingOglas(){
        if(IsConnectedToInternet()){
            String userId = fAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful())
                        HideWithReason(2);
                    else {
                        User user = task.getResult().getValue(User.class);

                        if(user != null){
                            String imageurl = user.getImageurl();
                            String username = user.getUsername();
                            continueAddingOglas(userId, imageurl, username);
                        }else
                            createToast("Ne postoji ovakav nalog");
                    }
                }
            });
        }else
            HideWithReason(2);
    }

    public void setupFireBase(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                setupFireBase();
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
        StaticVars.listOfFragments.add(6);

        viewThis = findViewById(R.id.dodajOglasActivity);
        viewNoInternet = (View) findViewById(R.id.nemaInternet);
        viewNotLoggedIn = (View) findViewById(R.id.nijePrijavljen);
        progressBar = viewNoInternet.findViewById(R.id.progressBar);

        city = findViewById(R.id.cityForOglas);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city.setAdapter(adapter);
        city.setOnItemSelectedListener(this);

        addOglas = findViewById(R.id.addOglas);
        editTextForDescribe = findViewById(R.id.describeOglas);
        editTextForPrice = findViewById(R.id.priceEditText);

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

        goToLoginButton = viewNotLoggedIn.findViewById(R.id.goToLoginIfDidnt);
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(dodajOglas.this, Login.class);
                startActivity(i);
            }
        });

        addOglas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsConnectedToInternet()){
                    isGood = true;

                    if(fAuth.getCurrentUser()!=null) {
                        textForDescribe = editTextForDescribe.getText().toString().trim();
                        textForPrice = editTextForPrice.getText().toString().trim();
                        if(TextUtils.isEmpty(textForDescribe))
                            setDescribeError("Unesite opis oglasa");

                        if(TextUtils.isEmpty(textForPrice))
                            setPriceError("Unesite cenu oglasa");

                        if(isGood)
                            startAddingOglas();
                    }
                }else
                    HideWithReason(2);
            }
        });

        tryToStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_oglas);

        setupView();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        cityText = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
        if(IsConnectedToInternet()){
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                ShowEverything();
                viewNotLoggedIn.setVisibility(View.GONE);
                tryToStart();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}