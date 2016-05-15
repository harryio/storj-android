package com.harryio.storj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.harryio.storj.util.SecurityUtils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairDAO {
    private static KeyPairDAO keyPairDAO;
    private final SQLiteOpenHelper databaseHelper;

    private KeyPairDAO(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static KeyPairDAO getInstance(Context context) {
        if (keyPairDAO == null) {
            keyPairDAO = new KeyPairDAO(context.getApplicationContext());
        }
        return keyPairDAO;
    }

    public void insert(KeyPair keyPair) {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
            ContentValues contentValues = map(keyPair);
            database.insert(DatabaseContract.KeyPairEntry.TABLE_NAME, null, contentValues);
        } finally {
            database.close();
        }
    }

    public PublicKey getPublicKey() {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        byte[] data = null;
        Cursor cursor = null;
        try {
            cursor = database.query(DatabaseContract.KeyPairEntry.TABLE_NAME,
                    new String[]{DatabaseContract.KeyPairEntry.COLUMN_PUB_KEY}, null,
                    null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                data = cursor.getBlob(cursor.getColumnIndex(DatabaseContract.KeyPairEntry.COLUMN_PUB_KEY));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.close();
        }

        return data != null ? SecurityUtils.instance().getPublicKey(data) : null;
    }

    public PrivateKey getPrivateKey() {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        byte[] data = null;
        Cursor cursor = null;
        try {
            cursor = database.query(DatabaseContract.KeyPairEntry.TABLE_NAME,
                    new String[]{DatabaseContract.KeyPairEntry.COLUMN_PRI_KEY}, null,
                    null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                data = cursor.getBlob(cursor.getColumnIndex(DatabaseContract.KeyPairEntry.COLUMN_PRI_KEY));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.close();
        }

        return data != null ? SecurityUtils.instance().getPrivateKey(data) : null;
    }

    private ContentValues map(KeyPair keyPair) {
        ContentValues contentValues = new ContentValues(2);
        contentValues.put(DatabaseContract.KeyPairEntry.COLUMN_PRI_KEY, keyPair.getPrivate().getEncoded());
        contentValues.put(DatabaseContract.KeyPairEntry.COLUMN_PUB_KEY, keyPair.getPublic().getEncoded());
        return contentValues;
    }
}
