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
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.Log;

import java.util.concurrent.Callable;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;

public final class EventAlias
{
    public EventAlias(@NonNull EventType type, @NonNull String id, @Nullable String label, @Nullable Integer color, @Nullable String uri, boolean shown)
    {
        this.type = type;
        this.id = id;
        this.label = (label != null ? label : id);
        this.color = (color != null ? color : EventSettingsInterface.PREF_DEF_EVENT_COLOR);
        this.uri = uri;
        this.shown = shown;
        this.summary = null;
    }

    public EventAlias(EventAlias other )
    {
        this.type = other.type;
        this.id = other.id;
        this.label = other.label;
        this.color = other.color;
        this.uri = other.uri;
        this.shown = other.shown;
        this.summary = other.summary;
    }

    private final EventType type;
    public EventType getType() {
        return type;
    }

    private final String id;
    @NonNull
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

    private final boolean shown;
    public boolean isShown() {
        return shown;
    }

    private final String label;
    @NonNull
    public String getLabel() {
        return label;
    }

    private final Integer color;
    public Integer getColor() {
        return color;
    }

    private String summary;
    public String getSummary(final Object context)
    {
        if (summary == null) {
            if (resolver != null) {
                summary = ExecutorUtils.getResult("getSummary", new Callable<String>()
                {
                    public String call() {
                        return resolver.resolveSummary(context, EventAlias.this);
                    }
                }, 1000);

            } else {
                Log.e("EventAlias.getSummary", "ItemResolver is null! make sure `initItemResolver` is called on application start.");
            }
        }
        return summary;
    }

    @NonNull
    public String toString() {
        return label != null ? label : id;
    }

    public String getAliasUri() {
        return EventUri.getEventInfoUri(EventUri.AUTHORITY(), getID());
    }

    private static EventItemResolver resolver = null;
    public static void initItemResolver(EventItemResolver value) {
        resolver = value;
    }
    public static EventItemResolver getItemResolver() {
        return resolver;
    }
}
