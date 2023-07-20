package it.gangoffive.eculture.graph;


import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;

import it.gangoffive.eculture.R;


public class GraphViewHolder extends RecyclerView.ViewHolder {

    TextView mSubtitle;
    TextView mMessage;
    TimelineView mTimelineView;
    LinearLayout mLayout;

    public GraphViewHolder(View itemView, int viewType) {
        super(itemView);
        mLayout = itemView.findViewById(R.id.frame);
        mSubtitle = itemView.findViewById(R.id.text_timeline_subtitle);
        mMessage = itemView.findViewById(R.id.text_timeline_title);
        mTimelineView = itemView.findViewById(R.id.timeline);
        mTimelineView.initLine(viewType);
    }
}