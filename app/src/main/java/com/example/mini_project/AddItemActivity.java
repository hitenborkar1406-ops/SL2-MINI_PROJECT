package com.example.mini_project;

import android.content.SharedPreferences;
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
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddItemActivity extends AppCompatActivity {

    // ── Categories available to the user ─────────────────────────
    static final String[] CATEGORIES = {
            "Electronics", "Keys", "Wallet / ID", "Books", "Clothing", "Accessories", "Other"
    };

    private TextInputEditText etItemName, etDescription, etLocation;
    private AutoCompleteTextView actvType, actvCategory;
    private ImageView ivImagePreview;
    private MaterialButton btnPickImage;

    private Uri selectedImageUri = null;
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
                        selectedImageUri = uri;
                        ivImagePreview.setImageURI(uri);
                        btnPickImage.setText(R.string.btn_change_image);
                    }
                }
        );

        setContentView(R.layout.activity_add_item);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);

        // Bind views
        etItemName   = findViewById(R.id.etItemName);
        etDescription= findViewById(R.id.etDescription);
        etLocation   = findViewById(R.id.etLocation);
        actvType     = findViewById(R.id.actvType);
        actvCategory = findViewById(R.id.actvCategory);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        btnPickImage = findViewById(R.id.btnPickImage);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        // Type dropdown
        String[] types = {"Lost", "Found"};
        actvType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, types));
        actvType.setText(types[0], false);

        // Category dropdown
        actvCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIES));
        actvCategory.setText(CATEGORIES[CATEGORIES.length - 1], false); // default: Other

        btnPickImage.setOnClickListener(v ->
                imagePickerLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                )
        );

        btnSave.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        String name     = etItemName.getText()   != null ? etItemName.getText().toString().trim()    : "";
        String desc     = etDescription.getText()!= null ? etDescription.getText().toString().trim() : "";
        String location = etLocation.getText()   != null ? etLocation.getText().toString().trim()    : "";
        String type     = actvType.getText()     != null ? actvType.getText().toString().trim()      : "Lost";
        String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim()  : "Other";

        // Validate required field
        if (TextUtils.isEmpty(name)) {
            etItemName.setError(getString(R.string.error_item_name_required));
            etItemName.requestFocus();
            return;
        }

        // Copy selected image to internal storage (safe against URI invalidation)
        String imagePath = null;
        if (selectedImageUri != null) {
            imagePath = copyImageToInternal(selectedImageUri);
        }

        // Attach poster info from user profile
        SharedPreferences prefs = getSharedPreferences(UserSetupActivity.PREFS_NAME, MODE_PRIVATE);
        String posterName    = prefs.getString(UserSetupActivity.KEY_NAME,    "Anonymous");
        String posterContact = prefs.getString(UserSetupActivity.KEY_CONTACT, "");

        Item item = new Item(0, name, desc, location, type, imagePath,
                             posterName, posterContact, "active",
                             System.currentTimeMillis(), category);
        long id = dbHelper.insertItem(item);

        if (id > 0) {
            Toast.makeText(this, R.string.toast_item_added, Toast.LENGTH_SHORT).show();
            NotificationHelper.sendNotification(this);
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
        }
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
