/**
    Copyright (C) 2021 Forrest Guice
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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_NAME;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_PHRASE;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_PHRASE_GENDER;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_PHRASE_QUANTITY;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_REQUIRES_LOCATION;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_SUMMARY;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_SUPPORTS_OFFSETDAYS;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_SUPPORTS_REPEATING;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_TIMEMILLIS;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_TITLE;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_NOW;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_OFFSET;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_REPEAT;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_REPEAT_DAYS;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_CALC;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_CALC_PROJECTION;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_INFO;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_INFO_PROJECTION;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.REPEAT_SUPPORT_BASIC;

/**
 * AlarmEventProvider
 * @see AlarmEventContract
 */
public class AlarmEventProvider extends ContentProvider
{
    private static final int URIMATCH_EVENTS = 0;
    private static final int URIMATCH_EVENT = 10;
    private static final int URIMATCH_EVENT_CALC = 20;

    private SuntimesUtils utils = null;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_INFO, URIMATCH_EVENTS);                            // content://AUTHORITY/eventInfo
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_INFO + "/*", URIMATCH_EVENT);                // content://AUTHORITY/eventInfo/[eventID]
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_CALC + "/*", URIMATCH_EVENT);                // content://AUTHORITY/eventCalc/[eventID]
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * query
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        if (utils == null) {
            utils = new SuntimesUtils();
            SuntimesUtils.initDisplayStrings(getContext());
        }

        HashMap<String, String> selectionMap = CalculatorProvider.processSelection(CalculatorProvider.processSelectionArgs(selection, selectionArgs));
        Cursor retValue = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_EVENTS:
                Log.d(getClass().getSimpleName(), "URIMATCH_EVENTS");
                retValue = queryEvents(null, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_EVENT:
                Log.d(getClass().getSimpleName(), "URIMATCH_EVENT");
                retValue = queryEvents(uri.getLastPathSegment(), uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_EVENT_CALC:
                Log.d(getClass().getSimpleName(), "URIMATCH_EVENT_CALC");
                retValue = calculateEvent(uri.getLastPathSegment(), uri, projection, selectionMap);
                break;

            default:
                Log.e(getClass().getSimpleName(), "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    /**
     * queryEvents
     */
    private Cursor queryEvents(@Nullable String eventID, @NonNull Uri uri, @Nullable String[] projection, @Nullable HashMap<String,String> selectionMap, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_EVENT_INFO_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        Context context = getContext();
        if (context != null)
        {
            SolarEvents event0 = (eventID != null) ? SolarEvents.valueOf(eventID, null) : null;
            if (eventID == null || event0 != null)
            {
                // eventID is null (list all SolarEvents), or eventID is a SolarEvents enum (list one)
                SolarEvents[] events = ((event0 != null) ? new SolarEvents[] { event0 } : SolarEvents.values());
                for (SolarEvents event : events) {
                    retValue.addRow(createRow(context, event, columns, selectionMap));
                }

            } else {   // eventID is not null, but also not a SolarEvents enum
                try {      // so assume its a timestamp; try parsing to Long
                    retValue.addRow(createRow(context, Long.parseLong(eventID), columns, selectionMap));

                } catch (NumberFormatException e) {
                    Log.w("AlarmEventsProvider", "queryEvents: unrecognized event: " + eventID + " .. " + e);
                }
            }
        }
        return retValue;
    }

    /**
     * calculateEvent
     */
    private Cursor calculateEvent(String eventID, @NonNull Uri uri, @Nullable String[] projection, @Nullable HashMap<String,String> selectionMap)
    {
        String[] columns = (projection != null ? projection : QUERY_EVENT_CALC_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        Context context = getContext();
        if (context != null)
        {
            SolarEvents event0 = (eventID != null) ? SolarEvents.valueOf(eventID, null) : null;
            if (event0 != null) {
                retValue.addRow(createRow(context, event0, columns, selectionMap));

            } else if (eventID != null) {
                try {
                    retValue.addRow(createRow(context, Long.parseLong(eventID), columns, selectionMap));
                } catch (NumberFormatException e) {
                    Log.w("AlarmEventsProvider", "calculateEvents: unrecognized event: " + eventID);
                }
            }
        }
        return retValue;
    }

    /**
     * createRow( SolarEvent )
     */
    private Object[] createRow(@NonNull Context context, @NonNull SolarEvents event, String[] columns, @Nullable HashMap<String,String> selectionMap)
    {
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case COLUMN_EVENT_TIMEMILLIS:
                    Location location = (selectionMap != null) ? CalculatorProvider.processSelection_location(selectionMap) : null;
                    if (location == null) {
                        location = WidgetSettings.loadLocationPref(context, 0);
                    }
                    String offsetString = selectionMap != null ? selectionMap.get(EXTRA_ALARM_OFFSET) : "0";
                    long offset = offsetString != null ? Long.parseLong(offsetString) : 0L;
                    boolean repeating = selectionMap != null && Boolean.parseBoolean(selectionMap.get(EXTRA_ALARM_REPEAT));
                    Calendar now = getNowCalendar(selectionMap != null ? selectionMap.get(EXTRA_ALARM_NOW) : null);
                    ArrayList<Integer> repeatingDays = (selectionMap != null ? getRepeatDays(selectionMap.get(EXTRA_ALARM_REPEAT_DAYS)) : new ArrayList<Integer>());

                    row[i] = AlarmNotifications.updateAlarmTime_solarEvent(context, event, location, offset, repeating, repeatingDays, now);
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.name();
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getLongDisplayString();
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = AlarmEvent.phrase(context, event);
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = AlarmEvent.phraseGender(context, event);
                    break;
                case COLUMN_EVENT_PHRASE_QUANTITY:
                    row[i] = AlarmEvent.phraseQuantity(context, event);
                    break;
                case COLUMN_EVENT_SUPPORTS_REPEATING:
                    row[i] = AlarmEvent.supportsRepeating(event);
                    break;
                case COLUMN_EVENT_SUPPORTS_OFFSETDAYS:
                    row[i] = Boolean.toString(AlarmEvent.supportsOffsetDays(event));
                    break;
                case COLUMN_EVENT_REQUIRES_LOCATION:
                    row[i] = Boolean.toString(AlarmEvent.requiresLocation(event));
                    break;
                case COLUMN_EVENT_SUMMARY:
                default:
                    row[i] = null;
                    break;
            }
        }
        return row;
    }

    /**
     * createRow( date+time )
     */
    private Object[] createRow(@NonNull Context context, long timedatemillis, String[] columns, @Nullable HashMap<String,String> selectionMap)
    {
        Calendar now = Calendar.getInstance();
        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.setTimeInMillis(timedatemillis);

        Calendar alarmCalendar = Calendar.getInstance();
        long offset = Long.parseLong(selectionMap != null && selectionMap.containsKey(EXTRA_ALARM_OFFSET) ? selectionMap.get(EXTRA_ALARM_OFFSET) : "0");
        alarmCalendar.setTimeInMillis(eventCalendar.getTimeInMillis() + offset);

        while (alarmCalendar.getTimeInMillis() < now.getTimeInMillis()) {
            eventCalendar.add(Calendar.YEAR, 1);
            alarmCalendar.setTimeInMillis(eventCalendar.getTimeInMillis() + offset);
            Log.w("AlarmEventProvider", "updateAlarmTime: " + timedatemillis + ", advancing by 1 year..");
        }

        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case COLUMN_EVENT_TIMEMILLIS:
                    row[i] = eventCalendar.getTimeInMillis();
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = Long.toString(timedatemillis);
                    break;
                case COLUMN_EVENT_TITLE:
                case COLUMN_EVENT_PHRASE:
                    row[i] = utils.calendarDateTimeDisplayString(getContext(), eventCalendar, true, false);
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = context.getString(R.string.date_gender);
                    break;
                case COLUMN_EVENT_PHRASE_QUANTITY:
                    row[i] = 1;   // TODO: are there locales where this value should return something other than 1?
                    break;
                case COLUMN_EVENT_SUPPORTS_REPEATING:
                    row[i] = REPEAT_SUPPORT_BASIC;
                    break;
                case COLUMN_EVENT_SUPPORTS_OFFSETDAYS:
                    row[i] = Boolean.toString(true);
                    break;
                case COLUMN_EVENT_REQUIRES_LOCATION:
                    row[i] = Boolean.toString(false);
                    break;
                case COLUMN_EVENT_SUMMARY:
                default:
                    row[i] = null;
                    break;
            }
        }
        return row;
    }

    public static Calendar getNowCalendar(String nowString)
    {
        long nowMillis = (nowString != null ? Long.parseLong(nowString) : System.currentTimeMillis());
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(nowMillis);
        return now;
    }

    public static ArrayList<Integer> getRepeatDays(@Nullable String repeatDaysString)
    {
        ArrayList<Integer> result = new ArrayList<>();
        if (repeatDaysString != null)
        {
            repeatDaysString = repeatDaysString.replaceAll("\\[", "");
            repeatDaysString = repeatDaysString.replaceAll("]", "");
            String[] repeatDaysArray = repeatDaysString.split(",");
            for (int i=0; i<repeatDaysArray.length; i++) {
                String element = repeatDaysArray[i].trim();
                if (!element.isEmpty()) {
                    result.add(Integer.parseInt(element));
                }
            }
        }
        return result;
    }

}