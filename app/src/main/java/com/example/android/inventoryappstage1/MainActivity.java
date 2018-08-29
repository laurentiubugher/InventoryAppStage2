package com.example.android.inventoryappstage1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryappstage1.data.ClothesContract.ClothesEntry;


public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the clothes data loader
     */
    private static final int CLOTHES_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    ClothesCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Find the ListView which will be populated with the clothes data
        ListView clothesListView = (ListView) findViewById(R.id.list_view_clothes);


        //Find an set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        clothesListView.setEmptyView(emptyView);

        //sets the CursorAdapter on the Listview to create a list item for each row of the clothes data in the Cursor
        //There is no clothes data yet(until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ClothesCursorAdapter(this, null);
        clothesListView.setAdapter(mCursorAdapter);

        clothesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new Intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                //Form the content URI that represents the specific clothes that was clicked on,
                //by appending the "id" (passes as input to this method) onto the
                //{@link ClothesEntry#CONTENT_URI}.
                // For example, the URI would bee "content://com.example.android.inventoryappstage1/clothes/2"
                // if the clothes with ID 2 was clicked on.
                Uri currentClothesUri = ContentUris.withAppendedId(ClothesEntry.CONTENT_URI, id);

                intent.setData(currentClothesUri);

                startActivity(intent);

            }
        });

        getSupportLoaderManager().initLoader(CLOTHES_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllClothes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertProduct() {

        ContentValues values = new ContentValues();

        values.put(ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME, "Skirt");
        values.put(ClothesEntry.COLUMN_CLOTHES_PRICE, 258);
        values.put(ClothesEntry.COLUMN_CLOTHES_QUANTITY, 2);
        values.put(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME, "H & M");
        values.put(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER, 03745455343);

        Uri newUri = getContentResolver().insert(ClothesEntry.CONTENT_URI, values);

    }

    /**
     * Helper method to delete all clothes in the database.
     */
    private void deleteAllClothes() {
        int rowsDeleted = getContentResolver().delete(ClothesEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from clothes database");
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ClothesEntry._ID,
                ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME,
                ClothesEntry.COLUMN_CLOTHES_PRICE,
                ClothesEntry.COLUMN_CLOTHES_QUANTITY,
        };

        return new android.support.v4.content.CursorLoader(this,
                ClothesEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}




