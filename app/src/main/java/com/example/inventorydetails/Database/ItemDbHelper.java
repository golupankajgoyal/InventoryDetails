package com.example.inventorydetails.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.inventorydetails.Database.ItemContract.ItemEntry;

public class ItemDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version, If you need to change the Database schema, then you must increase the DATABASE_VERSION
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ItemDbHelper}.
     *
     * @param context of the app
     */
    public ItemDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_ITEM_TABLE = " CREATE TABLE " + ItemEntry.TABLE_NAME + " ( "
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ItemEntry.COLUMN_ITEM_NAME
                + " TEXT NOT NULL, " + ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SUPPLIER + " INTEGER NOT NULL, " + ItemEntry.COLUMN_ITEM_MRP
                + " INTEGER NOT NULL DEFAULT 0, " + ItemEntry.COLUMN_ITEM_IMAGE +
                " BLOB );";

        db.execSQL(SQL_CREATE_ITEM_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // The database is still at version 1, so there's nothing to do be done here.
    }
}
