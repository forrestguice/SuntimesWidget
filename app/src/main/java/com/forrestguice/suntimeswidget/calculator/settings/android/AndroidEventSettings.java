package com.forrestguice.suntimeswidget.calculator.settings.android;

import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.events.EventSettingsInterface;
import com.forrestguice.suntimeswidget.events.EventType;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

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
        return EventSettings.suggestEventID(context);
    }

    @Override
    public String suggestEventLabel(EventType eventType) {
        return EventSettings.suggestEventLabel(context, eventType);
    }

    @Override
    public EventAlias saveEvent(EventType type, String id, String label, Integer color, String uri) {
        return EventSettings.saveEvent(context, type, id, label, color, uri);
    }

    @Override
    public void saveEvent(EventAlias event) {
        EventSettings.saveEvent(context, event);
    }

    @Override
    public Set<String> loadVisibleEvents() {
        return EventSettings.loadVisibleEvents(context);
    }

    @Override
    public Set<String> loadVisibleEvents(EventType... types) {
        return EventSettings.loadVisibleEvents(context, types);
    }

    @Override
    public Set<String> loadVisibleEvents(EventType type) {
        return EventSettings.loadVisibleEvents(context, type);
    }

    @Override
    public Set<String> loadEventList() {
        return EventSettings.loadEventList(context);
    }

    @Override
    public Set<String> loadEventList(EventType type) {
        return EventSettings.loadEventList(context, type);
    }

    @Override
    public String loadEventValue(String id, String key) {
        return EventSettings.loadEventValue(context, id, key);
    }

    @Override
    public boolean loadEventFlag(String id, String key) {
        return EventSettings.loadEventFlag(context, id, key);
    }

    @Override
    public void saveEventFlag(String id, String key, boolean value) {
        EventSettings.saveEventFlag(context, id, key, value);
    }

    @Override
    public EventAlias loadEvent(String id) {
        return EventSettings.loadEvent(context, id);
    }

    @Override
    public List<EventAlias> loadEvents(EventType type) {
        return EventSettings.loadEvents(context, type);
    }

    @Override
    public void deleteEvent(String id) {
        EventSettings.deleteEvent(context, id);
    }

    @Override
    public boolean hasEvent(String id) {
        return EventSettings.hasEvent(context, id);
    }

    @Override
    public boolean isShown(String id) {
        return EventSettings.isShown(context, id);
    }

    @Override
    public void setShown(String id, boolean value) {
        EventSettings.setShown(context, id, value);
    }

    @Override
    public String getEventUriLastPathSegment(String id) {
        return EventSettings.getEventUriLastPathSegment(context, id);
    }

    @Override
    public int getColor(String id) {
        return EventSettings.getColor(context, id);
    }

    @Override
    public EventType getType(String id) {
        return EventSettings.getType(context, id);
    }

    @Override
    public void deletePrefs() {
        EventSettings.deletePrefs(context);
    }

    @Override
    public void initDefaults() {
        EventSettings.initDefaults(context);
    }

    @Override
    public void initDisplayStrings() {
        EventSettings.initDisplayStrings(context);
    }
}
