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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmCreateDialog;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmEditActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.settings.AppSettings;
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

    public static final int REQUEST_EDIT_REMINDER = 50;

    protected RecyclerView list;
    protected BedtimeItemAdapter adapter;
    protected SuntimesUtils utils = new SuntimesUtils();

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

        SimpleItemAnimator animator = (SimpleItemAnimator) list.getItemAnimator();
        animator.setChangeDuration(0);

        if (savedState != null) {
            loadSettings(savedState);
        }

        reloadAdapter();
        return content;
    }

    public void notifyItemChanged(int position)
    {
        if (adapter != null) {
            adapter.notifyItemChanged(position);
        }
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
            case REQUEST_ADD_BEDTIME:
                onEditAlarmResult(resultCode, data, true, null);
                break;

            case REQUEST_EDIT_WAKEUP:
                onEditAlarmResult(resultCode, data, false, new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    public void onFinished(Boolean result, @Nullable AlarmClockItem[] items) {
                        if (result && items != null && items.length > 0) {
                            offerModifyBedtimeFromWakeup(getActivity());
                        }
                    }
                });
                break;

            case REQUEST_EDIT_REMINDER:
                onEditAlarmResult(resultCode, data, false, new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    public void onFinished(Boolean result, @Nullable AlarmClockItem[] items)
                    {
                        if (result && items != null && items.length > 0) {
                            if (items[0] != null) {
                                BedtimeSettings.savePrefReminderOffset(getActivity(), items[0].offset);
                            }
                        }
                    }
                });
                break;

            case REQUEST_EDIT_BEDTIME:
                onEditAlarmResult(resultCode, data, false, new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    public void onFinished(Boolean result, @Nullable AlarmClockItem[] items)
                    {
                        if (result && items != null && items.length > 0)
                        {
                            if (items[0] != null) {
                                BedtimeSettings.setAutomaticZenRule(getActivity(), items[0].enabled && BedtimeSettings.loadPrefBedtimeDoNotDisturb(getActivity()));
                            }
                            boolean showReminder = BedtimeSettings.loadPrefBedtimeReminder(getActivity());
                            BedtimeAlarmHelper.setBedtimeReminder_withEventItem(getActivity(),items[0], showReminder);

                            offerModifyWakeupFromBedtime(getActivity());
                        }
                    }
                });
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

                    case BEDTIME:
                        showAddBedtimeDialog(getActivity(), item);
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
                        showAlarmEditActivity(BedtimeSettings.loadAlarmID(getActivity(), BedtimeSettings.SLOT_BEDTIME_REMINDER), null, REQUEST_EDIT_REMINDER, false);
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
        long sleepTotalMs = BedtimeSettings.totalSleepTimeMs(context);
        final long bedtime_offset = -sleepTotalMs + (1000 * 60);
        int sleepMinutes = (int)(sleepTotalMs / (1000 * 60));

        Calendar wakeup = Calendar.getInstance();
        wakeup.add(Calendar.MINUTE, sleepMinutes);
        final int wakeup_hour = wakeup.get(Calendar.HOUR_OF_DAY);
        final int wakeup_minute = wakeup.get(Calendar.MINUTE);
        configureBedtimeAt(context, item, BedtimeSettings.SLOT_WAKEUP_ALARM, wakeup_hour, wakeup_minute, 0, true, true);

        Calendar bedtime = Calendar.getInstance();
        bedtime.setTimeInMillis(wakeup.getTimeInMillis() + bedtime_offset);
        configBedtimeToDate(context, item, bedtime, true, true);
    }

    protected void configBedtimeToDate(final Context context, BedtimeItem item, Calendar bedtime, boolean modifyEnabled, boolean enabled)
    {
        final int bedtime_hour = bedtime.get(Calendar.HOUR_OF_DAY);
        final int bedtime_minute = bedtime.get(Calendar.MINUTE);
        configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFY, bedtime_hour, bedtime_minute, 0, modifyEnabled, enabled);
        BedtimeSettings.setAutomaticZenRule(getActivity(), BedtimeSettings.loadPrefBedtimeDoNotDisturb(getActivity()));
        BedtimeAlarmHelper.setBedtimeReminder_withEventInfo(context, bedtime_hour, bedtime_minute, 0, BedtimeSettings.loadPrefBedtimeReminder(context));
    }

    protected void configBedtimeFromWakeup(final Context context, @Nullable final BedtimeItem item)
    {
        final long wakeupId = BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_WAKEUP_ALARM);
        if (wakeupId != BedtimeSettings.ID_NONE)
        {
            final long sleepTotalMs = BedtimeSettings.totalSleepTimeMs(context);
            BedtimeAlarmHelper.loadAlarmItem(context, wakeupId, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onLoadFinished(List<AlarmClockItem> result)
                {
                    if (result != null && result.size() > 0)
                    {
                        AlarmClockItem wakeupItem = result.get(0);
                        if (wakeupItem != null)
                        {
                            Calendar bedtime = Calendar.getInstance();
                            bedtime.setTimeInMillis(wakeupItem.timestamp + wakeupItem.offset - sleepTotalMs);
                            configBedtimeToDate(context, item, bedtime, false, false);

                        } else Log.w("DEBUG", "failed to configure bedtime to wakeup time; null");
                    } else Log.w("DEBUG", "failed to configure bedtime to wakeup time; load failed");
                }
            });
        } else Log.d("DEBUG", "failed to configure bedtime to wakeup time; not set");
    }

    protected void configWakeupFromBedtime(final Context context, @Nullable final BedtimeItem item)
    {
        final long rowId = BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_BEDTIME_NOTIFY);
        if (rowId != BedtimeSettings.ID_NONE)
        {
            final long sleepTotalMs = BedtimeSettings.totalSleepTimeMs(context);
            BedtimeAlarmHelper.loadAlarmItem(context, rowId, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onLoadFinished(List<AlarmClockItem> result)
                {
                    if (result != null && result.size() > 0)
                    {
                        AlarmClockItem bedtimeItem = result.get(0);
                        if (bedtimeItem != null)
                        {
                            Calendar wakeup = Calendar.getInstance();
                            wakeup.setTimeInMillis(bedtimeItem.timestamp + bedtimeItem.offset + sleepTotalMs);
                            final int hour = wakeup.get(Calendar.HOUR_OF_DAY);
                            final int minute = wakeup.get(Calendar.MINUTE);
                            configureBedtimeAt(context, item, BedtimeSettings.SLOT_WAKEUP_ALARM, hour, minute, 0, false, false);

                        } else Log.w("DEBUG", "failed to configure wakeup from bedtme; null");
                    } else Log.w("DEBUG", "failed to configure wakeup from bedtime; load failed");
                }
            });
        } else Log.d("DEBUG", "failed to configure wakeup from bedtime; not set");
    }

    protected void configureBedtimeAt(final Context context, @Nullable final BedtimeItem item, @NonNull final String slot, final int hour, final int minute, final long offset, final boolean modifyEnabled, final boolean enabled) {
        configureBedtimeAt(context, item, slot, null, hour, minute, offset, modifyEnabled, enabled);
    }
    protected void configureBedtimeAt(final Context context, @Nullable final BedtimeItem item, @NonNull final String slot, @Nullable final String event, final int hour, final int minute, final long offset, final boolean modifyEnabled, final boolean enabled)
    {
        long alarmID = BedtimeSettings.loadAlarmID(getActivity(), slot);
        if (alarmID == BedtimeSettings.ID_NONE)
        {
            AlarmClockItem alarmItem = (slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY))
                    ? BedtimeAlarmHelper.createBedtimeEventItem(context, item, hour, minute, offset)
                    : BedtimeAlarmHelper.createBedtimeAlarmItem(context, item, hour, minute, offset);
            alarmItem.enabled = enabled;
            alarmItem.modified = true;
            scheduleBedtimeAlarmItem(context, slot, alarmItem, item, true);

        } else {
            BedtimeAlarmHelper.loadAlarmItem(getActivity(), alarmID, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onLoadFinished(List<AlarmClockItem> result)
                {
                    AlarmClockItem alarmItem = ((result != null && result.size() > 0 && result.get(0) != null) ? result.get(0) : null);
                    boolean addAlarm = (alarmItem == null);
                    if (addAlarm) {
                        alarmItem = (slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY))
                                ? BedtimeAlarmHelper.createBedtimeEventItem(context, item, hour, minute, offset) : BedtimeAlarmHelper.createBedtimeAlarmItem(context, item, hour, minute, offset);
                    }
                    if (alarmItem.hour != hour || alarmItem.minute != minute || alarmItem.offset != offset) {
                        alarmItem.hour = hour;
                        alarmItem.minute = minute;
                        alarmItem.offset = offset;
                        alarmItem.setEvent(event);
                        alarmItem.modified = true;
                    }
                    if (modifyEnabled && alarmItem.enabled != enabled) {
                        alarmItem.enabled = enabled;
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
                BedtimeAlarmHelper.saveAlarmItem(getActivity(), alarmItem, addAlarm, new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    @Override
                    public void onFinished(Boolean result, AlarmClockItem alarmItem)
                    {
                        BedtimeSettings.saveAlarmID(getActivity(), slot, alarmItem.rowID);
                        //if (!alarmItem.enabled) {
                            getActivity().sendBroadcast(AlarmNotifications.getFullscreenBroadcast(alarmItem.getUri()));
                        //}
                        getActivity().sendBroadcast( AlarmNotifications.getAlarmIntent(getActivity(), AlarmNotifications.ACTION_RESCHEDULE, alarmItem.getUri()) );
                        Log.d("DEBUG", "Modified alarm scheduled: " + slot + ", enabled: " + alarmItem.enabled);
                    }
                });
            } else {
                getActivity().sendBroadcast( AlarmNotifications.getAlarmIntent(getActivity(), AlarmNotifications.ACTION_RESCHEDULE, alarmItem.getUri()) );
                Log.d("DEBUG", "Existing alarm (re)scheduled: " + slot + ", enabled: " + alarmItem.enabled);
            }
        } else {
            BedtimeSettings.clearAlarmID(context, slot);
            Log.d("DEBUG", "Cleared alarm ID: " + slot);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean showAlarmEditActivity(long rowID, @Nullable View sharedView, final int requestCode, boolean isNewAlarm)
    {
        if (rowID == BedtimeSettings.ID_NONE) {
            return false;
        }
        BedtimeAlarmHelper.loadAlarmItem(getActivity(), rowID, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
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

        BedtimeAlarmHelper.loadAlarmItem(getActivity(), rowID, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
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
                    bedtimeItem.offset = toItem.offset;
                    bedtimeItem.repeating = toItem.repeating;
                    bedtimeItem.setRepeatingDays(toItem.getRepeatingDays());
                    bedtimeItem.modified = true;

                    BedtimeAlarmHelper.saveAlarmItem(getActivity(), bedtimeItem, false, new AlarmDatabaseAdapter.AlarmItemTaskListener()
                    {
                        @Override
                        public void onFinished(Boolean result, @Nullable AlarmClockItem[] items) {
                            adapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return true;
    }

    protected void onEditAlarmResult(int resultCode, Intent data, boolean isNewAlarm, @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener onSaved)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            final AlarmClockItem item = data.getParcelableExtra(AlarmEditActivity.EXTRA_ITEM);
            if (item != null)
            {
                BedtimeAlarmHelper.saveAlarmItem(getActivity(), item, isNewAlarm, new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    @Override
                    public void onFinished(Boolean result, @Nullable final AlarmClockItem[] items)
                    {
                        int position = adapter.findItemPosition(getActivity(), item.rowID);
                        if (position >= 0) {
                            adapter.notifyItemChanged(position);
                        }
                        BedtimeAlarmHelper.scheduleAlarmItem(getActivity(), item, item.enabled);

                        if (onSaved != null) {
                            onSaved.onFinished(result, items);
                        }
                    }
                });
            } else {
                if (onSaved != null) {    // data may contain null item if EditActivity deleted its entry
                    onSaved.onFinished(true, new AlarmClockItem[] { item });
                }
            }
        } else {
            if (onSaved != null) {
                onSaved.onFinished(false, (AlarmClockItem[])null);
            }
        }
    }

    protected void offerModifyBedtimeFromWakeup(final Context context)
    {
        String sleepHours = utils.timeDeltaLongDisplayString(-1 * BedtimeSettings.totalSleepTimeMs(context));
        String messageString = "Set the bedtime to " + sleepHours + " before wake up time?";        // TODO
        CharSequence message = SuntimesUtils.createBoldSpan(null, messageString, sleepHours);

        Snackbar snackbar = Snackbar.make(getList(), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(context.getString(R.string.configAction_setBedtime), new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                configBedtimeFromWakeup(context, adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.BEDTIME)));
            }
        });
        SuntimesUtils.themeSnackbar(context, snackbar, null);
        snackbar.show();

        /*int[] attrs = { R.attr.icActionBedtime };
        TypedArray a = context.obtainStyledAttributes(attrs);
        int iconResID = a.getResourceId(0, R.drawable.ic_action_bedtime);
        a.recycle();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(iconResID);
        dialog.setMessage(message);
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {    // TODO
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setPositiveButton("Set Bedtime", new DialogInterface.OnClickListener() {    // TODO
            public void onClick(DialogInterface dialog, int which) {
                configBedtimeFromWakeup(context, adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.BEDTIME)));
            }
        });
        dialog.show();*/
    }

    protected void offerModifyWakeupFromBedtime(final Context context)
    {
        String sleepHours = utils.timeDeltaLongDisplayString(BedtimeSettings.totalSleepTimeMs(context));
        String messageString = "Set the wake up alarm to " + sleepHours + " after bedtime?";        // TODO
        CharSequence message = SuntimesUtils.createBoldSpan(null, messageString, sleepHours);

        Snackbar snackbar = Snackbar.make(getList(), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(context.getString(R.string.configAction_setAlarm), new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                configWakeupFromBedtime(context, adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.WAKEUP_ALARM)));
            }
        });
        SuntimesUtils.themeSnackbar(context, snackbar, null);
        snackbar.show();

        /*int[] attrs = { R.attr.icActionAlarm };
        TypedArray a = context.obtainStyledAttributes(attrs);
        int iconResID = a.getResourceId(0, R.drawable.ic_action_alarms);
        a.recycle();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(iconResID);
        dialog.setMessage(message);
        dialog.setNegativeButton("Leave it", new DialogInterface.OnClickListener() {    // TODO
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.setPositiveButton("Set Alarm", new DialogInterface.OnClickListener()    // TODO
        {
            public void onClick(DialogInterface dialog, int which) {
                configWakeupFromBedtime(context, adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.WAKEUP_ALARM)));
            }
        });
        dialog.show();*/
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void showAddBedtimeDialog(final Context context, BedtimeItem item)
    {
        // TODO: offer choice between configure to wakeup or a set time
        long wakeupId = BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_WAKEUP_ALARM);
        if (wakeupId != BedtimeSettings.ID_NONE) {
            configBedtimeFromWakeup(context, item);

        } else {
            Toast.makeText(context, "TODO: bedtime time dialog", Toast.LENGTH_SHORT).show();   // TODO
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
                    AlarmClockItem alarmItem = BedtimeAlarmHelper.createBedtimeAlarmItem(getActivity(), item, dialog.getHour(), dialog.getMinute(), dialog.getOffset());
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
                    offerModifyBedtimeFromWakeup(getActivity());
                }
            }
        };
    }

}
