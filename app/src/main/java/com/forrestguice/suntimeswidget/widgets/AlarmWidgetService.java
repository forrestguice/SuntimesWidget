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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemUri;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        public static final String EXTRA_APPWIDGETID = "appWidgetID";

        protected Context context;
        protected int appWidgetID = 0;
        protected List<AlarmClockItem> alarmList = new ArrayList<>();
        protected SuntimesUtils utils = new SuntimesUtils();

        public AlarmWidgetItemViewFactory(Context context, Intent intent)
        {
            this.context = context;
            init(context, intent);
        }

        protected void init(Context context, Intent intent)
        {
            SuntimesUtils.initDisplayStrings(context);
            if (intent != null) {
                appWidgetID = intent.getIntExtra(EXTRA_APPWIDGETID, 0);
            }
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public void onDataSetChanged() {
            initData();
        }

        protected void initData()
        {
            boolean enabledOnly = true;
            boolean enabledFirst = true;
            int sortOrder = AlarmSettings.SORT_BY_ALARMTIME;
            String[] types = new String[] { AlarmClockItem.AlarmType.ALARM.name(), AlarmClockItem.AlarmType.NOTIFICATION.name(), AlarmClockItem.AlarmType.NOTIFICATION1.name() };
            Set<String> filterTypes = new TreeSet<>(Arrays.asList(types));

            List<AlarmClockItem> items = new ArrayList<>();
            AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context);
            db.open();

            Cursor cursor = db.getAllAlarms(0, true, enabledOnly);
            while (!cursor.isAfterLast())
            {
                ContentValues entryValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, entryValues);

                AlarmClockItem item = new AlarmClockItem(context, entryValues);
                if (!item.enabled) {
                    AlarmNotifications.updateAlarmTime(context, item);
                }

                if (filterTypes.contains(item.type.name())) {
                    items.add(item);
                }
                cursor.moveToNext();
            }
            db.close();

            alarmList.clear();
            alarmList.addAll(AlarmListDialog.AlarmListDialogAdapter.sortItems(items, sortOrder, enabledFirst));
        }

        @Override
        public int getCount() {
            return alarmList.size();
        }

        protected int getViewLayoutID() {
            return android.R.layout.simple_list_item_1;
        }

        @Override
        public RemoteViews getViewAt(int position)
        {
            AlarmClockItem item = alarmList.get(position);
            RemoteViews view = new RemoteViews(context.getPackageName(), getViewLayoutID());

            SuntimesTheme theme = WidgetSettings.loadThemePref(context, appWidgetID);
            view.setTextColor(android.R.id.text1, theme.getTimeColor());

            WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetID);
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(item.alarmtime);
            CharSequence timeDisplay = utils.calendarTimeShortDisplayString(context, alarmTime, false, timeFormat).toString();
            view.setTextViewText(android.R.id.text1, timeDisplay);

            Intent fillInIntent = new Intent();
            fillInIntent.setData(ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, item.rowID));
            fillInIntent.putExtra(AlarmClockActivity.EXTRA_SELECTED_ALARM, item.rowID);
            view.setOnClickFillInIntent(android.R.id.text1, fillInIntent);

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
