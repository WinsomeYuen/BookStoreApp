package com.example.winsome.bookstoreapp.data;

import android.provider.BaseColumns;

public final class BookStoreContract {

    private BookStoreContract(){}

    public static final class BookEntry implements BaseColumns{
        public final static String TABLE_NAME="bookstore";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME ="productname";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_SUPPLIER_NAME = "suppliername";
        public final static String COLUMN_SUPPLIER_NUMBER = "number";
    }
}
