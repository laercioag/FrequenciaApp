package com.example.sinf.simplescanner.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sinf.simplescanner.activities.ListPeriodsActivity;
import com.example.sinf.simplescanner.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PeriodsRecyclerViewAdapter extends RecyclerView.Adapter<PeriodsRecyclerViewAdapter.ViewHolder>{

    ArrayList<HashMap<String, String>> periodsList;

    public PeriodsRecyclerViewAdapter(ArrayList<HashMap<String, String>> periodsList) {
        this.periodsList = new ArrayList<>(periodsList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_periods_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.name.setText(periodsList.get(+position).get(ListPeriodsActivity.TAG_NAME));
        viewHolder.data.setText(periodsList.get(+position).get(ListPeriodsActivity.TAG_DATA));
    }

    @Override
    public int getItemCount() {
        return periodsList.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView id, name, data;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.period_name);
            data = (TextView) itemView.findViewById(R.id.period_data);
        }
    }

}
