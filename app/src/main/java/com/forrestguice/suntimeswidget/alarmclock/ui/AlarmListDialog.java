/**
    Copyright (C) 2020 Forrest Guice
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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEventIcons;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("Convert2Diamond")
public class AlarmListDialog extends DialogFragment
{
    public static final String EXTRA_SELECTED_ROWID = "selectedRowID";

    protected View emptyView;
    protected RecyclerView list;
    protected AlarmListDialogAdapter adapter;
    protected ProgressBar progress;

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
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));
        View content = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_alarmlist, parent, false);

        progress = (ProgressBar) content.findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        emptyView = content.findViewById(android.R.id.empty);
        emptyView.setOnClickListener(onEmptyViewClick);
        emptyView.setVisibility(View.GONE);

        adapter = new AlarmListDialogAdapter(getActivity());
        adapter.setAdapterListener(adapterListener);

        list = (RecyclerView) content.findViewById(R.id.recyclerview);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addItemDecoration(itemDecoration);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.alarmlist, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        int sortValue = AlarmSettings.loadPrefAlarmSort(getActivity());
        MenuItem sort_alarmtime = menu.findItem(R.id.sortByAlarmTime);
        MenuItem sort_creation = menu.findItem(R.id.sortByCreation);
        sort_alarmtime.setChecked(sortValue == AlarmSettings.SORT_BY_ALARMTIME);
        sort_creation.setChecked(sortValue == AlarmSettings.SORT_BY_CREATION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.sortByAlarmTime:
                AlarmSettings.savePrefAlarmSort(getActivity(), AlarmSettings.SORT_BY_ALARMTIME);
                if (Build.VERSION.SDK_INT >= 11) {
                    getActivity().invalidateOptionsMenu();
                }  // else { TODO }
                adapter.sortItems();
                return true;

            case R.id.sortByCreation:
                AlarmSettings.savePrefAlarmSort(getActivity(), AlarmSettings.SORT_BY_CREATION);
                if (Build.VERSION.SDK_INT >= 11) {
                    getActivity().invalidateOptionsMenu();
                }  // else { TODO }
                adapter.sortItems();
                return true;

            case R.id.action_clear:
                confirmClearAlarms(getActivity());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void offerUndoClearAlarms(Context context, final List<AlarmClockItem> items)
    {
        View view = getView();
        if (context != null && view != null)
        {
            Snackbar snackbar = Snackbar.make(view, context.getString(R.string.clearalarms_toast_success), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(context.getString(R.string.configAction_undo), new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context context = getActivity();
                    if (context != null) {
                        for (AlarmClockItem item : items) {
                            if (item != null) {
                                addAlarm(context, item);
                            }
                        }
                    }
                }
            });
            SuntimesUtils.themeSnackbar(context, snackbar, null);
            snackbar.setDuration(UNDO_DELETE_MILLIS);
            snackbar.show();
        }
    }

    public void offerUndoDeleteAlarm(Context context, final AlarmClockItem deletedItem)
    {
        View view = getView();
        if (context != null && view != null && deletedItem != null)
        {
            Snackbar snackbar = Snackbar.make(view, context.getString(R.string.deletealarm_toast_success1, deletedItem.type.getDisplayString()), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(context.getString(R.string.configAction_undo), new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context context = getActivity();
                    if (context != null && deletedItem != null) {
                        addAlarm(getActivity(), deletedItem);
                    }
                }
            });
            SuntimesUtils.themeSnackbar(context, snackbar, null);
            snackbar.setDuration(UNDO_DELETE_MILLIS);
            snackbar.show();
        }
    }
    public static final int UNDO_DELETE_MILLIS = 8000;

    public AlarmClockItem createAlarm(final Context context, AlarmClockItem.AlarmType type, String label, String event, Location location, int hour, int minute, String timezone, boolean vibrate, Uri ringtoneUri, ArrayList<Integer> repetitionDays, boolean addToDatabase)
    {
        final AlarmClockItem alarm = createAlarm(context, type, label, event, location, hour, minute, timezone, vibrate, ringtoneUri, repetitionDays);
        if (addToDatabase) {
            addAlarm(context, alarm);
        }
        return alarm;
    }

    public static AlarmClockItem createAlarm(final Context context, AlarmClockItem.AlarmType type, String label, @NonNull String event, @NonNull Location location) {
        return createAlarm(context, type, label, event, location, -1, -1, null, AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, type), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
    }

    public static AlarmClockItem createAlarm(final Context context, AlarmClockItem.AlarmType type, String label, String event, Location location, int hour, int minute, String timezone, boolean vibrate, Uri ringtoneUri, ArrayList<Integer> repetitionDays)
    {
        final AlarmClockItem alarm = new AlarmClockItem();
        alarm.enabled = AlarmSettings.loadPrefAlarmAutoEnable(context);
        alarm.type = type;
        alarm.label = label;
        alarm.hour = hour;
        alarm.minute = minute;
        alarm.timezone = timezone;
        alarm.setEvent(event);
        alarm.location = (location != null ? location : WidgetSettings.loadLocationPref(context, 0));
        alarm.repeating = false;
        alarm.vibrate = vibrate;

        alarm.ringtoneURI = (ringtoneUri != null ? ringtoneUri.toString() : null);
        if (alarm.ringtoneURI != null)
        {
            Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);     // TODO: optimize.. getRingtone takes up to 100ms!
            alarm.ringtoneName = ringtone.getTitle(context);                           // another ~10ms
            ringtone.stop();                                                           // another ~30ms
        }

        alarm.setState(alarm.enabled ? AlarmState.STATE_NONE : AlarmState.STATE_DISABLED);
        alarm.modified = true;
        return alarm;
    }


    public AlarmClockItem addAlarm(final Context context, AlarmClockItem alarm)
    {
        AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, true, true);
        task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
        {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                if (result)
                {
                    if (listener != null) {
                        listener.onAlarmAdded(item);
                    }

                    setSelectedRowID(item.rowID);
                    reloadAdapter();

                    if (item.enabled) {
                        context.sendBroadcast( AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri()) );
                    }
                }
            }
        });
        task.execute(alarm);
        return alarm;
    }


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
        Log.d("DEBUG", "reloadAdapter");
    }

    protected AlarmListTask.AlarmListTaskListener onListLoaded = new AlarmListTask.AlarmListTaskListener() {
        @Override
        public void onLoadFinished(List<AlarmClockItem> data)
        {
            Log.d("DEBUG", "onListLoaded: " + data.size());
            adapter.setItems(data);
            updateViews();
            scrollToSelectedItem();
        }
    };

    protected AlarmListTask.AlarmListTaskListener onItemChanged = new AlarmListTask.AlarmListTaskListener() {
        @Override
        public void onLoadFinished(List<AlarmClockItem> data)
        {
            Log.d("DEBUG", "onItemChanged: " + data.size());
            adapter.setItem(data.get(0));
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
        private AlarmDatabaseAdapter db;
        private WeakReference<Context> contextRef;

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
                items.add(item);
                publishProgress(item);

                cursor.moveToNext();
            }
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
            public void onLoadFinished(List<AlarmClockItem> result) {};
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
        }

        public void setSelectedRowID(long rowID) {
            Log.d("setSelectedRowID", ""+ rowID);
            selectedRowID = rowID;
            notifyDataSetChanged();
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
                notifyItemChanged(selectedIndex);
            }
        }

        public void setItems(List<AlarmClockItem> values)
        {
            items.clear();
            items.addAll(sortItems(values));
            notifyDataSetChanged();
        }

        public void setItem(AlarmClockItem item)
        {
            int position = getIndex(item.rowID);
            if (position >= 0 && position < items.size())
            {
                items.add(position, item);
                AlarmClockItem previous = items.remove(position + 1);

                if (item.timestamp != previous.timestamp) {
                    sortItems();
                } else {
                    notifyItemChanged(position);
                }

            } else {
                items.add(item);
                notifyDataSetChanged();
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
            final long now = Calendar.getInstance().getTimeInMillis();
            final int sortMode = AlarmSettings.loadPrefAlarmSort(contextRef.get());
            Collections.sort(items, new Comparator<AlarmClockItem>()
            {
                @Override
                public int compare(AlarmClockItem o1, AlarmClockItem o2)
                {
                    switch (sortMode)
                    {
                        case AlarmSettings.SORT_BY_ALARMTIME:                // nearest alarm time first
                            return compareLong((o1.timestamp + o1.offset) - now, (o2.timestamp + o2.offset) - now);

                        case AlarmSettings.SORT_BY_CREATION:
                        default: return compareLong(o2.rowID, o1.rowID);    // newest items first
                    }
                }
            });
            return items;
        }

        static int compareLong(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);    // copied from Long.compare to support api < 19
        }

        @Override
        public long getItemId( int position ) {
            return (position >= 0 && position < items.size()) ? items.get(position).rowID : 0;
        }

        @Override
        public AlarmListDialogItem onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.layout_listitem_alarmclock2, parent, false);
            return new AlarmListDialogItem(view);
        }

        @Override
        public void onBindViewHolder(AlarmListDialogItem holder, int position)
        {
            AlarmClockItem item = items.get(position);
            holder.isSelected = (item.rowID == selectedRowID);
            holder.preview_offset = !holder.isSelected;
            ViewCompat.setTransitionName(holder.text_datetime, "transition_" + item.rowID);

            detachClickListeners(holder);
            holder.bindData(contextRef.get(), items.get(position));
            attachClickListeners(holder, position);
        }

        @Override
        public void onViewRecycled(AlarmListDialogItem holder)
        {
            detachClickListeners(holder);
            holder.isSelected = false;
        }

        private void attachClickListeners(@NonNull final AlarmListDialogItem holder, final int position)
        {
            if (holder.card != null) {
                holder.card.setOnClickListener(itemClickListener(position, holder));
                //holder.card.setOnLongClickListener(itemLongClickListener(position));
            }
            if (holder.overflow != null) {
                holder.overflow.setOnClickListener(overflowMenuListener(position));
            }
            if (holder.typeButton != null) {
                holder.typeButton.setOnClickListener(typeMenuListener(position, holder.typeButton));
            }
            if (holder.button_delete != null) {
                holder.button_delete.setOnClickListener(deleteButtonListener(position));
            }
            if (holder.text_note != null) {
                holder.text_note.setOnClickListener(noteListener(position, holder));
            }

            if (Build.VERSION.SDK_INT >= 14) {
                if (holder.switch_enabled != null) {
                    holder.switch_enabled.setOnCheckedChangeListener(alarmEnabledListener(position));
                }
            } else {
                if (holder.check_enabled != null) {
                    holder.check_enabled.setOnCheckedChangeListener(alarmEnabledListener(position));
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

        private View.OnClickListener itemClickListener(final int position, final AlarmListDialogItem holder)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (listener != null) {
                        listener.onItemClicked(items.get(position), holder);
                    }
                    setSelectedIndex(position);
                }
            };
        }

        private View.OnLongClickListener itemLongClickListener(final int position)
        {
            return new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    setSelectedIndex(position);
                    if (listener != null) {
                        return listener.onItemLongClicked(items.get(position));
                    } else return true;
                }
            };
        }

        private View.OnClickListener overflowMenuListener(final int position)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedIndex(position);
                    showOverflowMenu(contextRef.get(), position, v);
                }
            };
        }

        private View.OnClickListener deleteButtonListener(final int position)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlarmEditDialog.confirmDeleteAlarm(contextRef.get(), items.get(position), onDeleteConfirmed(contextRef.get(), items.get(position)));
                }
            };
        }

        private View.OnClickListener noteListener(final int position, final AlarmListDialogItem view)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemNoteClicked(items.get(position), view);
                    }
                }
            };
        }

        private View.OnClickListener editButtonListener(final int position, final AlarmListDialogItem holder)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedIndex(position);
                    if (listener != null) {
                        listener.onItemClicked(items.get(position), holder);
                    }
                }
            };
        }

        private View.OnClickListener typeMenuListener(final int position, View v)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedIndex(position);
                    showAlarmTypeMenu(contextRef.get(), position, v);
                }
            };
        }

        private CompoundButton.OnCheckedChangeListener alarmEnabledListener(final int position)
        {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setSelectedIndex(position);
                    enableAlarm(contextRef.get(), position, isChecked);
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

        protected void showOverflowMenu(Context context, final int position, final View buttonView)
        {
            PopupMenu menu = new PopupMenu(context, buttonView);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.alarmcontext1, menu.getMenu());
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    switch (menuItem.getItemId())
                    {
                        case R.id.action_delete:
                            AlarmEditDialog.confirmDeleteAlarm(contextRef.get(), items.get(position), onDeleteConfirmed(contextRef.get(), items.get(position)));
                            return true;

                        default:
                            return false;
                    }
                }
            });

            SuntimesUtils.forceActionBarIcons(menu.getMenu());
            menu.show();
        }

        protected void showAlarmTypeMenu(final Context context, final int position, final View buttonView)
        {
            PopupMenu menu = new PopupMenu(context, buttonView);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.alarmtype, menu.getMenu());

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    switch (menuItem.getItemId())
                    {
                        case R.id.alarmTypeNotification:
                            return changeAlarmType(context, position, AlarmClockItem.AlarmType.NOTIFICATION);

                        case R.id.alarmTypeAlarm:
                        default:
                            return changeAlarmType(context, position, AlarmClockItem.AlarmType.ALARM);
                    }
                }
            });

            SuntimesUtils.forceActionBarIcons(menu.getMenu());
            menu.show();
        }

        protected boolean changeAlarmType(Context context, final int position, AlarmClockItem.AlarmType type)
        {
            AlarmClockItem item = items.get(position);
            if (item.type != type)
            {
                Log.d("AlarmList", "alarmTypeMenu: alarm type is changed: " + type);
                if (item.enabled)
                {
                    Log.d("AlarmList", "alarmTypeMenu: alarm is enabled (reschedule required?)");
                    // item is enabled; disable it or reschedule/reenable
                    return false;

                } else {
                    Log.d("AlarmList", "alarmTypeMenu: alarm is disabled, changing its type..");
                    item.type = type;
                    item.setState(AlarmState.STATE_NONE);

                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                    task.setTaskListener(changeAlarmTypeTaskListener(position));
                    task.execute(item);
                    return true;
                }
            }
            Log.w("AlarmList", "alarmTypeMenu: alarm type is unchanged");
            return false;
        }
        private AlarmDatabaseAdapter.AlarmItemTaskListener changeAlarmTypeTaskListener(final int position)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item) {
                    notifyItemChanged(position);
                }
            };
        }

        public void enableAlarm(final Context context, final int position, final boolean enabled)
        {
            AlarmClockItem item = items.get(position);
            item.alarmtime = 0;
            item.enabled = enabled;
            item.modified = true;

            AlarmDatabaseAdapter.AlarmUpdateTask enableTask = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, false);
            enableTask.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (result) {
                        context.sendBroadcast( enabled ? AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri())
                                                       : AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISABLE, item.getUri()) );
                        if (!enabled) {
                            AlarmNotifications.updateAlarmTime(context, item);
                        }
                        notifyItemChanged(position);

                        if (listener != null) {
                            listener.onAlarmToggled(items.get(position), enabled);
                        }

                    } else Log.e("AlarmClockActivity", "enableAlarm: failed to save state!");
                }
            });
            enableTask.execute(item);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        public View cardBackdrop;
        public ImageButton typeButton;
        public TextView text_label;
        public TextView text_event;
        public TextView text_note;
        public TextView text_date;
        public TextView text_datetime;
        public TextView text_location;
        public TextView text_ringtone;
        public TextView text_action0;
        public TextView text_action1;
        public TextView text_vibrate;
        public CheckBox check_vibrate;
        public TextView text_repeat;
        public TextView text_offset;
        public ImageButton overflow;
        public ImageButton button_delete;
        public SwitchCompat switch_enabled;
        public CheckBox check_enabled;

        public int res_iconAlarm = R.drawable.ic_action_alarms;
        public int res_iconNotification = R.drawable.ic_action_notification;
        public int res_iconSoundOn = R.drawable.ic_action_soundenabled;
        public int res_iconSoundOff = R.drawable.ic_action_sounddisabled;
        public int res_iconVibrate = R.drawable.ic_action_vibration;
        public int res_iconAction = R.drawable.ic_action_extension;
        public int res_backgroundOn = R.drawable.card_alarmitem_enabled_dark1;
        public int res_backgroundOff = R.drawable.card_alarmitem_disabled_dark1;

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
            cardBackdrop = view.findViewById(R.id.layout_alarmcard0);
            typeButton = (ImageButton) view.findViewById(R.id.type_menu);
            text_label = (TextView) view.findViewById(android.R.id.text1);
            text_event = (TextView) view.findViewById(R.id.text_event);
            text_note = (TextView) view.findViewById(R.id.text_note);
            text_date = (TextView) view.findViewById(R.id.text_date);
            text_datetime = (TextView) view.findViewById(R.id.text_datetime);
            text_location = (TextView) view.findViewById(R.id.text_location);
            text_ringtone = (TextView) view.findViewById(R.id.text_ringtone);
            text_action0 = (TextView) view.findViewById(R.id.text_action0);
            text_action1 = (TextView) view.findViewById(R.id.text_action1);
            text_vibrate = (TextView) view.findViewById(R.id.text_vibrate);
            check_vibrate = (CheckBox) view.findViewById(R.id.check_vibrate);
            text_repeat = (TextView) view.findViewById(R.id.text_repeat);
            text_offset = (TextView) view.findViewById(R.id.text_datetime_offset);
            overflow = (ImageButton) view.findViewById(R.id.overflow_menu);
            button_delete = (ImageButton) view.findViewById(R.id.button_delete);

            if (Build.VERSION.SDK_INT >= 14) {
                switch_enabled = (SwitchCompat) view.findViewById(R.id.switch_enabled);        // switch used by api >= 14 (otherwise null)
            } else {
                check_enabled = (CheckBox) view.findViewById(R.id.switch_enabled);              // checkbox used by api < 14 (otherwise null)
            }
        }

        public void triggerPreviewOffset(final Context context, final AlarmClockItem item)
        {
            if (preview_offset_transition || item.offset == 0) {
                return;
            }

            preview_offset = true;
            preview_offset_transition = true;
            bindData(context, item);

            cardTray.postDelayed(new Runnable() {
                @Override
                public void run() {
                    preview_offset_transition = false;
                    preview_offset = !isSelected;
                    bindData(context, item);
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
                            R.attr.buttonPressColor };
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
            a.recycle();
        }


        public void bindData(Context context, @NonNull AlarmClockItem item)
        {
            themeViews(context);
            updateView(context, this, item);
        }

        protected void updateView(Context context, AlarmListDialogItem view, @NonNull final AlarmClockItem item)
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
            view.cardBackdrop.setBackgroundColor( isSelected ? ColorUtils.setAlphaComponent(color_selected, 170) : color_notselected);  // 66% alpha
            if (Build.VERSION.SDK_INT >= 16) {
                view.card.setBackground(item.enabled ? ContextCompat.getDrawable(context, res_backgroundOn) : ContextCompat.getDrawable(context, res_backgroundOff));
            } else {
                view.card.setBackgroundDrawable(item.enabled ? ContextCompat.getDrawable(context, res_backgroundOn) : ContextCompat.getDrawable(context, res_backgroundOff));
            }

            // enabled / disabled
            if (Build.VERSION.SDK_INT >= 14) {
                if (view.switch_enabled != null) {
                    view.switch_enabled.setChecked(item.enabled);
                }
            } else {
                if (view.check_enabled != null) {
                    view.check_enabled.setChecked(item.enabled);
                }
            }

            // type button
            if (view.typeButton != null) {
                view.typeButton.setImageDrawable(ContextCompat.getDrawable(context, (item.type == AlarmClockItem.AlarmType.ALARM ? res_iconAlarm : res_iconNotification)));
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
                    Drawable eventIcon = SolarEventIcons.getIconDrawable(context, event, (int)eventIconSize, (int)eventIconSize);
                    view.text_event.setCompoundDrawablePadding(SolarEventIcons.getIconDrawablePadding(context, event));
                    view.text_event.setCompoundDrawables(eventIcon, null, null, null);

                } else {
                    Drawable eventIcon = SolarEventIcons.getIconDrawable(context, item.timezone, (int)eventIconSize, (int)eventIconSize);
                    if (item.timezone == null) {
                        SolarEventIcons.tintDrawable(eventIcon, item.enabled ? color_on : color_off);
                    }
                    text_event.setCompoundDrawablePadding(SolarEventIcons.getIconDrawablePadding(context, item.timezone));
                    text_event.setCompoundDrawables(eventIcon, null, null, null);
                }
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
            if (view.text_location != null) {
                view.text_location.setVisibility((item.getEvent() == null && item.timezone == null) ? View.INVISIBLE : View.VISIBLE);
                view.text_location.setText(item.location.getLabel());
                view.text_location.setTextColor(item.enabled ? color_on : color_off);

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
                boolean noRepeat = item.repeatingDays == null || item.repeatingDays.isEmpty();
                String repeatText = AlarmClockItem.repeatsEveryDay(item.repeatingDays)
                        ? context.getString(R.string.alarmOption_repeat_all)
                        : noRepeat
                            ? context.getString(R.string.alarmOption_repeat_none)
                            : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);

                if (item.repeating && AlarmEvent.supportsRepeating(eventType) == AlarmEventContract.REPEAT_SUPPORT_BASIC) {
                    repeatText = context.getString(R.string.alarmOption_repeat);
                }

                view.text_repeat.setText(repeatText);
                view.text_repeat.setTextColor(item.enabled ? color_on : color_off);
                view.text_repeat.setVisibility((noRepeat) ? View.GONE : View.VISIBLE);
            }

            // offset (before / after)
            if (view.text_offset != null)
            {
                CharSequence offsetDisplay = (preview_offset ? "" : AlarmEditViewHolder.displayOffset(context, item));
                view.text_offset.setText((isSchedulable && isSelected) ? offsetDisplay : "");

                if (preview_offset && item.offset != 0) {
                    view.text_offset.setText(SuntimesUtils.createSpan(context, "i", "i", new ImageSpan(offsetIcon), ImageSpan.ALIGN_BASELINE));
                }

                view.text_offset.setTextColor(item.enabled ? color_on : color_off);
            }

            // extended tray
            if (view.cardTray != null) {
                view.cardTray.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }
            if (view.text_note != null) {
                view.text_note.setText(isSelected ? AlarmEditViewHolder.displayAlarmNote(context, item, isSchedulable) : "");
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
    }

    private RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration()
    {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            int position = parent.getChildAdapterPosition(view);
            if (position == adapter.getItemCount() - 1) {   // add bottom margin on last item to avoid blocking FAB
                outRect.bottom = 400;
            } else {
                super.getItemOffsets(outRect, view, parent, state);
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected AdapterListener listener;
    protected AdapterListener adapterListener = new AdapterListener()
    {
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
        void onItemClicked(AlarmClockItem item, AlarmListDialogItem view);
        boolean onItemLongClicked(AlarmClockItem item);
        void onItemNoteClicked(AlarmClockItem item, AlarmListDialogItem view);
        void onAlarmToggled(AlarmClockItem item, boolean enabled);
        void onAlarmAdded(AlarmClockItem item);
        void onAlarmDeleted(long rowID);
        void onAlarmsCleared();
    }

}
