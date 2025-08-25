/**
    Copyright (C) 2025 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final WeakReference<Context> contextRef;

    public ExceptionHandler(Context context, Thread.UncaughtExceptionHandler defaultHandler)
    {
        this.contextRef = new WeakReference<>(context);
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        try {
            Log.e("CRASH", e.getClass().getSimpleName(), e);
            launchCrashReportActivity(contextRef.get(), e);

        } finally {
            defaultHandler.uncaughtException(t, e);
        }
    }

    private static String createCrashReport(Throwable e)
    {
        return "Suntimes " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")" + (BuildConfig.DEBUG ? " [debug] " : " ") + "[" + BuildConfig.GIT_HASH + "]" +  "\n"
                + "on Android " + Build.VERSION.RELEASE +  " (" + Build.VERSION.SDK_INT + ")" + " [" + Build.MANUFACTURER + "]"
                + "\n\n" + Log.getStackTraceString(e);
    }

    private void launchCrashReportActivity(Context context, Throwable e)
    {
        if (context != null)
        {
            Intent intent = new Intent(context, ExceptionActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ExceptionActivity.EXTRA_REPORT, createCrashReport(e));
            context.startActivity(intent);

        } else {
            Log.e("CRASH", "launchCrashReportActivity: null context!");
        }
    }
}
