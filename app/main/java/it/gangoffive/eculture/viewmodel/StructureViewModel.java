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

import it.gangoffive.eculture.model.StructureModel;

public class StructureViewModel extends ViewModel {

    private MutableLiveData<List<StructureModel>> structures;

    private final List<StructureModel> result = new ArrayList<>();
    private final DatabaseReference structuresRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures");

    public StructureViewModel() {
        structures = new MutableLiveData<List<StructureModel>>();
    }

    public LiveData<List<StructureModel>> getStructures(CharSequence term, String role, String uid) {
        if (structures == null) {
            structures = new MutableLiveData<List<StructureModel>>();
            loadStructures(term, role, uid);
        }
        return structures;
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
    public void loadStructures(CharSequence term, String role, String uid) {

        structuresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                result.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = ds.child("name").getValue(String.class);
                    String address = ds.child("address").getValue(String.class);
                    String city = ds.child("city").getValue(String.class);
                    String region = ds.child("region").getValue(String.class);
                    String owner = ds.child("createdBy").getValue(String.class);
                    boolean queryCondition = (name != null && name.toLowerCase().contains(term.toString().toLowerCase())) ||
                            (address != null && address.toLowerCase().contains(term.toString().toLowerCase())) ||
                            (city != null && city.toLowerCase().contains(term.toString().toLowerCase())) ||
                            (region != null && region.toLowerCase().contains(term.toString().toLowerCase()));
                    if (role.equals("curator")) {
                        queryCondition = queryCondition && owner.equals(uid);
                    }
                    if (queryCondition){
                        result.add(new StructureModel(
                                ds.child("id").getValue(String.class),
                                ds.child("name").getValue(String.class),
                                ds.child("address").getValue(String.class),
                                ds.child("region").getValue(String.class),
                                ds.child("province").getValue(String.class),
                                ds.child("city").getValue(String.class),
                                ds.child("schedule").getValue(String.class),
                                ds.child("createdBy").getValue(String.class)
                        ));
                    }
                }
                structures.setValue(result); // IMPORTANTE! Questo è il trigger update
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }


}
