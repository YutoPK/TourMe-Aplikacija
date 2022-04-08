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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.tourme.Adapters.MessageAdapter;
import com.example.tourme.Model.Chat;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.example.tourme.Notifications.APIService;
import com.example.tourme.Notifications.Client;
import com.example.tourme.Notifications.Data;
import com.example.tourme.Notifications.MyResponse;
import com.example.tourme.Notifications.Sender;
import com.example.tourme.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    //View
    ImageView profile_image;
    TextView username;
    RecyclerView recyclerView;
    View viewNoInternet, viewThis, viewNotLoggedIn;
    ProgressBar progressBar;
    Button tryAgainButton, goToLoginButton;
    Intent intent;
    ImageButton button_send, button_back;
    EditText text_send;
    ValueEventListener seenListener;

    //Firebase
    FirebaseUser fUser;
    DatabaseReference reference;

    //Variables
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    Handler h = new Handler();
    int reasonForBadConnection = 1;
    String userid;
    String startedfrom;

    User senderInfo;

    APIService apiService;
    boolean notify = false;

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
    }

    Boolean IsConnectedToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    public void setupFireBase(){
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageurl().equals("default")) {
                    profile_image.setImageResource(R.drawable.default_image);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageurl()).into(profile_image);
                }

                readMessages(fUser.getUid(), userid, user.getImageurl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                senderInfo = user;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userid);
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet()){
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
        StaticVars.listOfFragments.add(8);

        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewThis = findViewById(R.id.messageActivity);
        viewNoInternet = (View) findViewById(R.id.nemaInternet);
        viewNotLoggedIn = (View) findViewById(R.id.nijePrijavljen);
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
                Intent i = new Intent(MessageActivity.this, Login.class);
                startActivity(i);
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        button_send = findViewById(R.id.button_send);
        button_back = findViewById(R.id.back);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        startedfrom = intent.getStringExtra("startedfrom");

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsConnectedToInternet()){
                    notify = true;
                    String msg = text_send.getText().toString();
                    if(!msg.equals("")){
                        sendMessage(fUser.getUid(), userid, msg.trim());
                        sendNotification1(fUser.getUid(), userid);
                    }else{
                        Toast.makeText(MessageActivity.this, "Ne možete da pošaljete praznu poruku", Toast.LENGTH_LONG).show();
                    }

                    text_send.setText("");
                }else{
                    HideWithReason(2);
                }
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startedfrom != null){
                    finish();
                }
                else{
                    Intent i = new Intent(MessageActivity.this, Glavni_ekran.class);
                    i.putExtra("fragment","poruke");
                    startActivity(i);
                    startedfrom = "nesto";
                }
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccount(userid);
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccount(userid);
            }
        });

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(recyclerView.getAdapter() != null)
                                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 0);
                }
            }
        });

        tryToStart();
    }

    @Override
    public void onBackPressed(){
        if(startedfrom != null){
            finish();
        }
        else{
            Intent i = new Intent(MessageActivity.this, Glavni_ekran.class);
            i.putExtra("fragment","poruke");
            startActivity(i);
            startedfrom = "nesto";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setupView();

    }

    private void openAccount(String userid){
        if(FirebaseAuth.getInstance().getCurrentUser().getUid()!=userid){
            Intent intent = new Intent(MessageActivity.this, Account.class);
            intent.putExtra("userid",userid);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(MessageActivity.this, MyAccount.class);
            startActivity(intent);
        }

    }

    private void seenMessage(String userid){
        reference = FirebaseDatabase.getInstance().getReference("chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification1(String sender, String receiver){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("to",receiver);
        hashMap1.put("title","Nova poruka");
        hashMap1.put("body",senderInfo.getUsername()+" ti je poslao poruku");
        reference.child("notifications").push().setValue(hashMap1);

    }



    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);

        reference.child("chats").push().setValue(hashMap);

        final String msg = message;
        if (notify) {
            sendNotification(receiver, senderInfo.getUsername(), msg);
            notify = false;
        }

    }
    private void sendNotification(String receiver, String username, String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), R.drawable.logo,username + " ti je poslao poruku", "Nova poruka", userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code()==200){
                                if(response.body().success == 1){
                                    //Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_LONG).show();
                                    //Log.e("Test",response.message());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void readMessages(String myid, String userid, String imageurl){
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.scrollToPosition(mChat.size()-1);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        StaticVars.isPoruke = true;
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
        StaticVars.isPoruke = false;
//        reference.removeEventListener(seenListener);
        status("offline");
    }


}