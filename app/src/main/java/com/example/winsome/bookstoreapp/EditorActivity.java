package com.example.winsome.bookstoreapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.winsome.bookstoreapp.data.BookStoreContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;

    private EditText mPriceEditText;

    private EditText mQuantityEditText;

    private int mQuantity;

    private ImageView mPhoto;

    private TextView mPhotoHintText;

    private EditText mSupplierNameEditText;

    private EditText mSupplierNumberEditText;

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;

    private Uri mImageUri;

    private Button mAddBookButton;

    private Button mMinusBookButton;

    private boolean mBookHasChanged = false;

    // BOOLEAN status for required fields,TRUE if these fields have been populated
    boolean hasAllRequiredValues = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mBookHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_book_quantity);
        mPhoto = (ImageView) findViewById(R.id.edit_product_photo);
        mPhotoHintText = (TextView) findViewById(R.id.add_or_edit_photo_hint);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierNumberEditText = (EditText) findViewById(R.id.edit_supplier_number);
        mAddBookButton = (Button) findViewById(R.id.addBookButton);
        mMinusBookButton = (Button) findViewById(R.id.minusBookButton);

        if(mCurrentBookUri == null){
            setTitle(getString(R.string.editor_activity_title_new_book));
            mPhoto.setImageResource(R.mipmap.ic_camera);
            mPhotoHintText.setText("Tap to change picture");
            mAddBookButton.setVisibility(View.GONE);
            mMinusBookButton.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }else{
            setTitle(getString(R.string.editor_activity_title_edit_book));
            mAddBookButton.setVisibility(View.VISIBLE);
            mMinusBookButton.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierNumberEditText.setOnTouchListener(mTouchListener);
        mAddBookButton.setOnTouchListener(mTouchListener);
        mMinusBookButton.setOnTouchListener(mTouchListener);

        mAddBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookButton(v);
            }
        });

        mMinusBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minusBookButton(v);
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mBookHasChanged = true;
            }
        });
    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mPhoto.setImageURI(mImageUri);
                mPhoto.invalidate();
            }
        }
    }

    public void orderMore() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+mSupplierNumberEditText.getText().toString().trim()));
        startActivity(intent);
    }

    private boolean saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierNumberString = mSupplierNumberEditText.getText().toString().trim();

        // Check if this is supposed to be a new book
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierNameString)
                && TextUtils.isEmpty(supplierNumberString) && mImageUri == null) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            hasAllRequiredValues = true;
            return hasAllRequiredValues;
        }



        // Create a ContentValues object where column names are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();

        // REQUIRED VALUES
        // Validation section
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_book_name), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(BookEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.validation_msg_book_quantity), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            int quantity = Integer.parseInt(quantityString);
            values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.validation_msg_book_price), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(BookEntry.COLUMN_PRODUCT_PRICE, priceString);
        }

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.validation_msg_book_image), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(BookEntry.COLUMN_PRODUCT_PICTURE, mImageUri.toString());
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_supplier_name), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        }

        if (TextUtils.isEmpty(supplierNumberString)) {
            Toast.makeText(this, getString(R.string.validation_msg_supplier_number), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, supplierNumberString);
        }


        // Show a toast message depending on whether or not the insertion was successful
        if (mCurrentBookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        hasAllRequiredValues = true;
        return hasAllRequiredValues;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveBook();
                if (hasAllRequiredValues == true) {
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            // Respond to a click on the "Order more" menu option
            case R.id.action_order:
                orderMore();
                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRODUCT_PRICE,
                BookEntry.COLUMN_PRODUCT_QUANTITY,
                BookEntry.COLUMN_PRODUCT_PICTURE,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY);
            int pictureColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PICTURE);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String imageUriString = cursor.getString(pictureColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierNumber = cursor.getString(supplierNumberColumnIndex);
            mImageUri = Uri.parse(imageUriString);
            mQuantity = quantity;

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPhoto.setImageURI(mImageUri);
            mSupplierNameEditText.setText(supplierName);
            mSupplierNumberEditText.setText(supplierNumber);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mPhoto.setImageResource(R.mipmap.ic_camera);
        mSupplierNameEditText.setText("");
        mSupplierNumberEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            getContentResolver().notifyChange(mCurrentBookUri, null);
            finish();
        }
    }

    public void addBookButton(View view) {
        mQuantity++;
        displayQuantity();
    }

    public void minusBookButton(View view) {
        if (mQuantity == 0) {
            Toast.makeText(this, "Can't decrease quantity", Toast.LENGTH_SHORT).show();
        } else {
            mQuantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        mQuantityEditText.setText(String.valueOf(mQuantity));
    }
}