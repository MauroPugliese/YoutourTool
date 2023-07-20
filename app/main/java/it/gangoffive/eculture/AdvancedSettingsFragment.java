package it.gangoffive.eculture;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;


public class AdvancedSettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_advanced_settings, container, false);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //Leggo, salvo e imposto le sharedPreferences relative all'impostazione dello scanner qr
        SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        SwitchCompat pulsante = getActivity().findViewById(R.id.scannerSwitch);
        pulsante.setChecked(sp.getBoolean("scannerSound", true));

        pulsante.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("scannerSound", b);
                editor.commit();
            }
        });
    }
}