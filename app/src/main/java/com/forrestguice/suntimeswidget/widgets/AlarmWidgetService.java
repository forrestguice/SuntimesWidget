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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemUri;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.tiles.AlarmTileBase;
import com.forrestguice.suntimeswidget.views.SpanUtils;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.util.android.AndroidResources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.MODE_2x2;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.MODE_3x2;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_TYPES;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_TYPES;

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
        public static final String EXTRA_LAYOUTMODE = "layoutMode";

        protected Context context;
        protected int appWidgetID = 0;
        protected String layoutMode = AlarmWidgetSettings.MODE_2x2;
        protected List<AlarmClockItem> alarmList = new ArrayList<>();
        protected static final TimeDateDisplay utils = new TimeDateDisplay();
        protected static final TimeDeltaDisplay delta_utils = new TimeDeltaDisplay();

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
                if (intent.hasExtra(EXTRA_LAYOUTMODE)) {
                    layoutMode = intent.getStringExtra(EXTRA_LAYOUTMODE);
                }
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
            boolean enabledFirst = AlarmSettings.loadPrefAlarmSortEnabledFirst(context);
            boolean enabledOnly = AlarmWidgetSettings.loadAlarmWidgetBool(context, appWidgetID, PREF_KEY_ALARMWIDGET_ENABLEDONLY, PREF_DEF_ALARMWIDGET_ENABLEDONLY);
            int sortOrder = AlarmWidgetSettings.loadAlarmWidgetInt(context, appWidgetID, PREF_KEY_ALARMWIDGET_SORTORDER, PREF_DEF_ALARMWIDGET_SORTORDER);
            Set<String> filterTypes = AlarmWidgetSettings.loadAlarmWidgetStringSet(context, appWidgetID, PREF_KEY_ALARMWIDGET_TYPES, PREF_DEF_ALARMWIDGET_TYPES);

            List<AlarmClockItem> items = new ArrayList<>();
            AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context);
            db.open();

            Cursor cursor = db.getAllAlarms(0, true, enabledOnly);
            if (cursor != null)
            {
                while (!cursor.isAfterLast())
                {
                    ContentValues entryValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, entryValues);

                    AlarmClockItem item = new AlarmClockItem(context, entryValues);
                    if (!item.enabled) {
                        AlarmNotifications.updateAlarmTime(context, item);
                    }

                    if (item.type != null && filterTypes.contains(item.type.name())) {
                        items.add(item);
                    }
                    cursor.moveToNext();
                }
                cursor.close();
            }
            db.close();

            alarmList.clear();
            alarmList.addAll(AlarmListDialog.AlarmListDialogAdapter.sortItems(items, sortOrder, enabledFirst));
        }

        @Override
        public int getCount() {
            return alarmList.size();
        }

        protected int getViewLayoutResID()
        {
            switch (layoutMode)
            {
                case MODE_3x2: return R.layout.layout_listitem_alarmwidget1;
                case MODE_2x2:
                default: return R.layout.layout_listitem_alarmwidget;
            }
        }

        @Override
        public RemoteViews getViewAt(int position)
        {
            AlarmClockItem item = alarmList.get(position);
            RemoteViews view = new RemoteViews(context.getPackageName(), getViewLayoutResID());

            SuntimesTheme theme = WidgetSettings.loadThemePref(context, appWidgetID);
            view.setTextColor(android.R.id.text1, theme.getTextColor());
            view.setTextColor(android.R.id.text2, theme.getTimeColor());

            String itemLabel = item.getLabel(item.getLabel(context));
            String eventDisplay = AlarmTileBase.formatEventDisplay(context, item);
            view.setTextViewText(android.R.id.text1, itemLabel);
            view.setTextViewText(R.id.text_event, eventDisplay);
            view.setViewVisibility(R.id.text_event, (eventDisplay != null && !eventDisplay.isEmpty() ? View.VISIBLE : View.GONE));

            long timeUntilMs = item.alarmtime - Calendar.getInstance().getTimeInMillis();
            String timeUntilDisplay = delta_utils.timeDeltaLongDisplayString(timeUntilMs, 0, false, true, false,false).getValue();
            //String timeUntilPhrase = context.getString(((timeUntilMs >= 0) ? R.string.hence : R.string.ago), timeUntilDisplay);
            view.setTextViewText(R.id.text_note, "~ " + timeUntilDisplay);  // TODO: i18n

            TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetID);
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(item.alarmtime);
            String timeDisplay = utils.calendarTimeShortDisplayString(AndroidResources.wrap(context), alarmTime, false, timeFormat).toString();
            view.setTextViewText(android.R.id.text2, (theme.getTimeBold() ? SpanUtils.createBoldSpan(null, timeDisplay, timeDisplay) : timeDisplay));

            boolean showIcon = AlarmWidgetSettings.loadAlarmWidgetBool(context, appWidgetID, PREF_KEY_ALARMWIDGET_SHOWICONS, PREF_DEF_ALARMWIDGET_SHOWICONS);
            Drawable icon = SuntimesUtils.tintDrawableCompat(ContextCompat.getDrawable(context.getResources(), item.getIcon(), null), theme.getTimeColor());
            view.setImageViewBitmap(android.R.id.icon1, SuntimesUtils.drawableToBitmap(context, icon, (int)theme.getTimeSizeSp(), (int)theme.getTimeSizeSp(), false));
            view.setViewVisibility(android.R.id.icon1, (showIcon ? View.VISIBLE : View.GONE));
            view.setViewVisibility(R.id.icon_layout, (showIcon ? View.VISIBLE : View.GONE));

            if (Build.VERSION.SDK_INT >= 16)
            {
                float textSizeSp = theme.getTextSizeSp();
                float timeSizeSp = theme.getTimeSizeSp();

                view.setTextViewTextSize(android.R.id.text1, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
                view.setTextViewTextSize(android.R.id.text2, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
                view.setTextViewTextSize(R.id.text_label, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
                view.setTextViewTextSize(R.id.text_event, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
                view.setTextViewTextSize(R.id.text_note, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
                view.setTextViewTextSize(R.id.text_note1, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            }

            Intent fillInIntent = new Intent();
            fillInIntent.setData(ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, item.rowID));
            fillInIntent.putExtra(AlarmClockActivity.EXTRA_SELECTED_ALARM, item.rowID);
            view.setOnClickFillInIntent(R.id.itemLayout, fillInIntent);

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
