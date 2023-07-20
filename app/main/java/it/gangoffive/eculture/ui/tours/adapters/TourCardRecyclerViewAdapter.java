package it.gangoffive.eculture.ui.tours.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import it.gangoffive.eculture.R;
import it.gangoffive.eculture.DetailsTourActivity;
import it.gangoffive.eculture.model.TourModel;

public class TourCardRecyclerViewAdapter extends RecyclerView.Adapter<TourCardRecyclerViewAdapter.TourCardViewHolder> {

    private List<TourModel> tourList;

    public TourCardRecyclerViewAdapter(List<TourModel> listData) {
        this.tourList = listData;
    }


    @NonNull
    @Override
    public TourCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_card_tour, parent, false);
        return new TourCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull TourCardViewHolder holder, int position) {

        TourModel tour = tourList.get(position);
        String file = holder.itemView.getContext().getString(R.string.pref_file);
        SharedPreferences sp = holder.itemView.getContext().getSharedPreferences(file, Context.MODE_PRIVATE);
        String userRole = sp.getString("type", "tourist");
        int icon = userRole.equals("curator") ? R.drawable.tour_icon_curator : R.drawable.tour_icon_tourist;

        if (tour.getId() != null) {
            holder.title.setText(tour.getTitle());
            holder.subtitle.setText(tour.getSubtitle());
            holder.subtitle.setTextColor(Color.parseColor("#787878"));
            holder.image.setImageResource(icon);
        } else {
            setNoDataWarning(holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailsTourActivity.class);
                intent.putExtra("ID", tour.getId());
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }


    public class TourCardViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView subtitle;
        public ImageView icon;
        public ImageView image;

        public TourCardViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tour_card_title);
            subtitle = itemView.findViewById(R.id.tour_card_subtitle);
            icon = itemView.findViewById(R.id.goto_tour_list_detail);
            image = itemView.findViewById(R.id.tour_card_image);
            title.setTextColor(Color.parseColor("#000000"));
        }

    }


    /**
     *
     * Imposta un messaggio di errore in caso di dati mancanti
     *
     * @param holder TourCardRecyclerViewAdapter.TourCardViewHolder
     *
     */
    private void setNoDataWarning(@NonNull TourCardRecyclerViewAdapter.TourCardViewHolder holder){
        holder.title.setText(R.string.caution);
        holder.title.setTextColor(Color.parseColor("#842029"));
        holder.subtitle.setText(R.string.wiz_no_data_msg);
        holder.subtitle.setTextColor(Color.parseColor("#842029"));
        holder.icon.setVisibility(View.INVISIBLE);
        holder.image.setImageResource(R.drawable.ic_baseline_error_outline_24);
        View v = (View) holder.image.getParent();
        v.setBackgroundColor(Color.parseColor("#F8D7DA"));
        MaterialCardView c = (MaterialCardView) holder.itemView;
        c.setCardBackgroundColor(Color.parseColor("#F8D7DA"));
        c.setRadius(c.getResources().getDimension(R.dimen.dp10));
    }



}
