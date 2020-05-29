package com.example.inventorydetails;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.inventorydetails.Database.ItemContract;
import com.example.inventorydetails.Database.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private int mDealer = ItemContract.ItemEntry.SUPPLIER_LOCAL;
    private Spinner mSpinner;
    private int EXISTING_LOADER=0;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private EditText nameEditText;
    private EditText quantityEditText;
    private EditText mrpEditText;
    private Button imageButton;
    private boolean mHasItemChanged=false;
    private Uri mCurrentItemUri;
    private Context context;
    private byte[] imgByte=null;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mHasItemChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener=new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mHasItemChanged=true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {

            setTitle("Add an Item");
            invalidateOptionsMenu();

        } else {
            setTitle("Edit Item");

            getSupportLoaderManager().initLoader(EXISTING_LOADER,null,this);
        }

        context = this;
        mSpinner = findViewById(R.id.spinner_dealer);
        nameEditText = findViewById(R.id.edit_item_name);
        quantityEditText = findViewById(R.id.edit_item_quantity);
        mrpEditText = findViewById(R.id.edit_item_mrp);
        imageButton = findViewById(R.id.image_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
            }
        });

        mSpinner.setOnTouchListener(mTouchListener);
        nameEditText.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        mrpEditText.setOnTouchListener(mTouchListener);
        imageButton.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    private void setupSpinner() {

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_dealer_options, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(spinnerAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //Retrieve selected dealer from spinner
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("Tata"))
                        mDealer = ItemContract.ItemEntry.SUPPLIER_TATA;
                    else if (selection.equals("Haldiram"))
                        mDealer = ItemContract.ItemEntry.SUPPLIER_HALDIRAM;
                    else
                        mDealer = ItemContract.ItemEntry.SUPPLIER_LOCAL;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDealer = ItemContract.ItemEntry.SUPPLIER_LOCAL;
            }
        });
    }

    private void saveItem() {
        ContentValues contentValues = new ContentValues();
        String name = nameEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String mrpString = mrpEditText.getText().toString().trim();

        if (mCurrentItemUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(mrpString)
                && mDealer == ItemEntry.SUPPLIER_LOCAL) {
            return;
        }

        if (imgByte == null) {
            Toast.makeText(context, "Select an Image", Toast.LENGTH_SHORT).show();
            return;
        } else
            contentValues.put(ItemEntry.COLUMN_ITEM_IMAGE, imgByte);

        contentValues.put(ItemEntry.COLUMN_ITEM_NAME, name);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

        int mrp = 0;
        if (!TextUtils.isEmpty(mrpString)) {
            mrp = Integer.parseInt(mrpString);
        }
        contentValues.put(ItemEntry.COLUMN_ITEM_MRP, mrp);

        contentValues.put(ItemEntry.COLUMN_ITEM_SUPPLIER, mDealer);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, contentValues);

            if (newUri == null) {
                Toast.makeText(context, "Error with savinh Item", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, "Item saved", Toast.LENGTH_SHORT).show();
        } else {
            int rowAffected = getContentResolver().update(mCurrentItemUri, contentValues, null, null);

            if (rowAffected == 0) {
                Toast.makeText(context, "Error in Updating Item", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imgByte = convertImage(photo);
            imageButton.setText("Image Selected");
        }


    }

    //Convert bitmap object  into byte array that can used to store in database as Blob
    public byte[] convertImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * this method is called after invalidateOptionMenu()
     * so that the menu can be updated (some item can be made visible or hide)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeletConfirmationDialog();
                return true;
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}
                if (!mHasItemChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        if (!mHasItemChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage("Discard your changes and quit editing?")
                .setPositiveButton("Discard", discardButtonClickListener)
                .setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }).create();

        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeletConfirmationDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Delete this item?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem();
                        finish();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }).create();
        alertDialog.show();
    }

    private void deleteItem() {

        if (mCurrentItemUri != null) {

            int rowAffected = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowAffected == 0) {
                Toast.makeText(this, "Error with deleting item", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                String[] projection = {
                ItemEntry._ID, ItemEntry.COLUMN_ITEM_NAME, ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_MRP, ItemEntry.COLUMN_ITEM_SUPPLIER, ItemEntry.COLUMN_ITEM_IMAGE
        };

        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

                if (cursor == null || cursor.getCount() < 0) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int mrpColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_MRP);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);

            nameEditText.setText(cursor.getString(nameColumnIndex));
            quantityEditText.setText(Integer.toString(cursor.getInt(quantityColumnIndex)));
            mrpEditText.setText(Integer.toString(cursor.getInt(mrpColumnIndex)));
            imageButton.setText("Select an Image");
            imgByte=cursor.getBlob(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE));


            switch (cursor.getInt(supplierColumnIndex)) {
                case ItemEntry.SUPPLIER_TATA:
                    mSpinner.setSelection(0);
                    break;
                case ItemEntry.SUPPLIER_HALDIRAM:
                    mSpinner.setSelection(1);
                    break;
                default:
                    mSpinner.setSelection(2);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

//    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        String[] projection = {
//                ItemEntry._ID, ItemEntry.COLUMN_ITEM_NAME, ItemEntry.COLUMN_ITEM_QUANTITY,
//                ItemEntry.COLUMN_ITEM_MRP, ItemEntry.COLUMN_ITEM_SUPPLIER, ItemEntry.COLUMN_ITEM_IMAGE
//        };
//
//        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
//
//        if (cursor == null || cursor.getCount() < 1) {
//            return;
//        }
//
//        if (cursor.moveToFirst()) {
//
//            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
//            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
//            int mrpColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_MRP);
//            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
//
//            nameEditText.setText(cursor.getString(nameColumnIndex));
//            quantityEditText.setText(Integer.toString(cursor.getInt(quantityColumnIndex)));
//            mrpEditText.setText(Integer.toString(cursor.getInt(mrpColumnIndex)));
//            imageButton.setText("Select an Image");
//
//
//            switch (cursor.getInt(supplierColumnIndex)) {
//                case ItemEntry.SUPPLIER_TATA:
//                    mSpinner.setSelection(0);
//                    break;
//                case ItemEntry.SUPPLIER_HALDIRAM:
//                    mSpinner.setSelection(1);
//                    break;
//                default:
//                    mSpinner.setSelection(2);
//                    break;
//            }
//        }
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
//
//        nameEditText.setText("");
//        quantityEditText.setText("");
//        mrpEditText.setText("");
//        mSpinner.setSelection(2);
//    }
}
