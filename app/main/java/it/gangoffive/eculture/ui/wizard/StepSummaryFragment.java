package it.gangoffive.eculture.ui.wizard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.List;

import it.gangoffive.eculture.DetailsTourActivity;
import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.graph.GraphAdapter;
import it.gangoffive.eculture.model.GraphModel;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.add.AddPlaceActivity;
import it.gangoffive.eculture.viewmodel.RoomViewModel;
import it.gangoffive.eculture.viewmodel.TourViewModel;

public class StepSummaryFragment extends Fragment implements Step {

    private GraphAdapter mGraphAdapter;
    private List<RoomModel> mRoomList = new ArrayList<>();
    private List<GraphModel> mRoomGraph = new ArrayList<>();
    private RoomViewModel roomViewModel;
    private TourViewModel tourViewModel;
    private RecyclerView recyclerView;
    private List<TourModel> mTourList = new ArrayList<>();
    private String userRole;
    private String userUid;
    private TourModel tour;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_summary, container, false);

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        tourViewModel = new ViewModelProvider(this).get(TourViewModel.class);

        userRole = ((WizardActivity) getActivity()).getUserRole();
        userUid = ((WizardActivity) getActivity()).getUserUid();
        tour = ((WizardActivity) getActivity()).getTour();

        recyclerView = view.findViewById(R.id.wiz_graph_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));

        roomViewModel.getRooms("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<RoomModel>>() {
            @Override
            public void onChanged(@Nullable List<RoomModel> list) {
                mRoomList.clear();
                for (int i = 0; i < list.size(); i++) {
                    if (tour.getRooms().contains(list.get(i).getId())) {
                        mRoomList.add(list.get(i));
                    }
                }
            }
        });
        roomViewModel.loadRooms("", userRole, userUid);
        tourViewModel.getTours("", "", "").observe(getViewLifecycleOwner(), new Observer<List<TourModel>>() {
            @Override
            public void onChanged(@Nullable List<TourModel> list) {
                mTourList = list;
            }
        });
        tourViewModel.loadTours("", "", "");
        return view;
    }

    @Override
    public VerificationError verifyStep() {
        DatabaseReference toursRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("tours");
        String execMode = ((WizardActivity) getActivity()).getMode();
        if(execMode != null && execMode.equals("WRITE")){
            int position = 0;
            if (mTourList != null){
                for (int i = 0; i < mTourList.size(); i++){
                    if (Integer.parseInt(mTourList.get(i).getId()) > position){
                        position = Integer.parseInt(mTourList.get(i).getId());
                    }
                }
            }
            tour.setId(String.valueOf(position + 1));
            tour.setCreatedBy(((WizardActivity) getActivity()).getUserUid());
            toursRef.child(String.valueOf(position + 1)).setValue(tour);
        } else {
            toursRef.child(tour.getId()).setValue(tour);
        }

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);

        return null;
    }

    @Override
    public void onSelected() {
        ((WizardActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.wiz_summary_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
        roomViewModel.loadRooms("", userRole, userUid);
        mRoomGraph.clear();
        ArrayList<String> roomIds = new ArrayList<String>();
        for (int i = 0; i < tour.getPlaces().size(); i++){
            for (int j = 0; j < mRoomList.size(); j++){
                if (mRoomList.get(j).getId().equals(tour.getPlaces().get(i).get("roomid").get(0))){

                    mRoomGraph.add(new GraphModel(
                            "rooms",
                            mRoomList.get(j).getId(),
                            mRoomList.get(j).getName(),
                            tour.getPlaces().get(i).get("places"),
                            roomIds.contains(mRoomList.get(j).getId()) ? 1 : 2)
                    );
                }
            }
            roomIds.add(tour.getPlaces().get(i).get("roomid").get(0));

        }
        mGraphAdapter = new GraphAdapter(mRoomGraph);
        recyclerView.setAdapter(mGraphAdapter);
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        // Ultimo Step, validazione non necessaria
    }

}