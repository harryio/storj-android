package com.harryio.storj.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {
    public static final String KEY_IS_USER_LOGGED_IN = "com.harryio.IS_USER_LOGGED_IN";
    private static final String PREF_NAME = "StorjPrefs";
    private static SharedPrefUtils sharedPrefUtils;
    private SharedPreferences sharedPreferences;

    private SharedPrefUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPrefUtils instance(Context context) {
        if (sharedPrefUtils == null) {
            sharedPrefUtils = new SharedPrefUtils(context.getApplicationContext());
        }

        return sharedPrefUtils;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void storeBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
