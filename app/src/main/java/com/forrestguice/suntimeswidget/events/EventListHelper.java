/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventListHelper
{
    public static final int REQUEST_IMPORT_URI = 300;
    public static final int REQUEST_EXPORT_URI = 400;

    public static final String DIALOGTAG_ADD = "add";
    public static final String DIALOGTAG_EDIT = "edit";
    public static final String DIALOGTAG_HELP = "help";
    private static final int HELP_PATH_ID = R.string.help_eventlist_path;

    private WeakReference<Context> contextRef;
    private android.support.v4.app.FragmentManager fragmentManager;

    private int selectedChild = -1;
    private EventSettings.EventAlias selectedItem;
    private ListView list;
    private EventDisplayAdapterInterface adapter;
    protected ActionMode actionMode = null;
    protected EventAliasActionMode1 actionModeCallback = new EventAliasActionMode1();

    protected View emptyView;
    protected ProgressBar progress;
    protected View progressLayout;

    protected boolean adapterModified = false;
    public boolean isAdapterModified() {
        return adapterModified;
    }

    public EventListHelper(@NonNull Context context, @NonNull android.support.v4.app.FragmentManager fragments)
    {
        contextRef = new WeakReference<>(context);
        setFragmentManager(fragments);
    }

    private View.OnClickListener onItemSelected = null;
    public void setOnItemAcceptedListener(View.OnClickListener listener) {
        onItemSelected = listener;
    }

    private View.OnClickListener onUpdateViews = null;
    public void setOnUpdateViews(View.OnClickListener listener) {
        onUpdateViews = listener;
    }

    public void setFragmentManager(android.support.v4.app.FragmentManager fragments) {
        fragmentManager = fragments;
    }

    private boolean disallowSelect = false;
    public void setDisallowSelect( boolean value ) {
        disallowSelect = value;
    }

    private boolean expanded = false;
    public void setExpanded( boolean value ) {
        expanded = value;
    }

    public void setSelected( String eventID ) {
        //Log.d("DEBUG", "setSelected: " + eventID);
        selectedItem = adapter.findItemByID(eventID);
        adapter.setSelected(selectedItem);
    }

    private Location location = null;
    public void setLocation(Location value) {
        location = value;
    }

    public void onRestoreInstanceState(Bundle savedState)
    {
        expanded = savedState.getBoolean("expanded", expanded);
        disallowSelect = savedState.getBoolean("disallowSelect", disallowSelect);
        adapterModified = savedState.getBoolean("adapterModified", adapterModified);

        String eventID = savedState.getString("selectedItem");
        if (eventID != null && !eventID.trim().isEmpty()) {
            setSelected(eventID);
            triggerActionMode(list, adapter.getSelected(), adapter.getSelectedChild());
        }
    }

    public void onSaveInstanceState( Bundle outState )
    {
        outState.putBoolean("expanded", expanded);
        outState.putBoolean("disallowSelect", disallowSelect);
        outState.putBoolean("adapterModified", adapterModified);
        outState.putString("selectedItem", selectedItem != null ? selectedItem.getID() : "");
    }

    public void onResume()
    {
        EditEventDialog addDialog = (EditEventDialog) fragmentManager.findFragmentByTag(DIALOGTAG_ADD);
        if (addDialog != null) {
            addDialog.setOnAcceptedListener(onEventSaved(contextRef.get(), addDialog));
        }

        EditEventDialog editDialog = (EditEventDialog) fragmentManager.findFragmentByTag(DIALOGTAG_EDIT);
        if (editDialog != null) {
            editDialog.setOnAcceptedListener(onEventSaved(contextRef.get(), editDialog));
        }

        HelpDialog helpDialog = (HelpDialog) fragmentManager.findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(contextRef.get(), HELP_PATH_ID), DIALOGTAG_HELP);
        }
    }

    public String getEventID()
    {
        if (list != null) {
            EventSettings.EventAlias selected = adapter.getSelected();
            return selected != null ? selected.getID() : null;
        } else return null;
    }

    public String getAliasUri()
    {
        if (list != null)
        {
            String suffix = "";
            if (selectedChild >= 0) {
                suffix = ((selectedChild == 0) ? AlarmEventProvider.ElevationEvent.SUFFIX_RISING : AlarmEventProvider.ElevationEvent.SUFFIX_SETTING);
            }

            EventSettings.EventAlias selected = adapter.getSelected();
            return selected != null ? selected.getAliasUri() + suffix : null;
        } else return null;
    }

    public String getLabel()
    {
        if (list != null) {
            EventSettings.EventAlias selected = (EventSettings.EventAlias) list.getSelectedItem();
            return selected != null ? selected.getLabel() : null;
        } else return null;
    }

    public ListView getListView() {
        return list;
    }

    protected void updateViews(Context context)
    {
        if (onUpdateViews != null) {
            onUpdateViews.onClick(list);
        }
    }

    public void initViews(Context context, View content, @Nullable Bundle savedState)
    {
        if (content == null) {
            return;
        }

        list = expanded ? (ListView) content.findViewById(R.id.explist_events) : (ListView) content.findViewById(R.id.list_events);
        list.setVisibility(View.VISIBLE);

        emptyView = content.findViewById(android.R.id.empty);
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }

        progress = (ProgressBar) content.findViewById(R.id.progress);
        progressLayout = content.findViewById(R.id.progressLayout);
        showProgress(false);

        if (expanded)
        {
            final ExpandableListView expandedList = (ExpandableListView) list;

            expandedList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition)
                {
                    for (int i=0; i<expandedList.getCount(); i++) {
                        if (i != groupPosition) {
                            expandedList.collapseGroup(i);
                        }
                    }
                    adapter.setSelected(selectedItem = (EventSettings.EventAlias) expandedList.getAdapter().getItem(groupPosition));
                    adapter.setSelected(selectedChild = 0);
                    updateViews(contextRef.get());
                    triggerActionMode(expandedList.getSelectedView(), selectedItem, selectedChild);
                }
            });

            expandedList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
                {
                    adapter.setSelected(selectedItem = (EventSettings.EventAlias) expandedList.getAdapter().getItem(groupPosition));
                    adapter.setSelected(selectedChild = childPosition);
                    updateViews(contextRef.get());
                    triggerActionMode(view, selectedItem, childPosition);
                    return true;
                }
            });

        } else {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    list.setSelection(position);
                    adapter.setSelected(selectedItem = (EventSettings.EventAlias) list.getItemAtPosition(position));
                    updateViews(contextRef.get());
                    triggerActionMode(view, selectedItem);
                }
            });
        }
        initAdapter(context);

        //ImageButton button_menu = (ImageButton) content.findViewById(R.id.edit_event_menu);
        //if (button_menu != null) {
        //    button_menu.setOnClickListener(onMenuButtonClicked);
        //}

        if (savedState != null) {
            onRestoreInstanceState(savedState);
        }
    }

    protected void initAdapter(Context context)
    {
        List<EventSettings.EventAlias> events = EventSettings.loadEvents(context, AlarmEventProvider.EventType.SUN_ELEVATION);
        events.addAll(EventSettings.loadEvents(context, AlarmEventProvider.EventType.SHADOWLENGTH));

        Collections.sort(events, new Comparator<EventSettings.EventAlias>() {
            @Override
            public int compare(EventSettings.EventAlias o1, EventSettings.EventAlias o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        if (emptyView != null) {
            emptyView.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (expanded)
        {
            ExpandableEventDisplayAdapter adapter0 = new ExpandableEventDisplayAdapter(context, R.layout.layout_listitem_events, R.layout.layout_listitem_events1, events);
            adapter0.setLocation(location);
            ExpandableListView expandedList = (ExpandableListView) list;
            expandedList.setAdapter(adapter0);
            adapter = adapter0;

        } else {
            EventDisplayAdapter adapter0 = new EventDisplayAdapter(context, R.layout.layout_listitem_events, events.toArray(new EventSettings.EventAlias[0]));
            list.setAdapter(adapter0);
            adapter = adapter0;
        }
    }

    protected View.OnClickListener onMenuButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            showOverflowMenu(contextRef.get(), v);
        }
    };

    protected void showOverflowMenu(Context context, View parent)
    {
        PopupMenu menu = new PopupMenu(context, parent);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.eventlist, menu.getMenu());
        menu.setOnMenuItemClickListener(onMenuItemClicked);
        PopupMenuCompat.forceActionBarIcons(menu.getMenu());
        prepareOverflowMenu(context, menu.getMenu());
        menu.show();
    }

    protected void prepareOverflowMenu(Context context, Menu menu)
    {
        String eventID = getEventID();
        boolean isModifiable = eventID != null && !eventID.trim().isEmpty();

        MenuItem editItem = menu.findItem(R.id.editEvent);
        if (editItem != null) {
            editItem.setEnabled(isModifiable);
            editItem.setVisible(isModifiable);
        }

        MenuItem deleteItem = menu.findItem(R.id.deleteEvent);
        if (deleteItem != null) {
            deleteItem.setEnabled(isModifiable);
            deleteItem.setVisible(isModifiable);
        }
    }

    protected PopupMenu.OnMenuItemClickListener onMenuItemClicked = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                //case R.id.addEvent:
                //    addEvent();
                //    return true;

                case R.id.editEvent:
                    editEvent(getEventID());
                    return true;

                case R.id.clearEvents:
                    clearEvents();
                    return true;

                case R.id.deleteEvent:
                    deleteEvent(getEventID());
                    return true;

                case R.id.helpEvents:
                    showHelp();
                    return true;

                default:
                    return false;
            }
        }
    });

    public void addEvent() {
        addEvent(AlarmEventProvider.EventType.SUN_ELEVATION);
    }
    public EditEventDialog addEvent(AlarmEventProvider.EventType type) {
        return addEvent(type, null, null, null);
    }
    public EditEventDialog addEvent(AlarmEventProvider.EventType type, final Double angle, final Double shadowLength, final Double objHeight)
    {
        final Context context = contextRef.get();
        final EditEventDialog saveDialog = new EditEventDialog();
        saveDialog.setType(type);
        saveDialog.setDialogMode(EditEventDialog.DIALOG_MODE_ADD);
        saveDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                saveDialog.setIsModified(false);
                if (angle != null && saveDialog.edit_angle != null) {
                    saveDialog.edit_angle.setText(Double.toString(angle));
                    saveDialog.edit_label.setText(saveDialog.edit_label.getText() + " " + angle.intValue());
                    saveDialog.check_shown.setChecked(true);
                    saveDialog.edit_label.selectAll();
                    saveDialog.setIsModified(true);
                    saveDialog.edit_label.requestFocus();
                }

                if (shadowLength != null || objHeight != null)
                {
                    if (objHeight != null && saveDialog.edit_objHeight != null) {
                        saveDialog.edit_objHeight.setText(Double.toString(objHeight));
                    }
                    if (shadowLength != null && saveDialog.edit_shadowLength != null) {
                        saveDialog.edit_shadowLength.setText(Double.toString(shadowLength));
                        saveDialog.edit_label.setText(saveDialog.edit_label.getText() + " " + shadowLength.intValue());
                    }
                    saveDialog.check_shown.setChecked(true);
                    saveDialog.edit_label.selectAll();
                    saveDialog.setIsModified(true);
                    saveDialog.edit_label.requestFocus();
                }
            }
        });
        saveDialog.setOnAcceptedListener(onEventSaved(context, saveDialog));
        saveDialog.show(fragmentManager, DIALOGTAG_ADD);
        return saveDialog;
    }

    protected void editEvent(final String eventID)
    {
        final Context context = contextRef.get();
        if (eventID != null && !eventID.trim().isEmpty() && context != null)
        {
            final EventSettings.EventAlias event = EventSettings.loadEvent(context, eventID);

            final EditEventDialog saveDialog = new EditEventDialog();
            saveDialog.setDialogMode(EditEventDialog.DIALOG_MODE_EDIT);
            saveDialog.setType(event.getType());
            saveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    saveDialog.setEvent(event);
                    saveDialog.setIsModified(false);
                }
            });

            saveDialog.setOnAcceptedListener(onEventSaved(context, saveDialog));
            saveDialog.show(fragmentManager, DIALOGTAG_EDIT);
        }
    }

    private DialogInterface.OnClickListener onEventSaved(final Context context, final EditEventDialog saveDialog)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String eventID = saveDialog.getEventID();
                EventSettings.saveEvent(context, saveDialog.getEvent());
                //Log.d("DEBUG", "onEventSaved " + saveDialog.getEvent().toString());
                //Toast.makeText(context, context.getString(R.string.saveevent_toast, saveDialog.getEventLabel(), eventID), Toast.LENGTH_SHORT).show();  // TODO
                initAdapter(context);
                updateViews(context);
                adapterModified = true;

                setSelected(eventID);
                triggerActionMode(list, selectedItem, selectedChild);
            }
        };
    }

    /**
     * Export Events
     */
    protected EventExportTask exportTask = null;

    public boolean exportEvents(Fragment fragment)
    {
        if (exportTask != null && importTask != null) {
            Log.e("ExportEvents", "Already busy importing/exporting! ignoring request");
            return false;
        }

        String exportTarget = "SuntimesEvents";
        Context context = contextRef.get();
        if (context != null && adapter != null)
        {
            EventSettings.EventAlias[] items = getItemsForExport();
            if (items.length > 0)
            {
                if (Build.VERSION.SDK_INT >= 19)
                {
                    String filename = exportTarget + EventExportTask.FILEEXT;
                    Intent intent = ExportTask.getCreateFileIntent(filename, EventExportTask.MIMETYPE);
                    try {
                        fragment.startActivityForResult(intent, REQUEST_EXPORT_URI);
                        return true;

                    } catch (ActivityNotFoundException e) {
                        Log.e("ExportEvents", "SAF is unavailable? (" + e + ").. falling back to legacy export method.");
                    }
                }
                exportTask = new EventExportTask(context, exportTarget, true, true);    // export to external cache
                exportTask.setItems(items);
                exportTask.setTaskListener(exportListener);
                exportTask.execute();
                return true;
            } else return false;
        }
        return false;
    }

    protected void exportEvents(Context context, @NonNull Uri uri)
    {
        if (exportTask != null && importTask != null) {
            Log.e("ExportEvents", "Already busy importing/exporting! ignoring request");

        } else {
            EventSettings.EventAlias[] items = getItemsForExport();
            if (items.length > 0)
            {
                exportTask = new EventExportTask(context, uri);    // export directly to uri
                exportTask.setItems(items);
                exportTask.setTaskListener(exportListener);
                exportTask.execute();
            }
        }
    }

    protected EventSettings.EventAlias[] getItemsForExport()
    {
        List<EventSettings.EventAlias> itemList = adapter.getItems();
        Collections.reverse(itemList);                                                // should be reversed for export (so import encounters/adds older items first)
        return itemList.toArray(new EventSettings.EventAlias[0]);
    }

    private ExportTask.TaskListener exportListener0 = null;
    public void setExportTaskListener(ExportTask.TaskListener listener) {
        exportListener0 = listener;
    }

    private final ExportTask.TaskListener exportListener = new ExportTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            showProgress(true);

            if (exportListener0 != null) {
                exportListener0.onStarted();
            }
        }

        @Override
        public void onFinished(ExportTask.ExportResult results)
        {
            exportTask = null;
            showProgress(false);

            if (exportListener0 != null) {
                exportListener0.onFinished(results);
            }
        }
    };

    protected void showProgress(boolean visible)
    {
        if (progress != null) {
            progress.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (progressLayout != null) {
            progressLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Import Events
     */
    protected EventImportTask importTask = null;

    public void importEvents(Fragment fragment)
    {
        if (importTask != null && exportTask != null) {
            Log.e("ImportEvents", "Already busy importing/exporting! ignoring request");
            return;
        }
        Intent intent = ExportTask.getOpenFileIntent(EventExportTask.MIMETYPE);
        fragment.startActivityForResult(intent, REQUEST_IMPORT_URI);
    }

    protected void importEvents(Context context, @NonNull Uri uri)
    {
        if (importTask != null && exportTask != null) {
            Log.e("ImportEvents", "Already busy importing/exporting! ignoring request");

        } else if (context != null) {
            importTask = new EventImportTask(context);
            importTask.setTaskListener(importListener);
            importTask.execute(uri);
        }
    }

    private EventImportTask.TaskListener importListener0 = null;
    public void setImportTaskListener(EventImportTask.TaskListener listener) {
        importListener0 = listener;
    }

    private EventImportTask.TaskListener importListener =  new EventImportTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            showProgress(true);

            if (importListener0 != null) {
                importListener0.onStarted();
            }
        }

        @Override
        public void onFinished(EventImportTask.TaskResult result)
        {
            importTask = null;
            showProgress(false);

            if (importListener0 != null) {
                importListener0.onFinished(result);
            }

            if (result.getResult())
            {
                Context context = contextRef.get();
                EventSettings.EventAlias[] items = result.getItems();

                if (context != null)
                {
                    for (int i=0; i<items.length; i++)
                    {
                        if (items[i] != null) {
                            EventSettings.saveEvent(context, items[i]);
                        }
                    }
                }

                initAdapter(context);
                updateViews(context);
                adapterModified = true;
                offerUndoImport(context, new ArrayList<EventSettings.EventAlias>(Arrays.asList(items)));
            }
        }
    };

    public void offerUndoImport(Context context, final List<EventSettings.EventAlias> items)
    {
        View view = list;
        if (context != null && view != null)
        {
            String plural = context.getResources().getQuantityString(R.plurals.eventPlural, items.size(), items.size());
            Snackbar snackbar = Snackbar.make(view, context.getString(R.string.importevents_toast_success, plural), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(context.getString(R.string.configAction_undo), new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context context = contextRef.get();
                    if (context != null)
                    {
                        for (EventSettings.EventAlias item : items) {
                            if (item != null) {
                                EventSettings.deleteEvent(context, item.getID());
                            }
                        }
                        initAdapter(context);
                        updateViews(context);
                        adapterModified = true;
                    }
                }
            });
            ViewUtils.themeSnackbar(context, snackbar, null);
            snackbar.setDuration(UNDO_IMPORT_MILLIS);
            snackbar.show();
        }
    }
    public static final int UNDO_IMPORT_MILLIS = 8000;

    /**
     * Delete Events
     */

    public void clearEvents()
    {
        final Context context = contextRef.get();
        if (context != null)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getString(R.string.clearevents_dialog_msg))
                    .setNegativeButton(context.getString(R.string.clearevents_dialog_cancel), null)
                    .setPositiveButton(context.getString(R.string.clearevents_dialog_ok),
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    EventSettings.deletePrefs(context);
                                    EventSettings.initDefaults(context);
                                    Toast.makeText(context, context.getString(R.string.clearevents_toast), Toast.LENGTH_SHORT).show();
                                    initAdapter(context);
                                    updateViews(context);
                                    adapterModified = true;
                                }
                            });
            dialog.show();
        }
    }

    protected void deleteEvent(final String eventID)
    {
        final Context context = contextRef.get();
        if (eventID != null && !eventID.trim().isEmpty() && context != null)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            String label = EventSettings.loadEventValue(context, eventID, EventSettings.PREF_KEY_EVENT_LABEL);

            dialog.setMessage(context.getString(R.string.delevent_dialog_msg, label, eventID))
                    .setNegativeButton(context.getString(R.string.delevent_dialog_cancel), null)
                    .setPositiveButton(context.getString(R.string.delevent_dialog_ok),
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    EventSettings.deleteEvent(context, eventID);
                                    adapterModified = true;

                                    list.post(new Runnable()
                                    {
                                        @Override
                                        public void run() {
                                            Context context = contextRef.get();
                                            if (context != null) {
                                                initAdapter(context);
                                                updateViews(context);
                                            }
                                        }
                                    });
                                }
                            });
            dialog.show();
        }
    }

    @SuppressLint("ResourceType")
    public void showHelp()
    {
        Context context = contextRef.get();
        if (context != null)
        {
            int iconSize = (int) context.getResources().getDimension(R.dimen.helpIcon_size);
            int[] iconAttrs = { R.attr.icActionNew, R.attr.icActionEdit, R.attr.icActionDelete, R.attr.icActionAccept, R.attr.icActionVisibilityOn };
            TypedArray typedArray = context.obtainStyledAttributes(iconAttrs);
            ImageSpan addIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(0, R.drawable.ic_action_new), iconSize, iconSize, 0);
            ImageSpan editIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(1, R.drawable.ic_action_edit), iconSize, iconSize, 0);
            ImageSpan deleteIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(2, R.drawable.ic_action_discard), iconSize, iconSize, 0);
            ImageSpan okIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(3, R.drawable.ic_action_accept), iconSize, iconSize, 0);
            ImageSpan viewIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(4, R.drawable.ic_action_visibility), iconSize, iconSize, 0);
            typedArray.recycle();

            SuntimesUtils.ImageSpanTag[] helpTags = {
                    new SuntimesUtils.ImageSpanTag("[Icon OK]", okIcon),
                    new SuntimesUtils.ImageSpanTag("[Icon Add]", addIcon),
                    new SuntimesUtils.ImageSpanTag("[Icon Edit]", editIcon),
                    new SuntimesUtils.ImageSpanTag("[Icon Delete]", deleteIcon),
                    new SuntimesUtils.ImageSpanTag("[Icon View]", viewIcon),
            };

            String helpString = context.getString(R.string.help_eventlist);
            SpannableStringBuilder helpSpan = SuntimesUtils.createSpan(context, helpString, helpTags);

            HelpDialog helpDialog = new HelpDialog();
            helpDialog.setContent(helpSpan);
            helpDialog.setShowNeutralButton(context.getString(R.string.configAction_onlineHelp));
            helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(context, HELP_PATH_ID), DIALOGTAG_HELP);
            helpDialog.show(fragmentManager, DIALOGTAG_HELP);
        }
    }

    /**
     * AdapterInterface
     */
    public interface EventDisplayAdapterInterface
    {
        EventSettings.EventAlias getSelected();
        int getSelectedChild();
        void setSelected( EventSettings.EventAlias item );
        void setSelected(int i);
        EventSettings.EventAlias findItemByID(String eventID);
        List<EventSettings.EventAlias> getItems();
    }

    /**
     * ExpandableEventDisplayAdapter
     */
    public static class ExpandableEventDisplayAdapter extends BaseExpandableListAdapter implements EventDisplayAdapterInterface
    {
        private WeakReference<Context> contextRef;
        private int groupResourceID, childResourceID;
        private List<EventSettings.EventAlias> objects;
        private EventSettings.EventAlias selectedItem;
        private int selectedChild = -1;
        private final SuntimesUtils utils = new SuntimesUtils();

        public ExpandableEventDisplayAdapter(Context context, int groupResourceID, int childResourceID, @NonNull List<EventSettings.EventAlias> objects)
        {
            this.contextRef = new WeakReference<>(context);
            this.groupResourceID = groupResourceID;
            this.childResourceID = childResourceID;
            this.objects = objects;
            SuntimesUtils.initDisplayStrings(context);
        }

        @Override
        public int getGroupCount() {
            return objects.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return objects.get(groupPosition);
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 2;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent)
        {
            Context context = contextRef.get();
            if (context == null) {
                return view;
            }
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(groupResourceID, parent, false);
            }

            view.setPadding((int)context.getResources().getDimension(R.dimen.eventIcon_width), 0, 0, 0);

            EventSettings.EventAlias item = (EventSettings.EventAlias) getGroup(groupPosition);
            if (item == null) {
                Log.w("getGroupView", "group at position " + groupPosition + " is null.");
                return view;
            }

            if (selectedItem != null && item.getID().equals(selectedItem.getID())) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.text_accent_dark));
            } else view.setBackgroundColor(Color.TRANSPARENT);


            TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
            if (primaryText != null) {
                primaryText.setText(item.toString());
            }

            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null) {
                secondaryText.setText(item.getSummary(context));
            }

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            if (icon != null) {
                icon.setBackgroundColor(item.getColor());
            }

            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent)
        {
            Context context = contextRef.get();
            if (context == null) {
                return view;
            }
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(childResourceID, parent, false);
            }

            view.setPadding((int)context.getResources().getDimension(R.dimen.eventIcon_width), 0, 0, 0);

            boolean rising = (childPosition == 0);
            EventSettings.EventAlias item = (EventSettings.EventAlias) getGroup(groupPosition);
            String displayString = item.toString() + " " + context.getString(rising ? R.string.eventalias_title_tag_rising : R.string.eventalias_title_tag_setting);  // TODO

            if (selectedItem != null && item.getID().equals(selectedItem.getID()) && (selectedChild == childPosition)) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.text_accent_dark));
            } else view.setBackgroundColor(Color.TRANSPARENT);

            TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
            if (primaryText != null) {
                primaryText.setText(displayString);
            }

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            if (icon != null)
            {
                Drawable drawable = ContextCompat.getDrawable(context, (rising ? R.drawable.svg_sunrise : R.drawable.svg_sunset));
                EventIcons.tintDrawable(drawable, item.getColor());
                icon.setImageDrawable( drawable );
            }

            TextView timeText = (TextView)view.findViewById(R.id.time_preview);
            if (timeText != null)
            {
                Calendar now = Calendar.getInstance();
                String uri = item.getUri() + (rising ? AlarmEventProvider.ElevationEvent.SUFFIX_RISING : AlarmEventProvider.ElevationEvent.SUFFIX_SETTING);
                Calendar eventTime = AlarmNotifications.updateAlarmTime_addonEvent(context, context.getContentResolver(), uri, getLocation(context), 0, false, AlarmClockItem.everyday(), now);
                boolean isSoon = (eventTime != null && (Math.abs(now.getTimeInMillis() - eventTime.getTimeInMillis()) < 1000 * 60 * 260 * 48));
                timeText.setText(eventTime != null
                        ? ( isSoon ? utils.calendarTimeShortDisplayString(context, eventTime).toString()
                                   : utils.calendarDateTimeDisplayString(context, eventTime, false, true, false, true).getValue())
                        : "");
                timeText.setVisibility(eventTime != null ? View.VISIBLE : View.GONE);
            }

            return view;
        }

        private Location location = null;
        public void setLocation(@Nullable Location value) {
            location = value;
        }
        public Location getLocation(Context context) {
            if (location == null) {
                location = WidgetSettings.loadLocationPref(context, 0);
            }
            return location;
        }

        public void setSelected( EventSettings.EventAlias item ) {
            selectedItem = item;
            notifyDataSetChanged();
        }

        @Override
        public void setSelected(int i) {
            selectedChild = i;
        }

        public EventSettings.EventAlias getSelected() {
            return selectedItem;
        }

        public int getSelectedChild() {
            return selectedChild;
        }

        public EventSettings.EventAlias findItemByID(String eventID)
        {
            for (int i=0; i<objects.size(); i++) {
                EventSettings.EventAlias item = objects.get(i);
                if (item != null && item.getID().equals(eventID)) {
                    //Log.d("DEBUG", "findItemByID: " + eventID + " .. " + item.toString());
                    return item;
                }
            }
            //Log.d("DEBUG", "findItemByID: " + eventID + " .. null");
            return null;
        }

        @Override
        public List<EventSettings.EventAlias> getItems() {
            return objects;
        }
    }

    /**
     * EventDisplayAdapter
     */
    public static class EventDisplayAdapter extends ArrayAdapter<EventSettings.EventAlias> implements EventDisplayAdapterInterface
    {
        private int resourceID, dropDownResourceID;
        private EventSettings.EventAlias selectedItem;

        public EventDisplayAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            init(context, resource);
        }

        public EventDisplayAdapter(@NonNull Context context, int resource, @NonNull EventSettings.EventAlias[] objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        public EventDisplayAdapter(@NonNull Context context, int resource, @NonNull List<EventSettings.EventAlias> objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        private void init(@NonNull Context context, int resource) {
            resourceID = dropDownResourceID = resource;
        }

        public void setSelected( EventSettings.EventAlias item ) {
            selectedItem = item;
            notifyDataSetChanged();
        }

        @Override
        public void setSelected(int i) {
            /* EMPTY */
        }

        public EventSettings.EventAlias getSelected() {
            return selectedItem;
        }

        public int getSelectedChild() {
            return -1;
        }

        public EventSettings.EventAlias findItemByID(String eventID)
        {
            for (int i=0; i<getCount(); i++) {
                EventSettings.EventAlias item = getItem(i);
                if (item != null && item.getID().equals(eventID)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public List<EventSettings.EventAlias> getItems()
        {
            ArrayList<EventSettings.EventAlias> items = new ArrayList<>();
            for (int i=0; i<getCount(); i++)
            {
                EventSettings.EventAlias item = getItem(i);
                if (item != null) {
                    items.add(item);
                }
            }
            return items;
        }

        @Override
        public void setDropDownViewResource(int resID) {
            super.setDropDownViewResource(resID);
            dropDownResourceID = resID;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, dropDownResourceID);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, resourceID);
        }

        private View getItemView(int position, View convertView, @NonNull ViewGroup parent, int resID)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(resID, parent, false);

            EventSettings.EventAlias item = getItem(position);
            if (item == null) {
                Log.w("getItemView", "item at position " + position + " is null.");
                return view;
            }

            if (selectedItem != null && item.getID().equals(selectedItem.getID())) {
                Log.d("DEBUG", "getItemView: " + selectedItem.getID());
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.text_accent_dark));
            } else view.setBackgroundColor(Color.TRANSPARENT);

            TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
            if (primaryText != null) {
                primaryText.setText(item.toString());
            }

            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null) {
                secondaryText.setText(item.getSummary(getContext()));
            }

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            if (icon != null) {
                icon.setBackgroundColor(item.getColor());
            }

            CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            if (checkbox != null) {
                checkbox.setChecked(EventSettings.isShown(getContext(), item.getID()));
                checkbox.setVisibility(checkbox.isChecked() ? View.VISIBLE : View.INVISIBLE);
            }

            return view;
        }
    }

    /**
     * triggerActionMode
     */
    public boolean triggerActionMode() {
        return triggerActionMode(list, selectedItem, selectedChild);
    }
    protected boolean triggerActionMode(View view, EventSettings.EventAlias item) {
        return triggerActionMode(view, item, -1);
    }
    protected boolean triggerActionMode(View view, EventSettings.EventAlias item, int i)
    {
        Context context = contextRef.get();
        if (context == null) {
             return false;
        }

        if (Build.VERSION.SDK_INT >= 11)
        {
            if (actionMode == null)
            {
                if (item != null)
                {
                    setSelected(item.getID());
                    actionModeCallback.setItem(item);
                    actionModeCallback.setItemChild(i);
                    actionMode = list.startActionModeForChild(view, actionModeCallback);
                    if (actionMode != null)
                    {
                        if (i >= 0) {
                            boolean rising = (i == 0);
                            actionMode.setTitle(context.getString(R.string.eventalias_title_format, item.getLabel(), context.getString(rising ? R.string.eventalias_title_tag_rising : R.string.eventalias_title_tag_setting)));
                        } else actionMode.setTitle(item.getLabel());
                    }
                }
                return true;

            } else {
                actionMode.finish();
                triggerActionMode(view, item, i);
                return false;
            }

        } else {
            Toast.makeText(contextRef.get(), "TODO", Toast.LENGTH_SHORT).show();  // TODO: legacy support
            return false;
        }
    }

    /**
     * EventAliasActionMode
     */
    private class EventAliasActionModeBase
    {
        public EventAliasActionModeBase() {
        }

        protected EventSettings.EventAlias event = null;
        public void setItem(EventSettings.EventAlias item) {
            event = item;
        }

        protected int itemChild = -1;
        public void setItemChild(int i) {
            itemChild = i;
        }

        protected boolean onCreateActionMode(MenuInflater inflater, Menu menu) {
            inflater.inflate(R.menu.eventlist1, menu);
            return true;
        }

        protected void onDestroyActionMode() {
            actionMode = null;
            setSelected(null);
        }

        protected boolean onPrepareActionMode(Menu menu)
        {
            PopupMenuCompat.forceActionBarIcons(menu);
            MenuItem selectItem = menu.findItem(R.id.selectEvent);
            selectItem.setVisible( !disallowSelect );

            String eventID = event.getID();
            boolean isModifiable = (eventID != null && !eventID.trim().isEmpty());

            MenuItem deleteItem = menu.findItem(R.id.deleteEvent);
            MenuItem editItem = menu.findItem(R.id.editEvent);
            deleteItem.setVisible( isModifiable );
            editItem.setVisible( isModifiable );
            return false;
        }

        protected boolean onActionItemClicked(MenuItem item)
        {
            if (event != null)
            {
                switch (item.getItemId())
                {
                    case R.id.selectEvent:
                        if (onItemSelected != null) {
                            onItemSelected.onClick(list);
                        }
                        return true;

                    case R.id.deleteEvent:
                        deleteEvent(event.getID());
                        return true;

                    case R.id.editEvent:
                        editEvent(event.getID());
                        return true;
                }
            }
            return false;
        }
    }

    private class EventAliasActionMode extends EventAliasActionModeBase implements android.support.v7.view.ActionMode.Callback
    {
        public EventAliasActionMode() {
            super();
        }
        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            return onCreateActionMode(mode.getMenuInflater(), menu);
        }
        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
            onDestroyActionMode();
        }
        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            return onPrepareActionMode(menu);
        }
        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item)
        {
            boolean result = onActionItemClicked(item);
            mode.finish();
            return result;
        }
    }

    @TargetApi(11)
    private class EventAliasActionMode1 extends EventAliasActionModeBase implements ActionMode.Callback
    {
        public EventAliasActionMode1() {
            super();
        }
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return onCreateActionMode(mode.getMenuInflater(), menu);
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            onDestroyActionMode();
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return onPrepareActionMode(menu);
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            boolean result = onActionItemClicked(item);
            mode.finish();
            return result;
        }
    }

}
