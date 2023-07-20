package it.gangoffive.eculture.ui.wizard.adapters;

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

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.RoomModel;
import it.gangoffive.eculture.ui.wizard.StepRoomsFragment;

public class RoomCardRecyclerViewAdapter extends RecyclerView.Adapter<RoomCardRecyclerViewAdapter.RoomCardViewHolder> {

    final private List<RoomModel> roomsList;
    private StepRoomsFragment fragment;

    public RoomCardRecyclerViewAdapter(List<RoomModel> listData, StepRoomsFragment fragment) {
        this.roomsList = listData;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public RoomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_card_wizard_rooms, parent, false);
        return new RoomCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomCardViewHolder holder, int position) {

        RoomModel room = roomsList.get(position);

        if (room.getId() != null) {
            holder.name.setText(room.getName());
            setImage(holder.cover, room.getId());
            holder.roomID.setText(room.getId());
            String timeMsg = fragment.getString(R.string.wiz_time_placeholder) +" "+ room.getVisiting_time() + " min";
            holder.roomTime.setText(timeMsg);
        } else {
            setNoDataWarning(holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.addToSelectedRoomList(getRoomModelById(String.valueOf(holder.roomID.getText())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomsList.size();
    }


    public static class RoomCardViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView roomID;
        public TextView roomTime;
        public ImageView cover;
        public ImageView icon;

        public RoomCardViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.wiz_room_title);
            roomID = itemView.findViewById(R.id.wiz_room_id);
            roomTime = itemView.findViewById(R.id.wiz_room_time);
            cover = itemView.findViewById(R.id.wiz_room_image);
            icon = itemView.findViewById(R.id.wiz_room_plus_icon);

        }
    }


    /**
     *
     * Imposta un messaggio di errore in caso di dati mancanti
     *
     * @param holder RoomCardViewHolder
     *
     */
    private void setNoDataWarning(@NonNull RoomCardViewHolder holder){
        holder.name.setText(R.string.caution);
        holder.name.setTextColor(Color.parseColor("#842029"));
        holder.roomTime.setText(R.string.wiz_no_data_msg);
        holder.roomTime.setTextColor(Color.parseColor("#842029"));
        holder.roomTime.setMaxWidth(Integer.MAX_VALUE);
        holder.icon.setVisibility(View.GONE);
        View v = (View) holder.cover.getParent();
        v.setVisibility(View.GONE);
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
        StorageReference storagePlaceRef = FirebaseStorage.getInstance().getReference().child("rooms");
        storagePlaceRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(@NotNull ListResult listResult) {
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


    /**
     *
     * Restituisce una istanza di RoomModel a partire dal suo ID
     *
     * @param id String
     * @return RoomModel
     *
     */
    private RoomModel getRoomModelById(String id){
        RoomModel room = new RoomModel(null, "", "", "", false, "", "");
        for (int i = 0; i < roomsList.size(); i++){
            if (roomsList.get(i).getId().equals(id)){
                room = roomsList.get(i);
            }
        }
        return room;
    }

}

