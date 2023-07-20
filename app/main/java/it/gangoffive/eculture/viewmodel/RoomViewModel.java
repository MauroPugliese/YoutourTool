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

import it.gangoffive.eculture.model.RoomModel;

public class RoomViewModel extends ViewModel {

    private MutableLiveData<List<RoomModel>> rooms;

    private final List<RoomModel> result = new ArrayList<>();
    private final DatabaseReference roomsRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms");

    public RoomViewModel() {
        rooms = new MutableLiveData<List<RoomModel>>();
    }

    public LiveData<List<RoomModel>> getRooms(CharSequence term, String role, String uid) {
        if (rooms == null) {
            rooms = new MutableLiveData<List<RoomModel>>();
            loadRooms(term, role, uid);
        }
        return rooms;
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
    public void loadRooms(CharSequence term, String role, String uid) {

        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                result.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String title = ds.child("name").getValue(String.class);
                    String owner = ds.child("createdBy").getValue(String.class);
                    boolean queryCondition = (title != null && title.toLowerCase().contains(term.toString().toLowerCase()));
                    if (role.equals("curator")) {
                        queryCondition = queryCondition && owner.equals(uid);
                    }
                    if(queryCondition){
                        result.add(new RoomModel(
                                ds.child("id").getValue(String.class),
                                ds.child("name").getValue(String.class),
                                ds.child("description").getValue(String.class),
                                ds.child("structure_id").getValue(String.class),
                                ds.child("isOpen").getValue(Boolean.class),
                                ds.child("createdBy").getValue(String.class),
                                ds.child("visiting_time").getValue(String.class)
                        ));
                    }
                }
                rooms.setValue(result); // IMPORTANTE! Questo è il trigger update
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }


}
