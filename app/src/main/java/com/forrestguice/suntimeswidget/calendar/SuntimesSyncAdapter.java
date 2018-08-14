/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;

import java.util.Calendar;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SuntimesSyncAdapter extends AbstractThreadedSyncAdapter
{
    public static final String ACCOUNT_NAME = "Suntimes";

    public static final String PREF_KEY_CALENDAR_LASTSYNC = "lastCalendarSync";

    public SuntimesSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
    }

    public SuntimesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
    }

    public static long readLastSyncTime(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(PREF_KEY_CALENDAR_LASTSYNC, -1L);
    }

    public static void writeLastSyncTime(Context context, Calendar calendar)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_CALENDAR_LASTSYNC, calendar.getTimeInMillis());
        prefs.apply();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {
        // TODO: add/remove calendar events
    }

    @TargetApi(15)
    public static Uri asSyncAdapter(Uri uri)
    {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();
    }
}
