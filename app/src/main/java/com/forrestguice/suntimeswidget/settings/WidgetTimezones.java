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
import android.os.AsyncTask;
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
        private double offsetHr;

        public TimeZoneItem(String timeZoneID, String displayString, double offsetHr)
        {
            this.timeZoneID = timeZoneID;
            this.displayString = displayString;
            this.offsetHr = offsetHr;
        }

        public String getID()
        {
            return timeZoneID;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public double getOffsetHr()
        {
            return offsetHr;
        }

        public String getOffsetString()
        {
            return offsetHr + "";
        }

        public String toString()
        {
            return timeZoneID + " (" + getOffsetString() + " " + displayString + ")";
        }
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static class TimeZoneItemAdapter extends ArrayAdapter<TimeZoneItem>
    {
        private int[] colors;
        private TimeZoneSort sortBy = null;
        private String line1, line2;
        private List<TimeZoneItem> items;

        public TimeZoneItemAdapter(Context context, int resource)
        {
            super(context, resource);
            init(context);
        }

        public TimeZoneItemAdapter(Context context, int resource, List<TimeZoneItem> items)
        {
            super(context, resource, items);
            this.items = items;
            init(context);
        }

        public TimeZoneItemAdapter(Context context, int resource, List<TimeZoneItem> items, TimeZoneSort sortBy)
        {
            super(context, resource, items);
            this.items = items;
            this.sortBy = sortBy;
            init(context);
            sort();
        }

        private void init(Context context)
        {
            colors = context.getResources().getIntArray(R.array.utcOffsetColors);
            line1 = context.getString(R.string.timezoneCustom_line1);
            line2 = context.getString(R.string.timezoneCustom_line2);
        }

        public int getColorForTimeZoneOffset( double utcHour )
        {
            int offset = (int)Math.round(utcHour);
            while (offset < 0)
            {
                offset += 12;
            }
            return colors[offset % colors.length];
        }

        public TimeZoneSort getSort()
        {
            return sortBy;
        }

        private void sort()
        {
            if (sortBy != null)
            {
                Collections.sort(items, sortBy.getComparator());
                notifyDataSetChanged();
            }
        }

        private View getItemView(int position, View convertView, ViewGroup parent, boolean colorize)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.layout_listitem_timezone, parent, false);

            TimeZoneItem timezone = getItem(position);
            if (timezone == null)
            {
                Log.w("getItemView", "timezone at position " + position + " is null.");
                return view;
            }

            TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
            primaryText.setText(String.format(line1, timezone.getID()));

            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null)
            {
                secondaryText.setText(String.format(line2, timezone.getOffsetString(), timezone.getDisplayString()));
            }

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            if (icon != null)
            {
                if (colorize)
                {
                    GradientDrawable d = (GradientDrawable) icon.getBackground().mutate();
                    d.setColor(getColorForTimeZoneOffset(timezone.getOffsetHr()));
                    d.invalidateSelf();
                    icon.setVisibility(View.VISIBLE);

                } else {
                    icon.setVisibility(View.GONE);
                }
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            return getItemView(position, convertView, parent, true);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return getItemView(position, convertView, parent, false);
        }


        public int ordinal( String timezoneID )
        {
            timezoneID = timezoneID.trim();

            int ord = -1;
            for (int i=0; i<items.size(); i++)
            {
                String otherID = items.get(i).getID().trim();
                if (timezoneID.equals(otherID))
                {
                    ord = i;
                    break;
                }
            }
            return ord;
        }

        public TimeZoneItem[] values()
        {
            int numTimeZones = items.size();
            TimeZoneItem[] retArray = new TimeZoneItem[numTimeZones];
            for (int i=0; i<numTimeZones; i++)
            {
                retArray[i] = items.get(i);
            }
            return retArray;
        }

        public List<TimeZoneItem> getValues()
        {
            return items;
        }
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public enum TimeZoneSort
    {
        SORT_BY_OFFSET("offset"), SORT_BY_ID("id"), SORT_BY_DISPLAYNAME("name");

        private String displayString;

        private TimeZoneSort( String displayString )
        {
            this.displayString = displayString;
        }

        public void setDisplayString(String displayString)
        {
            this.displayString = displayString;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public String toString()
        {
            return getDisplayString();
        }

        public static void initDisplayStrings( Context context )
        {
            String[] labels = context.getResources().getStringArray( R.array.timezoneSort_display );
            SORT_BY_OFFSET.setDisplayString(labels[0]);
            SORT_BY_ID.setDisplayString(labels[1]);
            SORT_BY_DISPLAYNAME.setDisplayString(labels[1]);
        }

        public Comparator<TimeZoneItem> getComparator()
        {
            Comparator<TimeZoneItem> c;
            switch (this)
            {
                case SORT_BY_OFFSET:
                    c = new Comparator<TimeZoneItem>()
                    {
                        @Override
                        public int compare(TimeZoneItem t1, TimeZoneItem t2)
                        {
                            Double offset1 = t1.getOffsetHr();
                            Double offset2 = t2.getOffsetHr();
                            return offset1.compareTo(offset2);
                        }
                    };
                    break;

                case SORT_BY_DISPLAYNAME:
                    c = new Comparator<TimeZoneItem>()
                    {
                        @Override
                        public int compare(TimeZoneItem t1, TimeZoneItem t2)
                        {
                            return t1.getDisplayString().compareTo(t2.getDisplayString());
                        }
                    };
                    break;

                case SORT_BY_ID:
                default:
                    c = new Comparator<TimeZoneItem>()
                    {
                        @Override
                        public int compare(TimeZoneItem t1, TimeZoneItem t2)
                        {
                            return t1.getID().compareTo(t2.getID());
                        }
                    };
                    break;
            }
            return c;
        }
    }
    ///////////////////////////////////////
    ///////////////////////////////////////

    public static class TimeZonesLoadTask extends AsyncTask<TimeZoneSort, Object, TimeZoneItemAdapter>
    {
        private Context context;

        public TimeZonesLoadTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected TimeZoneItemAdapter doInBackground(TimeZoneSort... sorts)
        {
            TimeZoneSort sortBy = null;
            if (sorts != null && sorts.length > 0)
            {
                sortBy = sorts[0];
            }

            ArrayList<TimeZoneItem> timezones = new ArrayList<TimeZoneItem>();
            String[] allTimezoneValues = TimeZone.getAvailableIDs();
            for (int i = 0; i < allTimezoneValues.length; i++)
            {
                TimeZone timezone = TimeZone.getTimeZone(allTimezoneValues[i]);
                double offsetHr = timezone.getRawOffset() / (1000 * 60 * 60);
                timezones.add(new TimeZoneItem(timezone.getID(), timezone.getDisplayName(), offsetHr));
            }

            if (sortBy != null)
            {
                Collections.sort(timezones, sortBy.getComparator());
            }

            return new WidgetTimezones.TimeZoneItemAdapter(context, 0, timezones, sortBy);
        }

        @Override
        protected void onProgressUpdate(Object... progress)
        {
        }

        @Override
        protected void onPostExecute(TimeZoneItemAdapter result)
        {
        }
    }

}
