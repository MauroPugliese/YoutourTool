package it.gangoffive.eculture.ui.add;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import it.gangoffive.eculture.R;

public class CommonAdd {
    private static final ArrayList<HashMap<String, String>> structures = new ArrayList<HashMap<String, String>>();
    private static final ArrayList<HashMap<String, String>> rooms = new ArrayList<HashMap<String, String>>();
    public static String filename = "";
    private static ArrayAdapter structuresAd;
    private static AutoCompleteTextView structuresA;
    private static ArrayAdapter roomAd;
    private static AutoCompleteTextView roomA;
    private static ImageView im;
    private static String extension = "";
    private static boolean imageInserted=false;

    public static ArrayList<HashMap<String, String>> getStructures() {
        return structures;
    }

    public static AutoCompleteTextView getStructuresA() {
        return structuresA;
    }

    public static ArrayAdapter getStructuresAd() {
        return structuresAd;
    }

    public static ArrayList<HashMap<String, String>> getRooms() {
        return rooms;
    }

    public static ArrayAdapter getRoomAd() {
        return roomAd;
    }

    public static void setRoomAd(ArrayAdapter roomAd) {
        CommonAdd.roomAd = roomAd;
    }

    public static AutoCompleteTextView getRoomA() {
        return roomA;
    }

    public static void setRoomA(AutoCompleteTextView roomA) {
        CommonAdd.roomA = roomA;
    }

    public static ImageView getIm() {
        return im;
    }

    public static String getExtension() {
        return extension;
    }

