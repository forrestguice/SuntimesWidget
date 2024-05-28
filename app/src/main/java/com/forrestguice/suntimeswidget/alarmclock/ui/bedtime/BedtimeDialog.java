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
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
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
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmEditDialog;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.MillisecondPickerDialog;
import com.forrestguice.suntimeswidget.settings.MillisecondPickerHelper;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
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
    protected LinearLayoutManager layout;
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
        list.setLayoutManager(layout = new LinearLayoutManager(getActivity()));
        list.addOnScrollListener(onListScrolled);
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
    public void onStop()
    {
        super.onStop();
        if (list != null) {
            list.setLayoutManager(null);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (list != null) {
            list.setLayoutManager(layout);
        }
        restoreDialogs(getActivity());
        if (onResume_refreshData) {
            adapter.reloadAlarmClockItems(getActivity());
        } else onResume_refreshData = true;
    }
    protected boolean onResume_refreshData = true;

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

        MillisecondPickerDialog sleepOffsetDialog = (MillisecondPickerDialog) fragments.findFragmentByTag(DIALOG_SLEEP_OFFSET);
        if (sleepOffsetDialog != null) {
            sleepOffsetDialog.setDialogListener(onSleepOffsetDialogListener(adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.SLEEP_CYCLE))));
        }

        MillisecondPickerDialog sleepCycleDialog = (MillisecondPickerDialog) fragments.findFragmentByTag(DIALOG_SLEEP_CYCLE);
        if (sleepCycleDialog != null) {
            sleepCycleDialog.setDialogListener(onSleepCycleDialogListener(adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.SLEEP_CYCLE))));
        }

        BedtimeSleepDialog sleepDialog = (BedtimeSleepDialog) fragments.findFragmentByTag(DIALOG_SLEEP_CYCLES);
        if (sleepDialog != null) {
            sleepDialog.setOnAcceptedListener(onSleepCyclesDialogAccepted(adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.SLEEP_CYCLE))));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        onResume_refreshData = false;
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
                        if (result && items != null && items.length > 0)
                        {
                            boolean wasDeleted = data.getBooleanExtra(AlarmNotifications.ACTION_DELETE, false);
                            if (!wasDeleted) {
                                offerModifyBedtimeFromWakeup(getActivity());
                            }
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
                            if (items[0] != null && items[0].getEvent() == null) {    // items based on events shouldn't touch this setting
                                BedtimeSettings.savePrefReminderOffset(getActivity(), items[0].offset);    // TODO: some other way to set reminder offset setting
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
                            onBedtimeChanged_updateBedtimeOff(getActivity(), adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.SLEEP_CYCLE)));

                            if (items[0] != null) {
                                BedtimeSettings.setAutomaticZenRule(getActivity(), items[0].enabled && BedtimeSettings.loadPrefBedtimeDoNotDisturb(getActivity()));
                            }
                            boolean showReminder = BedtimeSettings.loadPrefBedtimeReminder(getActivity());
                            BedtimeAlarmHelper.setBedtimeReminder_withEventItem(getActivity(),items[0], showReminder);

                            boolean wasDeleted = data.getBooleanExtra(AlarmNotifications.ACTION_DELETE, false);
                            if (!wasDeleted) {
                                offerModifyWakeupFromBedtime(getActivity());
                            }
                        }
                    }
                });
                break;

            default:
                onResume_refreshData = true;
                break;
        }
    }


    /**
     * OnScrollListener
     */
    private final RecyclerView.OnScrollListener onListScrolled = new RecyclerView.OnScrollListener()
    {
        private int lastCompletelyVisibleItemPosition;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            if (listener != null)
            {
                int position = layout.findLastCompletelyVisibleItemPosition();
                if (position != lastCompletelyVisibleItemPosition) {
                    lastCompletelyVisibleItemPosition = position;
                    listener.onScrolled(recyclerView, lastCompletelyVisibleItemPosition);
                }
            }
            super.onScrolled(recyclerView, dx, dy);
        }
    };


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

    protected DialogListener listener;
    protected BedtimeItemAdapter.AdapterListener adapterListener = new BedtimeItemAdapter.AdapterListener()
    {
        @Override
        public void onItemAction(BedtimeViewHolder holder, BedtimeItem item)
        {
            if (item != null)
            {
                switch (item.getItemType())
                {
                    case BEDTIME_NOW:
                        //triggerBedtimeNow(getActivity(), item);
                        AlarmNotifications.NotificationService.triggerBedtimeMode(getActivity(), true);
                        break;

                    case WAKEUP_ALARM:
                        if (BedtimeSettings.hasAlarmID(getActivity(), BedtimeSettings.SLOT_BEDTIME_NOTIFY)) {
                            showAddAlarmMenu(getActivity(), holder.getActionView(), item);
                        } else showAddAlarmDialog(getActivity(), item);
                        break;

                    case BEDTIME:
                        showAddBedtimeMenu(getActivity(), holder.getActionView(),  item);
                        break;

                    default:
                        if (listener != null) {
                            listener.onItemAction(holder, item);
                        }
                        break;
                }
            }
        }

        @Override
        public void onItemConfigure(BedtimeViewHolder holder, BedtimeItem item)
        {
            if (item != null)
            {
                switch (item.getItemType())
                {
                    case SLEEP_CYCLE:
                        configureSleepTime(getActivity(), holder.getConfigureActionView(), item);
                        break;

                    case WAKEUP_ALARM:
                        showEditBedtimeMenu(getActivity(), holder.getConfigureActionView(), item,  BedtimeSettings.SLOT_WAKEUP_ALARM, REQUEST_EDIT_WAKEUP, R.menu.bedtime_wakeup_edit);
                        break;

                    case BEDTIME:
                        showEditBedtimeMenu(getActivity(), holder.getConfigureActionView(), item,  BedtimeSettings.SLOT_BEDTIME_NOTIFY, REQUEST_EDIT_BEDTIME, R.menu.bedtime_edit);
                        break;

                    case BEDTIME_REMINDER:
                        showEditBedtimeMenu(getActivity(), holder.getConfigureActionView(), item,  BedtimeSettings.SLOT_BEDTIME_REMINDER, REQUEST_EDIT_REMINDER, R.menu.bedtime_reminder_edit);
                        break;

                    default:
                        if (listener != null) {
                            listener.onItemAction(holder, item);
                        }
                        break;
                }
            }
        }
    };

    public void setDialogListener(DialogListener listener) {
        this.listener = listener;
    }

    public interface DialogListener extends BedtimeItemAdapter.AdapterListener
    {
        void onScrolled(RecyclerView recyclerView, int firstCompletelyVisibleItemPosition);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configureSleepTime(final Context context, View v, @Nullable final BedtimeItem item)
    {
        PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.action_bedtime_sleepCycles:
                        showConfigureSleepCyclesDialog(item);
                        return true;

                    case R.id.action_bedtime_sleepCycleLength:
                        showConfigureSleepCycleDialog(item);
                        return true;

                    case R.id.action_bedtime_sleep_offset:
                        showConfigureSleepOffsetDialog(item);
                        return true;

                    case R.id.action_bedtime_sleep_autooff:
                        toggleConfigureBedtimeAutoOff(context, item);
                        return true;

                    default:
                        return false;
                }
            }
        };
        PopupMenu popup = PopupMenuCompat.createMenu(context, v, R.menu.bedtime_sleep, onMenuItemClickListener, null);
        Menu menu = popup.getMenu();

        MenuItem item_enabled = menu.findItem(R.id.action_bedtime_sleep_autooff);
        item_enabled.setChecked(BedtimeSettings.loadPrefBedtimeAutoOff(context));

        popup.show();
    }

    protected void toggleConfigureBedtimeAlarmOff(Context context, BedtimeItem item)
    {
        boolean toggled = !BedtimeSettings.loadPrefBedtimeAlarmOff(context);
        BedtimeSettings.savePrefBedtimeAlarmOff(context, toggled);
        Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show();    // TODO
    }

    protected void toggleConfigureBedtimeAutoOff(Context context, BedtimeItem item)
    {
        boolean toggled = !BedtimeSettings.loadPrefBedtimeAutoOff(context);
        BedtimeSettings.savePrefBedtimeAutoOff(context, toggled);
        onSleepTimeChanged_updateBedtimeOff(context, item);
    }

    protected void onBedtimeChanged_updateBedtimeOff(Context context, @Nullable BedtimeItem item)
    {
        BedtimeItem linkedItem = ((item != null) ? item.getLinkedItem() : null);
        AlarmClockItem linkedAlarmItem = ((linkedItem != null) ? linkedItem.getAlarmItem() : null);

        if (linkedAlarmItem != null)
        {
            if (linkedAlarmItem.getEvent() != null) {
                configureBedtimeAt(getActivity(), item, BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF, linkedAlarmItem.getEvent(), -1, -1, 0, true, linkedAlarmItem.enabled && BedtimeSettings.loadPrefBedtimeAutoOff(context));

            } else {
                AlarmNotifications.updateAlarmTime(context, linkedAlarmItem);
                Calendar bedtime = Calendar.getInstance();
                bedtime.set(Calendar.HOUR_OF_DAY, linkedAlarmItem.hour);
                bedtime.set(Calendar.MINUTE, linkedAlarmItem.minute);
                bedtime.setTimeInMillis(bedtime.getTimeInMillis() + BedtimeSettings.totalSleepTimeMs(getActivity()));
                configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF, bedtime.get(Calendar.HOUR_OF_DAY), bedtime.get(Calendar.MINUTE), 0, true, linkedAlarmItem.enabled && BedtimeSettings.loadPrefBedtimeAutoOff(context));
            }
        }
    }

    protected void onSleepTimeChanged_updateBedtimeOff(Context context, @Nullable BedtimeItem item)
    {
        BedtimeItem linkedItem = ((item != null) ? item.getLinkedItem() : null);
        AlarmClockItem linkedAlarmItem = ((linkedItem != null) ? linkedItem.getAlarmItem() : null);
        if (linkedAlarmItem != null)
        {
            if (linkedAlarmItem.getEvent() != null)
            {
                // when bedtime is based on an event, changes to sleep duration modify bedtime
                configureBedtimeAt(getActivity(), item, BedtimeSettings.SLOT_BEDTIME_NOTIFY, linkedAlarmItem.getEvent(), -1, -1, -BedtimeSettings.totalSleepTimeMs(getActivity()), true, linkedAlarmItem.enabled);
                configureBedtimeAt(getActivity(), item, BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF, linkedAlarmItem.getEvent(), -1, -1, 0, true, BedtimeSettings.loadPrefBedtimeAutoOff(context) && linkedAlarmItem.enabled);

            } else {
                // when bedtime is based on some time, changes to sleep duration modify "bedtime off"
                AlarmNotifications.updateAlarmTime(context, linkedAlarmItem);
                Calendar bedtime = Calendar.getInstance();
                bedtime.set(Calendar.HOUR_OF_DAY, linkedAlarmItem.hour);
                bedtime.set(Calendar.MINUTE, linkedAlarmItem.minute);
                bedtime.setTimeInMillis(bedtime.getTimeInMillis() + BedtimeSettings.totalSleepTimeMs(getActivity()));
                configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF, bedtime.get(Calendar.HOUR_OF_DAY), bedtime.get(Calendar.MINUTE), 0, true, BedtimeSettings.loadPrefBedtimeAutoOff(context) && linkedAlarmItem.enabled);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String DIALOG_SLEEP_CYCLE = "dialog_sleep_cycle";
    protected void showConfigureSleepCycleDialog(final BedtimeItem item)
    {
        FragmentManager fragments = getChildFragmentManager();

        final MillisecondPickerDialog dialog = new MillisecondPickerDialog();
        dialog.setMode(MillisecondPickerHelper.MODE_MINUTES);
        dialog.setParamMinMax(getResources().getInteger(R.integer.minSleepCycleMinutes),
                getResources().getInteger(R.integer.maxSleepCycleMinutes));
        dialog.setValue((int) BedtimeSettings.loadPrefSleepCycleMs(getActivity()));
        dialog.setDialogListener(onSleepCycleDialogListener(item));
        dialog.setDialogTitle(getString(R.string.configLabel_sleepCycle));
        dialog.show(fragments, DIALOG_SLEEP_CYCLE);
    }

    private MillisecondPickerDialog.DialogListener onSleepCycleDialogListener(final BedtimeItem item)
    {
        return new MillisecondPickerDialog.DialogListener()
        {
            @Override
            public void onDialogAccepted(long value) {
                BedtimeSettings.savePrefSleepCycleMs(getActivity(), value);
                onSleepTimeChanged_updateBedtimeOff(getActivity(), item);
                adapter.notifyItemChanged(adapter.findItemPosition(item));

                BedtimeItem linkedItem = item.getLinkedItem();
                if (linkedItem != null) {
                    adapter.notifyItemChanged(adapter.findItemPosition(linkedItem));
                }
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String DIALOG_SLEEP_CYCLES = "dialog_sleep_cycles";
    protected void showConfigureSleepCyclesDialog(final BedtimeItem item)
    {
        FragmentManager fragments = getChildFragmentManager();
        final BedtimeSleepDialog dialog = new BedtimeSleepDialog();
        dialog.setNumCycles(BedtimeSettings.loadPrefSleepCycleCount(getActivity()));
        dialog.setDialogTitle(getString(R.string.configLabel_sleepCycles));
        dialog.setOnAcceptedListener(onSleepCyclesDialogAccepted(item));
        dialog.show(fragments, DIALOG_SLEEP_CYCLES);
    }

    private final DialogInterface.OnClickListener onSleepCyclesDialogAccepted(final BedtimeItem item)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                FragmentManager fragments = getChildFragmentManager();
                final BedtimeSleepDialog dialog = (BedtimeSleepDialog) fragments.findFragmentByTag(DIALOG_SLEEP_CYCLES);
                if (dialog != null)
                {
                    BedtimeSettings.savePrefSleepCycleCount(getActivity(), dialog.getNumCycles());
                    onSleepTimeChanged_updateBedtimeOff(getActivity(), item);
                    adapter.notifyItemChanged(adapter.findItemPosition(item));

                    BedtimeItem linkedItem = item.getLinkedItem();
                    if (linkedItem != null) {
                        adapter.notifyItemChanged(adapter.findItemPosition(linkedItem));
                    }
                }
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String DIALOG_SLEEP_OFFSET = "dialog_sleep_offset";

    protected void showConfigureSleepOffsetDialog(final BedtimeItem item)
    {
        FragmentManager fragments = getChildFragmentManager();
        final MillisecondPickerDialog dialog = new MillisecondPickerDialog();
        dialog.setMode(MillisecondPickerHelper.MODE_MINUTES);
        dialog.setParamMinMax(getResources().getInteger(R.integer.minFallAsleepMinutes),
                getResources().getInteger(R.integer.maxFallAsleepMinutes));
        dialog.setValue((int) BedtimeSettings.loadPrefSleepOffsetMs(getActivity()));
        dialog.setDialogListener(onSleepOffsetDialogListener(item));
        dialog.setDialogTitle(getString(R.string.configLabel_sleepOffset));
        dialog.setParamZeroText(getString(R.string.cycleNone));
        dialog.show(fragments, DIALOG_SLEEP_OFFSET);
    }

    private MillisecondPickerDialog.DialogListener onSleepOffsetDialogListener(final BedtimeItem item)
    {
        return new MillisecondPickerDialog.DialogListener()
        {
            @Override
            public void onDialogAccepted(long value) {
                BedtimeSettings.savePrefSleepOffsetMs(getActivity(), value);
                onSleepTimeChanged_updateBedtimeOff(getActivity(), item);
                adapter.notifyItemChanged(adapter.findItemPosition(item));

                BedtimeItem linkedItem = item.getLinkedItem();
                if (linkedItem != null) {
                    adapter.notifyItemChanged(adapter.findItemPosition(linkedItem));
                }
            }
        };
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
        //final int wakeup_hour = wakeup.get(Calendar.HOUR_OF_DAY);
        //final int wakeup_minute = wakeup.get(Calendar.MINUTE);
        //configureBedtimeAt(context, item, BedtimeSettings.SLOT_WAKEUP_ALARM, wakeup_hour, wakeup_minute, 0, true, true);

        Calendar bedtime = Calendar.getInstance();
        bedtime.setTimeInMillis(wakeup.getTimeInMillis() + bedtime_offset);
        configBedtimeToDate(context, item, bedtime, true, true);
        offerModifyWakeupFromBedtime(getActivity());
    }

    protected void configBedtimeToDate(final Context context, BedtimeItem item, Calendar bedtime, boolean modifyEnabled, boolean enabled)
    {
        final int hour = bedtime.get(Calendar.HOUR_OF_DAY);
        final int minute = bedtime.get(Calendar.MINUTE);
        configBedtimeToDate(context, item, hour, minute, modifyEnabled, enabled);
    }
    protected void configBedtimeToDate(final Context context, BedtimeItem item, int hour, int minute, boolean modifyEnabled, boolean enabled)
    {
        Calendar bedtimeOff = bedtimeOffCalendar(context, hour, minute);
        configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF, bedtimeOff.get(Calendar.HOUR_OF_DAY), bedtimeOff.get(Calendar.MINUTE), 0, modifyEnabled, enabled && BedtimeSettings.loadPrefBedtimeAutoOff(context));
        configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFY, hour, minute, 0, modifyEnabled, enabled);
        BedtimeSettings.setAutomaticZenRule(getActivity(), BedtimeSettings.loadPrefBedtimeDoNotDisturb(getActivity()));
        BedtimeAlarmHelper.setBedtimeReminder_withEventInfo(context, hour, minute, 0, BedtimeSettings.loadPrefBedtimeReminder(context));
    }
    protected void configBedtimeOffsetEvent(final Context context, BedtimeItem item, String event, long offset, boolean modifyEnabled, boolean enabled)
    {
        configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF, event, -1, -1, 0, modifyEnabled, enabled && BedtimeSettings.loadPrefBedtimeAutoOff(context));
        configureBedtimeAt(context, item, BedtimeSettings.SLOT_BEDTIME_NOTIFY, event, -1, -1, offset, modifyEnabled, enabled);
        BedtimeSettings.setAutomaticZenRule(getActivity(), BedtimeSettings.loadPrefBedtimeDoNotDisturb(getActivity()));
        BedtimeAlarmHelper.setBedtimeReminder_withEventInfo(context, event, offset, BedtimeSettings.loadPrefBedtimeReminder(context));
    }

    protected Calendar bedtimeOffCalendar(Context context, int hour, int minute)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.setTimeInMillis(calendar.getTimeInMillis() + BedtimeSettings.totalSleepTimeMs(context));
        return calendar;
    }

    protected void configBedtimeFromWakeup(final Context context, @Nullable final BedtimeItem item, final boolean enabled)
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
                            if (wakeupItem.getEvent() != null) {
                                configBedtimeOffsetEvent(context, item, wakeupItem.getEvent(), wakeupItem.offset - sleepTotalMs, false, enabled);

                            } else {
                                Calendar bedtime = Calendar.getInstance();
                                bedtime.setTimeInMillis(wakeupItem.timestamp + wakeupItem.offset - sleepTotalMs);
                                configBedtimeToDate(context, item, bedtime, false, enabled);
                            }

                        } else Log.w("DEBUG", "failed to configure bedtime to wakeup time; null");
                    } else Log.w("DEBUG", "failed to configure bedtime to wakeup time; load failed");
                }
            });
        } else Log.d("DEBUG", "failed to configure bedtime to wakeup time; not set");
    }

    protected void configWakeupFromBedtime(final Context context, @Nullable final BedtimeItem item, final boolean enabled)
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
                            configureBedtimeAt(context, item, BedtimeSettings.SLOT_WAKEUP_ALARM, hour, minute, 0, false, enabled);

                        } else Log.w("DEBUG", "failed to configure wakeup from bedtme; null");
                    } else Log.w("DEBUG", "failed to configure wakeup from bedtime; load failed");
                }
            });
        } else Log.d("DEBUG", "failed to configure wakeup from bedtime; not set");
    }

    protected void configureBedtimeAt(final Context context, @Nullable final BedtimeItem item, @NonNull final String slot, final int hour, final int minute, final long offset, final boolean modifyEnabled, final boolean enabled) {
        configureBedtimeAt(context, item, slot, null, hour, minute, offset, modifyEnabled, enabled);
    }
    protected void configureBedtimeAt(final Context context, @Nullable final BedtimeItem item, @NonNull final String slot, final String event, final long offset, final boolean modifyEnabled, final boolean enabled) {
        configureBedtimeAt(context, item, slot, null, -1, -1, offset, modifyEnabled, enabled);
    }
    protected void configureBedtimeAt(final Context context, @Nullable final BedtimeItem item, @NonNull final String slot, @Nullable final String event, final int hour, final int minute, final long offset, final boolean modifyEnabled, final boolean enabled)
    {
        long alarmID = BedtimeSettings.loadAlarmID(getActivity(), slot);
        if (alarmID == BedtimeSettings.ID_NONE)
        {
            AlarmClockItem alarmItem = (slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY) || slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF))
                    ? BedtimeAlarmHelper.createBedtimeEventItem(context, slot, item, hour, minute, offset)
                    : BedtimeAlarmHelper.createBedtimeAlarmItem(context, item, hour, minute, offset);
            alarmItem.setEvent(event);
            alarmItem.offset = offset;
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
                        alarmItem = (slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFY) || slot.equals(BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF))
                                ? BedtimeAlarmHelper.createBedtimeEventItem(context, slot, item, hour, minute, offset) : BedtimeAlarmHelper.createBedtimeAlarmItem(context, item, hour, minute, offset);
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

    /*protected boolean mirrorAlarmItem(final Long rowID, final AlarmClockItem toItem)
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
    }*/

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
                        if (result)
                        {
                            Integer[] positions = adapter.findItemPositions(getActivity(), item.rowID);
                            for (int position : positions)
                            {
                                Log.d("DEBUG", "onEditAlarmResult :: " + position);
                                if (position >= 0)
                                {
                                    BedtimeItem bedtimeItem = adapter.getItem(position);
                                    if (bedtimeItem != null) {
                                        bedtimeItem.setAlarmItem(item);
                                    }
                                    adapter.notifyItemChanged(position);
                                }
                            }
                        }
                        BedtimeAlarmHelper.scheduleAlarmItem(getActivity(), item, item.enabled);

                        if (onSaved != null) {
                            onSaved.onFinished(result, items);
                        }
                    }
                });
            } else {
                if (onSaved != null) {    // data may contain null item if EditActivity deleted its entry
                    onSaved.onFinished(true, new AlarmClockItem[] { null });
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
        int[] attrs = { R.attr.text_accentColor };
        TypedArray a = context.obtainStyledAttributes(attrs);
        int accentColor = ContextCompat.getColor(context, a.getResourceId(0, R.color.text_accent_dark));
        a.recycle();

        String sleepHours = utils.timeDeltaLongDisplayString(-1 * BedtimeSettings.totalSleepTimeMs(context));
        String messageString = context.getString(R.string.prompt_bedtime_setFrom_wakeup, sleepHours);
        CharSequence message = SuntimesUtils.createBoldColorSpan(null, messageString, sleepHours, accentColor);

        Snackbar snackbar = Snackbar.make(getList(), message, 7000);
        snackbar.setAction(context.getString(R.string.configAction_setBedtime), new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                configBedtimeFromWakeup(context, adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.BEDTIME)), false);
            }
        });
        SuntimesUtils.themeSnackbar(context, snackbar, null);
        snackbar.show();
    }

    protected void offerModifyWakeupFromBedtime(final Context context)
    {
        int[] attrs = { R.attr.text_accentColor };
        TypedArray a = context.obtainStyledAttributes(attrs);
        int accentColor = ContextCompat.getColor(context, a.getResourceId(0, R.color.text_accent_dark));
        a.recycle();

        String sleepHours = utils.timeDeltaLongDisplayString(BedtimeSettings.totalSleepTimeMs(context));
        String messageString = context.getString(R.string.prompt_bedtime_setFrom_bedtime, sleepHours);
        CharSequence message = SuntimesUtils.createBoldColorSpan(null, messageString, sleepHours, accentColor);

        Snackbar snackbar = Snackbar.make(getList(), message, 7000);
        snackbar.setAction(context.getString(R.string.configAction_setAlarm), new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                configWakeupFromBedtime(context, adapter.getItem(adapter.findItemPosition(BedtimeItem.ItemType.WAKEUP_ALARM)), false);
            }
        });
        SuntimesUtils.themeSnackbar(context, snackbar, null);
        snackbar.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String DIALOG_ADD_BEDTIME = "dialog_add_bedtime";

    protected void showAddBedtimeMenu(final Context context, final View v, final BedtimeItem item)
    {
        PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.action_bedtime_set:
                        showAddBedtimeDialog(getActivity(), v, item);
                        return true;

                    case R.id.action_bedtime_from_wakeup:
                        configBedtimeFromWakeup(context, item, false);
                        return true;

                    case R.id.action_bedtime_now:
                        triggerBedtimeNow(context, item);
                        return true;

                    default:
                        return false;
                }
            }
        };
        PopupMenu popup = PopupMenuCompat.createMenu(context, v, R.menu.bedtime_add, onMenuItemClickListener, null);
        Menu menu = popup.getMenu();
        updateAddBedtimeMenu(context, v, menu);
        popup.show();
    }

    protected void updateAddBedtimeMenu(final Context context, final View v, final Menu menu)
    {
        MenuItem bedtimeFromWakeup = menu.findItem(R.id.action_bedtime_from_wakeup);
        if (bedtimeFromWakeup != null) {
            bedtimeFromWakeup.setVisible(BedtimeSettings.hasAlarmID(context, BedtimeSettings.SLOT_WAKEUP_ALARM));
        }
    }

    protected void showAddBedtimeDialog(final Context context, View v, BedtimeItem item)
    {
        AlarmCreateDialog dialog = new AlarmCreateDialog();
        FragmentManager fragments = getChildFragmentManager();

        dialog.setAlarmTime(22, 30, null);    // TODO: default bedtime
        dialog.setShowTabs(false);            // hide solar events tab
        dialog.setShowTimePreview(false);           // hide time preview
        dialog.setShowDateSelectButton(false);      // hide date selection
        dialog.setShowTimeZoneSelectButton(false);  // hide time zone selection
        dialog.setShowAlarmListButton(false);       // hide list button
        dialog.setAllowSelectType(false);           // disable type selector
        dialog.setLabelOverride(context.getString(R.string.configLabel_bedtime_alarm_notify));         // override type labels
        dialog.setAlarmType(AlarmClockItem.AlarmType.NOTIFICATION1);    // restrict type to notification

        dialog.setOnAcceptedListener(onAddBedtimeDialogAccept(DIALOG_ADD_BEDTIME, BedtimeSettings.SLOT_BEDTIME_NOTIFY, item));
        dialog.show(fragments, DIALOG_ADD_BEDTIME);
    }

    private final DialogInterface.OnClickListener onAddBedtimeDialogAccept(final String dialogTag, final String slot, final BedtimeItem item)
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
                    //AlarmClockItem alarmItem = BedtimeAlarmHelper.createBedtimeEventItem(getActivity(), item, dialog.getHour(), dialog.getMinute(), dialog.getOffset());
                    //alarmItem.type = dialog.getAlarmType();
                    //alarmItem.location = dialog.getLocation();
                    //Calendar bedtime = Calendar.getInstance();
                    //bedtime.setTimeInMillis(alarmItem.timestamp + alarmItem.offset);

                    configBedtimeToDate(getActivity(), item, dialog.getHour(), dialog.getMinute(), true, true);
                    //scheduleBedtimeAlarmItem(getActivity(), slot, alarmItem, item, true);
                    offerModifyWakeupFromBedtime(getActivity());
                }
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String DIALOG_ADD_ALARM = "dialog_add_alarm";

    protected void showAddAlarmMenu(final Context context, final View v, final BedtimeItem item)
    {
        PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.action_wakeup_set:
                        showAddAlarmDialog(getActivity(), item);
                        return true;

                    case R.id.action_wakeup_from_bedtime:
                        configWakeupFromBedtime(context, item, true);
                        return true;

                    default:
                        return false;
                }
            }
        };
        PopupMenu popup = PopupMenuCompat.createMenu(context, v, R.menu.bedtime_wakeup_add, onMenuItemClickListener, null);
        popup.show();
    }
    protected void updateAddAlarmMenu(final Context context, final View v, final Menu menu)
    {
        MenuItem setFromBedtime = menu.findItem(R.id.action_wakeup_from_bedtime);
        if (setFromBedtime != null) {
            setFromBedtime.setVisible(BedtimeSettings.hasAlarmID(getActivity(), BedtimeSettings.SLOT_BEDTIME_NOTIFY));
        }
    }
    protected void updateEditAlarmMenu(final Context context, final View v, final Menu menu)
    {
        MenuItem bedtimeAlarmOffItem = menu.findItem(R.id.action_bedtime_alarmoff);
        if (bedtimeAlarmOffItem != null) {
            bedtimeAlarmOffItem.setChecked(BedtimeSettings.loadPrefBedtimeAlarmOff(context));
        }
    }

    protected void showAddAlarmDialog(final Context context, BedtimeItem item)
    {
        AlarmCreateDialog dialog = new AlarmCreateDialog();
        FragmentManager fragments = getChildFragmentManager();

        dialog.setShowTimePreview(false);           // hide time preview
        dialog.setShowDateSelectButton(false);      // hide date selection
        dialog.setShowTimeZoneSelectButton(false);  // hide time zone selection
        dialog.setShowAlarmListButton(false);       // hide list button
        dialog.setAllowSelectType(false);           // disable type selector
        dialog.setLabelOverride(context.getString(R.string.configLabel_bedtime_alarm_wakeup));         // override type labels
        dialog.setAlarmType(AlarmClockItem.AlarmType.ALARM);    // restrict type to alarms only
        // TODO: locked/disabled events

        dialog.setOnAcceptedListener(onAddAlarmDialogAccept(DIALOG_ADD_ALARM, BedtimeSettings.SLOT_WAKEUP_ALARM, item));
        dialog.show(fragments, DIALOG_ADD_ALARM);
    }

    private DialogInterface.OnClickListener onAddAlarmDialogAccept(final String dialogTag, final String slot, final BedtimeItem item)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                FragmentManager fragments = getChildFragmentManager();
                final AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(dialogTag);
                if (dialog != null) {
                    onAddAlarmDialogAccept(dialog, slot, item, null);
                }
            }
        };
    }
    protected void onAddAlarmDialogAccept(AlarmCreateDialog dialog, String slot, BedtimeItem item, @Nullable AlarmClockItem alarmItem)
    {
        if (alarmItem == null) {
            alarmItem = BedtimeAlarmHelper.createBedtimeAlarmItem(getActivity(), item, dialog.getHour(), dialog.getMinute(), dialog.getOffset());
        }

        alarmItem.type = dialog.getAlarmType();
        alarmItem.hour = dialog.getHour();
        alarmItem.minute = dialog.getMinute();
        alarmItem.offset = dialog.getOffset();
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

    protected void showEditBedtimeMenu(final Context context, final View v, final BedtimeItem item, final String slotName, final int requestID) {
        showEditBedtimeMenu(context, v, item, slotName, requestID, R.menu.bedtime_edit);
    }
    protected void showEditBedtimeMenu(final Context context, final View v, final BedtimeItem item, final String slotName, final int requestID, int menuResID)
    {
        PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.action_wakeup_edit:
                    case R.id.action_reminder_edit:
                    case R.id.action_bedtime_edit:
                        showAlarmEditActivity(BedtimeSettings.loadAlarmID(getActivity(), slotName), null, requestID, false);
                        return true;

                    case R.id.action_wakeup_delete:
                    case R.id.action_reminder_delete:
                        AlarmEditDialog.confirmDeleteAlarm(context, item.getAlarmItem(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BedtimeAlarmHelper.clearBedtimeItem(getActivity(), slotName);
                            }
                        });
                        return true;

                    case R.id.action_bedtime_delete:
                        AlarmEditDialog.confirmDeleteAlarm(context, item.getAlarmItem(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BedtimeAlarmHelper.clearBedtimeItem(getActivity(), BedtimeSettings.SLOT_BEDTIME_NOTIFY);
                                BedtimeAlarmHelper.clearBedtimeItem(getActivity(), BedtimeSettings.SLOT_BEDTIME_NOTIFYOFF);
                                BedtimeAlarmHelper.clearBedtimeItem(getActivity(), BedtimeSettings.SLOT_BEDTIME_REMINDER);
                            }
                        });
                        return true;

                    case R.id.action_bedtime_set:
                        showAddBedtimeDialog(getActivity(), v, item);
                        return true;

                    case R.id.action_bedtime_from_wakeup:
                        configBedtimeFromWakeup(context, item, false);
                        return true;

                    case R.id.action_bedtime_now:
                        triggerBedtimeNow(context, item);
                        return true;

                    case R.id.action_wakeup_from_bedtime:
                        configWakeupFromBedtime(context, item, true);
                        return true;

                    case R.id.action_bedtime_alarmoff:
                        toggleConfigureBedtimeAlarmOff(context, item);
                        return true;

                    default:
                        return false;
                }
            }
        };
        PopupMenu popup = PopupMenuCompat.createMenu(context, v, menuResID, onMenuItemClickListener, null);
        Menu menu = popup.getMenu();
        updateAddBedtimeMenu(context, v, menu);
        updateAddAlarmMenu(context, v, menu);
        updateEditAlarmMenu(context, v, menu);
        popup.show();
    }

}
