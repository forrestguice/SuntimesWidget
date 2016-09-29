/**
 Copyright (C) 2014 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class WidgetTimezones
{
    private static ArrayList<TimeZoneItem> timezones = new ArrayList<TimeZoneItem>();
    private static boolean initialized = false;

    private static void initTimezoneList()
    {
        timezones.clear();

        String[] allTimezoneValues = TimeZone.getAvailableIDs();
        for (int i = 0; i < allTimezoneValues.length; i++)
        {
            TimeZone timezone = TimeZone.getTimeZone(allTimezoneValues[i]);
            timezones.add(new TimeZoneItem(timezone.getID(), timezone.getDisplayName()));
        }

        initialized = true;
    }

    public static int ordinal( String timezoneID )
    {
        if (!initialized)
        {
            initTimezoneList();
        }

        timezoneID = timezoneID.trim();

        int ord = -1;
        for (int i=0; i<timezones.size(); i++)
        {
            String otherID = timezones.get(i).getID().trim();
            if (timezoneID.equals(otherID))
            {
                ord = i;
                break;
            }
        }
        return ord;
    }

    public static TimeZoneItem[] values()
    {
        if (!initialized)
        {
            initTimezoneList();
        }

        int numTimeZones = timezones.size();
        TimeZoneItem[] retArray = new TimeZoneItem[numTimeZones];
        for (int i=0; i<numTimeZones; i++)
        {
            retArray[i] = timezones.get(i);
        }
        return retArray;
    }

    public static List<TimeZoneItem> getValues()
    {
        if (!initialized)
        {
            initTimezoneList();
        }

        return timezones;
    }

    /**
     * Perform a rough check of the timezone/location on the given date; this function should catch
     * gross mismatches (but in many cases may be completely inaccurate).
     * @param timezoneID a timezone
     * @param atLocation a location
     * @param onDate a date
     * @return true if the timezone/location/date combination seems unreasonable (timezone is probably not local)
     */
    public static boolean isProbablyNotLocal( String timezoneID, WidgetSettings.Location atLocation, Date onDate )
    {
        double offsetTolerance = 2;    // tolerance in hrs
        TimeZone timezone = TimeZone.getTimeZone(timezoneID);

        double zoneOffset = timezone.getOffset(onDate.getTime()) / (1000 * 60 * 60);   // timezone offset in hrs
        double lonOffset = atLocation.getLongitudeAsDouble() * 24 / 360;               // longitude offset in hrs

        double offsetDiff = Math.abs(lonOffset - zoneOffset);
        Log.d("DEBUG", "offsets: " + zoneOffset + ", " + lonOffset);
        Log.d("DEBUG", "timezone offset difference: " +  offsetDiff +" [" + offsetTolerance + "]");

        return (offsetDiff > offsetTolerance);
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static class TimeZoneItem
    {
        private String timeZoneID;
        private String displayString;

        public TimeZoneItem(String timeZoneID, String displayString)
        {
            this.timeZoneID = timeZoneID;
            this.displayString = displayString;
        }

        public String getID()
        {
            return timeZoneID;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public String toString()
        {
            return timeZoneID + " (" + displayString + ")";
        }
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static class TimeZoneItemAdapter extends ArrayAdapter<TimeZoneItem>
    {
        public TimeZoneItemAdapter(Context context, int textViewResourceId)
        {
            super(context, textViewResourceId);
        }

        public TimeZoneItemAdapter(Context context, int resource, List<TimeZoneItem> items)
        {
            super(context, resource, items);
        }

        private View getItemView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.layout_listitem_twoline, parent, false);

            TimeZoneItem timezone = getItem(position);
            if (timezone == null)
            {
                Log.w("getItemView", "timezone at position " + position + " is null.");
                return view;
            }

            TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
            primaryText.setText( timezone.getID() );

            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null)
            {
                secondaryText.setText( timezone.getDisplayString() );
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            return getItemView(position, convertView, parent);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return getItemView(position, convertView, parent);
        }
    }
}
