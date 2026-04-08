package com.example.mini_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide default action bar — the splash has its own full-screen layout
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs =
                    getSharedPreferences(UserSetupActivity.PREFS_NAME, MODE_PRIVATE);
            // If the user hasn't set up a profile yet, go there first
            Class<?> target = prefs.getBoolean(UserSetupActivity.KEY_SETUP_DONE, false)
                    ? MainActivity.class
                    : UserSetupActivity.class;
            startActivity(new Intent(SplashActivity.this, target));
            finish();
        }, SPLASH_DELAY_MS);
    }

    @Override
    @SuppressWarnings("MissingSuperCall")
    public void onBackPressed() {
        // Intentionally swallow back press during the splash delay
    }
}
