package it.gangoffive.eculture.ui.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.stream.Collectors;

import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.PlaceModel;
import it.gangoffive.eculture.model.QuizModel;

public class AddPlaceActivity extends AppCompatActivity {


    private static DatabaseReference mRef = null;
    private static DatabaseReference qRef = null;

    private static int maxPlaces;
    private static int maxQuiz;

    private static String nameImg;
    //Variabili per la gestione del feedback
    private boolean create = false;


    //Variabili per load del quiz
    private static AutoCompleteTextView placeGame;
    private static TextInputEditText domanda1;
    private static TextInputEditText domanda2;
    private static TextInputEditText domanda3;
    private static RadioButton targetButton;
    private static String[] items = null;
    AutoCompleteTextView quizTextView;
    TextInputEditText place_name;
    TextInputEditText author_name;
    TextInputEditText period_t;
    TextInputEditText description_t;
    SwitchMaterial swMaterial;
    private ArrayAdapter<String> adapter;
    private boolean quizSelected = false;
    private boolean puzzleSelected = false;
    private String selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Activity a = this;

        CommonAdd.initStructures(a);
        CommonAdd.imageListener(a);

        place_name = findViewById(R.id.place);
        author_name = findViewById(R.id.author);
        period_t = findViewById(R.id.period);
        description_t = findViewById(R.id.description);
        swMaterial = findViewById(R.id.open);
        String id = getIntent().getStringExtra("ID");
        if (id != null) {
            FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                    place_name.setText(hm.get("title"));
                    author_name.setText(hm.get("author"));
                    period_t.setText(hm.get("period"));
                    description_t.setText(hm.get("description"));
                    swMaterial.setChecked(String.valueOf(hm.get("isOpen")).equalsIgnoreCase("true"));
                    getSupportActionBar().setTitle(R.string.edit_place);
                    FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms").child(hm.get("roomID")).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                            CommonAdd.initRooms(a, hm.get("structure_id"));
                            if (CommonAdd.getRoomA() != null)
                                CommonAdd.setRoomA(a.findViewById(R.id.roomA));
                            CommonAdd.getRoomA().setText(hm.get("name"));
                            FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures").child(hm.get("structure_id")).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                                    CommonAdd.getStructuresA().setText(hm.get("name"), false);
                                    CommonAdd.loadImage(a, "places", id);
                                    ((Button) findViewById(R.id.add)).setText(R.string.save);

                                    FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("quiz").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            HashMap<String, String> hmQuiz = (HashMap<String, String>) task.getResult().getValue();

                                            setQuiz(hmQuiz);

                                        }
                                    });

                                }
                            });

                        }
                    });
                }
            });
        }else{
            getSupportActionBar().setTitle(R.string.add_place);
        }

        mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 1; i < Integer.MAX_VALUE; i++) {
                    if (!snapshot.hasChild(String.valueOf(i))) {
                        maxPlaces = i;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        qRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("quiz");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 1; i < Integer.MAX_VALUE; i++) {
                    if (!snapshot.hasChild(String.valueOf(i))) {
                        maxQuiz = i;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        //Gestione della AutoCompleteTextView per i minigiochi
        items = new String[]{getString(R.string.minigame_none), "Puzzle", "Quiz", "Puzzle & Quiz"};
        placeGame = findViewById(R.id.placeGame);
        adapter = new ArrayAdapter<>(this, R.layout.list_item, items);
        placeGame.setAdapter(adapter);
        placeGame.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Gestione della selezione delle opzioni nell'autocomplete text view per i minigiochi.
             * Viene gestita la visibilità per il fragment contenente il form di aggiunta dei quiz.
             * Gestita la visibilità della textview in caso il puzzle sia selezionato.
             * @param adapterView
             * @param view
             * @param i
             * @param l
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItem = (String) adapterView.getItemAtPosition(i);
                View fragmentView = findViewById(R.id.gameFormQuiz);
                TextView puzzDescr = findViewById(R.id.puzzleDescription);

                switch (selectedItem) {
                    case "Quiz":
                        fragmentView.setVisibility(View.VISIBLE);
                        puzzDescr.setVisibility(View.GONE);
                        quizSelected = true;
                        break;
                    case "Puzzle":
                        fragmentView.setVisibility(View.GONE);
                        puzzDescr.setVisibility(View.VISIBLE);
                        puzzleSelected = true;

                        break;
                    case "Puzzle & Quiz":
                        fragmentView.setVisibility(View.VISIBLE);
                        puzzDescr.setVisibility(View.VISIBLE);
                        puzzleSelected = true;
                        quizSelected = true;
                        break;
                    default:
                        fragmentView.setVisibility(View.GONE);
                        puzzDescr.setVisibility(View.GONE);
                        puzzleSelected = false;
                        quizSelected = false;

                }
            }
        });


        Button add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            /**
             * Metodo responsabile per la gestione della modifica o della creazione di un place.
             * Primo passo, verifica che tutti i campi siano stati compilati attraverso il metodo "checkFields";
             * Secondo passo, verifica se si tratta di una creazione di un nuovo place (primo if) o se si tratta di
             * una modifica di un place esistente (else if).
             * @param view
             */
            @Override
            public void onClick(View view) {
                boolean one_at_least = false;
                one_at_least = CommonAdd.checkFields(a);
                if (!one_at_least && id == null) {
                    create = true;
                    save(String.valueOf(maxPlaces), String.valueOf(maxQuiz));
                } else if (!one_at_least && id != null) {
                    save(id, id);
                } else {
                    ((ScrollView) findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_UP);
                    ((ScrollView) findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_UP);
                }

            }
        });
        CommonAdd.setRoomAd(new ArrayAdapter(a.getApplicationContext(), R.layout.list_item, CommonAdd.getRooms().stream().filter(room -> room != null).map(room -> room.get("name")).collect(Collectors.toList())));
        CommonAdd.setRoomA(findViewById(R.id.roomA));
        CommonAdd.getRoomA().setAdapter(CommonAdd.getRoomAd());


    }

    /**
     * Utilizzato per salvare tutti i dati relativi al punto di interesse (informazioni, immagine e minigames)
     *
     * Il metodo gestisce 4 scenari:
     * 1. Scenario in cui nessun minigame viene selezionato;
     * 2. Scenario in cui viene creato un quiz ma non il puzzle;
     * 3. Scenario in cui viene creato un puzzle ma non il quiz;
     * 4. Scenari  in cui vengono creati entrambi i minigame disponibili.
     *
     * Negli scenari 1 e 3, viene creato un record nel DB con tutti gli attributi relativi al quiz avvalorati a null.
     * Gestione della lingua delle risposte dei quiz.
     *
     * @param id della stanza che si va ad aggiungere/modificare
     * @param idQuiz
     */
    public void save(String id, String idQuiz) {
        //Recupero informazioni relative al quiz. Controllo per verificare che l'opzione del minigioco non sia stata cambiata
        TextInputEditText domanda1 = findViewById(R.id.domanda1);
        TextInputEditText domanda2 = findViewById(R.id.domanda2);
        TextInputEditText domanda3 = findViewById(R.id.domanda3);

        boolean risposta1 = false;
        boolean risposta2 = false;
        boolean risposta3 = false;

        if (quizSelected) {
            qRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("quiz");
            RadioGroup domanda1Radio = findViewById(R.id.radioDomanda1);
            RadioGroup domanda2Radio = findViewById(R.id.radioDomanda2);
            RadioGroup domanda3Radio = findViewById(R.id.radioDomanda3);
            RadioButton selected;

            int checkedOption1 = domanda1Radio.getCheckedRadioButtonId();
            int checkedOption2 = domanda2Radio.getCheckedRadioButtonId();
            int checkedOption3 = domanda3Radio.getCheckedRadioButtonId();


            if (checkedOption1 != -1) {
                selected = findViewById(checkedOption1);
                if (selected.getText().toString().equals("Vero") || selected.getText().toString().equals("True")) {
                    risposta1 = true;
                }
            }
            if (checkedOption2 != -1) {
                selected = findViewById(checkedOption2);
                if (selected.getText().toString().equals("Vero") || selected.getText().toString().equals("True")) {
                    risposta2 = true;
                }
            }
            if (checkedOption3 != -1) {
                selected = findViewById(checkedOption3);
                if (selected.getText().toString().equals("Vero") || selected.getText().toString().equals("True")) {
                    risposta3 = true;
                }
            }
            if (puzzleSelected) {
                qRef.child(idQuiz).setValue(new QuizModel(domanda1.getText().toString(), domanda2.getText().toString(), domanda3.getText().toString(), risposta1, risposta2, risposta3, id, true));
            } else {
                qRef.child(idQuiz).setValue(new QuizModel(domanda1.getText().toString(), domanda2.getText().toString(), domanda3.getText().toString(), risposta1, risposta2, risposta3, id, false));
            }

            puzzleSelected = false;
            quizSelected = false;

        } else if (puzzleSelected) {

            qRef.child(idQuiz).setValue(new QuizModel("null", "null", "null", false, false, false, id, true));

        } else {

            qRef.child(idQuiz).setValue(new QuizModel("null", "null", "null", false, false, false, id, false));
        }
        //Salvo il resto delle informazioni del place
        String place = place_name.getText().toString();
        String author = author_name.getText().toString();
        String period = period_t.getText().toString();
        String description = description_t.getText().toString();
        boolean checked = swMaterial.isChecked();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("places");
        for (int i = 0; i < CommonAdd.getRooms().size(); i++) {
            if (CommonAdd.getRoomA().getText().toString().equalsIgnoreCase(CommonAdd.getRooms().get(i).get("name"))) {
                mRef.child(id).setValue(new PlaceModel(id, place, description, CommonAdd.getRooms().get(i).get("id"), author, period, user.getUid(), checked, selectedItem));
                if (CommonAdd.getIm() != null) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference refe;
                    String file=CommonAdd.getFilename();
                    if (!TextUtils.isEmpty(file)) {
                        refe = storageRef.child("places/" + file);
                        refe.delete();
                        CommonAdd.setFilename("");
                    }
                    refe = storageRef.child("places/" + id + "." + CommonAdd.getExtension());
                    CommonAdd.imageClick(refe);
                }
            }
        }
        finish();
        onBackPressed();
        showFeedback();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CommonAdd.activityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Inizializzo edit view per la modifica del quiz
    public void setQuiz(HashMap<String, String> hm) {
        placeGame = findViewById(R.id.placeGame);
        TextView puzzDescr = findViewById(R.id.puzzleDescription);
        if (!hm.get("quest1").equals("null") && !hm.get("quest2").equals("null") && !hm.get("quest3").equals("null")) {
            View fragment = findViewById(R.id.gameFormQuiz);
            fragment.setVisibility(View.VISIBLE);
            domanda1 = findViewById(R.id.domanda1);
            domanda2 = findViewById(R.id.domanda2);
            domanda3 = findViewById(R.id.domanda3);
            domanda1.setText(hm.get("quest1"));
            domanda2.setText(hm.get("quest2"));
            domanda3.setText(hm.get("quest3"));

            if (String.valueOf(hm.get("answ1")).equals("true")) {

                targetButton = findViewById(R.id.domanda1V);
                targetButton.setChecked(true);

            } else {
                targetButton = findViewById(R.id.domanda1F);
                targetButton.setChecked(true);
            }

            if (String.valueOf(hm.get("answ2")).equals("true")) {

                targetButton = findViewById(R.id.domanda2V);
                targetButton.setChecked(true);

            } else {
                targetButton = findViewById(R.id.domanda2F);
                targetButton.setChecked(true);
            }

            if (String.valueOf(hm.get("answ3")).equals("true")) {

                targetButton = findViewById(R.id.domanda3V);
                targetButton.setChecked(true);

            } else {
                targetButton = findViewById(R.id.domanda3F);
                targetButton.setChecked(true);
            }

            //Quiz e puzzle
            if (String.valueOf(hm.get("hasPuzzle")).equals("true")) {

                puzzDescr.setVisibility(View.VISIBLE);
                placeGame.setText("Puzzle & Quiz", false);
            } else {

                placeGame.setText("Quiz", false);
            }


        } else if (String.valueOf(hm.get("hasPuzzle")).equals("true")) { //Solo puzzle
            placeGame.setText("Puzzle", false);
            puzzDescr.setVisibility(View.VISIBLE);

        } else { //nessuno
            placeGame.setText(getString(R.string.minigame_none), false);
        }
    }

    private void showFeedback(){
        Toast feedback;
        if(create){
            feedback = Toast.makeText(this, getString(R.string.toast_add), Toast.LENGTH_LONG);
            feedback.show();
        } else {
            feedback = Toast.makeText(this, getString(R.string.toast_edit), Toast.LENGTH_LONG);
            feedback.show();
        }
    }


}
