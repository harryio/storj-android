package com.harryio.storj.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {
    public static final String KEY_IS_USER_LOGGED_IN = "com.harryio.IS_USER_LOGGED_IN";
    public static final String KEY_IS_TUTORIAL_SHOWN = "com.harryio.IS_TUTORIAL_SHOWN";
    public static final String KEY_DEFAULT_BUCKET_ID = "com.harryio.DEFAULT_BUCKET_ID";
    public static final String KEY_USERNAME = "com.harryio.KEY_USERNAME";
    public static final String KEY_PASSWORD = "com.harryio.PASSWORD";

    private static final String PREF_NAME = "StorjPrefs";
    private static PrefUtils prefUtils;
    private SharedPreferences sharedPreferences;

    private PrefUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PrefUtils instance(Context context) {
        if (prefUtils == null) {
            prefUtils = new PrefUtils(context.getApplicationContext());
        }

        return prefUtils;
    }

    public void storeBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void storeString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
}
