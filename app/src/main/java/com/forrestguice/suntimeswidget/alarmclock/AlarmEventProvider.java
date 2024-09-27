/**
    Copyright (C) 2021-2023 Forrest Guice
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
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.REPEAT_SUPPORT_DAILY;

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

        Cursor retValue = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_EVENTS:
                //Log.d(getClass().getSimpleName(), "URIMATCH_EVENTS");
                retValue = queryEvents(null, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_EVENT:
                //Log.d(getClass().getSimpleName(), "URIMATCH_EVENT: " + uri.getLastPathSegment());
                retValue = queryEvents(uri.getLastPathSegment(), uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_EVENT_CALC:
                //Log.d(getClass().getSimpleName(), "URIMATCH_EVENT_CALC: " + uri.getLastPathSegment());
                retValue = calculateEvent(uri.getLastPathSegment(), uri, projection, selection, selectionArgs);
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
    private Cursor queryEvents(@Nullable String eventID, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        HashMap<String, String> selectionMap = CalculatorProvider.processSelection(CalculatorProvider.processSelectionArgs(selection, selectionArgs));
        String[] columns = (projection != null ? projection : QUERY_EVENT_INFO_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        Context context = getContext();
        if (context != null)
        {
            if (eventID == null)
            {
                // list all SolarEvents
                SolarEvents[] events = SolarEvents.values();
                for (SolarEvents event : events) {
                    retValue.addRow(createRow(context, event, columns, selectionMap));
                }

                // list all custom events
                List<EventSettings.EventAlias> events1 = EventSettings.loadEvents(context, EventType.SUN_ELEVATION);
                events1.addAll(EventSettings.loadEvents(context, EventType.SHADOWLENGTH));
                for (EventSettings.EventAlias event : events1)
                {
                    Object[] row1 = createRow(context, event, true, columns, selection, selectionArgs);
                    if (row1 != null) {
                        retValue.addRow(row1);
                    }
                    Object[] row2 = createRow(context, event, false, columns, selection, selectionArgs);
                    if (row2 != null) {
                        retValue.addRow(row2);
                    }
                 }

            } else {
                addRowsToCursor(context, retValue, eventID, columns, selection, selectionArgs);
            }
        }
        return retValue;
    }

    /**
     * calculateEvent
     */
    private Cursor calculateEvent(String eventID, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        HashMap<String, String> selectionMap = CalculatorProvider.processSelection(CalculatorProvider.processSelectionArgs(selection, selectionArgs));
        String[] columns = (projection != null ? projection : QUERY_EVENT_CALC_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        Context context = getContext();
        if (context != null) {
            addRowsToCursor(context, retValue, eventID, columns, selection, selectionArgs);
        }
        return retValue;
    }

    private void addRowsToCursor(Context context, MatrixCursor retValue, String eventID, String[] columns, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        HashMap<String, String> selectionMap = CalculatorProvider.processSelection(CalculatorProvider.processSelectionArgs(selection, selectionArgs));
        EventType type = EventType.resolveEventType(context, eventID);
        if (type == null) {
            Log.w("AlarmEventsProvider", "queryEvents: unrecognized event: " + eventID);
            return;
        }

        switch (type)
        {
            case DATE:
                try {
                    retValue.addRow(createRow(context, Long.parseLong(eventID), columns, selectionMap));
                } catch (NumberFormatException e) {
                    Log.w("AlarmEventsProvider", "queryEvents: unrecognized date event: " + eventID + " .. " + e);
                }
                break;

            case EVENTALIAS:
                String suffix = "";
                String aliasID = eventID;
                if (eventID.endsWith(ElevationEvent.SUFFIX_RISING) || eventID.endsWith(ElevationEvent.SUFFIX_SETTING)) {
                    suffix = eventID.substring(eventID.length() - 1);
                    aliasID = aliasID.substring(0, eventID.length() - 1);
                }
                boolean rising = suffix.equals(ElevationEvent.SUFFIX_RISING);
                EventSettings.EventAlias alias = EventSettings.loadEvent(context, aliasID);
                Object[] row = createRow(context, alias, rising, columns, selection, selectionArgs);
                if (row != null) {
                    retValue.addRow(row);
                }
                break;

            case SUN_ELEVATION:
                SunElevationEvent elevationEvent = SunElevationEvent.valueOf(eventID);
                if (elevationEvent != null) {
                    retValue.addRow(createRow(context, elevationEvent, columns, selectionMap));
                }
                break;

            case SHADOWLENGTH:
                ShadowLengthEvent shadowEvent = ShadowLengthEvent.valueOf(eventID);
                if (shadowEvent != null) {
                    retValue.addRow(createRow(context, shadowEvent, columns, selectionMap));
                }
                break;

            case SOLAREVENT:
                SolarEvents event0 = (eventID != null) ? SolarEvents.valueOf(eventID, null) : null;
                if (event0 != null) {
                    retValue.addRow(createRow(context, event0, columns, selectionMap));
                }
                break;

            default:
                Log.w("AlarmEventsProvider", "queryEvents: unrecognized event (1): " + eventID);
                break;
        }
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

                    Calendar calendar = AlarmNotifications.updateAlarmTime_solarEvent(context, event, location, offset, repeating, repeatingDays, now);
                    row[i] = ((calendar != null) ? calendar.getTimeInMillis() : null);
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
     * createRow( EventAlias )
     */
    @Nullable
    private Object[] createRow(@NonNull Context context, EventSettings.EventAlias event, boolean rising, String[] columns, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        Uri uri = Uri.parse(event.getUri() + (rising ? ElevationEvent.SUFFIX_RISING : ElevationEvent.SUFFIX_SETTING));
        Cursor cursor = context.getContentResolver().query(uri, columns, selection, selectionArgs, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        if (cursor != null && cursor.isAfterLast())
        {
            Log.w("AlarmEventProvider", "null result for " + event.getID());
            cursor.close();
            return null;
        }

        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case COLUMN_EVENT_TIMEMILLIS:
                    if (cursor != null) {
                        row[i] = cursor.getLong(cursor.getColumnIndex(COLUMN_EVENT_TIMEMILLIS));
                    } else row[i] = null;
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.getID();
                    break;

                case COLUMN_EVENT_TITLE:
                    row[i] = context.getString(R.string.eventalias_title_format, event.getLabel(), context.getString(rising ? R.string.eventalias_title_tag_rising : R.string.eventalias_title_tag_setting));
                    break;

                case COLUMN_EVENT_PHRASE:
                    if (cursor != null) {
                        row[i] = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_PHRASE));
                    } else row[i] = event.getLabel();
                    break;

                case COLUMN_EVENT_PHRASE_GENDER:
                    if (cursor != null) {
                        row[i] = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_PHRASE_GENDER));
                    } else row[i] = "other";
                    break;

                case COLUMN_EVENT_PHRASE_QUANTITY:
                    if (cursor != null) {
                        row[i] = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_PHRASE_QUANTITY));
                    } else row[i] = 1;
                    break;

                case COLUMN_EVENT_SUPPORTS_REPEATING:
                    if (cursor != null) {
                        row[i] = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_SUPPORTS_REPEATING));
                    } else row[i] = REPEAT_SUPPORT_DAILY;
                    break;

                case COLUMN_EVENT_SUPPORTS_OFFSETDAYS:
                    if (cursor != null) {
                        row[i] = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_SUPPORTS_OFFSETDAYS));
                    } else row[i] = Boolean.toString(false);
                    break;

                case COLUMN_EVENT_REQUIRES_LOCATION:
                    if (cursor != null) {
                        row[i] = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_REQUIRES_LOCATION));
                    } else row[i] = Boolean.toString(true);
                    break;

                case COLUMN_EVENT_SUMMARY:
                default:
                    if (cursor != null) {
                        row[i] = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_SUMMARY));
                    } else row[i] = null;
                    break;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return row;
    }

    /**
     * createRow( sunAngle )
     */
    private Object[] createRow(@NonNull Context context, SunElevationEvent event, String[] columns, @Nullable HashMap<String,String> selectionMap)
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

                    Calendar calendar = updateAlarmTime_sunElevationEvent(context, event, location, offset, repeating, repeatingDays, now);
                    if (calendar != null) {
                        row[i] = calendar.getTimeInMillis();
                    }
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.getEventName(context);
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getEventTitle(context);
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = event.getEventPhrase(context);
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = event.getEventGender(context);
                    break;
                case COLUMN_EVENT_PHRASE_QUANTITY:
                    row[i] = 1;
                    break;
                case COLUMN_EVENT_SUPPORTS_REPEATING:
                    row[i] = REPEAT_SUPPORT_DAILY;
                    break;
                case COLUMN_EVENT_SUPPORTS_OFFSETDAYS:
                    row[i] = Boolean.toString(false);
                    break;
                case COLUMN_EVENT_REQUIRES_LOCATION:
                    row[i] = Boolean.toString(true);
                    break;
                case COLUMN_EVENT_SUMMARY:
                default:
                    row[i] = event.getEventSummary(context);
                    break;
            }
        }
        return row;
    }

    /**
     * createRow( shadowLength )
     */
    private Object[] createRow(@NonNull Context context, ShadowLengthEvent event, String[] columns, @Nullable HashMap<String,String> selectionMap)
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

                    Calendar calendar = updateAlarmTime_shadowLengthEvent(context, event, location, offset, repeating, repeatingDays, now);
                    if (calendar != null) {
                        row[i] = calendar.getTimeInMillis();
                    }
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.getEventName(context);
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getEventTitle(context);
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = event.getEventPhrase(context);
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = event.getEventGender(context);
                    break;
                case COLUMN_EVENT_PHRASE_QUANTITY:
                    row[i] = 1;
                    break;
                case COLUMN_EVENT_SUPPORTS_REPEATING:
                    row[i] = REPEAT_SUPPORT_DAILY;
                    break;
                case COLUMN_EVENT_SUPPORTS_OFFSETDAYS:
                    row[i] = Boolean.toString(false);
                    break;
                case COLUMN_EVENT_REQUIRES_LOCATION:
                    row[i] = Boolean.toString(true);
                    break;
                case COLUMN_EVENT_SUMMARY:
                default:
                    row[i] = event.getEventSummary(context);
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

    /**
     * ElevationEvent
     */
    public abstract static class ElevationEvent
    {
        public static final String SUFFIX_RISING = "r";
        public static final String SUFFIX_SETTING = "s";

        public ElevationEvent(double angle, int offset, boolean rising) {
            this.angle = angle;
            this.offset = offset;
            this.rising = rising;
        }

        protected double angle;
        public double getAngle() {
            return angle;
        }

        protected int offset;            // milliseconds
        public int getOffset() {
            return offset;
        }

        protected boolean rising;
        public boolean isRising() {
            return rising;
        }

        protected String getUri(Context context) {
            return AlarmAddon.getEventCalcUri(AUTHORITY, getEventName(context));
        }

        private static final SuntimesUtils utils = new SuntimesUtils();
        public String offsetDisplay(Context context)
        {
            if (offset != 0)
            {
                SuntimesUtils.initDisplayStrings(context);
                String offsetDisplay = utils.timeDeltaLongDisplayString(0, offset, false).getValue();
                return context.getResources().getQuantityString((offset < 0 ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), (int)angle, offsetDisplay);
            } else return "";
        }

        protected abstract String getEventName(Context context);
        protected abstract String getEventTitle(Context context);
        protected abstract String getEventPhrase(Context context);
        protected abstract String getEventGender(Context context);
        protected abstract String getEventSummary(Context context);
    }

    /**
     * SunElevationEvent
     */
    public static final class SunElevationEvent extends ElevationEvent
    {
        public static final String NAME_PREFIX = "SUN_";

        public SunElevationEvent(double angle, int offset, boolean rising) {
            super(angle, offset, rising);
        }

        @Override
        protected String getEventTitle(Context context) {
            return offsetDisplay(context) + context.getString(R.string.sunevent_title) + " " + (rising ? "rising" : "setting") + " (" + angle + ")";   // TODO: format
        }
        @Override
        protected String getEventPhrase(Context context) {
            return offsetDisplay(context) + context.getString(R.string.sunevent_title) + " " + (rising ? "rising" : "setting") + " at " + angle;   // TODO: format
        }
        @Override
        protected String getEventGender(Context context) {
            return context.getString(R.string.sunevent_phrase_gender);
        }

        @Override
        protected String getEventSummary(Context context)
        {
            SuntimesUtils utils = new SuntimesUtils();
            String angle = utils.formatAsElevation(getAngle(), 1).toString();
            if (offset == 0) {
                return offsetDisplay(context) + context.getString(R.string.sunevent_summary_format, context.getString(R.string.sunevent_title), angle.toString());
            } else {
                return context.getString(R.string.sunevent_summary_format1, offsetDisplay(context), context.getString(R.string.sunevent_title), angle.toString());
            }
        }

        public static boolean isElevationEvent(String eventName) {
            return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
        }

        /**
         * @return e.g. SUN_-10r     (@ -10 degrees (rising)),
         *              SUN_-10|-300000r  (5m before @ 10 degrees (rising))
         */
        @Override
        protected String getEventName(Context context) {
            return getEventName(angle, offset, rising);
        }
        public static String getEventName(double angle, int offset, @Nullable Boolean rising) {
            String name = NAME_PREFIX
                    + angle
                    + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
            if (rising != null) {
                name += (rising ? SUFFIX_RISING : SUFFIX_SETTING);
            }
            return name;
        }

        @Nullable
        public static SunElevationEvent valueOf(String eventName)
        {
            if (isElevationEvent(eventName))
            {
                double angle;
                int offsetMinutes = 0;
                boolean hasSuffix = eventName.endsWith(SUFFIX_RISING) || eventName.endsWith(SUFFIX_SETTING);
                try {
                    String contentString = eventName.substring(4, eventName.length() - (hasSuffix ? 1 : 0));
                    String[] contentParts = contentString.split("\\|");

                    angle = Double.parseDouble(contentParts[0]);
                    if (contentParts.length > 1) {
                        offsetMinutes = Integer.parseInt(contentParts[1]);
                    }

                } catch (Exception e) {
                    Log.e("ElevationEvent", "createEvent: bad angle: " + e);
                    return null;
                }
                boolean rising = eventName.endsWith(SUFFIX_RISING);
                return new SunElevationEvent(angle, (offsetMinutes * 60 * 1000), rising);
            } else return null;
        }
    }

    @Nullable
    public static Calendar updateAlarmTime_sunElevationEvent(Context context, @NonNull SunElevationEvent event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesRiseSetData sunData = getData_sunElevationEvent(context, event.getAngle(), event.getOffset(), location);

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        sunData.setTodayIs(day);
        sunData.calculate();
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
                Log.e(AlarmNotifications.TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                return null;
            }

            Log.w(AlarmNotifications.TAG, "updateAlarmTime: sunElevationEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            sunData.setTodayIs(day);
            sunData.calculate();
            eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        return eventTime;
    }

    private static SuntimesRiseSetData getData_sunElevationEvent(Context context, double angle, int offset, @NonNull Location location)
    {
        SuntimesRiseSetData sunData = new SuntimesRiseSetData(context, 0);
        sunData.setLocation(location);
        sunData.setAngle(angle);
        sunData.setOffset(offset);
        sunData.setTodayIs(Calendar.getInstance());
        return sunData;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * ShadowLengthEvent
     */
    public static final class ShadowLengthEvent extends ElevationEvent
    {
        public static final String NAME_PREFIX = "SHADOW_";

        public ShadowLengthEvent(double objHeight, double length, int offset, boolean rising)
        {
            super(0, offset, rising);
            this.objHeight = objHeight;
            this.length = length;

            if (this.objHeight != 0 && this.length != 0) {
                this.angle = Math.toDegrees(Math.atan(this.objHeight / this.length));
            }
        }

        protected double length;    // meters
        public double getLength() {
            return length;
        }

        protected double objHeight;    // meters
        public double getObjHeight() {
            return objHeight;
        }

        @Override
        protected String getEventTitle(Context context) {
            return offsetDisplay(context) + context.getString(R.string.shadowevent_title) + " " + (rising ? "rising" : "setting") + " (" + angle + ")";   // TODO: format
        }

        @Override
        protected String getEventPhrase(Context context) {
            return offsetDisplay(context) + context.getString(R.string.shadowevent_title) + " " + (rising ? "rising" : "setting") + " at " + angle;   // TODO: format
        }

        @Override
        protected String getEventGender(Context context) {
            return context.getString(R.string.shadowevent_phrase_gender);
        }

        @Override
        protected String getEventSummary(Context context)
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
            String height = SuntimesUtils.formatAsHeight(context, getObjHeight(), units, 1, true).getValue();

            SuntimesUtils.TimeDisplayText t = SuntimesUtils.formatAsHeight(context, getLength(), units, 1, true);
            String length = context.getString(R.string.units_format_short, t.getValue(), t.getUnits());

            if (offset == 0) {
                return offsetDisplay(context) + context.getString(R.string.shadowevent_summary_format, context.getString(R.string.shadowevent_title), height, length);
            } else {
                return context.getString(R.string.shadowevent_summary_format1, offsetDisplay(context), context.getString(R.string.shadowevent_title), height, length);
            }
        }

        public static boolean isShadowLengthEvent(String eventName) {
            return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
        }

        /**
         * @return e.g. SHADOW_1:10r          (@ 10 meters (rising)),
         *              SHADOW_1:10|-300000r  (5m before @ 10 meters (rising))
         */
        @Override
        protected String getEventName(Context context) {
            return getEventName(objHeight, length, offset, rising);
        }
        public static String getEventName(double objHeight, double length, int offset, @Nullable Boolean rising) {
            String name = NAME_PREFIX
                    + objHeight + ":" + length
                    + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
            if (rising != null) {
                name += (rising ? SUFFIX_RISING : SUFFIX_SETTING);
            }
            return name;
        }

        @Nullable
        public static ShadowLengthEvent valueOf(String eventName)
        {
            if (isShadowLengthEvent(eventName))
            {
                double height = 1, length = 1;
                int offsetMinutes = 0;
                boolean hasSuffix = eventName.endsWith(SUFFIX_RISING) || eventName.endsWith(SUFFIX_SETTING);
                try {
                    String contentString = eventName.substring(7, eventName.length() - (hasSuffix ? 1 : 0));    // SHADOW_<contentString>
                    String[] contentParts = contentString.split("\\|");

                    String[] shadowParts = contentParts[0].split(":");
                    if (shadowParts.length > 1)
                    {
                        height = Double.parseDouble(shadowParts[0]);
                        length = Double.parseDouble(shadowParts[1]);

                    } else if (shadowParts.length > 0) {
                        height = 1;
                        length = Double.parseDouble(shadowParts[0]);
                    }

                    if (contentParts.length > 1) {
                        offsetMinutes = Integer.parseInt(contentParts[1]);
                    }

                } catch (Exception e) {
                    Log.e("ShadowLengthEvent", "createEvent: bad length: " + e);
                    return null;
                }
                boolean rising = eventName.endsWith(SUFFIX_RISING);
                return new ShadowLengthEvent(height, length, (offsetMinutes * 60 * 1000), rising);
            } else return null;
        }
    }

    public static Calendar updateAlarmTime_shadowLengthEvent(Context context, @NonNull ShadowLengthEvent event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesClockData data = getData_shadowLengthEvent(context, location);
        data.initCalculator(context);
        SuntimesCalculator calculator = data.calculator();

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime = null;

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate();

        eventTime = (event.isRising() ? calculator.getTimeOfShadowBeforeNoon(day, event.getObjHeight(), event.getLength())
                                      : calculator.getTimeOfShadowAfterNoon(day, event.getObjHeight(), event.getLength()));
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
                Log.e(AlarmNotifications.TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                return null;
            }

            Log.w(AlarmNotifications.TAG, "updateAlarmTime: shadowLengthEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            data.setTodayIs(day);
            data.calculate();
            eventTime = (event.isRising() ? calculator.getTimeOfShadowBeforeNoon(day, event.getObjHeight(), event.getLength())
                                          : calculator.getTimeOfShadowAfterNoon(day, event.getObjHeight(), event.getLength()));
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        return eventTime;
    }

    private static SuntimesClockData getData_shadowLengthEvent(Context context, @NonNull Location location)
    {
        SuntimesClockData data = new SuntimesClockData(context, 0);
        data.setLocation(location);
        data.setTodayIs(Calendar.getInstance());
        return data;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * EventType
     */
    public static enum EventType
    {
        DATE,
        EVENTALIAS,
        SOLAREVENT,
        SUN_ELEVATION,
        SHADOWLENGTH;

        private EventType() //String displayString)
        {
            //this.displayString = displayString;
        }

        public static AlarmEventProvider.EventType[] visibleTypes() {
            return new AlarmEventProvider.EventType[] { AlarmEventProvider.EventType.SUN_ELEVATION, AlarmEventProvider.EventType.SHADOWLENGTH };
        }

        //private String displayString;
        //public String getDisplayString()
        //{
        //    return displayString;
        //}
        //public void setDisplayString(String value)
        //{
        //    displayString = value;
        //}
        //public static void initDisplayStrings(Context context) {
        //    SUN_ELEVATION.setDisplayString(context.getString(R.string.eventType_sun_elevation));
        //}
        //public String toString()
        //{
        //    return displayString;
        //}

        @Nullable
        public static EventType resolveEventType(Context context, String eventID)
        {
            if (isNumeric(eventID)) {
                return EventType.DATE;
            }
            if (AlarmEventProvider.SunElevationEvent.isElevationEvent(eventID)) {
                return EventType.SUN_ELEVATION;
            }
            if (AlarmEventProvider.ShadowLengthEvent.isShadowLengthEvent(eventID)) {
                return EventType.SHADOWLENGTH;
            }
            for (SolarEvents event : SolarEvents.values()) {
                if (event.name().startsWith(eventID)) {
                    return EventType.SOLAREVENT;
                }
            }
            Set<String> eventList = EventSettings.loadEventList(context);
            for (String aliasID : eventList)
            {
                if (eventID.startsWith(aliasID)) {
                    return EventType.EVENTALIAS;
                }
            }
            return null;
        }

        /**
         * @param eventID eventID
         * @return true all characters are numeric, false if any character is not [1,9]
         */
        protected static boolean isNumeric(@NonNull String eventID)
        {
            for (int i=0; i<eventID.length(); i++)
            {
                char c = eventID.charAt(i);
                boolean isNumeric = (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7'|| c == '8' || c == '9');
                if (!isNumeric) {
                    return false;
                }
            }
            return true;
        }

    }

}