/**
    Copyright (C) 2018-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

public class AlarmScheduler
{
    public static final String TAG = "AlarmReceiver";

    /**
     * updateAlarmTime
     * @param item AlarmClockItem
     * @return true item was updated, false failed to update item
     */
    public static boolean updateAlarmTime(Context context, final AlarmClockItem item) {
        return updateAlarmTime(context, item, Calendar.getInstance(), true);
    }
    public static boolean updateAlarmTime(Context context, final AlarmClockItem item, Calendar now, boolean modifyItem)
    {
        Calendar eventTime = null;
        boolean modifyHourMinute = true;
        String eventID = item.getEvent();
        SolarEvents event = SolarEvents.valueOf(eventID, null);
        ArrayList<Integer> repeatingDays = (item.repeatingDays != null ? item.repeatingDays : AlarmClockItem.everyday());
        
        if (item.flagIsTrue(AlarmClockItem.FLAG_LOCATION_FROM_APP)) {
            item.location = WidgetSettings.loadLocationPref(context, 0);
        }

        if (item.location != null && event != null)
        {
            eventTime = updateAlarmTime_solarEvent(context, event, item.location, item.offset, item.repeating, repeatingDays, now);

        } else if (eventID != null) {
            eventTime = updateAlarmTime_addonEvent(context, context.getContentResolver(), eventID, item.location, item.offset, item.repeating, repeatingDays, now);

        } else {
            modifyHourMinute = false;    // "clock time" alarms should leave "hour" and "minute" values untouched
            eventTime = updateAlarmTime_clockTime(item.hour, item.minute, item.timezone, item.location, item.offset, item.repeating, repeatingDays, now);
        }

        if (eventTime == null) {
            Log.e(TAG, "updateAlarmTime: failed to update " + item + " :: " + item.getEvent() + "@" + item.location);
            return false;
        }

        if (modifyItem)
        {
            if (modifyHourMinute) {
                item.hour = eventTime.get(Calendar.HOUR_OF_DAY);
                item.minute = eventTime.get(Calendar.MINUTE);
            }
            item.timestamp = eventTime.getTimeInMillis();
            item.modified = true;
        }
        return true;
    }

    @Nullable
    protected static Calendar updateAlarmTime_solarEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        Calendar eventTime = null;
        switch (event.getType())
        {
            case SolarEvents.TYPE_MOON:
                eventTime = updateAlarmTime_moonEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;

            case SolarEvents.TYPE_MOONPHASE:
                eventTime = updateAlarmTime_moonPhaseEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;

            case SolarEvents.TYPE_SEASON:
                eventTime = updateAlarmTime_seasonEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;

            case SolarEvents.TYPE_SUN:
                eventTime = updateAlarmTime_sunEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;
        }
        return eventTime;
    }

    @Nullable
    protected static Calendar updateAlarmTime_sunEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_sunEvent: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        SuntimesRiseSetData sunData = getData_sunEvent(context, event, location);

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        sunData.setTodayIs(day);
        sunData.calculate(context);
        eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
        if (eventTime != null)
        {
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }

        int c = 0;
        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime)
                || eventTime == null
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            if (!timestamps.add(alarmTime.getTimeInMillis()) && c > 365) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: sunEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            sunData.setTodayIs(day);
            sunData.calculate(context);
            eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    @Nullable
    private static Calendar updateAlarmTime_moonEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_moonEvent: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        SuntimesMoonData moonData = getData_moonEvent(context, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        moonData.setTodayIs(day);
        moonData.calculate(context);
        Calendar eventTime = moonEventCalendar(event, moonData, true);
        if (eventTime != null)
        {
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        int c = 0;
        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime)
                || eventTime == null
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            if (!timestamps.add(alarmTime.getTimeInMillis()) && c > 365) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: moonEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            moonData.setTodayIs(day);
            moonData.calculate(context);
            eventTime = moonEventCalendar(event, moonData, true);
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    public static Calendar moonEventCalendar(SolarEvents event, SuntimesMoonData data, boolean today)
    {
        if (today)
        {
            switch (event) {
                case MOONNOON: return data.getLunarNoonToday();
                case MOONNIGHT: return data.getLunarMidnightToday();
                case MOONRISE: return data.moonriseCalendarToday();
                case MOONSET: default: return data.moonsetCalendarToday();
            }
        } else {
            switch (event) {
                case MOONNOON: return data.getLunarNoonTomorrow();
                case MOONNIGHT: return data.getLunarMidnightTomorrow();
                case MOONRISE: return data.moonriseCalendarTomorrow();
                case MOONSET: default: return data.moonsetCalendarTomorrow();
            }
        }
    }

    @Nullable
    protected static Calendar updateAlarmTime_moonPhaseEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        SuntimesCalculator.MoonPhase phase = event.toMoonPhase();
        SuntimesMoonData moonData = getData_moonEvent(context, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(now.getTimeInMillis());
        moonData.setTodayIs(day);
        moonData.calculate(context);

        int c = 0;
        Calendar eventTime = moonData.moonPhaseCalendar(phase);
        eventTime.set(Calendar.SECOND, 0);
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);

        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime))
                //|| (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))    // does it make sense to enforce repeatingDays for moon phases? probably not.
        {
            if (!timestamps.add(alarmTime.getTimeInMillis())) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            c++;
            Log.w("AlarmReceiverItem", "updateAlarmTime: moonPhaseEvent advancing to next cycle.. " + c);
            day.setTimeInMillis(eventTime.getTimeInMillis() + (24 * 60 * 60 * 1000));
            moonData.setTodayIs(day);
            moonData.calculate(context);
            eventTime = moonData.moonPhaseCalendar(phase);
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    @Nullable
    private static Calendar updateAlarmTime_seasonEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        SuntimesEquinoxSolsticeData data = getData_seasons(context, event, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate(context);

        Calendar eventTime = data.eventCalendarUpcoming(day);
        eventTime.set(Calendar.SECOND, 0);
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);

        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime))
                // || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))    // does it make sense to enforce repeatingDays for seasons? probably not.
        {
            if (!timestamps.add(alarmTime.getTimeInMillis())) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: seasonEvent advancing..");
            day.setTimeInMillis(eventTime.getTimeInMillis() + 1000 * 60 * 60 * 24);
            data.setTodayIs(day);
            data.calculate(context);
            eventTime = data.eventCalendarUpcoming(day);
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    public static Calendar updateAlarmTime_addonEvent(Context context, @Nullable ContentResolver resolver, @NonNull String eventID, @Nullable Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_addonEvent: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        Log.d(TAG, "updateAlarmTime_addonEvent: eventID: " + eventID + ", offset: " + offset + ", repeating: " + repeating + ", repeatingDays: " + repeatingDays);
        long nowMillis = now.getTimeInMillis();
        Uri uri_id = Uri.parse(eventID);
        Uri uri_calc = Uri.parse(EventUri.getEventCalcUri(uri_id.getAuthority(), uri_id.getLastPathSegment()));

        StringBuilder repeatingDaysString = new StringBuilder("[");
        if (repeating) {
            for (int i = 0; i < repeatingDays.size(); i++) {
                repeatingDaysString.append(repeatingDays.get(i));
                if (i != repeatingDays.size() - 1) {
                    repeatingDaysString.append(",");
                }
            }
        }
        repeatingDaysString.append("]");

        String[] selectionArgs = new String[] { Long.toString(nowMillis), Long.toString(offset), Boolean.toString(repeating), repeatingDaysString.toString() };
        String selection = AlarmEventContract.EXTRA_ALARM_NOW + "=? AND "
                + AlarmEventContract.EXTRA_ALARM_OFFSET + "=? AND "
                + AlarmEventContract.EXTRA_ALARM_REPEAT + "=? AND "
                + AlarmEventContract.EXTRA_ALARM_REPEAT_DAYS + "=?";

        if (location != null)
        {
            selectionArgs = new String[] { Long.toString(nowMillis), Long.toString(offset), Boolean.toString(repeating), repeatingDaysString.toString(),
                    location.getLatitude(), location.getLongitude(), location.getAltitude() };
            selection += " AND "
                    + CalculatorProviderContract.COLUMN_CONFIG_LATITUDE + "=? AND "
                    + CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE + "=? AND "
                    + CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE + "=?";
        }
        return queryAddonAlarmTimeWithTimeout(resolver, uri_calc, selection, selectionArgs, offset, now, MAX_WAIT_MS);
    }

    public static final long MAX_WAIT_MS = 990;
    protected static Calendar queryAddonAlarmTimeWithTimeout(@Nullable final ContentResolver resolver, final Uri uri_calc, final String selection, final String[] selectionArgs, final long offset, final Calendar now, long timeoutAfter)
    {
        return ExecutorUtils.getResult(TAG, new Callable<Calendar>() {
            public Calendar call() {
                return queryAddonAlarmTime(resolver, uri_calc, selection, selectionArgs, offset, now);
            }
        }, timeoutAfter);
    }

    protected static Calendar queryAddonAlarmTime(@Nullable ContentResolver resolver, Uri uri_calc, String selection, String[] selectionArgs, long offset, Calendar now)
    {
        if (resolver != null)
        {
            long nowMillis = now.getTimeInMillis();
            Cursor cursor = resolver.query(uri_calc, AlarmEventContract.QUERY_EVENT_CALC_PROJECTION, selection, selectionArgs, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                if (cursor.isAfterLast()) {
                    Log.e(TAG, "updateAlarmTime: failed to query alarm time; result is missing (no rows) :: " + uri_calc);
                    return null;
                }

                int i_eventTime = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_TIMEMILLIS);
                Long eventTimeMillis = i_eventTime >= 0 ? cursor.getLong(i_eventTime) : null;
                cursor.close();

                if (eventTimeMillis != null)
                {
                    if (nowMillis > (eventTimeMillis + offset)) {
                        Log.e(TAG, "updateAlarmTime: failed to query alarm time; result is invalid (past) :: " + uri_calc);
                        return null;
                    }
                    Calendar eventTime = Calendar.getInstance();
                    eventTime.setTimeInMillis(eventTimeMillis);
                    return eventTime;

                } else {
                    Log.e(TAG, "updateAlarmTime: failed to query alarm time; result is missing " + AlarmEventContract.COLUMN_EVENT_TIMEMILLIS + " :: " + uri_calc);
                    return null;
                }
            } else {
                Log.e(TAG, "updateAlarmTime: failed to query alarm time; null cursor!" + uri_calc);
                return null;
            }
        } else {
            Log.e(TAG, "updateAlarmTime: failed to query alarm time; null ContentResolver! " + uri_calc);
            return null;
        }
    }

    @Nullable
    protected static Calendar updateAlarmTime_clockTime(int hour, int minute, String tzID, @Nullable Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_clockTime: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        TimeZone timezone = AlarmClockItem.AlarmTimeZone.getTimeZone(tzID, location);
        Log.d(TAG, "updateAlarmTime_clockTime: hour: " + hour + ", minute: " + minute + ", timezone: " + timezone.getID() + ", offset: " + offset + ", repeating: " + repeating + ", repeatingDays: " + repeatingDays);
        Calendar alarmTime = Calendar.getInstance(timezone);
        Calendar eventTime = Calendar.getInstance(timezone);
        eventTime.setTimeInMillis(now.getTimeInMillis());

        eventTime.set(Calendar.SECOND, 0);
        if (hour >= 0 && hour < 24) {
            eventTime.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0 && minute < 60) {
            eventTime.set(Calendar.MINUTE, minute);
        }

        Set<Long> timestamps = new HashSet<>();
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        while (now.after(alarmTime)
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            if (!timestamps.add(alarmTime.getTimeInMillis())) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w(TAG, "updateAlarmTime: clock time " + hour + ":" + minute + " (+" + offset + ") advancing by 1 day..");
            eventTime.add(Calendar.DAY_OF_YEAR, 1);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    public static SuntimesData getData(Context context, @NonNull AlarmClockItem alarm)
    {
        SolarEvents event = SolarEvents.valueOf(alarm.getEvent(), null);   // TODO: non SolarEventsEnum
        if (alarm.location != null && event != null)
        {
            switch (event.getType())
            {
                case SolarEvents.TYPE_MOON:
                case SolarEvents.TYPE_MOONPHASE:
                    return getData_moonEvent(context, alarm.location);
                case SolarEvents.TYPE_SEASON:
                    return getData_seasons(context, event, alarm.location);
                case SolarEvents.TYPE_SUN:
                    return getData_sunEvent(context, event, alarm.location);
                default:
                    return getData_clockEvent(context, alarm.location);
            }
        } else {
            return getData_clockEvent(context, WidgetSettings.loadLocationPref(context, 0));
        }
    }

    private static SuntimesRiseSetData getData_sunEvent(Context context, @NonNull SolarEvents event, @NonNull Location location)
    {
        TimeMode timeMode = event.toTimeMode();
        SuntimesRiseSetData sunData = new SuntimesRiseSetData(context, 0);
        sunData.setLocation(location);
        sunData.setTimeMode(timeMode != null ? timeMode : TimeMode.OFFICIAL);
        sunData.setTodayIs(Calendar.getInstance());
        return sunData;
    }
    protected static SuntimesMoonData getData_moonEvent(Context context, @NonNull Location location)
    {
        SuntimesMoonData moonData = new SuntimesMoonData(context, 0);
        moonData.setLocation(location);
        moonData.setTodayIs(Calendar.getInstance());
        return moonData;
    }
    private static SuntimesEquinoxSolsticeData getData_seasons(Context context, @NonNull SolarEvents event, @NonNull Location location)
    {
        SolsticeEquinoxMode season = event.toSolsticeEquinoxMode();
        SuntimesEquinoxSolsticeData data = new SuntimesEquinoxSolsticeData(context, 0);
        data.setTimeMode(season);
        data.setLocation(location);
        data.setTodayIs(Calendar.getInstance());
        return data;
    }
    private static SuntimesClockData getData_clockEvent(Context context, @NonNull Location location)
    {
        SuntimesClockData data = new SuntimesClockData(context, 0);
        data.setLocation(location);
        data.setTodayIs(Calendar.getInstance());
        return data;
    }

    protected static boolean t_updateAlarmTime_brokenLoop = false;   // for testing; set true by updateAlarmTime_ methods if the same timestamp is encountered twice (breaking the loop)
    protected static boolean t_updateAlarmTime_runningLoop = false;  // for testing; set true/false by updateAlarmTime_ methods
}
