package com.example.inventorydetails.Database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ItemContract {

    private ItemContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.inventorydetails";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ITEMS = "items";

    public static final class ItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * The MIME type for the list of the Items
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type for the a single item of table
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        public final static String TABLE_NAME = "items";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_ITEM_NAME = "name";

        public final static String COLUMN_ITEM_QUANTITY = "quantity";

        public final static String COLUMN_ITEM_MRP = "mrp";

        public final static String COLUMN_ITEM_IMAGE = "image";

        public final static String COLUMN_ITEM_SUPPLIER = "supplier";

        public final static int SUPPLIER_TATA = 0;
        public final static int SUPPLIER_HALDIRAM = 1;
        public final static int SUPPLIER_LOCAL = 2;

        public static boolean isValidSupplier(int supplier) {

            if (supplier == SUPPLIER_TATA || supplier == SUPPLIER_HALDIRAM || supplier == SUPPLIER_LOCAL) {
                return true;
            } else return false;
        }
    }
}
