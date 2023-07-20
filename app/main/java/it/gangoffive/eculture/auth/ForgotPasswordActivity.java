package it.gangoffive.eculture.auth;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import it.gangoffive.eculture.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.forgot_password) + "</font>", Html.FROM_HTML_MODE_COMPACT));
        Button b = findViewById(R.id.forgotRequest);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                TextInputEditText et = findViewById(R.id.email);
                try {
                    auth.sendPasswordResetEmail(et.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        if (task.isSuccessful()) {
                                            emailSent();
                                        } else {
                                            throw task.getException();
                                        }
                                    } catch (FirebaseNetworkException fne) {
                                        alertForgot(R.string.caution, R.string.no_connection);
                                    } catch (FirebaseAuthInvalidCredentialsException faice) {
                                        emailSent();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } catch (Exception e) {
                    alertForgot(R.string.caution, R.string.empty_mail);
                }
            }
        });
    }

    /**
     * Metodo che informa l'utente dell'invio (eventuale) dell'email.
     * É stato scelto di mostrare a priori un messaggio con "Email Inviata"
     * per impedire di far conoscere quali mail siano effettivamente presenti nel database.
     */
    public void emailSent() {
        AlertDialog.Builder ad = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        ad.setTitle(R.string.email_sent);
        ad.setMessage(R.string.email_sent_info);
        ad.setNeutralButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        onBackPressed();
                    }
                });
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                onBackPressed();
            }
        });
        ad.show();
    }

    /**
     * Metodo per mostrare un messaggio di errore all'utente in caso la mail inserita non rispetti il formato richiesto
     *
     * @param title   Titolo dell'AlertDialog da mostrare
     * @param message Messaggio dell'AlertDialog da mostrare
     */
    public void alertForgot(int title, int message) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}
