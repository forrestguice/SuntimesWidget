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

package com.forrestguice.suntimeswidget.getfix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PlacesListFragment extends Fragment
{
    protected PlacesListAdapter adapter;
    protected RecyclerView listView;

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

        if (savedState != null) {
            // TODO
        }

        adapter = new PlacesListAdapter();
        adapter.setAdapterListener(listAdapterListener);

        listView = (RecyclerView) dialogContent.findViewById(R.id.placesList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(adapter);

        reloadAdapter();
        return dialogContent;
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
            case R.id.clearPlaces:
                clearPlaces();
                return true;

            case R.id.exportPlaces:
                exportPlaces();
                return true;

            case R.id.addWorldPlaces:
                addWorldPlaces();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clearPlaces() {
        // TODO
    }

    public void exportPlaces() {
        // TODO
    }

    public void addWorldPlaces() {
        // TODO
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void reloadAdapter()
    {
        PlacesListTask listTask = new PlacesListTask(getActivity());
        listTask.setTaskListener(listTaskListener);
        listTask.execute();
    }

    protected PlacesListTask.TaskListener listTaskListener = new PlacesListTask.TaskListener()
    {
        @Override
        public void onStarted() {}

        @Override
        public void onFinished(List<PlaceItem> results) {
            adapter.setValues(results);
        }
    };

    protected PlacesListAdapter.AdapterListener listAdapterListener = new PlacesListAdapter.AdapterListener()
    {
        @Override
        public void onItemClicked(PlaceItem item, int position) {
            adapter.setSelectedRowID(item.rowID);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListTask extends AsyncTask<Void, Location, List<PlaceItem>>
    {
        protected GetFixDatabaseAdapter database;

        public PlacesListTask(Context context) {
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

    public static class PlacesListAdapter extends RecyclerView.Adapter<PlacesListViewHolder>
    {
        protected ArrayList<PlaceItem> items = new ArrayList<>();

        public void setValues(List<PlaceItem> values)
        {
            items.clear();
            items.addAll(values);
            notifyDataSetChanged();
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
            View view = layout.inflate(android.R.layout.simple_selectable_list_item, parent, false);
            return new PlacesListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlacesListViewHolder holder, int position)
        {
            PlaceItem item = items.get(position);
            holder.selected = (item.rowID == selectedRowID);
            holder.bindViewHolder(item);
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
            if (holder.label != null) {
                holder.label.setOnClickListener(onItemClicked(position));
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
            if (holder.label != null) {
                holder.label.setOnClickListener(null);  // TODO
            }
        }

        public interface AdapterListener {
            void onItemClicked(PlaceItem item, int position);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListViewHolder extends RecyclerView.ViewHolder
    {
        public CheckedTextView label;
        public TextView summary;
        public boolean selected = false;

        public PlacesListViewHolder(View itemView)
        {
            super(itemView);
            label = (CheckedTextView) itemView.findViewById(android.R.id.text1);
            summary = (TextView) itemView.findViewById(android.R.id.text2);
        }

        public void bindViewHolder( PlaceItem item )
        {
            if (label != null)
            {
                label.setText(item != null && item.location != null ? item.location.getLabel() : "");
                //label.setBackgroundColor(selected ? Color.BLUE : Color.BLACK);  // TODO
                label.setChecked(selected);
            }
            if (summary != null) {
                summary.setText(item != null && item.location != null ? item.location.getLatitude() + ", " + item.location.getLongitude() + " (TODO)" : "");  // TODO
            }

        }

        public void unbindViewHolder() {
            selected = false;
            bindViewHolder(null);
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

}
