package com.example.winsome.bookstoreapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.winsome.bookstoreapp.data.BookStoreContract.BookEntry;
import static android.content.ContentValues.TAG;


public class DetailCursorAdapter extends CursorAdapter {

    public DetailCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.book_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.book_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.book_current_quantity);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.book_image);
        ImageButton buyImageButton = (ImageButton) view.findViewById(R.id.book_buy_button);

        // Find the columns of product attributes that we're interested in
        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY);
        int photoColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PICTURE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        double productPrice = cursor.getDouble(priceColumnIndex);
        final int quantityProduct = cursor.getInt(quantityColumnIndex);
        String imageUriString = cursor.getString(photoColumnIndex);
        Uri productImageUri = Uri.parse(imageUriString);


        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));
        quantityTextView.setText(String.valueOf(quantityProduct));
        photoImageView.setImageURI(productImageUri);

        buyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, productIdColumnIndex);
                adjustProductQuantity(context, productUri, quantityProduct);
            }
        });
    }


    private void adjustProductQuantity(Context context, Uri productUri, int currentQuantityInStock) {

        // Subtract 1 from current value if quantity of product >= 1
        int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;

        if (currentQuantityInStock == 0) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_out_of_stock_msg, Toast.LENGTH_SHORT).show();
        }

        // Update table by using new value of quantity
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookEntry.COLUMN_PRODUCT_QUANTITY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (numRowsUpdated > 0) {
            // Show error message in Logs with info about pass update.
            Log.i(TAG, context.getString(R.string.buy_msg_confirm));
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_SHORT).show();
            // Show error message in Logs with info about fail update.
            Log.e(TAG, context.getString(R.string.error_msg_stock_update));
        }


    }
}
