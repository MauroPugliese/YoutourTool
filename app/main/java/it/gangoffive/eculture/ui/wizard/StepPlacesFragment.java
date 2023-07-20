package it.gangoffive.eculture.ui.wizard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.model.PlaceModel;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.wizard.adapters.PlaceChecklistRecyclerViewAdapter;
import it.gangoffive.eculture.viewmodel.PlaceViewModel;


public class StepPlacesFragment extends Fragment implements Step {

    private String userRole;
    private String userUid;
    private TourModel tour;
    private PlaceViewModel placeViewModel;
    private List<PlaceModel> allPlacesList = new ArrayList<>();
    private List<PlaceModel> placesList = new ArrayList<>();
    RecyclerView recyclerView;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_places, container, false);

        placeViewModel = new ViewModelProvider(this).get(PlaceViewModel.class);

        userRole = ((WizardActivity) getActivity()).getUserRole();
        userUid = ((WizardActivity) getActivity()).getUserUid();
        tour = ((WizardActivity) getActivity()).getTour();

        recyclerView = view.findViewById(R.id.wiz_place_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));

        placeViewModel.getPlaces("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<PlaceModel>>() {
            @Override
            public void onChanged(@Nullable List<PlaceModel> list) {
                if (list.size() == 0){
                    list.add(new PlaceModel(null, "", "", "", "", "", "", false, ""));
                }
                allPlacesList = list;

                PlaceChecklistRecyclerViewAdapter adapter = new PlaceChecklistRecyclerViewAdapter(placesList, ((WizardActivity) getActivity()));
                recyclerView.setAdapter(adapter);
            }
        });
        placeViewModel.loadPlaces("", userRole, userUid);

        return view;
    }

    @Override
    public VerificationError verifyStep() {
        VerificationError next = null;
        ViewGroup container = getActivity().findViewById(R.id.wiz_place_recycler_view);

        String prevRoomId = "NULL";
        int position = 0;
        int counter = 0;
        int singleRoomCounter = -1;

        tour.resetPlaces();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            TextView roomIdHolder = v.findViewById(R.id.wiz_room_place_id);
            RadioButton placeIdHolder = v.findViewById(R.id.wiz_place_radio);
            TextView placeTitleHolder = v.findViewById(R.id.wiz_place_title);
            String roomID = String.valueOf(roomIdHolder.getText());

            if (!roomID.equals(prevRoomId) ){

                if (singleRoomCounter == 0){
                    tour.setPlaces(position, prevRoomId, "");
                }
                position++;
                prevRoomId = roomID;
                singleRoomCounter = 0;
            }

            if (placeIdHolder.isChecked() && placeIdHolder.getVisibility() == View.VISIBLE){
                counter++;
                singleRoomCounter++;
                tour.setPlaces(position, prevRoomId, String.valueOf(placeTitleHolder.getText()));
            }
        }

        if (singleRoomCounter == 0){
            tour.setPlaces(position, prevRoomId, "");
        }

        if (counter == 0){
            tour.resetPlaces();
            next = new VerificationError(getActivity().getString(R.string.wiz_no_selection));
        }

        return next;
    }

    @Override
    public void onSelected() {
        ((WizardActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.wiz_places_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
        placeViewModel.loadPlaces("", userRole, userUid);
        if (tour.getRooms() != null) {
            placesList.clear();
            for (int i = 0; i < tour.getRooms().size(); i++){
                int counter = 0;
                for (int j = 0; j < allPlacesList.size(); j++){
                    if (allPlacesList.get(j).getRoomID().equals(tour.getRooms().get(i))){
                        placesList.add(allPlacesList.get(j));
                        counter++;
                    }
                }
                if (counter == 0){
                    placesList.add(new PlaceModel(null, "", "", tour.getRooms().get(i), "", "", "", false, ""));
                }
            }
        }

    }

    @Override
    public void onError(@NonNull @NotNull VerificationError error) {
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
}
