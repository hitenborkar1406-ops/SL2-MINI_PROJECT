package com.example.mini_project;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private int itemId = -1;
    private Item item;
    private DatabaseHelper dbHelper;

    private ImageView ivDetailImage;
    private TextView tvDetailName, tvDetailType, tvDetailDesc;
    private TextView tvDetailLocation, tvDetailTimestamp;
    private TextView tvDetailPosterName, tvDetailContact;
    private TextView tvResolvedBanner;
    private MaterialButton btnMarkReturned, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Back button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        itemId = getIntent().getIntExtra("item_id", -1);
        if (itemId == -1) { finish(); return; }

        dbHelper = new DatabaseHelper(this);

        // Bind views
        ivDetailImage       = findViewById(R.id.ivDetailImage);
        tvDetailName        = findViewById(R.id.tvDetailName);
        tvDetailType        = findViewById(R.id.tvDetailType);
        tvDetailDesc        = findViewById(R.id.tvDetailDesc);
        tvDetailLocation    = findViewById(R.id.tvDetailLocation);
        tvDetailTimestamp   = findViewById(R.id.tvDetailTimestamp);
        tvDetailPosterName  = findViewById(R.id.tvDetailPosterName);
        tvDetailContact     = findViewById(R.id.tvDetailContact);
        tvResolvedBanner    = findViewById(R.id.tvResolvedBanner);
        btnMarkReturned     = findViewById(R.id.btnMarkReturned);
        btnDelete           = findViewById(R.id.btnDelete);

        loadItem();

        btnMarkReturned.setOnClickListener(v -> markAsReturned());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    // ── Load & bind ───────────────────────────────────────────────

    private void loadItem() {
        item = dbHelper.getItemById(itemId);
        if (item == null) { finish(); return; }

        // Hero image
        String imagePath = item.getImagePath();
        if (!TextUtils.isEmpty(imagePath)) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                ivDetailImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            } else {
                ivDetailImage.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            ivDetailImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Name + type badge
        tvDetailName.setText(item.getName());
        tvDetailType.setText(item.getType());
        if ("Lost".equals(item.getType())) {
            tvDetailType.setBackgroundResource(R.drawable.badge_lost);
        } else {
            tvDetailType.setBackgroundResource(R.drawable.badge_found);
        }
        tvDetailType.setTextColor(ContextCompat.getColor(this, R.color.white));

        // Description
        tvDetailDesc.setText(!TextUtils.isEmpty(item.getDesc())
                ? item.getDesc() : "No description provided.");

        // Location
        tvDetailLocation.setText(!TextUtils.isEmpty(item.getLocation())
                ? item.getLocation() : "Location not specified.");

        // Timestamp
        tvDetailTimestamp.setText(item.getCreatedAt() > 0
                ? formatDate(item.getCreatedAt()) : "Date unknown");

        // Poster info (may be null for items posted before this feature)
        tvDetailPosterName.setText(!TextUtils.isEmpty(item.getPosterName())
                ? item.getPosterName() : "Anonymous");
        tvDetailContact.setText(!TextUtils.isEmpty(item.getPosterContact())
                ? item.getPosterContact() : "No contact info");

        // Status UI
        applyStatusUI();
    }

    private void applyStatusUI() {
        boolean resolved = "resolved".equals(item.getStatus());
        tvResolvedBanner.setVisibility(resolved ? View.VISIBLE : View.GONE);
        btnMarkReturned.setVisibility(resolved ? View.GONE : View.VISIBLE);
    }

    // ── Actions ───────────────────────────────────────────────────

    private void markAsReturned() {
        dbHelper.updateItemStatus(itemId, "resolved");
        item = dbHelper.getItemById(itemId); // reload to reflect change
        applyStatusUI();
        Toast.makeText(this, "Marked as returned! 🎉", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to permanently delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteItem(itemId);
                    Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());
        return "Posted " + sdf.format(new Date(millis));
    }
}
