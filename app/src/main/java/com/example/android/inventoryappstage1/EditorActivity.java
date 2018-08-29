package com.example.android.inventoryappstage1;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventoryappstage1.data.ClothesContract.ClothesEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the clothes data loader
     */
    private static final int EXISTING_CLOTHES_LOADER = 0;

    /**
     * Content URI for the existing clothes(null if its a new clothes)
     */

    private Uri mCurrentClothesUri;

    /**
     * EditText field to enter the clothes name
     */
    private EditText mClothesNameEditText;

    /**
     * EditText field to enter the price for the clothes
     */
    private EditText mClothesPriceEditText;

    /**
     * EditText field to enter the Quantity for the clothes
     */
    private EditText mClothesQuantityEditText;

    /**
     * EditText field to enter the Supplier name
     */
    private EditText mSupplierNameEditText;

    /**
     * EditText field to enter the supplier phone number
     */

    private EditText mSupplierPhoneNumberEditText;

    /**
     * Boolean flag that keeps trach of whether the clothes has been edited (true) or not (false)
     */

    private boolean mClothesHasChanged = false;

    /**
     * int for given quantity
     */
    private int givenQuantity;

    //OnTouchListener that listens for any user touches on a View, implying that they are modyfying
    // the view, and we change the mClothesHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mClothesHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that was used to launch this activity,
        // in order to figure out if we are creating a new clothes product or editing an existing one.

        Intent intent = getIntent();
        mCurrentClothesUri = intent.getData();

        if (mCurrentClothesUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product_Clothes));
            //Invalidate the options menu, so the "Delet" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_existing_product_clothes));
            getLoaderManager().initLoader(EXISTING_CLOTHES_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mClothesNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mClothesPriceEditText = (EditText) findViewById(R.id.edit_price_field);
        mClothesQuantityEditText = (EditText) findViewById(R.id.edit_clothes_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name_text_field);
        mSupplierPhoneNumberEditText = (EditText) findViewById(R.id.edit_phone_text_field);

        ImageButton mIncrease = (ImageButton) findViewById(R.id.edit_quantity_increase);
        ImageButton mDecrease = (ImageButton) findViewById(R.id.edit_quantity_decrease);


        mClothesNameEditText.setOnTouchListener(mTouchListener);
        mClothesPriceEditText.setOnTouchListener(mTouchListener);
        mClothesQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);
        mIncrease.setOnTouchListener(mTouchListener);
        mDecrease.setOnTouchListener(mTouchListener);

        //increase quantity
        mIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String quantity = mClothesQuantityEditText.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    Toast.makeText(EditorActivity.this, R.string.editor_quantity_field_cant_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    givenQuantity = Integer.parseInt(quantity);
                    mClothesQuantityEditText.setText(String.valueOf(givenQuantity + 1));
                }
            }
        });

        //decrease quantity with button

        mDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = mClothesQuantityEditText.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    Toast.makeText(EditorActivity.this, R.string.editor_quantity_field_cant_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    givenQuantity = Integer.parseInt(quantity);
                    // to validate if quantity is greater than =
                    if ((givenQuantity - 1) >= 0) {
                        mClothesQuantityEditText.setText(String.valueOf(givenQuantity - 1));
                    } else {
                        Toast.makeText(EditorActivity.this, R.string.editor_quantity_cant_be_less_then_0, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //setting up the phone button in the editor activity to call the supplier
        final ImageButton mPhoneCallSupplierButton = (ImageButton) findViewById(R.id.call_supplier_phone_button);

        mPhoneCallSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mSupplierPhoneNumberEditText.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);

                }
            }

        });
    }

    @Override
    public void onBackPressed() {
        // If the clothes editing hasnt changed, continue with handling back button press
        if (!mClothesHasChanged) {
            super.onBackPressed();
            return;
        }
        //Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //User clicked "Discard" button, close the current activity-
                        finish();

                    }
                };
        //Show the dialog that there are unsaved changes
        showUnsavedChangedDialog(discardButtonClickListener);
    }

    private void saveProductClothes() {

        String productNameClothesString = mClothesNameEditText.getText().toString().trim();
        String priceClothesString = mClothesPriceEditText.getText().toString().trim();
        String quantityClothesString = mClothesQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        //Check if this is supposed to be a new clothes product
        // and check if all the fields in the editor are blank

        if (mCurrentClothesUri == null &&
                TextUtils.isEmpty(productNameClothesString) && TextUtils.isEmpty(priceClothesString) &&
                TextUtils.isEmpty(quantityClothesString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString))

        {
            Toast.makeText(this, getString(R.string.editor_fill_in), Toast.LENGTH_LONG).show();
            //Since no fields were modified, we can return early without creating a new clothes product.
            // No need to create ContentValues and no need to do any ContenProvider operations.
            return;
        }
        if (TextUtils.isEmpty(productNameClothesString)) {
            mClothesNameEditText.setError(getString(R.string.editor_question_for_empty_field_name));
            return;
        }
        if (TextUtils.isEmpty(priceClothesString)) {
            mClothesPriceEditText.setError(getString(R.string.editor_question_for_empty_field_price));
            return;
        }

        if (TextUtils.isEmpty(quantityClothesString)) {
            mClothesQuantityEditText.setError(getString(R.string.editor_quantity_field_cant_be_empty));
            return;
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            mSupplierNameEditText.setError(getString(R.string.editor_question_for_empty_field_supplier_name));
            return;
        }
        if (TextUtils.isEmpty(supplierPhoneNumberString)) {
            mSupplierPhoneNumberEditText.setError(getString(R.string.editor_question_for_empty_field_supplier_phone_number));
            return;
        }

        //Create a ContentValues object where colum name are the keys,
        // and pet attributes from the editro are the values
        ContentValues values = new ContentValues();
        values.put(ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME, productNameClothesString);
        values.put(ClothesEntry.COLUMN_CLOTHES_PRICE, priceClothesString);
        values.put(ClothesEntry.COLUMN_CLOTHES_QUANTITY, quantityClothesString);
        values.put(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME, supplierNameString);
        values.put(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

        //Determine if this a new or an existing product of clothes by checking if mCurrentClothesUri is null or not
        if (mCurrentClothesUri == null) {
            // this is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new clothes product-
            Uri newUri = getContentResolver().insert(ClothesEntry.CONTENT_URI, values);

            if (newUri == null) {
                //If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_clothes_failed), Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise, the insertion was succesful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_clothes_successful), Toast.LENGTH_SHORT).show();
            }

            finish();

        } else {
            //Otherwise this is an existing Clothes product, so update the clothes with content URI: mCurrentClothesUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentClothesUri will already identify the corret row in the database that
            // we want to modify

            int rowsAffected = getContentResolver().update(mCurrentClothesUri, values, null, null);

            //Show a toast message depending on whether or not the update was succesful.
            if (rowsAffected == 0) {
                //If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_insert_clothes_failed), Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_clothes_successful), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options from the res/menu/menu_editor.xml file.
        // this add menu items to the app bar
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentClothesUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProductClothes();

                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            //Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //If the product clothes hasnt changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mClothesHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                //Otherwise if there are unsaved changes, setuo a dialog to warn the user.
                //Create a clikc listener to handle the user confirming that
                // chnages should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //User clicked "Disacard" button, navigate to parent activity-
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }

                        };
                //Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangedDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //since the ditor show all the clothes attributes, define a projection that contains
        // all columns from the clothes table
        String[] projection = {
                ClothesEntry._ID,
                ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME,
                ClothesEntry.COLUMN_CLOTHES_PRICE,
                ClothesEntry.COLUMN_CLOTHES_QUANTITY,
                ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME,
                ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(this,
                mCurrentClothesUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Bail early if the cursor is null or there is less that 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //Proceed with moving to the first row of the cursor and reading data from it
        // This hsould be the only row in the cursor)
        if (cursor.moveToFirst()) {
            //Find the columns of clothes attributes that we ware interested in
            int clothesNameColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_CLOTHES_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_CLOTHES_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_CLOTHES_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(ClothesEntry.COLUMN_CLOTHES_SUPPLIER_PHONE_NUMBER);

            //Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(clothesNameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            long supplierPhoneNumber = cursor.getLong(supplierPhoneNumberColumnIndex);

            //Update the views on the screen with the values from the database
            mClothesNameEditText.setText(productName);
            mClothesPriceEditText.setText(Integer.toString(price));
            mClothesQuantityEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(Long.toString(supplierPhoneNumber));

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields.
        mClothesNameEditText.setText("");
        mClothesPriceEditText.setText("");
        mClothesQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");

    }

    /**
     * Perform the deletion of the clothes product in the database
     */

    private void deleteClothes() {

        //Only perform the delte if this is an existing product.
        if (mCurrentClothesUri != null) {
            //Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentClothesUri
            // content Uri already identifies the product that we want.

            int rowsDeleted = getContentResolver().delete(mCurrentClothesUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                //If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_clothes_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                //Otherwis, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_clothes_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    private void showDeleteConfirmationDialog() {
        //Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //user clicked the "Delete Button, so delete the clothes product.
                deleteClothes();

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangedDialog(
            DialogInterface.OnClickListener discardButtonClickListener
    ) {
        //Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative button on the dialog.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //User clicked the "Keep editing" button, so dismiss the dialog
                //and continue editing the clothes.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
