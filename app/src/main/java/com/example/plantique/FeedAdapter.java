package com.example.plantique;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<FeedPost> feedPosts;

    public FeedAdapter(Context context, List<FeedPost> feedPosts) {
        this.context = context;
        this.feedPosts = feedPosts;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedPost post = feedPosts.get(position);

        holder.tvUsername.setText(post.getUsername());
        holder.tvPlantName.setText(post.getPlantName());

        // Load plant image
        if (post.getPlantImageUrl() != null && !post.getPlantImageUrl().isEmpty()) {
            Picasso.get().load(post.getPlantImageUrl())
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .error(R.drawable.ic_plant_placeholder)
                    .into(holder.ivPlantImage);
        } else {
            holder.ivPlantImage.setImageResource(R.drawable.ic_plant_placeholder);
        }

        // Handle click on "Lihat selengkapnya"
        holder.tvSeeMore.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("post_id", post.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return feedPosts != null ? feedPosts.size() : 0;
    }

    public void updateData(List<FeedPost> newPosts) {
        this.feedPosts = newPosts;
        notifyDataSetChanged();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar, ivPlantImage;
        TextView tvUsername, tvPlantName, tvSeeMore;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            ivPlantImage = itemView.findViewById(R.id.iv_plant_image);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvPlantName = itemView.findViewById(R.id.tv_plant_name);
            tvSeeMore = itemView.findViewById(R.id.tv_see_more);
        }
    }
}