/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.actions;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.design.app.AlertDialog;
import com.forrestguice.support.design.app.FragmentManagerInterface;
import com.forrestguice.support.design.view.ActionModeHelper;
import com.forrestguice.support.design.widget.PopupMenu;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * LoadActionDialog
 */
public class ActionListHelper
{
    public static final String DIALOGTAG_ADD = "add";
    public static final String DIALOGTAG_EDIT = "edit";

    private WeakReference<Context> contextRef;
    private FragmentManagerInterface fragmentManager;

    private ActionDisplay selectedItem;
    private ListView list;
    private ActionDisplayAdapter adapter;
    protected ActionMode actionMode = null;
    protected ActionDisplayActionMode1 actionModeCallback = new ActionDisplayActionMode1();

    protected boolean adapterModified = false;
    public boolean isAdapterModified() {
        return adapterModified;
    }

    public ActionListHelper(@NonNull Context context, @NonNull FragmentManagerInterface fragments)
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

    public void setFragmentManager(FragmentManagerInterface fragments) {
        fragmentManager = fragments;
    }

    private boolean disallowSelect = false;
    public void setDisallowSelect( boolean value ) {
        disallowSelect = value;
    }

    public void setSelected( String actionID ) {
        adapter.setSelected(selectedItem = adapter.findItemByID(actionID));
    }

    protected SuntimesData data = null;
    public void setData(@Nullable SuntimesData data) {
        this.data = data;
    }

    public void onRestoreInstanceState(Bundle savedState)
    {
        disallowSelect = savedState.getBoolean("disallowSelect", disallowSelect);
        adapterModified = savedState.getBoolean("adapterModified", adapterModified);

        String actionID = savedState.getString("selectedItem");
        if (actionID != null && !actionID.trim().isEmpty()) {
            setSelected(actionID);
            triggerActionMode(list, adapter.getSelected());
        }
    }

    public void onSaveInstanceState( Bundle outState )
    {
        outState.putBoolean("disallowSelect", disallowSelect);
        outState.putBoolean("adapterModified", adapterModified);
        outState.putString("selectedItem", selectedItem != null ? selectedItem.id : "");
    }

    public void onResume()
    {
        SaveActionDialog addDialog = (SaveActionDialog) fragmentManager.get().findFragmentByTag(DIALOGTAG_ADD);
        if (addDialog != null)
        {
            addDialog.setOnAcceptedListener(onActionSaved(contextRef.get(), addDialog));
            addDialog.getEdit().setFragmentManager(fragmentManager);
        }

        SaveActionDialog editDialog = (SaveActionDialog) fragmentManager.get().findFragmentByTag(DIALOGTAG_EDIT);
        if (editDialog != null)
        {
            editDialog.setOnAcceptedListener(onActionSaved(contextRef.get(), editDialog));
            editDialog.getEdit().setFragmentManager(fragmentManager);
        }
    }

    public String getIntentID()
    {
        if (list != null) {
            ActionDisplay selected = adapter.getSelected();
            return selected != null ? selected.id : null;
        } else return null;
    }

