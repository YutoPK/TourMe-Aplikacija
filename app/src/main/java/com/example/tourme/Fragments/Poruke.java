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

import com.example.tourme.Login;
import com.example.tourme.Model.Chat;
import com.example.tourme.Model.Gradovi;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.example.tourme.Notifications.Token;
import com.example.tourme.R;
import com.example.tourme.Adapters.UserAdapater;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Poruke#newInstance} factory method to
 * create an instance of this fragment.
 */
public class  Poruke extends Fragment {

    //View
    View viewNoInternet, viewThis, viewNotLoggedIn, viewNoMessages;
    ProgressBar progressBar;
    Button tryAgainButton, goToLoginButton;

    //Firebase

    //Variables
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    Handler h = new Handler();
    int reasonForBadConnection = 1;

    public Poruke() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Poruke.
     */

    public static Poruke newInstance(String param1, String param2) {
        Poruke fragment = new Poruke();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
                if(v1 != viewNoInternet && v1 != viewNotLoggedIn && v1 != viewNoMessages)
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
                if(v1 != viewNoInternet && v1 != viewNotLoggedIn && v1 != viewNoMessages)
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

    public void manageNoMessages(int len){
        if(len == 0)
            viewNoMessages.setVisibility(View.VISIBLE);
        else
            viewNoMessages.setVisibility(View.GONE);
    }

    List<String> usersList;
    HashMap<String, Integer> usersListHM;
    int numberOfUsers = 0;

    public boolean tryToStart(){
        if(IsConnectedToInternet()){
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersListHM.clear();
                        numberOfUsers = 0;
                        List<Chat> chatsReversed = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            chatsReversed.add(chat);
                        }

                        Collections.reverse(chatsReversed);
                        for(Chat chat : chatsReversed){
                            if (chat.getSender().equals(firebaseUser.getUid())) {
                                if(!usersListHM.containsKey(chat.getReceiver())){
                                    usersListHM.put(chat.getReceiver(),numberOfUsers);
                                    numberOfUsers++;
                                }
                            }
                            if (chat.getReceiver().equals(firebaseUser.getUid())) {
                                if(!usersListHM.containsKey(chat.getSender())){
                                    usersListHM.put(chat.getSender(),numberOfUsers);
                                    numberOfUsers++;
                                }
                            }
                        }

                        readChats();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            String token = task.getResult().toString();
                            Log.d("Token", "token:" + token );
                            updateToken(token);
                        }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private RecyclerView recyclerView;
    private UserAdapater userAdapater;

    public void setupView(View view){
        StaticVars.listOfFragments.add(2);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersList = new ArrayList<>();
        usersListHM = new HashMap<String, Integer>();

        viewThis = view.findViewById(R.id.porukeFragment);
        viewNoInternet = (View) view.findViewById(R.id.nemaInternet);
        viewNotLoggedIn = (View) view.findViewById(R.id.nijePrijavljen);
        viewNoMessages = (View) view.findViewById(R.id.nemaPoruka);
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

        goToLoginButton = viewNotLoggedIn.findViewById(R.id.goToLoginIfDidnt);
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), Login.class);
                startActivity(i);
            }
        });

        tryToStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_poruke, container, false);
        setupView(view);

        return view;
    }

    private void readChats(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User[] helperUsers = new User[numberOfUsers];
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    if(usersListHM.containsKey(user.getId())){
                        helperUsers[usersListHM.get(user.getId())] = user;
                    }
                }

                List<User> mUsers = new ArrayList<>(Arrays.asList(helperUsers).subList(0, numberOfUsers));

                manageNoMessages(mUsers.size());
                userAdapater = new UserAdapater(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapater);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(FirebaseAuth.getInstance().getUid()).setValue(token1);
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