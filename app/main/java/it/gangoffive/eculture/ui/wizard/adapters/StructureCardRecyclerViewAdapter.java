package it.gangoffive.eculture.ui.wizard.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import it.gangoffive.eculture.model.StructureModel;

public class StructureCardRecyclerViewAdapter extends RecyclerView.Adapter<StructureCardRecyclerViewAdapter.StructuresCardViewHolder> {

    final private List<StructureModel> structuresList;
    final private String selectedStructure;

    public StructureCardRecyclerViewAdapter(List<StructureModel> listData, String selectedStructure) {
        this.structuresList = listData;
        this.selectedStructure = selectedStructure;
    }

    @NonNull
    @Override
    public StructuresCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_card_wizard_structures, parent, false);
        return new StructuresCardViewHolder(layoutView, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull StructuresCardViewHolder holder, int position) {

        StructureModel structure = structuresList.get(position);

        if (structure.getId() != null) {
            holder.name.setText(structure.getName());
            holder.address.setText(structure.getAddress());
            String cityFull = structure.getCity() +" ("+structure.getProvince()+ ") - " +structure.getRegion();
            holder.city.setText(cityFull);
            setImage(holder.cover, structure.getId());
            holder.selection.setText(structure.getId());
            if (selectedStructure != null && selectedStructure.equals(structure.getId())){
                holder.selection.setChecked(true);
            }
        } else {
            setNoDataWarning(holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ViewGroup r = holder.container;
                RadioButton radio = view.findViewById(R.id.wiz_structure_radio);

                for (int i = 0; i < r.getChildCount(); i++) {
                    View v = r.getChildAt(i);
                    RadioButton otherRadio = v.findViewById(R.id.wiz_structure_radio);
                    if (otherRadio.getText() == radio.getText()){
                        radio.setChecked(!radio.isChecked());
                    } else {
                        otherRadio.setChecked(false);
                    }
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return structuresList.size();
    }


    public static class StructuresCardViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView address;
        public TextView city;
        public RadioButton selection;
        public ImageView cover;
        public ViewGroup container;

        public StructuresCardViewHolder(@NonNull View itemView, ViewGroup parent) {
            super(itemView);

            name = itemView.findViewById(R.id.wiz_structure_title);
            address = itemView.findViewById(R.id.wiz_structure_address);
            city = itemView.findViewById(R.id.wiz_structure_city);
            selection = itemView.findViewById(R.id.wiz_structure_radio);
            cover = itemView.findViewById(R.id.wiz_structure_image);
            name.setTextColor(Color.parseColor("#000000"));
            address.setTextColor(Color.parseColor("#787878"));
            city.setTextColor(Color.parseColor("#787878"));
            selection.setTextColor(Color.parseColor("#FFFFFF"));

            container = parent;
        }
    }


    /**
     *
     * Imposta un messaggio di errore in caso di dati mancanti
     *
     * @param holder StructuresCardViewHolder
     *
     */
    private void setNoDataWarning(@NonNull StructuresCardViewHolder holder){
        holder.name.setText(R.string.caution);
        holder.name.setTextColor(Color.parseColor("#842029"));
        holder.address.setText(R.string.wiz_no_data_msg);
        holder.address.setTextColor(Color.parseColor("#842029"));
        holder.city.setVisibility(View.INVISIBLE);
        holder.selection.setVisibility(View.INVISIBLE);
        holder.cover.setImageResource(R.drawable.ic_baseline_error_outline_24);
        View v = (View) holder.cover.getParent();
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
        StorageReference storagePlaceRef = FirebaseStorage.getInstance().getReference().child("structures");
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


}

