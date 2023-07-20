package it.gangoffive.eculture.graph;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.ArrayList;
import java.util.List;

import it.gangoffive.eculture.DetailsPlaceActivity;
import it.gangoffive.eculture.DetailsRoomActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.model.GraphModel;

public class GraphAdapter extends RecyclerView.Adapter<GraphViewHolder> {

    private final List<GraphModel> mGraphList;
    private Context mContext;

    public GraphAdapter(List<GraphModel> graphList) {
        mGraphList = graphList;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @NonNull
    @Override
    public GraphViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        View view = mLayoutInflater.inflate(R.layout.item_graph, parent, false);
        return new GraphViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull GraphViewHolder holder, int position) {
        GraphModel graphModel = mGraphList.get(position);
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getString(R.string.pref_file), Context.MODE_PRIVATE);
        int color = R.color.curatorcolor;
        switch (sp.getString("type", "tourist")) {
            case "curator":
                color = R.color.curatorcolor;
                break;
            case "tourist":
                color = R.color.touristcolor;
                break;

        }
        if (graphModel.getStatus() == 2) {
            holder.mTimelineView.setEndLineColor(ContextCompat.getColor(mContext, color), getItemViewType(position));
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker), ContextCompat.getColor(mContext, color));
            holder.mTimelineView.setStartLineColor(ContextCompat.getColor(mContext, color), getItemViewType(position));
            holder.mLayout.setOnClickListener(view -> {
                switch (graphModel.getmType()) {
                    case "rooms":
                        Intent roomIntent = new Intent(view.getContext(), DetailsRoomActivity.class);
                        roomIntent.putExtra("ID", graphModel.getmId());
                        if (graphModel.getmPlace() != null) {
                            roomIntent.putStringArrayListExtra("places", (ArrayList<String>) graphModel.getmPlace());
                        }
                        view.getContext().startActivity(roomIntent);
                        break;
                    case "places":
                        Intent placeIntent = new Intent(view.getContext(), DetailsPlaceActivity.class);
                        placeIntent.putExtra("ID", graphModel.getmId());
                        view.getContext().startActivity(placeIntent);
                        break;
                }

            });

        } else {
            holder.mTimelineView.setEndLineColor(ContextCompat.getColor(mContext, color), getItemViewType(position));
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive), ContextCompat.getColor(mContext, color));
            holder.mTimelineView.setStartLineColor(ContextCompat.getColor(mContext, color), getItemViewType(position));
        }
        holder.mMessage.setText(graphModel.getMessage());
        if (graphModel.getmPlace() != null) {
            String description = "";
            for (int i = 0; i < graphModel.getmPlace().size(); i++) {
                description = description.concat(graphModel.getmPlace().get(i) + ", ");
            }
            if (description.length() >= 3) {
                holder.mSubtitle.setText(description.substring(0, description.length() - 2));
            } else {
                holder.mSubtitle.setText(mContext.getString(R.string.graph_walking_room));
                holder.mSubtitle.setTextColor(ContextCompat.getColor(mContext, R.color.error));
            }
            holder.mSubtitle.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return (mGraphList != null ? mGraphList.size() : 0);
    }

}