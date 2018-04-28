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

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Calendar;

public class SuntimesCalendarAdapter
{
    public static final String CALENDAR_SOLSTICE = "solsticeCalendar";
    public static final String CALENDAR_MOONPHASE = "moonPhaseCalendar";

    private ContentResolver contentResolver;

    public SuntimesCalendarAdapter(ContentResolver contentResolver)
    {
        this.contentResolver = contentResolver;
    }

    /**
     * Creates a new calender managed by the "Suntimes" local account.
     * @param calendarName the calendar's name
     * @param calendarDisplayName the calendar's display string
     * @param calendarColor the calendar's color (an index into calendar color table)
     */
    public void createCalendar(String calendarName, String calendarDisplayName, int calendarColor)
    {
        Uri uri = SuntimesSyncAdapter.asSyncAdapter(CalendarContract.Calendars.CONTENT_URI);
        ContentValues contentValues = SuntimesCalendarAdapter.createCalendarContentValues(calendarName, calendarDisplayName, calendarColor);
        contentResolver.insert(uri, contentValues);
    }

    /**
     * Removes all calendars managed by the "Suntimes" local account.
     */
    public boolean removeCalendars()
    {
        Cursor cursor = queryCalendars();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                long calendarID = cursor.getLong(PROJECTION_ID_INDEX);
                Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarID);
                contentResolver.delete(deleteUri, null, null);
                Log.d("removeCalendars", "removed calendar " + calendarID);
            }
            return true;
        } else return false;
    }

    /**
     * @param calendarID the calendar's ID
     * @param title the event title
     * @param description the event description
     * @param time the startTime of the event (endTime is the same)
     */
    @TargetApi(14)
    public void createCalendarEvent(long calendarID, String title, String description, Calendar time) throws SecurityException
    {
        ContentValues contentValues = SuntimesCalendarAdapter.createEventContentValues(calendarID, title, description, time);
        contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues);
    }

    /**
     * @return a Cursor to all calendars managed by the "Suntimes" local account
     */
    @TargetApi(14)
    public Cursor queryCalendars()
    {
        Uri uri = SuntimesSyncAdapter.asSyncAdapter(CalendarContract.Calendars.CONTENT_URI);
        String[] args = new String[] { SuntimesSyncAdapter.ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, SuntimesSyncAdapter.ACCOUNT_NAME };
        String select = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND (" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND (" + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        return contentResolver.query(uri, EVENT_PROJECTION, select, args, null);
    }

    /**
     * @param calendarName the calendar's name
     * @return a Cursor to the calendar w/ the given name managed by the "Suntimes" local account.
     */
    @TargetApi(14)
    public Cursor queryCalendar(String calendarName)
    {
        Uri uri = SuntimesSyncAdapter.asSyncAdapter(CalendarContract.Calendars.CONTENT_URI);
        String[] args = new String[] { SuntimesSyncAdapter.ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, calendarName, SuntimesSyncAdapter.ACCOUNT_NAME };
        String select = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.NAME + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        return contentResolver.query(uri, EVENT_PROJECTION, select, args, null);
    }

    /**
     * @param calendarName
     * @return
     */
    public long queryCalendarID(String calendarName)
    {
        long calendarID = -1;
        Cursor cursor = queryCalendar(calendarName);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                calendarID = cursor.getLong(PROJECTION_ID_INDEX);
            }
        } else {
            Log.w("initCalendars", "Calendar not found! (null cursor) " + calendarName);
            calendarID = -1;
        }
        return calendarID;
    }

    /**
     * @param calendarName the calendar's name
     * @return true if a calendar w/ given name is already managed by the "Suntimes" local account, false otherwise.
     */
    public boolean hasCalendar(String calendarName)
    {
        Cursor cursor = queryCalendar(calendarName);
        return (cursor != null && cursor.getCount() > 0);
    }

    /**
     * @param calendarName
     * @param displayName
     * @param calendarColor
     * @return
     */
    @TargetApi(15)
    public static ContentValues createCalendarContentValues(String calendarName, String displayName, int calendarColor)
    {
        ContentValues v = new ContentValues();
        v.put(CalendarContract.Calendars.ACCOUNT_NAME, SuntimesSyncAdapter.ACCOUNT_NAME);
        v.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        v.put(CalendarContract.Calendars.OWNER_ACCOUNT, SuntimesSyncAdapter.ACCOUNT_NAME);
        v.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);

        v.put(CalendarContract.Calendars.NAME, calendarName);
        v.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, displayName);
        v.put(CalendarContract.Calendars.CALENDAR_COLOR, calendarColor);
        v.put(CalendarContract.Calendars.VISIBLE, 1);
        v.put(CalendarContract.Calendars.SYNC_EVENTS, 1);

        v.put(CalendarContract.Calendars.ALLOWED_REMINDERS, "METHOD_ALERT, METHOD_EMAIL, METHOD_ALARM");
        //v.put(CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES, "TYPE_OPTIONAL, TYPE_REQUIRED, TYPE_RESOURCE");
        v.put(CalendarContract.Calendars.ALLOWED_AVAILABILITY, "AVAILABILITY_BUSY, AVAILABILITY_FREE, AVAILABILITY_TENTATIVE");

        return v;
    }

    /**
     * @param calendarID
     * @param title
     * @param description
     * @param time
     * @return
     */
    @TargetApi(14)
    public static ContentValues createEventContentValues(long calendarID, String title, String description, Calendar time)
    {
        ContentValues v = new ContentValues();
        v.put(CalendarContract.Events.CALENDAR_ID, calendarID);
        v.put(CalendarContract.Events.TITLE, title);
        v.put(CalendarContract.Events.DESCRIPTION, description);

        v.put(CalendarContract.Events.DTSTART, time.getTimeInMillis());
        v.put(CalendarContract.Events.DTEND, time.getTimeInMillis());
        v.put(CalendarContract.Events.EVENT_TIMEZONE, time.getTimeZone().getID());

        //v.put(CalendarContract.Events.EVENT_LOCATION, "Local");

        v.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        v.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_MODIFY, "0");
        return v;
    }

    /**
     * EVENT_PROJECTION
     */
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

}
