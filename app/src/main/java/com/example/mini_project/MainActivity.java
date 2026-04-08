package com.example.mini_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    // All categories, mirrored from AddItemActivity
    private static final String[] CATEGORY_LABELS = {
            "All", "Electronics", "Keys", "Wallet / ID", "Books", "Clothing", "Accessories", "Other"
    };

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvItemCount;
    private ItemAdapter adapter;
    private DatabaseHelper dbHelper;
    private ExtendedFloatingActionButton fab;

    private String currentQuery = "";
    private String currentTypeFilter    = ""; // "" = All
    private String currentCategoryFilter = ""; // "" = All categories

    // Refreshes subtitle when returning from ProfileActivity
    private final ActivityResultLauncher<Intent> profileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> updateToolbarSubtitle());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // First-launch guard
        SharedPreferences prefs = getSharedPreferences(UserSetupActivity.PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(UserSetupActivity.KEY_SETUP_DONE, false)) {
            startActivity(new Intent(this, UserSetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Toolbar + action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateToolbarSubtitle();

        // Notifications
        NotificationHelper.createNotificationChannel(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        dbHelper = new DatabaseHelper(this);

        // Views
        recyclerView = findViewById(R.id.recyclerView);
        emptyState   = findViewById(R.id.emptyState);
        tvItemCount  = findViewById(R.id.tvItemCount);
        fab          = findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // FAB scroll behavior
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 4) fab.shrink(); else if (dy < -4) fab.extend();
            }
        });
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddItemActivity.class)));

        // ── Search box ──────────────────────────────────────────
        TextInputEditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                currentQuery = s.toString();
                loadItems();
            }
        });

        // ── Filter chips ────────────────────────────────────────
        ChipGroup chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipLost)       currentTypeFilter = "Lost";
            else if (id == R.id.chipFound) currentTypeFilter = "Found";
            else                           currentTypeFilter = "";
            loadItems();
        });

        // ── Category chips (built dynamically) ──────────────────
        ChipGroup chipGroupCategory = findViewById(R.id.chipGroupCategory);
        for (String label : CATEGORY_LABELS) {
            Chip chip = new Chip(this);
            chip.setId(View.generateViewId());
            chip.setText(label);
            chip.setCheckable(true);
            chip.setChecked(label.equals("All"));
            chip.setTag(label);
            chip.setTextSize(12f);
            chipGroupCategory.addView(chip);
        }
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip checked = group.findViewById(checkedIds.get(0));
            String selected = (String) checked.getTag();
            currentCategoryFilter = "All".equals(selected) ? "" : selected;
            loadItems();
        });
    }

    // ── Toolbar menu ──────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            profileLauncher.launch(new Intent(this, ProfileActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_dark_mode) {
            toggleDarkMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleDarkMode() {
        SharedPreferences prefs = getSharedPreferences(MyApplication.PREFS_APP, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(MyApplication.KEY_DARK_MODE, false);
        boolean newMode = !isDark;
        prefs.edit().putBoolean(MyApplication.KEY_DARK_MODE, newMode).apply();
        AppCompatDelegate.setDefaultNightMode(newMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    // ── Lifecycle ─────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void updateToolbarSubtitle() {
        SharedPreferences prefs = getSharedPreferences(UserSetupActivity.PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(UserSetupActivity.KEY_NAME, "");
        if (getSupportActionBar() != null && !name.isEmpty()) {
            getSupportActionBar().setSubtitle("Hi, " + name + " 👋");
        }
    }

    private void loadItems() {
        List<Item> items = dbHelper.searchAndFilterItems(
                currentQuery, currentTypeFilter, currentCategoryFilter);
        adapter.updateItems(items);

        // Item count label
        int count = items.size();
        String filterLabel = !currentTypeFilter.isEmpty() ? currentTypeFilter
                : !currentCategoryFilter.isEmpty() ? currentCategoryFilter : "";
        tvItemCount.setText(count + " item" + (count == 1 ? "" : "s")
                + (filterLabel.isEmpty() ? "" : " · " + filterLabel));

        // Empty state
        boolean empty = items.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}