package com.forrestguice.support.appwidget;

import android.appwidget.AppWidgetManager;
import android.os.Build;

public class AppWidgetManagerCompat
{
    public static final String OPTION_APPWIDGET_RESTORE_COMPLETED;
    static {
        if (Build.VERSION.SDK_INT >= 30) {
            OPTION_APPWIDGET_RESTORE_COMPLETED = AppWidgetManager.OPTION_APPWIDGET_RESTORE_COMPLETED;
        } else OPTION_APPWIDGET_RESTORE_COMPLETED = "appWidgetRestoreCompleted";
    }


}
