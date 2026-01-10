/**
    Copyright (C) 2020-2023 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.forrestguice.colors.ColorUtils;
import com.forrestguice.suntimeswidget.views.SnackbarUtils;
import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.support.content.ContextCompat;

import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;
import com.forrestguice.support.app.DialogBase;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemExportTask;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemImportTask;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.events.EventIcons;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.widget.ImageViewCompat;
import com.forrestguice.support.widget.LinearLayoutManager;
import com.forrestguice.support.widget.RecyclerView;
import com.forrestguice.support.widget.SwitchCompat;
import com.forrestguice.support.view.ViewCompat;
import com.forrestguice.util.android.AndroidResources;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("Convert2Diamond")
public class AlarmListDialog extends DialogBase
{
    public static final String EXTRA_SELECTED_ROWID = "selectedRowID";

    public static final int REQUEST_IMPORT_URI = 100;
    public static final int REQUEST_EXPORT_URI = 200;

    public static final String DIALOG_IMPORT_WARNING = "importwarning";

    protected View emptyView;
    protected RecyclerView list;
    protected AlarmListDialogAdapter adapter;
    protected ProgressBar progress;
    protected View progressLayout;

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
        View content = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_alarmlist, parent, false);

        progress = (ProgressBar) content.findViewById(R.id.progress);
        progressLayout = content.findViewById(R.id.progressLayout);
        showProgress(false);

        emptyView = content.findViewById(android.R.id.empty);
        emptyView.setOnClickListener(onEmptyViewClick);
        emptyView.setVisibility(View.GONE);

        adapter = new AlarmListDialogAdapter(getActivity());
        adapter.setAdapterListener(adapterListener);

        AppColorValues colors = AppColorValuesCollection.initSelectedColors(getActivity());
        if (colors != null) {
            adapter.getOptions().colors = new AppColorValues(colors);
        }

        list = (RecyclerView) content.findViewById(R.id.recyclerview);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addItemDecoration(new RecyclerView.LastItemBottomMarginDecorator(contextWrapper, adapter, R.dimen.lastitem_margin));
        list.setAdapter(adapter);

        if (savedState != null) {
            loadSettings(savedState);
        }

        reloadAdapter();
        return content;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Bundle bundle)
    {
        if (adapter != null) {
            adapter.setSelectedRowID(bundle.getLong(EXTRA_SELECTED_ROWID, -1));
        }
    }

    protected void saveSettings(Bundle bundle)
    {
        if (adapter != null) {
            bundle.putLong(EXTRA_SELECTED_ROWID, adapter.getSelectedRowID());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_EXPORT_URI:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        exportAlarms(getActivity(), uri);
                    }
                }
                break;

            case REQUEST_IMPORT_URI:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        importAlarms(getActivity(), uri);
                    }
                }
                break;
        }
    }

    protected void showProgress(boolean visible)
    {
        if (progress != null) {
            progress.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (progressLayout != null) {
            progressLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.alarmlist, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        MenuItem selectedItem;
        switch (AlarmSettings.loadPrefAlarmSort(getActivity())) {
            case AlarmSettings.SORT_BY_ALARMTIME: selectedItem = menu.findItem(R.id.sortByAlarmTime); break;
            case AlarmSettings.SORT_BY_CREATION: default: selectedItem = menu.findItem(R.id.sortByCreation); break;
        }
        if (selectedItem != null) {
            selectedItem.setChecked(true);
        }

        MenuItem enabledFirst = menu.findItem(R.id.sortEnabledFirst);
        if (enabledFirst != null) {
            enabledFirst.setChecked(AlarmSettings.loadPrefAlarmSortEnabledFirst(getActivity()));
        }

        MenuItem showOffset = menu.findItem(R.id.showOffset);
        if (showOffset != null) {
            showOffset.setChecked(AlarmSettings.loadPrefAlarmSortShowOffset(getActivity()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Activity activity = getActivity();
        int itemId = item.getItemId();
        if (itemId == R.id.sortByAlarmTime) {
            AlarmSettings.savePrefAlarmSort(getActivity(), AlarmSettings.SORT_BY_ALARMTIME);
            if (Build.VERSION.SDK_INT >= 11) {
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }  // else { TODO }
            adapter.sortItems();
            return true;

        } else if (itemId == R.id.sortByCreation) {
            AlarmSettings.savePrefAlarmSort(getActivity(), AlarmSettings.SORT_BY_CREATION);
            if (Build.VERSION.SDK_INT >= 11) {
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }  // else { TODO }
            adapter.sortItems();
            return true;

        } else if (itemId == R.id.sortEnabledFirst) {
            AlarmSettings.savePrefAlarmSortEnabledFirst(getActivity(), !item.isChecked());
            if (Build.VERSION.SDK_INT >= 11) {
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }  // else { TODO }
            adapter.sortItems();
            return true;

        } else if (itemId == R.id.showOffset) {
            AlarmSettings.savePrefAlarmSortShowOffset(getActivity(), !item.isChecked());
            if (Build.VERSION.SDK_INT >= 11) {
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }  // else { TODO }
            adapter.sortItems();
            return true;

        } else if (itemId == R.id.action_clear) {
            if (activity != null) {
                confirmClearAlarms(activity);
            }
            return true;

        } else if (itemId == R.id.action_export) {
            if (activity != null) {
                exportAlarms(activity);
            }
            return true;

        } else if (itemId == R.id.action_import) {
            if (activity != null) {
                importAlarms(activity);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected View.OnClickListener onEmptyViewClick = null;
    protected void setOnEmptyViewClick( View.OnClickListener listener )
    {
        onEmptyViewClick = listener;
        if (emptyView != null) {
            emptyView.setOnClickListener(onEmptyViewClick);
        }
    }

    public RecyclerView getList() {
        return list;
    }

    public AlarmListDialogAdapter getAdapter() {
        return adapter;
    }

    public void setSelectedRowID(long value) {
        adapter.setSelectedRowID(value);
    }

    public long getSelectedRowID() {
        return ((adapter != null) ? adapter.getSelectedRowID() : -1);
    }

    public void scrollToSelectedItem()
    {
        int position = adapter.getSelectedIndex();
        if (position != -1) {
            list.scrollToPosition(position);
        }
    }

    public void clearSelection() {
        adapter.clearSelection();
    }

    public void notifyAlarmUpdated(long rowID) {
        reloadAdapter(rowID);
    }

    public void notifyAlarmDeleted(long rowID)
    {
        if (listener != null) {
            listener.onAlarmDeleted(rowID);
        }
        offerUndoDeleteAlarm(getActivity(), adapter.getItem(rowID));
        adapter.removeItem(rowID);
        updateViews();
    }

    public void notifyAlarmsCleared()
    {
        if (listener != null) {
            listener.onAlarmsCleared();
        }
        offerUndoClearAlarms(getActivity(), adapter.getItems());   // pass the (now stale) items to undo
        reloadAdapter();                                           // and reload adapter (now cleared)
    }

    protected static DialogInterface.OnClickListener onDeleteConfirmed(final Context context, final AlarmClockItem item) {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                context.sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DELETE, item.getUri()));
            }
        };
    }
    protected static DialogInterface.OnClickListener onClearAlarmsConfirmed(final Context context) {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                context.sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DELETE, null));
            }
        };
    }

    public static void confirmClearAlarms(final Context context)
    {
        int[] attrs = { R.attr.icActionDelete };
        TypedArray a = context.obtainStyledAttributes(attrs);
        int iconResID = a.getResourceId(0, R.drawable.ic_action_discard);
        a.recycle();

        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.clearalarms_dialog_title))
                .setMessage(context.getString(R.string.clearalarms_dialog_message))
                .setIcon(iconResID)
                .setPositiveButton(context.getString(R.string.clearalarms_dialog_ok), onClearAlarmsConfirmed(context))
                .setNegativeButton(context.getString(R.string.clearalarms_dialog_cancel), null);
        confirm.show();
    }

    @SuppressLint("WrongConstant")
    public void offerUndoClearAlarms(Context context, final List<AlarmClockItem> items)
    {
        View view = getView();
        if (context != null && view != null)
        {
            SnackbarUtils.make(context, view, context.getString(R.string.clearalarms_toast_success), SnackbarUtils.LENGTH_INDEFINITE)
                    .setAction(context.getString(R.string.configAction_undo), new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Context context = getActivity();
                    if (context != null) {
                        addAlarm(context, items.toArray(new AlarmClockItem[0]));
                    }
                }
            }).setDuration(UNDO_DELETE_MILLIS).show();
        }
    }

    @SuppressLint("WrongConstant")
    public void offerUndoDeleteAlarm(Context context, final AlarmClockItem deletedItem)
    {
        View view = getView();
        if (context != null && view != null && deletedItem != null)
        {
            SnackbarUtils.make(context, view, context.getString(R.string.deletealarm_toast_success1, deletedItem.type.getDisplayString()), SnackbarUtils.LENGTH_INDEFINITE)
                    .setAction(context.getString(R.string.configAction_undo), new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Context context = getActivity();
                    if (context != null && deletedItem != null) {
                        addAlarm(getActivity(), deletedItem);
                    }
                }
            }).setDuration(UNDO_DELETE_MILLIS).show();
        }
    }
    public static final int UNDO_DELETE_MILLIS = 8000;

    public AlarmClockItem createAlarm(final Context context, AlarmClockItem.AlarmType type, String label, String event, Location location, long date, int hour, int minute, String timezone, boolean vibrate, Uri ringtoneUri, String ringtoneName, ArrayList<Integer> repetitionDays, boolean addToDatabase)
    {
        final AlarmClockItem alarm = createAlarm(context, type, label, event, location, date, hour, minute, timezone, vibrate, ringtoneUri, ringtoneName, repetitionDays);
        if (addToDatabase) {
            addAlarm(context, alarm);
        }
        return alarm;
    }

    public static AlarmClockItem createAlarm(final Context context, AlarmClockItem.AlarmType type, String label, @NonNull String event, @NonNull Location location) {
        return createAlarm(context, type, label, event, location, -1L, -1, -1, null, AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, type), AlarmSettings.getDefaultRingtoneName(context, type), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
    }

    public static AlarmClockItem createAlarm(final Context context, AlarmClockItem.AlarmType type, String label, String event, Location location, long date, int hour, int minute, String timezone, boolean vibrate, Uri ringtoneUri, String ringtoneName, ArrayList<Integer> repetitionDays)
    {
        //Log.d("DEBUG", "createAlarm: ringToneURI: " + ringtoneUri + " (" + ringtoneName + ")" );
        final AlarmClockItem alarm = new AlarmClockItem();
        alarm.enabled = AlarmSettings.loadPrefAlarmAutoEnable(context);
        alarm.type = type;
        alarm.label = label;
        alarm.hour = hour;
        alarm.minute = minute;
        alarm.timezone = timezone;
        //Log.d("DEBUG", "createAlarm: with hour " + hour + " and minute " + minute + " .. timezone " + timezone);
        alarm.setEvent(date != -1L ? EventUri.getEventInfoUri(EventUri.AUTHORITY(), Long.toString(date)) : event);   // TODO: event on date
        alarm.location = (location != null ? location : WidgetSettings.loadLocationPref(context, 0));
        alarm.repeating = false;
        alarm.repeatingDays = new ArrayList<>(repetitionDays);
        alarm.vibrate = vibrate;

        alarm.ringtoneURI = (ringtoneUri != null ? ringtoneUri.toString() : null);
        if (alarm.ringtoneURI != null)
        {
            if (alarm.ringtoneURI.equals(AlarmSettings.VALUE_RINGTONE_DEFAULT)) {
                alarm.ringtoneURI = AlarmSettings.getDefaultRingtoneUri(context, type).toString();
                alarm.ringtoneName = AlarmSettings.getDefaultRingtoneName(context, type);
            } else {
                alarm.ringtoneName = ringtoneName;
            }
        }

        alarm.setState(alarm.enabled ? AlarmState.STATE_NONE : AlarmState.STATE_DISABLED);
        alarm.modified = true;
        return alarm;
    }

    /**
     * Add AlarmClockItem(s) to the alarms database.
     * @param items an array of one or more AlarmClockItem
     */
    public void addAlarm(final Context context, AlarmClockItem... items) {
        addAlarm(context, null, items);
    }
    public void addAlarm(final Context context, final @Nullable AlarmDatabaseAdapter.AlarmItemTaskListener l, AlarmClockItem... items)
    {
        AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, true, true);
        task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
        {
            @Override
            public void onFinished(Boolean result, AlarmClockItem[] items)
            {
                if (result)
                {
                    for (AlarmClockItem item : items)
                    {
                        if (listener != null) {
                            listener.onAlarmAdded(item);
                        }
                        if (item.enabled) {
                            context.sendBroadcast( AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri()) );
                        }
                    }
                    setSelectedRowID((items.length == 1) ? items[0].rowID : -1L);
                    reloadAdapter();
                }
                if (l != null) {
                    l.onFinished(result, items);
                }
            }
        });
        task.execute(items);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean exportAlarms(Context context)
    {
        if (exportTask != null && importTask != null) {
            Log.e("ExportAlarms", "Already busy importing/exporting! ignoring request");
            return false;
        }

        String exportTarget = "SuntimesAlarms";
        AlarmListDialogAdapter adapter = getAdapter();
        if (context != null && adapter != null)
        {
            AlarmClockItem[] items = getItemsForExport();
            if (items.length > 0)
            {
                if (Build.VERSION.SDK_INT >= 19)
                {
                    String filename = exportTarget + AlarmClockItemExportTask.FILEEXT;
                    Intent intent = ExportTask.getCreateFileIntent(filename, AlarmClockItemExportTask.MIMETYPE);
                    try {
                        startActivityForResult(intent, REQUEST_EXPORT_URI);
                        return true;

                    } catch (ActivityNotFoundException e) {
                        Log.e("ExportAlarms", "SAF is unavailable? (" + e + ").. falling back to legacy export method.");
                    }
                }
                exportTask = new AlarmClockItemExportTask(context, exportTarget, true, true);    // export to external cache
                exportTask.setItems(items);
                exportTask.setTaskListener(exportListener);
                exportTask.execute();
                return true;
            } else return false;
        }
        return false;
    }

    protected void exportAlarms(Context context, @NonNull Uri uri)
    {
        if (exportTask != null && importTask != null) {
            Log.e("ExportAlarms", "Already busy importing/exporting! ignoring request");

        } else {
            AlarmClockItem[] items = getItemsForExport();
            if (items.length > 0)
            {
                exportTask = new AlarmClockItemExportTask(context, uri);    // export directly to uri
                exportTask.setItems(items);
                exportTask.setTaskListener(exportListener);
                exportTask.execute();
            }
        }
    }

    protected AlarmClockItem[] getItemsForExport()
    {
        List<AlarmClockItem> itemList = adapter.getItems();
        AlarmListDialogAdapter.sortItems(itemList, AlarmSettings.SORT_BY_CREATION, false);   // list is displayed youngest -> oldest
        Collections.reverse(itemList);                                                // should be reversed for export (so import encounters/adds older items first)
        return itemList.toArray(new AlarmClockItem[0]);
    }

    protected AlarmClockItemExportTask exportTask = null;
    private final ExportTask.TaskListener exportListener = new ExportTask.TaskListener()
    {
        public void onStarted()
        {
            setRetainInstance(true);
            showProgress(true);
        }

        @Override
        public void onFinished(AlarmClockItemExportTask.ExportResult results)
        {
            setRetainInstance(false);
            exportTask = null;
            showProgress(false);

            Context context = getActivity();
            if (context != null)
            {
                File file = results.getExportFile();
                String path = ((file != null) ? file.getAbsolutePath() : ExportTask.getFileName(context.getContentResolver(), results.getExportUri()));

                if (results.getResult())
                {
                    if (isAdded()) {
                        String successMessage = getString(R.string.msg_export_success, path);
                        Toast.makeText(getActivity(), successMessage, Toast.LENGTH_LONG).show();
                        // TODO: use a snackbar instead; offer 'copy path' action
                    }

                    if (Build.VERSION.SDK_INT >= 19) {
                        if (results.getExportUri() == null) {
                            ExportTask.shareResult(getActivity(), results.getExportFile(), results.getMimeType());
                        }
                    } else {
                        ExportTask.shareResult(getActivity(), results.getExportFile(), results.getMimeType());
                    }
                    return;
                }

                if (isAdded()) {
                    String failureMessage = getString(R.string.msg_export_failure, path);
                    Toast.makeText(getActivity(), failureMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface ImportFragment
    {
        void startActivityForResult(Intent intent, int request);
    }

    public void importAlarms(final Context context)
    {
        if (importTask != null && exportTask != null) {
            Log.e("ImportAlarms", "Already busy importing/exporting! ignoring request");
            return;
        }
        ImportFragment fragment = new ImportFragment() {
            @Override
            public void startActivityForResult(Intent intent, int request) {
                AlarmListDialog.this.startActivityForResult(intent, request);
            }
        };
        importAlarms(fragment, context, getLayoutInflater(), REQUEST_IMPORT_URI);
    }

    public static void importAlarms(final ImportFragment fragment, final Context context, LayoutInflater layoutInflater, final int request)
    {
        DialogInterface.OnClickListener onWarningAcknowledged = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Intent intent = ExportTask.getOpenFileIntent(AlarmClockItemExportTask.MIMETYPE);
                    fragment.startActivityForResult(intent, request);
                } catch (Exception e) {
                    Log.e("ImportAlarms", "Failed to start activity! " + e);
                }
            }
        };
        if (!AppSettings.checkDialogDoNotShowAgain(context, DIALOG_IMPORT_WARNING)) {
            AppSettings.buildAlertDialog(DIALOG_IMPORT_WARNING, layoutInflater,
                    R.drawable.ic_action_warning, context.getString(android.R.string.dialog_alert_title),
                    context.getString(R.string.importalarms_msg_warning), onWarningAcknowledged).show();
        } else onWarningAcknowledged.onClick(null, DialogInterface.BUTTON_POSITIVE);
    }

    protected void importAlarms(final Context context, @NonNull Uri uri)
    {
        if (importTask != null && exportTask != null) {
            Log.e("ImportAlarms", "Already busy importing/exporting! ignoring request");

        } else if (context != null) {
            importTask = new AlarmClockItemImportTask(context);
            importTask.setTaskListener(importListener);
            importTask.execute(uri);
        }
    }

    protected AlarmClockItemImportTask importTask = null;
    private final AlarmClockItemImportTask.TaskListener importListener =  new AlarmClockItemImportTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            setRetainInstance(true);
            showProgress(true);
        }

        @Override
        public void onFinished(AlarmClockItemImportTask.TaskResult result)
        {
            setRetainInstance(false);
            importTask = null;
            showProgress(false);

            if (result.getResult())
            {
                AlarmClockItem[] items = result.getItems();
                addAlarm(getActivity(), new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    @Override
                    public void onFinished(Boolean result, @Nullable AlarmClockItem[] items) {
                        if (isAdded() && items != null) {
                            offerUndoImport(getActivity(), new ArrayList<AlarmClockItem>(Arrays.asList(items)));
                        }
                    }
                }, items);

                /*if (isAdded()) {
                    String successMessage = getString(R.string.msg_import_success, result.getUri().toString());
                    Toast.makeText(getActivity(), successMessage, Toast.LENGTH_LONG).show();
                }*/
                return;    // finished import

            } else {
                if (isAdded())
                {
                    Uri uri = result.getUri();   // import failed
                    String path = ((uri != null) ? uri.toString() : "<path>");
                    String failureMessage = getString(R.string.msg_import_failure, path);
                    Toast.makeText(getActivity(), failureMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @SuppressLint("WrongConstant")
    public void offerUndoImport(Context context, final List<AlarmClockItem> items)
    {
        View view = getView();
        if (context != null && view != null)
        {
            String plural = context.getResources().getQuantityString(R.plurals.alarmPlural, items.size(), items.size());
            SnackbarUtils.make(context, view, context.getString(R.string.importalarms_toast_success, plural), SnackbarUtils.LENGTH_INDEFINITE)
                    .setAction(context.getString(R.string.configAction_undo), new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Context context = getActivity();
                    if (context != null) {
                        for (AlarmClockItem item : items) {
                            if (item != null) {
                                context.sendBroadcast(AlarmNotifications.getAlarmIntent(getActivity(), AlarmNotifications.ACTION_DELETE, item.getUri()));
                            }
                        }
                    }
                }
            }).setDuration(UNDO_IMPORT_MILLIS).show();
        }
    }
    public static final int UNDO_IMPORT_MILLIS = 8000;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void reloadAdapter() {
        reloadAdapter(null, onListLoaded);
    }
    public void reloadAdapter(Long rowId) {
        reloadAdapter(rowId, onItemChanged);
    }
    public void reloadAdapter(Long rowId, AlarmListTask.AlarmListTaskListener taskListener)
    {
        AlarmListTask listTask = new AlarmListTask(getActivity());
        listTask.setTaskListener(taskListener);
        listTask.execute(rowId);
        //Log.d("DEBUG", "reloadAdapter");
    }

    protected AlarmListTask.AlarmListTaskListener onListLoaded = new AlarmListTask.AlarmListTaskListener() {
        @Override
        public void onLoadFinished(List<AlarmClockItem> data)
        {
            //Log.d("DEBUG", "onListLoaded: " + data.size());
            adapter.setItems(data);
            updateViews();
            scrollToSelectedItem();
        }
    };

    protected AlarmListTask.AlarmListTaskListener onItemChanged = new AlarmListTask.AlarmListTaskListener() {
        @Override
        public void onLoadFinished(List<AlarmClockItem> data)
        {
            if (data.size() > 0)
            {
                AlarmClockItem item = data.get(0);
                if (item != null)
                {
                    //Log.d("DEBUG", "onItemChanged: " + item.rowID + ", state: " + item.state.getState());
                    switch(item.getState())
                    {
                        case AlarmState.STATE_SOUNDING: case AlarmState.STATE_SNOOZING: case AlarmState.STATE_TIMEOUT:  // sounding/snoozing/timeout alarmtime shouldn't be touched until next transition
                        case AlarmState.STATE_SCHEDULED_SOON: case AlarmState.STATE_SCHEDULED_DISTANT:                  // scheduled_ alarmtime is already assigned
                            break;
                        default:
                            //Log.d("DEBUG", "onItemChanged: updating item timestamp");
                            AlarmNotifications.updateAlarmTime(getActivity(), item);
                            break;
                    }
                    adapter.setItem(item);
                }
            }
            updateViews();
            scrollToSelectedItem();
        }
    };

    protected void updateViews()
    {
        emptyView.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        list.setVisibility(adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * AlarmClockListTask
     */
    public static class AlarmListTask extends AsyncTask<Long, AlarmClockItem, List<AlarmClockItem>>
    {
        private final AlarmDatabaseAdapter db;
        private final WeakReference<Context> contextRef;

        private boolean option_includeState = true;
        public void setOption_includeState(boolean value) {
            option_includeState = value;
        }

        public AlarmListTask(Context context)
        {
            contextRef = new WeakReference<>(context);
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected List<AlarmClockItem> doInBackground(Long... rowIds)
        {
            ArrayList<AlarmClockItem> items = new ArrayList<>();
            db.open();
            Cursor cursor = (rowIds == null || rowIds.length <= 0 || rowIds[0] == null)
                          ? db.getAllAlarms(0, true) : db.getAlarm(rowIds[0]);

            while (!cursor.isAfterLast())
            {
                ContentValues entryValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, entryValues);

                AlarmClockItem item = new AlarmClockItem(contextRef.get(), entryValues);
                if (!item.enabled) {
                    AlarmNotifications.updateAlarmTime(contextRef.get(), item);
                }

                if (option_includeState)
                {
                    Cursor cursor1 = db.getAlarmState(item.rowID);
                    if (cursor1 != null) {
                        cursor1.moveToFirst();
                        if (!cursor1.isAfterLast()) {
                            ContentValues stateValues = new ContentValues();
                            DatabaseUtils.cursorRowToContentValues(cursor1, stateValues);
                            item.state = new AlarmState(stateValues);
                        }
                        cursor1.close();
                    }
                }

                items.add(item);
                publishProgress(item);

                cursor.moveToNext();
            }
            cursor.close();
            db.releaseUnusedUriPermissions(contextRef.get());
            db.close();
            return items;
        }

        @Override
        protected void onProgressUpdate(AlarmClockItem... item) {}

        @Override
        protected void onPostExecute(List<AlarmClockItem> result)
        {
            if (result != null)
            {
                if (taskListener != null) {
                    taskListener.onLoadFinished(result);
                }
            }
        }

        protected AlarmListTaskListener taskListener;
        public void setTaskListener( AlarmListTaskListener l )
        {
            taskListener = l;
        }

        public static abstract class AlarmListTaskListener
        {
            public void onLoadFinished(List<AlarmClockItem> result) {}
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * RecyclerView.Adapter
     */
    public static class AlarmListDialogAdapter extends RecyclerView.Adapter<AlarmListDialogItem>
    {
        protected long selectedRowID = -1;
        protected ArrayList<AlarmClockItem> items = new ArrayList<>();
        protected WeakReference<Context> contextRef;

        public AlarmListDialogAdapter(Context context) {
            super();
            contextRef = new WeakReference<>(context);
            setHasStableIds(true);
            initOptions(context);
        }

        protected AlarmListDialogOptions options;
        public void initOptions(Context context) {
            options = new AlarmListDialogOptions(context);
        }
        public AlarmListDialogOptions getOptions() {
            return options;
        }

        public void setSelectedRowID(long rowID) {
            //Log.d("setSelectedRowID", ""+ rowID);
            selectedRowID = rowID;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onItemSelected(selectedRowID);
            }
        }
        public long getSelectedRowID() {
            return selectedRowID;
        }

        public void setSelectedIndex(int index) {
            AlarmClockItem item = items.get(index);
            setSelectedRowID(item.rowID);
        }
        public int getSelectedIndex() {
            return getIndex(selectedRowID);
        }
        public int getIndex( long rowID )
        {
            for (int i=0; i<items.size(); i++)
            {
                AlarmClockItem item = items.get(i);
                if (item != null && item.rowID == rowID) {
                    return i;
                }
            }
            return -1;
        }

        public void clearSelection()
        {
            int selectedIndex = getSelectedIndex();
            selectedRowID = -1;
            if (selectedIndex != -1) {
                //notifyItemChanged(selectedIndex);
                notifyDataSetChanged();   // notifyItemChanged also resets the running animation
            }
            if (listener != null) {
                listener.onItemSelected(selectedRowID);
            }
        }

        public void setItems(List<AlarmClockItem> values)
        {
            items.clear();
            items.addAll(sortItems(values));
            notifyDataSetChanged();
        }

        public void setItem(@NonNull AlarmClockItem item)
        {
            int position = getIndex(item.rowID);
            if (position >= 0 && position < items.size())
            {
                items.add(position, item);
                AlarmClockItem previous = items.remove(position + 1);
                sortItems();

                /*if (item.timestamp != previous.timestamp) {
                    Log.d("DEBUG", "setItem: timestamp changed: " + previous.timestamp + " -> " + item.timestamp);
                    sortItems();
                } else {
                    Log.d("DEBUG", "setItem: position changed");
                    notifyItemChanged(position);
                }*/

            } else {
                items.add(0, item);
                sortItems();
            }
        }

        public void clearItems() {
            items.clear();
            notifyDataSetChanged();
        }

        public void removeItem(long alarmID)
        {
            int position = getIndex(alarmID);
            if (position >= 0 && position < items.size()) {
                items.remove(position);
                notifyItemRemoved(position);
            }
        }

        public AlarmClockItem getItem(long rowID)
        {
            for (int i=0; i<items.size(); i++)
            {
                AlarmClockItem item = items.get(i);
                if (item != null && item.rowID == rowID) {
                    return item;
                }
            }
            return null;
        }

        public List<AlarmClockItem> getItems() {
            return new ArrayList<>(items);
        }

        public void sortItems()
        {
            sortItems(items);
            notifyDataSetChanged();
        }

        protected List<AlarmClockItem> sortItems(List<AlarmClockItem> items)
        {
            sortItems(items, AlarmSettings.loadPrefAlarmSort(contextRef.get()), AlarmSettings.loadPrefAlarmSortEnabledFirst(contextRef.get()));
            return items;
        }

        public static List<AlarmClockItem> sortItems(List<AlarmClockItem> items, final int sortMode, final boolean enabledFirst)
        {
            final long now = Calendar.getInstance().getTimeInMillis();
            switch (sortMode)
            {
                case AlarmSettings.SORT_BY_ALARMTIME:    // nearest alarm time first
                    Collections.sort(items, new Comparator<AlarmClockItem>() {
                        @Override
                        public int compare(AlarmClockItem o1, AlarmClockItem o2)
                        {
                            if (enabledFirst) {
                                return (o1.enabled && !o2.enabled) ? -1
                                        : (!o1.enabled && o2.enabled) ? 1
                                        : compareLong((o1.timestamp + o1.offset) - now, (o2.timestamp + o2.offset) - now);
                            } else return compareLong((o1.timestamp + o1.offset) - now, (o2.timestamp + o2.offset) - now);
                        }
                    });
                    break;

                case AlarmSettings.SORT_BY_CREATION:    // newest items first
                default:
                    Collections.sort(items, new Comparator<AlarmClockItem>() {
                        @Override
                        public int compare(AlarmClockItem o1, AlarmClockItem o2)
                        {
                            if (enabledFirst) {
                                return (o1.enabled && !o2.enabled) ? -1
                                        : (!o1.enabled && o2.enabled) ? 1
                                        : compareLong(o2.rowID, o1.rowID);
                            } else return compareLong(o2.rowID, o1.rowID);
                        }
                    });
                    break;
            }
            return items;
        }

        @SuppressWarnings("UseCompareMethod")
        static int compareLong(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);    // copied from Long.compare to support api < 19
        }

        @Override
        public long getItemId( int position ) {
            return (position >= 0 && position < items.size()) ? items.get(position).rowID : 0;
        }

        @Override
        @NonNull
        public AlarmListDialogItem onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.layout_listitem_alarmclock2, parent, false);
            return new AlarmListDialogItem(view);
        }

        @Override
        public void onBindViewHolder(AlarmListDialogItem holder, int position)
        {
            Context context = contextRef.get();

            //Log.d("DEBUG", "onBindViewHolder: " + holder);
            AlarmClockItem item = items.get(position);
            holder.isSelected = (item.rowID == selectedRowID);
            holder.preview_offset = !holder.isSelected;
            ViewCompat.setTransitionName(holder.text_datetime, "transition_" + item.rowID);

            if (AlarmSettings.loadPrefAlarmSortShowOffset(context)) {
                holder.preview_offset = false;
            }

            detachClickListeners(holder);
            holder.bindData(context, item, options);
            holder.startBackgroundAnimation(context);
            attachClickListeners(holder, item.rowID);
        }

        @Override
        public void onViewRecycled(@NonNull AlarmListDialogItem holder)
        {
            //Log.d("DEBUG", "onViewRecycled: " + holder);
            detachClickListeners(holder);
            holder.isSelected = false;
            holder.resetBackground();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull AlarmListDialogItem holder)
        {
            super.onViewAttachedToWindow(holder);
            //Log.d("DEBUG", "onViewAttachedToWindow: " + holder);
            holder.startBackgroundAnimation(contextRef.get());
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull AlarmListDialogItem holder)
        {
            super.onViewDetachedFromWindow(holder);
            //Log.d("DEBUG", "onViewDetachedFromWindow: " + holder);
            holder.stopBackgroundAnimation(contextRef.get());
        }

        private void attachClickListeners(@NonNull final AlarmListDialogItem holder, final long rowId)
        {
            if (holder.card != null) {
                holder.card.setOnClickListener(itemClickListener(rowId, holder));
            }
            if (holder.overflow != null) {
                holder.overflow.setOnClickListener(overflowMenuListener(rowId));
            }
            if (holder.typeButton != null) {
                holder.typeButton.setOnClickListener(typeMenuListener(rowId, holder.typeButton));
            }
            if (holder.button_delete != null) {
                holder.button_delete.setOnClickListener(deleteButtonListener(rowId));
            }
            if (holder.button_dismiss != null) {
                holder.button_dismiss.setOnClickListener(dismissButtonListener(rowId));
            }
            if (holder.button_snooze != null) {
                holder.button_snooze.setOnClickListener(snoozeButtonListener(rowId));
            }
            if (holder.text_note != null) {
                holder.text_note.setOnClickListener(noteListener(rowId, holder));
            }

            if (Build.VERSION.SDK_INT >= 14) {
                if (holder.switch_enabled != null) {
                    holder.switch_enabled.setOnCheckedChangeListener(alarmEnabledListener(rowId));
                }
            } else {
                if (holder.check_enabled != null) {
                    holder.check_enabled.setOnCheckedChangeListener(alarmEnabledListener(rowId));
                }
            }
        }

        private void detachClickListeners(@NonNull AlarmListDialogItem holder)
        {
            if (holder.card != null) {
                holder.card.setOnClickListener(null);
                holder.card.setOnLongClickListener(null);
            }
            if (holder.overflow != null) {
                holder.overflow.setOnClickListener(null);
            }
            if (holder.typeButton != null) {
                holder.typeButton.setOnClickListener(null);
            }
            if (holder.button_delete != null) {
                holder.button_delete.setOnClickListener(null);
            }
            if (holder.button_dismiss != null) {
                holder.button_dismiss.setOnClickListener(null);
            }
            if (holder.button_snooze != null) {
                holder.button_snooze.setOnClickListener(null);
            }
            if (holder.text_note != null) {
                holder.text_note.setOnClickListener(null);
            }

            if (Build.VERSION.SDK_INT >= 14) {
                if (holder.switch_enabled != null) {
                    holder.switch_enabled.setOnCheckedChangeListener(null);
                }
            } else {
                if (holder.check_enabled != null) {
                    holder.check_enabled.setOnCheckedChangeListener(null);
                }
            }
        }

        private View.OnClickListener itemClickListener(final long rowId, final AlarmListDialogItem holder)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    AlarmClockItem item = getItem(rowId);
                    if (listener != null && item != null) {
                        listener.onItemClicked(item, holder);
                    }
                    setSelectedRowID(rowId);
                }
            };
        }

        private View.OnLongClickListener itemLongClickListener(final long rowId)
        {
            return new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    setSelectedRowID(rowId);
                    AlarmClockItem item = getItem(rowId);
                    if (listener != null && item != null) {
                        return listener.onItemLongClicked(item);
                    } else return true;
                }
            };
        }

        private View.OnClickListener overflowMenuListener(final long rowId)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedRowID(rowId);
                    Context context = contextRef.get();
                    if (context != null) {
                        showOverflowMenu(context, rowId, v);
                    }
                }
            };
        }

        private View.OnClickListener deleteButtonListener(final long rowId)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context context = contextRef.get();
                    AlarmClockItem item = getItem(rowId);
                    if (context != null && item != null) {
                        AlarmEditDialog.confirmDeleteAlarm(context, item, onDeleteConfirmed(context, item));
                    }
                }
            };
        }

        private View.OnClickListener dismissButtonListener(final long rowId)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    if (context != null)
                    {
                        AlarmClockItem alarm = getItem(rowId);
                        if (alarm != null && alarm.hasDismissChallenge(context)) {
                            context.startActivity(AlarmNotifications.getFullscreenIntent(context, alarm.getUri()).setAction(AlarmDismissActivity.ACTION_DISMISS));
                        } else {
                            context.sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISMISS, AlarmClockItem.getUri(rowId)));
                        }
                    }
                }
            };
        }

        private View.OnClickListener snoozeButtonListener(final long rowId)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    if (context != null) {
                        context.sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SNOOZE, AlarmClockItem.getUri(rowId)));
                    }
                }
            };
        }

        private View.OnClickListener noteListener(final long rowId, final AlarmListDialogItem view)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    AlarmClockItem item = getItem(rowId);
                    if (listener != null && item != null) {
                        listener.onItemNoteClicked(item, view);
                    }
                }
            };
        }

        private View.OnClickListener editButtonListener(final long rowId, final AlarmListDialogItem holder)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedRowID(rowId);
                    AlarmClockItem item = getItem(rowId);
                    if (listener != null && item != null) {
                        listener.onItemClicked(item, holder);
                    }
                }
            };
        }

        private View.OnClickListener typeMenuListener(final long rowId, View v)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (rowId == getSelectedRowID())
                    {
                        Context context = contextRef.get();
                        AlarmClockItem item = getItem(rowId);
                        if (context != null && item != null && !item.enabled) {
                            showAlarmTypeMenu(context, rowId, v);
                        }
                    } else setSelectedRowID(rowId);
                }
            };
        }

        private CompoundButton.OnCheckedChangeListener alarmEnabledListener(final long rowId)
        {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    setSelectedRowID(rowId);
                    Context context = contextRef.get();
                    if (context != null) {
                        enableAlarm(context, rowId, isChecked);
                    }
                }
            };
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        protected AdapterListener listener;
        public void setAdapterListener( AdapterListener listener ) {
            this.listener = listener;
        }

        ///

        protected void showOverflowMenu(final Context context, final long rowId, final View buttonView)
        {
            PopupMenuCompat.createMenu(context, buttonView, R.menu.alarmcontext1, new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
            {
                @Override
                public void onUpdateMenu(Context context, Menu menu) {
                }

                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    if (menuItem.getItemId() == R.id.action_delete) {
                        AlarmClockItem item = getItem(rowId);
                        if (item != null) {
                            AlarmEditDialog.confirmDeleteAlarm(context, item, onDeleteConfirmed(context, item));
                        }
                        return true;
                    }
                    return false;
                }
            })).show();
        }

        protected void showAlarmTypeMenu(final Context context, final long rowId, final View buttonView)
        {
            PopupMenuCompat.createMenu(context, buttonView, R.menu.alarmtype, new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
            {
                @Override
                public void onUpdateMenu(Context context, Menu menu) {
                }

                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.alarmTypeNotification) {
                        return changeAlarmType(context, rowId, AlarmClockItem.AlarmType.NOTIFICATION);

                    } else if (itemId == R.id.alarmTypeNotification1) {
                        return changeAlarmType(context, rowId, AlarmClockItem.AlarmType.NOTIFICATION1);

                    } else if (itemId == R.id.alarmTypeNotification2) {
                        return changeAlarmType(context, rowId, AlarmClockItem.AlarmType.NOTIFICATION2);
                    }
                    return changeAlarmType(context, rowId, AlarmClockItem.AlarmType.ALARM);
                }
            })).show();
        }

        protected boolean changeAlarmType(Context context, final long rowId, AlarmClockItem.AlarmType type)
        {
            AlarmClockItem item = getItem(rowId);
            if (item != null && item.type != type)
            {
                //Log.d("AlarmList", "alarmTypeMenu: alarm type is changed: " + type);
                if (item.enabled)
                {
                    //Log.d("AlarmList", "alarmTypeMenu: alarm is enabled (reschedule required?)");
                    // item is enabled; disable it or reschedule/reenable
                    return false;

                } else {
                    //Log.d("AlarmList", "alarmTypeMenu: alarm is disabled, changing its type..");
                    item.type = type;
                    item.setState(AlarmState.STATE_NONE);

                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                    task.setTaskListener(changeAlarmTypeTaskListener(rowId));
                    task.execute(item);
                    return true;
                }
            }
            Log.w("AlarmList", "alarmTypeMenu: alarm type is unchanged");
            return false;
        }
        private AlarmDatabaseAdapter.AlarmItemTaskListener changeAlarmTypeTaskListener(final long rowId)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item) {
                    notifyItemChanged(getIndex(rowId));
                }
            };
        }

        public void enableAlarm(final Context context, final long rowId, final boolean enabled)
        {
            AlarmClockItem item = getItem(rowId);
            if (item == null) {
                Log.w("AlarmListDialog", "enableAlarm: null item!");
                return;
            }

            item.alarmtime = 0;
            item.enabled = enabled;
            item.modified = true;

            AlarmDatabaseAdapter.AlarmUpdateTask enableTask = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, false);
            enableTask.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (result && item != null) {
                        context.sendBroadcast( enabled ? AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri())
                                                       : AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISABLE, item.getUri()) );
                        if (!enabled) {
                            AlarmNotifications.updateAlarmTime(context, item);
                        }
                        notifyItemChanged(getIndex(rowId));

                        if (listener != null) {
                            listener.onAlarmToggled(item, enabled);
                        }

                    } else Log.e("AlarmClockActivity", "enableAlarm: failed to save state!");
                }
            });
            enableTask.execute(item);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class AlarmListDialogOptions
    {
        public AppColorValues colors;

        public AlarmListDialogOptions() {
            colors = new AppColorValues();
        }

        public AlarmListDialogOptions(Context context) {
            init(context);
        }

        public void init(Context context) {
            colors = new AppColorValues(AndroidResources.wrap(context), true);
        }
    }

    /**
     * RecyclerView.ViewHolder
     */
    public static class AlarmListDialogItem extends RecyclerView.ViewHolder
    {
        public static SuntimesUtils utils = new SuntimesUtils();

        public boolean isSelected = false;
        public boolean preview_offset = true;
        public boolean preview_offset_transition = false;

        public View card;
        public View cardTray;
        public View noteTray;
        public View cardBackdrop;
        public ImageButton typeButton;
        public TextView text_label;
        public TextView text_usernote;
        public TextView text_event;
        public TextView text_note;
        public TextView text_date;
        public TextView text_datetime;
        public TextView text_location;
        public TextView text_ringtone;
        public TextView text_action0;
        public TextView text_action1;
        public TextView text_action2;
        public TextView text_vibrate;
        public CheckBox check_vibrate;
        public TextView text_repeat;
        public TextView text_offset;
        public ImageButton overflow;
        public ImageButton button_delete;
        public Button button_snooze;
        public Button button_dismiss;
        public SwitchCompat switch_enabled;
        public CheckBox check_enabled;

        public int res_iconAlarm = R.drawable.ic_action_alarms;
        public int res_iconNotification = R.drawable.ic_action_notification;
        public int res_iconNotification1 = R.drawable.ic_action_notification1;
        public int res_iconNotification2 = R.drawable.ic_action_notification2;
        public int res_iconSoundOn = R.drawable.ic_action_soundenabled;
        public int res_iconSoundOff = R.drawable.ic_action_sounddisabled;
        public int res_iconVibrate = R.drawable.ic_action_vibration;
        public int res_iconAction = R.drawable.ic_action_extension;
        public int res_icHome = R.drawable.ic_action_home;
        public int res_icPlace = R.drawable.ic_action_place;
        public int res_backgroundOn = R.drawable.card_alarmitem_enabled_dark1;
        public int res_backgroundOff = R.drawable.card_alarmitem_disabled_dark1;
        public int res_backgroundSounding = R.drawable.card_alarmitem_sounding_dark0;
        public int res_backgroundSnoozing = R.drawable.card_alarmitem_snoozing_dark0;
        public int res_backgroundTimeout = R.drawable.card_alarmitem_timeout_dark0;

        public boolean animatedBackground = false;
        public int res_backgroundCurrent = -1;
        public int anim_enterFadeDuration = 250;
        public int anim_exitFadeDuration = 750;

        public int color_on = Color.CYAN;
        public int color_off = Color.GRAY, color_off1 = Color.WHITE;
        public int color_press = Color.MAGENTA;
        public int color_selected = Color.CYAN;
        public int color_notselected = Color.TRANSPARENT;

        public AlarmListDialogItem(View view)
        {
            super(view);

            card = view.findViewById(R.id.layout_alarmcard);
            cardTray = view.findViewById(R.id.layout_alarmcard_tray);
            noteTray = view.findViewById(R.id.layout_alarm_note);
            cardBackdrop = view.findViewById(R.id.layout_alarmcard0);
            typeButton = (ImageButton) view.findViewById(R.id.type_menu);
            text_label = (TextView) view.findViewById(android.R.id.text1);
            text_usernote = (TextView) view.findViewById(R.id.text_alarm_note);
            text_event = (TextView) view.findViewById(R.id.text_event);
            text_note = (TextView) view.findViewById(R.id.text_note);
            text_date = (TextView) view.findViewById(R.id.text_date);
            text_datetime = (TextView) view.findViewById(R.id.text_datetime);
            text_location = (TextView) view.findViewById(R.id.text_location);
            text_ringtone = (TextView) view.findViewById(R.id.text_ringtone);
            text_action0 = (TextView) view.findViewById(R.id.text_action0);
            text_action1 = (TextView) view.findViewById(R.id.text_action1);
            text_action2 = (TextView) view.findViewById(R.id.text_action2);
            text_vibrate = (TextView) view.findViewById(R.id.text_vibrate);
            check_vibrate = (CheckBox) view.findViewById(R.id.check_vibrate);
            text_repeat = (TextView) view.findViewById(R.id.text_repeat);
            text_offset = (TextView) view.findViewById(R.id.text_datetime_offset);
            overflow = (ImageButton) view.findViewById(R.id.overflow_menu);
            button_delete = (ImageButton) view.findViewById(R.id.button_delete);
            button_dismiss = (Button) view.findViewById(R.id.button_dismiss);
            button_snooze = (Button) view.findViewById(R.id.button_snooze);

            if (Build.VERSION.SDK_INT >= 14) {
                switch_enabled = (SwitchCompat) view.findViewById(R.id.switch_enabled);        // switch used by api >= 14 (otherwise null)
            } else {
                check_enabled = (CheckBox) view.findViewById(R.id.switch_enabled);              // checkbox used by api < 14 (otherwise null)
            }

            initTooltips();
        }

        protected void initTooltips()
        {
            TooltipCompat.setTooltipText(button_delete, button_delete.getContentDescription());
            if (Build.VERSION.SDK_INT >= 14) {
                TooltipCompat.setTooltipText(switch_enabled, switch_enabled.getContentDescription());
            } else {
                TooltipCompat.setTooltipText(check_enabled, check_enabled.getContentDescription());
            }
        }

        public void triggerPreviewOffset(final Context context, final AlarmClockItem item, final AlarmListDialogOptions options)
        {
            if (preview_offset_transition || item.offset == 0) {
                return;
            }

            preview_offset = true;
            preview_offset_transition = true;
            bindData(context, item, options);

            cardTray.postDelayed(new Runnable() {
                @Override
                public void run() {
                    preview_offset_transition = false;
                    preview_offset = !isSelected;
                    bindData(context, item, options);
                }
            }, AlarmEditDialog.PREVIEW_OFFSET_DURATION_MILLIS);
        }

        @SuppressLint("ResourceType")
        private void themeViews(Context context)
        {
            int[] attrs = { R.attr.icActionAlarm, R.attr.icActionNotification,
                            R.attr.icActionSoundEnabled, R.attr.icActionSoundDisabled,
                            R.attr.icActionExtension, R.attr.icActionVibrationEnabled, R.attr.gridItemSelected,
                            R.attr.alarmCardEnabled, R.attr.alarmCardDisabled,
                            R.attr.alarmColorEnabled, android.R.attr.textColorSecondary, android.R.attr.textColorPrimary,
                            R.attr.buttonPressColor, R.attr.alarmCardSounding, R.attr.alarmCardSnoozing, R.attr.alarmCardTimeout,
                            R.attr.icActionNotification1, R.attr.icActionNotification2,
                            R.attr.icActionHome, R.attr.icActionPlace };
            TypedArray a = context.obtainStyledAttributes(attrs);
            res_iconAlarm = a.getResourceId(0, R.drawable.ic_action_alarms);
            res_iconNotification = a.getResourceId(1, R.drawable.ic_action_notification);
            res_iconSoundOn = a.getResourceId(2, R.drawable.ic_action_soundenabled);
            res_iconSoundOff = a.getResourceId(3, R.drawable.ic_action_sounddisabled);
            res_iconAction = a.getResourceId(4, R.drawable.ic_action_extension);
            res_iconVibrate = a.getResourceId(5, R.drawable.ic_action_extension);
            color_selected = ContextCompat.getColor(context, a.getResourceId(6, R.color.grid_selected_dark));
            res_backgroundOn = a.getResourceId(7, R.drawable.card_alarmitem_enabled_dark1);
            res_backgroundOff = a.getResourceId(8, R.drawable.card_alarmitem_disabled_dark1);
            color_on = ContextCompat.getColor(context, a.getResourceId(9, R.color.alarm_enabled_dark));
            color_off = ContextCompat.getColor(context, a.getResourceId(10, android.R.color.secondary_text_dark));
            color_off1 = ContextCompat.getColor(context, a.getResourceId(11, android.R.color.primary_text_dark));
            color_press = ContextCompat.getColor(context, a.getResourceId(12, R.color.btn_tint_pressed_dark));
            res_backgroundSounding = a.getResourceId(13, R.drawable.card_alarmitem_sounding_dark0);
            res_backgroundSnoozing = a.getResourceId(14, R.drawable.card_alarmitem_snoozing_dark0);
            res_backgroundTimeout = a.getResourceId(15, R.drawable.card_alarmitem_timeout_dark0);
            res_iconNotification1 = a.getResourceId(16, R.drawable.ic_action_notification1);
            res_iconNotification2 = a.getResourceId(17, R.drawable.ic_action_notification2);
            res_icHome = a.getResourceId(18, R.drawable.ic_action_home);
            res_icPlace = a.getResourceId(19, R.drawable.ic_action_place);
            a.recycle();
        }


        public void bindData(Context context, @NonNull AlarmClockItem item, AlarmListDialogOptions options)
        {
            themeViews(context);
            updateView(context, this, item, options);
        }

        protected void updateView(Context context, AlarmListDialogItem view, @NonNull final AlarmClockItem item, @NonNull AlarmListDialogOptions options)
        {
            SolarEvents event = SolarEvents.valueOf(item.getEvent(), null);
            int eventType = event == null ? -1 : event.getType();
            boolean isSchedulable = AlarmNotifications.updateAlarmTime(context, item, Calendar.getInstance(), false);

            // spannable icons
            int iconColor = (item.enabled ? color_on : color_off);
            int[] attrs = { R.attr.icActionTimeReset };
            TypedArray a = context.obtainStyledAttributes(attrs);
            int offsetIconSize = (int)context.getResources().getDimension(R.dimen.offsetIcon_width);
            int offsetIconResID = a.getResourceId(0, R.drawable.ic_action_timereset);
            a.recycle();
            Drawable offsetIcon = SuntimesUtils.createImageSpan(context, offsetIconResID, offsetIconSize, offsetIconSize, iconColor).getDrawable().mutate();

            // background
            Resources r = context.getResources();
            int resBackground = item.enabled ? res_backgroundOn : res_backgroundOff;
            int alarmState = (item.state != null) ? item.state.getState() : AlarmState.STATE_NONE;
            switch(alarmState)
            {
                case AlarmState.STATE_SNOOZING:
                    resBackground = res_backgroundSnoozing;
                    view.anim_enterFadeDuration = r.getInteger(R.integer.anim_alarmitem_snoozing_fadeIn_duration);
                    view.anim_exitFadeDuration = r.getInteger(R.integer.anim_alarmitem_snoozing_fadeOut_duration);
                    view.animatedBackground = true;
                    break;

                case AlarmState.STATE_SOUNDING:
                    resBackground = res_backgroundSounding;
                    view.anim_enterFadeDuration = r.getInteger(R.integer.anim_alarmitem_sounding_fadeIn_duration);
                    view.anim_exitFadeDuration = r.getInteger(R.integer.anim_alarmitem_sounding_fadeOut_duration);
                    view.animatedBackground = true;
                    break;

                case AlarmState.STATE_TIMEOUT:
                    resBackground = res_backgroundTimeout;
                    view.anim_enterFadeDuration = r.getInteger(R.integer.anim_alarmitem_timeout_fadeIn_duration);
                    view.anim_exitFadeDuration = r.getInteger(R.integer.anim_alarmitem_timeout_fadeOut_duration);
                    view.animatedBackground = true;
                    break;

                default:
                    view.animatedBackground = false;
                    break;
            }

            view.cardBackdrop.setBackgroundColor( isSelected ? ColorUtils.setAlphaComponent(color_selected, 170) : color_notselected);  // 66% alpha
            if (resBackground != res_backgroundCurrent)
            {
                res_backgroundCurrent = resBackground;    // don't set background unless actually changed (avoids interrupting running animations)
                Drawable background = ContextCompat.getDrawable(context, resBackground);
                if (background != null) {
                    background.mutate();
                }
                if (Build.VERSION.SDK_INT >= 16) {
                    view.card.setBackground(background);
                } else {
                    view.card.setBackgroundDrawable(background);
                }
            }

            // enabled / disabled
            if (Build.VERSION.SDK_INT >= 14) {
                if (view.switch_enabled != null)
                {
                    view.switch_enabled.setChecked(item.enabled);
                    view.switch_enabled.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
                }
            } else {
                if (view.check_enabled != null) {
                    view.check_enabled.setChecked(item.enabled);
                    view.check_enabled.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
                }
            }

            // type button
            if (view.typeButton != null)
            {
                int typeDrawable;
                switch (item.type) {
                    case NOTIFICATION: typeDrawable = res_iconNotification; break;
                    case NOTIFICATION1: typeDrawable = res_iconNotification1; break;
                    case NOTIFICATION2: typeDrawable = res_iconNotification2; break;
                    case ALARM: default: typeDrawable = res_iconAlarm; break;
                }
                view.typeButton.setImageDrawable(ContextCompat.getDrawable(context, typeDrawable));
                view.typeButton.setContentDescription(item.type.getDisplayString());

                ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(
                        (!isSelected && !item.enabled ? color_off : (item.enabled ? color_on : color_off)),
                        color_off,
                        (!isSelected && !item.enabled ? color_off : color_press)
                ));
            }

            // label
            if (view.text_label != null) {
                view.text_label.setText(AlarmEditViewHolder.displayAlarmLabel(context, item));
                view.text_label.setTextColor(item.enabled ? color_on : color_off);
            }

            // event
            if (view.text_event != null)
            {
                view.text_event.setText(AlarmEditViewHolder.displayEvent(context, item));
                view.text_event.setTextColor(item.enabled ? color_on : color_off);

                float eventIconSize = context.getResources().getDimension(R.dimen.eventIcon_width);
                if (event != null)
                {
                    boolean northward = WidgetSettings.loadLocalizeHemispherePref(context, 0) && (item.location != null) && (item.location.getLatitudeAsDouble() < 0);
                    Drawable eventIcon = EventIcons.getIconDrawable(context, event, (int)eventIconSize, (int)eventIconSize, northward, options.colors);
                    view.text_event.setCompoundDrawablePadding(EventIcons.getIconDrawablePadding(context, event));
                    view.text_event.setCompoundDrawables(eventIcon, null, null, null);

                } else {
                    String tag = EventIcons.getIconTag(context, item);
                    Drawable eventIcon = EventIcons.getIconDrawable(context, tag, (int)eventIconSize, (int)eventIconSize);
                    Integer tint = EventIcons.getIconTint(context, tag);
                    if (tint == null) {    // re-tint uncolored icons
                        EventIcons.tintDrawable(eventIcon, item.enabled ? color_on : color_off);
                    }
                    view.text_event.setCompoundDrawablePadding(EventIcons.getIconDrawablePadding(context, item.timezone));
                    view.text_event.setCompoundDrawables(eventIcon, null, null, null);
                }
                view.text_event.setVisibility(item.getEvent() == null && item.timezone == null ? View.GONE : View.VISIBLE);
            }

            // time
            if (view.text_datetime != null)
            {
                CharSequence timeDisplay = isSchedulable ? AlarmEditViewHolder.displayAlarmTime(context, item, preview_offset) : "";
                view.text_datetime.setText(timeDisplay);
                view.text_datetime.setTextColor(item.enabled ? color_on : (isSelected ? color_off1 : color_off));

                /*if (item.offset != 0 && !isSelected) {
                    view.text_datetime.setCompoundDrawablePadding((int)context.getResources().getDimension(R.dimen.offsetIcon_margin));
                    view.text_datetime.setCompoundDrawables(offsetIcon, null, null, null);
                } else {
                    text_event.setCompoundDrawablePadding(SolarEventIcons.getIconDrawablePadding(context, item.timezone));
                    view.text_datetime.setCompoundDrawablePadding(0);
                    view.text_datetime.setCompoundDrawables(null, null, null, null);
                }*/
            }

            // date
            if (view.text_date != null) {
                view.text_date.setText(isSchedulable ? AlarmEditViewHolder.displayAlarmDate(context, item, preview_offset) : "");
                view.text_date.setVisibility(isSchedulable && AlarmEditViewHolder.showAlarmDate(context, item) ? View.VISIBLE : View.GONE);
                view.text_date.setTextColor(item.enabled ? color_on : (isSelected ? color_off1 : color_off));
            }

            // location
            if (view.text_location != null)
            {
                AlarmEvent.AlarmEventItem eventItem = item.getEventItem(context);
                boolean isClockTime = (item.getEvent() == null && item.timezone == null);
                boolean isSolarTime = (item.getEvent() == null && item.timezone != null);

                if (isClockTime) {
                    eventItem.setRequiresLocation(false);
                }
                if (isSolarTime) {
                    eventItem.setRequiresLocation(true);
                }

                view.text_location.setVisibility(eventItem.requiresLocation() ? View.VISIBLE : View.INVISIBLE);
                view.text_location.setText(item.location != null ? item.location.getLabel() : "");
                view.text_location.setTextColor(item.enabled ? color_on : color_off);

                int iconMargin = (int)context.getResources().getDimension(R.dimen.eventIcon_margin);
                boolean useAppLocation = item.flagIsTrue(AlarmClockItem.FLAG_LOCATION_FROM_APP);
                text_location.setCompoundDrawablePadding(iconMargin);
                text_location.setCompoundDrawablesWithIntrinsicBounds((useAppLocation ? res_icHome : res_icPlace), 0, 0, 0);

                Drawable[] d = SuntimesUtils.tintCompoundDrawables(view.text_location.getCompoundDrawables(), (item.enabled ? color_on : color_off));
                view.text_location.setCompoundDrawables(d[0], d[1], d[2], d[3]);
            }

            // ringtone
            if (view.text_ringtone != null) {
                view.text_ringtone.setText( ringtoneDisplayChip(context, item, isSelected) );
                view.text_ringtone.setTextColor(item.enabled ? color_on : color_off);
            }

            // action
            if (view.text_action0 != null) {
                view.text_action0.setText(actionDisplayChip(context, item, 0, isSelected));
                view.text_action0.setVisibility( item.actionID0 != null ? View.VISIBLE : View.GONE );
                view.text_action0.setTextColor(item.enabled ? color_on : color_off);
            }

            if (view.text_action1 != null) {
                view.text_action1.setText(actionDisplayChip(context, item, 1, isSelected));
                view.text_action1.setVisibility( item.actionID1 != null ? View.VISIBLE : View.GONE );
                view.text_action1.setTextColor(item.enabled ? color_on : color_off);
            }

            if (view.text_action2 != null) {
                view.text_action2.setText(actionDisplayChip(context, item, 2, isSelected));
                view.text_action2.setVisibility( item.actionID2 != null ? View.VISIBLE : View.GONE );
                view.text_action2.setTextColor(item.enabled ? color_on : color_off);
            }

            // vibrate
            if (view.check_vibrate != null) {
                view.check_vibrate.setChecked(item.vibrate);
                view.check_vibrate.setTextColor(item.enabled ? color_on : color_off);
            }
            if (view.text_vibrate != null) {
                view.text_vibrate.setText(vibrateDisplayChip(context, item, isSelected));
                view.text_vibrate.setVisibility(item.vibrate ? View.VISIBLE : View.GONE);
                view.text_vibrate.setTextColor(item.enabled ? color_on : color_off);
            }

            // repeating
            if (view.text_repeat != null)
            {
                String repeatText = context.getString(R.string.alarmOption_repeat_none);
                boolean repeating = (item.repeating && item.repeatingDays != null && !item.repeatingDays.isEmpty());
                if (repeating)
                {
                    switch (item.getEventItem(context).supportsRepeating())
                    {
                        case AlarmEventContract.REPEAT_SUPPORT_BASIC:
                            repeatText = context.getString(R.string.alarmOption_repeat);
                            break;

                        case AlarmEventContract.REPEAT_SUPPORT_DAILY:
                            repeatText = (AlarmClockItem.repeatsEveryDay(item.repeatingDays))
                                    ? context.getString(R.string.alarmOption_repeat_all)
                                    : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);
                            break;

                        case AlarmEventContract.REPEAT_SUPPORT_NONE:
                        default:
                            repeatText = context.getString(R.string.alarmOption_repeat_none);
                            break;
                    }
                }

                view.text_repeat.setText(repeatText);
                view.text_repeat.setTextColor(item.enabled ? color_on : color_off);
                view.text_repeat.setVisibility(repeating ? View.VISIBLE : View.GONE);
            }

            // offset (before / after)
            if (view.text_offset != null)
            {
                boolean alwaysShowOffset = AlarmSettings.loadPrefAlarmSortShowOffset(context);
                CharSequence offsetDisplay = (preview_offset && !alwaysShowOffset ? "" : AlarmEditViewHolder.displayOffset(context, item));
                view.text_offset.setText((isSchedulable && isSelected || alwaysShowOffset) ? offsetDisplay : "");

                if (preview_offset && item.offset != 0) {
                    view.text_offset.setText(SuntimesUtils.createSpan(context, "i", "i", new ImageSpan(offsetIcon), ImageSpan.ALIGN_BASELINE));
                }

                view.text_offset.setTextColor(item.enabled ? color_on : color_off);
            }

            // extended controls
            if (item.type == AlarmClockItem.AlarmType.ALARM)
            {
                switch(alarmState)
                {
                    case AlarmState.STATE_SNOOZING:
                        view.text_note.setVisibility(View.GONE);
                        view.button_dismiss.setVisibility(View.VISIBLE);
                        view.button_snooze.setVisibility(View.GONE);
                        break;
                    case AlarmState.STATE_SOUNDING:
                        view.text_note.setVisibility(View.GONE);
                        view.button_dismiss.setVisibility(View.VISIBLE);
                        view.button_snooze.setVisibility(View.VISIBLE);
                        break;
                    case AlarmState.STATE_TIMEOUT:
                        view.text_note.setVisibility(View.VISIBLE);
                        view.button_dismiss.setVisibility(View.VISIBLE);
                        view.button_snooze.setVisibility(View.GONE);
                        break;
                    case AlarmState.STATE_SCHEDULED_SOON:
                    case AlarmState.STATE_SCHEDULED_DISTANT:
                        long soonMillis = AlarmSettings.loadPrefAlarmUpcoming(context);
                        if (soonMillis <= 0) {
                            soonMillis = 1000 * 60 * 60 * 6;
                        }
                        boolean isSoon = ((item.alarmtime - System.currentTimeMillis()) <= soonMillis);
                        view.text_note.setVisibility(View.VISIBLE);
                        view.button_dismiss.setVisibility((item.enabled && item.repeating && isSoon) ? View.VISIBLE : View.GONE);    // allow dismiss early (and reschedule)
                        view.button_snooze.setVisibility(View.GONE);
                        break;
                    default:
                        view.text_note.setVisibility(View.VISIBLE);
                        view.button_dismiss.setVisibility(View.GONE);
                        view.button_snooze.setVisibility(View.GONE);
                        break;
                }

            } else {
                switch(alarmState)
                {
                    case AlarmState.STATE_SNOOZING:
                    case AlarmState.STATE_SOUNDING:
                        view.text_note.setVisibility(View.GONE);
                        view.button_dismiss.setVisibility(View.VISIBLE);
                        view.button_snooze.setVisibility(View.GONE);
                        break;
                    case AlarmState.STATE_TIMEOUT:
                        view.text_note.setVisibility(View.VISIBLE);
                        view.button_dismiss.setVisibility(View.VISIBLE);
                        view.button_snooze.setVisibility(View.GONE);
                        break;
                    default:
                        view.text_note.setVisibility(View.VISIBLE);
                        view.button_dismiss.setVisibility(View.GONE);
                        view.button_snooze.setVisibility(View.GONE);
                        break;
                }
            }

            // note tray
            if (text_usernote != null) {
                text_usernote.setText(item.note != null ? DataSubstitutions.displayStringForTitlePattern0(AndroidSuntimesDataSettings.wrap(context), item.note, AlarmNotifications.getData(context, item)) : "");
            }
            if (view.noteTray != null) {
                view.noteTray.setVisibility(isSelected && item.note != null && !item.note.isEmpty() ? View.VISIBLE : View.GONE);
            }

            // extended tray
            if (view.cardTray != null) {
                view.cardTray.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }
            if (view.text_note != null)
            {
                if (isSelected)
                {
                    switch (alarmState)
                    {
                        case AlarmState.STATE_SOUNDING:
                        case AlarmState.STATE_SNOOZING:
                            view.text_note.setText("");
                            break;
                        case AlarmState.STATE_TIMEOUT:
                            view.text_note.setText(context.getString(R.string.alarmAction_timeout));
                            break;
                        case AlarmState.STATE_NONE: default:
                            view.text_note.setText(AlarmEditViewHolder.displayAlarmNote(context, item, isSchedulable));
                            break;
                    }
                } else {
                    view.text_note.setText("");
                }
            }
        }

        private CharSequence ringtoneDisplayChip(Context context, AlarmClockItem item, boolean isSelected)
        {
            int iconDimen = (int) context.getResources().getDimension(R.dimen.chipIcon_size);
            int iconID = item.ringtoneName != null ? res_iconSoundOn : res_iconSoundOff;
            ImageSpan icon = isSelected || item.enabled
                    ? SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, item.enabled ? color_on : 0)
                    : SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, color_off, PorterDuff.Mode.MULTIPLY);
            return SuntimesUtils.createSpan(context, "[icon]", "[icon]", icon);
        }

        private CharSequence vibrateDisplayChip(Context context, AlarmClockItem item, boolean isSelected)
        {
            if (item.vibrate)
            {
                int iconID = res_iconVibrate;
                int iconDimen = (int) context.getResources().getDimension(R.dimen.chipIcon_size);
                ImageSpan ringtonIcon = isSelected || item.enabled
                        ? SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, item.enabled ? color_on : 0)
                        : SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, color_off, PorterDuff.Mode.MULTIPLY);
                return SuntimesUtils.createSpan(context, "[icon]", "[icon]", ringtonIcon);
            } else {
                return "";
            }
        }

        private CharSequence actionDisplayChip(Context context, AlarmClockItem item, int actionNum, boolean isSelected)
        {
            int iconDimen = (int) context.getResources().getDimension(R.dimen.chipIcon_size);
            ImageSpan icon = (isSelected || item.enabled)
                    ? SuntimesUtils.createImageSpan(context, res_iconAction, iconDimen, iconDimen, item.enabled ? color_on : 0)
                    : SuntimesUtils.createImageSpan(context, res_iconAction, iconDimen, iconDimen, color_off, PorterDuff.Mode.MULTIPLY);
            return SuntimesUtils.createSpan(context, "[icon]", "[icon]", icon);
        }

        public void resetBackground()
        {
            if (Build.VERSION.SDK_INT >= 16) {
                card.setBackground(null);
            } else {
                card.setBackgroundDrawable(null);
            }
            res_backgroundCurrent = -1;
            animatedBackground = false;
        }

        public void startBackgroundAnimation(Context context)
        {
            if (this.animatedBackground && this.card != null)
            {
                Drawable background = card.getBackground();
                if (background != null)
                {
                    if (background instanceof StateListDrawable) {
                        //Log.d("DEBUG", "starting background (StateListDrawable): " + this);
                        AlarmListDialogItem.startStateListAnimations(context, (StateListDrawable) background, this.anim_enterFadeDuration, this.anim_exitFadeDuration);
                    } else if (background instanceof AnimationDrawable) {
                        //Log.d("DEBUG", "starting background (AnimatedDrawable): " + this);
                        AlarmListDialogItem.startAnimatedDrawable(context, (AnimationDrawable) background, this.anim_enterFadeDuration, this.anim_exitFadeDuration);
                    } else {
                        //Log.d("DEBUG", "starting background: skipped: " + this);
                    }
                }
            }
        }

        public void stopBackgroundAnimation(Context context)
        {
            if (this.animatedBackground && this.card != null)
            {
                Drawable background = this.card.getBackground();
                if (background != null)
                {
                    if (background instanceof StateListDrawable) {
                        //Log.d("DEBUG", "stopping background (StateListDrawable): " + this);
                        AlarmListDialogItem.stopStateListAnimations(context, (StateListDrawable) background);
                    } else if (background instanceof AnimationDrawable) {
                        //Log.d("DEBUG", "stopping background (StateListDrawable): " + this);
                        ((AnimationDrawable) background).setVisible(false, false);
                    } /*else {
                        //Log.d("DEBUG", "stopping background: skipped: " + this);
                    }*/
                }
            }
        }

        public static void startStateListAnimations(Context context, @NonNull StateListDrawable drawable, int enterFadeDuration, int exitFadeDuration)
        {
            Drawable current = drawable.getCurrent();
            if (current instanceof AnimationDrawable) {
                startAnimatedDrawable(context, (AnimationDrawable)((AnimationDrawable) current).mutate(), enterFadeDuration, exitFadeDuration);
            }
        }

        public static void stopStateListAnimations(Context context, @NonNull StateListDrawable drawable)
        {
            Drawable current = drawable.getCurrent();
            if (current instanceof AnimationDrawable) {
                AnimationDrawable animated = (AnimationDrawable) current;
                animated.setVisible(false, true);
            }
        }

        public static void startAnimatedDrawable(Context context, AnimationDrawable animated, int enterFadeDuration, int exitFadeDuration)
        {
            if (Build.VERSION.SDK_INT >= 11)
            {
                animated.setEnterFadeDuration(enterFadeDuration);
                animated.setExitFadeDuration(exitFadeDuration);
            }
            animated.setOneShot(false);
            animated.setVisible(true, true);
            animated.stop();
            animated.start();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected AdapterListener listener;
    protected AdapterListener adapterListener = new AdapterListener()
    {
        @Override
        public void onItemSelected(long rowID) {
            if (listener != null) {
                listener.onItemSelected(rowID);
            }
        }

        @Override
        public void onItemClicked(AlarmClockItem item, AlarmListDialogItem holder)
        {
            if (listener != null) {
                listener.onItemClicked(item, holder);
            }
        }

        @Override
        public boolean onItemLongClicked(AlarmClockItem item) {
            if (listener != null) {
                return listener.onItemLongClicked(item);
            } else return false;
        }

        @Override
        public void onItemNoteClicked(AlarmClockItem item, AlarmListDialogItem view) {
            if (listener != null) {
                listener.onItemNoteClicked(item, view);
            }
        }

        @Override
        public void onAlarmToggled(AlarmClockItem item, boolean enabled) {
            if (listener != null) {
                listener.onAlarmToggled(item, enabled);
            }
        }

        @Override
        public void onAlarmAdded(AlarmClockItem item) {
            if (listener != null) {
                listener.onAlarmAdded(item);
            }
        }

        @Override
        public void onAlarmDeleted(long rowID) {
            if (listener != null) {
                listener.onAlarmDeleted(rowID);
            }
        }

        @Override
        public void onAlarmsCleared() {
            if (listener != null) {
                listener.onAlarmsCleared();
            }
        }
    };

    public void setAdapterListener(AdapterListener listener) {
        this.listener = listener;
    }

    public interface AdapterListener
    {
        void onItemSelected(long rowID);
        void onItemClicked(AlarmClockItem item, AlarmListDialogItem view);
        boolean onItemLongClicked(AlarmClockItem item);
        void onItemNoteClicked(AlarmClockItem item, AlarmListDialogItem view);
        void onAlarmToggled(AlarmClockItem item, boolean enabled);
        void onAlarmAdded(AlarmClockItem item);
        void onAlarmDeleted(long rowID);
        void onAlarmsCleared();
    }

}
