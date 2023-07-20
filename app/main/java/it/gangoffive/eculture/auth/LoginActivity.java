package it.gangoffive.eculture.auth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextInputEditText mail;
    private TextInputEditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.login) + "</font>", Html.FROM_HTML_MODE_COMPACT));
        mAuth = FirebaseAuth.getInstance();
        mail = findViewById(R.id.email);
        password = findViewById(R.id.password);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onBackPressed();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }

    /**
     * Login attraverso le credenziali fornite dall'utente. Chiama il metodo alertLogin in caso ci siano problemi nell'effettuare l'operazione.
     * Viene utilizzato SharedPreferences per salvare il tipo di utente che sta effettuando il login.
     */
    public void login(View v) {
        if (!checkFields()) {
            try {
                mAuth.signInWithEmailAndPassword(mail.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        DatabaseReference userReference = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("user");
                                        userReference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.e("firebase", "Error getting data on user welcome name", task.getException());
                                                } else {
                                                    HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                                                    SharedPreferences sp = getApplicationContext().getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
                                                    sp.edit().putString("type", hm.get("type")).apply();
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                }
                                            }
                                        });
                                    } else {
                                        throw task.getException();
                                    }
                                } catch (FirebaseNetworkException fne) {
                                    alertLogin(R.string.caution, R.string.no_connection);
                                } catch (Exception e) {
                                    alertLogin(R.string.caution, R.string.wrong_credentials);
                                }
                            }
                        });
            } catch (IllegalArgumentException e) {
                if (TextUtils.isEmpty(mail.getText().toString()) && TextUtils.isEmpty(password.getText().toString())) {
                    alertLogin(R.string.caution, R.string.empty_string);
                } else if (TextUtils.isEmpty(password.getText().toString())) {
                    alertLogin(R.string.caution, R.string.empty_password);
                } else {
                    alertLogin(R.string.caution, R.string.empty_mail);
                }
            }
        }
    }

    /**
     * Metodo che controlla dinamicamente tutti gli EditText presenti nel layout e verifica se sono vuoti
     *
     * @return Restituisce Vero se ha trovato campi vuoti, Falso altrimenti
     */
    public boolean checkFields() {
        boolean check = false;
        View layout = findViewById(R.id.scroll);
        ArrayList<View> touchables = layout.getTouchables();
        for (View text : touchables) {
            if (text instanceof EditText) {
                int ident = getResources().getIdentifier(getResources().getResourceName(text.getId()).substring(26).concat("_error"), "id", getPackageName());
                TextView tv = findViewById(ident);
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

    /**
     * Mostra un AlertDialog all'utente, generalmente è un messaggio di errore per problemi nell'effettuare il login
     *
     * @param title   Titolo dell'AlertDialog da mostrare
     * @param message Messaggio dell'AlertDialog da mostrare
     */
    public void alertLogin(int title, int message) {
        AlertDialog.Builder ad = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setNeutralButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
        ad.show();
    }

    /**
     * Fa partire l'Activity di richiesta password
     *
     * @param v Bottone che ha invocato il metodo
     */
    public void forgotPassword(View v) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }
}