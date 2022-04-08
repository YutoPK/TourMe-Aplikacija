package com.example.tourme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class editOglas extends AppCompatActivity {

    //View
    EditText editDescirbeText, editPriceText;
    TextView cityText;
    Button changeOglasButton, dontChangeOglasButton, tryAgainButton;
    View viewNoInternet, viewThis;
    ProgressBar progressBar;

    //Firebase

    //Variables
    Handler h = new Handler();
    int reasonForBadConnection = 1;
    String grad, opis, cena, IDOglasa;

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

    private void ShowEverythingRecursion(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i = 0 ; i < viewgroup.getChildCount(); i++) {
            View v1 = viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup){
                if(v1 != viewNoInternet) {
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

    public void setupView(){
        editDescirbeText = findViewById(R.id.describeEditOglas);
        editPriceText = findViewById(R.id.priceEditOglas);
        cityText = findViewById(R.id.gradEditOglas);

        grad = getIntent().getStringExtra("grad");
        opis = getIntent().getStringExtra("opis");
        cena = getIntent().getStringExtra("cena");
        IDOglasa = getIntent().getStringExtra("id");

        cityText.setText(grad);
        editDescirbeText.setText(opis);
        editPriceText.setText(cena);

        viewThis = findViewById(R.id.editOglasActivity);
        viewNoInternet = (View) findViewById(R.id.nemaInternet);
        progressBar = viewNoInternet.findViewById(R.id.progressBar);

        changeOglasButton = findViewById(R.id.editOglas);
        changeOglasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isReady = true;
                String priceText = editPriceText.getText().toString().trim();
                String describeText = editDescirbeText.getText().toString().trim();

                if(TextUtils.isEmpty(priceText)){
                    isReady = false;
                    editPriceText.setError("Unesite cenu oglasa");
                }
                if(TextUtils.isEmpty(describeText)){
                    isReady = false;
                    editDescirbeText.setError("Unesite opis oglasa");
                }
                if(isReady){
                    if(IsConnectedToInternet()){
                        int cenaInt = Integer.parseInt(priceText);
                        FirebaseDatabase.getInstance().getReference().child("oglasi").child(IDOglasa).child("opis").setValue(describeText);
                        FirebaseDatabase.getInstance().getReference().child("oglasi").child(IDOglasa).child("cenaOglasa").setValue(cenaInt);
                        Toast.makeText(editOglas.this,"Uspešno sačuvano",Toast.LENGTH_LONG).show();
                        finish();
                    }else{
                        HideEverything();
                    }
                }
            }
        });

        dontChangeOglasButton = findViewById(R.id.odustani);
        dontChangeOglasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tryAgainButton = viewNoInternet.findViewById(R.id.TryAgainButton);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideButtonShowProgress();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressShowButton();
                        ShowEverything();
                    }
                }, 1000);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_oglas);

        setupView();
    }
}