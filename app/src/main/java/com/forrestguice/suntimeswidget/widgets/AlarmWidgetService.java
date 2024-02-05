/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.widgets;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * AlarmWidgetService
 */
public class AlarmWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AlarmWidgetItemViewFactory(this, intent);
    }

    /**
     * AlarmWidgetItemViewFactory
     */
    public static class AlarmWidgetItemViewFactory implements RemoteViewsService.RemoteViewsFactory
    {
        public static final String EXTRA_THEME = "theme";
        public static final String EXTRA_TIMEFORMATMODE = "timeformatmode";

        protected List<AlarmClockItem> alarmList = new ArrayList<>();
        protected Context context;
        protected SuntimesTheme theme = null;
        protected WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.TimeFormatMode.MODE_SYSTEM;
        protected SuntimesUtils utils = new SuntimesUtils();

        public AlarmWidgetItemViewFactory(Context context, Intent intent)
        {
            this.context = context;
            initLocale(context);
            initOptions(intent);
        }

        protected void initLocale(Context context) {
            SuntimesUtils.initDisplayStrings(context);
        }

        protected void initOptions(Intent intent)
        {
            if (intent != null)
            {
                ContentValues themeValues = intent.getParcelableExtra(EXTRA_THEME);
                if (themeValues != null) {
                    theme = new SuntimesTheme(themeValues);
                }

                String timeFormatModeString = intent.getStringExtra(EXTRA_TIMEFORMATMODE);
                if (timeFormatModeString != null)
                {
                    try {
                        timeFormat = WidgetSettings.TimeFormatMode.valueOf(timeFormatModeString);

                    } catch (IllegalArgumentException e) {
                        timeFormat = WidgetSettings.TimeFormatMode.MODE_SYSTEM;
                        Log.e("AlarmWidgetService", "init: failed to load TimeFormatMode! " + e);
                    }
                }
            }
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            initData();
        }

        protected void initData()
        {
            List<AlarmClockItem> items = new ArrayList<>();
            AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context);
            db.open();

            Cursor cursor = db.getAllAlarms(0, true, true);
            while (!cursor.isAfterLast())
            {
                ContentValues entryValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, entryValues);

                AlarmClockItem item = new AlarmClockItem(context, entryValues);
                if (!item.enabled) {
                    AlarmNotifications.updateAlarmTime(context, item);
                }

                items.add(item);
                cursor.moveToNext();
            }

            db.close();
            alarmList.clear();
            alarmList.addAll(items);
        }

        @Override
        public void onDestroy() {
            /* EMPTY */
        }

        @Override
        public int getCount() {
            return alarmList.size();
        }

        @Override
        public RemoteViews getViewAt(int position)
        {
            AlarmClockItem item = alarmList.get(position);
            RemoteViews view = new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_1);

            if (theme != null) {
                view.setTextColor(android.R.id.text1, theme.getTimeColor());
            }

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(item.alarmtime);
            CharSequence timeDisplay = utils.calendarTimeShortDisplayString(context, alarmTime, false, timeFormat).toString();
            view.setTextViewText(android.R.id.text1, timeDisplay);
            return view;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position)
        {
            AlarmClockItem item = alarmList.get(position);
            return (item != null ? item.rowID : -1);
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }

}
