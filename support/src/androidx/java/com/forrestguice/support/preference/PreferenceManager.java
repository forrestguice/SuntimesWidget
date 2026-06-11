package com.forrestguice.support.preference;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class PreferenceManager
{
    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    /*public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
                getDefaultSharedPreferencesMode());
    }
    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }
    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }*/
}
