package com.harryio.storj.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StorjDb";
    private static final int DATABASE_VERSION = 1;

    private final String SQL_CREATE_KEYPAIR_TABLE = "CREATE TABLE " +
            DatabaseContract.KeyPairEntry.TABLE_NAME + "(" +
            DatabaseContract.KeyPairEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DatabaseContract.KeyPairEntry.COLUMN_PUB_KEY + " BLOB," +
            DatabaseContract.KeyPairEntry.COLUMN_PRI_KEY + " BLOB" + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_KEYPAIR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP_TABLE = "DROP TABLE IF EXISTS ";
        db.execSQL(DROP_TABLE + DatabaseContract.KeyPairEntry.TABLE_NAME);
        onCreate(db);
    }
}
