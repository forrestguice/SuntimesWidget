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
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.AppSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

public class SuntimesCalendarTask extends AsyncTask<Void, String, Boolean>
{
    private SuntimesCalendarAdapter adapter;
    private WeakReference<Context> contextRef;

    private HashMap<String, String> calendarDisplay = new HashMap<>();
    private HashMap<String, Integer> calendarColors = new HashMap<>();

    private String[] solsticeStrings = new String[4];
    private int[] solsticeColors = new int[4];
    private SuntimesEquinoxSolsticeData solsticeData;
    private SuntimesMoonData moonData;

    private long lastSync = -1;
    private long calendarWindow0 = -1, calendarWindow1 = -1;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    public static final int NOTIFICATION_ID = 1000;
    private String notificationTitle;
    private String notificationMsgAdding, notificationMsgAdded;
    private String notificationMsgClearing, notificationMsgCleared;
    private int notificationIcon = R.drawable.ic_action_time;

    public SuntimesCalendarTask(Activity context)
    {
        contextRef = new WeakReference<Context>(context);
        adapter = new SuntimesCalendarAdapter(context.getContentResolver());
        calendarWindow0 = AppSettings.loadPrefCalendarWindow0(context);
        calendarWindow1 = AppSettings.loadPrefCalendarWindow1(context);

        // solstice calendar resources
        solsticeData = new SuntimesEquinoxSolsticeData(context, 0);
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_SOLSTICE, context.getString(R.string.calendar_solstice_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_SOLSTICE, ContextCompat.getColor(context, R.color.winterColor_light));

        solsticeStrings[0] = context.getString(R.string.timeMode_equinox_vernal);
        solsticeStrings[1] = context.getString(R.string.timeMode_solstice_summer);
        solsticeStrings[2] = context.getString(R.string.timeMode_equinox_autumnal);
        solsticeStrings[3] = context.getString(R.string.timeMode_solstice_winter);

        solsticeColors[0] = ContextCompat.getColor(context, R.color.springColor_light);
        solsticeColors[1] = ContextCompat.getColor(context, R.color.summerColor_light);
        solsticeColors[2] = ContextCompat.getColor(context, R.color.fallColor_light);
        solsticeColors[3] = ContextCompat.getColor(context, R.color.winterColor_light);

        // moon phase calendar resources
        moonData = new SuntimesMoonData(context, 0, "moon");
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, context.getString(R.string.calendar_moonPhase_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, ContextCompat.getColor(context, R.color.moonIcon_color_rising_light));
        MoonPhaseDisplay.initDisplayStrings(context);

        notificationManager = NotificationManagerCompat.from(context);
        notificationBuilder = new NotificationCompat.Builder(context);
        notificationTitle = context.getString(R.string.app_name);
        notificationMsgAdding = "Adding calendars"; // TODO
        notificationMsgAdded = "Added calendars"; // TODO
        notificationMsgClearing = "Clearing calendars"; // TODO
        notificationMsgCleared = "Cleared calendars"; // TODO
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
                    .setPriority(NotificationCompat.PRIORITY_LOW)
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
                    .setPriority(NotificationCompat.PRIORITY_LOW)
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
            solsticeData.initCalculator();
            SuntimesCalculator calculator = solsticeData.calculator();

            Calendar d = (Calendar)startDate.clone();
            while (d.before(endDate))
            {
                adapter.createCalendarEvent(calendarID, solsticeStrings[0], solsticeStrings[0], calculator.getVernalEquinoxForYear(d));
                adapter.createCalendarEvent(calendarID, solsticeStrings[1], solsticeStrings[1], calculator.getSummerSolsticeForYear(d));
                adapter.createCalendarEvent(calendarID, solsticeStrings[2], solsticeStrings[2], calculator.getAutumnalEquinoxForYear(d));
                adapter.createCalendarEvent(calendarID, solsticeStrings[3], solsticeStrings[3], calculator.getWinterSolsticeForYear(d));
                d.add(Calendar.YEAR, 1);
            }
            return true;
        } else return false;
    }

    private boolean initMoonPhaseCalendar( Calendar startDate, Calendar endDate )
    {
        String calendarName = SuntimesCalendarAdapter.CALENDAR_MOONPHASE;
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarDisplay.get(calendarName), calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            String fullMoonString = MoonPhaseDisplay.FULL.getLongDisplayString();
            String newMoonString = MoonPhaseDisplay.NEW.getLongDisplayString();
            String firstQuarterString = MoonPhaseDisplay.FIRST_QUARTER.getLongDisplayString();
            String thirdQuarterString = MoonPhaseDisplay.THIRD_QUARTER.getLongDisplayString();

            moonData.initCalculator();
            SuntimesCalculator calculator = moonData.calculator();

            ArrayList<Calendar> events = new ArrayList<>();
            Calendar d = (Calendar)startDate.clone();
            while (d.before(endDate))
            {
                Calendar fullMoon, newMoon, firstQuarter, thirdQuarter;

                events.clear();
                events.add(fullMoon = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FULL, d));
                events.add(newMoon = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.NEW, d));
                events.add(firstQuarter = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FIRST_QUARTER, d));
                events.add(thirdQuarter = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.THIRD_QUARTER, d));
                Collections.sort(events);
                d.setTimeInMillis( events.get(events.size()-1).getTimeInMillis() + 1000 );

                adapter.createCalendarEvent(calendarID, fullMoonString, fullMoonString, fullMoon);
                adapter.createCalendarEvent(calendarID, newMoonString, newMoonString, newMoon);
                adapter.createCalendarEvent(calendarID, firstQuarterString, firstQuarterString, firstQuarter);
                adapter.createCalendarEvent(calendarID, thirdQuarterString, thirdQuarterString, thirdQuarter);
            }
            return true;
        } else return false;
    }
}