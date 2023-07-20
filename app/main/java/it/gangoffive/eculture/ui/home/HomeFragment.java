package it.gangoffive.eculture.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.login.LoginManager;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import it.gangoffive.eculture.ExportCsvActivity;
import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.SettingsActivity;
import it.gangoffive.eculture.WelcomeActivity;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.databinding.FragmentHomeBinding;
import it.gangoffive.eculture.model.PlaceModel;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.model.StructureModel;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.add.AddPlaceActivity;
import it.gangoffive.eculture.ui.add.AddRoomActivity;
import it.gangoffive.eculture.ui.add.AddStructureActivity;
import it.gangoffive.eculture.ui.places.adapters.PlacesIconRecyclerViewAdapter;
import it.gangoffive.eculture.ui.rooms.adapters.RoomsIconRecyclerViewAdapter;
import it.gangoffive.eculture.ui.structures.adapters.StructuresIconRecyclerViewAdapter;
import it.gangoffive.eculture.ui.tours.adapters.ToursIconRecyclerViewAdapter;
import it.gangoffive.eculture.viewmodel.PlaceViewModel;
import it.gangoffive.eculture.viewmodel.RoomViewModel;
import it.gangoffive.eculture.viewmodel.StructureViewModel;
import it.gangoffive.eculture.viewmodel.TourViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private String userRole;
    private String userUid;

    private PlaceViewModel placesViewModel;
    private TourViewModel toursViewModel;
    private StructureViewModel strucuresViewModel;
    private RoomViewModel roomViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        placesViewModel = new ViewModelProvider(this).get(PlaceViewModel.class);
        toursViewModel = new ViewModelProvider(this).get(TourViewModel.class);
        strucuresViewModel = new ViewModelProvider(this).get(StructureViewModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);


        View root = binding.getRoot();


        final com.getbase.floatingactionbutton.FloatingActionsMenu mainFab = binding.homeFab;

        userRole = ((MainActivity) getActivity()).getUserRole();
        userUid = ((MainActivity) getActivity()).getUserUid();
        //Identificazione ruolo utente. Se è turista non viene instanziato il fab e tutti i listener. Vengono impostate le stringhe consone per il turista.
        if (userRole.equals("tourist")) {
            mainFab.setVisibility(View.GONE);
            TextView tvStrucures = binding.tvStrucutres;
            TextView tvRooms = binding.tvRooms;
            TextView tvPlaces = binding.tvPlaces;

            tvStrucures.setText(getString(R.string.home_tourist_strucures));
            tvRooms.setText(getString(R.string.home_tourist_rooms));
            tvPlaces.setText(getString(R.string.home_tourist_poi));

        } else {
            initiateHomeFAB(mainFab);
        }


        //Messaggio di benvenuto
        TextView welcomeMessage = root.findViewById(R.id.welcome_message);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("user").child(userUid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                HashMap<String, String> res = (HashMap<String, String>) task.getResult().getValue();
                String userName = res != null ? res.get("name") : "";
                welcomeMessage.setText(getString(R.string.welcome_home) + " " + userName);
            }
        });


        //Recycler View Orizzontale per le Strutture sulla homefragment
        RecyclerView recyclerViewStrucutre = root.findViewById(R.id.recyclerViewStructure);
        recyclerViewStrucutre.setNestedScrollingEnabled(false);
        recyclerViewStrucutre.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        strucuresViewModel.getStructures("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<StructureModel>>() {
            @Override
            public void onChanged(List<StructureModel> structureModels) {
                if (structureModels.size() == 0) {
                        structureModels.add(new StructureModel(null,"","","","","","",""));
                    }
                    StructuresIconRecyclerViewAdapter adapterStructure = new StructuresIconRecyclerViewAdapter(structureModels);
                    recyclerViewStrucutre.setAdapter(adapterStructure);
                }
            });
        strucuresViewModel.loadStructures("",userRole,userUid);


        //Recycler View Orizzontale per i Punti d'interesse sulla homefragment
        RecyclerView recyclerViewPlace = root.findViewById(R.id.recyclerViewPlace);
        recyclerViewPlace.setNestedScrollingEnabled(false);
        recyclerViewPlace.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        placesViewModel.getPlaces("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<PlaceModel>>() {
            @Override
            public void onChanged(@Nullable List<PlaceModel> list) {
                if (list.size() == 0){
                    list.add(new PlaceModel(null,"","", "","","","",false, ""));
                }
                PlacesIconRecyclerViewAdapter adapterPlace = new PlacesIconRecyclerViewAdapter(list);
                recyclerViewPlace.setAdapter(adapterPlace);
            }
        });
        placesViewModel.loadPlaces("", userRole, userUid);


        //Recycler View Orizzontale per i Itinerari sulla homefragment
        RecyclerView recyclerViewTour = root.findViewById(R.id.recyclerViewTour);
        recyclerViewTour.setNestedScrollingEnabled(false);
        recyclerViewTour.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        toursViewModel.getTours("", "curator", userUid).observe(getViewLifecycleOwner(), new Observer<List<TourModel>>() {
            @Override
            public void onChanged(@Nullable List<TourModel> list) {
                if (list.size() == 0){
                    list.add(new TourModel());
                }
                ToursIconRecyclerViewAdapter adapterTour = new ToursIconRecyclerViewAdapter(list);
                recyclerViewTour.setAdapter(adapterTour);
            }
        });
        toursViewModel.loadTours("", "curator", userUid);


        //Recycler View Orizzontale per le stanze sulla homefragment
        RecyclerView recyclerViewRoom = root.findViewById(R.id.recyclerViewRoom);
        recyclerViewRoom.setNestedScrollingEnabled(false);
        recyclerViewRoom.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        roomViewModel.getRooms("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<RoomModel>>() {
            @Override
            public void onChanged(@Nullable List<RoomModel> list) {
                if (list.size() == 0){
                    list.add(new RoomModel(null, "", "", "", false, "", ""));
                }
                RoomsIconRecyclerViewAdapter adapterRoom = new RoomsIconRecyclerViewAdapter(list);
                recyclerViewRoom.setAdapter(adapterRoom);
            }
        });

        roomViewModel.loadRooms("", userRole, userUid);


        return root;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        checkForInternetConnection();


    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.toolbar_home_menu, menu);
        int bg = (userRole != null && userRole.equals("curator")) ? R.drawable.curator_bg : R.drawable.tourist_bg;
        ((MainActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), bg));
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.title_home)+"</font>", Html.FROM_HTML_MODE_COMPACT));
    }

    public void error() {
        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;

            case R.id.export:
                startActivity(new Intent(getActivity(), ExportCsvActivity.class));
                return true;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(getActivity(), WelcomeActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Implementa i listener per i pulsanti del floating action button nella home del curatore.
     * La libreria utilizzata per l'implementazione di questa feature fa differenza fra FAB base e Floating action menu.
     * Floating action menù è quello con dimensioni maggiori, contiene l'icona del "+" e al suo click vengono mostrati gli altri fab base attraverso un'animazione.
     * Utilizzando i metodi di callback (sul floating action menu) della libreria, il metodo applica un effetto di blur attraverso la modifica
     * del parametro alpha.
     *
     * @param mainFab FloatingActionsMenu
     */
    private void initiateHomeFAB(com.getbase.floatingactionbutton.FloatingActionsMenu mainFab){
        //Listener per creazioni strutture
        final com.getbase.floatingactionbutton.FloatingActionButton buttonActivityStructure = binding.addStructure;
        buttonActivityStructure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddStructureActivity.class);
                startActivity(intent);
            }
        });

        //Listener per creazioni punti d'interesse
        final com.getbase.floatingactionbutton.FloatingActionButton buttonActivityPlaces = binding.addPointOfInterest;
        buttonActivityPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddPlaceActivity.class);
                startActivity(intent);
            }
        });

        //Listener per creazioni  percorsi
        final com.getbase.floatingactionbutton.FloatingActionButton buttonActivityTours = binding.addTour;
        buttonActivityTours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WizardActivity.class);
                startActivity(intent);
            }
        });

        //Listener per creazioni delle stanze
        final com.getbase.floatingactionbutton.FloatingActionButton  buttonActivityRoom = binding.addRoom;
        buttonActivityRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddRoomActivity.class);
                startActivity(intent);
            }
        });

        //Listener per il FAB menu
        mainFab.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                View relativeHome = getActivity().findViewById(R.id.layer1home);
                View strutture = getActivity().findViewById(R.id.recyclerStrutture);
                View tours = getActivity().findViewById(R.id.recyclerPercorsi);
                View rooms = getActivity().findViewById(R.id.recyclerTappe);
                View puntidinteresse = getActivity().findViewById(R.id.recyclerPuntidinteresse);
                relativeHome.setAlpha((float) 0.4);
                strutture.setAlpha((float) 0.4);
                tours.setAlpha((float) 0.4);
                rooms.setAlpha((float) 0.4);
                puntidinteresse.setAlpha((float) 0.4);
            }

            @Override
            public void onMenuCollapsed() {
                View relativeHome = getActivity().findViewById(R.id.layer1home);
                View strutture = getActivity().findViewById(R.id.recyclerStrutture);
                View tours = getActivity().findViewById(R.id.recyclerPercorsi);
                View rooms = getActivity().findViewById(R.id.recyclerTappe);
                View puntidinteresse = getActivity().findViewById(R.id.recyclerPuntidinteresse);
                relativeHome.setAlpha((float) 1);
                strutture.setAlpha((float) 1);
                tours.setAlpha((float) 1);
                rooms.setAlpha((float) 1);
                puntidinteresse.setAlpha((float) 1);

            }
        });
    }


    /**
     * Controllo per connessione internet. Nel caso fosse assente, viene mostrato a schermo un box informativo
     */
     private void checkForInternetConnection(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
         com.google.android.material.card.MaterialCardView alertBox = getActivity().findViewById(R.id.alertBoxHomeFrag);
        if(cm.getActiveNetworkInfo() == null){
            alertBox.setVisibility(View.VISIBLE);
        }
        else {
            alertBox.setVisibility(View.GONE);
        }

    }


}