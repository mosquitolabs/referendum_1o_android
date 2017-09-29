/*
 * Copyright (c) 2017 Tadaima.cat. All rights reserved.
 */

package com.referendum.uoctubre.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.referendum.uoctubre.UOctubreApplication;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesUtils {
    public static final String USER_HASHTAGS = "user_hashtags";
    public static final String APP_LANGUAGE = "app_language";
    public static final String NUMBER_OF_EXECUTIONS = "number_of_executions";
    public static final String HAS_RATED = "has_rated";

    public static String getString(String key, String defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        return settings.getString(key, defaultValue);
    }

    public static void setString(String key, String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        settings.edit().putString(key, value).apply();
    }

    public static int getInt(String key, int defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        return settings.getInt(key, defaultValue);
    }

    public static void setInt(String key, int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        settings.edit().putInt(key, value).apply();
    }

    public static long getLong(String key, long defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        return settings.getLong(key, defaultValue);
    }

    public static void setLong(String key, long value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        settings.edit().putLong(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        return settings.getBoolean(key, defaultValue);
    }

    public static void setBoolean(String key, boolean value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        settings.edit().putBoolean(key, value).apply();
    }

    public static Set<String> getStringSet(String key) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        return settings.getStringSet(key, new HashSet<String>());
    }

    public static void setStringSet(String key, Set<String> value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UOctubreApplication.getInstance());
        settings.edit().putStringSet(key, value).apply();
    }
}
