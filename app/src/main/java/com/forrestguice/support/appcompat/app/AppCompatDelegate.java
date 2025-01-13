package com.forrestguice.support.appcompat.app;

public abstract class AppCompatDelegate
{
    public static final int MODE_NIGHT_NO = android.support.v7.app.AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_NIGHT_YES = android.support.v7.app.AppCompatDelegate.MODE_NIGHT_YES;
    public static final int MODE_NIGHT_AUTO = android.support.v7.app.AppCompatDelegate.MODE_NIGHT_AUTO;
    public static final int MODE_NIGHT_FOLLOW_SYSTEM = android.support.v7.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    public static void setDefaultNightMode(int mode) {
        android.support.v7.app.AppCompatDelegate.setDefaultNightMode(mode);
    }
}