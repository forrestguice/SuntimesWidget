/**
    Copyright (C) 2021-2023 Forrest Guice
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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
import com.forrestguice.suntimeswidget.events.ElevationEvent;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.EventIcons;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.SolarEventsAdapter;
import com.forrestguice.suntimeswidget.views.ExecutorUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

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
        public static final long MAX_WAIT_MS = 1000;

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

        public AlarmEventItem( @NonNull String authority, @NonNull String name, @Nullable final ContentResolver resolver)
        {
            event = null;
            uri = EventUri.getEventInfoUri(authority, name);
            resolved = ExecutorUtils.runTask("AlarmEventItem", resolveItemTask(resolver), MAX_WAIT_MS);
        }

        public AlarmEventItem( @Nullable String eventUri, @Nullable final ContentResolver resolver)
        {
            event = SolarEvents.valueOf(eventUri, null);
            if (event == null) {
                uri = eventUri;
                title = eventUri != null ? Uri.parse(eventUri).getLastPathSegment() : "";
                resolved = ExecutorUtils.runTask("AlarmEventItem", resolveItemTask(resolver), MAX_WAIT_MS);
            }
        }

        private Callable<Boolean> resolveItemTask(@Nullable final ContentResolver resolver)
        {
            return new Callable<Boolean>() {
                public Boolean call() {
                    return AlarmAddon.queryDisplayStrings(AlarmEventItem.this, resolver);
                }
            };
        }

        @NonNull
        public String getTitle() {
            return (event != null ? event.getLongDisplayString() : title);
        }

        @Nullable
        public String getSummary() {
            return (event != null ? null : summary);
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

        public int getIcon(@Nullable Context context) {
            return (event != null)
                    ? EventIcons.getResID(context, event.getIcon(), R.attr.icActionExtension)
                    : EventIcons.getIconResID(context, EventIcons.getIconTag(context, uri));
        }

        public Integer getColor(@Nullable Context context) {
            if (event == null) {
                return EventIcons.getIconTint(context, EventIcons.getIconTag(context, uri));
            } else return null;
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
        public void setRequiresLocation(boolean value) {
            requires_location = value;
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

            int iconResource = item.getIcon(context);

            SolarEvents event = item.getEvent();                                      // apply icon
            if (event != null) {
                SolarEventsAdapter.adjustIcon(iconResource, iconView, event);

            } else {
                Resources resources = context.getResources();
                int dimen = (int)resources.getDimension(R.dimen.sunIconLarge_width);
                adjustIcon(context, iconResource, iconView, new int[] {dimen, dimen}, 8, item.getColor(context));
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);     // apply text
            if (textView != null) {
                textView.setText(item.getTitle());
            }

            TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
            if (textView2 != null) {
                String summary = item.getSummary();
                textView2.setText(summary);
                textView2.setVisibility(summary != null ? View.VISIBLE : View.GONE);
            }

            return view;
        }
    }

    public static void adjustIcon(Context context, int iconRes, ImageView iconView, int[] dimen, int marginDp, Integer color)
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
        if (color != null)
        {
            Drawable eventIcon = EventIcons.getIconDrawable(context, iconRes, dimen[0], dimen[1], EventIcons.getIconScale((String)null), EventIcons.getIconDrawableInset(context, (String)null), color);
            if (Build.VERSION.SDK_INT >= 16) {
                iconView.setBackground(eventIcon);
            } else {
                iconView.setBackgroundDrawable(eventIcon);
            }
        } else {
            iconView.setBackgroundResource(iconRes);
        }
    }

    public static AlarmEventAdapter createAdapter(Context context, boolean northward)
    {
        ArrayList<AlarmEventItem> items = new ArrayList<>();

        Set<String> customEvents = EventSettings.loadVisibleEvents(context);
        for (String eventID : customEvents)
        {
            EventAlias alias = EventSettings.loadEvent(context, eventID);
            items.add(new AlarmEventItem(alias.getAliasUri() + ElevationEvent.SUFFIX_RISING, context.getContentResolver()));
            items.add(new AlarmEventItem(alias.getAliasUri() + ElevationEvent.SUFFIX_SETTING, context.getContentResolver()));
        }

        SolarEventsAdapter solarEventsAdapter = SolarEventsAdapter.createAdapter(context, northward);
        for (SolarEvents event : solarEventsAdapter.getChoices()) {
            items.add(new AlarmEventItem(event));
        }
        return new AlarmEventAdapter(context, items);
    }
}
