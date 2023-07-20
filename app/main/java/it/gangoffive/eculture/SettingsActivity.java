package it.gangoffive.eculture;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {
    private boolean isOpen = false;
    private String name;
    private String surname;
    private String userName;
    private String userUid;
    private FirebaseAuth mAuth;

    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userRole = sp.getString("type", null);
            setContentView(R.layout.activity_settings);

            //Instanzio appbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.settings)+"</font>", Html.FROM_HTML_MODE_COMPACT));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(userRole.equals("curator") ? R.color.curatorcolor : R.color.touristcolor)));

        }

        //Prendo i dati relativi all'utente loggato
        userUid = currentUser.getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user").child(userUid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                HashMap<String, String> res = (HashMap<String, String>) task.getResult().getValue();
                name = res.get("name");
                surname = res.get("surname");

                TextView profileName = findViewById(R.id.profile_name);
                userName = name + " " + surname;
                profileName.setText(userName);
            }
        });

        //Listener per la visualizzazione delle categoria di impostazioni cliccata
        LinearLayout advancedSettings = findViewById(R.id.advancedSettingsBox);
        advancedSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent advancedSettings = new Intent(SettingsActivity.this, AdvancedSettingsFragment.class);
                View fragmentView = findViewById(R.id.fragment_container_view);
                if(isOpen == false){
                    fragmentView.setVisibility(View.VISIBLE);
                    isOpen = true;
                }else{
                    fragmentView.setVisibility(View.GONE);
                    isOpen = false;
                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onClick(View view) {

    }
}