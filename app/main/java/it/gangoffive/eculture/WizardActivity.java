package it.gangoffive.eculture;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stepstone.stepper.StepperLayout;

import java.util.ArrayList;
import java.util.HashMap;

import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.wizard.adapters.WizardAdapter;

public class WizardActivity extends AppCompatActivity {

    private StepperLayout mStepperLayout;
    private String userRole;
    private String userUid;
    private TourModel tour;
    private String mode = "WRITE";

    private HashMap<String, String> selectedRooms = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tour = new TourModel();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(WizardActivity.this, WelcomeActivity.class));
        } else {
            userUid = currentUser.getUid();

            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userRole = sp.getString("type", null);

            Bundle data = getIntent().getExtras();
            if (data != null){
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                String tourID = data.getString("ID");
                mode = "EDIT";
                mDatabase.child("tours").child(tourID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<DataSnapshot> task) {
                       HashMap<String, String> res = (HashMap<String, String>) task.getResult().getValue();
                       tour = new TourModel(
                               res.get("id"),
                               res.get("title"),
                               res.get("subtitle"),
                               res.get("description"),
                               res.get("structure"),
                               ( (HashMap<String, ArrayList>) task.getResult().getValue() ).get("rooms"),
                               ( (HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>>) task.getResult().getValue() ).get("places"),
                               res.get("createdBy")
                       );
                       setContentView(R.layout.activity_wizard);
                       mStepperLayout = findViewById(R.id.stepperLayout);
                       mStepperLayout.setAdapter(new WizardAdapter(getSupportFragmentManager(), WizardActivity.this));
                   }
               });
            } else {
                setContentView(R.layout.activity_wizard);
                mStepperLayout = findViewById(R.id.stepperLayout);
                mStepperLayout.setAdapter(new WizardAdapter(getSupportFragmentManager(), WizardActivity.this));
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_login_24);
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.wiz_tab_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
            getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.wiz_structures_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(userRole.equals("curator") ? R.color.curatorcolor : R.color.touristcolor)));

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                        .setTitle(R.string.wiz_alert_title)
                        .setMessage(getString(R.string.wiz_alert_msg))
                        .setNegativeButton(R.string.wiz_alert_decline, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}
                        })
                        .setPositiveButton(R.string.wiz_alert_accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(WizardActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getUserRole(){
        return this.userRole;
    }

    public String getUserUid(){
        return this.userUid;
    }

    public TourModel getTour() {
        return this.tour;
    }

    public String getMode() {
        return this.mode;
    }

    public HashMap<String, String> getSelectedRooms(){
        return this.selectedRooms;
    }

    public void setSelectedRooms(String roomID, String roomTitle){
        this.selectedRooms.put(roomID, roomTitle);
    }
}