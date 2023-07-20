package it.gangoffive.eculture.ui.add;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import it.gangoffive.eculture.DetailsStructureActivity;
import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.CityModel;
import it.gangoffive.eculture.model.StructureModel;

public class AddStructureActivity extends AppCompatActivity {
    private static final ArrayList<CityModel> cities = new ArrayList<>();
    private static final ArrayList<String> regions = new ArrayList<>();
    private static DatabaseReference mRef = null;

    private static int maxStructures;
    private boolean create = false;

    AutoCompleteTextView regionA;
    AutoCompleteTextView provinceA;
    AutoCompleteTextView citiesA;
    TextInputEditText structET;
    TextInputEditText addressET;
    TextInputEditText scheduleET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.theme);
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add_structure);
        regionA = findViewById(R.id.region);
        provinceA = findViewById(R.id.province);
        citiesA = findViewById(R.id.city);
        structET = findViewById(R.id.structures);
        addressET = findViewById(R.id.address);
        scheduleET = findViewById(R.id.schedule);
        Activity a = this;
        CommonAdd.imageListener(a);

        mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 1; i < Integer.MAX_VALUE; i++) {
                    if (!snapshot.hasChild(String.valueOf(i))) {
                        maxStructures = i;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button next = findViewById(R.id.next);
        String id = getIntent().getStringExtra("ID");
        if (id != null) {
            getSupportActionBar().setTitle(R.string.edit_structure);
            FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    HashMap<String, String> hm = (HashMap<String, String>) task.getResult().getValue();
                    addressET.setText(hm.get("address"));
                    structET.setText(hm.get("name"));
                    scheduleET.setText(hm.get("schedule"));
                    regionA.setText(hm.get("region"), false);
                    provinceA.setText(hm.get("province"), false);
                    citiesA.setText(hm.get("city"), false);
                    citiesA.performClick();
                    initEditCities(hm.get("region"), hm.get("province"));
                    ((Button) findViewById(R.id.next)).setText(R.string.save);
                    CommonAdd.loadImage(a, "structures", id);
                }
            });
        }else{
            getSupportActionBar().setTitle(R.string.add_structure);
        }
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean one_at_least;
                one_at_least = CommonAdd.checkFields(a);
                if (!one_at_least && id == null) {
                    create = true;
                    save(String.valueOf(maxStructures), true);
                } else if (!one_at_least && id != null) {
                    save(id, false);
                } else {
                    ((ScrollView) findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_UP);
                    ((ScrollView) findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_UP);
                }
            }
        });
        initCities();
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.list_item, regions);

        regionA.setAdapter(adapter);
        regionA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayAdapter ad = new ArrayAdapter(getApplicationContext(), R.layout.list_item,
                        cities.stream().filter(city -> city.getRegion().contains(regions.get(i))).map(city -> city.province).distinct().collect(Collectors.toList()));
                provinceA.setText("");
                citiesA.setText("");
                provinceA.setAdapter(ad);
                citiesA.setAdapter(null);
                provinceA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ArrayAdapter arrad = new ArrayAdapter(getApplicationContext(), R.layout.list_item,
                                cities.stream().filter(city -> city.getProvince().contains(adapterView.getItemAtPosition(i).toString())).map(city -> city.city_name).distinct().collect(Collectors.toList()));
                        citiesA.setText("");
                        citiesA.setAdapter(arrad);
                    }
                });
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CommonAdd.activityResult(this, requestCode, resultCode, data);
    }


    /**
     * Utilizzato per caricare dal file presente in assets/italy.xml tutti i dati relativi alle città (compreso di provincia e regione)
     */
    public void initCities() {
        if (cities.size() == 0) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                AssetManager assetManager = getAssets();
                Document doc = db.parse(assetManager.open("italy.xml"));
                doc.getDocumentElement().normalize();
                NodeList list = doc.getElementsByTagName("element");
                for (int temp = 0; temp < list.getLength(); temp++) {

                    Node node = list.item(temp);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        if (!regions.contains(element.getElementsByTagName("regione").item(0).getTextContent()))
                            regions.add(element.getElementsByTagName("regione").item(0).getTextContent());
                        cities.add(new CityModel(element.getElementsByTagName("regione").item(0).getTextContent(), element.getElementsByTagName("provincia").item(0).getTextContent(), element.getElementsByTagName("comune").item(0).getTextContent()));
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
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

    /**
     * Metodo che viene invocato quando l'activity viene chiamata per effettuare una modifica.
     * Imposta gli adapter sulla regione e provincia precedentemente indicata
     * @param region Regione utilizzata per filtrare le province
     * @param province Provincia utilizzata per filtrare le città
     */
    public void initEditCities(String region, String province) {
        ArrayAdapter ad = new ArrayAdapter(getApplicationContext(), R.layout.list_item,
                cities.stream().filter(city -> city.getRegion().contains(region)).map(city -> city.province).distinct().collect(Collectors.toList()));
        provinceA.setAdapter(ad);
        ArrayAdapter arrad = new ArrayAdapter(getApplicationContext(), R.layout.list_item,
                cities.stream().filter(city -> city.getProvince().contains(province)).map(city -> city.city_name).distinct().collect(Collectors.toList()));
        citiesA.setAdapter(arrad);
    }

    /**
     * Utilizzato per salvare tutti i dati relativi alla struttura (informazioni e immagine)
     *
     * @param id della struttura che si va ad aggiungere/modificare
     * @param cont flag utilizzato per inviare all'utente al dettaglio della struttura (se ha solamente modificato) o alla creazione
     *             di una stanza (se ha appena creato la struttura)
     */
    public void save(String id, boolean cont) {
        String structure = structET.getText().toString();
        String address = addressET.getText().toString();
        String schedule = scheduleET.getText().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures");
        mRef.child(String.valueOf(id)).setValue(new StructureModel(String.valueOf(id), structure, address, regionA.getText().toString(), provinceA.getText().toString(), citiesA.getText().toString(), schedule, user.getUid()));
        if (CommonAdd.getIm() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference refe;
            if (!TextUtils.isEmpty(CommonAdd.getFilename())) {
                refe = storageRef.child("structures/" + CommonAdd.getFilename());
                refe.delete();
                CommonAdd.setFilename("");
            }
            refe = storageRef.child("structures/" + (id) + "." + CommonAdd.getExtension());
            CommonAdd.imageClick(refe);
        }
        Intent intent;
        if (cont) {
            intent = new Intent(getApplicationContext(), AddRoomActivity.class);
            intent.putExtra("structure", structure);
            finish();
            showFeedback();
            startActivity(intent);
        } else {
            finish();
            showFeedback();
            onBackPressed();
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
