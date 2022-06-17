/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class EventSettings
{
    public static final String PREFS_EVENTS = "suntimes.events";
    public static final String PREF_PREFIX_KEY = WidgetSettings.PREF_PREFIX_KEY;
    public static final String PREF_PREFIX_KEY_EVENT = "_event_";

    public static final String PREF_KEY_EVENT_URI = "uri"; // SuntimesEventContract.COLUMN_EVENT_URI;  // TODO: contract class
    public static String PREF_DEF_EVENT_URI = null;

    public static final String PREF_KEY_EVENT_TYPE = "type";  //SuntimesEventsContract.COLUMN_ACTION_TYPE;  // TODO: contract class
    public static final EventType PREF_DEF_EVENT_TYPE = EventType.SUN_ELEVATION;

    public static final String PREF_KEY_EVENT_LABEL = "label"; // SuntimesEventContract.COLUMN_EVENT_LABEL;  // TODO: contract class
    public static final String PREF_KEY_EVENT_COLOR = "color"; // SuntimesEventContract.COLUMN_EVENT_COLOR;  // TODO: contract class
    public static int PREF_DEF_EVENT_COLOR = Color.YELLOW;

    public static final String PREF_KEY_EVENT_LIST = "list";
    public static final String PREF_DEF_EVENT_ID = "CUSTOM";

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * EventType
     */
    public static enum EventType
    {
        SUN_ELEVATION("Sun (elevation)");

        private EventType(String displayString)
        {
            this.displayString = displayString;
        }

        private String displayString;
        public String getDisplayString()
        {
            return displayString;
        }
        public void setDisplayString(String value)
        {
            displayString = value;
        }
        public static void initDisplayStrings(Context context) {
            SUN_ELEVATION.setDisplayString(context.getString(R.string.eventType_sun_elevation));
        }
        public String toString()
        {
            return displayString;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveEvent(Context context, @NonNull EventType type, @Nullable String id, @Nullable String label, @Nullable Integer color, @NonNull String uri)
    {
        boolean hasID = true;
        if (id == null) {
            int i = 0;
            do {
                id = PREF_DEF_EVENT_ID + i;
                i++;
            } while (hasEvent(context, id));
            hasID = false;
        }

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        prefs.putString(prefs_prefix0 + PREF_KEY_EVENT_URI, uri);
        prefs.putString(prefs_prefix0 + PREF_KEY_EVENT_TYPE, type.name());
        prefs.putString(prefs_prefix0 + PREF_KEY_EVENT_LABEL, (label != null ? label : id));
        prefs.putInt(prefs_prefix0 + PREF_KEY_EVENT_COLOR, (color != null ? color : PREF_DEF_EVENT_COLOR));
        prefs.apply();

        if (hasID)
        {
            Set<String> eventList = loadEventList(context, type);
            eventList.add(id);
            putStringSet(prefs, PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LIST, eventList);
            prefs.apply();
        }
    }

    public static Set<String> loadEventList(Context context, EventType type)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String listKey = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LIST;
        Set<String> eventList = getStringSet(prefs, listKey, null);
        return (eventList != null) ? new TreeSet<String>(eventList) : new TreeSet<String>();
    }
    public static String loadEvent(Context context, @NonNull String id, @Nullable String key)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";

        if (key == null || key.isEmpty()) {
            return prefs.getString(prefs_prefix + PREF_KEY_EVENT_URI, PREF_DEF_EVENT_URI);

        } else {
            switch (key)
            {
                case PREF_KEY_EVENT_TYPE:
                    return prefs.getString(prefs_prefix + PREF_KEY_EVENT_TYPE, PREF_DEF_EVENT_TYPE.name());

                case PREF_KEY_EVENT_LABEL:
                    return prefs.getString(prefs_prefix + PREF_KEY_EVENT_LABEL, id);

                case PREF_KEY_EVENT_COLOR:
                    return Integer.toString(prefs.getInt(prefs_prefix + PREF_KEY_EVENT_COLOR, PREF_DEF_EVENT_COLOR));

                case PREF_KEY_EVENT_URI:
                    return prefs.getString(prefs_prefix + PREF_KEY_EVENT_URI, null);
            }
            return null;
        }
    }

    public static void deleteEvent(Context context, @NonNull String id)
    {
        EventType type = getType(context, id);

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_URI);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_TYPE);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_LABEL);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_COLOR);
        prefs.apply();

        Set<String> eventList = loadEventList(context, type);
        eventList.remove(id);
        putStringSet(prefs, PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LIST, eventList);
        prefs.commit();
    }

    public static boolean hasEvent(Context context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        return prefs.contains(prefs_prefix + PREF_KEY_EVENT_TYPE);
    }

    public static EventType getType(Context context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        String typeString = prefs.getString(prefs_prefix + PREF_KEY_EVENT_TYPE, PREF_DEF_EVENT_TYPE.name());
        try {
            return EventType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            Log.e("EventSettings", "getType: " + id + ": " + e);
            return PREF_DEF_EVENT_TYPE;
        }
    }

    public static Set<String> getStringSet(SharedPreferences prefs, String key, @Nullable Set<String> defValues)    // TODO: needs test
    {
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getStringSet(key, defValues);
        } else {
            String s = prefs.getString(key, null);
            return (s != null) ? new TreeSet<>(Arrays.asList(s.split("\\|"))) : null;
        }
    }

    public static void putStringSet(SharedPreferences.Editor prefs, String key, @Nullable Set<String> values)  // TODO: needs test
    {
        if (Build.VERSION.SDK_INT >= 11) {
            prefs.putStringSet(key, values);
        } else {
            prefs.putString(key, stringSetToString(values));
        }
        prefs.apply();
    }
    public static String stringSetToString(@Nullable Set<String> values)
    {
        if (values != null) {
            StringBuilder s = new StringBuilder();
            for (String v : values) {
                s.append(v).append("|");
            }
            return s.toString();
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void deletePrefs(Context context)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        prefs.clear();
        prefs.apply();
    }

    public static void initDefaults(Context context) {
    }

    public static void initDisplayStrings( Context context ) {
        EventType.initDisplayStrings(context);
    }

}
