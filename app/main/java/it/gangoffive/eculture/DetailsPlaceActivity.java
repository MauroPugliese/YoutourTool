package it.gangoffive.eculture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import it.gangoffive.eculture.model.PlaceModel;
import it.gangoffive.eculture.ui.add.AddPlaceActivity;
import it.gangoffive.eculture.viewmodel.PlaceViewModel;

public class DetailsPlaceActivity extends AppCompatActivity {

    private PlaceModel place;
    private PlaceViewModel placeViewModel;
    private String userRole;
    private String userUid;
    private String placeID;
    private boolean found = false;
    private android.app.AlertDialog alert = null;
    private DatabaseReference dataBaseRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            userUid = currentUser.getUid();
            SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
            userRole = sp.getString("type", null);

            placeID = String.valueOf(getIntent().getExtras().getString("ID"));
            if (placeID == null) {
                onBackPressed();
            } else {
                setContentView(R.layout.activity_detail_place);

                setDataToView();

                Toolbar myToolbar = findViewById(R.id.topAppBar);
                myToolbar.setBackgroundColor(getResources().getColor(userRole.equals("curator") ? R.color.curatorcolor : R.color.touristcolor));
                setSupportActionBar(myToolbar);

            }

        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setDataToView();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userRole.equals("curator")){
            super.onCreateOptionsMenu(menu);
            if (menu instanceof MenuBuilder) {
                ((MenuBuilder) menu).setOptionalIconsVisible(true);
            }
            getMenuInflater().inflate(R.menu.toolbar_detail_menu, menu);
        }
        return true;
    }

    /**
     * Gestione della modifica e eliminazione dell'elemento selezionato.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Intent intent = new Intent(this, AddPlaceActivity.class);
                intent.putExtra("ID", place.getId());
                startActivity(intent);
                return true;
            case R.id.delete:
                AlertDialog myDialog = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                        .setTitle(R.string.delete_dialog_title)
                        .setMessage(getString(R.string.delete_dialog_msg))
                        .setIcon(R.drawable.ic_baseline_error_outline_24)
                        .setNegativeButton(R.string.delete_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setPositiveButton(R.string.delete_dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference placeRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places");
                                dataBaseRef.child("quiz").child(placeID).removeValue();
                                placeRef.child(place.getId()).setValue(null);
                                deleteImage(place.getId(), "places");
                                showFeedback();
                                onBackPressed();
                            }
                        })
                        .show();
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Metodo che si occupa del recupero dei dati dal DB, relativi all'elemnto selezionato.
     * In particolare, viene gestito il caso in cui un curatore scansiona un QR di un place che non ha creato lui.
     */

    private void setDataToView(){
        placeViewModel = new ViewModelProvider(DetailsPlaceActivity.this).get(PlaceViewModel.class);
        placeViewModel.getPlaces("", userRole, userUid).observe(DetailsPlaceActivity.this, new Observer<List<PlaceModel>>() {
            @Override
            public void onChanged(@Nullable List<PlaceModel> list) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equals(placeID)) {
                        place = list.get(i);
                        setCoverImage();
                        setQrCode();
                        setQuiz();
                        TextView title = findViewById(R.id.place_detail_title);
                        TextView description = findViewById(R.id.place_detail_description);
                        TextView author = findViewById(R.id.author);
                        TextView period = findViewById(R.id.period);
                        SwitchMaterial sm = findViewById(R.id.place_detail_title);
                        sm.setChecked(place.getIsOpen());
                        sm.setClickable(false);
                        period.setText(getResources().getString(R.string.period) + " : " + place.getPeriod());
                        author.setText(getResources().getString(R.string.author) + " : " + place.getAuthor());
                        title.setText(place.getTitle());
                        description.setText(place.getDescription());
                        getSupportActionBar().setTitle(place.getTitle());
                        break;
                    }
                    if(i==list.size()-1){
                        android.app.AlertDialog.Builder builder;
                        builder = new android.app.AlertDialog.Builder(DetailsPlaceActivity.this);
                        builder.setMessage(getString(R.string.place_not_yours_decription)).setTitle(getString(R.string.place_not_yours_title)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                onBackPressed();

                            }
                        });
                        alert = builder.create();
                        alert.show();
                    }
                }

            }

        });


        placeViewModel.loadPlaces("", userRole, userUid);
    }


    private void setCoverImage(){
        ImageView iv = findViewById(R.id.place_detail_image);
        iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.no_image));
        StorageReference storagePlaceRef = FirebaseStorage.getInstance().getReference().child("places");
        storagePlaceRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] name = item.getName().split("\\.");
                    if (name[0].equalsIgnoreCase(place.getId())) {
                        final long TEN_MEGABYTE = 1024 * 1024 * 10;
                        item.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                iv.setImageBitmap(bmp);
                            }
                        });
                    }
                }
            }
        });
    }

    public void deleteImage(String id, String type) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(type);
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] name = item.getName().split("\\.");
                    if (name[0].equalsIgnoreCase(id)) {
                        StorageReference ref = FirebaseStorage.getInstance().getReference();
                        ref.child(type).child(item.getName()).delete();
                        break;
                    }
                }
            }
        });
    }


    /**
     * Metodo responsabile della creazione di un QR attraverso la lettura delle informazioni relative al place.
     */
    private void setQrCode() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(concatInfoQr(place.getId(), place.getTitle(), "places"), BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ((ImageView) findViewById(R.id.place_detail_qr)).setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Metodo per la concatenazione delle info nel qr
     * Esempio: places-1-Gioconda
     *
     * @param id
     * @param title
     * @param slice String rappresenta il tipo di oggetto che il qr descrive: strucutres, places, tours, rooms
     * @return
     */
    public String concatInfoQr(String id, String title, String slice) {
        final String SEPARATOR = "-";
        String encodedInfo;

        encodedInfo = slice + SEPARATOR + id + SEPARATOR + title;

        return encodedInfo;
    }

    /**
     * Metodo responsabile della visualizzazione del/i game/s associato/i al place.
     * Nel caso in cui vi è l'assenza di uno o l'altro (o entrambi), viene mostrato un messaggio appropriato.
     * Gestione della lingua a run-time.
     * Gestione della tipologia di utenti (curatore può vedere anche le risposte corrette al contrario del turista  che
     * potrà visualizzare esclusivamente le domande).
     */
    public void setQuiz(){
        SharedPreferences sp = getSharedPreferences(getString(R.string.pref_file), MODE_PRIVATE);
        String tipo = sp.getString("type", "tourist");

        dataBaseRef.child("quiz").child(placeID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                TextView quiz = findViewById(R.id.quiz);
                TextView puzzle = findViewById(R.id.puzzle);
                int flag = 0;
                String answString;
                HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                if(hm.get("quest1").equals("null") && hm.get("quest2").equals("null") && hm.get("quest3").equals("null")){
                    quiz.setText(getString(R.string.quiz_noQuiz_forthisplace));
                    flag = 1;
                }
                if(tipo.equals("curator") && flag == 0){
                    String risp1;
                    String risp2;
                    String risp3;
                    if(Locale.getDefault().getDisplayLanguage().equals("English")){

                        answString = "\nAnswer: ";
                        if(String.valueOf(hm.get("answ1")).equals("true")){
                            risp1 = "True";
                        }else{
                            risp1 = "False";
                        }
                        if(String.valueOf(hm.get("answ2")).equals("true")){
                            risp2 = "True";
                        }else{
                            risp2 = "False";
                        }
                        if(String.valueOf(hm.get("answ3")).equals("true")){
                            risp3 = "True";
                        }else{
                            risp3 = "False";
                        }

                    }else{
                        answString = "\nRisposta: ";
                        if(String.valueOf(hm.get("answ1")).equals("true")){
                            risp1 = "Vero";
                        }else{
                            risp1 = "Falso";
                        }
                        if(String.valueOf(hm.get("answ2")).equals("true")){
                            risp2 = "Vero";
                        }else{
                            risp2 = "Falso";
                        }
                        if(String.valueOf(hm.get("answ3")).equals("true")){
                            risp3 = "Vero";
                        }else{
                            risp3 = "Falso";
                        }
                    }

                    quiz.setText(getString(R.string.quiz_questEansw_selected)+ "\n\n" + hm.get("quest1") + answString + risp1 + "\n\n" + hm.get("quest2") + answString + risp2 + "\n\n" + hm.get("quest3") + answString + risp3);

                }else if(tipo.equals("tourist") && flag == 0){

                    quiz.setText(getString(R.string.quiz_quest_selected) + "\n\n" + hm.get("quest1") + "\n\n" + hm.get("quest2") +"\n\n" + hm.get("quest3"));
                }

                if(String.valueOf(hm.get("hasPuzzle")).equals("true")){
                    puzzle.setText(getString(R.string.place_has_puzzle));
                }else{
                    puzzle.setText(getString(R.string.place_hasNOT_puzzle));
                }

            }
        });

    }

    private void showFeedback(){
        Toast feedback = Toast.makeText(this, getString(R.string.toast_delete), Toast.LENGTH_LONG);
        feedback.show();
    }



}
