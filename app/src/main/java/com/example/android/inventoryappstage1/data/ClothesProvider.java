package com.example.android.inventoryappstage1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.inventoryappstage1.data.ClothesContract.ClothesEntry;

import java.util.Objects;

public class ClothesProvider extends ContentProvider {

    private static final String LOG_TAG = ClothesProvider.class.getSimpleName();

    private static final int CLOTHES = 100;
    private static final int CLOTHES_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ClothesContract.CONTENT_AUTHORITY, ClothesContract.PATH_CLOTHES, CLOTHES);
        sUriMatcher.addURI(ClothesContract.CONTENT_AUTHORITY, ClothesContract.PATH_CLOTHES + "/#", CLOTHES_ID);
    }

    /**
     * Database helper object
     */

    private ProductDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */

    public boolean onCreate() {

        // Creates a new database object.
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //This cursor will hold the result of the query
        Cursor cursor;

        //Figure out if the Uri matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                //For the CLOTHES code, query the clothes table directly with the given
                // projection, selection, selection arguments, and sort order. The curso
                // could contain multiple row of the clothes tabel.

                cursor = database.query(ClothesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CLOTHES_ID:
                // For the CLOTHES_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryappstage1/clothes/2",
                // the selction will be "_id=?" and the selction argument will be a
                // String array containing the actual ID of 2 in this case.
                //
                // For every "?" in the selction, we need to have an element in the selection
                // argument that will fil in the "?". Since we have 1 question mar in the
                // selection, we have 1 String in the selection argument String array.
                selection = ClothesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the clothes table where the _id quals 3 to return a
                // Curso containing that row of the table.

                cursor = database.query(ClothesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                return ClothesEntry.CONTENT_LIST_TYPE;
            case CLOTHES_ID:
                return ClothesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unkown URI" + uri + " with match " + match);
        }

    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                return insertClothes(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);

        }
    }

    private Uri insertClothes(Uri uri, ContentValues values) {

        String productName = values.getAsString(ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Clothes need a name");
        }
        Integer price = values.getAsInteger(ClothesEntry.COLUMN_CLOTHES_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("The product needs a price over 0");
        }

        Integer quantity = values.getAsInteger(ClothesEntry.COLUMN_CLOTHES_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("The quantity must be 0 or above");
        }
        String supplierName = values.getAsString(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException(" A name of the supplier must be filled in");
        }

        Long supplierPhoneNumber = values.getAsLong(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER);
        if (supplierPhoneNumber == null)
            throw new IllegalArgumentException(" A valid phone number must be filled in");

        //Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Insert a new product with the given values
        long id = database.insert(ClothesEntry.TABLE_NAME, null, values);

        //If the ID is -1 then the insertion falied. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Falied to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the clothes content URI
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);

    }

    /**
     * Delete the data at the given slection and selection arguments.
     */

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        // get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Trach the number of rows that were deleted

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                rowsDeleted = database.delete(ClothesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CLOTHES_ID:
                //Delete a single row given by the ID in the URI
                selection = ClothesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ClothesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were delted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Updates the data at the given params with the new Content Values
     *
     * @param uri
     * @param contentValues
     * @param selection
     * @param selectionArgs
     * @return new Content values
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CLOTHES:
                return updateClothes(uri, contentValues, selection, selectionArgs);
            case CLOTHES_ID:
                //For the CLOTHES_ID code, extract out the ID from the URI,
                // so we know whcih row to update. Selection will be "_id=?" and selection
                // arguments will be String array containing the actual ID.
                selection = ClothesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateClothes(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update clothes in the database with the given content calues. Apply the changes to the rows
     * specified in the selection and selection aruments (which could be 0 or 1 or more clothes).
     * Return the number of rows that were succesfully updated.
     */
    private int updateClothes(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //If the {@link ClothesEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME)) {
            String productName = values.getAsString(ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException(" Product requires a name");
            }
        }

        //If the {@link ClothesEntry#COLUMN_CLOTHES_PRICE} key is present,
        // check that the price value is valid.

        if (values.containsKey(ClothesEntry.COLUMN_CLOTHES_PRICE)) {
            Integer price = values.getAsInteger(ClothesEntry.COLUMN_CLOTHES_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires a valid price either 0 or above");
            }
        }

        //If the {@link ClothesEntry#COLUMN_CLOTHES_SUPPLIER_NAME} key is present,
        // check that the price value is valid.
        if (values.containsKey(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Please insert a valid supplier name");
            }
        }

        //If the {@link ClothesEntry#COLUMN_CLOTHES_SUPPLLIER_PHONE_NUMBER key is present,
        // check that the phone number value is valid.
        if (values.containsKey(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER)) {
            Long supplierPhoneNumber = values.getAsLong(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER);
            if (supplierPhoneNumber == null) {
                throw new IllegalArgumentException("Plese insert a valid phone number");
            }
            // If there are no values to update, then dont try to update the database.
            if (values.size() == 0) {
                return 0;
            }
        }

        // Otherwise , get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ClothesEntry.TABLE_NAME, values, selection, selectionArgs);

        //If 1 or more rows were updated, then notify all listeners that the data at the
        // given URL has changed
        if (rowsUpdated != 0)
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        // Return the number of rows updated
        return rowsUpdated;
    }
}