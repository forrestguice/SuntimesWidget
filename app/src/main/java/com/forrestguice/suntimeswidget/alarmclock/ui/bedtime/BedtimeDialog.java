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
package com.forrestguice.suntimeswidget.alarmclock.ui.bedtime;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmCreateDialog;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmEditActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmRepeatDialog;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.Toast;

import java.util.Calendar;
import java.util.List;

@SuppressWarnings("Convert2Diamond")
public class BedtimeDialog extends DialogFragment
{
    public static final int REQUEST_EDIT_WAKEUP = 10;
    public static final int REQUEST_ADD_WAKEUP = 20;

    public static final int REQUEST_EDIT_BEDTIME = 30;
    public static final int REQUEST_ADD_BEDTIME = 40;

    protected RecyclerView list;
    protected BedtimeItemAdapter adapter;

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        AlarmSettings.setDefaultRingtoneUris(getActivity());
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));
        View content = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_bedtime, parent, false);

        adapter = new BedtimeItemAdapter(getActivity());
        adapter.setAdapterListener(adapterListener);

        list = (RecyclerView) content.findViewById(R.id.recyclerview);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        //list.addItemDecoration(itemDecoration);
        list.setAdapter(adapter);

        if (savedState != null) {
            loadSettings(savedState);
        }

        reloadAdapter();
        return content;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Bundle bundle) {
    }

    protected void saveSettings(Bundle bundle) {
    }

    @Override
    public void onResume()
    {
        super.onResume();
        restoreDialogs(getActivity());
        adapter.notifyDataSetChanged();
    }

    protected void restoreDialogs(Context context)
    {
        FragmentManager fragments = getChildFragmentManager();
        AlarmCreateDialog addAlarmDialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOG_ADD_ALARM);
        if (addAlarmDialog != null)
        {
            int position = adapter.findItemPosition(BedtimeItem.ItemType.WAKEUP_ALARM);
            BedtimeItem item = (position >= 0 && position < adapter.getItemCount()) ? adapter.getItem(position) : null;
            addAlarmDialog.setOnAcceptedListener(onAddAlarmDialogAccept(DIALOG_ADD_ALARM, BedtimeSettings.SLOT_WAKEUP_ALARM, item));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_ADD_WAKEUP:
                onEditAlarmResult(resultCode, data, true, BedtimeSettings.SLOT_BEDTIME_NOTIFY);
                break;

            case REQUEST_EDIT_WAKEUP:
                onEditAlarmResult(resultCode, data, false, BedtimeSettings.SLOT_BEDTIME_NOTIFY);
                break;

            case REQUEST_EDIT_BEDTIME:
                onEditAlarmResult(resultCode, data, false, BedtimeSettings.SLOT_WAKEUP_ALARM);
                if (resultCode == Activity.RESULT_OK && data != null) {
                    AlarmClockItem item = data.getParcelableExtra(AlarmEditActivity.EXTRA_ITEM);
                    BedtimeSettings.setAutomaticZenRule(getActivity(), item.enabled && BedtimeSettings.loadPrefBedtimeDoNotDisturb(getActivity()));
                }
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bedtime_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public RecyclerView getList() {
        return list;
    }

    public BedtimeItemAdapter getAdapter() {
        return adapter;
    }

    public void reloadAdapter() {
        adapter.initItems();
        adapter.notifyDataSetChanged();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected BedtimeItemAdapter.AdapterListener listener;
    protected BedtimeItemAdapter.AdapterListener adapterListener = new BedtimeItemAdapter.AdapterListener()
    {
        @Override
        public void onItemAction(BedtimeItem item)
        {
            if (item != null)
            {
                switch (item.getItemType())
                {
                    case BEDTIME_NOW:
                        triggerBedtimeNow(getActivity(), item);
                        break;

                    case WAKEUP_ALARM:
                        showAddAlarmDialog(getActivity(), item);
                        break;

                    case BEDTIME:  // TODO
                        Toast.makeText(getActivity(), "TODO3", Toast.LENGTH_SHORT).show();
                        break;

                    case BEDTIME_REMINDER:  // TODO
                        Toast.makeText(getActivity(), "TODO3", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        if (listener != null) {
                            listener.onItemAction(item);
                        }
                        break;
                }
            }
        }

        @Override
        public void onItemConfigure(BedtimeItem item)
        {
            if (item != null)
            {
                switch (item.getItemType())
                {
                    case SLEEP_CYCLE:
                        configureSleepCycle(getActivity(), item);
                        break;

                    case WAKEUP_ALARM:
                        showAlarmEditActivity(BedtimeSettings.loadAlarmID(getActivity(), BedtimeSettings.SLOT_WAKEUP_ALARM), null, REQUEST_EDIT_WAKEUP, false);
                        break;

                    case BEDTIME:
                        showAlarmEditActivity(BedtimeSettings.loadAlarmID(getActivity(), BedtimeSettings.SLOT_BEDTIME_NOTIFY), null, REQUEST_EDIT_BEDTIME, false);
                        break;

                    case BEDTIME_REMINDER:
                        Toast.makeText(getActivity(), "TODO", Toast.LENGTH_SHORT).show();    // TODO
                        break;

                    default:
                        if (listener != null) {
                            listener.onItemAction(item);
                        }
                        break;
                }
            }
        }
    };

    public void setAdapterListener(BedtimeItemAdapter.AdapterListener listener) {
        this.listener = listener;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configureSleepCycle(Context context, @Nullable BedtimeItem item)
    {
        Toast.makeText(context, "TODO1", Toast.LENGTH_SHORT).show();                        // TODO
        adapter.notifyItemChanged(adapter.findItemPosition(item));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void triggerBedtimeNow(final Context context, @Nullable final BedtimeItem item)
    {
        Toast.makeText(context, "triggering bedtime now..", Toast.LENGTH_SHORT).show();                        // TODO

        float numSleepCycles = BedtimeSettings.loadPrefSleepCycleCount(context);
        long sleepCycleMs = BedtimeSettings.loadPrefSleepCycleMs(context);
        long sleepTotalMs = (long)(numSleepCycles * sleepCycleMs);
        final long bedtime_offset = -sleepTotalMs + (1000 * 60);
        int sleepMinutes = (int)(sleepTotalMs / (1000 * 60));

        Calendar wakeup = Calendar.getInstance();
        wakeup.add(Calendar.MINUTE, sleepMinutes);
        final int wakeup_hour = wakeup.get(Calendar.HOUR_OF_DAY);
        final int wakeup_minute = wakeup.get(Calendar.MINUTE);

        /*Calendar bedtime = Calendar.getInstance();
        bedtime.setTimeInMillis(wakeup.getTimeInMillis());
        bedtime.add(Calendar.MINUTE, (-sleepMinutes) + 1);
        final int bedtime_hour = bedtime.get(Calendar.HOUR_OF_DAY);
        final int bedtime_minute = bedtime.get(Calendar.MINUTE);*/

        configureBedtimeAt(context, item, BedtimeSettings.SLOT_WAKEUP_ALARM, wakeup_hour, wakeup_minute, 0);
        configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFY, wakeup_hour, wakeup_minute, bedtime_offset);
        BedtimeSettings.setAutomaticZenRule(getActivity(), BedtimeSettings.loadPrefBedtimeDoNotDisturb(getActivity()));
    }

    protected void configureBedtimeAt(final Context context, @Nullable final BedtimeItem item, @NonNull final String slot, final int hour, final int minute, final long offset)
    {
        long alarmID = BedtimeSettings.loadAlarmID(getActivity(), slot);
        if (alarmID == BedtimeSettings.ID_NONE)
        {
            AlarmClockItem alarmItem = (slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY))
                    ? createBedtimeEventItem(context, item, hour, minute, offset) : createBedtimeAlarmItem(context, item, hour, minute, offset);
            scheduleBedtimeAlarmItem(context, slot, alarmItem, item, true);

        } else {
            loadAlarmItem(alarmID, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onLoadFinished(List<AlarmClockItem> result)
                {
                    AlarmClockItem alarmItem = ((result != null && result.size() > 0 && result.get(0) != null) ? result.get(0) : null);
                    boolean addAlarm = (alarmItem == null);
                    if (addAlarm) {
                        alarmItem = (slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY))
                                ? createBedtimeEventItem(context, item, hour, minute, offset) : createBedtimeAlarmItem(context, item, hour, minute, offset);
                    }
                    if (alarmItem.hour != hour || alarmItem.minute != minute || alarmItem.offset != offset) {
                        alarmItem.hour = hour;
                        alarmItem.minute = minute;
                        alarmItem.offset = offset;
                        alarmItem.modified = true;
                    }
                    if (!alarmItem.enabled) {
                        alarmItem.enabled = true;
                        alarmItem.modified = true;
                    }
                    scheduleBedtimeAlarmItem(context, slot, alarmItem, item, addAlarm);
                }
            });
        }
    }

    protected void scheduleBedtimeAlarmItem(final Context context, final String slot, @Nullable AlarmClockItem alarmItem, @Nullable final BedtimeItem item, boolean addAlarm)
    {
        if (alarmItem != null)
        {
            if (alarmItem.modified)
            {
                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(getActivity(), addAlarm, false);
                task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    @Override
                    public void onFinished(Boolean result, AlarmClockItem alarmItem)
                    {
                        BedtimeSettings.saveAlarmID(getActivity(), slot, alarmItem.rowID);
                        getActivity().sendBroadcast( AlarmNotifications.getAlarmIntent(getActivity(), AlarmNotifications.ACTION_RESCHEDULE, alarmItem.getUri()) );
                        Toast.makeText(context, "Modified alarm scheduled: " + slot, Toast.LENGTH_SHORT).show();                        // TODO
                        //adapter.moveItem(adapter.findItemPosition(BedtimeItem.ItemType.WAKEUP_ALARM), 0);
                    }
                });
                task.execute(alarmItem);

            } else {
                getActivity().sendBroadcast( AlarmNotifications.getAlarmIntent(getActivity(), AlarmNotifications.ACTION_RESCHEDULE, alarmItem.getUri()) );
                Toast.makeText(context, "Existing alarm (re)scheduled: " + slot, Toast.LENGTH_SHORT).show();                        // TODO
            }
        } else {
            BedtimeSettings.clearAlarmID(context, slot);
            Toast.makeText(context, "Cleared alarm ID: " + slot, Toast.LENGTH_SHORT).show();                        // TODO
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected AlarmClockItem createBedtimeAlarmItem(final Context context, @Nullable final BedtimeItem item, int hour, int minute, long offset)
    {
        Location location = WidgetSettings.loadLocationPref(context, 0);
        AlarmClockItem alarmItem = AlarmListDialog.createAlarm(context, AlarmClockItem.AlarmType.ALARM,
                "Wake up",    // TODO: i18n
                null, location, -1, hour, minute, null,
                AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, AlarmClockItem.AlarmType.ALARM), AlarmSettings.getDefaultRingtoneName(context, AlarmClockItem.AlarmType.ALARM), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);

        alarmItem.offset = offset;
        alarmItem.repeating = true;
        alarmItem.enabled = true;
        AlarmNotifications.updateAlarmTime(context, alarmItem);
        return alarmItem;
    }

    protected AlarmClockItem createBedtimeEventItem(final Context context, @Nullable final BedtimeItem item, int hour, int minute, long offset)
    {
        Location location = WidgetSettings.loadLocationPref(context, 0);
        AlarmClockItem alarmItem = AlarmListDialog.createAlarm(context, AlarmClockItem.AlarmType.NOTIFICATION,
                "Bedtime",    // TODO: i18n
                null, location, -1, hour, minute, null,
                AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, AlarmClockItem.AlarmType.NOTIFICATION), AlarmSettings.getDefaultRingtoneName(context, AlarmClockItem.AlarmType.NOTIFICATION), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);

        alarmItem.offset = offset;
        alarmItem.repeating = true;
        alarmItem.actionID0 = WidgetActions.SuntimesAction.TRIGGER_BEDTIME.name();
        alarmItem.actionID1 = WidgetActions.SuntimesAction.DISMISS_BEDTIME.name();
        alarmItem.vibrate = false;
        alarmItem.enabled = true;
        AlarmNotifications.updateAlarmTime(context, alarmItem);
        return alarmItem;
    }

    protected void loadAlarmItem(Long rowId, AlarmListDialog.AlarmListTask.AlarmListTaskListener taskListener)
    {
        if (rowId != null)
        {
            AlarmListDialog.AlarmListTask listTask = new AlarmListDialog.AlarmListTask(getActivity());
            listTask.setTaskListener(taskListener);
            listTask.execute(rowId);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean showAlarmEditActivity(long rowID, @Nullable View sharedView, final int requestCode, boolean isNewAlarm)
    {
        if (rowID == BedtimeSettings.ID_NONE) {
            return false;
        }
        loadAlarmItem(rowID, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
        {
            @Override
            public void onLoadFinished(List<AlarmClockItem> result)
            {
                super.onLoadFinished(result);
                if (result != null && result.size() > 0) {
                    showAlarmEditActivity(result.get(0), null, requestCode, false);
                }
            }
        });
        return true;
    }

    protected boolean showAlarmEditActivity(@NonNull AlarmClockItem item, @Nullable View sharedView, int requestCode, boolean isNewAlarm)
    {
        Intent intent = new Intent(getActivity(), AlarmEditActivity.class);
        intent.putExtra(AlarmEditActivity.EXTRA_ITEM, item);
        intent.putExtra(AlarmEditActivity.EXTRA_ISNEW, isNewAlarm);

        if (Build.VERSION.SDK_INT >= 16 && sharedView != null)
        {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), sharedView, ViewCompat.getTransitionName(sharedView));
            startActivityForResult(intent, requestCode, options.toBundle());

        } else {
            startActivityForResult(intent, requestCode);
        }
        return true;
    }

    protected boolean mirrorAlarmItem(final Long rowID, final AlarmClockItem toItem)
    {
        if (rowID == null || rowID == BedtimeSettings.ID_NONE) {
            return false;
        }

        loadAlarmItem(rowID, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
        {
            @Override
            public void onLoadFinished(List<AlarmClockItem> result)
            {
                super.onLoadFinished(result);
                if (result != null && result.size() > 0)
                {
                    AlarmClockItem bedtimeItem = result.get(0);
                    bedtimeItem.setEvent(toItem.getEvent());
                    bedtimeItem.location = toItem.location;
                    bedtimeItem.hour = toItem.hour;
                    bedtimeItem.minute = toItem.minute;
                    bedtimeItem.repeating = toItem.repeating;
                    bedtimeItem.setRepeatingDays(toItem.getRepeatingDays());
                    bedtimeItem.modified = true;

                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(getActivity(), false, false);
                    task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                        @Override
                        public void onFinished(Boolean result, @Nullable AlarmClockItem[] items) {
                            super.onFinished(result, items);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    task.execute(bedtimeItem);

                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return true;
    }

    protected void onEditAlarmResult(int resultCode, Intent data, boolean isNewAlarm, final String pairedSlot)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            if (data != null)
            {
                AlarmClockItem item = data.getParcelableExtra(AlarmEditActivity.EXTRA_ITEM);
                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(getActivity(), isNewAlarm, false);
                task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    @Override
                    public void onFinished(Boolean result, @Nullable final AlarmClockItem[] items)
                    {
                        super.onFinished(result, items);
                        Long rowID = BedtimeSettings.loadAlarmID(getActivity(), pairedSlot);
                        if (!mirrorAlarmItem(rowID, items[0])) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                task.execute(item);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String DIALOG_ADD_ALARM = "dialog_add_alarm";

    protected void showAddAlarmDialog(final Context context, BedtimeItem item)
    {
        AlarmCreateDialog dialog = new AlarmCreateDialog();
        FragmentManager fragments = getChildFragmentManager();

        dialog.setShowDateSelectButton(false);      // hide date selection
        dialog.setShowTimeZoneSelectButton(false);  // hide time zone selection
        dialog.setShowAlarmListButton(false);       // hide list button
        dialog.setAllowSelectType(false);           // disable type selector
        dialog.setLabelOverride("Wake up");         // override type labels    // TODO: i18n
        dialog.setAlarmType(AlarmClockItem.AlarmType.ALARM);    // restrict type to alarms only
        // TODO: locked/disabled events

        dialog.setOnAcceptedListener(onAddAlarmDialogAccept(DIALOG_ADD_ALARM, BedtimeSettings.SLOT_WAKEUP_ALARM, item));
        dialog.show(fragments, DIALOG_ADD_ALARM);
    }

    private final DialogInterface.OnClickListener onAddAlarmDialogAccept(final String dialogTag, final String slot, final BedtimeItem item)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                FragmentManager fragments = getChildFragmentManager();
                AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(dialogTag);
                if (dialog != null)
                {
                    AlarmClockItem alarmItem = createBedtimeAlarmItem(getActivity(), item, dialog.getHour(), dialog.getMinute(), dialog.getOffset());
                    alarmItem.type = dialog.getAlarmType();
                    alarmItem.location = dialog.getLocation();

                    if (dialog.getMode() == 0)
                    {
                        String eventString = dialog.getEvent();
                        if (eventString != null) {
                            alarmItem.setEvent(eventString);
                        }
                    }
                    scheduleBedtimeAlarmItem(getActivity(), slot, alarmItem, item, true);

                    float numSleepCycles = BedtimeSettings.loadPrefSleepCycleCount(getActivity());
                    long sleepCycleMs = BedtimeSettings.loadPrefSleepCycleMs(getActivity());
                    long sleepTotalMs = (long)(numSleepCycles * sleepCycleMs);
                    final long bedtime_offset = -sleepTotalMs + (1000 * 60);
                    configureBedtimeAt(getActivity(), item, BedtimeSettings.SLOT_BEDTIME_NOTIFY, alarmItem.hour, alarmItem.minute, bedtime_offset);
                }
            }
        };
    }

}
