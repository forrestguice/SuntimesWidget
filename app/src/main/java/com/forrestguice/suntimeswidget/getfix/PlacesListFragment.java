/**
    Copyright (C) 2014-2020 Forrest Guice
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
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

public class PlacesListFragment extends Fragment
{
    public static final String KEY_SELECTED_ROWID = "selectedRowID";
    public static final String KEY_ALLOW_PICK = "allowPick";
    public static final String KEY_MODIFIED = "isModified";

    public static final String DIALOG_EDITPLACE = "placedialog";

    protected FragmentListener listener;
    protected PlacesListAdapter adapter;
    protected RecyclerView listView;
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
        adapter.setAdapterListener(listAdapterListener);

        listView = (RecyclerView) dialogContent.findViewById(R.id.placesList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(adapter);

        progressView = dialogContent.findViewById(R.id.progressLayout);

        if (savedState != null) {
            reloadAdapter(listTaskListener(savedState.getLong(KEY_SELECTED_ROWID, -1)));
        } else reloadAdapter();

        return dialogContent;
    }

    @Override
    public void onSaveInstanceState(Bundle state)
    {
        state.putLong(KEY_SELECTED_ROWID, adapter.getSelectedRowID());
        super.onSaveInstanceState(state);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.placeslist, menu);

        final MenuItem searchItem = menu.findItem(R.id.searchPlaces);
        if (searchItem != null)
        {
            MenuItemCompat.setOnActionExpandListener(searchItem, onItemSearchExpand);
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setOnQueryTextListener(onItemSearch);
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

    protected void triggerActionMode(PlaceItem item)
    {
        if (actionMode == null)
        {
            if (item != null)
            {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                actionMode = activity.startSupportActionMode(actions);
                if (actionMode != null) {
                    updateActionMode(getActivity(), item);
                }
            }

        } else {
            updateActionMode(getActivity(), item);
        }
    }

    protected void updateActionMode(Context context, PlaceItem item)
    {
        if (actionMode != null)
        {
            adapter.setSelectedRowID(item != null ? item.rowID : -1);
            actions.setItem(item);
            actionMode.setTitle(item.location != null ? item.location.getLabel() : "");
            actionMode.setSubtitle(item.location != null ? locationDisplayString(context, item.location, true) : "");

        } else {
            triggerActionMode(item);
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
        private PlaceItem item = null;
        public void setItem(PlaceItem item) {
            this.item = item;
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

            MenuItem pickPlace = menu.findItem(R.id.pickPlace);
            if (pickPlace != null) {
                pickPlace.setVisible(allowPick());
            }

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.pickPlace:
                    pickPlace(item);
                    finishActionMode();
                    return true;

                case R.id.editPlace:
                    editPlace(item);
                    return true;

                case R.id.copyPlace:
                    copyPlace(item);
                    return true;

                case R.id.deletePlace:
                    deletePlace(getActivity(), item);
                    finishActionMode();
                    return true;

                case R.id.sharePlace:
                    sharePlace(item);
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

    protected PlacesListTask.TaskListener listTaskListener(final long selectedRowID)
    {
        return new PlacesListTask.TaskListener() {
            @Override
            public void onStarted() {}

            @Override
            public void onFinished(List<PlaceItem> results)
            {
                adapter.setSelectedRowID(selectedRowID);
                adapter.setValues(results);

                if (selectedRowID != -1)
                {
                    listView.scrollToPosition(adapter.indexOf(selectedRowID));
                    triggerActionMode(adapter.getItem(selectedRowID));
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
    };

    public void showProgress( Context context, CharSequence title, CharSequence message )
    {
        if (progressView != null) {
            progressView.setVisibility(View.VISIBLE);
        }
    }

    public void dismissProgress()
    {
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
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
                    if (!TextUtils.equals(resolveInfo.activityInfo.packageName, "com.forrestguice.suntimeswidget"))
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

    private PlacesEditFragment.FragmentListener onEditPlace = new PlacesEditFragment.FragmentListener()
    {
        @Override
        public void onCanceled(PlaceItem item) {
            updateActionMode(getActivity(), item);
        }

        @Override
        public void onAccepted(PlaceItem item)
        {
            setModified(true);
            PlacesEditTask task = new PlacesEditTask(getActivity());
            task.setTaskListener(new PlacesListTask.TaskListener()
            {
                @Override
                public void onStarted() {}

                @Override
                public void onFinished(List<PlaceItem> results)
                {
                    if (results.size() > 0)
                    {
                        adapter.updateValues(results);
                        updateActionMode(getActivity(), results.get(0));
                        scrollToSelection();
                    }
                    dismissEditPlaceDialog();
                }
            });
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
            adapter.applyFilter(text);
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
            getActivity().invalidateOptionsMenu();
            return true;
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void deletePlace(final Context context, @Nullable final PlaceItem item)
    {
        if (item != null && item.location != null)
        {
            AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.locationdelete_dialog_title))
                    .setMessage(context.getString(R.string.locationdelete_dialog_message, item.location.getLabel()))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(context.getString(R.string.locationdelete_dialog_ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            DeletePlaceTask task = new DeletePlaceTask(context);
                            task.setTaskListener(new DeletePlaceTask.TaskListener()
                            {
                                @Override
                                public void onFinished(long rowID, boolean result) {
                                    adapter.removeItem(rowID);
                                    setModified(true);
                                }
                            });
                            task.execute(item.rowID);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.locationdelete_dialog_cancel), null);

            confirm.show();
        }
    }

    public static class DeletePlaceTask extends AsyncTask<Object, Object, Boolean>
    {
        private GetFixDatabaseAdapter database;
        private long rowID = -1;

        public DeletePlaceTask(Context context) {
            database = new GetFixDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected Boolean doInBackground(Object... params)
        {
            if (params.length > 0) {
                rowID = (Long)params[0];
            }
            if (rowID != -1)
            {
                database.open();
                boolean result = database.removePlace(rowID);
                database.close();
                return result;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (taskListener != null)
                taskListener.onFinished(rowID, result);
        }

        private TaskListener taskListener = null;
        public void setTaskListener( TaskListener listener ) {
            taskListener = listener;
        }
        public static abstract class TaskListener
        {
            public void onFinished( long rowID, boolean result ) {}
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
                Toast.makeText(context, context.getString(R.string.locationcleared_toast_success), Toast.LENGTH_LONG).show();
            }
            reloadAdapter();
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void exportPlaces(Context context)
    {
        ExportPlacesTask task = new ExportPlacesTask(context, "SuntimesPlaces", true, true);  // export to external cache
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
                if (results.getResult())
                {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setType(results.getMimeType());
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        //Uri shareURI = Uri.fromFile(results.getExportFile());  // this URI works until api26 (throws FileUriExposedException)
                        Uri shareURI = FileProvider.getUriForFile(context, "com.forrestguice.suntimeswidget.fileprovider", results.getExportFile());
                        shareIntent.putExtra(Intent.EXTRA_STREAM, shareURI);

                        String successMessage = context.getString(R.string.msg_export_success, results.getExportFile().getAbsolutePath());
                        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show();

                        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.msg_export_to)));
                        return;   // successful export ends here...

                    } catch (Exception e) {
                        Log.e("ExportPlaces", "Failed to share file URI! " + e);
                    }

                }

                File file = results.getExportFile();    // export failed
                String path = ((file != null) ? file.getAbsolutePath() : "<path>");
                String failureMessage = context.getString(R.string.msg_export_failure, path);
                Toast.makeText(context, failureMessage, Toast.LENGTH_LONG).show();
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
            if (listener != null) {
                listener.onStarted();
            }

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

            return result;
        }

        @Override
        protected void onPostExecute(List<PlaceItem> result)
        {
            if (listener != null) {
                listener.onFinished(result);
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
            if (listener != null) {
                listener.onStarted();
            }

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

    public void setSelectedRowID( long rowID ) {
        adapter.setSelectedRowID(rowID);
    }
    public long selectedRowID() {
        return adapter.getSelectedRowID();
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
    }

    public interface AdapterListener {
        void onItemClicked(PlaceItem item, int position);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListAdapter extends RecyclerView.Adapter<PlacesListViewHolder> implements Filterable
    {
        protected WeakReference<Context> contextRef;
        protected ArrayList<PlaceItem> items0, items;
        protected String filterText = "";

        public PlacesListAdapter(Context context)
        {
            contextRef = new WeakReference<>(context);
            items0 = new ArrayList<>();
            items = new ArrayList<>();
        }

        public void setValues(List<PlaceItem> values)
        {
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
            }
            applyFilter(getFilterText());
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
                        return o1.location.getLabel().compareTo(o2.location.getLabel());
                    }
                }
            });
            return items;
        }

        private long selectedRowID = -1;
        public void setSelectedRowID( long rowID )
        {
            selectedRowID = rowID;
            notifyDataSetChanged();
        }
        public long getSelectedRowID() {
            return selectedRowID;
        }
        public void clearSelection() {
            setSelectedRowID(-1);
        }

        public int getSelectedPosition() {
            return indexOf(selectedRowID);
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
            holder.selected = (item.rowID == selectedRowID);
            holder.bindViewHolder(contextRef.get(), item);
            attachClickListeners(holder, position);
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

        protected void attachClickListeners(PlacesListViewHolder holder, int position)
        {
            if (holder.itemView != null) {
                holder.itemView.setOnClickListener(onItemClicked(position));
            }
        }

        protected View.OnClickListener onItemClicked(final int position)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(items.get(position), position);
                    }
                }
            };
        }

        protected void detachClickListeners(PlacesListViewHolder holder)
        {
            if (holder.itemView != null) {
                holder.itemView.setOnClickListener(null);
            }
        }

        public void applyFilter(String text) {
            filterText = text;
            getFilter().filter(filterText);
        }

        public String getFilterText() {
            return filterText;
        }

        @Override
        public Filter getFilter()
        {
            return new Filter()
            {
                @Override
                protected FilterResults performFiltering(CharSequence constraint)
                {
                    FilterResults results = new FilterResults();
                    results.values = new ArrayList<>((constraint.length() > 0) ? getFilteredValues(constraint.toString().toLowerCase()) : items0);
                    return results;
                }

                protected List<PlaceItem> getFilteredValues(String constraint)
                {
                    List<PlaceItem> values0  = new ArrayList<>();
                    List<PlaceItem> values1  = new ArrayList<>();
                    for (PlaceItem item : items0)
                    {
                        String label = item.location.getLabel().toLowerCase().trim();

                        if (label.startsWith(constraint)) {
                            values0.add(item);
                        } else if (label.contains(constraint)) {
                            values1.add(item);
                        }
                    }
                    List<PlaceItem> values = new ArrayList<>(values0);
                    values.addAll(values1);
                    return values;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results)
                {
                    items.clear();
                    items.addAll((List<PlaceItem>) results.values);
                    notifyDataSetChanged();
                }
            };
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
