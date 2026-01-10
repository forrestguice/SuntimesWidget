package com.forrestguice.suntimeswidget.calculator.settings.android;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.events.EventSettingsInterface;
import com.forrestguice.suntimeswidget.events.EventType;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.util.Resources;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.prefs.SharedPreferences;

import java.util.List;
import java.util.Set;

public class AndroidEventSettings implements EventSettingsInterface
{
    private final android.content.Context context;
    public AndroidEventSettings(android.content.Context context) {
        this.context = context;
    }

    public static AndroidEventSettings wrap(android.content.Context context) {
        return new AndroidEventSettings(context);
    }

    @Override
    public String prefixKey() {
        return WidgetSettings.PREF_PREFIX_KEY;
    }

    @Override
    public String suggestEventID() {
        return EventSettings.suggestEventID(this);
    }

    @Override
    public String suggestEventLabel(EventType eventType) {
        return EventSettings.suggestEventLabel(this, eventType);
    }

    @Override
    public EventAlias saveEvent(@NonNull EventType type, String id, String label, Integer color, @NonNull String uri) {
        return EventSettings.saveEvent(this, type, id, label, color, uri);
    }

    @Override
    public void saveEvent(EventAlias event) {
        EventSettings.saveEvent(this, event);
    }

    @Override
    public Set<String> loadVisibleEvents() {
        return EventSettings.loadVisibleEvents(this);
    }

    @Override
    public Set<String> loadVisibleEvents(EventType... types) {
        return EventSettings.loadVisibleEvents(this, types);
    }

    @Override
    public Set<String> loadVisibleEvents(EventType type) {
        return EventSettings.loadVisibleEvents(this, type);
    }

    @Override
    public Set<String> loadEventList() {
        return EventSettings.loadEventList(this);
    }

    @Override
    public Set<String> loadEventList(EventType type) {
        return EventSettings.loadEventList(this, type);
    }

    @Override
    public String loadEventValue(@NonNull String id, String key) {
        return EventSettings.loadEventValue(this, id, key);
    }

    @Override
    public boolean loadEventFlag(@NonNull String id, @NonNull String key) {
        return EventSettings.loadEventFlag(this, id, key);
    }

    @Override
    public void saveEventFlag(@NonNull String id, @NonNull String key, boolean value) {
        EventSettings.saveEventFlag(this, id, key, value);
    }

    @Override
    public EventAlias loadEvent(@NonNull String id) {
        return EventSettings.loadEvent(this, id);
    }

    @Override
    public List<EventAlias> loadEvents(EventType type) {
        return EventSettings.loadEvents(this, type);
    }

    @Override
    public void deleteEvent(@NonNull String id) {
        EventSettings.deleteEvent(this, id);
    }

    @Override
    public boolean hasEvent(@NonNull String id) {
        return EventSettings.hasEvent(this, id);
    }

    @Override
    public boolean isShown(@NonNull String id) {
        return EventSettings.isShown(this, id);
    }

    @Override
    public void setShown(@NonNull String id, boolean value) {
        EventSettings.setShown(this, id, value);
    }

    @Override
    public String getEventUriLastPathSegment(@NonNull String id) {
        return EventSettings.getEventUriLastPathSegment(this, id);
    }

    @Override
    public int getColor(@NonNull String id) {
        return EventSettings.getColor(this, id);
    }

    @Override
    public EventType getType(@NonNull String id) {
        return EventSettings.getType(this, id);
    }

    @Override
    public void deletePrefs() {
        EventSettings.deletePrefs(this);
    }

    @Override
    public void initDefaults() {
        EventSettings.initDefaults(this);
    }

    @Override
    public void initDisplayStrings() {
        EventSettings.initDisplayStrings(getResources());
    }

    @Override
    public Resources getResources() {
        return AndroidResources.wrap(context);
    }

    @Override
    public String getString(int id) {
        return context.getString(id);
    }

    @Override
    public String getString(int id, Object... formatArgs) {
        return context.getString(id, formatArgs);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int flags) {
        return AndroidSharedPreferences.wrap(context.getSharedPreferences(name, flags));
    }
}
