package com.forrestguice.support.app;

import androidx.appcompat.app.AppCompatDelegate;

public class AppCompatDelegateHelper
{
    public static final int MODE_NIGHT_NO = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_NIGHT_YES = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int MODE_NIGHT_AUTO = AppCompatDelegate.MODE_NIGHT_AUTO;
    public static final int MODE_NIGHT_FOLLOW_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    public static void setDefaultNightMode(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}