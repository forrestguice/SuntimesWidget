/**
    Copyright (C) 2021-2025 Forrest Guice
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
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidEventSettings;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;
import com.forrestguice.suntimeswidget.events.DayPercentEvent;
import com.forrestguice.suntimeswidget.events.ElevationEvent;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.events.EventType;
import com.forrestguice.suntimeswidget.events.MoonElevationEvent;
import com.forrestguice.suntimeswidget.events.MoonIllumEvent;
import com.forrestguice.suntimeswidget.events.ShadowLengthEvent;
import com.forrestguice.suntimeswidget.events.SunElevationEvent;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.util.android.AndroidResources;

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
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_TYPE;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_TYPE_LABEL;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_NOW;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_OFFSET;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_REPEAT;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.EXTRA_ALARM_REPEAT_DAYS;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_CALC;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_CALC_PROJECTION;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_INFO;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_INFO_PROJECTION;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_TYPES;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_TYPES_PROJECTION;
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
    private static final int URIMATCH_EVENT_TYPES = 30;

    private SuntimesUtils utils = null;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_INFO, URIMATCH_EVENTS);                            // content://AUTHORITY/eventInfo
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_INFO + "/*", URIMATCH_EVENT);                // content://AUTHORITY/eventInfo/[eventID]
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_CALC + "/*", URIMATCH_EVENT);                // content://AUTHORITY/eventCalc/[eventID]
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_TYPES, URIMATCH_EVENT_TYPES);                      // content://AUTHORITY/eventTypes
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
        initDisplayStrings_EventType(getContext());

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

            case URIMATCH_EVENT_TYPES:
                //Log.d(getClass().getSimpleName(), "URIMATCH_EVENT_TYPES: " + uri.getLastPathSegment());
                retValue = queryEventTypes(uri, projection, selection, selectionArgs, sortOrder);
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
                String ofType = selectionMap.get(COLUMN_EVENT_TYPE);
                if (ofType == null || EventType.SOLAREVENT.name().equals(ofType)) {
                    for (SolarEvents event : SolarEvents.values()) {
                        retValue.addRow(createRow(context, event, columns, selectionMap));
                    }
                }

                if (ofType != null && ofType.startsWith(EventType.SOLAREVENT.name()))
                {
                    Integer subtype = null;
                    try {
                        String[] parts = ofType.split("_");
                        subtype = (parts.length > 1) ? Integer.parseInt(parts[1]) : null;
                    } catch (NumberFormatException e) {
                        Log.w("AlarmEventProvider", "invalid SolarEvents subtype; ignoring..." );
                    }
                    for (SolarEvents event : SolarEvents.values(subtype)) {
                        retValue.addRow(createRow(context, event, columns, selectionMap));
                    }
                }

                List<EventAlias> events1 = new ArrayList<>();    // list custom events
                if (ofType == null || EventType.SUN_ELEVATION.name().equals(ofType)) {
                    events1.addAll(EventSettings.loadEvents(AndroidEventSettings.wrap(context), EventType.SUN_ELEVATION));
                }
                if (ofType == null || EventType.SHADOWLENGTH.name().equals(ofType)) {
                    events1.addAll(EventSettings.loadEvents(AndroidEventSettings.wrap(context), EventType.SHADOWLENGTH));
                }
                if (ofType == null || EventType.DAYPERCENT.name().equals(ofType)) {
                    events1.addAll(EventSettings.loadEvents(AndroidEventSettings.wrap(context), EventType.DAYPERCENT));
                }
                if (ofType == null || EventType.MOON_ELEVATION.name().equals(ofType)) {
                    events1.addAll(EventSettings.loadEvents(AndroidEventSettings.wrap(context), EventType.MOON_ELEVATION));
                }
                if (ofType == null || EventType.MOONILLUM.name().equals(ofType)) {
                    events1.addAll(EventSettings.loadEvents(AndroidEventSettings.wrap(context), EventType.MOONILLUM));
                }

                for (EventAlias event : events1)
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
     * queryEventTypes
     */
    private Cursor queryEventTypes(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        HashMap<String, String> selectionMap = CalculatorProvider.processSelection(CalculatorProvider.processSelectionArgs(selection, selectionArgs));
        String[] columns = (projection != null ? projection : QUERY_EVENT_TYPES_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        Context context = getContext();
        if (context != null)
        {
            for (EventType type : EventType.values())
            {
                if (type == EventType.SOLAREVENT) {
                    for (int subtype : SolarEvents.types()) {
                        retValue.addRow(createRow(context, type, subtype, columns, selectionMap));
                    }
                } else {
                    retValue.addRow(createRow(context, type, 0, columns, selectionMap));
                }
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
        EventType type = EventType.resolveEventType(AndroidEventSettings.wrap(context), eventID);
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
                EventAlias alias = EventSettings.loadEvent(AndroidEventSettings.wrap(context), aliasID);
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

            case DAYPERCENT:
                DayPercentEvent percentEvent = DayPercentEvent.valueOf(eventID);
                if (percentEvent != null) {
                    retValue.addRow(createRow(context, percentEvent, columns, selectionMap));
                }
                break;

            case MOONILLUM:
                MoonIllumEvent moonIllumEvent = MoonIllumEvent.valueOf(eventID);
                if (moonIllumEvent != null) {
                    retValue.addRow(createRow(context, moonIllumEvent, columns, selectionMap));
                }
                break;

            case MOON_ELEVATION:
                MoonElevationEvent moonElevationEvent = MoonElevationEvent.valueOf(eventID);
                if (moonElevationEvent != null) {
                    retValue.addRow(createRow(context, moonElevationEvent, columns, selectionMap));
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
     * createRow( EventType )
     */
    private Object[] createRow(@NonNull Context context, @NonNull EventType type, Integer subtype, String[] columns, @Nullable HashMap<String,String> selectionMap)
    {
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case COLUMN_EVENT_TYPE:
                    if (type == EventType.SOLAREVENT) {
                        row[i] = EventType.SOLAREVENT.getSubtypeID(subtype + "");
                    } else {
                        row[i] = type.name();
                    }
                    break;

                case COLUMN_EVENT_TYPE_LABEL:
                    if (type == EventType.SOLAREVENT) {
                        row[i] = SolarEvents.getTypeLabel(AndroidResources.wrap(context), subtype);
                    } else {
                        row[i] = type.getDisplayString();
                    }
                    break;

                default:
                    row[i] = null;
                    break;
            }
        }
        return row;
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
                case COLUMN_EVENT_TYPE:
                    row[i] = EventType.SOLAREVENT.getSubtypeID(event.getType() + "");
                    break;
                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = EventType.SOLAREVENT.getDisplayString();
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
    public static Object[] createRow(@NonNull Context context, EventAlias event, boolean rising, String[] columns, @Nullable String selection, @Nullable String[] selectionArgs)
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
                        int j = cursor.getColumnIndex(COLUMN_EVENT_TIMEMILLIS);
                        row[i] = (j >= 0) ? cursor.getLong(j) : null;
                    } else row[i] = null;
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.getID();
                    break;

                case COLUMN_EVENT_TYPE:
                    row[i] = event.getType().name();
                    break;

                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = event.getType().getDisplayString();
                    break;

                case COLUMN_EVENT_TITLE:
                    row[i] = context.getString(R.string.eventalias_title_format, event.getLabel(), context.getString(rising ? R.string.eventalias_title_tag_rising : R.string.eventalias_title_tag_setting));
                    break;

                case COLUMN_EVENT_PHRASE:
                    if (cursor != null) {
                        int j = cursor.getColumnIndex(COLUMN_EVENT_PHRASE);
                        row[i] = (j >= 0) ? cursor.getString(j) : event.getLabel();
                    } else row[i] = event.getLabel();
                    break;

                case COLUMN_EVENT_PHRASE_GENDER:
                    if (cursor != null) {
                        int j = cursor.getColumnIndex(COLUMN_EVENT_PHRASE_GENDER);
                        row[i] = (j >= 0) ? cursor.getString(j) : "other";
                    } else row[i] = "other";
                    break;

                case COLUMN_EVENT_PHRASE_QUANTITY:
                    if (cursor != null) {
                        int j = cursor.getColumnIndex(COLUMN_EVENT_PHRASE_QUANTITY);
                        row[i] = (j >= 0) ? cursor.getInt(j) : 1;
                    } else row[i] = 1;
                    break;

                case COLUMN_EVENT_SUPPORTS_REPEATING:
                    if (cursor != null) {
                        int j = cursor.getColumnIndex(COLUMN_EVENT_SUPPORTS_REPEATING);
                        row[i] = (j >= 0) ? cursor.getInt(j) : REPEAT_SUPPORT_DAILY;
                    } else row[i] = REPEAT_SUPPORT_DAILY;
                    break;

                case COLUMN_EVENT_SUPPORTS_OFFSETDAYS:
                    if (cursor != null) {
                        int j = cursor.getColumnIndex(COLUMN_EVENT_SUPPORTS_OFFSETDAYS);
                        row[i] = (j >= 0) ? cursor.getString(j) : Boolean.toString(false);
                    } else row[i] = Boolean.toString(false);
                    break;

                case COLUMN_EVENT_REQUIRES_LOCATION:
                    if (cursor != null) {
                        int j = cursor.getColumnIndex(COLUMN_EVENT_REQUIRES_LOCATION);
                        row[i] = (j >= 0) ? cursor.getString(j) : Boolean.toString(true);
                    } else row[i] = Boolean.toString(true);
                    break;

                case COLUMN_EVENT_SUMMARY:
                default:
                    if (cursor != null) {
                        int j = cursor.getColumnIndex(COLUMN_EVENT_SUMMARY);
                        row[i] = (j >= 0) ? cursor.getString(j) : null;
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
                    row[i] = event.getEventName();
                    break;
                case COLUMN_EVENT_TYPE:
                    row[i] = EventType.SUN_ELEVATION.name();
                    break;
                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = EventType.SUN_ELEVATION.getDisplayString();
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getEventTitle(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = event.getEventPhrase(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = event.getEventGender(AndroidSuntimesDataSettings.wrap(context));
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
                    row[i] = event.getEventSummary(AndroidSuntimesDataSettings.wrap(context));
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
                    row[i] = event.getEventName();
                    break;
                case COLUMN_EVENT_TYPE:
                    row[i] = EventType.SHADOWLENGTH.name();
                    break;
                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = EventType.SHADOWLENGTH.getDisplayString();
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getEventTitle(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = event.getEventPhrase(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = event.getEventGender(AndroidSuntimesDataSettings.wrap(context));
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
                    row[i] = event.getEventSummary(AndroidSuntimesDataSettings.wrap(context));
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
                case COLUMN_EVENT_TYPE:
                    row[i] = EventType.DATE.name();
                    break;
                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = EventType.DATE.getDisplayString();
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

    @Nullable
    public static Calendar updateAlarmTime_sunElevationEvent(Context context, @NonNull SunElevationEvent event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesRiseSetData sunData = getData_sunElevationEvent(context, event.getAngle(), event.getOffset(), location);

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(now.getTimeInMillis());

        sunData.setTodayIs(day);
        sunData.calculate(context);
        eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
        if (eventTime != null) {
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
            sunData.calculate(context);
            eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
            if (eventTime != null) {
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

    public static Calendar updateAlarmTime_shadowLengthEvent(Context context, @NonNull ShadowLengthEvent event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesClockData data = getClockData(context, location);
        data.initCalculator();
        SuntimesCalculator calculator = data.calculator();

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime = null;

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate(context);

        eventTime = (event.isRising() ? calculator.getTimeOfShadowBeforeNoon(day, event.getObjHeight(), event.getLength())
                                      : calculator.getTimeOfShadowAfterNoon(day, event.getObjHeight(), event.getLength()));
        if (eventTime != null) {
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
            data.calculate(context);
            eventTime = (event.isRising() ? calculator.getTimeOfShadowBeforeNoon(day, event.getObjHeight(), event.getLength())
                                          : calculator.getTimeOfShadowAfterNoon(day, event.getObjHeight(), event.getLength()));
            if (eventTime != null) {
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        return eventTime;
    }

    private static SuntimesClockData getClockData(Context context, @NonNull Location location)
    {
        SuntimesClockData data = new SuntimesClockData(context, 0);
        data.setLocation(location);
        data.setTodayIs(Calendar.getInstance());
        return data;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private Object[] createRow(@NonNull Context context, DayPercentEvent event, String[] columns, @Nullable HashMap<String,String> selectionMap)
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

                    SuntimesData data = getClockData(context, location);
                    Calendar calendar = DayPercentEvent.updateAlarmTime(context, event, data, offset, repeating, repeatingDays, now);
                    if (calendar != null) {
                        row[i] = calendar.getTimeInMillis();
                    }
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.getEventName();
                    break;
                case COLUMN_EVENT_TYPE:
                    row[i] = EventType.DAYPERCENT.name();
                    break;
                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = EventType.DAYPERCENT.getDisplayString();
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getEventTitle(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = event.getEventPhrase(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = event.getEventGender(AndroidSuntimesDataSettings.wrap(context));
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
                    row[i] = event.getEventSummary(AndroidSuntimesDataSettings.wrap(context));
                    break;
            }
        }
        return row;
    }

    private Object[] createRow(@NonNull Context context, MoonIllumEvent event, String[] columns, @Nullable HashMap<String,String> selectionMap)
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

                    SuntimesData data = getClockData(context, location);
                    Calendar calendar = MoonIllumEvent.updateAlarmTime(context, event, data, offset, repeating, repeatingDays, now);
                    Log.d("DEBUG", "createRow: isRising? " + event.isWaxing() + ": " + calendar);
                    if (calendar != null) {
                        row[i] = calendar.getTimeInMillis();
                    }
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.getEventName();
                    break;
                case COLUMN_EVENT_TYPE:
                    row[i] = EventType.MOONILLUM.name();
                    break;
                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = EventType.MOONILLUM.getDisplayString();
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getEventTitle(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = event.getEventPhrase(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = event.getEventGender(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE_QUANTITY:
                    row[i] = 1;
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
                    row[i] = event.getEventSummary(AndroidSuntimesDataSettings.wrap(context));
                    break;
            }
        }
        return row;
    }

    private Object[] createRow(@NonNull Context context, MoonElevationEvent event, String[] columns, @Nullable HashMap<String,String> selectionMap)
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

                    SuntimesMoonData data = getMoonData(context, location);
                    Calendar calendar = MoonElevationEvent.updateAlarmTime(context, event, data, offset, repeating, repeatingDays, now);
                    if (calendar != null) {
                        row[i] = calendar.getTimeInMillis();
                    }
                    break;

                case COLUMN_EVENT_NAME:
                    row[i] = event.getEventName();
                    break;
                case COLUMN_EVENT_TYPE:
                    row[i] = EventType.MOON_ELEVATION.name();
                    break;
                case COLUMN_EVENT_TYPE_LABEL:
                    row[i] = EventType.MOON_ELEVATION.getDisplayString();
                    break;
                case COLUMN_EVENT_TITLE:
                    row[i] = event.getEventTitle(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE:
                    row[i] = event.getEventPhrase(AndroidSuntimesDataSettings.wrap(context));
                    break;
                case COLUMN_EVENT_PHRASE_GENDER:
                    row[i] = event.getEventGender(AndroidSuntimesDataSettings.wrap(context));
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
                    row[i] = event.getEventSummary(AndroidSuntimesDataSettings.wrap(context));
                    break;
            }
        }
        return row;
    }

    private static SuntimesMoonData getMoonData(Context context, @NonNull Location location)
    {
        SuntimesMoonData data = new SuntimesMoonData(context, 0);
        data.setLocation(location);
        data.setTodayIs(Calendar.getInstance());
        data.calculate(context);
        return data;
    }

    public static void initDisplayStrings_EventType(Context context) {
        EventType.SUN_ELEVATION.setDisplayString(context.getString(R.string.eventType_sun_elevation));
    }

}