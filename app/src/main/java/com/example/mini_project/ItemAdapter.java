package com.example.mini_project;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> items;
    private final Context context;

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);

        // Name
        holder.tvName.setText(item.getName());

        // Location + category on card
        String loc = item.getLocation();
        String cat = item.getCategory();
        String locCat = (!loc.isEmpty() ? loc : "Location not specified")
                + (!cat.isEmpty() && !"Other".equals(cat) ? "  ·  " + cat : "");
        holder.tvLocation.setText(locCat);

        // Timestamp — relative time
        holder.tvTimestamp.setText(formatRelativeTime(item.getCreatedAt()));

        // Type badge + left strip color + card background tint
        String type = item.getType();
        holder.tvType.setText(type);
        if ("Lost".equals(type)) {
            holder.tvType.setBackgroundResource(R.drawable.badge_lost);
            holder.viewStrip.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLost));
            ((MaterialCardView) holder.itemView).setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.cardBgLost));
        } else {
            holder.tvType.setBackgroundResource(R.drawable.badge_found);
            holder.viewStrip.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFound));
            ((MaterialCardView) holder.itemView).setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.cardBgFound));
        }
        holder.tvType.setTextColor(ContextCompat.getColor(context, R.color.white));

        // Thumbnail
        String imagePath = item.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                holder.ivThumbnail.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            } else {
                holder.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Resolved overlay
        boolean resolved = "resolved".equals(item.getStatus());
        holder.resolvedOverlay.setVisibility(resolved ? View.VISIBLE : View.GONE);

        // Click → DetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("item_id", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String formatRelativeTime(long millis) {
        if (millis == 0) return "";
        long diff = System.currentTimeMillis() - millis;
        long minutes = diff / 60_000;
        if (minutes < 1)  return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24)   return hours + "h ago";
        long days = hours / 24;
        if (days < 7)     return days + "d ago";
        return new SimpleDateFormat("MMM d", Locale.getDefault()).format(new Date(millis));
    }

    // ── ViewHolder ────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvLocation, tvTimestamp;
        ImageView ivThumbnail;
        View viewStrip;
        View resolvedOverlay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName          = itemView.findViewById(R.id.tvItemName);
            tvType          = itemView.findViewById(R.id.tvItemType);
            tvLocation      = itemView.findViewById(R.id.tvItemLocation);
            tvTimestamp     = itemView.findViewById(R.id.tvTimestamp);
            ivThumbnail     = itemView.findViewById(R.id.ivThumbnail);
            viewStrip       = itemView.findViewById(R.id.viewStrip);
            resolvedOverlay = itemView.findViewById(R.id.resolvedOverlay);
        }
    }
}
