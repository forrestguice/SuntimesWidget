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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

public class SuntimesCalendarTask extends AsyncTask<Void, String, Boolean>
{
    private SuntimesCalendarAdapter adapter;
    private WeakReference<Context> contextRef;

    private HashMap<String, String> calendarDisplay = new HashMap<>();
    private HashMap<String, Integer> calendarColors = new HashMap<>();

    private String[] phaseStrings = new String[4];
    private String[] solsticeStrings = new String[4];
    //private int[] solsticeColors = new int[4];

    private long lastSync = -1;
    private long calendarWindow0 = -1, calendarWindow1 = -1;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    public static final int NOTIFICATION_ID = 1000;
    private String notificationTitle;
    private String notificationMsgAdding, notificationMsgAdded;
    private String notificationMsgClearing, notificationMsgCleared;
    private int notificationIcon = R.drawable.ic_action_time;
    private int notificationPriority = NotificationCompat.PRIORITY_LOW;
    private PendingIntent notificationIntent;

    public SuntimesCalendarTask(Activity context)
    {
        contextRef = new WeakReference<Context>(context);
        adapter = new SuntimesCalendarAdapter(context.getContentResolver());
        calendarWindow0 = SuntimesCalendarSettings.loadPrefCalendarWindow0(context);
        calendarWindow1 = SuntimesCalendarSettings.loadPrefCalendarWindow1(context);

        // solstice calendar resources
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_SOLSTICE, context.getString(R.string.calendar_solstice_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_SOLSTICE, ContextCompat.getColor(context, R.color.winterColor_light));

        solsticeStrings[0] = context.getString(R.string.timeMode_equinox_vernal);
        solsticeStrings[1] = context.getString(R.string.timeMode_solstice_summer);
        solsticeStrings[2] = context.getString(R.string.timeMode_equinox_autumnal);
        solsticeStrings[3] = context.getString(R.string.timeMode_solstice_winter);

        phaseStrings[0] = context.getString(R.string.timeMode_moon_new);
        phaseStrings[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings[2] = context.getString(R.string.timeMode_moon_full);
        phaseStrings[3] = context.getString(R.string.timeMode_moon_thirdquarter);

        //solsticeColors[0] = ContextCompat.getColor(context, R.color.springColor_light);
        //solsticeColors[1] = ContextCompat.getColor(context, R.color.summerColor_light);
        //solsticeColors[2] = ContextCompat.getColor(context, R.color.fallColor_light);
        //solsticeColors[3] = ContextCompat.getColor(context, R.color.winterColor_light);

        // moon phase calendar resources
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, context.getString(R.string.calendar_moonPhase_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, ContextCompat.getColor(context, R.color.moonIcon_color_rising_light));

        notificationManager = NotificationManagerCompat.from(context);
        notificationBuilder = new NotificationCompat.Builder(context);
        notificationTitle = context.getString(R.string.app_name);
        notificationMsgAdding = context.getString(R.string.calendars_notification_adding);
        notificationMsgAdded = context.getString(R.string.calendars_notification_added);
        notificationMsgClearing = context.getString(R.string.calendars_notification_clearing);
        notificationMsgCleared = context.getString(R.string.calendars_notification_cleared);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            Uri.Builder uriBuilder = CalendarContract.CONTENT_URI.buildUpon();
            uriBuilder.appendPath("time");
            ContentUris.appendId(uriBuilder, System.currentTimeMillis());
            intent = intent.setData(uriBuilder.build());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        notificationIntent = PendingIntent.getActivity(context, 0, intent, 0);
    }

    private boolean flag_clear = false;
    public void setFlagClearCalendars( boolean flag )
    {
        flag_clear = flag;
    }

    @Override
    protected void onPreExecute()
    {
        Context context = contextRef.get();
        if (context != null)
        {
            lastSync = SuntimesSyncAdapter.readLastSyncTime(context);

            notificationBuilder.setContentTitle(notificationTitle)
                    .setContentText((flag_clear ? notificationMsgClearing : notificationMsgAdding))
                    .setSmallIcon(notificationIcon)
                    .setPriority(notificationPriority)
                    .setContentIntent(null).setAutoCancel(false)
                    .setProgress(0, 0, true);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        if (Build.VERSION.SDK_INT < 14)
            return false;

        boolean retValue = adapter.removeCalendars();
        if (!flag_clear)
        {
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            Calendar now = Calendar.getInstance();

            startDate.setTimeInMillis(now.getTimeInMillis() - calendarWindow0);
            startDate.set(Calendar.MONTH, 0);            // round down to start of year
            startDate.set(Calendar.DAY_OF_MONTH, 0);
            startDate.set(Calendar.HOUR_OF_DAY, 0);
            startDate.set(Calendar.MINUTE, 0);
            startDate.set(Calendar.SECOND, 0);

            endDate.setTimeInMillis(now.getTimeInMillis() + calendarWindow1);
            endDate.add(Calendar.YEAR, 1);       // round up to end of year
            endDate.set(Calendar.MONTH, 0);
            endDate.set(Calendar.DAY_OF_MONTH, 0);
            endDate.set(Calendar.HOUR_OF_DAY, 0);
            endDate.set(Calendar.MINUTE, 0);
            endDate.set(Calendar.SECOND, 0);

            Log.d("DEBUG", "startWindow: " + calendarWindow0 + ", endWindow: " + calendarWindow1);
            Log.d("DEBUG", "startDate: " + startDate.get(Calendar.YEAR) + ", endDate: " + endDate.get(Calendar.YEAR));

            retValue = retValue && initSolsticeCalendar(startDate, endDate);
            retValue = retValue && initMoonPhaseCalendar(startDate, endDate);
        }
        return retValue;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        if (result)
        {
            Context context = contextRef.get();
            if (context != null)
            {
                SuntimesSyncAdapter.writeLastSyncTime(context, Calendar.getInstance());
            }

            notificationBuilder.setContentTitle(notificationTitle)
                    .setContentText((flag_clear ? notificationMsgCleared : notificationMsgAdded))
                    .setSmallIcon(notificationIcon)
                    .setPriority(notificationPriority)
                    .setContentIntent(notificationIntent).setAutoCancel(true)
                    .setProgress(0, 0, false);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            if (flag_clear)
                Log.i("SuntimesCalendarTask", "Cleared Suntimes Calendars...");
            else Log.i("SuntimesCalendarTask", "Added / updated Suntimes Calendars...");

        } else {
            Log.w("SuntimesCalendarTask", "Failed to complete task!");
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private boolean initSolsticeCalendar( Calendar startDate, Calendar endDate )
    {
        String calendarName = SuntimesCalendarAdapter.CALENDAR_SOLSTICE;
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarDisplay.get(calendarName), calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SEASONS + "/" + startDate.get(Calendar.YEAR) + "-" + endDate.get(Calendar.YEAR));
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SEASON_VERNAL, CalculatorProviderContract.COLUMN_SEASON_SUMMER, CalculatorProviderContract.COLUMN_SEASON_AUTUMN, CalculatorProviderContract.COLUMN_SEASON_WINTER };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast())
                    {
                        for (int i=0; i<projection.length; i++)
                        {
                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(cursor.getLong(i));
                            adapter.createCalendarEvent(calendarID, solsticeStrings[i], solsticeStrings[i], eventTime);
                        }
                        cursor.moveToNext();
                    }
                    cursor.close();
                } else Log.w("initSolsticeCalendar", "Failed to resolve URI! " + uri);

                return true;

            } else {
                Log.e("initSolsticeCalendar", "unable to getContentResolver!");
                return false;
            }
        } else return false;
    }

    private boolean initMoonPhaseCalendar( Calendar startDate, Calendar endDate )
    {
        String calendarName = SuntimesCalendarAdapter.CALENDAR_MOONPHASE;
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarDisplay.get(calendarName), calendarColors.get(calendarName));
        } else return false;

        String[] projection = new String[] {
                CalculatorProviderContract.COLUMN_MOON_NEW,
                CalculatorProviderContract.COLUMN_MOON_FIRST,
                CalculatorProviderContract.COLUMN_MOON_FULL,
                CalculatorProviderContract.COLUMN_MOON_THIRD };

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPHASE + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast())
                    {
                        for (int i=0; i<projection.length; i++)
                        {
                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(cursor.getLong(i));
                            adapter.createCalendarEvent(calendarID, phaseStrings[i], phaseStrings[i], eventTime);
                        }
                        cursor.moveToNext();
                    }
                    cursor.close();
                } else Log.w("initMoonPhaseCalendar", "Failed to resolve URI! " + uri);

                return true;

            } else {
                Log.e("initMoonPhaseCalendar", "unable to getContentResolver!");
                return false;
            }
        } else return false;
    }
}