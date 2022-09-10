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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmAddon;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;

public class EventSettings
{
    public static final String PREFS_EVENTS = "suntimes.events";
    public static final String PREF_PREFIX_KEY = WidgetSettings.PREF_PREFIX_KEY;
    public static final String PREF_PREFIX_KEY_EVENT = "_event_";

    public static final String PREF_KEY_EVENT_ID = "id";  //SuntimesEventsContract.COLUMN_ACTION_TYPE;  // TODO: contract class

    public static final String PREF_KEY_EVENT_URI = "uri"; // SuntimesEventContract.COLUMN_EVENT_URI;  // TODO: contract class
    public static String PREF_DEF_EVENT_URI = null;

    public static final String PREF_KEY_EVENT_TYPE = "type";  //SuntimesEventsContract.COLUMN_ACTION_TYPE;  // TODO: contract class
    public static final AlarmEventProvider.EventType PREF_DEF_EVENT_TYPE = AlarmEventProvider.EventType.SUN_ELEVATION;

    public static final String PREF_KEY_EVENT_LABEL = "label"; // SuntimesEventContract.COLUMN_EVENT_LABEL;  // TODO: contract class
    public static final String PREF_KEY_EVENT_COLOR = "color"; // SuntimesEventContract.COLUMN_EVENT_COLOR;  // TODO: contract class
    public static int PREF_DEF_EVENT_COLOR = Color.YELLOW;

    public static final String PREF_KEY_EVENT_SHOWN = "shown"; // SuntimesEventContract.COLUMN_EVENT_SHOWN;  // TODO: contract class
    public static final boolean PREF_DEF_EVENT_SHOWN = false;

    public static final String PREF_KEY_EVENT_LIST = "list";
    public static final String PREF_DEF_EVENT_ID = "CUSTOM";

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * EventAlias
     */
    public static final class EventAlias
    {
        public EventAlias(@NonNull AlarmEventProvider.EventType type, @NonNull String id, @Nullable String label, @Nullable Integer color, @Nullable String uri, boolean shown)
        {
            this.type = type;
            this.id = id;
            this.label = (label != null ? label : id);
            this.color = (color != null ? color : PREF_DEF_EVENT_COLOR);
            this.uri = uri;
            this.shown = shown;
            this.summary = null;
        }

        public EventAlias( EventAlias other )
        {
            this.type = other.type;
            this.id = other.id;
            this.label = other.label;
            this.color = other.color;
            this.uri = other.uri;
            this.shown = other.shown;
            this.summary = other.summary;
        }

        public EventAlias( ContentValues values )
        {
            this.type = AlarmEventProvider.EventType.valueOf(values.getAsString(PREF_KEY_EVENT_TYPE));
            this.id = values.getAsString(PREF_KEY_EVENT_ID);
            this.label = values.getAsString(PREF_KEY_EVENT_LABEL);
            this.color = values.getAsInteger(PREF_KEY_EVENT_COLOR);
            this.uri = values.getAsString(PREF_KEY_EVENT_URI);
            this.shown = values.getAsBoolean(PREF_KEY_EVENT_SHOWN);
            this.summary = null;
        }

        public ContentValues toContentValues()
        {
            ContentValues values = new ContentValues();
            values.put(PREF_KEY_EVENT_TYPE, this.type.name());
            values.put(PREF_KEY_EVENT_ID, this.id);
            values.put(PREF_KEY_EVENT_LABEL, this.label);
            values.put(PREF_KEY_EVENT_COLOR, this.color);
            values.put(PREF_KEY_EVENT_URI, this.uri);
            values.put(PREF_KEY_EVENT_SHOWN, this.shown);
            return values;
        }

        private final AlarmEventProvider.EventType type;
        public AlarmEventProvider.EventType getType() {
            return type;
        }

        private final String id;
        public String getID() {
            return id;
        }

        /**
         * @return a partial AlarmEvent uri (needs rising/setting suffix to be complete)
         */
        public String getUri() {
            return uri;
        }
        private final String uri;

        private boolean shown;
        public boolean isShown() {
            return shown;
        }

        private final String label;
        public String getLabel() {
            return label;
        }

        private final Integer color;
        public Integer getColor() {
            return color;
        }

        private String summary;
        public String getSummary(Context context) {
            if (summary == null) {
                summary = resolveSummary(context);
            }
            return summary;
        }
        protected String resolveSummary(Context context)
        {
            String retValue = null;
            String uri = getUri();
            if (uri != null && !uri.trim().isEmpty())
            {
                Cursor cursor = context.getContentResolver().query(Uri.parse(uri), new String[] { AlarmEventContract.COLUMN_EVENT_SUMMARY }, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                    if (!cursor.isAfterLast()) {
                        retValue = cursor.getString(cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_SUMMARY));
                    }
                    cursor.close();
                }
            }
            return retValue;
        }

