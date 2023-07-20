package it.gangoffive.eculture.ui.wizard.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.model.PlaceModel;

public class PlaceChecklistRecyclerViewAdapter extends RecyclerView.Adapter<PlaceChecklistRecyclerViewAdapter.PlacesChecklistViewHolder>  {

    final private List<PlaceModel> placesList;
    final private HashMap<String, String> rooms;
    final private WizardActivity activity;

    private ArrayList<String> roomID = new ArrayList<>();
    private String prevRoomId = "NULL";
    private int positionRoomID = -1;

    public PlaceChecklistRecyclerViewAdapter(List<PlaceModel> listData, WizardActivity activity) {
        this.placesList = listData;
        this.rooms = activity.getSelectedRooms();
        this.activity = activity;
    }

    @NonNull
    @Override
    public PlacesChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_card_wizard_places, parent, false);
        return new PlacesChecklistViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesChecklistViewHolder holder, int position) {

        PlaceModel place = placesList.get(position);
        String game = activity.getString(R.string.wiz_minigame) + " " + place.getMinigame();

        roomID.add(place.getRoomID());
        holder.roomId.setText(place.getRoomID());
        if (position == 0){
            holder.titleContainer.setVisibility(View.VISIBLE);
            holder.roomTitle.setText(rooms.get(place.getRoomID()));

            if (place.getId() != null){
                holder.placeTitle.setText(place.getTitle());
                holder.selection.setText(place.getId());
                holder.minigame.setText(game);
            } else {
                setNoDataWarning(holder);
            }
        } else {
            if (roomID.get(position).equals(roomID.get(position - 1))) {
                holder.placeTitle.setText(place.getTitle());
                holder.selection.setText(place.getId());
                holder.minigame.setText(game);
            } else {
                holder.titleContainer.setVisibility(View.VISIBLE);
                holder.roomTitle.setText(rooms.get(place.getRoomID()));

                if (place.getId() != null){
                    holder.placeTitle.setText(place.getTitle());
                    holder.selection.setText(place.getId());
                    holder.minigame.setText(game);
                } else {
                    setNoDataWarning(holder);
                }
            }
        }

        setCheckedFromPreviousState(holder);

        holder.roomContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.selection.isChecked()){
                    holder.selection.setChecked(false);
                } else {
                    holder.selection.setChecked(true);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }


    public static class PlacesChecklistViewHolder extends RecyclerView.ViewHolder {
        public TextView placeTitle;
        public RadioButton selection;
        public TextView roomTitle;
        public TextView roomId;
        public MaterialCardView titleContainer;
        public MaterialCardView roomContainer;
        public TextView minigame;

        public PlacesChecklistViewHolder(@NonNull View itemView) {
            super(itemView);

            placeTitle = itemView.findViewById(R.id.wiz_place_title);
            selection = itemView.findViewById(R.id.wiz_place_radio);
            roomTitle = itemView.findViewById(R.id.wiz_room_place_title);
            roomId = itemView.findViewById(R.id.wiz_room_place_id);
            titleContainer = itemView.findViewById(R.id.wiz_room_title_container);
            roomContainer = itemView.findViewById(R.id.wiz_room_container);
            minigame = itemView.findViewById(R.id.wiz_place_game);

        }
    }


    /**
     *
     * Imposta un messaggio di errore in caso di dati mancanti
     *
     * @param holder PlacesChecklistViewHolder
     *
     */
    private void setNoDataWarning(@NonNull PlacesChecklistViewHolder holder){
        holder.placeTitle.setText(R.string.wiz_no_place_msg);
        holder.selection.setVisibility(View.GONE);
        holder.minigame.setVisibility(View.GONE);
        holder.placeTitle.setTextColor(Color.parseColor("#842029"));
        holder.placeTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        holder.placeTitle.setMaxWidth(Integer.MAX_VALUE);
        holder.placeTitle.setTextSize(12);
        holder.placeTitle.setClickable(false);
    }


    /**
     *
     * Maniene uno stato locale con i valori precedentemente selezionati dall'utente
     *
     * @param holder PlacesChecklistViewHolder
     */
    private void setCheckedFromPreviousState(PlacesChecklistViewHolder holder){

        ArrayList<HashMap<String, ArrayList<String>>> selectedPlaces = activity.getTour().getPlaces();

        if (selectedPlaces != null) {
            String roomID = String.valueOf(holder.roomId.getText());
            if (!roomID.equals(prevRoomId)) {
                positionRoomID++;
                prevRoomId = roomID;
            }
            if (selectedPlaces.size() > positionRoomID){
                HashMap<String, ArrayList<String>> hashMap = selectedPlaces.get(positionRoomID);
                if (hashMap.get("roomid").contains(prevRoomId)) {
                    ArrayList<String> arrayList = hashMap.get("places");
                    if (arrayList.contains(holder.placeTitle.getText())) {
                        holder.selection.setChecked(true);
                    }
                }
            }
        }
    }




}

