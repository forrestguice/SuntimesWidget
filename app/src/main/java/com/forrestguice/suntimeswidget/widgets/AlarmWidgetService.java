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

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;

import java.util.ArrayList;
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
        protected List<AlarmClockItem> alarmList = new ArrayList<>();
        protected Context context = null;

        public AlarmWidgetItemViewFactory(Context context, Intent intent) {
            this.context = context;
        }

        protected void initData()
        {
            alarmList.clear();
            AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context);
            db.open();

            Long upcomingAlarmID = AlarmSettings.loadUpcomingAlarmId(context);    // TODO: load list
            if (upcomingAlarmID != null) {
                alarmList.add(AlarmDatabaseAdapter.AlarmItemTask.loadAlarmClockItem(context, db, AlarmSettings.loadUpcomingAlarmId(context)));
                alarmList.add(AlarmDatabaseAdapter.AlarmItemTask.loadAlarmClockItem(context, db, AlarmSettings.loadUpcomingAlarmId(context)));
                alarmList.add(AlarmDatabaseAdapter.AlarmItemTask.loadAlarmClockItem(context, db, AlarmSettings.loadUpcomingAlarmId(context)));
            }

            db.close();
        }

        @Override
        public void onCreate() {
            initData();
        }

        @Override
        public void onDataSetChanged() {
            initData();
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
            RemoteViews view = new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_1);
            view.setTextViewText(android.R.id.text1, alarmList.get(position).alarmtime + "");    // TODO: format
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
