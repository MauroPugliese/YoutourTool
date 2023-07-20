package it.gangoffive.eculture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import it.gangoffive.eculture.graph.GraphAdapter;
import it.gangoffive.eculture.model.GraphModel;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.viewmodel.RoomViewModel;
import it.gangoffive.eculture.viewmodel.TourViewModel;

public class DetailsTourActivity extends AppCompatActivity {

    private List<RoomModel> mRoomList = new ArrayList<>();
    private List<GraphModel> mRoomGraph = new ArrayList<>();
    private TourModel tour;
    private RoomViewModel roomViewModel;
    private RecyclerView mRecyclerView;
    private GraphAdapter mGraphAdapter;
    private String userRole;
    private String userUid;
    private String tourID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tourID = getIntent().getExtras().getString("ID");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userUid = currentUser.getUid();
            userRole = sp.getString("type", "tourist");

            if (tourID == null) {
                super.onBackPressed();
            } else {
                setContentView(R.layout.activity_detail_tour);

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
        if (tour != null && userUid.equals(tour.getCreatedBy())) {
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

                Intent intent = new Intent(this, WizardActivity.class);
                intent.putExtra("ID", tour.getId());
                startActivity(intent);

                return true;
            case R.id.delete:
                new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
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
                                DatabaseReference toursRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("tours");
                                toursRef.child(tour.getId()).setValue(null);
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
        TourViewModel tourViewModel = new ViewModelProvider(this).get(TourViewModel.class);
        tourViewModel.getTours("", "", "").observe(this, new Observer<List<TourModel>>() {
            @Override
            public void onChanged(@Nullable List<TourModel> list) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equals(tourID)) {
                        tour = list.get(i);

                        TextView title = findViewById(R.id.tour_detail_title);
                        TextView description = findViewById(R.id.tour_detail_description);
                        title.setText(tour.getTitle());
                        description.setText(tour.getDescription());
                        getSupportActionBar().setTitle(tour.getTitle());

                        roomViewModel = new ViewModelProvider(DetailsTourActivity.this).get(RoomViewModel.class);
                        roomViewModel.getRooms("", userRole, userUid).observe(DetailsTourActivity.this, new Observer<List<RoomModel>>() {
                            @Override
                            public void onChanged(@Nullable List<RoomModel> list) {
                                mRoomList.clear();
                                for (int i = 0; i < list.size(); i++) {
                                    if (tour.getRooms().contains(list.get(i).getId())) {
                                        mRoomList.add(list.get(i));
                                    }
                                }

                                mRoomGraph.clear();
                                ArrayList<String> roomIds = new ArrayList<>();
                                for (int i = 0; i < tour.getPlaces().size(); i++){

                                    for (int j = 0; j < mRoomList.size(); j++){
                                        if (mRoomList.get(j).getId().equals(tour.getPlaces().get(i).get("roomid").get(0))){

                                            mRoomGraph.add(new GraphModel(
                                                    "rooms",
                                                    mRoomList.get(j).getId(),
                                                    mRoomList.get(j).getName(),
                                                    tour.getPlaces().get(i).get("places"),
                                                    (roomIds.contains(mRoomList.get(j).getId()) || tour.getPlaces().get(i).get("places").get(0).equals("")) ? 1 : 2)
                                            );
                                        }
                                    }
                                    roomIds.add(tour.getPlaces().get(i).get("roomid").get(0));
                                }
                                mGraphAdapter = new GraphAdapter(mRoomGraph);
                                mRecyclerView.setAdapter(mGraphAdapter);


                            }
                        });
                        roomViewModel.loadRooms("", userRole, userUid);
                        break;
                    }
                }
            }
        });
        tourViewModel.loadTours("", "", "");
    }


}
