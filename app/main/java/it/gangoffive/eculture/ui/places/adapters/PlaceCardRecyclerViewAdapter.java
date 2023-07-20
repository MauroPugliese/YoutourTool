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
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.gangoffive.eculture.DetailsPlaceActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.PlaceModel;

public class PlaceCardRecyclerViewAdapter extends RecyclerView.Adapter<PlaceCardRecyclerViewAdapter.PlaceCardViewHolder> {

    private List<PlaceModel> placeList;

    public PlaceCardRecyclerViewAdapter(List<PlaceModel> listData) {
        this.placeList = listData;
    }


    @NonNull
    @Override
    public PlaceCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_card_place, parent, false);
        return new PlaceCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceCardViewHolder holder, int position) {

        PlaceModel place = placeList.get(position);

        if (place.getId() != null) {
            holder.title.setText(place.getTitle());
            holder.author.setText(place.getAuthor());
            setImage(holder.image, place.getId());
        } else {
            setNoDataWarning(holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailsPlaceActivity.class);
                intent.putExtra("ID", place.getId());
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }


    public static class PlaceCardViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView author;
        public ImageView icon;
        public ImageView image;

        public PlaceCardViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.place_title);
            author = itemView.findViewById(R.id.place_author);
            icon = itemView.findViewById(R.id.goto_places_list_detail);
            image = itemView.findViewById(R.id.tour_place_image);
            title.setTextColor(Color.parseColor("#000000"));
            author.setTextColor(Color.parseColor("#787878"));

        }
    }


    /**
     *
     * Imposta un messaggio di errore in caso di dati mancanti
     *
     * @param holder PlaceCardViewHolder
     *
     */
    private void setNoDataWarning(@NonNull PlaceCardViewHolder holder){
        holder.title.setText(R.string.caution);
        holder.title.setTextColor(Color.parseColor("#842029"));
        holder.author.setText(R.string.wiz_no_data_msg);
        holder.author.setTextColor(Color.parseColor("#842029"));
        holder.icon.setVisibility(View.INVISIBLE);
        holder.image.setImageResource(R.drawable.ic_baseline_error_outline_24);
        View v = (View) holder.image.getParent();
        v.setBackgroundColor(Color.parseColor("#F8D7DA"));
        MaterialCardView c = (MaterialCardView) holder.itemView;
        c.setCardBackgroundColor(Color.parseColor("#F8D7DA"));
        c.setRadius(c.getResources().getDimension(R.dimen.dp10));
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
                            public void onSuccess(@NotNull byte[] bytes) {
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
