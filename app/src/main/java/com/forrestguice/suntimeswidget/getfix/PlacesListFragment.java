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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.LocationConfigDialog;
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
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.placeslist, menu);
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

    protected boolean triggerActionMode(View view, PlaceItem item)
    {
        if (actionMode == null)
        {
            if (item != null)
            {
                adapter.setSelectedRowID(item.rowID);
                actions.setItem(item);

                AppCompatActivity activity = (AppCompatActivity) getActivity();
                actionMode = activity.startSupportActionMode(actions);
                if (actionMode != null) {
                    actionMode.setTitle(item.location != null ? item.location.getLabel() : "");
                    actionMode.setSubtitle(item.location != null ? locationDisplayString(activity, item.location, true) : "");
                }
            }
            return true;

        } else {
            actionMode.finish();
            triggerActionMode(view, item);
            return false;
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
                    triggerActionMode(null, adapter.getItem(selectedRowID));
                }
            }
        };
    }

    protected AdapterListener listAdapterListener = new AdapterListener()
    {
        @Override
        public void onItemClicked(PlaceItem item, int position)
        {
            triggerActionMode(null, item);
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
        // TODO: add place
        setModified(true);   // TODO: move to onAdded
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
                dialog.setLocation(context, item.location);
                dialog.show(getChildFragmentManager(), DIALOG_EDITPLACE);
            }
        }
    }

    private PlacesEditFragment.FragmentListener onEditPlace = new PlacesEditFragment.FragmentListener() {
        // TODO
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

    public static class PlacesListTask extends AsyncTask<Void, Location, List<PlaceItem>>
    {
        protected GetFixDatabaseAdapter database;

        public PlacesListTask(@NonNull Context context) {
            database = new GetFixDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected List<PlaceItem> doInBackground(Void... voids)
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
                    Location location = new Location(name, lat, lon, alt);
                    location.setUseAltitude(true);
                    result.add(new PlaceItem(cursor.getLong(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_ROWID)), location));
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

    public static class PlacesListAdapter extends RecyclerView.Adapter<PlacesListViewHolder>
    {
        protected WeakReference<Context> contextRef;
        protected ArrayList<PlaceItem> items;

        public PlacesListAdapter(Context context) {
            contextRef = new WeakReference<>(context);
            items = new ArrayList<>();
        }

        public void setValues(List<PlaceItem> values)
        {
            items.clear();
            items.addAll(sortItems(values));
            notifyDataSetChanged();
        }

        public int indexOf(long rowID)
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
            int position = indexOf(rowID);
            if (position != -1)
            {
                items.remove(position);
                notifyItemRemoved(position);
            }
        }

        public PlaceItem getItem(long rowID)
        {
            int position = indexOf(rowID);
            if (position >= 0) {
                return items.get(position);
            } else return null;
        }

        protected List<PlaceItem> sortItems(List<PlaceItem> items)
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListViewHolder extends RecyclerView.ViewHolder
    {
        public TextView label;
        public TextView summary;
        public boolean selected = false;

        public PlacesListViewHolder(View itemView)
        {
            super(itemView);
            label = (TextView) itemView.findViewById(android.R.id.text1);
            summary = (TextView) itemView.findViewById(android.R.id.text2);
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
        }

        public void unbindViewHolder() {
            selected = false;
            bindViewHolder(null, null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlaceItem
    {
        public long rowID = -1;
        public Location location = null;

        public PlaceItem() {}

        public PlaceItem( long rowID, Location location )
        {
            this.rowID = rowID;
            this.location = location;
        }
    }

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
