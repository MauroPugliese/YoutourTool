package it.gangoffive.eculture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.gangoffive.eculture.auth.LoginActivity;
import it.gangoffive.eculture.auth.RegistrationActivity;
import it.gangoffive.eculture.model.UserModel;

public class WelcomeActivity extends AppCompatActivity {
    private static DatabaseReference userRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!MainActivity.persistence) {
            FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").setPersistenceEnabled(true);
            MainActivity.persistence = true;
        }
        userRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("user");

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_welcome);
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.registerCallback(CallbackManager.Factory.create(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onError(FacebookException exception) {
                LoginManager.getInstance().logOut();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        }
    }


    /**
     * Gestione accesso via Facebook
     * @param token Token di accesso
     */
    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Bundle b = new Bundle();
        b.putString("fields", "public_profile");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String[] split = user.getDisplayName().split(" ");
                            String name = split[0];
                            for (int i = 1; i < split.length - 1; i++) {
                                name = name.concat(" " + split[i]);
                            }
                            UserModel userModel = null;
                            userModel = new UserModel(split[split.length - 1], name, null);
                            userRef.child(mAuth.getUid()).setValue(userModel);

                            Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                            intent.putExtra("facebook", true);
                            startActivity(intent);


                        } else {
                            Toast.makeText(WelcomeActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    /**
     * Fa partire l'Activity di login
     *
     * @param v Bottone che ha invocato il metodo
     */
    public void login_action(View v) {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
    }

    /**
     * Fa partire l'Activity di registrazione
     *
     * @param v Bottone che ha invocato il metodo
     */
    public void register_action(View v) {
        startActivity(new Intent(WelcomeActivity.this, RegistrationActivity.class));
    }
}