        public String toString() {
            return label != null ? label : id;
        }

        public String getAliasUri() {
            return AlarmAddon.getEventInfoUri(AUTHORITY, getID());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static String suggestEventID(Context context)
    {
        String id;
        int i = 0;
        do {
            id = PREF_DEF_EVENT_ID + i;
            i++;
        } while (hasEvent(context, id));
        return id;
    }

    public static String suggestEventLabel(Context context, @Nullable String eventID) {
        return context.getString(R.string.editevent_dialog_label_suggested);
    }

    public static EventAlias saveEvent(Context context, @NonNull AlarmEventProvider.EventType type, @Nullable String id, @Nullable String label, @Nullable Integer color, @NonNull String uri)
    {
        if (id == null) {
            id = suggestEventID(context);
        }
        EventAlias alias = new EventAlias(type, id, label, color, uri, false);
        saveEvent(context, alias);
        return alias;
    }
    public static void saveEvent(Context context, EventAlias event)
    {
        String id = event.getID();
        AlarmEventProvider.EventType type = event.getType();

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        prefs.putString(prefs_prefix0 + PREF_KEY_EVENT_URI, event.getUri());
        prefs.putString(prefs_prefix0 + PREF_KEY_EVENT_TYPE, type.name());
        prefs.putString(prefs_prefix0 + PREF_KEY_EVENT_LABEL, event.getLabel());
        prefs.putInt(prefs_prefix0 + PREF_KEY_EVENT_COLOR, event.getColor());
        prefs.putBoolean(prefs_prefix0 + PREF_KEY_EVENT_SHOWN, event.isShown());
        prefs.apply();

        Set<String> eventList = loadEventList(context, type);
        eventList.add(id);
        putStringSet(prefs, PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LIST, eventList);
        prefs.apply();
    }

    public static Set<String> loadEventList(Context context, AlarmEventProvider.EventType type)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String listKey = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LIST;
        Set<String> eventList = getStringSet(prefs, listKey, null);
        return (eventList != null) ? new TreeSet<String>(eventList) : new TreeSet<String>();
    }

    public static String loadEventValue(Context context, @NonNull String id, @Nullable String key)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";

        if (key == null || key.isEmpty()) {
            return prefs.getString(prefs_prefix + PREF_KEY_EVENT_URI, PREF_DEF_EVENT_URI);

        } else {
            switch (key)
            {
                case PREF_KEY_EVENT_ID:
                    return id;

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

    public static boolean loadEventFlag(Context context, @NonNull String id, @NonNull String key)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";

        switch (key)
        {
            case PREF_KEY_EVENT_SHOWN:
                return prefs.getBoolean(prefs_prefix + PREF_KEY_EVENT_SHOWN, PREF_DEF_EVENT_SHOWN);
        }
        return false;
    }

    public static EventAlias loadEvent(Context context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        return new EventAlias(getType(context, id), id,
                loadEventValue(context, id, PREF_KEY_EVENT_LABEL),
                prefs.getInt(prefs_prefix + PREF_KEY_EVENT_COLOR, PREF_DEF_EVENT_COLOR),
                loadEventValue(context, id, PREF_KEY_EVENT_URI),
                loadEventFlag(context, id, PREF_KEY_EVENT_SHOWN));
    }

    public static List<EventAlias> loadEvents(Context context, AlarmEventProvider.EventType type)
    {
        Set<String> events = loadEventList(context, type);
        ArrayList<EventAlias> list = new ArrayList<>();
        for (String id : events) {
            list.add(loadEvent(context, id));
        }
        return list;
    }

    public static void deleteEvent(Context context, @NonNull String id)
    {
        AlarmEventProvider.EventType type = getType(context, id);

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_URI);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_TYPE);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_LABEL);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_COLOR);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_SHOWN);
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

    public static boolean isShown(Context context, @NonNull String id) {
        return loadEventFlag(context, id, PREF_KEY_EVENT_SHOWN);
    }

    public static String getEventUriLastPathSegment(Context context, @NonNull String id)
    {
        String eventUri = EventSettings.loadEventValue(context, id, EventSettings.PREF_KEY_EVENT_URI);
        return Uri.parse(eventUri).getLastPathSegment();
    }

    public static int getColor(Context context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        return prefs.getInt(prefs_prefix + PREF_KEY_EVENT_COLOR, PREF_DEF_EVENT_COLOR);
    }

    public static AlarmEventProvider.EventType getType(Context context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        String typeString = prefs.getString(prefs_prefix + PREF_KEY_EVENT_TYPE, PREF_DEF_EVENT_TYPE.name());
        try {
            return AlarmEventProvider.EventType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            Log.e("EventSettings", "getType: " + id + ": " + e);
            return PREF_DEF_EVENT_TYPE;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

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
        //EventType.initDisplayStrings(context);
    }

}
