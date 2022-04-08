package com.example.tourme;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

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
import android.widget.ProgressBar;

import com.example.tourme.Model.Gradovi;
import com.example.tourme.Model.Oglas;
import com.example.tourme.Model.StaticVars;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.tourme.databinding.ActivityMapaGradoviBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.rpc.Help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mapaGradovi extends FragmentActivity implements OnMapReadyCallback {

    //View

    //Firebase
    private GoogleMap mMap;
    private ActivityMapaGradoviBinding binding;

    //Variables

    Boolean IsConnectedToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    public void setupFirebase(){
        binding = ActivityMapaGradoviBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void tryToStart(){
        if(IsConnectedToInternet()){
            setupFirebase();
        }else{
            Intent i = new Intent(mapaGradovi.this, HelperMapaGradovi.class);
            startActivity(i);
            finish();
        }
    }

    public void setupView(){
        StaticVars.listOfFragments.add(14);
        tryToStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupView();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final List<Gradovi.Grad> lokacija  = Arrays.asList(
        new Gradovi.Grad("Beograd", new LatLng(44.8167, 20.4667)),
        new Gradovi.Grad("Novi Sad", new LatLng(45.2644, 19.8317)),
        new Gradovi.Grad("Niš", new LatLng(43.3192, 21.8961)),
        new Gradovi.Grad("Kragujevac", new LatLng(44.0142, 20.9394)),
        new Gradovi.Grad("Priština", new LatLng(42.667542, 21.166191)),
        new Gradovi.Grad("Subotica", new LatLng(46.0983, 19.6700)),
        new Gradovi.Grad("Zrenjanin", new LatLng(45.3778, 20.3861)),
        new Gradovi.Grad("Pančevo", new LatLng(44.8739, 20.6519)),
        new Gradovi.Grad("Čačak", new LatLng(43.8914, 20.3497)),
        new Gradovi.Grad("Kruševac", new LatLng(43.5833, 21.3267)),
        new Gradovi.Grad("Kraljevo", new LatLng(43.7234, 20.6870)),
        new Gradovi.Grad("Novi Pazar", new LatLng(43.1500, 20.5167)),
        new Gradovi.Grad("Smederevo", new LatLng(44.66278, 20.93)),
        new Gradovi.Grad("Leskovac", new LatLng(42.9981, 21.9461)),
        new Gradovi.Grad("Užice", new LatLng(43.8500, 19.8500)),
        new Gradovi.Grad("Vranje", new LatLng(42.5542, 21.8972)),
        new Gradovi.Grad("Valjevo", new LatLng(44.2667, 19.8833)),
        new Gradovi.Grad("Šabac", new LatLng(44.748861, 19.690788)),
        new Gradovi.Grad("Sombor", new LatLng(45.7800, 19.1200)),
        new Gradovi.Grad("Požarevac", new LatLng(44.62133, 21.18782)),
        new Gradovi.Grad("Pirot", new LatLng(43.15306, 22.58611)),
        new Gradovi.Grad("Zaječar", new LatLng(43.90358, 22.26405)),
        new Gradovi.Grad("Kikinda", new LatLng(45.8244, 20.4592)),
        new Gradovi.Grad("Sremska Mitrovica", new LatLng(44.97639, 19.61222)),
        new Gradovi.Grad("Vršac", new LatLng(45.1206, 21.2986)),
        new Gradovi.Grad("Bor", new LatLng(44.07488, 22.09591)),
        new Gradovi.Grad("Prokuplje", new LatLng(43.23417, 21.58806)),
        new Gradovi.Grad("Jagodina", new LatLng(43.97713, 21.26121)),
        new Gradovi.Grad("Loznica", new LatLng(44.5333, 19.2258)),
        new Gradovi.Grad("Zlatar", new LatLng(43.4103, 19.7926)),
        new Gradovi.Grad("Tara", new LatLng(43.8470, 19.4500)),
        new Gradovi.Grad("Golija", new LatLng(43.3486, 20.3021)),
        new Gradovi.Grad("Rtanj", new LatLng(43.7807, 21.9318)),
        new Gradovi.Grad("Vrnjačka Banja", new LatLng(43.6394, 20.8880)),
        new Gradovi.Grad("Niška Banja", new LatLng(43.3070, 22.0029)),
        new Gradovi.Grad("Sokobanja", new LatLng(43.6794, 21.8881)),
        new Gradovi.Grad("Prolom Banja", new LatLng(43.0502, 21.4128)),
        new Gradovi.Grad("Ribarska Banja", new LatLng(43.4300, 21.5036)),
        new Gradovi.Grad("Rudnik", new LatLng(44.1613, 20.4968)),
        new Gradovi.Grad("Kopaonik", new LatLng(43.2767, 20.7870)),
        new Gradovi.Grad("Aleksinac", new LatLng(43.5414, 21.7081)),
        new Gradovi.Grad("Knjaževac", new LatLng(43.5662, 22.2480)),
        new Gradovi.Grad("Ćuprija", new LatLng(43.9731, 21.3773)),
        new Gradovi.Grad("Paraćin", new LatLng(43.8870, 21.4116)),
        new Gradovi.Grad("Arandjelovac", new LatLng(44.3261, 20.5528)),
        new Gradovi.Grad("Lazarevac", new LatLng(44.3904, 20.2584)),
        new Gradovi.Grad("Zlatibor", new LatLng(43.7291, 19.7048)));

        FirebaseDatabase.getInstance().getReference("oglasi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> zaPrikaz = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Oglas oglas = dataSnapshot.getValue(Oglas.class);
                    String grad = oglas.getGrad();
                    zaPrikaz.add(grad);
                }
                for(int i=0;i<lokacija.size();++i){
                    if(zaPrikaz.contains(lokacija.get(i).getIme())){
                        mMap.addMarker(new MarkerOptions().position(lokacija.get(i).getLokacija()).title(lokacija.get(i).getIme()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lokacija.get(i).getLokacija()));
                    }

                }
                //centrira mapu u centar srbiju
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Intent i = new Intent(getApplicationContext(), pregledOglasaGrad.class);
                        i.putExtra("grad", marker.getTitle());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        return false;
                    }
                });
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.010013,20.490270), 6.8f));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}