package com.example.mini_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etContact;
    private TextView tvCurrentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Back button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContact);
        tvCurrentName = findViewById(R.id.tvCurrentName);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        // Pre-fill existing info
        SharedPreferences prefs = getSharedPreferences(UserSetupActivity.PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString(UserSetupActivity.KEY_NAME, "");
        String savedContact = prefs.getString(UserSetupActivity.KEY_CONTACT, "");

        tvCurrentName.setText(savedName);
        etName.setText(savedName);
        etContact.setText(savedContact);

        // Move cursor to end for convenience
        if (etName.getText() != null) {
            etName.setSelection(etName.getText().length());
        }

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contact)) {
            etContact.setError("Contact is required");
            etContact.requestFocus();
            return;
        }

        // Persist updated info
        SharedPreferences prefs = getSharedPreferences(UserSetupActivity.PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(UserSetupActivity.KEY_NAME, name)
                .putString(UserSetupActivity.KEY_CONTACT, contact)
                .apply();

        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK); // tell MainActivity to refresh subtitle
        finish();
    }
}
