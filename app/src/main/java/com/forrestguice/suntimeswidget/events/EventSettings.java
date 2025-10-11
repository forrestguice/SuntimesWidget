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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.ContextInterface;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.util.Resources;
import com.forrestguice.util.UriUtils;
import com.forrestguice.util.prefs.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREFS_EVENTS;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_DEF_EVENT_COLOR;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_DEF_EVENT_ID;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_DEF_EVENT_SHOWN;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_DEF_EVENT_TYPE;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_DEF_EVENT_URI;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_COLOR;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_ID;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_LABEL;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_LIST;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_LISTSHOWN;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_SHOWN;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_TYPE;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_KEY_EVENT_URI;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_PREFIX_KEY;
import static com.forrestguice.suntimeswidget.events.EventSettingsInterface.PREF_PREFIX_KEY_EVENT;

public class EventSettings
{
    public static String suggestEventID(@NonNull ContextInterface context)
    {
        String id;
        int i = 0;
        do {
            id = PREF_DEF_EVENT_ID + i;
            i++;
        } while (hasEvent(context, id));
        return id;
    }

    public static String suggestEventLabel(@NonNull ContextInterface context, EventType eventType)
    {
        switch (eventType) {
            case DAYPERCENT: return context.getString(R.string.editevent_dialog_label_suggested2);
            case SHADOWLENGTH: return context.getString(R.string.editevent_dialog_label_suggested1);
            case SUN_ELEVATION:
            default: return context.getString(R.string.editevent_dialog_label_suggested);
        }
    }

    public static EventAlias saveEvent(@NonNull ContextInterface context, @NonNull EventType type, @Nullable String id, @Nullable String label, @Nullable Integer color, @NonNull String uri)
    {
        if (id == null) {
            id = suggestEventID(context);
        }
        EventAlias alias = new EventAlias(type, id, label, color, uri, false);
        saveEvent(context, alias);
        return alias;
    }
    public static void saveEvent(ContextInterface context, EventAlias event)
    {
        String id = event.getID();
        EventType type = event.getType();

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

        Set<String> eventList1 = loadVisibleEvents(context, type);
        if (event.isShown()) {
            eventList1.add(id);
        } else eventList1.remove(id);
        putStringSet(prefs, PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LISTSHOWN, eventList1);
        prefs.apply();
    }

    public static Set<String> loadVisibleEvents(ContextInterface context) {
        return loadVisibleEvents(context, EventType.visibleTypes());
    }
    public static Set<String> loadVisibleEvents(ContextInterface context, EventType... types)
    {
        Set<String> result = new TreeSet<>();
        for (EventType type : types) {
            result.addAll(loadVisibleEvents(context, type));
        }
        return result;
    }
    public static Set<String> loadVisibleEvents(ContextInterface context, EventType type)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String listKey = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LISTSHOWN;
        Set<String> eventList = getStringSet(prefs, listKey, null);
        return (eventList != null) ? new TreeSet<String>(eventList) : new TreeSet<String>();
    }

    public static Set<String> loadEventList(ContextInterface context)
    {
        Set<String> result = new TreeSet<>();
        for (EventType type : EventType.values()) {
            result.addAll(loadEventList(context, type));
        }
        return result;
    }
    public static Set<String> loadEventList(ContextInterface context, EventType type)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String listKey = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LIST;
        Set<String> eventList = getStringSet(prefs, listKey, null);
        return (eventList != null) ? new TreeSet<String>(eventList) : new TreeSet<String>();
    }

    public static String loadEventValue(ContextInterface context, @NonNull String id, @Nullable String key)
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

    public static boolean loadEventFlag(ContextInterface context, @NonNull String id, @NonNull String key)
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
    public static void saveEventFlag(ContextInterface context, @NonNull String id, @NonNull String key, boolean value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        prefs.putBoolean(prefs_prefix + key, value);
        prefs.apply();
    }

    public static EventAlias loadEvent(ContextInterface context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        return new EventAlias(getType(context, id), id,
                loadEventValue(context, id, PREF_KEY_EVENT_LABEL),
                prefs.getInt(prefs_prefix + PREF_KEY_EVENT_COLOR, PREF_DEF_EVENT_COLOR),
                loadEventValue(context, id, PREF_KEY_EVENT_URI),
                loadEventFlag(context, id, PREF_KEY_EVENT_SHOWN));
    }

    public static List<EventAlias> loadEvents(ContextInterface context, EventType type)
    {
        Set<String> events = loadEventList(context, type);
        ArrayList<EventAlias> list = new ArrayList<>();
        for (String id : events) {
            list.add(loadEvent(context, id));
        }
        return list;
    }

    public static void deleteEvent(ContextInterface context, @NonNull String id)
    {
        EventType type = getType(context, id);

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_URI);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_TYPE);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_LABEL);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_COLOR);
        prefs.remove(prefs_prefix0 + PREF_KEY_EVENT_SHOWN);
        prefs.apply();

        Set<String> eventList1 = loadVisibleEvents(context, type);
        eventList1.remove(id);
        putStringSet(prefs, PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LISTSHOWN, eventList1);
        prefs.apply();

        Set<String> eventList = loadEventList(context, type);
        eventList.remove(id);
        putStringSet(prefs, PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + type.name() + "_" + PREF_KEY_EVENT_LIST, eventList);
        prefs.commit();
    }

    public static boolean hasEvent(@NonNull ContextInterface context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        return prefs.contains(prefs_prefix + PREF_KEY_EVENT_TYPE);
    }

    public static boolean isShown(ContextInterface context, @NonNull String id) {
        return loadEventFlag(context, id, PREF_KEY_EVENT_SHOWN);
    }
    public static void setShown(ContextInterface context, @NonNull String id, boolean value)
    {
        saveEventFlag(context, id, PREF_KEY_EVENT_SHOWN, value);

        EventType eventType = getType(context, id);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        Set<String> eventList1 = loadVisibleEvents(context, eventType);
        if (value) {
            eventList1.add(id);
        } else eventList1.remove(id);
        putStringSet(prefs, PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + eventType.name() + "_" + PREF_KEY_EVENT_LISTSHOWN, eventList1);
        prefs.apply();
    }

    public static String getEventUriLastPathSegment(ContextInterface context, @NonNull String id)
    {
        String eventUri = EventSettings.loadEventValue(context, id, PREF_KEY_EVENT_URI);
        return UriUtils.getLastPathSegment(eventUri);
    }

    public static int getColor(ContextInterface context, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EVENTS, 0);
        String prefs_prefix = PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_EVENT + id + "_";
        return prefs.getInt(prefs_prefix + PREF_KEY_EVENT_COLOR, PREF_DEF_EVENT_COLOR);
    }

    public static EventType getType(ContextInterface context, @NonNull String id)
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static Set<String> getStringSet(SharedPreferences prefs, String key, @Nullable Set<String> defValues) {
        return prefs.getStringSet(key, defValues);
    }

    public static void putStringSet(SharedPreferences.Editor prefs, String key, @Nullable Set<String> values) {
        prefs.putStringSet(key, values);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void deletePrefs(ContextInterface context)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_EVENTS, 0).edit();
        prefs.clear();
        prefs.apply();
    }

    public static void initDefaults(ContextInterface context) {
    }

    public static void initDisplayStrings(Resources context) {
        //EventType.initDisplayStrings(context);
    }

}
