package com.example.tourme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tourme.Adapters.CommentAdapter;
import com.example.tourme.Model.Comment;
import com.example.tourme.Model.Oglas;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.example.tourme.Notifications.APIService;
import com.example.tourme.Notifications.Client;
import com.example.tourme.Notifications.Data;
import com.example.tourme.Notifications.MyResponse;
import com.example.tourme.Notifications.Sender;
import com.example.tourme.Notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class pregledJednogOglasa extends AppCompatActivity {

    //View
//    Spinner rating;
    Button sendMessage, buttonAddRating, tryAgainButton, editOglasButton, deleteOglasButton, backButton;
    Button yesDeleteButton, noDeleteButton, saveOglasButton;
    ImageView profile_image, slikaGrada;
    TextView ime, prezime, opis, grad, starost, cena, averageRatingText, numberOfRatingsText, username;
    EditText textForNewRating;
    RatingBar newRatingBar, averageRatingBar;
    View viewNoInternet, viewNoPage, viewThis, viewDodajOcenu, viewConfirmDelete;
    ProgressBar progressBar;
    ScrollView scrollView;

    //FireBase
    private DatabaseReference mDatabase;

    //Variables
    String newRatingText;
    String IDOglasa, nazivGrada, IDUser;
    String opisString, gradString, cenaString;
    String startedfrom;
    Handler h = new Handler();
    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    int reasonForBadConnection = 1;
    boolean isGood = true, isDeleteOpen = false, isSaved = false, haveName = true;

    //API
    APIService apiService;

    public void manageButtonsAndViews(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if(FirebaseAuth.getInstance().getUid().equals(IDUser)){
                deleteOglasButton.setVisibility(View.VISIBLE);
                editOglasButton.setVisibility(View.VISIBLE);
                sendMessage.setVisibility(View.GONE);
                viewDodajOcenu.setVisibility(View.GONE);
                saveOglasButton.setVisibility(View.GONE);
            }else{
                deleteOglasButton.setVisibility(View.GONE);
                editOglasButton.setVisibility(View.GONE);
                sendMessage.setVisibility(View.VISIBLE);
                viewDodajOcenu.setVisibility(View.VISIBLE);
                saveOglasButton.setVisibility(View.VISIBLE);
                checkForSaved();
            }
        }else{
            deleteOglasButton.setVisibility(View.GONE);
            editOglasButton.setVisibility(View.GONE);
            sendMessage.setVisibility(View.VISIBLE);
            viewDodajOcenu.setVisibility(View.GONE);
            saveOglasButton.setVisibility(View.GONE);
        }

        if(haveName){
            username.setVisibility(View.INVISIBLE);
            ime.setVisibility(View.VISIBLE);
            prezime.setVisibility(View.VISIBLE);
        }else{
            username.setVisibility(View.VISIBLE);
            ime.setVisibility(View.INVISIBLE);
            prezime.setVisibility(View.INVISIBLE);
        }
    }

    void setRatingTextError(String errorText){
        textForNewRating.setError(errorText);
        isGood = false;
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
                if(v1 != viewNoInternet && v1 != viewNoPage && v1 != viewConfirmDelete && v1 != viewDodajOcenu)
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
                if(v1 != viewNoInternet && v1 != viewNoPage && v1 != viewConfirmDelete && v1 != viewDodajOcenu)
                    ShowEverythingRecursion(v1);
            }else
                v1.setVisibility(View.VISIBLE);
        }
    }

    void ShowEverything(){
        ShowEverythingRecursion(viewThis);
        viewNoInternet.setVisibility(View.GONE);
        manageButtonsAndViews();
    }

    Boolean IsConnectedToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    private void sendNotification1(String receiver){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("to",receiver);
        hashMap1.put("title","Nova ocena");
        hashMap1.put("body","Dobili ste novu ocenu za oglas!");
        reference.child("notifications").push().setValue(hashMap1);

    }

    public void updateUser(){
        if(IsConnectedToInternet()){
            mDatabase.child("users").child(IDUser).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    User user = task.getResult().getValue(User.class);
                    Integer brojOcena = user.getBrojOcena() + 1;
                    double ocena = user.getUkupnaProsecnaOcena();
                    double doubleNewRating = (double)(newRatingBar.getRating());

                    ocena = (((double)brojOcena - 1) * ocena + doubleNewRating) / ((double)brojOcena);
                    ocena = Math.round(ocena * 10.0) / 10.0;
                    FirebaseDatabase.getInstance().getReference().child("users").child(IDUser).child("brojOcena").setValue(brojOcena);
                    FirebaseDatabase.getInstance().getReference().child("users").child(IDUser).child("ukupnaProsecnaOcena").setValue(ocena);

                    newRatingBar.setRating(0.0F);
                    textForNewRating.setText("");
                    Toast.makeText(pregledJednogOglasa.this, "Ocena je dodata", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            HideWithReason(2);
        }
    }

    public void startAddingRating(){
        if(IsConnectedToInternet()){
            mDatabase.child("oglasi").child(IDOglasa).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        HideWithReason(2);
                    }
                    else {
                        Oglas oglas = task.getResult().getValue(Oglas.class);

                        if(oglas != null){
                            Integer brojOcena = oglas.getBrojOcena() + 1;
                            double ocena = oglas.getOcena();
                            double doubleNewRating = (double)(newRatingBar.getRating());

                            ocena = (((double)brojOcena - 1) * ocena + doubleNewRating) / ((double)brojOcena);
                            ocena = Math.round(ocena * 10.0) / 10.0;
                            Comment comment = new Comment(doubleNewRating,newRatingText,FirebaseAuth.getInstance().getCurrentUser().getUid());

                            mDatabase.child("oglasi").child(IDOglasa).child("brojOcena").setValue(brojOcena);
                            mDatabase.child("oglasi").child(IDOglasa).child("ocena").setValue(ocena);
                            mDatabase.child("oglasi").child(IDOglasa).child("oceneOglasa").child(brojOcena.toString()).setValue(comment);
                            updateUser();
                            sendNotification1(oglas.getUserId());
                            sendNotification(oglas);
                        }else{
                            Toast.makeText(pregledJednogOglasa.this, "Ne postoji ovakav oglas", Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });
        }else{
            HideWithReason(2);
        }

    }
    
    private void sendNotification(Oglas oglas){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(oglas.getUserId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(FirebaseAuth.getInstance().getUid(), R.drawable.logo, "Dobili ste novu ocenu", "Nova ocena", oglas.getUserId(), oglas.getIdOglasa(), oglas.getGrad(), oglas.getUserId());

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success == 1) {
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
    public void checkForSaved(){
        String idCurUser = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(idCurUser).child(("sacuvaniOglasi")).child(IDOglasa).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String dataFromDatabase = String.valueOf(task.getResult().getValue());
                if(dataFromDatabase.equals("null")) {
                    isSaved = false;
                    saveOglasButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_star_outline);
                }else{
                    isSaved = true;
                    saveOglasButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_star);
                }
            }
        });
    }

    public void setupFireBase(){
        manageButtonsAndViews();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseDatabase.getInstance().getReference().child("users").child(IDUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(user.getIme().equals("") || user.getPrezime().equals("")){
                    username.setVisibility(View.VISIBLE);
                    username.setText(user.getUsername());
                    ime.setVisibility(View.INVISIBLE);
                    prezime.setVisibility(View.INVISIBLE);
                    haveName = false;
                }
                else{
                    ime.setText(user.getIme());
                    prezime.setText(user.getPrezime());
                    username.setVisibility(View.GONE);
                    haveName = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference("oglasi").child(IDOglasa).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Oglas oglas = snapshot.getValue(Oglas.class);
                if(oglas != null) {
                    FirebaseDatabase.getInstance().getReference("users").child(oglas.getUserId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            User user = snapshot.getValue(User.class);
                            if (oglas != null) {
                                if (user.getImageurl().equals("default"))
                                    profile_image.setImageResource(R.drawable.default_image);
                                else
                                    Glide.with(getApplicationContext()).load(user.getImageurl()).into(profile_image);

                                profile_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        openAccount(oglas.getUserId());
                                    }
                                });

                                opisString = oglas.getOpis();
                                gradString = oglas.getGrad();
                                cenaString = String.valueOf(oglas.getCenaOglasa());

                                numberOfRatingsText.setText(String.valueOf(oglas.getBrojOcena()));
                                averageRatingText.setText(String.valueOf(oglas.getOcena()));
                                averageRatingBar.setRating((float) oglas.getOcena());

                                opis.setText(opisString);
                                grad.setText(gradString);
                                cena.setText(cenaString);

                                String nekiGrad = "@drawable/ph_" + oglas.getGrad().toLowerCase().replace(" ", "_")
                                        .replace("š", "s").replace("č", "c")
                                        .replace("ž", "z").replace("š", "s")
                                        .replace("ć","c");

                                String uri = nekiGrad;
                                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                                Drawable res = getResources().getDrawable(imageResource);
                                slikaGrada.setImageDrawable(res);

                            } else {
                                HideEverything();
                                viewNoInternet.setVisibility(View.GONE);
                                viewNoPage.setVisibility(View.VISIBLE);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    HideEverything();
                    viewNoInternet.setVisibility(View.GONE);
                    viewNoPage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("oglasi").child(IDOglasa).child("oceneOglasa");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> mComment = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    mComment.add(comment);
                }
                commentAdapter = new CommentAdapter(pregledJednogOglasa.this, mComment);
                recyclerView.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteOglas(){
        if(IsConnectedToInternet()){
            mDatabase.child("users").child(IDUser).child("brojOglasa").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful())
                        HideWithReason(2);
                    else {
                        String numberOfOglas = String.valueOf(task.getResult().getValue());

                        if(!numberOfOglas.equals("null")){
                            Integer intNumOfOglas = Integer.parseInt(numberOfOglas) - 1;
                            String newNumberForOglas = intNumOfOglas.toString();

                            mDatabase.child("users").child(IDUser).child("brojOglasa").setValue(newNumberForOglas);
                            FirebaseDatabase.getInstance().getReference().child("oglasi").child(IDOglasa).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("users").child(IDUser).child("oglas").child(nazivGrada).removeValue();
                            Toast.makeText(pregledJednogOglasa.this,"Uspešno izbrisano",Toast.LENGTH_LONG).show();

                            finish();
                        }else {
                            viewConfirmDelete.setVisibility(View.GONE);
                            HideWithReason(2);
                        }
                    }
                }
            });
        }else{
            viewConfirmDelete.setVisibility(View.GONE);
            HideWithReason(2);
        }
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet()) {
            setupFireBase();
        }else{
            HideWithReason(1);
            return false;
        }
        return true;
    }

    public void openDelelePopUp(){
        viewConfirmDelete.setVisibility(View.VISIBLE);
        isDeleteOpen = true;
        sendMessage.setClickable(false);
        buttonAddRating.setClickable(false);
        editOglasButton.setClickable(false);
        deleteOglasButton.setClickable(false);
        profile_image.setClickable(false);
        saveOglasButton.setClickable(false);
    }

    public void closeDeletePopUp(){
        viewConfirmDelete.setVisibility(View.GONE);
        isDeleteOpen = false;
        sendMessage.setClickable(true);
        buttonAddRating.setClickable(true);
        editOglasButton.setClickable(true);
        deleteOglasButton.setClickable(true);
        profile_image.setClickable(true);
        saveOglasButton.setClickable(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(isDeleteOpen){
            Rect viewRect = new Rect();
            View popupWinodw = (View) viewConfirmDelete.findViewById(R.id.popupWindow);
            popupWinodw.getGlobalVisibleRect(viewRect);
            if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                closeDeletePopUp();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setupView(){
        StaticVars.listOfFragments.add(10);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        IDOglasa = getIntent().getStringExtra("IDOglasa");
        nazivGrada = getIntent().getStringExtra("NazivGrada");
        IDUser = getIntent().getStringExtra("IDUser");
        startedfrom = getIntent().getStringExtra("startedfrom");

        viewThis = findViewById(R.id.PregledJednogOglasaActivity);
        viewNoInternet = (View) findViewById(R.id.nemaInternet);
        viewDodajOcenu = (View) findViewById(R.id.dodaj_ocenu);
        viewNoPage = (View) findViewById(R.id.nemaStranice);
        viewConfirmDelete = (View) findViewById(R.id.confrimDelete);
        progressBar = viewNoInternet.findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);

        saveOglasButton = findViewById(R.id.saveOglasButton);
        saveOglasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsConnectedToInternet()){
                    if(!isSaved) {
                        FirebaseAuth fAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase.getInstance().getReference().child("users").child(fAuth.getUid()).child("sacuvaniOglasi").child(IDOglasa).setValue(IDOglasa);
                        Toast.makeText(pregledJednogOglasa.this, "Oglas je sačuvan", Toast.LENGTH_LONG).show();
                        saveOglasButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_star);
                    }else{
                        FirebaseAuth fAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase.getInstance().getReference().child("users").child(fAuth.getUid()).child("sacuvaniOglasi").child(IDOglasa).removeValue();
                        Toast.makeText(pregledJednogOglasa.this, "Oglas je uklonjen sa liste", Toast.LENGTH_LONG).show();
                        saveOglasButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_star_outline);
                    }
                    isSaved = !isSaved;
                }else{
                    HideWithReason(2);
                }
            }
        });

        yesDeleteButton = viewConfirmDelete.findViewById(R.id.yesButton);
        yesDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOglas();
            }
        });

        noDeleteButton = viewConfirmDelete.findViewById(R.id.noButton);
        noDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDeletePopUp();
            }
        });

        editOglasButton = findViewById(R.id.editButton);
        editOglasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(pregledJednogOglasa.this, editOglas.class);
                i.putExtra("opis", opisString);
                i.putExtra("grad", gradString);
                i.putExtra("cena", cenaString);
                i.putExtra("id", IDOglasa);
                startActivity(i);
            }
        });

        backButton = viewNoPage.findViewById(R.id.backButttonNoPage);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        deleteOglasButton = findViewById(R.id.deleteThisButton);
        deleteOglasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDelelePopUp();
            }
        });


        profile_image = findViewById(R.id.profile_image);
        slikaGrada = findViewById(R.id.slikaGrada);
        ime = findViewById(R.id.ime);
        prezime = findViewById(R.id.prezime);
        username = findViewById(R.id.username);
        opis = findViewById(R.id.opis);
        grad = findViewById(R.id.grad);
        cena = findViewById(R.id.cena2);
        textForNewRating = findViewById(R.id.textForRatingForOglas);
        starost = findViewById(R.id.godine);
        newRatingBar = findViewById(R.id.ratingForOglas);
        averageRatingBar = findViewById(R.id.ocena_bar);
        averageRatingText = findViewById(R.id.ocena);
        numberOfRatingsText = findViewById(R.id.ukupanBrojOcena);

        buttonAddRating = findViewById(R.id.addRatingButton);
        buttonAddRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isGood = true;
                newRatingText = textForNewRating.getText().toString().trim();
                if(TextUtils.isEmpty(newRatingText)){
                    setRatingTextError("Dodajte opis ocene za ovaj oglas");
                }
                if(isGood){
                    startAddingRating();
                }
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
                        if(reasonForBadConnection == 1) {
                            if (tryToStart()) ShowEverything();
                        }else ShowEverything();
                    }
                }, 1000);
            }
        });

        sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMessaging();
            }
        });


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(pregledJednogOglasa.this));

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return isDeleteOpen;
            }
        });

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return isDeleteOpen;
            }
        });

        tryToStart();
    }

    public void onBackPressed(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregled_jednog_oglasa);

        setupView();
    }

    private void openAccount(String userid){
        Intent intent = new Intent(pregledJednogOglasa.this, Account.class);
        intent.putExtra("userid",userid);
        startActivity(intent);
    }

    private void startMessaging() {
        if(IsConnectedToInternet()) {
            FirebaseDatabase.getInstance().getReference("oglasi").child(IDOglasa).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    Oglas oglas = task.getResult().getValue(Oglas.class);

                    Intent i = new Intent(pregledJednogOglasa.this, MessageActivity.class);
                    i.putExtra("userid", oglas.getUserId());
                    i.putExtra("startedfrom","oglas");
                    startActivity(i);
                }
            });
        }else{
            HideWithReason(2);
        }

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