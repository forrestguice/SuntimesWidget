/**
    Copyright (C) 2017 Forrest Guice
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

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.forrestguice.colors.Color;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesCalculator;
import com.forrestguice.colors.ResourceColorValues;
import com.forrestguice.suntimeswidget.colors.android.AndroidColor;
import com.forrestguice.util.Log;
import com.forrestguice.util.android.AndroidLog;

import net.time4j.android.ApplicationStarter;

public class SuntimesApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        //init(this);    // initialization has been moved to CalculatorProvider.onCreate
    }

    private static boolean initialized = false;
    public static boolean isInitialized() {
        return initialized;
    }

    public static void init(Context context)
    {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(context, Thread.getDefaultUncaughtExceptionHandler()));
        Log.init(new AndroidLog());
        Log.setShowDebug(BuildConfig.DEBUG);
        Log.d("DEBUG", "SuntimesApplication.init:");

        ApplicationStarter.initialize(context, false);
        AndroidSuntimesCalculator.init(context);

        Color.init(new AndroidColor());
        ResourceColorValues.DEBUG = BuildConfig.DEBUG;

        if (BuildConfig.DEBUG)
        {
            StrictMode.enableDefaults();
            StrictMode.allowThreadDiskWrites();
        }
        initialized = true;
    }
}
