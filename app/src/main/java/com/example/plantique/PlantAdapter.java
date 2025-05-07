package com.example.plantique;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;
    private Context context;

    public PlantAdapter(List<Plant> plantList, Context context) {
        this.plantList = plantList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        // Set plant name
        holder.plantName.setText(plant.getName());

        // Set plant category/type
        String category = getCategoryFromPlant(plant);
        holder.plantType.setText(category);

        // Load image using Picasso
        loadPlantImage(holder.plantImage, plant.getImageUrl());

        // Set click listener to open detail activity
        setupCardClickListener(holder.cardView, plant);
    }

    /**
     * Gets category text from plant object
     * Handles potential missing getCategory() method
     */
    private String getCategoryFromPlant(Plant plant) {
        // Try to get category if the method exists
        try {
            // Use reflection to check if getCategory method exists
            String category = (String) plant.getClass().getMethod("getCategory").invoke(plant);
            if (category != null && !category.isEmpty()) {
                return category;
            }
        } catch (Exception e) {
            // Method doesn't exist or returned null - we'll use source instead
        }

        // Fallback: use plant source with formatting
        String source = plant.getSource();
        if (source != null && !source.isEmpty()) {
            // Capitalize first letter
            return source.substring(0, 1).toUpperCase() + source.substring(1);
        }

        return "Unknown";
    }

    /**
     * Loads plant image with Picasso
     */
    private void loadPlantImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .error(R.drawable.ic_plant_placeholder)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_plant_placeholder);
        }
    }

    /**
     * Sets up click listener for plant card
     */
    private void setupCardClickListener(CardView cardView, Plant plant) {
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("plant_id", plant.getId());
                intent.putExtra("plant_source", plant.getSource());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return plantList != null ? plantList.size() : 0;
    }

    /**
     * Updates adapter data and refreshes the view
     */
    public void updateData(List<Plant> plants) {
        this.plantList = plants;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for plant items
     */
    static class PlantViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView plantImage;
        TextView plantName;
        TextView plantType;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);  // Changed from card_plant
            plantImage = itemView.findViewById(R.id.img_plant);  // Changed from plant_image
            plantName = itemView.findViewById(R.id.tv_plant_name);  // Changed from plant_name
            plantType = itemView.findViewById(R.id.tv_plant_type);  // Changed from plant_type

        }
    }
}