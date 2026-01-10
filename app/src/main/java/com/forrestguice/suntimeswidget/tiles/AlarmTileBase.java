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

package com.forrestguice.suntimeswidget.tiles;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.ContextThemeWrapper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.AlarmWidget0;

import java.util.Calendar;
import java.util.TimeZone;

@TargetApi(24)
public class AlarmTileBase extends SuntimesTileBase
{
    public static final WidgetSettings.ActionMode DEF_ACTION_MODE = WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY;
    public static final ContentValues DEF_ACTION_VALUES =  WidgetActions.SuntimesAction.OPEN_ALARM_LIST.toContentValues();
    public static final boolean DEF_SHOW_LABELS = true;    // show alarm label and note as part of dialog

    protected static SuntimesUtils utils = new SuntimesUtils();

    public AlarmTileBase(@Nullable Activity activity) {
        super(activity);
    }

    @Override
    protected int appWidgetId() {
        return AlarmTileService.ALARMTILE_APPWIDGET_ID;
    }

    @Override
    public Intent getConfigIntent(Context context) {
        return getConfigIntent(context, appWidgetId(), AlarmTileConfigActivity.class);
    }

    @Override
    public Intent getLaunchIntent(Context context) {
        return getLaunchIntent(context, appWidgetId(), initData(context));
    }

    @Override
    @Nullable
    protected Intent getLockScreenIntent(Context context) {
        return new Intent(context, TileLockScreenActivity.class);
    }

    @Override
    protected void initDefaults(Context context)
    {
        super.initDefaults(context);
        WidgetSettings.saveShowLabelsPref(context, appWidgetId(), DEF_SHOW_LABELS);
        WidgetSettings.saveActionModePref(context, appWidgetId(), DEF_ACTION_MODE);
        WidgetActions.saveActionLaunchPref(context, DEF_ACTION_VALUES, appWidgetId());
    }

    @Nullable
    protected AlarmClockItem initAlarmItem(Context context)
    {
        Long rowID = AlarmSettings.loadUpcomingAlarmId(context);
        if (rowID != null && (alarmItem == null || alarmItem.rowID != rowID)) {
            alarmItem = AlarmWidget0.loadAlarmClockItem(context, rowID);
        }
        return alarmItem;
    }
    protected void clearAlarmItem() {
        alarmItem = null;
    }
    protected AlarmClockItem alarmItem = null;

    @Override
    public int updateTaskRateMs() {
        return UPDATE_RATE;
    }
    public static final int UPDATE_RATE = 12000;     // dialog update rate: 12s

    protected Drawable getDialogIcon(Context context)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));
        int[] attrs = { R.attr.text_primaryColor };
        TypedArray a = contextWrapper.obtainStyledAttributes(attrs);
        int color = a.getResourceId(0, R.color.text_primary_dark);
        a.recycle();

        AlarmClockItem alarm = initAlarmItem(context);
        Drawable d = ContextCompat.getDrawable(context, ((alarm != null) ? alarm.getIcon() : R.drawable.ic_action_alarms));
        d.setTint(ContextCompat.getColor(contextWrapper, color));
        return d;
    }

    protected SpannableStringBuilder formatDialogTitle(Context context)
    {
        SpannableStringBuilder title = new SpannableStringBuilder();
        AlarmClockItem item = initAlarmItem(context);
        if (item != null)
        {
            WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId());
            Calendar event = Calendar.getInstance(TimeZone.getDefault());
            event.setTimeInMillis(item.alarmtime);
            String timeString = utils.calendarTimeShortDisplayString(context, event, false, timeFormat).toString();
            SpannableString timeDisplay = SuntimesUtils.createBoldSpan(null, timeString, timeString);
            timeDisplay = SuntimesUtils.createRelativeSpan(timeDisplay, timeString, timeString, 1.25f);
            title.append(timeDisplay);

        } else {
            title.append(context.getString(R.string.configLabel_alarms_nextAlarm));
        }
        return title;
    }

    protected SpannableStringBuilder formatDialogMessage(Context context)
    {
        SpannableStringBuilder msg = new SpannableStringBuilder();
        AlarmClockItem item = initAlarmItem(context);
        if (item != null)
        {
            // formatted alarm time
            WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId());
            Calendar event = Calendar.getInstance(TimeZone.getDefault());
            event.setTimeInMillis(item.alarmtime);
            String timeString = utils.calendarTimeShortDisplayString(context, event, false, timeFormat).toString();

            // formatted "time until"
            long timeUntilMs = item.alarmtime - Calendar.getInstance().getTimeInMillis();
            String timeUntilString = utils.timeDeltaLongDisplayString(timeUntilMs);
            String timeUntilPhrase = context.getString(((timeUntilMs >= 0) ? R.string.hence : R.string.ago), timeUntilString);

            String eventDisplay = formatEventDisplay(context, item);    // formatted event label

            String dialogMessage = (eventDisplay != null ? context.getString(R.string.alarmtile_dialogmsg_format1, timeString, eventDisplay, timeUntilPhrase)
                                                         : context.getString(R.string.alarmtile_dialogmsg_format0, timeString, timeUntilPhrase));
            SpannableString dialogDisplay = SuntimesUtils.createBoldSpan(null, dialogMessage, timeString);
            dialogDisplay = SuntimesUtils.createBoldSpan(dialogDisplay, dialogMessage, timeUntilString);
            msg.append(dialogDisplay);

            // show alarm label note too
            if (WidgetSettings.loadShowLabelsPref(context, appWidgetId(), DEF_SHOW_LABELS))
            {
                SuntimesData data = AlarmNotifications.getData(context, item);
                data.calculate(context);

                if (item.label != null || item.note != null) {
                    msg.append("\n");
                }
                if (item.label != null && !item.label.isEmpty()) {
                    msg.append("\n");
                    msg.append(SuntimesUtils.createBoldSpan(null, item.label, item.label));
                }
                if (item.note != null) {
                    msg.append("\n");
                    msg.append(DataSubstitutions.displayStringForTitlePattern0(context, item.note, data));
                }
            }

        } else {
            msg.append(context.getString(R.string.alarmtile_dialogmsg_none));
        }
        return msg;
    }

    public static String formatEventDisplay(Context context, AlarmClockItem item)
    {
        String eventString = item.getEvent();
        AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(eventString, context.getContentResolver());
        String eventDisplay = (eventString != null) ? eventItem.getTitle() : null;
        if (item.offset != 0) {
            eventDisplay = (eventString != null) ? AlarmNotifications.formatOffsetMessage(context, item.offset, item.timestamp, eventItem)
                                                 : AlarmNotifications.formatOffsetMessage(context, item.offset, item.timestamp);
        }
        return eventDisplay;
    }

}
