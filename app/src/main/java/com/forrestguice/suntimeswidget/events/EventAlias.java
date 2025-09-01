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
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.alarmclock.AlarmAddon;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.views.ExecutorUtils;

import java.util.concurrent.Callable;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;

/**
 * EventAlias
 */
public final class EventAlias
{
    public EventAlias(@NonNull AlarmEventProvider.EventType type, @NonNull String id, @Nullable String label, @Nullable Integer color, @Nullable String uri, boolean shown)
    {
        this.type = type;
        this.id = id;
        this.label = (label != null ? label : id);
        this.color = (color != null ? color : EventSettings.PREF_DEF_EVENT_COLOR);
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

    public EventAlias(ContentValues values )
    {
        this.type = AlarmEventProvider.EventType.valueOf(values.getAsString(EventSettings.PREF_KEY_EVENT_TYPE));
        this.id = values.getAsString(EventSettings.PREF_KEY_EVENT_ID);
        this.label = values.getAsString(EventSettings.PREF_KEY_EVENT_LABEL);
        this.color = values.getAsInteger(EventSettings.PREF_KEY_EVENT_COLOR);
        this.uri = values.getAsString(EventSettings.PREF_KEY_EVENT_URI);
        this.shown = values.getAsBoolean(EventSettings.PREF_KEY_EVENT_SHOWN);
        this.summary = null;
    }

    public ContentValues toContentValues()
    {
        ContentValues values = new ContentValues();
        values.put(EventSettings.PREF_KEY_EVENT_TYPE, this.type.name());
        values.put(EventSettings.PREF_KEY_EVENT_ID, this.id);
        values.put(EventSettings.PREF_KEY_EVENT_LABEL, this.label);
        values.put(EventSettings.PREF_KEY_EVENT_COLOR, this.color);
        values.put(EventSettings.PREF_KEY_EVENT_URI, this.uri);
        values.put(EventSettings.PREF_KEY_EVENT_SHOWN, this.shown);
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
    public String getSummary(final Context context) {
        if (summary == null) {
            summary = ExecutorUtils.getResult("getSummary", new Callable<String>()
            {
                public String call() {
                    return resolveSummary(context);
                }
            }, 1000);
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
                    int i = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_SUMMARY);
                    retValue = ((i >= 0) ? cursor.getString(i) : null);
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
