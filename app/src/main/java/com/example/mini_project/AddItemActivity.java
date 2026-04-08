package com.example.mini_project;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    // ── Intent extras ────────────────────────────────────────────
    /** Pass this extra with an item's integer ID to enter Edit mode. */
    public static final String EXTRA_EDIT_ID = "edit_item_id";

    // ── Categories ───────────────────────────────────────────────
    static final String[] CATEGORIES = {
            "Electronics", "Keys", "Wallet / ID", "Books", "Clothing", "Accessories", "Other"
    };

    // ── Views ────────────────────────────────────────────────────
    private TextInputEditText etItemName, etDescription, etLocation, etEventDate;
    private AutoCompleteTextView actvType, actvCategory;
    private ImageView ivImagePreview;
    private MaterialButton btnPickImage;

    // ── State ────────────────────────────────────────────────────
    private int    editItemId        = -1;   // -1 = add mode
    private String existingImagePath = null; // kept when user doesn't pick a new image
    private Uri    selectedImageUri  = null;
    private long   selectedEventDate = 0L;   // 0 = not set

    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Must register BEFORE setContentView (before STARTED state)
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri  = uri;
                        existingImagePath = null;      // replaced by new pick
                        ivImagePreview.setImageURI(uri);
                        btnPickImage.setText(R.string.btn_change_image);
                    }
                }
        );

        setContentView(R.layout.activity_add_item);

        // Detect edit mode
        editItemId = getIntent().getIntExtra(EXTRA_EDIT_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(editItemId >= 0 ? "Edit Item" : getString(R.string.title_add_item));
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);

        // ── Bind views ───────────────────────────────────────────
        etItemName    = findViewById(R.id.etItemName);
        etDescription = findViewById(R.id.etDescription);
        etLocation    = findViewById(R.id.etLocation);
        etEventDate   = findViewById(R.id.etEventDate);
        actvType      = findViewById(R.id.actvType);
        actvCategory  = findViewById(R.id.actvCategory);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        btnPickImage   = findViewById(R.id.btnPickImage);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        // ── Date picker ──────────────────────────────────────────
        etEventDate.setOnClickListener(v -> showDatePicker());

        // ── Dropdowns ────────────────────────────────────────────
        String[] types = {"Lost", "Found"};
        actvType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, types));

        actvCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIES));

        // ── Pre-fill or set defaults ──────────────────────────────
        if (editItemId >= 0) {
            prefillForEdit(editItemId);
            btnSave.setText("Save Changes");
        } else {
            actvType.setText(types[0], false);
            actvCategory.setText(CATEGORIES[CATEGORIES.length - 1], false);
        }

        // ── Image picker ─────────────────────────────────────────
        btnPickImage.setOnClickListener(v ->
                imagePickerLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                )
        );

        btnSave.setOnClickListener(v -> {
            if (editItemId >= 0) updateItem(); else saveItem();
        });
    }

    // ── Pre-fill existing data (Edit mode) ────────────────────────

    private void prefillForEdit(int id) {
        Item item = dbHelper.getItemById(id);
        if (item == null) { finish(); return; }

        etItemName.setText(item.getName());
        etDescription.setText(item.getDesc());
        etLocation.setText(item.getLocation());
        actvType.setText(item.getType(), false);
        actvCategory.setText(item.getCategory() != null ? item.getCategory() : "Other", false);

        // Restore previously saved event date
        selectedEventDate = item.getEventDate();
        if (selectedEventDate > 0) {
            etEventDate.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(new Date(selectedEventDate)));
        }

        // Load existing image preview
        existingImagePath = item.getImagePath();
        if (!TextUtils.isEmpty(existingImagePath)) {
            File imgFile = new File(existingImagePath);
            if (imgFile.exists()) {
                ivImagePreview.setImageBitmap(BitmapFactory.decodeFile(existingImagePath));
                btnPickImage.setText(R.string.btn_change_image);
            }
        }
    }

    // ── Save (Add mode) ───────────────────────────────────────────

    private void saveItem() {
        String name     = getText(etItemName);
        String desc     = getText(etDescription);
        String location = getText(etLocation);
        String type     = getDropdownText(actvType,     "Lost");
        String category = getDropdownText(actvCategory, "Other");

        if (TextUtils.isEmpty(name)) {
            etItemName.setError(getString(R.string.error_item_name_required));
            etItemName.requestFocus();
            return;
        }

        String imagePath = (selectedImageUri != null) ? copyImageToInternal(selectedImageUri) : null;

        SharedPreferences prefs = getSharedPreferences(UserSetupActivity.PREFS_NAME, MODE_PRIVATE);
        String posterName    = prefs.getString(UserSetupActivity.KEY_NAME,    "Anonymous");
        String posterContact = prefs.getString(UserSetupActivity.KEY_CONTACT, "");

        Item item = new Item(0, name, desc, location, type, imagePath,
                posterName, posterContact, "active",
                System.currentTimeMillis(), category, selectedEventDate);
        long insertedId = dbHelper.insertItem(item);

        if (insertedId > 0) {
            Toast.makeText(this, R.string.toast_item_added, Toast.LENGTH_SHORT).show();
            NotificationHelper.sendNotification(this);
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ── Update (Edit mode) ────────────────────────────────────────

    private void updateItem() {
        String name     = getText(etItemName);
        String desc     = getText(etDescription);
        String location = getText(etLocation);
        String type     = getDropdownText(actvType,     "Lost");
        String category = getDropdownText(actvCategory, "Other");

        if (TextUtils.isEmpty(name)) {
            etItemName.setError(getString(R.string.error_item_name_required));
            etItemName.requestFocus();
            return;
        }

        // Only copy a new image if the user explicitly picked one
        String newImagePath = (selectedImageUri != null) ? copyImageToInternal(selectedImageUri) : null;

        boolean ok = dbHelper.updateItem(
                editItemId, name, desc, location, type, category, newImagePath, selectedEventDate);

        if (ok) {
            Toast.makeText(this, "Item updated ✓", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Date picker ───────────────────────────────────────────────

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(selectedEventDate > 0
                        ? selectedEventDate
                        : MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedEventDate = selection;
            etEventDate.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(new Date(selectedEventDate)));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String getDropdownText(AutoCompleteTextView actv, String fallback) {
        return actv.getText() != null ? actv.getText().toString().trim() : fallback;
    }

    private String copyImageToInternal(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File outFile = new File(getFilesDir(), fileName);

            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            inputStream.close();
            return outFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
