package com.example.winsome.bookstoreapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.preference.Preference;
import android.provider.BaseColumns;

public final class BookStoreContract {

    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    private BookStoreContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.winsome.bookstoreapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOKS = "books";

    public static final class BookEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        public final static String TABLE_NAME="bookstore";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME ="productname";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_PRODUCT_PICTURE = "image";
        public final static String COLUMN_SUPPLIER_NAME = "suppliername";
        public final static String COLUMN_SUPPLIER_NUMBER = "number";

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
    }
}
