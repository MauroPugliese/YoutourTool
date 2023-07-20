package it.gangoffive.eculture.ui.wizard.adapters;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.ui.wizard.StepInfoFragment;
import it.gangoffive.eculture.ui.wizard.StepPlacesFragment;
import it.gangoffive.eculture.ui.wizard.StepRoomsFragment;
import it.gangoffive.eculture.ui.wizard.StepStructuresFragment;
import it.gangoffive.eculture.ui.wizard.StepSummaryFragment;

public class WizardAdapter extends AbstractFragmentStepAdapter {

    public WizardAdapter(FragmentManager fm, Context context) {
        super(fm, context);
    }

    @Override
    public Step createStep(int position) {
        Step step = new StepStructuresFragment();
        switch (position){
            case 0:
                step = new StepInfoFragment();
                break;
            case 1:
                step = new StepStructuresFragment();
                break;
            case 2:
                step = new StepRoomsFragment();
                break;
            case 3:
                step = new StepPlacesFragment();
                break;
            case 4:
                step = new StepSummaryFragment();
                break;
        }
        Bundle b = new Bundle();
        b.putInt("CURRENT_STEP_POSITION_KEY", position);
        ((Fragment) step).setArguments(b);
        return step;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        int stepTitle = R.string.wiz_tab_title;
        switch (position){
            case 0:
                stepTitle = R.string.wiz_structures_title;
                break;
            case 1:
                stepTitle = R.string.wiz_rooms_title;
                break;
            case 2:
                stepTitle = R.string.wiz_info_title;
                break;
            case 3:
                stepTitle = R.string.wiz_summary_title;
                break;
        }
        return new StepViewModel.Builder(context).setTitle(stepTitle).create();
    }

}
