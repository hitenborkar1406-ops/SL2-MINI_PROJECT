package com.example.mini_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UserSetupActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "user_prefs";
    public static final String KEY_NAME = "user_name";
    public static final String KEY_CONTACT = "user_contact";
    public static final String KEY_SETUP_DONE = "setup_done";

    private TextInputEditText etName, etContact;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setup);

        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContact);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void saveUserInfo() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_name_required));
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contact)) {
            etContact.setError(getString(R.string.error_contact_required));
            etContact.requestFocus();
            return;
        }

        // Persist user info
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_CONTACT, contact)
                .putBoolean(KEY_SETUP_DONE, true)
                .apply();

        Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
