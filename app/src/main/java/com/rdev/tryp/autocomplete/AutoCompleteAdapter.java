package com.rdev.tryp.autocomplete;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.rdev.tryp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AutoCompleteAdapter extends RecyclerView.Adapter<AutoCompleteAdapter.TextHolder> {
    List<AutocompletePrediction> data;

    public interface onPlacePicked {
        void onPlace(AutocompletePrediction prediction);
    }

    private onPlacePicked listener;

    public AutoCompleteAdapter(List<AutocompletePrediction> autocompletePredictions, onPlacePicked listener) {
        this.data = autocompletePredictions;
        this.listener = listener;
    }

    class TextHolder extends RecyclerView.ViewHolder {

        final TextView address_tv;
        final TextView address_desc_tv;
        private AutocompletePrediction data;

        public TextHolder(@NonNull View itemView) {
            super(itemView);
            this.address_tv = itemView.findViewById(R.id.address_tv);
            this.address_desc_tv = itemView.findViewById(R.id.address_desc_tv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onPlace(data);
                }
            });
        }

        void onAttach(AutocompletePrediction data) {
            this.data = data;
            address_tv.setText(data.getPrimaryText(null));
            address_desc_tv.setText(data.getFullText(null));
        }
    }

    @NonNull
    @Override
    public TextHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TextHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.autocomplete_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TextHolder holder, int position) {
        holder.onAttach(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<AutocompletePrediction> data){
        this.data = data;
        notifyDataSetChanged();
    }
}