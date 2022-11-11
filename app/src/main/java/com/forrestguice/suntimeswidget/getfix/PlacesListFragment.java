/**
    Copyright (C) 2014-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.getfix;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PlacesListFragment extends Fragment
{
    public static final String KEY_SELECTED_ROWID = "selectedRowID";
    public static final String KEY_FILTER_TEXT = "filterText";
    public static final String KEY_FILTER_EXCEPTIONS = "filterExceptions";
    public static final String KEY_ALLOW_PICK = "allowPick";
    public static final String KEY_MODIFIED = "isModified";

    public static final String DIALOG_EDITPLACE = "placedialog";

    public static final int IMPORT_REQUEST = 100;
    public static final int EXPORT_REQUEST = 200;

    protected FragmentListener listener;
    protected PlacesListAdapter adapter;
    protected RecyclerView listView;
    protected View emptyView;
    protected View progressView;
    protected ActionMode actionMode = null;
    protected PlacesListActionCompat actions = new PlacesListActionCompat();

    public PlacesListFragment()
    {
        super();
        setArguments(new Bundle());
        setAllowPick(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        FragmentManager fragments = getChildFragmentManager();
        PlacesEditFragment editDialog = (PlacesEditFragment) fragments.findFragmentByTag(DIALOG_EDITPLACE);
        if (editDialog != null) {
            editDialog.setFragmentListener(onEditPlace);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        View dialogContent = inflater.inflate(R.layout.layout_dialog_placeslist, parent, false);

        adapter = new PlacesListAdapter(getActivity());
        adapter.setFilterText(getFilterText());
        adapter.setAdapterListener(listAdapterListener);

        listView = (RecyclerView) dialogContent.findViewById(R.id.placesList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(adapter);

        emptyView = dialogContent.findViewById(android.R.id.empty);
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }

        progressView = dialogContent.findViewById(R.id.progressLayout);
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
        }

        if (savedState != null) {
            reloadAdapter(listTaskListener(savedState.getLongArray(KEY_SELECTED_ROWID)));
        } else reloadAdapter();

        return dialogContent;
    }

    @Override
    public void onSaveInstanceState(Bundle state)
    {
        state.putLongArray(KEY_SELECTED_ROWID, adapter.getSelectedRowID());
        super.onSaveInstanceState(state);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case EXPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        exportPlaces(getActivity(), uri);
                    }
                }
                break;

            case IMPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        importPlaces(getActivity(), uri);
                    }
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.placeslist, menu);

        final MenuItem worldPlacesItem = menu.findItem(R.id.addWorldPlaces);
        if (worldPlacesItem != null)
        {
            if (Build.VERSION.SDK_INT >= 17) {
                worldPlacesItem.setVisible(true);
                worldPlacesItem.setEnabled(true);
            } else {
                worldPlacesItem.setEnabled(false);
                worldPlacesItem.setVisible(false);   // TODO: legacy support
            }
        }

        final MenuItem searchItem = menu.findItem(R.id.searchPlaces);
        if (searchItem != null)
        {
            if (Build.VERSION.SDK_INT >= 11)
            {
                MenuItemCompat.setOnActionExpandListener(searchItem, onItemSearchExpand);
                SearchView searchView = (SearchView) searchItem.getActionView();
                if (searchView != null)
                {
                    if (!TextUtils.isEmpty(adapter.getFilterText()))
                    {
                        if (Build.VERSION.SDK_INT >= 14) {
                            searchItem.expandActionView();
                        }
                        searchView.setQuery(adapter.getFilterText(), true);
                        searchView.clearFocus();
                    }
                    searchView.setOnQueryTextListener(onItemSearch);
                }

            } else {
                searchItem.setVisible(false);  // TODO: legacy support
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.addPlace:
                addPlace(getActivity());
                return true;

            case R.id.clearPlaces:
                clearPlaces(getActivity());
                return true;

            case R.id.importPlaces:
                importPlaces(getActivity());
                return true;

            case R.id.exportPlaces:
                exportPlaces(getActivity());
                return true;

            case R.id.addWorldPlaces:
                addWorldPlaces(getActivity());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void triggerActionMode(PlaceItem... items)
    {
        if (actionMode == null)
        {
            if (items[0] != null)
            {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                actionMode = activity.startSupportActionMode(actions);
                if (actionMode != null) {
                    updateActionMode(getActivity(), items);
                }
            }

        } else {
            updateActionMode(getActivity(), items);
        }
    }

    protected void updateActionMode(Context context, PlaceItem... items)
    {
        if (items == null || items.length == 0 ||
                items[0] == null || items[0].location == null) {
            return;
        }

        if (actionMode != null)
        {
            long[] rowID = new long[items.length];
            for (int i=0; i<items.length; i++) {
                if (items[i] != null) {
                    rowID[i] = items[i].rowID;
                }
            }
            adapter.setSelectedRowID(rowID);

            if (rowID.length > 1)
            {
                actionMode.setTitle(context.getResources().getQuantityString(R.plurals.placePlural, rowID.length, rowID.length));
                actionMode.setSubtitle("");

            } else {
                actionMode.setTitle(items[0].location.getLabel());
                actionMode.setSubtitle(locationDisplayString(context, items[0].location, true));
            }
            actions.setItems(items);
            actionMode.invalidate();

        } else {
            triggerActionMode(items);
        }
    }

    protected void finishActionMode()
    {
        actionMode.finish();
        if (listener != null) {
            listener.onActionModeFinished();
        }
    }

    private class PlacesListActionCompat implements android.support.v7.view.ActionMode.Callback
    {
        private PlaceItem[] items = null;
        public void setItems(PlaceItem[] values) {
            this.items = values;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.placescontext, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            SuntimesUtils.forceActionBarIcons(menu);

            int[] singleSelectItems = new int[] { R.id.pickPlace, R.id.sharePlace, R.id.editPlace, R.id.copyPlace };
            for (int resID : singleSelectItems)
            {
                MenuItem menuItem = menu.findItem(resID);
                if (menuItem != null) {
                    menuItem.setVisible(items == null || items.length == 1);
                }
            }

            MenuItem pickPlace = menu.findItem(R.id.pickPlace);
            if (pickPlace != null) {
                pickPlace.setVisible(pickPlace.isVisible() && allowPick());
                Log.d("DEBUG", "onPrepareActionMode: allowPick: " + allowPick());
            }

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.pickPlace:
                    pickPlace(items[0]);
                    finishActionMode();
                    return true;

                case R.id.editPlace:
                    editPlace(items[0]);
                    return true;

                case R.id.copyPlace:
                    copyPlace(items[0]);
                    return true;

                case R.id.deletePlace:
                    deletePlace(getActivity(), items);
                    return true;

                case R.id.sharePlace:
                    sharePlace(items[0]);
                    return true;

                case android.R.id.home:
                    finishActionMode();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;
            adapter.setSelectedRowID(-1);

            if (listener != null) {
                listener.onActionModeFinished();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void reloadAdapter() {
        reloadAdapter(listTaskListener(-1));
    }

    public void reloadAdapter( PlacesListTask.TaskListener taskListener )
    {
        Context context = getActivity();
        if (context != null)
        {
            PlacesListTask listTask = new PlacesListTask(context);
            listTask.setTaskListener(taskListener);
            listTask.execute();
        }
    }

    protected PlacesListTask.TaskListener listTaskListener(final long... selectedRowID)
    {
        return new PlacesListTask.TaskListener() {
            @Override
            public void onStarted() {
                emptyView.setVisibility(View.GONE);
            }

            @Override
            public void onFinished(List<PlaceItem> results)
            {
                if (emptyView != null) {
                    emptyView.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                }
                listView.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                dismissProgress();

                adapter.setSelectedRowID(selectedRowID);
                adapter.setValues(results);
                adapter.setFilterExceptions(getFilterExceptions());
                adapter.applyFilter(getFilterText(), false);

                if (selectedRowID != null && selectedRowID.length > 0 && selectedRowID[0] != -1)
                {
                    listView.scrollToPosition(adapter.indexOf(selectedRowID[0]));
                    triggerActionMode(adapter.getItems(selectedRowID));
                }
            }
        };
    }

    protected AdapterListener listAdapterListener = new AdapterListener()
    {
        @Override
        public void onItemClicked(PlaceItem item, int position)
        {
            triggerActionMode(item);
            if (listener != null) {
                listener.onItemClicked(item, position);
            }
        }

        @Override
        public boolean onItemLongClicked(PlaceItem item, int position)
        {
            long[] valuesArray = adapter.getSelectedRowID();
            boolean emptySelection = (valuesArray.length == 1 && valuesArray[0] == -1);

            ArrayList<Long> values = new ArrayList<>();
            for (int i=0; i<valuesArray.length; i++) {
                values.add(valuesArray[i]);
            }

            PlaceItem[] selection;
            if (emptySelection) {
                selection = new PlaceItem[] { item };

            } else {
                if (values.contains(item.rowID)) {
                    values.remove(item.rowID);
                } else values.add(item.rowID);

                selection = adapter.getItems(values);
            }

            triggerActionMode(selection);
            if (listener != null) {
                listener.onItemLongClicked(item, position);
            }
            return true;
        }

        @Override
        public void onFilterChanged(String filterText, Long[] filterExceptions)
        {
            getArguments().putString(KEY_FILTER_TEXT, filterText);

            long[] array = new long[filterExceptions.length];
            for (int i=0; i<array.length; i++) {
                array[i] = filterExceptions[i];
            }
            getArguments().putLongArray(KEY_FILTER_EXCEPTIONS, array);
        }
    };

    public void showProgress( Context context, CharSequence title, CharSequence message )
    {
        if (progressView != null) {
            progressView.setVisibility(View.VISIBLE);
        }
        if (listener != null) {
            listener.onToggleProgress(true);
        }
    }

    public void dismissProgress()
    {
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
        }
        if (listener != null) {
            listener.onToggleProgress(false);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void pickPlace(@Nullable PlaceItem item)
    {
        if (listener != null && allowPick()) {
            listener.onItemPicked(item);
        }
    }

    protected void sharePlace(@Nullable PlaceItem item)
    {
        Context context = getActivity();
        if (item != null && item.location != null && context != null)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(item.location.getUri());
            List<ResolveInfo> info = context.getPackageManager().queryIntentActivities(intent, 0);
            List<Intent> geoIntents = new ArrayList<Intent>();

            if (!info.isEmpty())
            {
                for (ResolveInfo resolveInfo : info)
                {
                    if (!TextUtils.equals(resolveInfo.activityInfo.packageName, BuildConfig.APPLICATION_ID))
                    {
                        Intent geoIntent = new Intent(Intent.ACTION_VIEW);
                        geoIntent.setPackage(resolveInfo.activityInfo.packageName);
                        geoIntent.setData(item.location.getUri());
                        geoIntents.add(geoIntent);
                    }
                }
            }

            if (geoIntents.size() > 0)
            {
                Intent chooserIntent = Intent.createChooser(geoIntents.remove(0), getString(R.string.configAction_mapLocation_chooser));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, geoIntents.toArray(new Parcelable[0]));
                startActivity(chooserIntent);

            } else {
                Toast.makeText(context, context.getString(R.string.configAction_mapLocation_noapp), Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void addPlace(Context context)
    {
        PlacesEditFragment dialog = new PlacesEditFragment();
        dialog.setFragmentListener(onEditPlace);
        dialog.show(getChildFragmentManager(), DIALOG_EDITPLACE);
    }

    protected void copyPlace(@Nullable PlaceItem item)
    {
        if (item != null && item.location != null)
        {
            Location location = new Location("", item.location.getLatitude(), item.location.getLongitude(), item.location.getAltitude());
            PlaceItem place = new PlaceItem(-1, location);

            PlacesEditFragment dialog = new PlacesEditFragment();
            dialog.setFragmentListener(onEditPlace);
            dialog.setPlace(place);
            dialog.show(getChildFragmentManager(), DIALOG_EDITPLACE);
        }
    }

    protected void editPlace(@Nullable PlaceItem item)
    {
        boolean editHandled = false;
        if (listener != null) {
            editHandled = listener.onItemEdit(item);
        }

        if (!editHandled)
        {
            Context context = getActivity();
            if (item != null && item.location != null && context != null)
            {
                PlacesEditFragment dialog = new PlacesEditFragment();
                dialog.setFragmentListener(onEditPlace);
                dialog.setPlace(item);
                dialog.show(getChildFragmentManager(), DIALOG_EDITPLACE);
            }
        }
    }

    protected void addOrUpdatePlace(PlaceItem... item)
    {
        addOrUpdatePlace(new PlacesListTask.TaskListener()
        {
            @Override
            public void onStarted() {}

            @Override
            public void onFinished(List<PlaceItem> results)
            {
                if (results.size() > 0)
                {
                    updateActionMode(getActivity(), results.toArray(new PlaceItem[0]));

                    if (adapter.getItemCount() == 0) {
                        reloadAdapter(listTaskListener(results.get(0).rowID));

                    } else {
                        adapter.updateValues(results);
                        scrollToSelection();
                    }
                }
                dismissEditPlaceDialog();
            }
        }, item);
    }

    protected void addOrUpdatePlace(PlacesListTask.TaskListener listener, PlaceItem... item)
    {
        setModified(true);
        PlacesEditTask task = new PlacesEditTask(getActivity());
        task.setTaskListener(listener);
        task.execute(item);
    }

    protected void scrollToSelection()
    {
        LinearLayoutManager layout = (LinearLayoutManager) listView.getLayoutManager();
        int selected = adapter.getSelectedPosition();
        int start = layout.findFirstVisibleItemPosition();
        int end = layout.findLastVisibleItemPosition();
        if (selected != -1 && (selected <= start || selected >= end)) {
            listView.smoothScrollToPosition(selected);
        }
    }

    private PlacesEditFragment.FragmentListener onEditPlace = new PlacesEditFragment.FragmentListener()
    {
        @Override
        public void onCanceled(PlaceItem item) {
        }

        @Override
        public void onAccepted(PlaceItem item) {
            addOrUpdatePlace(item);
        }
    };

    protected void dismissEditPlaceDialog()
    {
        FragmentManager fragments = getChildFragmentManager();
        PlacesEditFragment dialog = (PlacesEditFragment) fragments.findFragmentByTag(DIALOG_EDITPLACE);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private SearchView.OnQueryTextListener onItemSearch = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String text) {
            adapter.applyFilter(text, true);
            return true;
        }
    };

    private MenuItemCompat.OnActionExpandListener onItemSearchExpand = new MenuItemCompat.OnActionExpandListener()
    {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            item.setVisible(false);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            item.setVisible(true);
            if (Build.VERSION.SDK_INT >= 11) {
                getActivity().invalidateOptionsMenu();
            }
            return true;
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void deletePlace(final Context context, @Nullable final PlaceItem... items)
    {
        if (items != null && items.length > 0
                && items[0] != null && items[0].location != null)
        {
            final Long[] rowIDs = new Long[items.length];
            for (int i=0; i<rowIDs.length; i++) {
                rowIDs[i] = items[i] != null ? items[i].rowID : -1;
            }

            final boolean multiDelete = (items.length > 1);
            String title = context.getString(multiDelete ? R.string.locationdelete_dialog_title1 : R.string.locationdelete_dialog_title);
            String desc = (multiDelete
                    ? context.getResources().getQuantityString(R.plurals.placePlural, items.length, items.length)
                    : items[0].location.getLabel());
            String message = context.getString(R.string.locationdelete_dialog_message, desc);

            AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                    .setTitle(title).setMessage(message).setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(context.getString(R.string.locationdelete_dialog_ok), onConfirmDeletePlace(context, rowIDs))
                    .setNegativeButton(context.getString(R.string.locationdelete_dialog_cancel), null);
            confirm.show();
        }
    }

    private DialogInterface.OnClickListener onConfirmDeletePlace(final Context context, final Long[] rowIDs)
    {
        return new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                DeletePlaceTask task = new DeletePlaceTask(context);
                task.setTaskListener(new DeletePlaceTask.TaskListener()
                {
                    @Override
                    public void onFinished(boolean result, Long... rowIDs)
                    {
                        List<PlaceItem> deletedItems = new ArrayList<>();
                        for (long rowID : rowIDs)
                        {
                            PlaceItem item = adapter.getItem(rowID);
                            if (item != null) {
                                deletedItems.add(item);
                            }
                            adapter.removeItem(rowID);
                        }
                        setModified(true);
                        offerUndoDeletePlace(context, deletedItems.toArray(new PlaceItem[0]));
                    }
                });

                finishActionMode();
                task.execute(rowIDs);
            }
        };
    }

    protected void offerUndoDeletePlace(Context context, final PlaceItem... deletedItems)
    {
        View view = getView();
        if (context != null && view != null && deletedItems != null)
        {
            Snackbar snackbar = Snackbar.make(view, context.getResources().getQuantityString(R.plurals.locationdelete_dialog_success, deletedItems.length, deletedItems.length), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(context.getString(R.string.configAction_undo), new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context context = getActivity();
                    if (context != null) {
                        for (PlaceItem item : deletedItems) {
                            item.rowID = -1;    // re-add item
                        }
                        addOrUpdatePlace(deletedItems);
                    }
                }
            });
            SuntimesUtils.themeSnackbar(context, snackbar, null);
            snackbar.setDuration(UNDO_DELETE_MILLIS);
            snackbar.show();
        }
    }
    public static final int UNDO_DELETE_MILLIS = 8000;

    public static class DeletePlaceTask extends AsyncTask<Long, Object, Boolean>
    {
        private GetFixDatabaseAdapter database;
        private Long[] rowIDs = new Long[] { -1L };

        public DeletePlaceTask(Context context) {
            database = new GetFixDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected Boolean doInBackground(Long... params)
        {
            if (params.length > 0) {
                rowIDs = params;
            }

            boolean result = false;
            database.open();
            for (long rowID : rowIDs)
            {
                if (rowID != -1) {
                    result = database.removePlace(rowID);
                }
            }
            database.close();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (taskListener != null)
                taskListener.onFinished(result, rowIDs);
        }

        private TaskListener taskListener = null;
        public void setTaskListener( TaskListener listener ) {
            taskListener = listener;
        }
        public static abstract class TaskListener
        {
            public void onFinished( boolean result, Long... rowIDs ) {}
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void clearPlaces(final Context context)
    {
        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.locationclear_dialog_title))
                .setMessage(context.getString(R.string.locationclear_dialog_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(context.getString(R.string.locationclear_dialog_ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        BuildPlacesTask task = new BuildPlacesTask(context);
                        task.setTaskListener(clearPlacesListener);
                        task.execute(true);   // clearFlag set to true
                    }
                })
                .setNegativeButton(context.getString(R.string.locationclear_dialog_cancel), null);

        confirm.show();
    }
    private BuildPlacesTask.TaskListener clearPlacesListener = new BuildPlacesTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            setRetainInstance(true);
            Context context = getActivity();
            if (context != null) {
                showProgress(context, context.getString(R.string.locationcleared_dialog_title), context.getString(R.string.locationcleared_dialog_message));
            }
        }

        @Override
        public void onFinished(Integer result)
        {
            setModified(true);
            setRetainInstance(false);
            dismissProgress();

            Context context = getActivity();
            if (context != null) {
                offerUndoClearPlaces(context, adapter.getItems());
            }
            reloadAdapter();
        }
    };
    protected void offerUndoClearPlaces(Context context, final PlaceItem... deletedItems)
    {
        View view = getView();
        if (context != null && view != null && deletedItems != null)
        {
            Snackbar snackbar = Snackbar.make(view, context.getString(R.string.locationcleared_toast_success), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(context.getString(R.string.configAction_undo), new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context context = getActivity();
                    if (context != null)
                    {
                        showProgress(context, null, null);
                        for (PlaceItem item : deletedItems) {
                            item.rowID = -1;    // re-add item
                        }
                        addOrUpdatePlace(new PlacesListTask.TaskListener()
                        {
                            @Override
                            public void onStarted() {}

                            @Override
                            public void onFinished(List<PlaceItem> results)
                            {
                                // dismissProgress();    // dismissed by reloadAdapter
                                setSelectedRowID(-1);
                                reloadAdapter();
                                dismissEditPlaceDialog();
                            }
                        }, deletedItems);
                        setSelectedRowID(-1);
                    }
                }
            });
            SuntimesUtils.themeSnackbar(context, snackbar, null);
            snackbar.setDuration(UNDO_DELETE_MILLIS);
            snackbar.show();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean importPlaces(Context context)
    {
        if (context != null)
        {
            Intent intent = ExportTask.getOpenFileIntent("text/*");
            startActivityForResult(intent, IMPORT_REQUEST);
            return true;
        }
        return false;
    }

    public boolean importPlaces(Context context, @NonNull Uri uri)
    {
        Log.i("importPlaces", "Starting import task: " + uri);
        BuildPlacesTask task = new BuildPlacesTask(context);
        task.setTaskListener(buildPlacesListener);
        task.execute(false, uri);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void exportPlaces(Context context)
    {
        String exportTarget = "SuntimesPlaces";
        if (Build.VERSION.SDK_INT >= 19)
        {
            String filename = exportTarget + ExportPlacesTask.FILEEXT;
            Intent intent = ExportTask.getCreateFileIntent(filename, ExportPlacesTask.MIMETYPE);
            try {
                startActivityForResult(intent, EXPORT_REQUEST);
                return;

            } catch (ActivityNotFoundException e) {
                Log.e("exportPlaces", "SAF is unavailable? (" + e + ").. falling back to legacy export method.");
            }
        }
        ExportPlacesTask task = new ExportPlacesTask(context, exportTarget, true, true);  // export to external cache
        task.setTaskListener(exportPlacesListener);
        task.execute();
    }

    public void exportPlaces(Context context, @NonNull Uri uri)
    {
        Log.i("exportPlaces", "Starting export task: " + uri);
        ExportPlacesTask task = new ExportPlacesTask(context, uri);
        task.setTaskListener(exportPlacesListener);
        task.execute();
    }

    private ExportPlacesTask.TaskListener exportPlacesListener = new ExportPlacesTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            setRetainInstance(true);
            Context context = getActivity();
            if (context != null) {
                showProgress(context, context.getString(R.string.locationexport_dialog_title), context.getString(R.string.locationexport_dialog_message));
            }
        }

        @Override
        public void onFinished(ExportPlacesTask.ExportResult results)
        {
            setRetainInstance(false);
            dismissProgress();

            Context context = getActivity();
            if (context != null)
            {
                File file = results.getExportFile();
                String path = ((file != null) ? file.getAbsolutePath()
                        : ExportTask.getFileName(context.getContentResolver(), results.getExportUri()));

                if (results.getResult())
                {
                    if (isAdded()) {
                        String successMessage = context.getString(R.string.msg_export_success, path);
                        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show();
                    }

                    if (Build.VERSION.SDK_INT >= 19) {
                        if (results.getExportUri() == null) {
                            ExportTask.shareResult(context, file, results.getMimeType());
                        }
                    } else {
                        ExportTask.shareResult(context, file, results.getMimeType());
                    }
                    return;
                }

                if (isAdded()) {
                    String failureMessage = context.getString(R.string.msg_export_failure, path);
                    Toast.makeText(context, failureMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void addWorldPlaces(Context context)
    {
        BuildPlacesTask task = new BuildPlacesTask(context);
        task.setTaskListener(buildPlacesListener);
        task.execute();
    }
    private BuildPlacesTask.TaskListener buildPlacesListener = new BuildPlacesTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            setRetainInstance(true);
            Context context = getActivity();
            if (context != null) {
                showProgress(context, context.getString(R.string.locationbuild_dialog_title), context.getString(R.string.locationbuild_dialog_message));
            }
        }

        @Override
        public void onFinished(Integer result)
        {
            setRetainInstance(false);
            dismissProgress();
            if (result > 0)
            {
                reloadAdapter();
                Context context = getActivity();
                if (context != null) {
                    Toast.makeText(context, context.getString(R.string.locationbuild_toast_success, result.toString()), Toast.LENGTH_LONG).show();
                }
            } // else // TODO: fail msg
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * PlacesListTask
     */
    public static class PlacesListTask extends AsyncTask<PlaceItem, Location, List<PlaceItem>>
    {
        protected GetFixDatabaseAdapter database;

        public PlacesListTask(@NonNull Context context) {
            database = new GetFixDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected List<PlaceItem> doInBackground(PlaceItem... items)
        {
            ArrayList<PlaceItem> result = new ArrayList<>();

            database.open();
            Cursor cursor = database.getAllPlaces(0, true);
            if (cursor != null)
            {
                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    String name = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_NAME));
                    String lat = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_LATITUDE));
                    String lon = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_LONGITUDE));
                    String alt = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_ALTITUDE));
                    String comment = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_COMMENT));
                    Location location = new Location(name, lat, lon, alt);
                    location.setUseAltitude(true);

                    PlaceItem item = new PlaceItem(cursor.getLong(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_ROWID)), location);
                    item.isDefault = (comment != null && comment.contains(PlaceItem.TAG_DEFAULT));

                    result.add(item);
                    cursor.moveToNext();
                }
            }
            database.close();
            return result;
        }

        @Override
        protected void onPostExecute(List<PlaceItem> result)
        {
            if (listener != null) {
                listener.onFinished(result);
            }
        }

        @Override
        protected void onPreExecute()
        {
            if (listener != null) {
                listener.onStarted();
            }
        }

        protected TaskListener listener = null;
        public void setTaskListener(TaskListener listener) {
            this.listener = listener;
        }

        public interface TaskListener
        {
            void onStarted();
            void onFinished(List<PlaceItem> results);
        }
    }

    /**
     * PlacesEditTask
     */
    public static class PlacesEditTask extends PlacesListTask
    {
        public PlacesEditTask(@NonNull Context context) {
            super(context);
        }

        @Override
        protected List<PlaceItem> doInBackground(PlaceItem... items)
        {
            ArrayList<PlaceItem> result = new ArrayList<>();
            database.open();
            for (PlaceItem item : items)
            {
                if (item != null)
                {
                    if (item.rowID == -1) {
                        item.rowID = database.addPlace(item.location);
                        Log.i(getClass().getSimpleName(), "Added place " + item.rowID);

                    } else {
                        database.updatePlace(item.rowID, item.location);
                        Log.i(getClass().getSimpleName(), "Updated place " + item.rowID);
                    }
                    result.add(item);
                }
            }
            database.close();
            return result;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setSelectedRowID( long... rowID ) {
        adapter.setSelectedRowID(rowID);
    }
    public long[] selectedRowID() {
        return adapter.getSelectedRowID();
    }

    public void setFilterText( String value ) {
        getArguments().putString(KEY_FILTER_TEXT, value);
        if (adapter != null) {
            adapter.setFilterText(value);
        }
    }
    public String getFilterText() {
        String value = getArguments().getString(KEY_FILTER_TEXT);
        return (value != null ? value : "");
    }
    public long[] getFilterExceptions() {
        return getArguments().getLongArray(KEY_FILTER_EXCEPTIONS);
    }

    public void setAllowPick(boolean value) {
        getArguments().putBoolean(KEY_ALLOW_PICK, value);
    }
    public boolean allowPick() {
        return getArguments().getBoolean(KEY_ALLOW_PICK, false);
    }

    public boolean isModified() {
        return getArguments().getBoolean(KEY_MODIFIED, false);
    }
    protected void setModified(boolean value) {
        getArguments().putBoolean(KEY_MODIFIED, value);
    }

    public void setFragmentListener(FragmentListener value) {
        listener = value;
    }

    public interface FragmentListener extends AdapterListener
    {
        boolean onItemEdit(PlaceItem item);
        void onItemPicked(PlaceItem item);
        void onActionModeFinished();
        void onToggleProgress(boolean value);
        void onLiftAppBar(boolean value);
    }

    public interface AdapterListener {
        void onItemClicked(PlaceItem item, int position);
        boolean onItemLongClicked(PlaceItem item, int position);
        void onFilterChanged(String filterText, Long[] filterExceptions);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListAdapter extends RecyclerView.Adapter<PlacesListViewHolder> implements Filterable
    {
        protected WeakReference<Context> contextRef;
        protected ArrayList<PlaceItem> items0, items;
        protected String filterText = "";
        protected ArrayList<Long> filterExceptions;

        public PlacesListAdapter(Context context)
        {
            contextRef = new WeakReference<>(context);
            items0 = new ArrayList<>();
            items = new ArrayList<>();
            filterExceptions = new ArrayList<>();
        }

        public void setValues(List<PlaceItem> values)
        {
            filterExceptions.clear();

            items0.clear();
            items0.addAll(sortItems(values));

            items.clear();
            items.addAll(items0);

            notifyDataSetChanged();
        }

        public void updateValues(List<PlaceItem> values)
        {
            for (PlaceItem value : values)
            {
                int position = indexOf(value.rowID, items0);
                if (position >= 0 && position < items0.size())
                {
                    items0.set(position, value);
                } else {
                    items0.add(value);
                    sortItems(items0);
                }
                filterExceptions.add(value.rowID);
            }
            applyFilter(getFilterText(), false);
        }

        public int indexOf(long rowID) {
            return indexOf(rowID, items);
        }
        protected static int indexOf(long rowID, List<PlaceItem> items)
        {
            int position = -1;
            for (int i=0; i<items.size(); i++)
            {
                PlaceItem item = items.get(i);
                if (item != null && item.rowID == rowID) {
                    position = i;
                    break;
                }
            }
            return position;
        }

        public void removeItem(long rowID)
        {
            int position0 = indexOf(rowID, items0);
            if (position0 != -1) {
                items0.remove(position0);
            }

            int position1 = indexOf(rowID, items);
            if (position1 != -1)
            {
                items.remove(position1);
                notifyItemRemoved(position1);
            }
        }

        public PlaceItem getItem(long rowID)
        {
            int position = indexOf(rowID, items0);
            if (position >= 0) {
                return items0.get(position);
            } else return null;
        }

        public PlaceItem[] getItems() {
            return items0.toArray(new PlaceItem[0]);
        }

        public PlaceItem[] getItems(long[] rowID)
        {
            PlaceItem[] array = new PlaceItem[rowID.length];
            for (int i=0; i<array.length; i++) {
                array[i] = getItem(rowID[i]);
            }
            return array;
        }

        public PlaceItem[] getItems(List<Long> rowID)
        {
            PlaceItem[] array = new PlaceItem[rowID.size()];
            for (int i=0; i<array.length; i++) {
                array[i] = getItem(rowID.get(i));
            }
            return array;
        }

        protected static List<PlaceItem> sortItems(List<PlaceItem> items)
        {
            Collections.sort(items, new Comparator<PlaceItem>() {
                @Override
                public int compare(PlaceItem o1, PlaceItem o2)
                {
                    if ((o1 == null || o1.location == null) && (o2 == null || o2.location == null)) {
                        return 0;

                    } else if (o1 == null || o1.location == null) {
                        return -1;

                    } else if (o2 == null || o2.location == null) {
                        return 1;

                    } else {
                        return o1.location.getLabel().toLowerCase(Locale.ROOT).compareTo(o2.location.getLabel().toLowerCase(Locale.ROOT));
                    }
                }
            });
            return items;
        }

        private long[] selectedRowID = new long[] { -1 };
        public void setSelectedRowID( long... rowID )
        {
            if (rowID != null)
            {
                selectedRowID = new long[rowID.length];
                System.arraycopy(rowID, 0, selectedRowID, 0, rowID.length);
                notifyDataSetChanged();
            }
        }
        public long[] getSelectedRowID() {
            return selectedRowID;
        }
        public void clearSelection() {
            setSelectedRowID(-1);
        }

        public boolean isSelected(long rowID)
        {
            for (long id : selectedRowID) {
                if (id == rowID) {
                    return true;
                }
            }
            return false;
        }

        public int getSelectedPosition() {
            return indexOf(selectedRowID[0]);
        }

        @Override
        public PlacesListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.layout_listitem_places, parent, false);
            return new PlacesListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlacesListViewHolder holder, int position)
        {
            PlaceItem item = items.get(position);
            holder.selected = isSelected(item.rowID);
            holder.bindViewHolder(contextRef.get(), item);
            attachClickListeners(holder);
        }

        @Override
        public void onViewRecycled(PlacesListViewHolder holder)
        {
            detachClickListeners(holder);
            holder.unbindViewHolder();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        protected AdapterListener listener = null;
        public void setAdapterListener(AdapterListener listener) {
            this.listener = listener;
        }

        protected void attachClickListeners(PlacesListViewHolder holder)
        {
            if (holder.itemView != null) {
                holder.itemView.setOnClickListener(onItemClicked(holder));
                holder.itemView.setOnLongClickListener(onItemLongClicked(holder));
            }
        }

        protected View.OnClickListener onItemClicked(final PlacesListViewHolder holder)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (listener != null && position >= 0 && position < items.size()) {
                        listener.onItemClicked(items.get(position), position);
                    }
                }
            };
        }

        protected View.OnLongClickListener onItemLongClicked(final PlacesListViewHolder holder)
        {
            return new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (listener != null && position >= 0 && position < items.size()) {
                        return listener.onItemLongClicked(items.get(position), position);
                    }
                    return false;
                }
            };
        }

        protected void detachClickListeners(PlacesListViewHolder holder)
        {
            if (holder.itemView != null) {
                holder.itemView.setOnClickListener(null);
                holder.itemView.setOnLongClickListener(null);
            }
        }

        public void applyFilter(@Nullable String text, boolean clearExceptions) {
            filterText = (text != null ? text : "");
            if (listener != null) {
                listener.onFilterChanged(filterText, filterExceptions.toArray(new Long[0]));
            }

            if (clearExceptions) {
                filterExceptions.clear();
            }
            getFilter().filter(filterText);
        }

        public void setFilterText( String value ) {
            filterText = value;
        }

        public String getFilterText() {
            return filterText;
        }

        public void setFilterExceptions(long... values)
        {
            filterExceptions.clear();
            if (values != null) {
                for (Long value : values) {
                    filterExceptions.add(value);
                }
            }
        }
        public List<Long> getFilterExceptions() {
            return new ArrayList<>(filterExceptions);
        }

        @Override
        public Filter getFilter() {
            return new PlacesFilter();
        }

        /**
         * PlacesFilter
         */
        private class PlacesFilter extends Filter
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults results = new FilterResults();
                results.values = new ArrayList<>((constraint.length() > 0) ? getFilteredValues(constraint.toString().toLowerCase(Locale.ROOT)) : items0);
                return results;
            }

            protected List<PlaceItem> getFilteredValues(String constraint)
            {
                List<PlaceItem> values0  = new ArrayList<>();
                List<PlaceItem> values1  = new ArrayList<>();
                for (PlaceItem item : items0)
                {
                    String label = item.location.getLabel().toLowerCase(Locale.ROOT).trim();

                    if (label.equals(constraint) || filterExceptions.contains(item.rowID)) {
                        values0.add(0, item);

                    } else if (label.startsWith(constraint)) {
                        values0.add(item);

                    } else if (label.contains(constraint)) {
                        values1.add(item);
                    }
                }
                List<PlaceItem> values = new ArrayList<>(values0);
                values.addAll(values1);
                return values;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                items.clear();
                items.addAll((List<PlaceItem>) results.values);
                notifyDataSetChanged();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListViewHolder extends RecyclerView.ViewHolder
    {
        public TextView label;
        public TextView summary;
        public ImageView icon_default, icon_userdefined;
        public boolean selected = false;

        public PlacesListViewHolder(View itemView)
        {
            super(itemView);
            label = (TextView) itemView.findViewById(android.R.id.text1);
            summary = (TextView) itemView.findViewById(android.R.id.text2);
            icon_userdefined = (ImageView) itemView.findViewById(R.id.icon1);
            icon_default = (ImageView) itemView.findViewById(R.id.icon2);
        }

        public void bindViewHolder(@Nullable Context context, @Nullable PlaceItem item )
        {
            this.itemView.setSelected(selected);
            if (label != null) {
                label.setText(context == null || item == null || item.location == null ? ""
                        : item.location.getLabel());
            }
            if (summary != null) {
                summary.setText(context == null || item == null || item.location == null ? ""
                        : locationDisplayString(context, item.location, true));
            }

            if (item != null)
            {
                if (icon_default != null) {
                    icon_default.setVisibility(item.isDefault ? View.VISIBLE : View.GONE);
                }
                if (icon_userdefined != null) {
                    icon_userdefined.setVisibility(item.isDefault ? View.GONE : View.VISIBLE);
                }
            }
        }

        public void unbindViewHolder() {
            selected = false;
            bindViewHolder(null, null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * locationDisplayString .. "lat, lon [alt]"
     */
    public static CharSequence locationDisplayString(@NonNull Context context, @NonNull Location location, boolean showAltitude)
    {
        String locationString = context.getString(R.string.location_format_latlon, location.getLatitude(), location.getLongitude());
        if (showAltitude)
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
            SuntimesUtils.TimeDisplayText altitudeText = SuntimesUtils.formatAsHeight(context, location.getAltitudeAsDouble(), units, 0,true);
            String altitudeString = context.getString(R.string.location_format_alt, altitudeText.getValue(), altitudeText.getUnits());
            String altitudeTag = context.getString(R.string.location_format_alttag, altitudeString);
            String displayString = context.getString(R.string.location_format_latlonalt, locationString, altitudeTag);
            return SuntimesUtils.createRelativeSpan(null, displayString, altitudeTag, 0.75f);

        } else {
            return locationString;
        }
    }

}
