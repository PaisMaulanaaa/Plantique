package com.example.plantique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipViewHolder> {

    private List<Tip> tipList;
    private Context context;

    public TipAdapter(List<Tip> tipList, Context context) {
        this.tipList = tipList;
        this.context = context;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        Tip tip = tipList.get(position);

        holder.tipTitle.setText(tip.getTitle());
        holder.tipDescription.setText(tip.getDescription());

        // Set icon based on tip type
        int iconResId = getIconResource(tip.getIcon());
        if (iconResId != 0) {
            holder.tipIcon.setImageResource(iconResId);
        }

        // Set icon color if needed
        // You can implement this based on your design
    }

    @Override
    public int getItemCount() {
        return tipList != null ? tipList.size() : 0;
    }

    public void updateData(List<Tip> tips) {
        this.tipList = tips;
        notifyDataSetChanged();
    }

    private int getIconResource(String iconName) {
        switch (iconName) {
            case "ic_water_drop":
                return R.drawable.ic_water_drop;
            case "ic_sun":
                return R.drawable.ic_sun;
            case "ic_compost":
                return R.drawable.ic_compost;
            case "ic_humidity":
                return R.drawable.ic_humidity;
            case "ic_fertilizer":
                return R.drawable.ic_fertilizer;
            case "ic_temperature":
                return R.drawable.ic_temprature;
            default:
                return 0;
        }
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {
        ImageView tipIcon;
        TextView tipTitle;
        TextView tipDescription;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tipIcon = itemView.findViewById(R.id.tip_icon);
            tipTitle = itemView.findViewById(R.id.tip_title);
            tipDescription = itemView.findViewById(R.id.tip_description);
        }
    }
}