package com.example.android.inventoryappstage1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryappstage1.data.ClothesContract.ClothesEntry;

public class ProductDbHelper extends SQLiteOpenHelper {

    /**
     * Creates constants for the databse name and database version
     */
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "clothes.db";

    /**
     * Creates a constructor
     */

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_CLOTHE_TABLE = "CREATE TABLE " + ClothesEntry.TABLE_NAME + " ("
                + ClothesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME + " TEXT, "
                + ClothesEntry.COLUMN_CLOTHES_PRICE + " INTEGER NOT NULL, "
                + ClothesEntry.COLUMN_CLOTHES_QUANTITY + " INTEGER, "
                + ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER + " LONG NOT NULL);";

        db.execSQL(SQL_CREATE_CLOTHE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
