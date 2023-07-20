package it.gangoffive.eculture.ui.tours.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.TourModel;

public class TourChecklistRecyclerViewAdapter extends RecyclerView.Adapter<TourChecklistRecyclerViewAdapter.TourChecklistViewHolder>  {

    private List<TourModel> tourList;

    public TourChecklistRecyclerViewAdapter(List<TourModel> listData) {
        this.tourList = listData;
    }


    @NonNull
    @Override
    public TourChecklistRecyclerViewAdapter.TourChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_checklist_tour, parent, false);
        return new TourChecklistRecyclerViewAdapter.TourChecklistViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull TourChecklistRecyclerViewAdapter.TourChecklistViewHolder holder, int position) {

        TourModel tour = tourList.get(position);

        if (tour.getId() != null) {
            holder.title.setText(tour.getTitle());
            holder.selection.setText(tour.getId());
        } else {
            setNoDataWarning(holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton radio = view.findViewById(R.id.tour_checklist_radio);
                radio.setChecked(!radio.isChecked());
            }
        });

    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }


    public class TourChecklistViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public RadioButton selection;

        public TourChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tour_checklist_title);
            selection = itemView.findViewById(R.id.tour_checklist_radio);
            title.setTextColor(Color.parseColor("#000000"));
            selection.setTextColor(Color.parseColor("#FFFFFF"));
        }

    }


    /**
     *
     * Imposta un messaggio di errore in caso di dati mancanti
     *
     * @param holder TourChecklistRecyclerViewAdapter.TourChecklistViewHolder
     *
     */
    private void setNoDataWarning(@NonNull TourChecklistRecyclerViewAdapter.TourChecklistViewHolder holder){
        holder.title.setText(R.string.wiz_no_data_msg);
        holder.title.setTextColor(Color.parseColor("#842029"));
        holder.selection.setVisibility(View.GONE);
        View v = (View) holder.title.getParent();
        v.setBackgroundColor(Color.parseColor("#F8D7DA"));
        MaterialCardView c = (MaterialCardView) holder.itemView;
        c.setCardBackgroundColor(Color.parseColor("#F8D7DA"));
        c.setRadius(c.getResources().getDimension(R.dimen.dp10));
    }


}
