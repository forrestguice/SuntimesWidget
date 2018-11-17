/**
    Copyright (C) 2014-2018 Forrest Guice
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
import android.content.res.Resources;
import android.content.res.TypedArray;

import android.support.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("Convert2Diamond")
public enum SolarEvents
{
    MORNING_ASTRONOMICAL("astronomical twilight", "morning astronomical twilight", R.attr.sunriseIconLarge, 0, true), // 0
    MORNING_NAUTICAL("nautical twilight", "morning nautical twilight", R.attr.sunriseIconLarge, 0, true),             // 1
    MORNING_BLUE8("blue hour", "morning blue hour", R.attr.sunriseIconLarge, 0, true),                                // 2
    MORNING_CIVIL("civil twilight", "morning civil twilight", R.attr.sunriseIconLarge, 0, true),                      // 3
    MORNING_BLUE4("blue hour", "morning blue hour", R.attr.sunriseIconLarge, 0, true),                                // 4
    SUNRISE("sunrise", "sunrise", R.attr.sunriseIconLarge, 0, true),                                                  // 5
    MORNING_GOLDEN("golden hour", "morning golden hour", R.attr.sunriseIconLarge, 0, true),                           // 6
    NOON("solar noon", "solar noon", R.attr.sunnoonIcon, 0, false),                                                    // 7
    EVENING_GOLDEN("golden hour", "evening golden hour", R.attr.sunsetIconLarge, 0, false),                            // 8
    SUNSET("sunset", "sunset", R.attr.sunsetIconLarge, 0, false),                                                      // 9
    EVENING_BLUE4("blue hour", "evening blue hour", R.attr.sunsetIconLarge, 0, false),                                 // 10
    EVENING_CIVIL("civil twilight", "evening civil twilight", R.attr.sunsetIconLarge, 0, false),                       // 11
    EVENING_BLUE8("blue hour", "evening blue hour", R.attr.sunsetIconLarge, 0, false),                                 // 12
    EVENING_NAUTICAL("nautical twilight", "evening nautical twilight", R.attr.sunsetIconLarge, 0, false),              // 13
    EVENING_ASTRONOMICAL("astronomical twilight", "evening astronomical twilight", R.attr.sunsetIconLarge, 0, false),  // 14
    MOONRISE("moonrise", "moonrise", R.attr.moonriseIcon, 1, true),                                               // 15
    MOONSET("moonset", "mooonset", R.attr.moonsetIcon, 1, false);                                                 // 16
                                                                                                        // .. R.array.solarevents_short/_long req same length/order

    private int iconResource;
    private String shortDisplayString, longDisplayString;
    public int type;
    public boolean rising;

    public static final int TYPE_SUN = 0;
    public static final int TYPE_MOON = 1;

    private SolarEvents(String shortDisplayString, String longDisplayString, int iconResource, int type, boolean rising)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
        this.iconResource = iconResource;
        this.type = type;
        this.rising = rising;
    }

    public String toString()
    {
        return longDisplayString;
    }

    public int getIcon()
    {
        return iconResource;
    }

    public int getType()
    {
        return type;
    }

    public boolean isRising()
    {
        return rising;
    }

    public String getShortDisplayString()
    {
        return shortDisplayString;
    }

    public String getLongDisplayString()
    {
        return longDisplayString;
    }

    public void setDisplayString(String shortDisplayString, String longDisplayString)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    public static void initDisplayStrings(Context context)
    {
        String[] modes_short = context.getResources().getStringArray(R.array.solarevents_short);
        String[] modes_long = context.getResources().getStringArray(R.array.solarevents_long);
        if (modes_long.length != modes_short.length)
        {
            Log.e("initDisplayStrings", "The size of solarevents_short and solarevents_long DOES NOT MATCH! locale: " + AppSettings.getLocale().toString());
            return;
        }

        SolarEvents[] values = values();
        if (modes_long.length != values.length)
        {
            Log.e("initDisplayStrings", "The size of solarevents_long and SolarEvents DOES NOT MATCH! locale: " + AppSettings.getLocale().toString());
            return;
        }

        for (int i = 0; i < values.length; i++)
        {
            values[i].setDisplayString(modes_short[i], modes_long[i]);
        }
    }

    public static SolarEvents valueOf(String value, SolarEvents defaultType)
    {
        SolarEvents retValue = defaultType;
        if (value != null)
        {
            try {
                retValue = SolarEvents.valueOf(value);

            } catch (IllegalArgumentException e) {
                Log.w("SolarEvents", "valueOf :: failed to load '" + value);
                retValue = defaultType;
            }
        }
        return retValue;
    }

    public static SolarEventsAdapter createAdapter(Context context)
    {
        ArrayList<SolarEvents> choices = new ArrayList<SolarEvents>();
        choices.addAll(Arrays.asList(SolarEvents.values()));
        return new SolarEventsAdapter(context, choices);
    }

    /**
     * ArrayAdapter that displays SolarEvents items (with icon) as list or dropdown.
     */
    public static class SolarEventsAdapter extends ArrayAdapter<SolarEvents>
    {
        private final Context context;
        private final ArrayList<SolarEvents> choices;
        private int resID_noonIcon;

        public SolarEventsAdapter(Context context, ArrayList<SolarEvents> choices)
        {
            super(context, R.layout.layout_listitem_solarevent, choices);
            this.context = context;
            this.choices = choices;

            int[] iconAttr = { R.attr.sunnoonIcon };
            TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
            resID_noonIcon = typedArray.getResourceId(0, R.drawable.ic_noon_large);
            typedArray.recycle();
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return alarmItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return alarmItemView(position, convertView, parent);
        }

        private View alarmItemView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.layout_listitem_solarevent, parent, false);
            }

            int[] iconAttr = { choices.get(position).getIcon() };
            TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
            int def = R.drawable.ic_moon_rise;
            int iconResource = typedArray.getResourceId(0, def);
            typedArray.recycle();

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            adjustIcon(iconResource, icon);

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(choices.get(position).getLongDisplayString());

            return view;
        }

        private void adjustIcon(int iconResource, ImageView icon)
        {
            Resources resources = icon.getContext().getResources();
            int iconWidth = (int)resources.getDimension(R.dimen.sunIconLarge_width);
            int iconHeight = (int)resources.getDimension(R.dimen.sunIconLarge_height);
            if (iconResource == resID_noonIcon)
            {
                //noinspection SuspiciousNameCombination
                iconHeight = iconWidth;
            }

            ViewGroup.LayoutParams iconParams = icon.getLayoutParams();
            iconParams.width = iconWidth;
            iconParams.height = iconHeight;

            icon.setImageDrawable(null);
            icon.setBackgroundResource(iconResource);
        }
    }

    /**
     * SolarEventField
     */
    public static class SolarEventField
    {
        public SolarEvents event = SolarEvents.NOON;
        public Boolean tomorrow = false;

        public SolarEventField(SolarEvents event, boolean tomorrow)
        {
            this.event = event;
            this.tomorrow = tomorrow;
        }

        public boolean equals(Object obj)
        {
            if (!(obj instanceof SolarEventField))
            {
                return false;

            } else {
                SolarEventField that = (SolarEventField)obj;
                return (this.event.equals(that.event) && (this.tomorrow == that.tomorrow));
            }
        }

        public int hashCode()
        {
            int hash = this.event.hashCode();
            hash = hash * 37 + (tomorrow ? 0 : 1);
            return hash;
        }

        public String toString()
        {
            return event + " " + (tomorrow ? "tomorrow" : "today");
        }
    }

    public WidgetSettings.TimeMode toTimeMode()
    {
        return toTimeMode(this);
    }

    /**
     * toTimeMode
     * @param event SolarEvents enum
     * @return a TimeMode (or null if not applicable)
     */
    public static WidgetSettings.TimeMode toTimeMode( SolarEvents event )
    {
        switch (event)
        {
            case MORNING_ASTRONOMICAL:
            case EVENING_ASTRONOMICAL: return WidgetSettings.TimeMode.ASTRONOMICAL;

            case MORNING_NAUTICAL:
            case EVENING_NAUTICAL: return WidgetSettings.TimeMode.NAUTICAL;

            case MORNING_BLUE8:
            case EVENING_BLUE8: return WidgetSettings.TimeMode.BLUE8;

            case MORNING_BLUE4:
            case EVENING_BLUE4: return WidgetSettings.TimeMode.BLUE4;

            case MORNING_CIVIL:
            case EVENING_CIVIL: return WidgetSettings.TimeMode.CIVIL;

            case MORNING_GOLDEN:
            case EVENING_GOLDEN: return WidgetSettings.TimeMode.GOLD;

            case NOON: return WidgetSettings.TimeMode.NOON;

            case SUNSET:
            case SUNRISE: return WidgetSettings.TimeMode.OFFICIAL;
        }
        return null;
    }
}
