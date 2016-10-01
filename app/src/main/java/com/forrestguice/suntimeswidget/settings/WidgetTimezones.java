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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static TimeZone localMeanTime( Context context, WidgetSettings.Location location )
    {
        return new LocalMeanTime(location.getLongitude(), context.getString(R.string.solartime_localMean));
    }

    public static TimeZone apparentSolarTime( Context context, WidgetSettings.Location location )
    {
        return new ApparentSolarTime(location.getLongitude(), context.getString(R.string.solartime_apparent));
    }

    /**
     * LocalMeanTime : TimeZone
     */
    public static class LocalMeanTime extends TimeZone
    {
        public static final String TIMEZONEID = "Local Mean Time";

        private int rawOffset = 0;

        public LocalMeanTime(String longitude)
        {
            super();
            setID(TIMEZONEID);
            setRawOffset(findOffset(Double.parseDouble(longitude)));
        }

        public LocalMeanTime(double longitude)
        {
            super();
            setID(TIMEZONEID);
            setRawOffset(findOffset(longitude));
        }

        public LocalMeanTime(String longitude, String name)
        {
            super();
            setID(name);
            setRawOffset(findOffset(Double.parseDouble(longitude)));
        }

        public LocalMeanTime(double longitude, String name)
        {
            super();
            setID(name);
            setRawOffset(findOffset(longitude));
        }

        /**
         * @param longitude a longitude value; degrees [-180, 180]
         * @return the offset of this longitude from utc (in miliseconds)
         */
        public int findOffset( double longitude )
        {
            double offsetHrs = longitude * 24 / 360;           // offset from gmt in hrs
            int offsetMs = (int)(offsetHrs * 60 * 60 * 1000);  // hrs * 60min in a day * 60s in a min * 1000ms in a second
            //Log.d("DEBUG", "offset: " + offsetHrs + " (" + offsetMs + ")");
            return offsetMs;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds)
        {
            return getRawOffset();
        }

        @Override
        public int getOffset( long date )
        {
            return getRawOffset();
        }

        @Override
        public int getRawOffset()
        {
            return rawOffset;
        }

        @Override
        public void setRawOffset(int offset)
        {
            rawOffset = offset;
        }

        @Override
        public boolean inDaylightTime(Date date)
        {
            return false;
        }

        @Override
        public boolean useDaylightTime()
        {
            return false;
        }

        @Override
        public String toString()
        {
            return "id: " + getID() + ", offset: " + getRawOffset() + ", useDaylight: " + useDaylightTime();
        }
    }

    /**
     * ApparentSolarTime : TimeZone
     */
    public static class ApparentSolarTime extends LocalMeanTime
    {
        public static final String TIMEZONEID = "Apparent Solar Time";

        public ApparentSolarTime(String longitude)
        {
            super(longitude, TIMEZONEID);
        }

        public ApparentSolarTime(double longitude)
        {
            super(longitude, TIMEZONEID);
        }

        public ApparentSolarTime(String longitude, String name)
        {
            super(longitude, name);
        }

        public ApparentSolarTime(double longitude, String name)
        {
            super(longitude, name);
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds)
        {
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, month, day);
            return getOffset(calendar.getTimeInMillis());
        }

        /**
         * @param date a given date
         * @return ms offset with "equation of time" correction applied for the given date
         */
        @Override
        public int getOffset( long date )
        {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(date);
            double equationOfTimeOffset = equationOfTimeOffset(calendar.get(Calendar.DAY_OF_YEAR));  // equation of time correction (minutes)
            Log.d("DEBUG", "eot: " + equationOfTimeOffset);

            int localMeanOffsetMs = getRawOffset();
            int equationOfTimeOffsetMs = (int)(equationOfTimeOffset * 60 * 1000);
            return localMeanOffsetMs + equationOfTimeOffsetMs;
        }

        /**
         * http://www.esrl.noaa.gov/gmd/grad/solcalc/solareqns.PDF
         * @param n day of year (n=1 is january 1)
         * @return equation of time correction in decimal minutes
         */
        private double equationOfTimeOffset(int n)
        {
            while (n <= 0)    // n in range [1, 365]
            {
                n += 365;
            }
            while (n > 365)
            {
                n -= 365;
            }

            double d = (2 * Math.PI / 365.24) * (n - 1);   // fractional year (radians)
            return 229.18 * (0.000075
                    + 0.001868 * Math.cos(d)
                    - 0.032077 * Math.sin(d)
                    - 0.014615 * Math.cos(2*d)
                    - 0.040849 * Math.sin(2*d));   // .oO(a truly magical return statement)
        }

        @Override
        public boolean useDaylightTime()
        {
            return true;
        }
        public boolean observesDaylightTime()
        {
            return useDaylightTime();
        }

        @Override
        public boolean inDaylightTime(Date date)
        {
            return true;
        }

        @Override
        public int getDSTSavings()
        {
            return 1;
        }
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
