package it.gangoffive.eculture.ui.places.adapters;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import it.gangoffive.eculture.DetailsPlaceActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.PlaceModel;


public class PlacesIconRecyclerViewAdapter extends RecyclerView.Adapter<PlacesIconRecyclerViewAdapter.ViewHolder> {

    private List<PlaceModel> localDataSet = new ArrayList<PlaceModel>();
    private final int maxStringLength = 14;

    /**
     * Dichiara una classe ViewHolder che fa riferimento agli elementi del HomeFragment
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView image;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.idPlaceName);
            image = (ImageView) view.findViewById(R.id.place_home_image);
        }

        public TextView getTextView() {
            return textView;
        }
        public ImageView getImage() {
            return image;
        }
    }

    /**
     * Inizializza la variabile "localDataSet" perchè contenga gli elementi con cui popolare la recycler view.
     *
     * @param dataSet String[]
     */
    public PlacesIconRecyclerViewAdapter(List<PlaceModel> dataSet) {
        localDataSet = dataSet;
    }


    /**
     *  Effettua l'inflate del layout da utilizzare per gli elementi della recycler view
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ui_icon_place, viewGroup, false);

        return new ViewHolder(view);
    }

    /**
     * Inserisce i dati riguardo i place all'interno del view holder nella recycler view
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        int spaceFlag = 0;
        String name = localDataSet.get(position).getTitle();

        if (localDataSet.get(position).getId() != null) {
            //Controllo lunghezza stringa per il trim
            if(name.length() > maxStringLength)
            {
                name = name.substring(0, maxStringLength) + "...";
            }
            //Controllo lunghezza per aggiunta char
            for(int i = name.length(); i<maxStringLength; i++)
            {
                if(name.length() < maxStringLength && name.length() % 2 == 0)
                {
                    name = " " + name + " ";
                }
                else if(name.length() < maxStringLength && name.length() % 2 != 0)
                {
                    if(spaceFlag == 0)
                    {
                        name = name + " ";
                        spaceFlag = 1;
                    }
                    else
                    {
                        name = " " + name;
                        spaceFlag = 0;
                    }
                }
            }


            // Imposta il listener del click perchè mostri il dettaglio dell'elemento selezionato.
            viewHolder.getTextView().setText(name);
            setImage(viewHolder.image, localDataSet.get(position).getId());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(view.getContext(), DetailsPlaceActivity.class);
                    intent.putExtra("ID", localDataSet.get(position).getId());
                    view.getContext().startActivity(intent);

                }
            });

        } else {
             setNoDataWarning(viewHolder);
        }

    }


    // Restituisce la dimensione del dataset (invocato dal layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    /**
     *
     * Imposta un messaggio di errore in caso di dati mancanti
     *
     * @param holder PlaceCardViewHolder
     *
     */
    private void setNoDataWarning(@NonNull PlacesIconRecyclerViewAdapter.ViewHolder holder){
        holder.textView.setText(R.string.wiz_no_data_msg);
        holder.textView.setTextColor(Color.parseColor("#842029"));
        holder.textView.setMaxWidth(Integer.MAX_VALUE);
        holder.image.setVisibility(View.GONE);
    }



    /**
     *
     * Imposta una immagine nella vista prendendola dal db
     *
     * @param imgContainer ImageView
     * @param id String
     *
     */
    private void setImage(ImageView imgContainer, String id){
        StorageReference storagePlaceRef = FirebaseStorage.getInstance().getReference().child("places");
        storagePlaceRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] name = item.getName().split("\\.");
                    if (name[0].equalsIgnoreCase(id)) {
                        final long ONE_MEGABYTE = 1024 * 1024 * 10;
                        item.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                imgContainer.setImageBitmap(Bitmap.createBitmap(bmp));
                            }
                        });
                    }
                }
            }
        });
    }




}
