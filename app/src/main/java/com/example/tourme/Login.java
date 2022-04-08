package com.example.tourme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tourme.Model.StaticVars;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    //View
    EditText mEmail, mPassword;
    Button login_dugme, buttonShowHidePassword, tryAgainButton;
    TextView registerButton, forgotPasswordButton;
    View viewNoInternet, viewThis;
    ProgressBar progressBar;

    //Firebase
    FirebaseAuth fAuth;
    private DatabaseReference mDatabase;

    //Variables
    Handler h = new Handler();
    int reasonForBadConnection = 1;
    boolean didFindError = false, isPasswordHidden = true;
    String patternForEmail = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    void setEmailError(String errorText){
        mEmail.setError(errorText);
        didFindError = true;
    }

    void setPasswordError(String errorText){
        mPassword.setError(errorText);
        didFindError = true;
    }

    void resetErrors(){
        mEmail.setError(null);
        mPassword.setError(null);
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

    void HideWithReason(int reason){
        HideEverything();
        reasonForBadConnection = reason;
    }

    private void ShowEverythingRecursion(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i = 0 ; i < viewgroup.getChildCount(); i++) {
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

    void finishSigningIn(String email, String password){
        if(IsConnectedToInternet()){
            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Login.this,"Uspešna prijava",Toast.LENGTH_LONG).show();
                        finish();
                    } else{
                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                        Log.e("gf", errorCode);
                        if(errorCode.equals("ERROR_USER_NOT_FOUND")){
                            setEmailError("Ne postoji nalog sa ovim email-om");
                        }else if(errorCode.equals("ERROR_WRONG_PASSWORD")){
                            setPasswordError("Pogresna sifra");
                        }else{
                            HideWithReason(2);
                        }
                    }
                }
            });
        }else{
            HideWithReason(2);
        }
    }

    void continueSignIn(String id, String password){
        if(IsConnectedToInternet()){
            mDatabase.child("users").child(id).child("email").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        HideWithReason(2);
                    } else {
                        String emailFromDatabase = String.valueOf(task.getResult().getValue());
                        if(!emailFromDatabase.equals("null")){
                            finishSigningIn(emailFromDatabase, password);
                        }else{
                            setEmailError("Ne postoji nalog sa ovim ID-em");
                        }
                    }
                }
            });
        }else{
            HideWithReason(2);
        }
    }

    void startSigningIn(String email, String password){
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(patternForEmail);
        java.util.regex.Matcher m = p.matcher(email);


        if(!m.matches()) {
            if(IsConnectedToInternet()){
                //nije email nego je username
                String username = email;
                mDatabase.child("usersID").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful())
                            HideWithReason(2);
                        else {
                            String IDFromDatabase = String.valueOf(task.getResult().getValue());
                            if(!IDFromDatabase.equals("null"))
                                continueSignIn(IDFromDatabase, password);
                            else
                                setEmailError("Ne postoji ovakvo korisnicko ime");
                        }
                    }
                });
            }else {
                HideWithReason(2);
            }
        }else {
            finishSigningIn(email, password);
        }
    }

    public void setupFireBase(){
        fAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public boolean tryToStart(){
        if(IsConnectedToInternet()){
            setupFireBase();
        }else{
            HideWithReason(1);
            return false;
        }
        return true;
    }

    public void setupView(){
        StaticVars.listOfFragments.add(7);

        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.password_login);


        viewThis = findViewById(R.id.LoginActivity);
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


        registerButton = (TextView)findViewById(R.id.goToRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Login.this, Register.class);
                startActivity(i);
            }
        });

        forgotPasswordButton = (TextView) findViewById(R.id.forgotPasswordText);
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, ForgotPassword.class);
                startActivity(i);
            }
        });

        buttonShowHidePassword = findViewById(R.id.buttonForShowingPasswordLogin);
        buttonShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPasswordHidden){
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    buttonShowHidePassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_eye_crossed);
                }else{
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    buttonShowHidePassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_eye);
                }
                isPasswordHidden = !isPasswordHidden;
                mPassword.setSelection(mPassword.length());
            }
        });

        login_dugme = findViewById(R.id.login_dugme);
        login_dugme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didFindError = false;
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                resetErrors();

                if(TextUtils.isEmpty(email)){
                    setEmailError("Unesite email");
                }

                if(TextUtils.isEmpty(password)){
                    setPasswordError("Unesite sifru");
                }
                if(!didFindError){
                    startSigningIn(email, password);
                }

            }
        });

        tryToStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupView();
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