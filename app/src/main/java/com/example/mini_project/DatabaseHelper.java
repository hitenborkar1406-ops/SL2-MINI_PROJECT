package com.example.mini_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "campus_lost_found.db";
    private static final int    DATABASE_VERSION = 3;   // v3 adds 'category'

    private static final String TABLE_ITEMS = "items";

    // Column names
    private static final String COL_ID             = "_id";
    private static final String COL_NAME           = "name";
    private static final String COL_DESC           = "description";
    private static final String COL_LOCATION       = "location";
    private static final String COL_TYPE           = "type";
    private static final String COL_IMAGE_PATH     = "image_path";
    private static final String COL_POSTER_NAME    = "poster_name";
    private static final String COL_POSTER_CONTACT = "poster_contact";
    private static final String COL_STATUS         = "status";
    private static final String COL_CREATED_AT     = "created_at";
    private static final String COL_CATEGORY       = "category";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
            COL_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME           + " TEXT NOT NULL, " +
            COL_DESC           + " TEXT, " +
            COL_LOCATION       + " TEXT, " +
            COL_TYPE           + " TEXT, " +
            COL_IMAGE_PATH     + " TEXT, " +
            COL_POSTER_NAME    + " TEXT, " +
            COL_POSTER_CONTACT + " TEXT, " +
            COL_STATUS         + " TEXT DEFAULT 'active', " +
            COL_CREATED_AT     + " INTEGER DEFAULT 0, " +
            COL_CATEGORY       + " TEXT DEFAULT 'Other'" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ── Schema creation ───────────────────────────────────────────

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Non-destructive: add only missing columns, preserving user data
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_POSTER_NAME    + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_POSTER_CONTACT + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_STATUS         + " TEXT DEFAULT 'active'");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_CREATED_AT     + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_CATEGORY + " TEXT DEFAULT 'Other'");
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────

    public long insertItem(Item item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME,           item.getName());
        values.put(COL_DESC,           item.getDesc());
        values.put(COL_LOCATION,       item.getLocation());
        values.put(COL_TYPE,           item.getType());
        values.put(COL_IMAGE_PATH,     item.getImagePath());
        values.put(COL_POSTER_NAME,    item.getPosterName());
        values.put(COL_POSTER_CONTACT, item.getPosterContact());
        values.put(COL_STATUS,         item.getStatus() != null ? item.getStatus() : "active");
        values.put(COL_CREATED_AT,     item.getCreatedAt());
        values.put(COL_CATEGORY,       item.getCategory() != null ? item.getCategory() : "Other");
        return getWritableDatabase().insert(TABLE_ITEMS, null, values);
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        Cursor cursor = getReadableDatabase()
                .query(TABLE_ITEMS, null, null, null, null, null, COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            do { items.add(cursorToItem(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public Item getItemById(int id) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_ITEMS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        if (cursor.moveToFirst()) {
            Item item = cursorToItem(cursor);
            cursor.close();
            return item;
        }
        cursor.close();
        return null;
    }

    /** Combined search + type + category filter (any combination). */
    public List<Item> searchAndFilterItems(String query, String typeFilter, String categoryFilter) {
        List<Item> items = new ArrayList<>();
        StringBuilder selection = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            selection.append("(")
                     .append(COL_NAME).append(" LIKE ? OR ")
                     .append(COL_DESC).append(" LIKE ? OR ")
                     .append(COL_LOCATION).append(" LIKE ?)");
            String like = "%" + query + "%";
            args.add(like); args.add(like); args.add(like);
        }

        if (typeFilter != null && !typeFilter.isEmpty()) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COL_TYPE).append("=?");
            args.add(typeFilter);
        }

        if (categoryFilter != null && !categoryFilter.isEmpty()) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COL_CATEGORY).append("=?");
            args.add(categoryFilter);
        }

        Cursor cursor = getReadableDatabase().query(
                TABLE_ITEMS, null,
                selection.length() > 0 ? selection.toString() : null,
                args.isEmpty() ? null : args.toArray(new String[0]),
                null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do { items.add(cursorToItem(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public void deleteItem(int id) {
        getWritableDatabase().delete(TABLE_ITEMS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateItemStatus(int id, String status) {
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        getWritableDatabase().update(TABLE_ITEMS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** Full update — called when a user edits an existing item. */
    public boolean updateItem(int id, String name, String desc, String location,
                              String type, String category, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME,       name);
        values.put(COL_DESC,       desc);
        values.put(COL_LOCATION,   location);
        values.put(COL_TYPE,       type);
        values.put(COL_CATEGORY,   category != null ? category : "Other");
        if (imagePath != null) {
            // Only overwrite image if the user actually picked a new one
            values.put(COL_IMAGE_PATH, imagePath);
        }
        int rows = getWritableDatabase()
                .update(TABLE_ITEMS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }


    // ── Helper ────────────────────────────────────────────────────

    private Item cursorToItem(Cursor cursor) {
        // Use getColumnIndex (not OrThrow) for category to be safe with old DB rows
        int catIdx = cursor.getColumnIndex(COL_CATEGORY);
        return new Item(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_POSTER_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_POSTER_CONTACT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
                catIdx >= 0 ? cursor.getString(catIdx) : "Other"
        );
    }
}
