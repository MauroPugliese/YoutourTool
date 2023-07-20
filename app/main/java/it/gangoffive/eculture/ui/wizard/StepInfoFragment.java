package it.gangoffive.eculture.ui.wizard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.model.TourModel;

public class StepInfoFragment extends Fragment implements Step {

    private TourModel tour;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_step_info, container, false);
        tour = ((WizardActivity) getActivity()).getTour();
        return v;
    }

    @Override
    public VerificationError verifyStep() {
        VerificationError next = null;
        TextInputEditText title = getActivity().findViewById(R.id.wiz_input_title);
        TextInputEditText subtitle = getActivity().findViewById(R.id.wiz_input_subtitle);
        TextInputEditText description = getActivity().findViewById(R.id.wiz_input_description);

        if (String.valueOf(title.getText()).equals("") || String.valueOf(description.getText()).equals("")){
            next = new VerificationError(getActivity().getString(R.string.wiz_no_input));
        }

        tour.setTitle(String.valueOf(title.getText()));
        tour.setSubtitle(String.valueOf(subtitle.getText()));
        tour.setDescription(String.valueOf(description.getText()));

        return next;
    }

    @Override
    public void onSelected() {
        ((WizardActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.wiz_info_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
        TextInputEditText title = getActivity().findViewById(R.id.wiz_input_title);
        TextInputEditText subtitle = getActivity().findViewById(R.id.wiz_input_subtitle);
        TextInputEditText description = getActivity().findViewById(R.id.wiz_input_description);

        if (tour.getTitle() != null){
            title.setText(tour.getTitle());
        }
        if (tour.getSubtitle() != null){
            subtitle.setText(tour.getSubtitle());
        }
        if (tour.getDescription() != null){
            description.setText(tour.getDescription());
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

}