    public String getIntentTitle()
    {
        if (list != null) {
            ActionDisplay selected = (ActionDisplay) list.getSelectedItem();
            return selected != null ? selected.title : null;
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

        list = (ListView) content.findViewById(R.id.list_intentid);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                list.setSelection(position);
                adapter.setSelected(selectedItem = (ActionDisplay) list.getItemAtPosition(position));
                updateViews(contextRef.get());
                triggerActionMode(view, selectedItem);
            }
        });
        initAdapter(context);

        ImageButton button_menu = (ImageButton) content.findViewById(R.id.edit_intent_menu);
        if (button_menu != null) {
            button_menu.setOnClickListener(onMenuButtonClicked);
        }

        if (savedState != null) {
            onRestoreInstanceState(savedState);
        }
    }

    protected void initAdapter(Context context)
    {
        ArrayList<ActionDisplay> ids = new ArrayList<>();
        Set<String> intentIDs = WidgetActions.loadActionLaunchList(context, 0);
        for (String id : intentIDs)
        {
            String title = WidgetActions.loadActionLaunchPref(context, 0, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
            String desc = WidgetActions.loadActionLaunchPref(context, 0, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
            Integer color = Integer.parseInt(WidgetActions.loadActionLaunchPref(context, 0, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_COLOR));
            String[] tags = WidgetActions.loadActionTags(context, 0, id).toArray(new String[0]);
            if (title != null && !title.trim().isEmpty()) {
                ids.add(new ActionDisplay(id, title, desc, color, tags));
            }
        }

        Collections.sort(ids, new Comparator<ActionDisplay>() {
            @Override
            public int compare(ActionDisplay o1, ActionDisplay o2)
            {
                if (o1.title.equals(o2.title)) {
                    return o1.desc.compareTo(o2.desc);
                } else return o1.title.compareTo(o2.title);
            }
        });

        ids.add(0, new ActionDisplay("", context.getString(R.string.configActionDesc_doNothing), context.getString(R.string.configActionDesc_doNothing), WidgetActions.PREF_DEF_ACTION_LAUNCH_COLOR, new String[] {SuntimesActionsContract.TAG_DEFAULT}));

        adapter = new ActionDisplayAdapter(context, R.layout.layout_listitem_actions, ids.toArray(new ActionDisplay[0]));
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
        inflater.inflate(R.menu.editintent1, menu.getMenu());
        menu.setOnMenuItemClickListener(onMenuItemClicked);
        PopupMenuCompat.forceActionBarIcons(menu.getMenu());
        prepareOverflowMenu(context, menu.getMenu());
        menu.show();
    }

    protected void prepareOverflowMenu(Context context, Menu menu)
    {
        String actionId = getIntentID();
        boolean isModifiable = actionId != null && !actionId.trim().isEmpty();

        MenuItem editItem = menu.findItem(R.id.editAction);
        if (editItem != null) {
            editItem.setEnabled(isModifiable);
            editItem.setVisible(isModifiable);
        }

        MenuItem deleteItem = menu.findItem(R.id.deleteAction);
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
                case R.id.addAction:
                    addAction();
                    return true;

                case R.id.editAction:
                    editAction();
                    return true;

                case R.id.clearAction:
                    clearActions();
                    return true;

                case R.id.deleteAction:
                    deleteAction();
                    return true;

                default:
                    return false;
            }
        }
    });

    public void addAction()
    {
        final Context context = contextRef.get();
        final SaveActionDialog saveDialog = new SaveActionDialog();
        saveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                saveDialog.getEdit().setData(data);
            }
        });
        saveDialog.setOnAcceptedListener(onActionSaved(context, saveDialog));
        saveDialog.show(fragmentManager.get(), DIALOGTAG_ADD);
    }

    protected void editAction()
    {
        final Context context = contextRef.get();
        final String actionID = getIntentID();
        if (actionID != null && !actionID.trim().isEmpty() && context != null)
        {
            final SaveActionDialog saveDialog = new SaveActionDialog();
            saveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    saveDialog.getEdit().setData(data);
                    saveDialog.getEdit().loadIntent(context, 0, actionID);
                    saveDialog.setIntentID(actionID);
                }
            });

            saveDialog.setOnAcceptedListener(onActionSaved(context, saveDialog));
            saveDialog.show(fragmentManager.get(), DIALOGTAG_EDIT);
        }
    }

    private DialogInterface.OnClickListener onActionSaved(final Context context, final SaveActionDialog saveDialog)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String intentID = saveDialog.getIntentID();
                saveDialog.getEdit().saveIntent(context, 0, intentID, saveDialog.getIntentTitle(), saveDialog.getIntentDesc());
                Toast.makeText(context, context.getString(R.string.saveaction_toast, saveDialog.getIntentTitle(), intentID), Toast.LENGTH_SHORT).show();
                initAdapter(context);
                updateViews(context);
                adapterModified = true;

                setSelected(intentID);
                triggerActionMode(list, selectedItem);
            }
        };
    }

    public void exportActions() {
        // TODO
    }

    public void importActions() {
        // TODO
        adapterModified = true;
    }

    public void clearActions()
    {
        final Context context = contextRef.get();
        if (context != null)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getString(R.string.clearactions_dialog_msg))
                    .setNegativeButton(context.getString(R.string.clearactions_dialog_cancel), null)
                    .setPositiveButton(context.getString(R.string.clearactions_dialog_ok),
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    WidgetActions.deletePrefs(context);
                                    WidgetActions.initDefaults(context);
                                    Toast.makeText(context, context.getString(R.string.clearactions_toast), Toast.LENGTH_SHORT).show();
                                    initAdapter(context);
                                    updateViews(context);
                                    adapterModified = true;
                                }
                            });
            dialog.show();
        }
    }

    protected void deleteAction()
    {
        final Context context = contextRef.get();
        final String actionID = getIntentID();
        if (actionID != null && !actionID.trim().isEmpty() && context != null)
        {
            Set<String> tags = WidgetActions.loadActionTags(context, 0, actionID);
            final boolean isDefault = tags.contains(SuntimesActionsContract.TAG_DEFAULT);

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            String title = WidgetActions.loadActionLaunchPref(context, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
            dialog.setMessage(context.getString(isDefault ? R.string.delaction_dialog_msg1 : R.string.delaction_dialog_msg, title, actionID))
                    .setNegativeButton(context.getString(R.string.delaction_dialog_cancel), null)
                    .setPositiveButton(context.getString(isDefault ? R.string.delaction_dialog_ok1 : R.string.delaction_dialog_ok),
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    WidgetActions.deleteActionLaunchPref(context, 0, actionID);
                                    adapterModified = true;

                                    list.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            Context context = contextRef.get();
                                            if (context != null)
                                            {
                                                if (isDefault) {
                                                    WidgetActions.initDefaults(context);
                                                }
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

    /**
     * ActionDisplay
     */
    public static class ActionDisplay
    {
        public String id, title, desc;
        public int color;
        public String[] tags;

        public ActionDisplay(String id, String title, String desc, int color, String[] tags)
        {
            this.id = id;
            this.title = title;
            this.desc = desc;
            this.color = color;
            this.tags = tags;
        }
        public String toString() {
             return title;
        }

        public boolean hasTag(String value)
        {
            for (String tag : tags) {
                if (tag.equals(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * ActionDisplayAdapter
     */
    public static class ActionDisplayAdapter extends ArrayAdapter<ActionDisplay>
    {
        private int resourceID, dropDownResourceID;
        private ActionDisplay selectedItem;
        private ActionDisplay[] objects;

        public ActionDisplayAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            init(context, resource);
        }

        public ActionDisplayAdapter(@NonNull Context context, int resource, @NonNull ActionDisplay[] objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        public ActionDisplayAdapter(@NonNull Context context, int resource, @NonNull List<ActionDisplay> objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        private void init(@NonNull Context context, int resource) {
            resourceID = dropDownResourceID = resource;
        }

        public void setSelected( ActionDisplay item ) {
            selectedItem = item;
            notifyDataSetChanged();
        }
        public ActionDisplay getSelected() {
            return selectedItem;
        }

        public ActionDisplay findItemByID(String actionID)
        {
            for (int i=0; i<getCount(); i++)
            {
                ActionDisplay item = getItem(i);
                if (item != null && item.id.equals(actionID)) {
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
            return getItemView(position, convertView, parent, true, dropDownResourceID);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, true, resourceID);
        }

        private View getItemView(int position, View convertView, @NonNull ViewGroup parent, boolean colorize, int resID)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(resID, parent, false);

            ActionDisplay item = getItem(position);
            if (item == null) {
                Log.w("getItemView", "item at position " + position + " is null.");
                return view;
            }

            if (selectedItem != null && item.id.equals(selectedItem.id)) {
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.text_accent_dark));
            } else view.setBackgroundColor(Color.TRANSPARENT);

            TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
            primaryText.setText(item.toString());

            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null) {
                secondaryText.setText(item.desc != null && !item.desc.trim().isEmpty() ? item.desc : item.id);
            }

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            if (icon != null && colorize)
            {
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                if (item.hasTag(SuntimesActionsContract.TAG_SUNTIMESALARMS)) {
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_launcher_alarms_foreground));

                } else if (item.hasTag(SuntimesActionsContract.TAG_SUNTIMES)) {
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_launcher_foreground));

                } else if (item.hasTag(SuntimesActionsContract.TAG_CALENDAR)) {
                    int[] attrs = { R.attr.icActionCalendar };
                    TypedArray a = getContext().obtainStyledAttributes(attrs);
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), a.getResourceId(0, R.drawable.ic_calendar)));
                    a.recycle();

                } else if (item.hasTag(SuntimesActionsContract.TAG_LOCATION)) {
                    int[] attrs = { R.attr.icActionPlace };
                    TypedArray a = getContext().obtainStyledAttributes(attrs);
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), a.getResourceId(0, R.drawable.ic_action_place)));
                    a.recycle();

                } else if (item.hasTag(SuntimesActionsContract.TAG_ALARM)) {
                    int[] attrs = { R.attr.icActionAlarm };
                    TypedArray a = getContext().obtainStyledAttributes(attrs);
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), a.getResourceId(0, R.drawable.ic_action_alarms)));
                    a.recycle();

                } else if (item.hasTag(SuntimesActionsContract.TAG_SETTINGS)) {
                    int[] attrs = { R.attr.icActionSettings };
                    TypedArray a = getContext().obtainStyledAttributes(attrs);
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), a.getResourceId(0, R.drawable.ic_action_settings)));
                    a.recycle();

                } else {
                    int[] attrs = { R.attr.icActionExtension };
                    TypedArray a = getContext().obtainStyledAttributes(attrs);
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), a.getResourceId(0, R.drawable.ic_action_extension)));
                    a.recycle();
                }
            }

            return view;
        }

        private int getColorForPosition(int position) {
            ActionDisplay item = getItem(position);
            return item != null ? item.color : WidgetActions.PREF_DEF_ACTION_LAUNCH_COLOR;
        }
    }

    /**
     * triggerActionMode
     */
    public boolean triggerActionMode() {
        return triggerActionMode(list, selectedItem);
    }
    protected boolean triggerActionMode(View view, ActionDisplay item)
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            if (actionMode == null)
            {
                if (item != null)
                {
                    actionModeCallback.setItem(item);
                    actionMode = list.startActionModeForChild(view, actionModeCallback);
                    if (actionMode != null) {
                        actionMode.setTitle(item.title);
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
     * ActionDisplayActionMode
     */
    private class ActionDisplayActionModeBase
    {
        public ActionDisplayActionModeBase() {
        }

        protected ActionDisplay action = null;
        public void setItem(ActionDisplay item) {
            action = item;
        }

        protected boolean onCreateActionMode(MenuInflater inflater, Menu menu) {
            inflater.inflate(R.menu.editintent2, menu);
            return true;
        }

        protected void onDestroyActionMode() {
            actionMode = null;
        }

        protected boolean onPrepareActionMode(Menu menu)
        {
            String actionId = getIntentID();
            boolean isModifiable = (actionId != null && !actionId.trim().isEmpty());

            PopupMenuCompat.forceActionBarIcons(menu);
            MenuItem selectItem = menu.findItem(R.id.selectAction);
            selectItem.setVisible( !disallowSelect );

            MenuItem deleteItem = menu.findItem(R.id.deleteAction);
            MenuItem editItem = menu.findItem(R.id.editAction);
            deleteItem.setVisible( isModifiable );
            editItem.setVisible( isModifiable );
            return false;
        }

        protected boolean onActionItemClicked(MenuItem item)
        {
            if (action != null)
            {
                switch (item.getItemId())
                {
                    case R.id.selectAction:
                        if (onItemSelected != null) {
                            onItemSelected.onClick(list);
                        }
                        return true;

                    case R.id.deleteAction:
                        deleteAction();
                        return true;

                    case R.id.editAction:
                        editAction();
                        return true;
                }
            }
            return false;
        }

    }

    private class ActionDisplayActionMode extends ActionDisplayActionModeBase implements ActionModeHelper.ActionModeCallback
    {
        public ActionDisplayActionMode() {
            super();
        }
        @Override
        public boolean onCreateActionMode(ActionModeHelper.ActionModeInterface mode, Menu menu) {
            return onCreateActionMode(mode.getMenuInflater(), menu);
        }
        @Override
        public void onDestroyActionMode(ActionModeHelper.ActionModeInterface mode) {
            onDestroyActionMode();
        }
        @Override
        public boolean onPrepareActionMode(ActionModeHelper.ActionModeInterface mode, Menu menu) {
            return onPrepareActionMode(menu);
        }
        @Override
        public boolean onActionItemClicked(ActionModeHelper.ActionModeInterface mode, MenuItem item) {
            mode.finish();
            return onActionItemClicked(item);
        }
    }

    @TargetApi(11)
    private class ActionDisplayActionMode1 extends ActionDisplayActionModeBase implements ActionMode.Callback
    {
        public ActionDisplayActionMode1() {
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
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            mode.finish();
            return onActionItemClicked(item);
        }
    }



}
