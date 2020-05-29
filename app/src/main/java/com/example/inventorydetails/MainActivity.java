package com.example.inventorydetails;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorydetails.Database.ItemContract.ItemEntry;
import com.example.inventorydetails.Database.ItemDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private byte[] imgByte;
    private ItemDbHelper mDbHelper;
    private RecyclerView mRecyclerView;
    private ItemsRecyclerViewAdapter mAdapter;
    private final int ITEM_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler_view);

        //Set click listener for FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(ITEM_LOADER, null, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Convert bitmap object  into byte array that can used to store in database as Blob
    public byte[] convertImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void insertItem() {
        mDbHelper = new ItemDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.delivery_cart);
        imgByte = convertImage(icon);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemEntry.COLUMN_ITEM_NAME, "Salt");
        contentValues.put(ItemEntry.COLUMN_ITEM_IMAGE, imgByte);
        contentValues.put(ItemEntry.COLUMN_ITEM_SUPPLIER, 0);
        contentValues.put(ItemEntry.COLUMN_ITEM_MRP, 10);
        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, 20);
        Uri uri = getContentResolver().insert(ItemEntry.CONTENT_URI, contentValues);
    }

    private void deleteAllItems() {

        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from Inventory database");

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {ItemEntry._ID, ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_MRP, ItemEntry.COLUMN_ITEM_QUANTITY, ItemEntry.COLUMN_ITEM_SUPPLIER, ItemEntry.COLUMN_ITEM_IMAGE};

        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        mAdapter=new ItemsRecyclerViewAdapter(this,data);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.mCursorAdapter.swapCursor(null);
    }
}
