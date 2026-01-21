/**
    Copyright (C) 2023 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.bedtime;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemUri;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmRepeatDialog;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.concurrent.ProgressListener;
import com.forrestguice.util.concurrent.SimpleProgressListener;

import java.util.Calendar;
import java.util.List;

public class BedtimeAlarmHelper
{
    public static void pauseBedtimeEvent(final Context context) {
        context.sendBroadcast( AlarmNotifications.getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_PAUSE) );
    }
    public static void resumeBedtimeEvent(final Context context) {
        context.sendBroadcast( AlarmNotifications.getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_RESUME) );
    }

    public static void dismissBedtimeEvent(final Context context)
    {
        final long rowID = BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_BEDTIME_NOTIFY);
        if (rowID != BedtimeSettings.ID_NONE)
        {
            BedtimeAlarmHelper.loadAlarmItem(context, rowID, new SimpleProgressListener<AlarmClockItem, List<AlarmClockItem>>()
            {
                @Override
                public void onFinished(List<AlarmClockItem> result)
                {
                    if (result != null && result.size() > 0)
                    {
                        AlarmClockItem item = result.get(0);
                        if (item != null)
                        {
                            boolean isSounding = (item.getState() == AlarmState.STATE_SOUNDING);
                            boolean hasBedtimeAction = WidgetActions.SuntimesAction.DISMISS_BEDTIME.name().equals(item.actionID1);

                            if (isSounding)
                            {
                                context.sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISMISS, item.getUri()));
                                if (!hasBedtimeAction) {
                                    context.sendBroadcast( AlarmNotifications.getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_DISMISS)) ;
                                }
                            } else {
                                context.sendBroadcast( AlarmNotifications.getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_DISMISS)) ;
                            }
                        } else {
                            context.sendBroadcast( AlarmNotifications.getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_DISMISS) );
                        }
                    } else {
                        context.sendBroadcast( AlarmNotifications.getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_DISMISS) );
                    }
                }
            });
        } else {
            context.sendBroadcast( AlarmNotifications.getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_DISMISS) );
        }
    }

    public static AlarmClockItem createBedtimeAlarmItem(final Context context, @Nullable final BedtimeItem item, int hour, int minute, long offset)
    {
        Location location = WidgetSettings.loadLocationPref(context, 0);
        AlarmClockItem alarmItem = AlarmListDialog.createAlarm(context, AlarmClockItem.AlarmType.ALARM,
                context.getString(R.string.configLabel_bedtime_alarm_wakeup),
                null, location, -1, hour, minute, null,
                AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, AlarmClockItem.AlarmType.ALARM), AlarmSettings.getDefaultRingtoneName(context, AlarmClockItem.AlarmType.ALARM), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);

        alarmItem.offset = offset;
        alarmItem.repeating = true;
        alarmItem.actionID1 = (BedtimeSettings.loadPrefBedtimeAlarmOff(context) ? WidgetActions.SuntimesAction.DISMISS_BEDTIME.name() : null);
        alarmItem.enabled = true;
        AlarmNotifications.updateAlarmTime(context, alarmItem);
        return alarmItem;
    }

    public static AlarmClockItem createBedtimeReminderItem(final Context context, @Nullable final BedtimeItem item, int hour, int minute, long offset)
    {
        Location location = WidgetSettings.loadLocationPref(context, 0);
        AlarmClockItem alarmItem = AlarmListDialog.createAlarm(context, AlarmClockItem.AlarmType.NOTIFICATION1,
                context.getString(R.string.configLabel_bedtime_alarm_reminder),
                null, location, -1, hour, minute, null,
                AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, AlarmClockItem.AlarmType.NOTIFICATION1), AlarmSettings.getDefaultRingtoneName(context, AlarmClockItem.AlarmType.NOTIFICATION1), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
        alarmItem.offset = offset;
        alarmItem.repeating = true;
        alarmItem.enabled = true;
        AlarmNotifications.updateAlarmTime(context, alarmItem);
        return alarmItem;
    }

    public static AlarmClockItem createBedtimeEventItem(final Context context, String slot, @Nullable final BedtimeItem item, int hour, int minute, long offset)
    {
        Location location = WidgetSettings.loadLocationPref(context, 0);
        AlarmClockItem alarmItem = AlarmListDialog.createAlarm(context, AlarmClockItem.AlarmType.NOTIFICATION1,
                context.getString(slot == null || slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY) ? R.string.configLabel_bedtime_alarm_notify : R.string.configLabel_bedtime_alarm_notify_off),
                null, location, -1, hour, minute, null,
                false, null, null, AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
        alarmItem.offset = offset;
        alarmItem.repeating = true;
        alarmItem.actionID0 = (slot == null || slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY))
                ? WidgetActions.SuntimesAction.TRIGGER_BEDTIME.name()
                : WidgetActions.SuntimesAction.DISMISS_BEDTIME.name();
        alarmItem.enabled = true;
        AlarmNotifications.updateAlarmTime(context, alarmItem);
        return alarmItem;
    }

    public static void loadAlarmItem(Context context, @Nullable Long rowId, ProgressListener<AlarmClockItem, List<AlarmClockItem>> taskListener)
    {
        if (rowId != null)
        {
            AlarmListDialog.AlarmListTask listTask = new AlarmListDialog.AlarmListTask(context, new Long[] { rowId });
            ExecutorUtils.runProgress("LoadAlarmTask", listTask, taskListener);
        }
    }
    public static void saveAlarmItem(Context context, @Nullable AlarmClockItem item, boolean addAlarm, @Nullable AlarmDatabaseAdapter.AlarmItemTaskListener taskListener)
    {
        if (item != null)
        {
            AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, addAlarm, false);
            if (taskListener != null) {
                task.setTaskListener(taskListener);
            }
            task.execute(item);
        }
    }

    public static void scheduleAlarmItem(@NonNull final Context context, AlarmClockItem item, boolean enabled)
    {
        context.sendBroadcast( enabled ? AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_RESCHEDULE, item.getUri())
                                       : AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISABLE, item.getUri()) );
    }

    public static void toggleAlarmItem(final Context context, @Nullable final AlarmClockItem item, final boolean enabled)
    {
        if (item != null)
        {
            item.alarmtime = 0;
            item.enabled = enabled;
            item.modified = true;

            BedtimeAlarmHelper.saveAlarmItem(context, item, false, new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item) {
                    if (result) {
                        BedtimeAlarmHelper.scheduleAlarmItem(context, item, enabled);
                    }
                }
            });
        }
    }

    public static void setBedtimeReminder_withReminderItem(final Context context, @Nullable final AlarmClockItem reminderItem, final boolean enabled)
    {
        long rowID = BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_BEDTIME_NOTIFY);
        if (rowID != BedtimeSettings.ID_NONE && enabled)
        {
            BedtimeAlarmHelper.loadAlarmItem(context, rowID, new SimpleProgressListener<AlarmClockItem, List<AlarmClockItem>>()
            {
                @Override
                public void onFinished(List<AlarmClockItem> result)
                {
                    AlarmClockItem bedtimeItem = ((result != null && result.size() > 0) ? result.get(0) : null);
                    BedtimeAlarmHelper.setBedtimeReminder(context, reminderItem, bedtimeItem, true);
                }
            });
        } else {
            BedtimeAlarmHelper.setBedtimeReminder(context, reminderItem, null, enabled);
        }
    }

    public static void setBedtimeReminder_withEventInfo(final Context context, final int hour, final int minute, final long offset, Location location, String flags, final boolean enabled)
    {
        //AlarmClockItem eventItem = (enabled ? BedtimeAlarmHelper.createBedtimeEventItem(context, null, hour, minute, offset) : null);   // to also clear reminder when disabled
        AlarmClockItem eventItem = BedtimeAlarmHelper.createBedtimeEventItem(context, null, null, hour, minute, offset);
        eventItem.location = location;
        eventItem.setAlarmFlags(flags);
        setBedtimeReminder_withEventItem(context, eventItem, enabled);
    }

    public static void setBedtimeReminder_withEventInfo(final Context context, String event, Location location, final long offset, String flags, final boolean enabled)
    {
        AlarmClockItem eventItem = BedtimeAlarmHelper.createBedtimeEventItem(context, null, null, -1, -1, offset);
        eventItem.setEvent(event);
        eventItem.location = location;
        eventItem.setAlarmFlags(flags);
        setBedtimeReminder_withEventItem(context, eventItem, enabled);
    }

    public static void setBedtimeReminder_withEventItem(final Context context, @Nullable final AlarmClockItem eventItem, final boolean enabled)
    {
        long rowID = BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_BEDTIME_REMINDER);
        if (rowID != BedtimeSettings.ID_NONE)
        {
            BedtimeAlarmHelper.loadAlarmItem(context, rowID, new SimpleProgressListener<AlarmClockItem, List<AlarmClockItem>>()
            {
                @Override
                public void onFinished(List<AlarmClockItem> result)
                {
                    AlarmClockItem reminderItem = ((result != null && result.size() > 0) ? result.get(0) : null);
                    BedtimeAlarmHelper.setBedtimeReminder(context, reminderItem, eventItem, enabled);
                }
            });
        } else if (BedtimeSettings.loadPrefBedtimeReminder(context)) {
            BedtimeAlarmHelper.setBedtimeReminder(context, null, eventItem, enabled);
        }
    }

    public static void setBedtimeReminder(final Context context, @Nullable AlarmClockItem reminderItem, @Nullable AlarmClockItem eventItem, final boolean enabled)
    {
        if (eventItem != null)
        {
            long reminderOffset = BedtimeSettings.loadPrefBedtimeReminderOffset(context);
            final boolean addReminder = (reminderItem == null);
            long existingOffset = (reminderItem != null ? reminderItem.offset : 0);

            if (reminderItem == null) {
                Log.d("DEBUG", "creating new reminder item");
                reminderItem = BedtimeAlarmHelper.createBedtimeReminderItem(context, null, eventItem.hour, eventItem.minute, eventItem.offset + reminderOffset);
            } else Log.d("DEBUG", "existing reminder item");

            if (eventItem.getEvent() != null)
            {
                reminderItem.setEvent(eventItem.getEvent());
                reminderItem.location = eventItem.location;
                reminderItem.hour = -1;
                reminderItem.minute = -1;
                reminderItem.offset = (eventItem.offset + reminderOffset);

            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(eventItem.timestamp + eventItem.offset);
                reminderItem.hour = calendar.get(Calendar.HOUR_OF_DAY);
                reminderItem.minute = calendar.get(Calendar.MINUTE);
                reminderItem.offset = (existingOffset != 0) ? existingOffset : reminderOffset;
            }

            reminderItem.setAlarmFlags(eventItem.getAlarmFlags());
            reminderItem.alarmtime = 0;
            reminderItem.enabled = enabled;
            reminderItem.modified = true;

            BedtimeAlarmHelper.saveAlarmItem(context, reminderItem, addReminder, new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    Log.d("DEBUG", "saved reminder item " + item.rowID + ": " + result);
                    if (result) {
                        BedtimeSettings.saveAlarmID(context, BedtimeSettings.SLOT_BEDTIME_REMINDER, item.rowID);
                        BedtimeAlarmHelper.scheduleAlarmItem(context, item, enabled);
                    }
                }
            });

        } else {
            clearBedtimeItem(context, BedtimeSettings.SLOT_BEDTIME_REMINDER);
        }
    }

    public static void clearBedtimeItems(Context context) {
        for (String slot : BedtimeSettings.ALL_SLOTS) {
            clearBedtimeItem(context, slot);
        }
        BedtimeSettings.clearAutomaticZenRules(context);
    }

    public static void clearBedtimeItem(Context context, String slotName)
    {
        long rowID = BedtimeSettings.loadAlarmID(context, slotName);
        clearBedtimeItem(context, slotName, rowID);
    }

    public static void clearBedtimeItem(Context context, String slotName, long rowID)
    {
        if (rowID != BedtimeSettings.ID_NONE)
        {
            Log.d("DEBUG", "deleting existing bedtime item " + rowID);
            Uri uri = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, rowID);
            BedtimeSettings.clearAlarmID(context, slotName);
            context.sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DELETE, uri));
        }
    }


}
