package it.gangoffive.eculture.viewmodel;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import it.gangoffive.eculture.model.PlaceModel;

public class PlaceViewModel extends ViewModel {

    private MutableLiveData<List<PlaceModel>> places;

    private final List<PlaceModel> result = new ArrayList<>();
    private final DatabaseReference placesRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places");

    public PlaceViewModel() {
        places = new MutableLiveData<List<PlaceModel>>();
    }

    public LiveData<List<PlaceModel>> getPlaces(CharSequence term, String role, String uid) {
        if (places == null) {
            places = new MutableLiveData<List<PlaceModel>>();
            loadPlaces(term, role, uid);
        }
        return places;
    }


    /**
     *
     * Acquisisce una lista di valori dal Database filtrandoli
     * per Termine di ricerca (se specificato), Ruolo e Identificativo utente (se Curatore Museale)
     *
     * @param term CharSequence
     * @param role String
     * @param uid String
     *
     */
    public void loadPlaces(CharSequence term, String role, String uid) {

        placesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                result.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String title = ds.child("title").getValue(String.class);
                    String author = ds.child("author").getValue(String.class);
                    String owner = ds.child("createdBy").getValue(String.class);
                    boolean queryCondition = (title != null && title.toLowerCase().contains(term.toString().toLowerCase())) ||
                            (author != null && author.toLowerCase().contains(term.toString().toLowerCase()));
                    if (role != null && role.equals("curator")) {
                        queryCondition = queryCondition && owner.equals(uid);
                    }
                    if (queryCondition){
                        result.add(new PlaceModel(
                                ds.child("id").getValue(String.class),
                                ds.child("title").getValue(String.class),
                                ds.child("description").getValue(String.class),
                                ds.child("roomID").getValue(String.class),
                                ds.child("author").getValue(String.class),
                                ds.child("period").getValue(String.class),
                                ds.child("createdBy").getValue(String.class),
                                ds.child("isOpen").getValue(Boolean.class),
                                ds.child("minigame").getValue(String.class)
                        ));
                    }
                }
                places.setValue(result); // IMPORTANTE! Questo è il trigger update
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }


}