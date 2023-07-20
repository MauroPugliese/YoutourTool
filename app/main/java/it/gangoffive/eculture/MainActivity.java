package it.gangoffive.eculture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;

import it.gangoffive.eculture.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private String userRole;
    private String userUid;

    private ActivityMainBinding binding;
    private static final HashMap<String, String> hm = new HashMap();
    public static int theme;
    public static boolean persistence = false;
    private FirebaseAuth mAuth;
    private AlertDialog alert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!persistence) {
            FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").setPersistenceEnabled(true);
            persistence = true;
        }
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
        } else {

            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userUid = currentUser.getUid();

            userRole = sp.getString("type", "tourist");

            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            switch (userRole) {
                case "curator":
                    setTheme(R.style.Theme_Eculture_curator);
                    theme = R.style.Theme_Eculture_curator;
                    break;
                case "tourist":
                    setTheme(R.style.Theme_Eculture_tourist);
                    theme = R.style.Theme_Eculture_tourist;
                    break;
            }


            BottomNavigationView navView = findViewById(R.id.nav_view);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_places, R.id.navigation_tours)
                    .build();
            NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_activity_main);
            NavigationUI.setupActionBarWithNavController(MainActivity.this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);

        }


    }

    @Override
    public void onBackPressed() {
        String home = getResources().getString(R.string.title_home);
        String title = String.valueOf(getSupportActionBar().getTitle());
        if (home.equalsIgnoreCase(title)) {
            finishAffinity();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Metodo responsabile della decodifica dell'informazione letta dallo scanner lanciato nel "PlaceFragment".
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                //Caso in cui l'utente usa il tasto back

            } else {
                //Gestione con blocco try per i QR non validi
                try{
                    String rawString = result.getContents();
                    String[] parti = rawString.split("-");
                    String slice = parti[0];
                    String title = parti[2];
                    String idInt = parti[1];


                    switch (slice){
                        case "places":
                            Intent intentJumpPlace = new Intent(this, DetailsPlaceActivity.class);
                            intentJumpPlace.putExtra("ID", idInt);
                            startActivity(intentJumpPlace);
                            break;
                        case "rooms":
                            Intent intentJumpRoom = new Intent(this, DetailsRoomActivity.class);
                            intentJumpRoom.putExtra("ID", idInt);
                            startActivity(intentJumpRoom);
                            break;
                        case "structures":
                            Intent intentJumpStructure = new Intent(this, DetailsStructureActivity.class);
                            intentJumpStructure.putExtra("ID", idInt);
                            startActivity(intentJumpStructure);
                            break;
                        case "tours":
                            Intent intentJumpTour = new Intent(this, DetailsTourActivity.class);
                            intentJumpTour.putExtra("ID", idInt);
                            startActivity(intentJumpTour);
                            break;
                    }

                }catch (Exception e){
                    showFaultyQrDialog();
                }

            }
        }
    }

    /**
     * Metodo responsabile della costruzione di un dialog nel caso in cui il QR non sia valido o sconosciuto.
     */
    private void showFaultyQrDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.qr_code_notvalid)).setTitle(getString(R.string.qr_code)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                alert = null;

            }
        });
        alert = builder.create();
        alert.show();

    }
    public String getUserRole(){
        return this.userRole;
    }

    public String getUserUid(){
        return this.userUid;
    }
}

