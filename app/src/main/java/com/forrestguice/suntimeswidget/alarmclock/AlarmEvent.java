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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import java.util.ArrayList;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.REPEAT_SUPPORT_BASIC;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.REPEAT_SUPPORT_DAILY;

@SuppressWarnings("Convert2Diamond")
public class AlarmEvent
{
    public static int supportsRepeating(@NonNull SolarEvents event) {
        return supportsRepeating(event.getType());
    }
    public static int supportsRepeating(int eventType) {
        return (eventType == SolarEvents.TYPE_MOONPHASE || eventType == SolarEvents.TYPE_SEASON) ? REPEAT_SUPPORT_BASIC : REPEAT_SUPPORT_DAILY;
    }

    public static boolean supportsOffsetDays(@NonNull SolarEvents event) {
        return supportsOffsetDays(event.getType());
    }
    public static boolean supportsOffsetDays(int eventType) {
        return (eventType == SolarEvents.TYPE_MOONPHASE || eventType == SolarEvents.TYPE_SEASON);
    }

    public static boolean requiresLocation(@NonNull SolarEvents event) {
        return requiresLocation(event.getType());
    }
    public static boolean requiresLocation(int eventType) {
        return (eventType != SolarEvents.TYPE_MOONPHASE);
    }

    public static String phrase(Context context, @NonNull SolarEvents event) {
        String[] values = context.getResources().getStringArray(R.array.solarevents_long1);
        return values[event.ordinal()];
    }
    public static String phraseGender(Context context, @NonNull SolarEvents event) {
        String[] values = context.getResources().getStringArray(R.array.solarevents_gender);
        return values[event.ordinal()];
    }
    public static int phraseQuantity(Context context, @NonNull SolarEvents event) {
        int[] values = context.getResources().getIntArray(R.array.solarevents_quantity);
        return values[event.ordinal()];
    }

    /**
     * AlarmEventPhrase
     */
    public static class AlarmEventPhrase
    {
        public AlarmEventPhrase(@NonNull String noun) {
            this.noun = noun;
        }
        public AlarmEventPhrase(@NonNull String noun, @Nullable String gender, int quantity) {
            this.noun = noun;
            this.gender = (gender != null) ? gender : "other";
            this.quantity = gender != null ? quantity : Math.max(1, quantity);
        }

        @NonNull
        public String getNoun() {
            return noun;
        }
        protected String noun;

        @NonNull
        public String getGender() {
            return gender;
        }
        protected String gender = "other";

        protected int quantity = 1;
        public int getQuantity() {
            return quantity;
        }
    }

    /**
     * AlarmEventItem
     * wraps SolarEvent or addon-alarm URI
     */
    public static class AlarmEventItem
    {
        protected SolarEvents event;
        protected String title = "", summary = null;
        protected AlarmEventPhrase phrase = null;
        protected String uri = null;
        protected boolean resolved = false;

        protected int supports_repeating = REPEAT_SUPPORT_DAILY;
        protected boolean supports_offset_days = false;
        protected boolean requires_location = true;

        public AlarmEventItem( @NonNull SolarEvents event ) {
            this.event = event;
            resolved = true;
        }

        public AlarmEventItem( @NonNull String authority, @NonNull String name, @Nullable ContentResolver resolver)
        {
            event = null;
            uri = AlarmAddon.getEventInfoUri(authority, name);
            resolved = AlarmAddon.queryDisplayStrings(this, resolver);
        }

        public AlarmEventItem( @Nullable String eventUri, @Nullable ContentResolver resolver)
        {
            event = SolarEvents.valueOf(eventUri, null);
            if (event == null) {
                uri = eventUri;
                title = eventUri != null ? Uri.parse(eventUri).getLastPathSegment() : "";
                resolved = AlarmAddon.queryDisplayStrings(this, resolver);
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

        @Nullable
        public AlarmEventPhrase getPhrase(Context context)
        {
            if (phrase == null) {
                phrase = (event != null ? new AlarmEventPhrase(phrase(context, event), phraseGender(context, event), phraseQuantity(context, event))
                                        : new AlarmEventPhrase(title));          // fallback; queryDisplayStrings is primary way of assigning phrase
            }
            return phrase;
        }

        public int getIcon() {
            return (event != null ? event.getIcon() : R.attr.icActionExtension);
        }

        public int supportsRepeating() {
            return (event != null ? AlarmEvent.supportsRepeating(event) : supports_repeating);
        }
        public boolean supportsOffsetDays() {
            return (event != null ? AlarmEvent.supportsOffsetDays(event) : supports_offset_days);
        }
        public boolean requiresLocation() {
            return (event != null ? AlarmEvent.requiresLocation(event) : requires_location);
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

        public boolean isResolved() {
            return resolved;
        }
    }

    /**
     * isValidEventID
     */
    public static boolean isValidEventID(Context context, String eventID)
    {
        if (eventID == null) {
            return true;
        } else {
            SolarEvents solarEvent = SolarEvents.valueOf(eventID, null);
            if (solarEvent != null) {
                return true;
            } else {
                return AlarmAddon.checkUriPermission(context, eventID);
            }
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

        public boolean removeItem(SolarEvents event) {
            return event != null && removeItem(event.name());
        }

        public boolean removeItem(String event)
        {
            for (AlarmEventItem item : items)
            {
                String eventID = item.getEventID();
                if (eventID != null && eventID.equals(event))
                {
                    items.remove(item);
                    notifyDataSetChanged();
                    return true;
                }
            }
            return false;
        }

        public boolean containsItem(String eventID) {
            return findItemPosition(eventID) >= 0;
        }

        public int findItemPosition(String eventID)
        {
            for (int i=0; i<items.size(); i++)
            {
                AlarmEventItem item = items.get(i);
                if (item.getEventID().equals(eventID)) {
                    return i;
                }
            }
            return -1;
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
            AlarmEventItem item = items.get(position);
            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.layout_listitem_solarevent, parent, false);
            }

            ImageView iconView = (ImageView) view.findViewById(android.R.id.icon1);   // retrieve icon
            int[] iconAttr = { items.get(position).getIcon() };
            TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
            int def = R.drawable.ic_moon_rise;
            int iconResource = typedArray.getResourceId(0, def);
            typedArray.recycle();

            SolarEvents event = item.getEvent();                                      // apply icon
            if (event != null) {
                SolarEvents.SolarEventsAdapter.adjustIcon(iconResource, iconView, event);
            } else {
                Resources resources = context.getResources();
                int s = (int)resources.getDimension(R.dimen.sunIconLarge_width);
                int[] iconDimen = new int[] {s,s};
                adjustIcon(iconResource, iconView, iconDimen, 8);
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);     // apply text
            if (textView != null) {
                textView.setText(item.getTitle());
            }

            TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
            if (textView2 != null) {
                textView2.setText(item.getSummary());
            }

            return view;
        }
    }

    public static void adjustIcon(int iconRes, ImageView iconView, int[] dimen, int marginDp)
    {
        Resources resources = iconView.getContext().getResources();
        ViewGroup.LayoutParams iconParams = iconView.getLayoutParams();
        iconParams.width = dimen[0];
        iconParams.height = dimen[1];

        if (iconParams instanceof ViewGroup.MarginLayoutParams)
        {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) iconParams;
            float vertMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, resources.getDisplayMetrics());
            float horizMargin = vertMargin / 2f;
            params.setMargins((int)horizMargin, (int)vertMargin, (int)horizMargin, (int)vertMargin);
        }

        iconView.setImageDrawable(null);
        iconView.setBackgroundResource(iconRes);
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
