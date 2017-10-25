package com.app.courier.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.courier.R;
import com.app.courier.models.PlaceQueryResult;

import java.util.List;

/**
 * Created by rafaelgontijo on 24/10/17.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.MyViewHolder> {

    private List<PlaceQueryResult> queryResults;
    private View.OnClickListener listener;

    public PlacesAdapter(List<PlaceQueryResult> queryResults, View.OnClickListener listener) {
        this.queryResults = queryResults;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_place, parent, false);
        itemView.setOnClickListener(listener);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PlaceQueryResult result = this.queryResults.get(position);
        holder.description.setText(result.getDescription());
    }

    @Override
    public int getItemCount() {
        return this.queryResults.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView description;

        public MyViewHolder(View view) {
            super(view);
            description = (TextView) view.findViewById(R.id.place_adapter_description);
        }
    }
}
