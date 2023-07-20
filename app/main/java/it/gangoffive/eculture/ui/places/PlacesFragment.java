package it.gangoffive.eculture.ui.places;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentIntegrator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.gangoffive.eculture.CustomCaptureActivity;
import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.PlaceModel;
import it.gangoffive.eculture.ui.add.AddPlaceActivity;
import it.gangoffive.eculture.model.StructureModel;
import it.gangoffive.eculture.ui.home.HomeFragment;
import it.gangoffive.eculture.ui.places.adapters.PlaceCardRecyclerViewAdapter;
import it.gangoffive.eculture.viewmodel.PlaceViewModel;

public class PlacesFragment extends Fragment {

    private PlaceViewModel placesViewModel;
    private String userRole;
    private String userUid;
    private TextWatcher watcher;
    private TextInputEditText term;

    private Button scanBtn;
    private AlertDialog alert = null;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        placesViewModel = new ViewModelProvider(this).get(PlaceViewModel.class);
        View view = inflater.inflate(R.layout.fragment_list_places, container, false);

        userRole = ((MainActivity) getActivity()).getUserRole();
        userUid = ((MainActivity) getActivity()).getUserUid();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));

        placesViewModel.getPlaces("", userRole, userUid).observe(getViewLifecycleOwner(), new Observer<List<PlaceModel>>() {
            @Override
            public void onChanged(@Nullable List<PlaceModel> list) {
                if (list.size() == 0){
                    list.add(new PlaceModel(null, "", "", "", "", "", "", false, ""));
                }
                PlaceCardRecyclerViewAdapter adapter = new PlaceCardRecyclerViewAdapter(list);
                recyclerView.setAdapter(adapter);
            }
        });
        placesViewModel.loadPlaces("", userRole, userUid);




        final FloatingActionButton buttonActivityPlaces = view.findViewById(R.id.addPointOfInterestPlaces);
        //Identificazione ruolo utente. Se è turista non instanzio il fab e tutti i listener
        if (userRole.equals("tourist")) {
            buttonActivityPlaces.setVisibility(View.GONE);
        }
        //Listener per creazioni  stanze
        buttonActivityPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddPlaceActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        __initInputSearchListener();
        checkForInternetConnection();

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        __initInputSearchListener();
    }

    @Override
    public void onStop(){
        super.onStop();
        term.removeTextChangedListener(watcher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.toolbar_places_menu, menu);
        int bg = userRole.equals("curator") ? R.drawable.curator_bg : R.drawable.tourist_bg;
        ((MainActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), bg));
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.title_places)+"</font>", Html.FROM_HTML_MODE_COMPACT));
    }

    /**
     * Gestione della selezione del pulsante per la scannerizzazione del codice QR.
     * In primis, verifica se il permesso è stato già concesso o negato o non ancora chiesto.
     *
     * NOTA: Il processo post scan viene fatto nell'activity_main nel metodo di callback "onActivityResult"
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.qrcodescanner:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startScannerQR();
                }else{
                    showCustomDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Metodo che mostra un dialog di spiegazione se:
     * 1. Il permesso non è stato ancora chiesto;
     * 2. Il permesso è stato chiesto ma negato e l'opzione "don't ask again" non è stata spuntata;
     *
     * Nel caso in cui l'opzione "don't ask again" è stata spuntata, viene mostrato un dialog che ricorda
     * la scelta fatta in precedenza e spiega all'utente come attivare il permesso.
     */
    private void showCustomDialog() {
        final int PERMESSO_CAMERA = 0;
        SharedPreferences spCheck = getActivity().getPreferences(Context.MODE_PRIVATE);
        int check = spCheck.getInt("askedtimesQrPermission", 0);
        //Caso in cui i permessi vengono chiesti per la prima volta
        if(check == 0 || check != 0 && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA))
        {
            SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("askedtimesQrPermission", 1);
            editor.apply();
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.scannerqr_permission_description)).setTitle(getString(R.string.scannerqr_permission_title)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMESSO_CAMERA);
                }
            });
            alert = builder.create();
            alert.show();
        }
        //Caso in cui l'opzione di don't ask me again viene spuntata. Per ricordare all'utente che ha scelto l'opzione di non richiesta del permesso
        else{
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.scannerqr_dontaskpermission_description)).setTitle(getString(R.string.scannerqr_permission_title)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                }
            });
            alert = builder.create();
            alert.show();
        }


    }

    /**
     * Metodo attraverso il quale viene fatta partire l'activity responsabile della lettura del codice QR.
     * Inoltre, il metodo, richiamando metodi della libreria, impsota le preferenze per l'UI dello scanner.
     */
    private void startScannerQR() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
        intentIntegrator.setBeepEnabled(getActivity().getSharedPreferences("SettingsActivity" ,Context.MODE_PRIVATE).getBoolean("scannerSound", true));
        intentIntegrator.setPrompt(getString(R.string.scannerqr_prompt_message));
        intentIntegrator.setCaptureActivity(CustomCaptureActivity.class);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }

    /**
     * Controllo per connessione internet. Nel caso fosse assente, viene mostrato a schermo un box informativo
     */
    private void checkForInternetConnection(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        com.google.android.material.card.MaterialCardView alertBox = getActivity().findViewById(R.id.alertBoxPlaceFrag);
        if(cm.getActiveNetworkInfo() == null){
            alertBox.setVisibility(View.VISIBLE);
        }
        else {
            alertBox.setVisibility(View.GONE);
        }
    }

    /**
     *
     * Inizializza il Listener sull'input box di ricerca
     *
     */
    private void __initInputSearchListener(){
        term = (TextInputEditText) getActivity().findViewById(R.id.search_places);
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                placesViewModel.loadPlaces(charSequence, userRole, userUid);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        term.addTextChangedListener(watcher);
    }
}