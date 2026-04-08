package com.example.mini_project;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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
    private TextView tvDetailLocation, tvDetailTimestamp, tvDetailCategory;
    private TextView tvDetailEventDate;
    private android.view.View rowEventDate;
    private TextView tvDetailPosterName, tvDetailContact;
    private TextView tvResolvedBanner;
    private MaterialButton btnShare, btnMarkReturned, btnDelete, btnEdit;

    // Launcher so DetailActivity can refresh after returning from edit
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register edit launcher BEFORE setContentView (before STARTED state)
        editLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadItem();   // refresh all fields after edit
                    }
                }
        );

        setContentView(R.layout.activity_detail);

        // Back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        itemId = getIntent().getIntExtra("item_id", -1);
        if (itemId == -1) { finish(); return; }

        dbHelper = new DatabaseHelper(this);

        // Bind views
        ivDetailImage      = findViewById(R.id.ivDetailImage);
        tvDetailName       = findViewById(R.id.tvDetailName);
        tvDetailType       = findViewById(R.id.tvDetailType);
        tvDetailDesc       = findViewById(R.id.tvDetailDesc);
        tvDetailLocation   = findViewById(R.id.tvDetailLocation);
        tvDetailCategory   = findViewById(R.id.tvDetailCategory);
        tvDetailTimestamp  = findViewById(R.id.tvDetailTimestamp);
        tvDetailEventDate  = findViewById(R.id.tvDetailEventDate);
        rowEventDate       = findViewById(R.id.rowEventDate);
        tvDetailPosterName = findViewById(R.id.tvDetailPosterName);
        tvDetailContact    = findViewById(R.id.tvDetailContact);
        tvResolvedBanner   = findViewById(R.id.tvResolvedBanner);
        btnShare        = findViewById(R.id.btnShare);
        btnMarkReturned = findViewById(R.id.btnMarkReturned);
        btnDelete       = findViewById(R.id.btnDelete);
        btnEdit         = findViewById(R.id.btnEdit);

        loadItem();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            intent.putExtra(AddItemActivity.EXTRA_EDIT_ID, itemId);
            editLauncher.launch(intent);
        });
        btnShare.setOnClickListener(v -> shareItem());
        btnMarkReturned.setOnClickListener(v -> markAsReturned());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    // ── Load & bind ───────────────────────────────────────────────

    private void loadItem() {
        item = dbHelper.getItemById(itemId);
        if (item == null) { finish(); return; }

        // Hero image
        String imagePath = item.getImagePath();
        if (!TextUtils.isEmpty(imagePath) && new File(imagePath).exists()) {
            ivDetailImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        } else {
            ivDetailImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Name + type badge
        tvDetailName.setText(item.getName());
        tvDetailType.setText(item.getType());
        tvDetailType.setBackgroundResource("Lost".equals(item.getType())
                ? R.drawable.badge_lost : R.drawable.badge_found);
        tvDetailType.setTextColor(ContextCompat.getColor(this, R.color.white));

        // Info
        tvDetailDesc.setText(!TextUtils.isEmpty(item.getDesc())
                ? item.getDesc() : "No description provided.");
        tvDetailLocation.setText(!TextUtils.isEmpty(item.getLocation())
                ? item.getLocation() : "Location not specified.");
        // Category
        String cat = item.getCategory();
        tvDetailCategory.setText(!TextUtils.isEmpty(cat) ? cat : "Other");

        // Event date (when item was actually lost / found) — hidden if not set
        long eventDate = item.getEventDate();
        if (eventDate > 0) {
            String label = "Lost".equals(item.getType()) ? "Lost on: " : "Found on: ";
            tvDetailEventDate.setText(label +
                    new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                            .format(new Date(eventDate)));
            rowEventDate.setVisibility(android.view.View.VISIBLE);
        } else {
            rowEventDate.setVisibility(android.view.View.GONE);
        }

        tvDetailTimestamp.setText(item.getCreatedAt() > 0
                ? "Posted " + new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
                                  .format(new Date(item.getCreatedAt()))
                : "Date unknown");

        // Poster
        tvDetailPosterName.setText(!TextUtils.isEmpty(item.getPosterName())
                ? item.getPosterName() : "Anonymous");
        tvDetailContact.setText(!TextUtils.isEmpty(item.getPosterContact())
                ? item.getPosterContact() : "No contact info");

        applyStatusUI();
    }

    private void applyStatusUI() {
        boolean resolved = "resolved".equals(item.getStatus());
        tvResolvedBanner.setVisibility(resolved ? View.VISIBLE : View.GONE);
        btnMarkReturned.setVisibility(resolved ? View.GONE : View.VISIBLE);
    }

    // ── Actions ───────────────────────────────────────────────────

    /** Share item details (and photo if available) to WhatsApp or any app. */
    private void shareItem() {
        String text = "📢 *Lost & Found — " + item.getType() + "*\n\n" +
                "📦 *Item:* " + item.getName() + "\n";
        if (!TextUtils.isEmpty(item.getLocation()))
            text += "📍 *Location:* " + item.getLocation() + "\n";
        if (!TextUtils.isEmpty(item.getDesc()))
            text += "📝 *Description:* " + item.getDesc() + "\n";
        text += "👤 *Posted by:* " + (TextUtils.isEmpty(item.getPosterName())
                ? "Anonymous" : item.getPosterName());
        if (!TextUtils.isEmpty(item.getPosterContact()))
            text += "\n📞 *Contact:* " + item.getPosterContact();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String imagePath = item.getImagePath();

        if (!TextUtils.isEmpty(imagePath) && new File(imagePath).exists()) {
            // Share with image via FileProvider (no permission required)
            Uri imageUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", new File(imagePath));
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        } else {
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }

        // Try WhatsApp specifically; fall back to system chooser
        shareIntent.setPackage("com.whatsapp");
        try {
            startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            shareIntent.setPackage(null);
            startActivity(Intent.createChooser(shareIntent, "Share via…"));
        }
    }

    private void markAsReturned() {
        dbHelper.updateItemStatus(itemId, "resolved");
        item = dbHelper.getItemById(itemId);
        applyStatusUI();
        Toast.makeText(this, "Marked as returned! 🎉", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Permanently delete this item?")
                .setPositiveButton("Delete", (d, w) -> {
                    dbHelper.deleteItem(itemId);
                    Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
