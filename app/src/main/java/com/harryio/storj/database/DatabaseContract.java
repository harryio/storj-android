package com.harryio.storj.database;

import android.provider.BaseColumns;

public class DatabaseContract {
    public static class KeyPairEntry implements BaseColumns {
        public static final String TABLE_NAME = "KeyPair";

        public static final String COLUMN_PUB_KEY = "public_key";
        public static final String COLUMN_PRI_KEY = "private_key";
    }
}