    /**
     * Utilizzato per caricare nell'Adapter tutte le strutture del curatore
     *
     * @param a Activity (per ottenere il context)
     */
    public static void initStructures(Activity a) {
        structures.clear();
        structuresA = a.findViewById(R.id.structures);
        DatabaseReference mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("structures");
        mRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    ArrayList<HashMap<String, String>> al = (ArrayList) task.getResult().getValue();
                    if (al != null) {
                        for (HashMap<String, String> hm : al) {
                            if (hm != null)
                                if (hm.get("createdBy").equalsIgnoreCase(FirebaseAuth.getInstance().getUid()))
                                    CommonAdd.getStructures().add(hm);
                        }
                    }
                    structuresAd = new ArrayAdapter(a.getApplicationContext(), R.layout.list_item, CommonAdd.getStructures().stream().filter(room -> room != null).map(room -> room.get("name")).collect(Collectors.toList()));
                    structuresA.setAdapter(structuresAd);
                    structuresAd.notifyDataSetChanged();
                }
                if (a.getClass().getSimpleName().equalsIgnoreCase("AddPlaceActivity")) {
                    structuresA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            initRooms(a, structures.get(i).get("id"));
                            if (roomA != null)
                                roomA.setText("");
                        }
                    });
                }

                if (CommonAdd.getStructures().size() == 0) {
                    alertAdd(a, R.string.caution, R.string.no_structures, 0);
                }
            }
        });
    }

    /**
     * Utilizzato per caricare nell'Adapter tutte le stanze della struttura scelta
     *
     * @param a Activity (per ottenere il Context)
     * @param id Id della struttura scelta
     */
    public static void initRooms(Activity a, String id) {
        rooms.clear();
        DatabaseReference mRef = FirebaseDatabase.getInstance("https://e-culture-tool-b3e92-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("rooms");
        mRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    ArrayList<HashMap<String, String>> al = (ArrayList) task.getResult().getValue();
                    for (HashMap<String, String> hm : al) {
                        if (hm != null)
                            if (id.equalsIgnoreCase(hm.get("structure_id")))
                                rooms.add(hm);
                    }
                    roomAd = new ArrayAdapter(a.getApplicationContext(), R.layout.list_item, rooms.stream().filter(room -> room != null).map(room -> room.get("name")).collect(Collectors.toList()));
                    roomA.setAdapter(roomAd);
                    roomAd.notifyDataSetChanged();

                }
                if (rooms.size() == 0) {
                    alertAdd(a, R.string.caution, R.string.no_rooms, 0);
                }
            }
        });
    }

    /**
     * Messaggio da visualizzare all'utente in caso di imprevisti nell'aggiunta
     *
     * @param a Activity (per ottenere il Context)
     * @param title Titolo dell'alert da mostrare
     * @param message Messaggio dell'alert da mostrare
     * @param type Tipo se 1 indica che l'utente ha premuto il pulsante di back e quindi avvisare che potrebbe perdere le informazioni inserite
     *             e mostrare l'opzione per poter continuare l'aggiunta
     */
    public static void alertAdd(Activity a, int title, int message, int type) {
        AlertDialog.Builder ad = new MaterialAlertDialogBuilder(a, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        ad.setTitle(title);
        ad.setMessage(message);
        if (type == 1) {
            ad.setNegativeButton(a.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }
        ad.setNeutralButton(a.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        switch (type) {
                            case 1:
                                a.finish();
                                break;
                        }
                    }
                });
        ad.show();
    }

    /**
     * Controllo dinamico dei TextInputEditText e AutoCompleteTextView
     *
     * @param a Activity (per ottenere il Context)
     * @return Boolean che può essere Vero se ha trovato dei campi vuoti/non conformi, falso altrimenti.
     */
    public static boolean checkFields(Activity a) {
        boolean check = false;
        View layout = a.findViewById(R.id.scroll);
        ArrayList<View> touchables = layout.getTouchables();
        for (View text : touchables) {
            if (text instanceof TextInputEditText || text instanceof AutoCompleteTextView) {
                int ident = a.getResources().getIdentifier(a.getResources().getResourceName(text.getId()).substring(26).concat("_error"), "id", a.getApplicationContext().getPackageName());
                TextView tv = a.findViewById(ident);
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
     * Va a inserire nel Firebase Storage l'immagine selezionata e mostrare nell'ImageView
     *
     * @param refe Reference allo Storage
     */
    public static void imageClick(StorageReference refe) {
        if(imageInserted) {
            im.setDrawingCacheEnabled(true);
            im.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) im.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            refe.putBytes(data);
        }
    }

    /**
     * Listener quando si clicca sul bottone per la scelta dell'immagine. Avvia l'Intent(Chooser) di selezione
     *
     * @param a Activity (per ottenere il Context)
     */
    public static void imageListener(Activity a) {
        Button image = a.findViewById(R.id.select_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                a.startActivityForResult(Intent.createChooser(i, a.getResources().getString(R.string.select_image)), 1);
            }

        });
    }

    public static void activityResult(Activity a, int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri uriImage = data.getData();
                if (uriImage != null) {
                    im = a.findViewById(R.id.imageImported);
                    im.setVisibility(View.VISIBLE);
                    im.setImageURI(uriImage);
                    ContentResolver contentResolver = a.getContentResolver();
                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                    extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uriImage));
                    imageInserted=true;
                }
            }
        }
    }

    public static String getFilename() {
        return filename;
    }

    public static void setFilename(String filename) {
        CommonAdd.filename = filename;
    }

    /**
     * Va a ricercare e prelevare l'immagine (se presente) nello Storage e la mostra nell'ImageView
     * Metodo che viene invocato solo quando si effettua la modifica di un punto, stanza o struttura
     *
     * @param a Acitivity (per ottenere il Context)
     * @param type Tipo (places,rooms,structures) da dove andare a prelevare
     * @param id ID univoco con cui è chiamato il file
     */
    public static void loadImage(Activity a, String type, String id) {
        setFilename("");
        StorageReference storagePlaceRef = FirebaseStorage.getInstance().getReference().child(type);
        storagePlaceRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] name = item.getName().split("\\.");
                    if (name[0].equalsIgnoreCase(id)) {
                        setFilename(name[0]);
                        extension=name[1];
                        final long ONE_MEGABYTE = 1024 * 1024 * 10;
                        item.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                im = a.findViewById(R.id.imageImported);
                                im.setVisibility(View.VISIBLE);
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                im.setImageBitmap(bmp);
                            }
                        });
                    }
                }
            }
        });
    }
}
