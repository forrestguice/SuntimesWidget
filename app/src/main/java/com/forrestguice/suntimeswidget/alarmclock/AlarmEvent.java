/**
    Copyright (C) 2021 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import java.util.ArrayList;

@SuppressWarnings("Convert2Diamond")
public class AlarmEvent
{
    /**
     * AlarmEventItem
     * wraps SolarEvent or addon-alarm URI
     */
    public static class AlarmEventItem
    {
        protected SolarEvents event;
        protected String title = null, summary = null;
        protected String uri = null;

        public AlarmEventItem( @NonNull SolarEvents event ) {
            this.event = event;
        }

        public AlarmEventItem( @NonNull String eventUri, @NonNull ContentResolver resolver)
        {
            this.event = null;
            this.uri = eventUri;
            queryDisplayStrings(resolver);
        }

        private void queryDisplayStrings(@NonNull ContentResolver resolver)
        {
            Uri info_uri = Uri.parse(uri);
            String name = info_uri.getLastPathSegment();

            Cursor cursor = resolver.query(info_uri, AlarmAddon.QUERY_ALARM_INFO_PROJECTION, null, null, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                int i_title = cursor.getColumnIndex(AlarmAddon.COLUMN_ALARM_TITLE);
                int i_summary = cursor.getColumnIndex(AlarmAddon.COLUMN_ALARM_SUMMARY);
                this.title = (i_title >= 0) ? cursor.getString(i_title) : name;
                this.summary = (i_summary >= 0) ? cursor.getString(i_summary) : "";
                cursor.close();
            }
        }

        @NonNull
        public String getTitle() {
            return (event != null ? event.getLongDisplayString() : title);
        }

        @Nullable
        public String getSummary() {
            return (event != null ? "" : summary);
        }

        public int getIcon() {
            return (event != null ? event.getIcon() : 0);
        }

        public String toString() {
            return getTitle();
        }

        public String getEventID() {
            return (event != null ? event.name() : uri);
        }

        @Nullable
        public SolarEvents getEvent() {
            return event;
        }

        @Nullable
        public String getUri() {
            return uri;
        }
    }

    /**
     * AlarmEventAdapter
     */
    public static class AlarmEventAdapter extends ArrayAdapter<AlarmEventItem>
    {
        private final Context context;
        private final ArrayList<AlarmEventItem> items;

        public AlarmEventAdapter(Context context, ArrayList<AlarmEventItem> items)
        {
            super(context, R.layout.layout_listitem_solarevent, items);
            this.context = context;
            this.items = items;
        }

        public boolean removeItem(SolarEvents event)
        {
            for (AlarmEventItem item : items)
            {
                if (event == item.getEvent())
                {
                    items.remove(item);
                    notifyDataSetChanged();
                    return true;
                }
            }
            return false;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return itemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return itemView(position, convertView, parent);
        }

        private View itemView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.layout_listitem_solarevent, parent, false);
            }

            int[] iconAttr = { items.get(position).getIcon() };
            TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
            int def = R.drawable.ic_moon_rise;
            int iconResource = typedArray.getResourceId(0, def);
            typedArray.recycle();

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            TextView text = (TextView) view.findViewById(android.R.id.text1);

            AlarmEventItem item = items.get(position);
            SolarEvents event = item.getEvent();
            if (event != null) {
                SolarEvents.SolarEventsAdapter.adjustIcon(iconResource, icon, event);
            }  // TODO: icons for non SolarEvent enum
            text.setText(item.getTitle());
            return view;
        }
    }

    public static AlarmEventAdapter createAdapter(Context context)
    {
        SolarEvents.SolarEventsAdapter solarEventsAdapter = SolarEvents.createAdapter(context);
        ArrayList<AlarmEventItem> items = new ArrayList<>();
        for (SolarEvents event : solarEventsAdapter.getChoices()) {
            items.add(new AlarmEventItem(event));
        }
        return new AlarmEventAdapter(context, items);
    }
}
