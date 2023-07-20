package it.gangoffive.eculture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.gangoffive.eculture.graph.GraphAdapter;
import it.gangoffive.eculture.model.GraphModel;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.model.StructureModel;
import it.gangoffive.eculture.ui.add.AddStructureActivity;
import it.gangoffive.eculture.viewmodel.RoomViewModel;
import it.gangoffive.eculture.viewmodel.StructureViewModel;

public class DetailsStructureActivity extends AppCompatActivity {

    private StructureModel structure;
    private String structureID;
    private StructureViewModel structureViewModel;
    private RoomViewModel roomViewModel;
    private RecyclerView mRecyclerView;
    private GraphAdapter mGraphAdapter;
    private final List<GraphModel> mRoomList = new ArrayList<>();
    private String userRole;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        structureID = getIntent().getExtras().getString("ID");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userUid = currentUser.getUid();
            userRole = sp.getString("type", "tourist");

            if (structureID == null) {
                super.onBackPressed();
            } else {
                setContentView(R.layout.activity_detail_structure);

                mRecyclerView = findViewById(R.id.recyclerView);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false));

                setDataToView();

                Toolbar myToolbar = findViewById(R.id.topAppBar);
                myToolbar.setBackgroundColor(getResources().getColor(userRole.equals("curator") ? R.color.curatorcolor : R.color.touristcolor));
                setSupportActionBar(myToolbar);

            }
        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userRole.equals("curator")) {
            super.onCreateOptionsMenu(menu);
            if (menu instanceof MenuBuilder) {
                ((MenuBuilder) menu).setOptionalIconsVisible(true);
            }
            getMenuInflater().inflate(R.menu.toolbar_detail_menu, menu);
        }
        return true;
    }


    /**
     * Gestione della modifica e eliminazione dell'elemento selezionato.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Intent intent = new Intent(this, AddStructureActivity.class);
                intent.putExtra("ID", structure.getId());
                startActivity(intent);
                return true;
            case R.id.delete:
                AlertDialog myDialog = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                        .setTitle(R.string.delete_dialog_title)
                        .setMessage(getString(R.string.delete_dialog_msg))
                        .setIcon(R.drawable.ic_baseline_error_outline_24)
                        .setNegativeButton(R.string.delete_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setPositiveButton(R.string.delete_dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference structureRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures");
                                structureRef.child(structure.getId()).setValue(null);
                                DatabaseReference roomsRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms");
                                roomsRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        } else {
                                            ArrayList<HashMap<String, String>> al = (ArrayList) task.getResult().getValue();
                                            for (HashMap<String, String> hm : al) {
                                                if (hm != null)
                                                    if (hm.get("structure_id").equalsIgnoreCase(structure.getId())) {
                                                        DatabaseReference roomRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms");
                                                        roomRef.child(hm.get("id")).setValue(null);
                                                        deleteImage(hm.get("id"), "rooms");
                                                        DatabaseReference placesRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places");
                                                        placesRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                if (!task.isSuccessful()) {
                                                                    Log.e("firebase", "Error getting data", task.getException());
                                                                } else {
                                                                    ArrayList<HashMap<String, String>> al = (ArrayList) task.getResult().getValue();
                                                                    for (HashMap<String, String> place : al) {
                                                                        if (place != null)
                                                                            if (place.get("roomID").equalsIgnoreCase(hm.get("id"))) {
                                                                                DatabaseReference placeRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places");
                                                                                placeRef.child(place.get("id")).setValue(null);
                                                                                deleteImage(place.get("id"), "places");
                                                                            }
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                            }
                                        }
                                    }
                                });
                                deleteImage(structure.getId(), "structures");
                                showFeedback();
                                onBackPressed();
                            }
                        })
                        .show();
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metodo che si occupa del recupero dei dati dal DB, relativi all'elemnto selezionato.
     *
     */
    private void setDataToView() {

        structureViewModel = new ViewModelProvider(this).get(StructureViewModel.class);
        structureViewModel.getStructures("", "", "").observe(this, new Observer<List<StructureModel>>() {
            @Override
            public void onChanged(@Nullable List<StructureModel> list) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equals(structureID)) {
                        structure = list.get(i);

                        setCoverImage();
                        TextView title = findViewById(R.id.structure_detail_title);
                        TextView address = findViewById(R.id.structure_detail_address);
                        TextView schedule = findViewById(R.id.structure_detail_time);
                        schedule.setText(getResources().getString(R.string.schedule) + ": " + structure.getSchedule());
                        title.setText(structure.getName());
                        title.setTextColor(Color.parseColor("#000000"));
                        address.setText(structure.getAddress() + " - " + structure.getCity() + " (" + structure.getProvince() + ") " + structure.getRegion().toUpperCase());
                        getSupportActionBar().setTitle(structure.getName());

                        roomViewModel = new ViewModelProvider(DetailsStructureActivity.this).get(RoomViewModel.class);
                        roomViewModel.getRooms("", userRole, userUid).observe(DetailsStructureActivity.this, new Observer<List<RoomModel>>() {
                            @Override
                            public void onChanged(@Nullable List<RoomModel> list) {
                                mRoomList.clear();
                                for (int i = 0; i < list.size(); i++) {
                                    if (structure.getId().equals(list.get(i).getStructure_id())) {
                                        mRoomList.add(new GraphModel(
                                                "rooms",
                                                list.get(i).getId(),
                                                list.get(i).getName(),
                                                null,
                                                2)
                                        );
                                    }
                                }
                                mGraphAdapter = new GraphAdapter(mRoomList);
                                mRecyclerView.setAdapter(mGraphAdapter);
                            }
                        });
                        roomViewModel.loadRooms("", userRole, userUid);
                        break;
                    }
                }
            }
        });
        structureViewModel.loadStructures("", "", "");
    }


    private void setCoverImage() {
        ImageView iv = findViewById(R.id.structure_detail_image);
        iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.no_image));
        StorageReference storagePlaceRef = FirebaseStorage.getInstance().getReference().child("structures");
        storagePlaceRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] name = item.getName().split("\\.");
                    if (name[0].equalsIgnoreCase(structure.getId())) {
                        final long TEN_MEGABYTE = 1024 * 1024 * 10;
                        item.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                iv.setImageBitmap(bmp);
                            }
                        });
                    }
                }
            }
        });
    }

    public void deleteImage(String id, String type) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(type);
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] name = item.getName().split("\\.");
                    if (name[0].equalsIgnoreCase(id)) {
                        StorageReference ref = FirebaseStorage.getInstance().getReference();
                        ref.child(type).child(item.getName()).delete();
                        break;
                    }
                }
            }
        });
    }


    private void showFeedback(){
        Toast feedback = Toast.makeText(this, getString(R.string.toast_delete), Toast.LENGTH_LONG);
        feedback.show();
    }

}