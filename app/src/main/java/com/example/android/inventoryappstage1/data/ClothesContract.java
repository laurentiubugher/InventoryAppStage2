package com.example.android.inventoryappstage1.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ClothesContract {

    private ClothesContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage1";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // The uri can go 2 paths later. This path is for the whole table clothes
    public static final String PATH_CLOTHES = "clothes";

    public static class ClothesEntry implements BaseColumns {
        public static final String TABLE_NAME = "clothes";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CLOTHES_PRODUCT_NAME = "product_name";
        public static final String COLUMN_CLOTHES_PRICE = "price";
        public static final String COLUMN_CLOTHES_QUANTITY = "quantity";
        public static final String COLUMN_CLOTHES_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";

        // this must be inside the PetEntry class Creates an Uri with BASE_CONTENT_URI, PATH_PETS.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CLOTHES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of clothes.
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLOTHES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single clothes.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLOTHES;

    }
}
