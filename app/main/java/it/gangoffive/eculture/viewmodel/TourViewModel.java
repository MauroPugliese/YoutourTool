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
import java.util.HashMap;
import java.util.List;

import it.gangoffive.eculture.model.TourModel;

public class TourViewModel extends ViewModel {

    private MutableLiveData<List<TourModel>> tours;

    private final List<TourModel> result = new ArrayList<>();
    private final DatabaseReference roomsRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("tours");

    public TourViewModel() {
        tours = new MutableLiveData<List<TourModel>>();
    }

    public LiveData<List<TourModel>> getTours(CharSequence term, String role, String uid) {
        if (tours == null) {
            tours = new MutableLiveData<List<TourModel>>();
            loadTours(term, role, uid);
        }
        return tours;
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
    public void loadTours(CharSequence term, String role, String uid) {

        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                result.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String title = ds.child("title").getValue(String.class);
                    String subtitle = ds.child("subtitle").getValue(String.class);
                    String owner = ds.child("createdBy").getValue(String.class);
                    boolean queryCondition = (title != null && title.toLowerCase().contains(term.toString().toLowerCase())) ||
                            (subtitle != null && subtitle.toLowerCase().contains(term.toString().toLowerCase()));
                    if (role.equals("curator")) {
                        queryCondition = queryCondition && owner.equals(uid);
                    }
                    if (queryCondition) {
                        result.add(new TourModel(
                                ds.child("id").getValue(String.class),
                                ds.child("title").getValue(String.class),
                                ds.child("subtitle").getValue(String.class),
                                ds.child("description").getValue(String.class),
                                ds.child("structure").getValue(String.class),
                                (ArrayList<String>) ds.child("rooms").getValue(),
                                (ArrayList<HashMap<String, ArrayList<String>>>) ds.child("places").getValue(),
                                ds.child("createdBy").getValue(String.class)
                        ));
                    }
                }
                tours.setValue(result); // IMPORTANTE! Questo è il trigger update
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }


}
