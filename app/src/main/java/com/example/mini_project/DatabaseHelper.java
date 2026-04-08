package com.example.mini_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "lost_found.db";
    private static final int    DB_VERSION = 2; // bumped: added poster/status/timestamp cols

    public static final String TABLE_ITEMS      = "items";
    public static final String COL_ID           = "id";
    public static final String COL_NAME         = "name";
    public static final String COL_DESC         = "desc";
    public static final String COL_LOCATION     = "location";
    public static final String COL_TYPE         = "type";
    public static final String COL_IMAGE        = "image";
    public static final String COL_POSTER_NAME  = "poster_name";
    public static final String COL_POSTER_CONTACT = "poster_contact";
    public static final String COL_STATUS       = "status";
    public static final String COL_CREATED_AT   = "created_at";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COL_ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME            + " TEXT NOT NULL, " +
                    COL_DESC            + " TEXT, " +
                    COL_LOCATION        + " TEXT, " +
                    COL_TYPE            + " TEXT, " +
                    COL_IMAGE           + " TEXT, " +
                    COL_POSTER_NAME     + " TEXT, " +
                    COL_POSTER_CONTACT  + " TEXT, " +
                    COL_STATUS          + " TEXT DEFAULT 'active', " +
                    COL_CREATED_AT      + " INTEGER" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Non-destructive migration: add new columns to existing DB
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_POSTER_NAME    + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_POSTER_CONTACT + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_STATUS + " TEXT DEFAULT 'active'");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_CREATED_AT + " INTEGER DEFAULT 0");
        }
    }

    // ── Write ─────────────────────────────────────────────────────

    /** Insert a new item. Returns the row id, or -1 on failure. */
    public long insertItem(Item item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_NAME,           item.getName());
        v.put(COL_DESC,           item.getDesc());
        v.put(COL_LOCATION,       item.getLocation());
        v.put(COL_TYPE,           item.getType());
        v.put(COL_IMAGE,          item.getImagePath());
        v.put(COL_POSTER_NAME,    item.getPosterName());
        v.put(COL_POSTER_CONTACT, item.getPosterContact());
        v.put(COL_STATUS,         item.getStatus());
        v.put(COL_CREATED_AT,     item.getCreatedAt());
        return db.insert(TABLE_ITEMS, null, v);
    }

    /** Mark an item as resolved / active. */
    public void updateItemStatus(int id, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_STATUS, status);
        db.update(TABLE_ITEMS, v, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** Permanently delete an item. */
    public void deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ITEMS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // ── Read ──────────────────────────────────────────────────────

    /**
     * Search items by name/desc/location and/or filter by type.
     * Pass empty strings for no filter.
     */
    public List<Item> searchAndFilterItems(String query, String typeFilter) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder sel = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim() + "%";
            sel.append("(")
               .append(COL_NAME).append(" LIKE ? OR ")
               .append(COL_DESC).append(" LIKE ? OR ")
               .append(COL_LOCATION).append(" LIKE ?)");
            args.add(q); args.add(q); args.add(q);
        }
        if (typeFilter != null && !typeFilter.isEmpty()) {
            if (sel.length() > 0) sel.append(" AND ");
            sel.append(COL_TYPE).append(" = ?");
            args.add(typeFilter);
        }

        Cursor cursor = db.query(TABLE_ITEMS, null,
                sel.length() > 0 ? sel.toString() : null,
                args.isEmpty() ? null : args.toArray(new String[0]),
                null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do { items.add(cursorToItem(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    /** Convenience: all items, newest first. */
    public List<Item> getAllItems() {
        return searchAndFilterItems("", "");
    }

    /** Single item by id. */
    public Item getItemById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        Item item = null;
        if (cursor.moveToFirst()) item = cursorToItem(cursor);
        cursor.close();
        return item;
    }

    // ── Internal ─────────────────────────────────────────────────

    private Item cursorToItem(Cursor c) {
        return new Item(
                c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_DESC)),
                c.getString(c.getColumnIndexOrThrow(COL_LOCATION)),
                c.getString(c.getColumnIndexOrThrow(COL_TYPE)),
                c.getString(c.getColumnIndexOrThrow(COL_IMAGE)),
                c.getString(c.getColumnIndexOrThrow(COL_POSTER_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_POSTER_CONTACT)),
                c.getString(c.getColumnIndexOrThrow(COL_STATUS)),
                c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT))
        );
    }
}
