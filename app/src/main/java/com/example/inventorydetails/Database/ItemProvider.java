package com.example.inventorydetails.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.inventorydetails.Database.ItemContract.ItemEntry;

public class ItemProvider extends ContentProvider {

    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    private static final int ITEMS = 100;

    private static final int ITEMS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);

        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEMS_ID);

    }

    ItemDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);

        switch (match) {

            case ITEMS:
                cursor = db.query(ItemContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case ITEMS_ID:
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ItemContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Can not query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    private Uri insertItem(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a valid Name");
        }

        Integer quantity = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        if (quantity == null || quantity<0) {
            throw new IllegalArgumentException("Item requires the total quantity");
        }

        Integer mrp = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_MRP);
        if (mrp == null || mrp < 0) {
            throw new IllegalArgumentException("Item requires a valid MRP");
        }

        Integer supplier = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_SUPPLIER);
        if (supplier == null || !ItemEntry.isValidSupplier(supplier)) {
            throw new IllegalArgumentException("Item requires a valid supplier");
        }

        byte[] imgByte=contentValues.getAsByteArray(ItemEntry.COLUMN_ITEM_IMAGE);
        if (imgByte == null) {
            throw new IllegalArgumentException("Item requires an image");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(ItemEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowDeleted;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                rowDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEMS_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEMS_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Updating is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        String name = contentValues.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a valid Name");
        }

        Integer quantity = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Item requires the total quantity");
        }

        Integer mrp = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_MRP);
        if (mrp == null || mrp < 0) {
            throw new IllegalArgumentException("Item requires a valid MRP");
        }

        Integer supplier = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_SUPPLIER);
        if (supplier == null || !ItemEntry.isValidSupplier(supplier)) {
            throw new IllegalArgumentException("Item requires a valid supplier");
        }

        byte[] imgByte=contentValues.getAsByteArray(ItemEntry.COLUMN_ITEM_IMAGE);
        if (imgByte == null) {
            throw new IllegalArgumentException("Item requires an image");
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowUpdated = db.update(ItemEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        return rowUpdated;
    }
}
