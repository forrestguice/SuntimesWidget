/**
 Copyright (C) 2014-2022 Forrest Guice
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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
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
import com.forrestguice.suntimeswidget.views.Toast;
import android.graphics.drawable.GradientDrawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;
import java.util.TimeZone;

public class WidgetTimezones
{
    public static final String TZID_UTC = "UTC";
    public static final String TZID_SUNTIMES = "SUNTIMES";
    public static final String TZID_SYSTEM = "SYSTEM";

    public static boolean isProbablyNotLocal(TimeZone timezone, Location atLocation, Date onDate ) {
        return isProbablyNotLocal(timezone, atLocation.getLongitudeAsDouble(), onDate);
    }
    public static boolean isProbablyNotLocal(TimeZone timezone, double longitude, Date onDate )
    {
        if (timezone.getID().equals(TZID_UTC) || timezone.getID().equals(TimeZones.SiderealTime.TZID_GMST) || timezone.getID().equals(TimeZones.SiderealTime.TZID_LMST)) {
            return false;
        }

        double zoneOffset = timezone.getOffset(onDate.getTime()) / (1000d * 60d * 60d);   // timezone offset in hrs
        double lonOffset = longitude * 24d / 360d;               // longitude offset in hrs
        double offsetDiff = Math.abs(lonOffset - zoneOffset);

        //noinspection UnnecessaryLocalVariable
        boolean isProbablyNotLocal = (offsetDiff >= WARNING_TOLERANCE_HOURS);
        //Log.d("DEBUG", "offsets: " + zoneOffset + ", " + lonOffset);
        //Log.d("DEBUG", "offset delta: " +  offsetDiff +" [" + offsetTolerance + "] (" + isProbablyNotLocal + ")");

        return isProbablyNotLocal;
    }
    public static final double WARNING_TOLERANCE_HOURS = 3;

    public static TimeZone getTimeZone(String tzId, @Nullable Double longitude, @Nullable SuntimesCalculator calculator)
    {
        if (longitude == null) {
            longitude = 0.0;
        }
        switch (tzId) {
            case TimeZones.ApparentSolarTime.TIMEZONEID: return new TimeZones.ApparentSolarTime(longitude, tzId, calculator);
            case TimeZones.LocalMeanTime.TIMEZONEID: case TimeZones.SiderealTime.TZID_LMST: return new TimeZones.LocalMeanTime(longitude, tzId);
            case TimeZones.SiderealTime.TZID_GMST: return new TimeZones.LocalMeanTime(0, tzId);
            case TZID_SYSTEM: case TZID_SUNTIMES: return TimeZone.getDefault();
            default: return TimeZone.getTimeZone(tzId);
        }
    }

    public static String getTimeZoneDisplay(Context context, TimeZone tz) {
        if (tz != null)
        {
            switch (tz.getID())
            {
                case TimeZones.LocalMeanTime.TIMEZONEID: return context.getString(R.string.time_localMean);
                case TimeZones.ApparentSolarTime.TIMEZONEID: return context.getString(R.string.time_apparent);
                default: return tz.getID();
            }
        } else return "";
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static int menuItemForTimeZone(@Nullable String tzId)
    {
        if (tzId != null) {
            switch (tzId) {
                case TimeZones.ApparentSolarTime.TIMEZONEID: return R.id.tz_item_apparentsolar;
                case TimeZones.LocalMeanTime.TIMEZONEID: return R.id.tz_item_localmean;
                case TimeZones.SiderealTime.TZID_LMST: return R.id.tz_item_lmst;
                case TimeZones.SiderealTime.TZID_GMST: return R.id.tz_item_gmst;
                case TZID_SUNTIMES: return R.id.tz_item_suntimes;
                case TZID_SYSTEM: return R.id.tz_item_system;
                case TZID_UTC: default: return R.id.tz_item_utc;
            }
        } else return R.id.tz_item_utc;
    }
    public static String timeZoneForMenuItem(int itemId)
    {
        switch (itemId) {
            case R.id.tz_item_apparentsolar:  return TimeZones.ApparentSolarTime.TIMEZONEID;
            case R.id.tz_item_localmean: return TimeZones.LocalMeanTime.TIMEZONEID;
            case R.id.tz_item_lmst:  return TimeZones.SiderealTime.TZID_LMST;
            case R.id.tz_item_gmst:  return TimeZones.SiderealTime.TZID_GMST;
            case R.id.tz_item_suntimes: return TZID_SUNTIMES;
            case R.id.tz_item_system: return TZID_SYSTEM;
            case R.id.tz_item_utc: return TZID_UTC;
            default: return null;
        }
    }

    public static void updateTimeZoneMenu(Menu menu, @Nullable String tzId)
    {
        MenuItem tzItem = menu.findItem(WidgetTimezones.menuItemForTimeZone(tzId));
        if (tzItem != null) {
            tzItem.setChecked(true);
        }
    }

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

    public static TimeZone localMeanTime(Location location) {
        return new TimeZones.LocalMeanTime(location.getLongitudeAsDouble(), TimeZones.LocalMeanTime.TIMEZONEID);
    }

    ///////////////////////////////////////
    ///////////////////////////////////////

    public static class TimeZoneItem
    {
        private final String timeZoneID;
        private final String displayString;
        private final double offsetHr, rawOffsetHr;

        public TimeZoneItem(String timeZoneID, String displayString, double offsetHr)
        {
            this.timeZoneID = timeZoneID;
            this.displayString = displayString;
            this.offsetHr = this.rawOffsetHr = offsetHr;
        }

        public TimeZoneItem(String timeZoneID, String displayString, double offsetHr, double rawOffsetHr)
        {
            this.timeZoneID = timeZoneID;
            this.displayString = displayString;
            this.offsetHr = offsetHr;
            this.rawOffsetHr = rawOffsetHr;
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
            return offsetHr;   // offset today
        }

        public String getOffsetString()
        {
            return offsetHr + "";
        }

        public double getRawOffsetHr()
        {
            return rawOffsetHr;
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
        private final int resID;

        public TimeZoneItemAdapter(Context context)
        {
            super(context, R.layout.layout_listitem_timezone);
            this.resID = R.layout.layout_listitem_timezone;
            init(context, R.string.timezoneCustom_line1, R.string.timezoneCustom_line2);
        }

        public TimeZoneItemAdapter(Context context, int resource)
        {
            super(context, resource);
            this.resID = resource;
            init(context, R.string.timezoneCustom_line1, R.string.timezoneCustom_line2);
        }

        public TimeZoneItemAdapter(Context context, int resource, List<TimeZoneItem> items, int resource_line1, int resource_line2)
        {
            super(context, resource, items);
            this.resID = resource;
            this.items = items;
            init(context, resource_line1, resource_line2);
        }

        public TimeZoneItemAdapter(Context context, int resource, List<TimeZoneItem> items, TimeZoneSort sortBy)
        {
            super(context, resource, items);
            this.resID = resource;
            this.items = items;
            this.sortBy = sortBy;
            init(context, R.string.timezoneCustom_line1, R.string.timezoneCustom_line2);
            sort();
        }

        private void init(Context context, int resource_line1, int resource_line2)
        {
            colors = context.getResources().getIntArray(R.array.utcOffsetColors);
            line1 = context.getString(resource_line1);
            line2 = context.getString(resource_line2);
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
            View view = layoutInflater.inflate(resID, parent, false);

            TimeZoneItem timezone = getItem(position);
            if (timezone == null)
            {
                Log.w("getItemView", "timezone at position " + position + " is null.");
                return view;
            }

            TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
            primaryText.setText(String.format(line1, timezone.getID()));

            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null) {
                secondaryText.setText(String.format(line2, timezone.getOffsetString(), timezone.getDisplayString()));
            }

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            if (icon != null)
            {
                if (colorize)
                {
                    GradientDrawable d = (GradientDrawable) icon.getBackground().mutate();
                    d.setColor(getColorForTimeZoneOffset(timezone.getRawOffsetHr()));
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

        public TimeZoneItem[] values() {
            return items.toArray(new TimeZoneItem[0]);
        }

        public TimeZoneItem[] findItems(double longitude)
        {
            ArrayList<TimeZoneItem> matches = new ArrayList<>();
            double lonOffsetHr = longitude * 24d / 360d;
            double nearest = Double.POSITIVE_INFINITY;
            for (TimeZoneItem item : items)
            {
                double d = Math.abs(lonOffsetHr - item.getRawOffsetHr());
                if (d <= nearest) {
                    if (d < nearest) {
                        nearest = d;
                        matches.clear();
                    }
                    matches.add(0, item);
                }
            }
            return matches.toArray(new TimeZoneItem[0]);
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
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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

    @SuppressWarnings("Convert2Diamond")
    public static class TimeZonesLoadTask extends AsyncTask<TimeZoneSort, Object, TimeZoneItemAdapter>
    {
        private final WeakReference<Context> contextRef;

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

            Date today = new Date();
            ArrayList<TimeZoneItem> timezones = new ArrayList<TimeZoneItem>();
            String[] allTimezoneValues = TimeZone.getAvailableIDs();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < allTimezoneValues.length; i++)
            {
                TimeZone timezone = TimeZone.getTimeZone(allTimezoneValues[i]);
                double rawOffsetHr = timezone.getRawOffset() / (double)(1000 * 60 * 60);
                double offsetHr = timezone.getOffset(today.getTime()) / (double)(1000 * 60 * 60);
                String displayName = timezone.getDisplayName(timezone.inDaylightTime(today), TimeZone.LONG, SuntimesUtils.getLocale());
                timezones.add(new TimeZoneItem(timezone.getID(), displayName, offsetHr, rawOffsetHr));
            }

            if (sortBy != null)
            {
                Collections.sort(timezones, sortBy.getComparator());
            }

            Context context = contextRef.get();
            if (context != null)
                return new WidgetTimezones.TimeZoneItemAdapter(context, R.layout.layout_listitem_timezone, timezones, sortBy);
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
