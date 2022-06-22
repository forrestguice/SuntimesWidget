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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventListHelper
{
    public static final String DIALOGTAG_ADD = "add";
    public static final String DIALOGTAG_EDIT = "edit";
    public static final String DIALOGTAG_HELP = "help";

    private WeakReference<Context> contextRef;
    private android.support.v4.app.FragmentManager fragmentManager;

    private EventSettings.EventAlias selectedItem;
    private ListView list;
    private EventDisplayAdapter adapter;
    protected ActionMode actionMode = null;
    protected EventAliasActionMode1 actionModeCallback = new EventAliasActionMode1();

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

    public void setSelected( String eventID ) {
        Log.d("DEBUG", "setSelected: " + eventID);
        selectedItem = adapter.findItemByID(eventID);
        adapter.setSelected(selectedItem);
    }

    public void onRestoreInstanceState(Bundle savedState)
    {
        disallowSelect = savedState.getBoolean("disallowSelect", disallowSelect);
        adapterModified = savedState.getBoolean("adapterModified", adapterModified);

        String eventID = savedState.getString("selectedItem");
        if (eventID != null && !eventID.trim().isEmpty()) {
            setSelected(eventID);
            triggerActionMode(list, adapter.getSelected());
        }
    }

    public void onSaveInstanceState( Bundle outState )
    {
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
        if (list != null) {
            EventSettings.EventAlias selected = adapter.getSelected();
            return selected != null ? selected.getAliasUri() : null;
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

        list = (ListView) content.findViewById(R.id.list_events);
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
        Collections.sort(events, new Comparator<EventSettings.EventAlias>() {
            @Override
            public int compare(EventSettings.EventAlias o1, EventSettings.EventAlias o2) {
                return o1.getID().compareTo(o2.getID());
            }
        });
        adapter = new EventDisplayAdapter(context, R.layout.layout_listitem_events, events.toArray(new EventSettings.EventAlias[0]));
        list.setAdapter(adapter);
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
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
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

    protected PopupMenu.OnMenuItemClickListener onMenuItemClicked = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.addEvent:
                    addEvent();
                    return true;

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
    };

    public void addEvent()
    {
        final Context context = contextRef.get();
        final EditEventDialog saveDialog = new EditEventDialog();
        saveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //saveDialog.setData(data);
            }
        });
        saveDialog.setOnAcceptedListener(onEventSaved(context, saveDialog));
        saveDialog.show(fragmentManager, DIALOGTAG_ADD);
    }

    protected void editEvent(final String eventID)
    {
        final Context context = contextRef.get();
        if (eventID != null && !eventID.trim().isEmpty() && context != null)
        {
            final EditEventDialog saveDialog = new EditEventDialog();
            saveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    saveDialog.setEvent(EventSettings.loadEvent(context, eventID));
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
                //Toast.makeText(context, context.getString(R.string.saveevent_toast, saveDialog.getEventLabel(), eventID), Toast.LENGTH_SHORT).show();  // TODO
                initAdapter(context);
                updateViews(context);
                adapterModified = true;

                setSelected(eventID);
                triggerActionMode(list, selectedItem);
            }
        };
    }

    public void exportEvents() {
        // TODO
    }

    public void importEvents() {
        // TODO
        adapterModified = true;
    }

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

    public void showHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(contextRef.get().getString(R.string.help_eventlist));
        helpDialog.show(fragmentManager, DIALOGTAG_HELP);
    }

    /**
     * EventDisplayAdapter
     */
    public static class EventDisplayAdapter extends ArrayAdapter<EventSettings.EventAlias>
    {
        private int resourceID, dropDownResourceID;
        private EventSettings.EventAlias selectedItem;
        private EventSettings.EventAlias[] objects;

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
        public EventSettings.EventAlias getSelected() {
            return selectedItem;
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
            primaryText.setText(item.toString());

            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null) {
                secondaryText.setText(item.getSummary(getContext()));
            }

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            if (icon != null)
            {
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

                int[] attrs = { R.attr.icActionExtension };    // TODO: icon based on type, use color
                TypedArray a = getContext().obtainStyledAttributes(attrs);
                icon.setImageDrawable(ContextCompat.getDrawable(getContext(), a.getResourceId(0, R.drawable.ic_action_extension)));
                a.recycle();
            }
            return view;
        }

        private int getColorForPosition(int position) {
            EventSettings.EventAlias item = getItem(position);
            return item != null ? item.getColor() : EventSettings.PREF_DEF_EVENT_COLOR;
        }
    }

    /**
     * triggerActionMode
     */
    public boolean triggerActionMode() {
        return triggerActionMode(list, selectedItem);
    }
    protected boolean triggerActionMode(View view, EventSettings.EventAlias item)
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            if (actionMode == null)
            {
                if (item != null)
                {
                    setSelected(item.getID());
                    actionModeCallback.setItem(item);
                    actionMode = list.startActionModeForChild(view, actionModeCallback);
                    if (actionMode != null) {
                        actionMode.setTitle(item.getLabel());
                    }
                }
                return true;

            } else {
                actionMode.finish();
                triggerActionMode(view, item);
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
            SuntimesUtils.forceActionBarIcons(menu);
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
