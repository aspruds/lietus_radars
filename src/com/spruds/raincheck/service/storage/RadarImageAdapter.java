package com.spruds.raincheck.service.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.preference.PreferenceManager;
import java.io.ByteArrayOutputStream;
import java.util.Date;

public class RadarImageAdapter {
    private static final int DEFAULT_CACHE_SIZE = 10;
    private static final int DEFAULT_CACHE_FREQUENCY = -1;
    private static final String PREF_HISTORY_CACHE_SIZE = "PREF_HISTORY_CACHE_SIZE";
    private static final String PREF_HISTORY_UPDATE_FREQ = "PREF_HISTORY_UPDATE_FREQ";
    
    private Context context;
    private WeatherImageOpenHelper helper;
    private SQLiteDatabase db;

    private static final String DATABASE_NAME = "raincheck.db";
    private static final int DATABASE_VERSION = 1;

    public RadarImageAdapter(Context context) {
        this.context = context;
        helper = new WeatherImageOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open() throws SQLiteException {
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }
    }

    public void close() {
        db.close();
    }

    private void deleteExpiredItems() {
        long count = getImageCount();
        if(count >= getCacheSize()) {
            Date lastDate = getLastValidTimestamp();
            deleteItemsOlderThan(lastDate);
        }
    }

    private void deleteItemsOlderThan(Date date) {
        String query = "DELETE FROM images WHERE date_fetched <= ?";
        db.execSQL(query, new Object[]{date.getTime()});
    }

    public Date getLastTimestamp() {
        String query = "SELECT date_fetched FROM images ORDER BY date_fetched "
                + "DESC LIMIT ?";
        try {
            long lastTimestamp = DatabaseUtils.longForQuery(db, query, new String[]{"1"});
            return new Date(lastTimestamp);
        }
        catch(SQLiteDoneException ex) {
            return null;
        }
    }

    
    private Date getLastValidTimestamp() {
        String query = "SELECT date_fetched FROM images ORDER BY date_fetched "
                + "DESC LIMIT 1 OFFSET ?";

        try {
            long lastTimestamp = DatabaseUtils.longForQuery(db, query,
                new String[]{Long.toString(getCacheSize()-1)});
            return new Date(lastTimestamp);
        }
        catch(SQLiteDoneException ex) {
            return null;
        }
    }

    public long getImageCount() {
        String query = "SELECT COUNT(*) FROM images";
        return DatabaseUtils.longForQuery(db, query, null);
    }

    public long insertImage(Bitmap bmp) {
        deleteExpiredItems();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG,100, bos);

        // Create a new row of values to insert.
        ContentValues imageValues = new ContentValues();
        // Assign values for each row.
        imageValues.put("image", bos.toByteArray());
        imageValues.put("date_fetched", new Date().getTime());
        // Insert the row.
        return db.insert("images", null, imageValues);
    }

    /**
     * @return the cacheSize
     */
    public int getCacheSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String size = prefs.getString(PREF_HISTORY_CACHE_SIZE, String.valueOf(DEFAULT_CACHE_SIZE));
        return Integer.parseInt(size);
    }

    public int getCacheFrequency() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String size = prefs.getString(PREF_HISTORY_UPDATE_FREQ, String.valueOf(DEFAULT_CACHE_SIZE));
        return Integer.parseInt(size);
    }

    class WeatherImageOpenHelper extends SQLiteOpenHelper {
        Context context;
        String name;
        CursorFactory factory;
        int version;

        public WeatherImageOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            this.context = context;
            this.name = name;
            this.factory = factory;
            this.version = version;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE images ("+
                     "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                     "image BLOB NOT NULL,"+
                     "date_fetched INTEGER NOT NULL)";

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS images");
            onCreate(db);
        }
    }
}
