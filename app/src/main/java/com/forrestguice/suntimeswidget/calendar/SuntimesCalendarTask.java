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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

public class SuntimesCalendarTask extends AsyncTask<Void, String, Boolean>
{
    public static final String PREF_KEY_CALENDAR_WINDOW = "calendarWindow";
    public static final long PREF_DEF_CALENDAR_WINDOW = 63072000000L;  // 2 years

    private SuntimesCalendarAdapter adapter;
    private WeakReference<Context> contextRef;

    private HashMap<String, String> calendarDisplay = new HashMap<>();
    private HashMap<String, Integer> calendarColors = new HashMap<>();

    private String[] solsticeStrings = new String[4];
    private int[] solsticeColors = new int[4];
    private SuntimesEquinoxSolsticeDataset solsticeData;
    private SuntimesMoonData moonData;

    private long lastSync = -1;
    private long calendarWindow = -1;

    public SuntimesCalendarTask(Activity context)
    {
        contextRef = new WeakReference<Context>(context);
        adapter = new SuntimesCalendarAdapter(context.getContentResolver());
        calendarWindow = readCalendarWindow(context);

        // solstice calendar resources
        solsticeData = new SuntimesEquinoxSolsticeDataset(context);
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_SOLSTICE, "Solstices and Equinoxes");  // TODO: i18n
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
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, "Moon Phases");  // TODO: i18n
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, ContextCompat.getColor(context, R.color.moonIcon_color_rising_light));
        MoonPhaseDisplay.initDisplayStrings(context);
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
        if (context != null) {
            lastSync = SuntimesSyncAdapter.readLastSyncTime(context);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        boolean retValue = adapter.removeCalendars();
        if (!flag_clear)
        {
            Calendar startDate, endDate;
            startDate = endDate = Calendar.getInstance();
            if (lastSync >= 0) {
                startDate.setTimeInMillis(lastSync);
            }
            endDate.setTimeInMillis(startDate.getTimeInMillis() + calendarWindow);

            solsticeData.calculateData();  // TODO: within window
            moonData.calculate();

            retValue = retValue && initSolsticeCalendar();
            retValue = retValue && initMoonPhaseCalendar();
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
        }
    }

    public static long readCalendarWindow(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(PREF_KEY_CALENDAR_WINDOW, PREF_DEF_CALENDAR_WINDOW);
    }

    private boolean initSolsticeCalendar()
    {
        String calendarName = SuntimesCalendarAdapter.CALENDAR_SOLSTICE;
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarDisplay.get(calendarName), calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            adapter.createCalendarEvent(calendarID, solsticeStrings[0], solsticeStrings[0], solsticeData.dataEquinoxVernal.eventCalendarThisYear());
            adapter.createCalendarEvent(calendarID, solsticeStrings[0], solsticeStrings[0], solsticeData.dataEquinoxVernal.eventCalendarOtherYear());

            adapter.createCalendarEvent(calendarID, solsticeStrings[1], solsticeStrings[1], solsticeData.dataSolsticeSummer.eventCalendarThisYear());
            adapter.createCalendarEvent(calendarID, solsticeStrings[1], solsticeStrings[1], solsticeData.dataSolsticeSummer.eventCalendarOtherYear());

            adapter.createCalendarEvent(calendarID, solsticeStrings[2], solsticeStrings[2], solsticeData.dataEquinoxAutumnal.eventCalendarThisYear());
            adapter.createCalendarEvent(calendarID, solsticeStrings[2], solsticeStrings[2], solsticeData.dataEquinoxAutumnal.eventCalendarOtherYear());

            adapter.createCalendarEvent(calendarID, solsticeStrings[3], solsticeStrings[3], solsticeData.dataSolsticeWinter.eventCalendarThisYear());
            adapter.createCalendarEvent(calendarID, solsticeStrings[3], solsticeStrings[3], solsticeData.dataSolsticeWinter.eventCalendarOtherYear());
            return true;
        } else return false;
    }

    private boolean initMoonPhaseCalendar()
    {
        String calendarName = SuntimesCalendarAdapter.CALENDAR_MOONPHASE;
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarDisplay.get(calendarName), calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            adapter.createCalendarEvent(calendarID, MoonPhaseDisplay.FULL.getLongDisplayString(), MoonPhaseDisplay.FULL.getLongDisplayString(), moonData.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FULL));
            adapter.createCalendarEvent(calendarID, MoonPhaseDisplay.NEW.getLongDisplayString(), MoonPhaseDisplay.NEW.getLongDisplayString(), moonData.moonPhaseCalendar(SuntimesCalculator.MoonPhase.NEW));
            adapter.createCalendarEvent(calendarID, MoonPhaseDisplay.FIRST_QUARTER.getLongDisplayString(), MoonPhaseDisplay.FIRST_QUARTER.getLongDisplayString(), moonData.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FIRST_QUARTER));
            adapter.createCalendarEvent(calendarID, MoonPhaseDisplay.THIRD_QUARTER.getLongDisplayString(), MoonPhaseDisplay.THIRD_QUARTER.getLongDisplayString(), moonData.moonPhaseCalendar(SuntimesCalculator.MoonPhase.THIRD_QUARTER));
            return true;
        } else return false;
    }
}