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

import com.forrestguice.suntimeswidget.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.GradientDrawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;
import java.util.TimeZone;

public class WidgetTimezones
{
    public static boolean isProbablyNotLocal( String timezoneID, WidgetSettings.Location atLocation, Date onDate )
    {
        return isProbablyNotLocal(TimeZone.getTimeZone(timezoneID), atLocation, onDate);
    }
    public static boolean isProbablyNotLocal( TimeZone timezone, WidgetSettings.Location atLocation, Date onDate )
    {
        double zoneOffset = timezone.getOffset(onDate.getTime()) / (1000 * 60 * 60);   // timezone offset in hrs
        double lonOffset = atLocation.getLongitudeAsDouble() * 24 / 360;               // longitude offset in hrs
        double offsetDiff = Math.abs(lonOffset - zoneOffset);

        double offsetTolerance = 3;    // tolerance in hrs
        //noinspection UnnecessaryLocalVariable
        boolean isProbablyNotLocal = (offsetDiff > offsetTolerance);
        //Log.d("DEBUG", "offsets: " + zoneOffset + ", " + lonOffset);
        //Log.d("DEBUG", "offset delta: " +  offsetDiff +" [" + offsetTolerance + "] (" + isProbablyNotLocal + ")");

        return isProbablyNotLocal;
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static void selectTimeZone( Spinner spinner, TimeZoneItemAdapter adapter, String timezoneID )
    {
        if (spinner == null || adapter == null || timezoneID == null)
            return;

        int i = adapter.ordinal(timezoneID);
        int n = adapter.values().length;

        if (i >= 0 && i < n)
        {
            spinner.setSelection(i, false);

        } else {
            spinner.setSelection(0);
            Log.w("selectTimeZone", "unable to find timezone " + timezoneID + " in the list! Setting selection to 0.");
        }
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
         * @return the offset of this longitude from utc (in milliseconds)
         */
        public int findOffset( double longitude )
        {
            double offsetHrs = longitude * 24 / 360;           // offset from gmt in hrs
            //noinspection UnnecessaryLocalVariable
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
            //Log.d("DEBUG", "eot: " + equationOfTimeOffset);

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
        private final String timeZoneID;
        private final String displayString;
        private final double offsetHr;

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

        private View getItemView(int position, View convertView, @NonNull ViewGroup parent, boolean colorize)
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
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return getItemView(position, convertView, parent, true);
        }


        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
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

    /**
     * ActionMode (base) for sorting time zone spinners.
     */
    public abstract static class TimeZoneSpinnerSortActionBase
    {
        protected Context context;
        protected Spinner spinner;

        public void init(Context context, Spinner spinner)
        {
            this.context = context;
            this.spinner = spinner;
        }

        public void onSaveSortMode( WidgetTimezones.TimeZoneSort sortMode ) {}

        public void onSortTimeZones( TimeZoneItemAdapter adapter, WidgetTimezones.TimeZoneSort sortMode )
        {
            String msg = context.getString(R.string.timezone_sort_msg, sortMode.getDisplayString());
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
        }

        protected void sortTimeZones( final WidgetTimezones.TimeZoneSort sortMode )
        {
            onSaveSortMode(sortMode);
            WidgetTimezones.TimeZonesLoadTask loadTask = new WidgetTimezones.TimeZonesLoadTask(context);
            loadTask.setListener(new TimeZonesLoadTaskListener()
            {
                @Override
                public void onFinished(TimeZoneItemAdapter result)
                {
                    super.onFinished(result);
                    spinner.setAdapter(result);
                    onSortTimeZones(result, sortMode);
                }
            });
            loadTask.execute(sortMode);
        }

        public boolean onActionItemClicked(int action)
        {
            switch (action)
            {
                case R.id.sortById:
                    sortTimeZones(WidgetTimezones.TimeZoneSort.SORT_BY_ID);
                    return true;

                case R.id.sortByOffset:
                    sortTimeZones(WidgetTimezones.TimeZoneSort.SORT_BY_OFFSET);
                    return true;

                default:
                    return false;
            }
        }
    }

    /**
     * ActionMode for sorting time zone spinners.
     */
    @TargetApi(11)
    public static class TimeZoneSpinnerSortAction extends TimeZoneSpinnerSortActionBase implements ActionMode.Callback
    {
        public TimeZoneSpinnerSortAction(Context context, Spinner spinner)
        {
            init(context, spinner);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.timezonesort, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {}

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            if (onActionItemClicked(item.getItemId()))
            {
                mode.finish();
                return true;
            }
            return false;
        }
    }

    /**
     * ActionMode for sorting time zone spinners (support mode version).
     */
    public static class TimeZoneSpinnerSortActionCompat extends TimeZoneSpinnerSortActionBase implements android.support.v7.view.ActionMode.Callback
    {
        public TimeZoneSpinnerSortActionCompat(Context context, Spinner spinner)
        {
            init(context, spinner);
        }

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.timezonesort, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode) {}

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item)
        {
            if (onActionItemClicked(item.getItemId()))
            {
                mode.finish();
                return true;
            }
            return false;
        }
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static class TimeZonesLoadTask extends AsyncTask<TimeZoneSort, Object, TimeZoneItemAdapter>
    {
        private WeakReference<Context> contextRef;

        public TimeZonesLoadTask(Context context)
        {
            this.contextRef = new WeakReference<Context>(context);
        }

        @Override
        protected void onPreExecute()
        {
            if (listener != null)
            {
                listener.onStart();
            }
        }

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

            Context context = contextRef.get();
            if (context != null)
                return new WidgetTimezones.TimeZoneItemAdapter(context, 0, timezones, sortBy);
            else return null;
        }

        @Override
        protected void onProgressUpdate(Object... progress)
        {
        }

        @Override
        protected void onPostExecute(TimeZoneItemAdapter result)
        {
            if (result != null && listener != null)
            {
                listener.onFinished(result);
            }
        }

        private TimeZonesLoadTaskListener listener = null;
        public void setListener(TimeZonesLoadTaskListener listener)
        {
            this.listener = listener;
        }
        public void clearListener()
        {
            this.listener = null;
        }
    }

    @SuppressWarnings("EmptyMethod")
    public static abstract class TimeZonesLoadTaskListener
    {
        public void onStart() {}
        public void onFinished( WidgetTimezones.TimeZoneItemAdapter result ) {}
    }

}
