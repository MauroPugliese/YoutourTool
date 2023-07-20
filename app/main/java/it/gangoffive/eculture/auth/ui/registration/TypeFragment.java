package it.gangoffive.eculture.auth.ui.registration;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.auth.RegistrationActivity;

public class TypeFragment extends Fragment implements View.OnClickListener {
    private static int oldPressed = 0;
    private final DatabaseReference userRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("user");
    private FirebaseAuth auth;

    public TypeFragment() {
    }

    /**
     * @param face Boolean,
     *             se Vero indica che si sta andando a registrare un utente che ha effettuato l'accesso con Facebook,
     *             falso altrimenti.
     * @return Istanza di TypeFragment con il Bundle associato
     */
    public static TypeFragment newInstance(boolean face) {
        TypeFragment fragment = new TypeFragment();
        if (face) {
            Bundle args = new Bundle();
            args.putBoolean("facebook", true);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (getActivity().getIntent().getBooleanExtra("facebook", false)) {
                userRef.child(user.getUid()).child("type").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            if (task.getResult().getValue() != null)
                                startActivity(new Intent(getContext(), MainActivity.class));
                        }
                    }
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button b = getView().findViewById(R.id.tourist);
        int temp = b.getId();
        b.setOnClickListener(this);
        b = getView().findViewById(R.id.curator);
        b.setOnClickListener(this);
        b = getView().findViewById(R.id.nextButton);
        b.setOnClickListener(this);
        if (oldPressed != 0) {
            int t = oldPressed;
            oldPressed++;
            expand(getActivity().findViewById(t));
        } else {
            oldPressed = temp;
        }

    }

    /**
     * Metodo che va ad "espandere" il dettaglio del tipo di utente selezionato mostrandone una descrizione.
     *
     * @param v è una View (Button) che viene utilizzato per controllare quale delle due descrizioni mostrare.
     */
    public void expand(@NonNull View v) {
        if (v.getId() != oldPressed) {
            resetExpand();
            Button b = getView().findViewById(v.getId());
            b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_outline_24, 0);
            switch (b.getId()) {
                case R.id.tourist:
                    b.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.touristcolor));
                    getView().findViewById(R.id.touristtext).setVisibility(View.VISIBLE);
                    ((RegistrationActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.register_as_tourist) + "</font>", Html.FROM_HTML_MODE_COMPACT));
                    break;
                case R.id.curator:
                    b.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.curatorcolor));
                    getView().findViewById(R.id.curatortext).setVisibility(View.VISIBLE);
                    ((RegistrationActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.register_as_curator) + "</font>", Html.FROM_HTML_MODE_COMPACT));
            }
            oldPressed = v.getId();
        }
    }

    /**
     * Nel momento in cui si va a espandere la nuova descrizione a priori si vanno a chiudere entrambe. (Impostando la visibilità a GONE e resettando le icone)
     */
    public void resetExpand() {
        getView().findViewById(R.id.curatortext).setVisibility(View.GONE);
        getView().findViewById(R.id.touristtext).setVisibility(View.GONE);
        getView().findViewById(R.id.tourist).setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.white));
        getView().findViewById(R.id.curator).setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.white));
        ((Button) getView().findViewById(R.id.tourist)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_expand_more_24, 0, 0, 0);
        ((Button) getView().findViewById(R.id.curator)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_expand_more_24, 0, 0, 0);

    }

    /**
     * Metodo che inserisce in SharedPreferences il tipo di utente (viene effettuato sia qui che nel RegistrationFragment poiché
     * se l'utente effettua la registrazione con Facebook non andrà mai nel RegistrationFragment).
     * Inoltre se l'utente effeettua la registrazione con Facebook viene rimandato alla MainActivity, altrimenti continua la registrazione
     * con il RegistrationFragment
     */
    public void registration() {
        String type = "";
        switch (oldPressed) {
            case R.id.tourist:
                type = "tourist";
                break;
            case R.id.curator:
                type = "curator";
                break;
        }
        if (getActivity().getIntent().getBooleanExtra("facebook", false)) {
            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            sp.edit().putString("type", type).apply();
            auth = FirebaseAuth.getInstance();
            userRef.child(auth.getUid()).child("type").setValue(type);
            startActivity(new Intent(getActivity(), MainActivity.class));
        } else {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerReg, RegistrationFragment.newInstance(type))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.curator:
            case R.id.tourist:
                expand(v);
                break;
            case R.id.nextButton:
                registration();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && !getActivity().getIntent().getBooleanExtra("facebook", false)) {
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }

}
