package it.gangoffive.eculture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.switchmaterial.SwitchMaterial;
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
import java.util.stream.Collectors;

import it.gangoffive.eculture.graph.GraphAdapter;
import it.gangoffive.eculture.model.GraphModel;
import it.gangoffive.eculture.model.PlaceModel;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.ui.add.AddRoomActivity;
import it.gangoffive.eculture.viewmodel.PlaceViewModel;
import it.gangoffive.eculture.viewmodel.RoomViewModel;

public class DetailsRoomActivity extends AppCompatActivity {

    private RoomModel room;
    private String roomID;
    private RoomViewModel roomViewModel;
    private PlaceViewModel placeViewModel;
    private RecyclerView mRecyclerView;
    private GraphAdapter mGraphAdapter;
    private List<String> places;
    private final List<GraphModel> mPlaceList = new ArrayList<>();
    private String userRole;
    private String userUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        roomID = String.valueOf(getIntent().getExtras().getString("ID"));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userUid = currentUser.getUid();
            userRole = sp.getString("type", "tourist");

            if (roomID == null){
                super.onBackPressed();
            } else {
                setContentView(R.layout.activity_detail_room);

                mRecyclerView = findViewById(R.id.recyclerView);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false));

                try {
                    places = getIntent().getExtras().getStringArrayList("places");
                } catch (Exception e) {
                }
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
        if (userRole.equals("curator")){
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
                Intent intent = new Intent(this, AddRoomActivity.class);
                intent.putExtra("ID", room.getId());
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
                                DatabaseReference roomRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms");
                                roomRef.child(room.getId()).setValue(null);
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
                                                    if (place.get("roomID").equalsIgnoreCase(room.getId())) {
                                                        DatabaseReference placeRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places");
                                                        placeRef.child(place.get("id")).setValue(null);
                                                        deleteImage(place.get("id"), "places");

                                                    }
                                            }
                                        }
                                    }
                                });
                                deleteImage(room.getId(), "rooms");
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
    private void setDataToView(){

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        roomViewModel.getRooms("", userRole, userUid).observe(this, new Observer<List<RoomModel>>() {
            @Override
            public void onChanged(@Nullable List<RoomModel> list) {
                for (int i = 0; i < list.size(); i++){
                    if (list.get(i).getId().equals(roomID)) {
                        room = list.get(i);

                        setCoverImage();
                        TextView title = findViewById(R.id.room_detail_title);
                        TextView description = findViewById(R.id.room_detail_description);
                        TextView visiting_time = findViewById(R.id.visiting_time);
                        visiting_time.setText(getResources().getString(R.string.visit_time) + " : " + room.getVisiting_time() + " min.");
                        SwitchMaterial availability = findViewById(R.id.room_detail_title);
                        title.setText(room.getName());
                        description.setText(room.getDescription());
                        availability.setChecked(room.getIsOpen());
                        availability.setClickable(false);
                        getSupportActionBar().setTitle(room.getName());

                        placeViewModel = new ViewModelProvider(DetailsRoomActivity.this).get(PlaceViewModel.class);
                        placeViewModel.getPlaces("", userRole, userUid).observe(DetailsRoomActivity.this, new Observer<List<PlaceModel>>() {
                            @Override
                            public void onChanged(@Nullable List<PlaceModel> list) {
                                mPlaceList.clear();
                                for (int i = 0; i < list.size(); i++) {
                                    if (room.getId().equals(list.get(i).getRoomID())) {
                                        mPlaceList.add(new GraphModel(
                                                "places",
                                                list.get(i).getId(),
                                                list.get(i).getTitle(),
                                                null,
                                                2)
                                        );
                                    }
                                }
                                if (mPlaceList.size() == 0) {
                                    findViewById(R.id.room_detail_graph).setVisibility(View.GONE);
                                    mRecyclerView.setVisibility(View.GONE);
                                } else {
                                    if (places == null) {
                                        mGraphAdapter = new GraphAdapter(mPlaceList);
                                        TextView listPlacesTitle = findViewById(R.id.room_detail_graph);
                                        listPlacesTitle.setText(getString(R.string.list_places_all));
                                    } else {
                                        mGraphAdapter = new GraphAdapter(mPlaceList.stream().filter(place -> places.contains(place.getMessage())).collect(Collectors.toList()));
                                    }
                                    mRecyclerView.setAdapter(mGraphAdapter);
                                }
                            }
                        });
                        placeViewModel.loadPlaces("", userRole, userUid);
                        break;
                    }
                }
            }
        });
        roomViewModel.loadRooms("", "", "");
    }


    private void setCoverImage() {
        ImageView iv = findViewById(R.id.room_detail_image);
        iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.no_image));
        StorageReference storagePlaceRef = FirebaseStorage.getInstance().getReference().child("rooms");
        storagePlaceRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] name = item.getName().split("\\.");
                    if (name[0].equalsIgnoreCase(room.getId())) {
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