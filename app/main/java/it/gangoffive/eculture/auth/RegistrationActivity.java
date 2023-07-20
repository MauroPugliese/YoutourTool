package it.gangoffive.eculture.auth;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.auth.ui.registration.TypeFragment;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.register_title) + "</font>", Html.FROM_HTML_MODE_COMPACT));
        if (getIntent().getBooleanExtra("facebook", false)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerReg, TypeFragment.newInstance(true))
                    .commitNow();
        } else if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerReg, TypeFragment.newInstance(false))
                    .commitNow();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Fa partire l'Activity di Login
     *
     * @param v Bottone che ha attivato il metodo
     */
    public void login_action(View v) {
        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
    }
}