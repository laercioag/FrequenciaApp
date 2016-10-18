package com.example.sinf.simplescanner.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sinf.simplescanner.activities.ListParticipantsActivity;
import com.example.sinf.simplescanner.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ParticipantsRecyclerViewAdapter extends RecyclerView.Adapter<ParticipantsRecyclerViewAdapter.ViewHolder>{

    ArrayList<HashMap<String, String>> participantsList;

    public ParticipantsRecyclerViewAdapter(ArrayList<HashMap<String, String>> participantsList) {
        this.participantsList = new ArrayList<>(participantsList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_participants_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.name.setText(participantsList.get(+position).get(ListParticipantsActivity.TAG_NAME));

        //To-Do: Change icon based on attendance
        boolean attendance = Boolean.parseBoolean(participantsList.get(+position).get(ListParticipantsActivity.TAG_ATTENDANCE));
        if(attendance) {
            viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(viewHolder.icon.getContext(), R.drawable.ic_person_green_48dp));
        } else {
            viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(viewHolder.icon.getContext(), R.drawable.ic_person_grey_48dp));
        }
    }

    @Override
    public int getItemCount() {
        return participantsList.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public ImageView icon;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.participant_name);
            icon = (ImageView) itemView.findViewById(R.id.participant_icon);
        }
    }
}
