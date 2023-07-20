package it.gangoffive.eculture.ui.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import it.gangoffive.eculture.DetailsRoomActivity;
import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.RoomModel;

public class AddRoomActivity extends AppCompatActivity {

    private static DatabaseReference mRef = null;

    private static int maxRooms;
    private boolean create = false;

    private static String structure = "";
    TextInputEditText roomET;
    TextInputEditText descriptiont;
    TextInputEditText visitingt;
    SwitchMaterial swMaterial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        roomET = findViewById(R.id.room);
        descriptiont = findViewById(R.id.description);
        visitingt = findViewById(R.id.period);
        swMaterial = findViewById(R.id.open);

        Activity a = this;
        structure = getIntent().getStringExtra("structure");
        CommonAdd.initStructures(a);
        CommonAdd.imageListener(a);
        mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 1; i < Integer.MAX_VALUE; i++) {
                    if (!snapshot.hasChild(String.valueOf(i))) {
                        maxRooms = i;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button add = findViewById(R.id.add);
        String id = getIntent().getStringExtra("ID");
        if (id != null) {
            getSupportActionBar().setTitle(R.string.edit_room);
            FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                    descriptiont.setText(hm.get("description"));
                    roomET.setText(hm.get("name"));
                    visitingt.setText(hm.get("visiting_time"));
                    swMaterial.setChecked(String.valueOf(hm.get("isOpen")).equalsIgnoreCase("true"));
                    FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures").child(hm.get("structure_id")).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                            CommonAdd.getStructuresA().setText(hm.get("name"), false);
                            CommonAdd.loadImage(a, "rooms", id);
                            ((Button) findViewById(R.id.add)).setText(R.string.save);
                        }
                    });

                }
            });

        }else{
            getSupportActionBar().setTitle(R.string.add_room);
        }
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean one_at_least;
                one_at_least = CommonAdd.checkFields(a);
                if (!one_at_least) {
                    if (TextUtils.isDigitsOnly(((TextInputEditText) findViewById(R.id.period)).getText().toString())) {
                        if (id == null) {
                            create = true;
                            save(String.valueOf(maxRooms));
                        } else {
                            save(id);
                        }
                    } else {
                        CommonAdd.alertAdd(a, R.string.caution, R.string.only_numbers, 0);
                    }
                } else {
                    ((ScrollView) findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_UP);
                    ((ScrollView) findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_UP);
                }
            }
        });
        if (structure != null) {
            CommonAdd.getStructuresA().setText(structure, false);
        } else {
            structure = "";
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CommonAdd.activityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (structure.equalsIgnoreCase("")) {
            super.onBackPressed();
        } else {
            CommonAdd.alertAdd(this, R.string.sure, R.string.need_room, 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (structure.equalsIgnoreCase("")) {
                    super.onBackPressed();
                } else {
                    CommonAdd.alertAdd(this, R.string.sure, R.string.need_room, 1);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Utilizzato per salvare tutti i dati relativi alla stanza (informazioni e immagine)
     *
     * @param id della stanza che si va ad aggiungere/modificare
     */
    public void save(String id) {
        String room_name = roomET.getText().toString();
        String description = descriptiont.getText().toString();
        String visiting = visitingt.getText().toString();
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms");
            for (int i = 0; i < CommonAdd.getStructures().size(); i++) {
                if (CommonAdd.getStructuresA().getText().toString().equalsIgnoreCase(CommonAdd.getStructures().get(i).get("name"))) {
                    mRef.child(String.valueOf(id)).setValue(new RoomModel(String.valueOf(id), room_name, description, CommonAdd.getStructures().get(i).get("id"), swMaterial.isChecked(), user.getUid(), visiting));
                    if (CommonAdd.getIm() != null) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference refe;
                        if (!TextUtils.isEmpty(CommonAdd.getFilename())) {
                            refe = storageRef.child("rooms/" + CommonAdd.getFilename());
                            refe.delete();
                            CommonAdd.setFilename("");
                        }
                        refe = storageRef.child("rooms/" + (id) + "." + CommonAdd.getExtension());
                        CommonAdd.imageClick(refe);
                    }
                }
            }
            finish();
            showFeedback();
            structure="";
            onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFeedback(){
        Toast feedback;
        if(create){
            feedback = Toast.makeText(this, getString(R.string.toast_add), Toast.LENGTH_LONG);
            feedback.show();
        } else {
            feedback = Toast.makeText(this, getString(R.string.toast_edit), Toast.LENGTH_LONG);
            feedback.show();
        }
    }

}
