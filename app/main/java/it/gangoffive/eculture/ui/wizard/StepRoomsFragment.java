package it.gangoffive.eculture.ui.wizard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.List;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.model.StructureModel;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.wizard.adapters.RoomAddedCardRecyclerViewAdapter;
import it.gangoffive.eculture.ui.wizard.adapters.RoomCardRecyclerViewAdapter;
import it.gangoffive.eculture.viewmodel.RoomViewModel;

public class StepRoomsFragment extends Fragment implements Step {

    private RoomViewModel roomViewModel;
    private List<RoomModel> allRoomList = new ArrayList<>();
    private List<RoomModel> selectedRoomList = new ArrayList<>();
    private RoomAddedCardRecyclerViewAdapter adapterSelected;
    private String userRole;
    private String userUid;
    private TourModel tour;
    private String mode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_rooms, container, false);

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        userRole = ((WizardActivity) getActivity()).getUserRole();
        userUid = ((WizardActivity) getActivity()).getUserUid();
        tour = ((WizardActivity) getActivity()).getTour();
        mode = ((WizardActivity) getActivity()).getMode();


        // Set up the RecyclerView for All Rooms List
        RecyclerView roomsRecyclerView = view.findViewById(R.id.wiz_room_recycler_view);
        roomsRecyclerView.setHasFixedSize(true);
        roomsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));

        // Set up the RecyclerView for Selected Rooms List
        RecyclerView selectedRoomsRecyclerView = view.findViewById(R.id.wiz_added_room_recycler_view);
        selectedRoomsRecyclerView.setHasFixedSize(true);
        selectedRoomsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));

        roomViewModel.getRooms("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<RoomModel>>() {
            @Override
            public void onChanged(@Nullable List<RoomModel> list) {
                if (list.size() == 0){
                    list.add(new RoomModel(null, "", "", "", false, "", ""));
                } else {
                    allRoomList.clear();
                    for (int i = 0; i < list.size(); i++){
                        if (tour.getStructure() != null && tour.getStructure().equals(list.get(i).getStructure_id())){
                            allRoomList.add(list.get(i));
                        } else if (tour.getRooms() != null && tour.getRooms().contains(list.get(i).getId())){
                            addToSelectedRoomList(list.get(i));
                        }
                    }
                }
                adapterSelected = new RoomAddedCardRecyclerViewAdapter(selectedRoomList, StepRoomsFragment.this);
                RoomCardRecyclerViewAdapter adapter = new RoomCardRecyclerViewAdapter(allRoomList, StepRoomsFragment.this);
                roomsRecyclerView.setAdapter(adapter);
                selectedRoomsRecyclerView.setAdapter(adapterSelected);
                if (selectedRoomList.size() > 0 && tour.getStructure() != null && !selectedRoomList.get(0).getStructure_id().equals(tour.getStructure())){
                    selectedRoomList.clear();
                }
            }
        });
        roomViewModel.loadRooms("", userRole, userUid);
        return view;
    }


    @Override
    public VerificationError verifyStep() {
        VerificationError next = null;
        ViewGroup container = getActivity().findViewById(R.id.wiz_added_room_recycler_view);
        ArrayList<String> rooms = new ArrayList<>();
        int flag = 0;
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            TextView roomIdHolder = v.findViewById(R.id.wiz_room_id);
            rooms.add(String.valueOf(roomIdHolder.getText()));
        }
        for (int i = 1; i < rooms.size(); i++) {
            if (rooms.size() >= 1 && rooms.get(i).equals(rooms.get(i - 1))){
                flag = 1;
                break;
            }
        }
        if (container.getChildCount() == 0){
            next = new VerificationError(getActivity().getString(R.string.wiz_no_selection));
        }
        if (flag == 1){
            next = new VerificationError(getActivity().getString(R.string.wiz_room_repeated_msg));
        }
        for (int i = 0; i < selectedRoomList.size(); i++){
            ((WizardActivity) getActivity()).setSelectedRooms(selectedRoomList.get(i).getId(), selectedRoomList.get(i).getName());
        }
        tour.setRooms(rooms);
        return next;
    }

    @Override
    public void onSelected() {
        ((WizardActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.wiz_rooms_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
        roomViewModel.loadRooms("", userRole, userUid);
        if (tour.getRooms() != null){
            selectedRoomList.clear();

            for (int i = 0; i < tour.getRooms().size(); i++){
                for (int j = 0; j < allRoomList.size(); j++){
                    if (allRoomList.get(j).getId().equals(tour.getRooms().get(i))){
                        addToSelectedRoomList(allRoomList.get(j));
                    }
                }
            }
        }
        if (mode.equals("WRITE")){
            tour.resetPlaces();
        } else {
            ViewGroup container = getActivity().findViewById(R.id.wiz_added_room_recycler_view);
            ArrayList<String> selectedRoomsOnEdit = new ArrayList<>();
            for (int i = 0; i < container.getChildCount(); i++) {
                View v = container.getChildAt(i);
                TextView roomIdHolder = v.findViewById(R.id.wiz_room_id);
                selectedRoomsOnEdit.add(String.valueOf(roomIdHolder.getText()));
            }
            if (selectedRoomsOnEdit.equals(tour.getRooms())){
                tour.resetPlaces();
            }
        }
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        new MaterialAlertDialogBuilder(getActivity(), R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(R.string.caution)
                .setMessage(error.getErrorMessage())
                .setIcon(R.drawable.ic_baseline_error_outline_24)
                .setPositiveButton(R.string.wiz_no_selection_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }


    /**
     *
     * Aggiunge la stanza selezionata alla lista corrispondente
     * e aggiorna la View
     *
     * @param room RoomModel
     */
    public void addToSelectedRoomList(RoomModel room){
        selectedRoomList.add(room);
        adapterSelected.notifyDataSetChanged();
        adapterSelected.notifyItemInserted(selectedRoomList.size() - 1);
    }

}