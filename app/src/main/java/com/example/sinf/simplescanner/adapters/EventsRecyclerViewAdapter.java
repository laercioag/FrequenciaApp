package com.example.sinf.simplescanner.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sinf.simplescanner.activities.ListEventsActivity;
import com.example.sinf.simplescanner.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewAdapter.ViewHolder>{

    ArrayList<HashMap<String, String>> eventsList;

    public EventsRecyclerViewAdapter(ArrayList<HashMap<String, String>> eventsList) {
        this.eventsList = new ArrayList<>(eventsList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_events_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.id.setText(eventsList.get(+position).get(ListEventsActivity.TAG_ID));
        viewHolder.name.setText(eventsList.get(+position).get(ListEventsActivity.TAG_NAME));
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public HashMap<String, String> removeItem(int position) {
        final HashMap<String, String> item = eventsList.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void addItem(int position, HashMap<String, String> item) {
        eventsList.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final HashMap<String, String> item = eventsList.remove(fromPosition);
        eventsList.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(ArrayList<HashMap<String,String>> lEventsList) {
        applyAndAnimateRemovals(lEventsList);
        applyAndAnimateAdditions(lEventsList);
        applyAndAnimateMovedItems(lEventsList);
    }

    private void applyAndAnimateRemovals(ArrayList<HashMap<String,String>> newEventsList) {
        for (int i = eventsList.size() - 1; i >= 0; i--) {
            final HashMap<String, String> item = eventsList.get(i);
            if (!newEventsList.contains(item)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<HashMap<String,String>> newEventsList) {
        for (int i = 0, count = newEventsList.size(); i < count; i++) {
            final HashMap<String, String> item = newEventsList.get(i);
            if (!eventsList.contains(item)) {
                addItem(i, item);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<HashMap<String,String>> newEventsList) {
        for (int toPosition = newEventsList.size() - 1; toPosition >= 0; toPosition--) {
            final HashMap<String, String> item = newEventsList.get(toPosition);
            final int fromPosition = eventsList.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView id, name;
        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.event_id);
            name = (TextView) itemView.findViewById(R.id.event_name);
        }
    }

}
