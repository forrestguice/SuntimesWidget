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
        public void onFinished(List<Location> results) {
            adapter.setValues(results);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListTask extends AsyncTask<Void, Location, List<Location>>
    {
        protected GetFixDatabaseAdapter database;

        public PlacesListTask(Context context) {
            database = new GetFixDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected List<Location> doInBackground(Void... voids)
        {
            if (listener != null) {
                listener.onStarted();
            }

            ArrayList<Location> result = new ArrayList<>();

            database.open();
            Cursor cursor = database.getAllPlaces(0, true);
            if (cursor != null)
            {
                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    String name = values.getAsString(GetFixDatabaseAdapter.KEY_PLACE_NAME);
                    String lat = values.getAsString(GetFixDatabaseAdapter.KEY_PLACE_LATITUDE);
                    String lon = values.getAsString(GetFixDatabaseAdapter.KEY_PLACE_LONGITUDE);
                    String alt = values.getAsString(GetFixDatabaseAdapter.KEY_PLACE_ALTITUDE);
                    Location location = new Location(name, lat, lon, alt);
                    location.setUseAltitude(true);
                    result.add(location);
                    cursor.moveToNext();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<Location> result)
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
            void onFinished(List<Location> results);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListAdapter extends RecyclerView.Adapter<PlacesListViewHolder>
    {
        protected ArrayList<Location> locations = new ArrayList<>();

        public void setValues(List<Location> values)
        {
            locations.clear();
            locations.addAll(values);
            notifyDataSetChanged();
        }

        @Override
        public PlacesListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new PlacesListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlacesListViewHolder holder, int position) {
            holder.bindViewHolder(locations.get(position));
        }

        @Override
        public void onViewRecycled(PlacesListViewHolder holder) {
            holder.unbindViewHolder();
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListViewHolder extends RecyclerView.ViewHolder
    {
        public TextView label;
        public TextView summary;

        public PlacesListViewHolder(View itemView)
        {
            super(itemView);
            label = (TextView) itemView.findViewById(android.R.id.text1);
            summary = (TextView) itemView.findViewById(android.R.id.text2);
        }

        public void bindViewHolder( Location location )
        {
            if (label != null) {
                label.setText(location != null ? location.getLabel() : "");
            }
            if (summary != null) {
                summary.setText(location != null ? location.getLatitude() + ", " + location.getLongitude() + " (TODO)" : "");  // TODO
            }
        }

        public void unbindViewHolder() {
            bindViewHolder(null);
        }
    }

}
