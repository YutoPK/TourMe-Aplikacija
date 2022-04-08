package com.example.tourme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tourme.Adapters.NotificationAdapter;
import com.example.tourme.Adapters.UserAdapater;
import com.example.tourme.Login;
import com.example.tourme.Model.Chat;
import com.example.tourme.Model.Gradovi;
import com.example.tourme.Model.Notification;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.example.tourme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Obavestenja#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Obavestenja extends Fragment {

    //View
    View viewNoInternet, viewThis, viewNotLoggedIn, viewNoNotifications;
    ProgressBar progressBar;
    Button tryAgainButton, goToLoginButton;
    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    FloatingActionButton deleteNotifications;

    //Firebase

    //Variables
    Handler h = new Handler();
    int reasonForBadConnection = 1;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public Obavestenja() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Obavestenja.
     */

    public static Obavestenja newInstance(String param1, String param2) {
        Obavestenja fragment = new Obavestenja();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        for (int i = 0 ;i < viewgroup.getChildCount(); i++) {
            View v1 = viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup){
                if(v1 != viewNoInternet && v1 != viewNotLoggedIn)
                    ShowEverythingRecursion(v1);
            }else
                v1.setVisibility(View.VISIBLE);
        }
    }

    void ShowEverything(){
        ShowEverythingRecursion(viewThis);
        viewNoInternet.setVisibility(View.GONE);
//        if(recyclerView.getAdapter() != null)
//            shiftViewNoNotifications(recyclerView.getAdapter().getItemCount());
    }

    Boolean IsConnectedToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    public void shiftViewNoNotifications(int len){
        if(len == 0)
            viewNoNotifications.setVisibility(View.VISIBLE);
        else
            viewNoNotifications.setVisibility(View.GONE);
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet()){
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notifications");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Notification> mNotification = new ArrayList<>();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Notification notification = dataSnapshot.getValue(Notification.class);

                            if(notification.getTo().equals(FirebaseAuth.getInstance().getUid())){
                                mNotification.add(notification);
                            }

                        }
                        Collections.reverse(mNotification);
                        shiftViewNoNotifications(mNotification.size());
                        notificationAdapter = new NotificationAdapter(getContext(), mNotification);
                        recyclerView.setAdapter(notificationAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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

    public void setupView(View view){
        StaticVars.listOfFragments.add(3);

        viewThis = view.findViewById(R.id.obavestenjaFragment);
        viewNoInternet = (View) view.findViewById(R.id.nemaInternet);
        viewNotLoggedIn = (View) view.findViewById(R.id.nijePrijavljen);
        viewNoNotifications = (View) view.findViewById(R.id.noNotifications);
        progressBar = viewNoInternet.findViewById(R.id.progressBar);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                Intent i = new Intent(getActivity(), Login.class);
                startActivity(i);
            }
        });

        deleteNotifications = view.findViewById(R.id.deleteNotifications);
        deleteNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllNotifications();
            }
        });
        tryToStart();
    }

    public void deleteAllNotifications(){
            if(IsConnectedToInternet()){
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notifications");
                    reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            DataSnapshot snapshot = task.getResult();
                            boolean brisano = false;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                Notification notification = dataSnapshot.getValue(Notification.class);

                                if(notification.getTo().equals(FirebaseAuth.getInstance().getUid())){
                                    dataSnapshot.getRef().removeValue();
                                    brisano = true;
                                }

                            }
                            if(brisano) {
                                Toast.makeText(getContext(), "Obaveštenja su izbrisana", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }else{
                HideWithReason(2);
            }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_obavestenja, container, false);
        setupView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(IsConnectedToInternet()){
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                ShowEverything();
                viewNotLoggedIn.setVisibility(View.GONE);
                tryToStart();
            }else{
                HideEverything(2);
            }
        }else{
            HideWithReason(1);
        }
    }
}