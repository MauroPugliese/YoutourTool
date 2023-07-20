package it.gangoffive.eculture.ui.wizard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.List;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.model.StructureModel;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.wizard.adapters.StructureCardRecyclerViewAdapter;
import it.gangoffive.eculture.viewmodel.StructureViewModel;

public class StepStructuresFragment extends Fragment implements Step {

    private StructureViewModel structureViewModel;
    private String userRole;
    private String userUid;
    private TourModel tour;
    private String mode;
    private TextWatcher watcher;
    private TextInputEditText term;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_structures, container, false);
        userRole = ((WizardActivity) getActivity()).getUserRole();
        userUid = ((WizardActivity) getActivity()).getUserUid();
        tour = ((WizardActivity) getActivity()).getTour();
        mode = ((WizardActivity) getActivity()).getMode();

        structureViewModel = new ViewModelProvider(this).get(StructureViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.wiz_structure_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));

        structureViewModel.getStructures("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<StructureModel>>() {
            @Override
            public void onChanged(@Nullable List<StructureModel> list) {
                if (list.size() == 0){
                    list.add(new StructureModel(null,"","","","","","",""));
                }
                StructureCardRecyclerViewAdapter adapter = new StructureCardRecyclerViewAdapter(list, tour.getStructure());
                recyclerView.setAdapter(adapter);
            }
        });
        structureViewModel.loadStructures("", userRole, userUid);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        term = getActivity().findViewById(R.id.search_structures);
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                structureViewModel.loadStructures(charSequence, userRole, userUid);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        term.addTextChangedListener(watcher);
    }

    @Override
    public void onStop(){
        super.onStop();
        term.removeTextChangedListener(watcher);
    }

    @Override
    public VerificationError verifyStep() {
        VerificationError next = null;
        ViewGroup container = getActivity().findViewById(R.id.wiz_structure_recycler_view);
        int counter = 0;
        String structure = null;
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            RadioButton radio = v.findViewById(R.id.wiz_structure_radio);
            if (radio.isChecked()){
                structure = (String) radio.getText();
                counter++;
            }
        }
        if (counter == 0){
            next = new VerificationError(getActivity().getString(R.string.wiz_no_selection));
        }
        tour.setStructure(structure);
        if (mode.equals("WRITE")){
            tour.setRooms(null);
        }
        return next;
    }

    @Override
    public void onSelected() {
        ((WizardActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.wiz_structures_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
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

}