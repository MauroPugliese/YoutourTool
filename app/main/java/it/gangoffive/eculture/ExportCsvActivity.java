package it.gangoffive.eculture;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import it.gangoffive.eculture.model.PlaceModel;
import it.gangoffive.eculture.model.QuizModel;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.model.StructureModel;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.tours.adapters.TourChecklistRecyclerViewAdapter;
import it.gangoffive.eculture.viewmodel.TourViewModel;

public class ExportCsvActivity extends AppCompatActivity {

    private String userRole;
    private String userUid;
    private TourViewModel tourViewModel;

    private TextWatcher watcher;
    private TextInputEditText term;
    private TabLayout tabs;
    private TabLayout.OnTabSelectedListener tabListener;

    private int counter = 0;
    private StringBuilder data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_csv);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userUid = currentUser.getUid();
            userRole = sp.getString("type", "tourist");

            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false));
            Button exportBtn = findViewById(R.id.export_btn);
            int bgColor = userRole.equals("curator") ? ResourcesCompat.getColor(getResources(), R.color.curatorcolor, null) : ResourcesCompat.getColor(getResources(), R.color.touristcolor, null);
            exportBtn.setBackgroundColor(bgColor);

            tourViewModel = new ViewModelProvider(this).get(TourViewModel.class);
            tourViewModel.getTours("", userRole, userUid).observe(this, new Observer<List<TourModel>>() {
                @Override
                public void onChanged(@Nullable List<TourModel> list) {
                    if (list.size() == 0){
                        list.add(new TourModel());
                    }
                    TourChecklistRecyclerViewAdapter adapter = new TourChecklistRecyclerViewAdapter(list);
                    recyclerView.setAdapter(adapter);
                }
            });
            tourViewModel.loadTours("", userRole, userUid);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.export_title)+"</font>", Html.FROM_HTML_MODE_COMPACT));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(userRole.equals("curator") ? R.color.curatorcolor : R.color.touristcolor)));

        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // Listener per l'input di ricerca
        __initSearchInputListener();

        //Listener per selezione del tab attivo
        __initTabSelectionListener();

        // Listener per il pulsante di export
        __initExportButtonListener();

        // Listener per il pulsante di import
        __initImportButtonListener();

    }


    @Override
    protected void onRestart() {
        super.onRestart();

        // Listener per l'input di ricerca
        __initSearchInputListener();

        //Listener per selezione del tab attivo
        __initTabSelectionListener();

    }




    @Override
    public void onStop(){
        super.onStop();
        term.removeTextChangedListener(watcher);
        tabs.removeOnTabSelectedListener(tabListener);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     *
     * Crea il file da esportare e chiama l'Intent corrispondente alla condivisione
     *
     * @param data StringBuilder
     *
     */
    private void exportDataToCSV(StringBuilder data) {
        try {
            Calendar calendar = Calendar.getInstance();
            long time= calendar.getTimeInMillis();
            FileOutputStream out = openFileOutput("ECULTURE_"+time+".csv", Context.MODE_PRIVATE);
            out.write(data.toString().getBytes());
            out.close();
            Context context = getApplicationContext();
            final File newFile = new File(Environment.getExternalStorageDirectory(),"EcultureCSV");
            if(!newFile.exists()){
                newFile.mkdir();
            }
            File file = new File(context.getFilesDir(),"ECULTURE_"+time+".csv");
            Uri path = FileProvider.getUriForFile(context,"it.gangoffive.eculture",file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            intent.putExtra(Intent.EXTRA_STREAM, path);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent,"Excel Data"));
        } catch (Exception e) {
            showErrorDialog(R.string.import_error);
        }
    }

    /**
     *
     * Mostra una finestra di dialog in caso di errori o warnings per l'utente
     *
     * @param msgRef int
     *
     */
    private void showErrorDialog(int msgRef){
        new MaterialAlertDialogBuilder(ExportCsvActivity.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
            .setTitle(R.string.caution)
            .setMessage(msgRef)
            .setIcon(R.drawable.ic_baseline_error_outline_24)
            .setPositiveButton(R.string.wiz_no_selection_close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            })
            .show();
    }


    /**
     *
     * Verifica se il permesso di scrittura è stato concesso
     *
     * @return boolean
     *
     */
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
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
    private void requestPermission() {
        final int PERMESSO_EXPORT = 0;
        AlertDialog alert = null;
        SharedPreferences spCheck = getPreferences(Context.MODE_PRIVATE);
        int check = spCheck.getInt("askedtimesExternalPermission", 0);
        //Caso in cui i permessi vengono chiesti per la prima volta
        if(check == 0 || check != 0 && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("askedtimesExternalPermission", 1);
            editor.apply();
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(ExportCsvActivity.this);
            builder.setMessage(getString(R.string.export_permission_description)).setTitle(getString(R.string.export_permission_title)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                    ActivityCompat.requestPermissions(ExportCsvActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMESSO_EXPORT);
                }
            });
            alert = builder.create();
            alert.show();
        }
        //Caso in cui l'opzione di don't ask me again viene spuntata. Per ricordare all'utente che ha scelto l'opzione di non richiesta del permesso
        else{
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(ExportCsvActivity.this);
            builder.setMessage(getString(R.string.export_dontaskpermission_description)).setTitle(getString(R.string.export_permission_title)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
     *
     * Inizializza il Listener sull'input box di ricerca
     *
     */
    private void __initSearchInputListener(){
        term = findViewById(R.id.search_tours);
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tourViewModel.loadTours(charSequence, userRole, userUid);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        term.addTextChangedListener(watcher);
    }


    /**
     *
     * Inizializza il Listener alla selezione del Tab
     *
     */
    private void __initTabSelectionListener(){
        tabs = findViewById(R.id.export_tab_container);
        if (userRole != null && userRole.equals("curator")) {
            tabListener = new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    LinearLayout tabPanelExport = findViewById(R.id.tab_panel_export);
                    LinearLayout tabPanelImport = findViewById(R.id.tab_panel_import);
                    if (String.valueOf(tab.getText()).equals(getString(R.string.tab_export_title))) {
                        tabPanelImport.setVisibility(View.GONE);
                        tabPanelExport.setVisibility(View.VISIBLE);
                        tourViewModel.loadTours("", userRole, userUid);
                    } else {
                        tabPanelExport.setVisibility(View.GONE);
                        tabPanelImport.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            };
            tabs.addOnTabSelectedListener(tabListener);
        } else {
            tabs.setVisibility(View.GONE);
        }
    }


    /**
     *
     * Inizializza il Listener sul pulsante di export
     *
     */
    private void __initExportButtonListener(){
        Button exportBtn = findViewById(R.id.export_btn);
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ViewGroup container = findViewById(R.id.recycler_view);
                ArrayList<String> toursSelected = new ArrayList<>();

                for (int i = 0; i < container.getChildCount(); i++) {
                    View v = container.getChildAt(i);
                    RadioButton radio = v.findViewById(R.id.tour_checklist_radio);
                    if (radio.isChecked()){
                        toursSelected.add((String) radio.getText());
                    }
                }
                if (toursSelected.size() == 0){
                    showErrorDialog(R.string.wiz_no_selection);
                } else {
                    data = new StringBuilder();

                    tourViewModel.getTours("", userRole, userUid).observe(ExportCsvActivity.this, new Observer<List<TourModel>>() {
                        @Override
                        public void onChanged(@Nullable List<TourModel> list) {
                            data.append("");
                            for (int i = 0; i < list.size(); i++) {
                                if (counter == 0 && toursSelected.contains(list.get(i).getId())) {
                                    data.append(
                                            "\"" + "tours" + "\","
                                            + "\"" + list.get(i).getId().replace("\"","'") + "\","
                                            + "\"" + list.get(i).getTitle().replace("\"","'") + "\","
                                            + "\"" + list.get(i).getSubtitle().replace("\"","'") + "\","
                                            + "\"" + list.get(i).getDescription().replace("\"","'") + "\","
                                            + "\"" + list.get(i).getStructure().replace("\"","'") + "\","
                                            + "\"" + getStringFromList(list.get(i).getRooms()).replace("\"","'") + "\","
                                            + "\"" + getStringFromList(list.get(i).getPlaces()).replace("\"","'") + "\","
                                            + "\"" + list.get(i).getCreatedBy().replace("\"","'") + "\"" + "\n");
                                }
                            }
                            if(checkPermission()){
                                exportDataToCSV(data);
                            }else{
                                requestPermission();
                            }
                            counter++;
                        }
                    });
                }
            }
        });
    }


    /**
     *
     * Inizializza il Listener sul pulsante di import
     *
     */
    private void __initImportButtonListener(){
        Button exportBtn = findViewById(R.id.import_btn);
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(ExportCsvActivity.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                    .setTitle(R.string.caution)
                    .setMessage(R.string.import_warning)
                    .setIcon(R.drawable.ic_baseline_error_outline_24)
                    .setNegativeButton(R.string.import_dismiss_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    })
                    .setPositiveButton(R.string.import_continue_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("text/csv");
                            startActivityForResult(Intent.createChooser(intent, "Open CSV"), 1);
                        }
                    })
                    .show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView error = findViewById(R.id.errorAlert);
        if (resultCode == RESULT_OK) {
            importCsvFile(data.getData());
            error.setVisibility(View.GONE);
        } else {
            error.setVisibility(View.VISIBLE);
        }
    }


    /**
     *
     * Legge il contenuto di un file CSV
     *
     * @param file Uri
     */
    private void importCsvFile(Uri file){
        try {
            InputStream inputStream = getContentResolver().openInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            CSVReader dataRead = new CSVReader(reader);
            String[] line = null;
            while((line = dataRead.readNext()) != null) {
                writeOnDb(line);
            }
            reader.close();
        } catch (Exception e) {
            showErrorDialog(R.string.import_error);
        }


    }


    /**
     *
     * Scrive nel database un set di dati
     *
     * @param data String[]
     *
     */
    private void writeOnDb(String[] data){
        String slice = data[0];
        DatabaseReference dbDataRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        StorageReference dbImgRef = FirebaseStorage.getInstance().getReference();
        TextView error = findViewById(R.id.errorAlert);
        TextView success = findViewById(R.id.successAlert);

        if (validateRequiredFields(data) > 0){
            error.setVisibility(View.VISIBLE);
            success.setVisibility(View.GONE);
        } else {
            switch (slice){
                case "tours":
                    TourModel tour = new TourModel(
                            data[1],
                            data[2],
                            data[3],
                            data[4],
                            data[5],
                            getRoomsFromString(data[6]),
                            getPlacesFromString(data[7]),
                            data[8]
                    );
                    dbDataRef.child(slice).child(data[1]).setValue(tour);
                    break;
                case "structures":
                    StructureModel structure = new StructureModel(
                            data[1],
                            data[2],
                            data[3],
                            data[4],
                            data[5],
                            data[6],
                            data[7],
                            data[8]
                    );
                    dbDataRef.child(slice).child(data[1]).setValue(structure);
                    break;
                case "rooms":
                    RoomModel room = new RoomModel(
                            data[1],
                            data[2],
                            data[3],
                            data[4],
                            data[5].toLowerCase().equals("true"),
                            data[6],
                            data[7]
                    );
                    dbDataRef.child(slice).child(data[1]).setValue(room);
                    break;
                case "places":
                    PlaceModel place = new PlaceModel(
                            data[1],
                            data[2],
                            data[3],
                            data[4],
                            data[5],
                            data[6],
                            data[7],
                            data[8].toLowerCase().equals("true"),
                            data[9]
                    );
                    dbDataRef.child(slice).child(data[1]).setValue(place);
                    break;
                case "quiz":
                    QuizModel quiz = new QuizModel(
                            data[2],
                            data[3],
                            data[4],
                            data[5].toLowerCase().equals("true"),
                            data[6].toLowerCase().equals("true"),
                            data[7].toLowerCase().equals("true"),
                            data[8],
                            data[9].toLowerCase().equals("true")
                    );
                    dbDataRef.child(slice).child(data[1]).setValue(quiz);
                    break;
                case "images":
                    // TODO connection to web service
                    break;
            }

            error.setVisibility(View.GONE);
            success.setVisibility(View.VISIBLE);
        }


    }


    /**
     *
     * Verifica la presenza di elementi obbligatori in un set di dati
     *
     * @param data String[]
     * @return int
     *
     */
    private int validateRequiredFields(String[] data){
        int counter = 0;
        switch (data[0]){
            case "quiz":
            case "places":
                if (data[1] == null || data[2] == null || data[3] == null || data[4] == null  || data[5] == null || data[6] == null || data[7] == null || data[8] == null || data[9] == null){
                    counter++;
                }
                break;
            case "structures":
                if (data[1] == null || data[2] == null || data[3] == null || data[4] == null  || data[5] == null || data[6] == null || data[7] == null || data[8] == null){
                    counter++;
                }
                break;
            case "tours":
            case "rooms":
                if (data[1] == null || data[2] == null || data[3] == null || data[4] == null  || data[5] == null || data[6] == null || data[7] == null){
                    counter++;
                }
                break;
            case "images":
                if (data[1] == null || data[2] == null){
                    counter++;
                }
        }
        return counter;
    }


    /**
     *
     * Converte una stringa nel tipo di dato corrispondente al campo Rooms della classe TourModel
     *
     * @param s String
     * @return Arraylist
     *
     */
    public ArrayList<String> getRoomsFromString(String s){
        ArrayList<String> arrayList = new ArrayList<>();
        String[] parts = s.substring(1, s.length() - 1).split("-");
        for (int i = 0; i < parts.length; i++){
            arrayList.add(parts[i]);
        }
        return arrayList;
    }


    /**
     *
     * Converte una stringa nel tipo di dato corrispondente al campo Places della classe TourModel
     *
     * @param s String
     * @return List
     *
     */
    public List<HashMap<String, ArrayList<String>>> getPlacesFromString(String s){
        ArrayList<HashMap<String, ArrayList<String>>> list = new ArrayList<>();
        ArrayList<String> roomid = new ArrayList<>();
        ArrayList<String> places = new ArrayList<>();
        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
        String[] parts = s.substring(1, s.length() - 1).split("#ID");
        for (int i = 0; i < parts.length; i++){
            String[] subparts = parts[i].split("#PLACES");
            int counter = 0;
            for (int j = 0; j < subparts.length; j++){
                counter++;
                if (counter % 2 == 0){
                    roomid = new ArrayList<>();
                    places = new ArrayList<>();
                    hashMap = new HashMap<>();
                    roomid.add(subparts[j - 1]);
                    String[] placesId = subparts[j].substring(1, subparts[j].length() - 1).split("# ");
                    for (int k = 0; k < placesId.length; k++){
                        places.add(placesId[k]);
                    }
                    hashMap.put("roomid", roomid);
                    hashMap.put("places", places);
                    list.add(hashMap);
                }
            }
        }
        return list;
    }


    /**
     *
     * Converte una lista in Stringa applicando un pattern
     *
     * @param list Arraylist
     * @return String
     *
     */
    public String getStringFromList(ArrayList<?> list){
        StringBuilder s = new StringBuilder();
        s.append('[');
        for (int i = 0; i < list.size(); i++){
            if (list.get(i) instanceof String){
                s.append(list.get(i));
                if (i != list.size() - 1){
                    s.append('-');
                }
            } else if (list.get(i) instanceof HashMap){
                HashMap<String, ArrayList<String>> hashMap = (HashMap<String, ArrayList<String>>) list.get(i);
                s.append("#ID");
                s.append(hashMap.get("roomid").get(0));
                s.append("#PLACES");
                s.append(hashMap.get("places").toString().replace(',','#'));
            }

        }
        s.append(']');
        return s.toString();
    }

}