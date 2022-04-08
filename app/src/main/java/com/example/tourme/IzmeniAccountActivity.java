package com.example.tourme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tourme.Model.StaticVars;
import com.example.tourme.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class IzmeniAccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //View
    EditText imeText, prezimeText, opisText;
    Spinner spinnerDan, spinnerMesec, spinnerGodina;
    Button sacuvajPromene, odbaciPromene, izmeniSliku, izbrisiSliku, tryAgainButton;
    ImageView profileImage;
    Uri imageUri;
    View viewNoInternet, viewThis;
    ProgressBar progressBar;

    //Firebase
    FirebaseAuth fAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage storage;
    StorageReference storageReference;

    //Variables
    ArrayAdapter<CharSequence> adapterDan, adapterMesec, adapterGodina;
    String danString, mesecString, godinaString;
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
            fAuth = FirebaseAuth.getInstance();
            String userId = fAuth.getCurrentUser().getUid();

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();

            FirebaseDatabase.getInstance().getReference().child("users").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    spinnerDan.setSelection(adapterDan.getPosition(user.getDan()));
                    spinnerMesec.setSelection(adapterMesec.getPosition(user.getMesec()));
                    spinnerGodina.setSelection(adapterGodina.getPosition(user.getGodina()));
                    imeText.setText(user.getIme());
                    prezimeText.setText(user.getPrezime());
                    opisText.setText(user.getOpis());

                    if (user.getImageurl().equals("default"))
                        profileImage.setImageResource(R.drawable.default_image);
                    else
                        Glide.with(getApplicationContext()).load(user.getImageurl()).into(profileImage);
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
        setContentView(R.layout.activity_izmeni_account);

        profileImage = findViewById(R.id.profilna);

        imeText = findViewById(R.id.ime);
        prezimeText = findViewById(R.id.prezime);
        opisText = findViewById(R.id.opis);

        spinnerDan = findViewById(R.id.dan);
        adapterDan = ArrayAdapter.createFromResource(this, R.array.Dan, android.R.layout.simple_spinner_item);
        adapterDan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDan.setAdapter(adapterDan);
        spinnerDan.setOnItemSelectedListener(this);

        spinnerMesec = findViewById(R.id.mesec);
        adapterMesec = ArrayAdapter.createFromResource(this, R.array.Mesec, android.R.layout.simple_spinner_item);
        adapterMesec.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMesec.setAdapter(adapterMesec);
        spinnerMesec.setOnItemSelectedListener(this);

        spinnerGodina = findViewById(R.id.godina);
        adapterGodina = ArrayAdapter.createFromResource(this, R.array.Godina, android.R.layout.simple_spinner_item);
        adapterGodina.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGodina.setAdapter(adapterGodina);
        spinnerGodina.setOnItemSelectedListener(this);

        viewThis = findViewById(R.id.izmeniAccountActivity);
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

        izmeniSliku = findViewById(R.id.izmeni);
        izmeniSliku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsConnectedToInternet()) {
                    choseImage();
                }else{
                    HideWithReason(2);
                }
            }
        });

        izbrisiSliku = findViewById(R.id.izbrisi);
        izbrisiSliku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToInternet()){
                    String userId = fAuth.getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("imageurl").setValue("default");
                }else{
                    HideWithReason(2);
                }
            }
        });

        sacuvajPromene = findViewById(R.id.sacuvaj);
        sacuvajPromene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsConnectedToInternet()){
                    String d1 = danString;
                    String m1 = mesecString;
                    String g1 = godinaString;
                    String d2 = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
                    String m2 = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());
                    String g2 = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());

                    if(StaticVars.numberOfYears(d1, StaticVars.convertMonth(m1), g1, d2, m2, g2) >= 18) {
                        String userId = fAuth.getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("ime").setValue(imeText.getText().toString().trim());
                        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("prezime").setValue(prezimeText.getText().toString().trim());
                        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("opis").setValue(opisText.getText().toString().trim());

                        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("dan").setValue(danString);
                        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("mesec").setValue(mesecString);
                        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("godina").setValue(godinaString);

                        Toast.makeText(IzmeniAccountActivity.this, "Uspešno sačuvano", Toast.LENGTH_LONG).show();

                        Intent i = new Intent(IzmeniAccountActivity.this, MyAccount.class);
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Morate imati najmanje 18 godina", Toast.LENGTH_LONG).show();
                    }
                }else{
                    HideWithReason(2);
                }
            }
        });

        odbaciPromene = findViewById(R.id.odustani);
        odbaciPromene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tryToStart();

        //izmeniAccountActivity

    }

    private void choseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImage();
        }
    }

    private void uploadImage(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Učitavanje...");
        progressDialog.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("imageurl").setValue(imageUrl);
                            }
                        });
                    }
                }
                Snackbar.make(findViewById(android.R.id.content), "Image uploaded.", Snackbar.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Neuspešno", Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Percentage: " + (int)progressPercent + "%");
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String textFromSpinner = adapterView.getItemAtPosition(i).toString();
        switch (adapterView.getId()){
            case R.id.dan:
                danString = textFromSpinner;
                break;
            case R.id.mesec:
                mesecString = textFromSpinner;
                break;
            case R.id.godina:
                godinaString = textFromSpinner;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}