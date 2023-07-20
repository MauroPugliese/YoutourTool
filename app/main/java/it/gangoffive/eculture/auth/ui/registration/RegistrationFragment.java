package it.gangoffive.eculture.auth.ui.registration;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.auth.RegistrationActivity;
import it.gangoffive.eculture.model.UserModel;

public class RegistrationFragment extends Fragment {


    public static final String TYPE = "TYPE";
    private static String mType = "";
    private final DatabaseReference userRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("user");
    private TextInputEditText email;
    private TextInputEditText password;
    private TextInputEditText surname;
    private FirebaseAuth auth;
    private TextInputEditText name;

    public RegistrationFragment() {
    }

    /**
     * @param type Stringa contenente il tipo di utente che si vuole registrare.
     *             Può essere: "tourist" o "curator".
     * @return Istanza di RegistrationFragment con il Bundle associato
     */
    public static RegistrationFragment newInstance(String type) {
        RegistrationFragment fragment = new RegistrationFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        name = getView().findViewById(R.id.name);
        surname = getView().findViewById(R.id.surname);
        email = getView().findViewById(R.id.email);
        password = getView().findViewById(R.id.password);
        Button register = getView().findViewById(R.id.register);
        int roleMsg = R.string.register_as_curator;
        if (mType.equals("tourist")) {
            roleMsg = R.string.register_as_tourist;
        }
        ((RegistrationActivity) getActivity()).getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(roleMsg) + "</font>", Html.FROM_HTML_MODE_COMPACT));
        auth = FirebaseAuth.getInstance();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean error = false;
                String[] mail = {""};
                try {
                    mail = email.getText().toString().split("@")[1].split("\\.");
                } catch (Exception e) {
                    error = true;
                }
                if (!checkFields()) {
                    if (password.getText().toString().length() < 6) {
                        alertRegistration(R.string.caution, R.string.password_short);
                    } else if (error) {
                        alertRegistration(R.string.caution, R.string.error_mail);
                    } else if (mail[mail.length - 1].length() < 2) {
                        alertRegistration(R.string.caution, R.string.error_mail);
                    } else {
                        registerUser(email.getText().toString(), password.getText().toString(), surname.getText().toString(), name.getText().toString());
                    }
                }
            }
        });
    }

    /**
     * Metodo che va a salvare nel FirebaseAuth le credenziali di accesso dell'utente e in FirebaseDatabase i restanti dati
     * Viene anche salvato attraverso SharedPreferences una stringa contenente il tipo di utente per permettere successivamente
     * di impostare il tema relativo.
     *
     * @param email    Stringa da salvare nel database (FirebaseAuth) per consentire l'accesso successivamente
     * @param password Stringa da salvare nel database (FirebaseAuth) per consentire l'accesso successivamente
     * @param surname  Stringa che viene salvata nel database (FirebaseDatabase) -> Dato personale dell'utente
     * @param name     Stringa che viene salvata nel database (FirebaseDatabase) -> Dato personale dell'utente
     */
    private void registerUser(String email, String password, String surname, String name) {
        try {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    try {
                        if (task.isSuccessful()) {
                            UserModel user = new UserModel(surname, name, mType);
                            userRef.child(auth.getUid()).setValue(user);
                            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
                            sp.edit().putString("type", mType).commit();
                            startActivity(new Intent(getContext(), MainActivity.class));
                        } else {
                            throw task.getException();
                        }
                    } catch (FirebaseNetworkException fne) {
                        alertRegistration(R.string.caution, R.string.no_connection);
                    } catch (FirebaseAuthUserCollisionException fauce) {
                        alertRegistration(R.string.caution, R.string.account_exist);
                    } catch (FirebaseAuthInvalidCredentialsException faice) {
                        alertRegistration(R.string.caution, R.string.error_mail);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    /**
     * Mostra un AlertDialog all'utente. Il metodo viene chiamato quando c'è stato un problema nella validazione e registrazione dell'account
     *
     * @param title   Titolo da mostrare nell'AlertDialog
     * @param message Messaggio da mostrare nell'AlertDialog
     */
    public void alertRegistration(int title, int message) {
        AlertDialog.Builder ad = new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setNeutralButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // In caso l'utente clicca OK nessun azione viene eseguita
                    }
                });
        ad.show();
    }

    /**
     * Metodo che controlla dinamicamente tutti gli EditText presenti nel layout e verifica se sono vuoti
     *
     * @return Restituisce Vero se ha trovato campi vuoti, Falso altrimenti
     */
    public boolean checkFields() {
        boolean check = false;
        View layout = getActivity().findViewById(R.id.scroll);
        ArrayList<View> touchables = layout.getTouchables();
        for (View text : touchables) {
            if (text instanceof EditText) {
                int ident = getResources().getIdentifier(getResources().getResourceName(text.getId()).substring(26).concat("_error"), "id", getActivity().getPackageName());
                TextView tv = getActivity().findViewById(ident);
                if (TextUtils.isEmpty(((EditText) text).getText().toString())) {
                    tv.setVisibility(View.VISIBLE);
                    check = true;
                } else {
                    tv.setVisibility(View.GONE);
                }
            }
        }
        return check;
    }

}