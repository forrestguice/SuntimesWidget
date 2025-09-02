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

import java.util.List;
import java.util.Set;

public interface EventSettingsInterface
{
    String PREFS_EVENTS = "suntimes.events";
    String PREF_PREFIX_KEY_EVENT = "_event_";

    String PREF_KEY_EVENT_ID = "id";  //SuntimesEventsContract.COLUMN_ACTION_TYPE;  // TODO: contract class

    String PREF_KEY_EVENT_URI = "uri"; // SuntimesEventContract.COLUMN_EVENT_URI;  // TODO: contract class
    String PREF_DEF_EVENT_URI = null;

    String PREF_KEY_EVENT_TYPE = "type";  //SuntimesEventsContract.COLUMN_ACTION_TYPE;  // TODO: contract class
    EventType PREF_DEF_EVENT_TYPE = EventType.SUN_ELEVATION;

    String PREF_KEY_EVENT_LABEL = "label"; // SuntimesEventContract.COLUMN_EVENT_LABEL;  // TODO: contract class
    String PREF_KEY_EVENT_COLOR = "color"; // SuntimesEventContract.COLUMN_EVENT_COLOR;  // TODO: contract class
    int PREF_DEF_EVENT_COLOR = 0;

    String PREF_KEY_EVENT_SHOWN = "shown"; // SuntimesEventContract.COLUMN_EVENT_SHOWN;  // TODO: contract class
    boolean PREF_DEF_EVENT_SHOWN = false;

    String PREF_KEY_EVENT_LIST = "list";
    String PREF_KEY_EVENT_LISTSHOWN = "list_shown";

    String PREF_DEF_EVENT_ID = "CUSTOM";

    /////////////////////////////////////////////////////

    String prefixKey();

    String suggestEventID();
    String suggestEventLabel(EventType eventType);
    EventAlias saveEvent(@NonNull EventType type, @Nullable String id, @Nullable String label, @Nullable Integer color, @NonNull String uri);
    void saveEvent(EventAlias event);

    Set<String> loadVisibleEvents();
    Set<String> loadVisibleEvents(EventType... types);
    Set<String> loadVisibleEvents(EventType type);
    Set<String> loadEventList();
    Set<String> loadEventList(EventType type);
    String loadEventValue(@NonNull String id, @Nullable String key);
    boolean loadEventFlag(@NonNull String id, @NonNull String key);
    void saveEventFlag(@NonNull String id, @NonNull String key, boolean value);
    EventAlias loadEvent(@NonNull String id);
    List<EventAlias> loadEvents(EventType type);

    void deleteEvent(@NonNull String id);

    boolean hasEvent(@NonNull String id);

    boolean isShown(@NonNull String id);
    void setShown(@NonNull String id, boolean value);

    String getEventUriLastPathSegment(@NonNull String id);

    int getColor(@NonNull String id);
    EventType getType(@NonNull String id);
    void deletePrefs();
    void initDefaults();
    void initDisplayStrings();
}
