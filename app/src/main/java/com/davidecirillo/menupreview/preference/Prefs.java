package com.davidecirillo.menupreview.preference;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Map;

/**
 * Handles SharedPreferences at the application level.
 */
public class Prefs {

    private static int DEFAULT_INT = 0;
    private static long DEFAULT_LONG = 0L;
    private static String DEFAULT_STRING = null;
    private static boolean DEFAULT_BOOLEAN = false;

    /*
    *
    * Preference getters
    *
    */
    public static boolean getBooleanPreference(Context context, int resId, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(resId), defaultValue);
    }

    public static boolean getBooleanPreference(Context context, int resId) {
        return getBooleanPreference(context, resId, DEFAULT_BOOLEAN);
    }

    public static int getIntPreference(Context context, int resId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(resId), DEFAULT_INT);
    }

    public static int getIntPreference(Context context, int resId, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(resId), defaultValue);
    }

    public static int getIntPreference(Context context, String prefKey, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(prefKey, defaultValue);
    }


    public static String getStringPreference(Context context, int resId, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(resId), defaultValue);
    }

    public static String getStringPreference(Context context, String resId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(resId, DEFAULT_STRING);
    }

    public static String getStringPreference(Context context, int resId) {
        return getStringPreference(context, resId, DEFAULT_STRING);
    }

    public static Long getLongPreference(Context context, int resId, Long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(context.getString(resId), defaultValue);
    }

    public static Long getLongPreference(Context context, int resId) {
        return getLongPreference(context, resId, DEFAULT_LONG);
    }

    /*
    *
    * Preference savers
    *
     */
    public static void savePreference(Context context, int resId, int newValue) {
        savePreference(context, context.getString(resId), newValue);
    }

    public static void savePreference(Context context, String prefKey, int newValue) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(prefKey, newValue);
        editor.apply();
    }

    public static void savePreference(Context context, int resId, String newValue) {
        savePreference(context, context.getString(resId), newValue);
    }

    public static void savePreference(Context context, String prefKey, String newValue) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(prefKey, newValue);
        editor.apply();
    }

    public static void savePreference(Context context, int resId, Boolean newValue) {
        savePreference(context, context.getString(resId), newValue);
    }

    private static void savePreference(Context context, String prefKey, Boolean newValue) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(prefKey, newValue);
        editor.apply();
    }

    public static void savePreference(Context context, int resId, long newValue) {
        savePreference(context, context.getString(resId), newValue);
    }

    private static void savePreference(Context context, String prefKey, long newValue) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(prefKey, newValue);
        editor.apply();
    }


    public static void removePreference(Context context, int resId) {
        removePreference(context, context.getString(resId));
    }

    private static void removePreference(Context context, String prefKey) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(prefKey);
        editor.apply();
    }

    public static void clearPreferences(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    public static Map<String, ?> getAll(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getAll();
    }
}
