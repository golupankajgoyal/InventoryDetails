package com.example.inventorydetails;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorydetails.Database.ItemContract.ItemEntry;

public class ItemsRecyclerViewAdapter extends RecyclerView.Adapter<ItemsRecyclerViewAdapter.ViewHolder> {

    CursorAdapter mCursorAdapter;

    Context mContext;

    public ItemsRecyclerViewAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                TextView nameTv = view.findViewById(R.id.name);
                TextView quantityTv = view.findViewById(R.id.quantity);
                TextView mrpTv = view.findViewById(R.id.mrp);
                TextView supplierTv = view.findViewById(R.id.dealer);
                ImageView itemImage = view.findViewById(R.id.item_image);

                int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
                int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
                int mrpColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_MRP);
                int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
                String[] supplierArray=mContext.getResources().getStringArray(R.array.array_dealer_options);
                int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
                byte[] imgByte = cursor.getBlob(imageColumnIndex);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

                nameTv.setText(cursor.getString(nameColumnIndex));
                quantityTv.setText("" + cursor.getInt(quantityColumnIndex));
                mrpTv.setText("" + cursor.getInt(mrpColumnIndex));
                supplierTv.setText("" + supplierArray[cursor.getInt(supplierColumnIndex)]);
                itemImage.setImageBitmap(bitmap);

            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Passing the inflater job to the cursor-adapter
        View view = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        mCursorAdapter.getCursor().moveToPosition(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,EditorActivity.class);
                Uri currentUri= ContentUris.withAppendedId(ItemEntry.CONTENT_URI,position+2);
                intent.setData(currentUri);
                mContext.startActivity(intent);
            }
        });
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
