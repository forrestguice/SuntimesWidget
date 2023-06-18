/**
    Copyright (C) 2023 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.ui.bedtime;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;

@SuppressWarnings("Convert2Diamond")
public class BedtimeDialog extends DialogFragment
{
    protected RecyclerView list;
    protected BedtimeItemAdapter adapter;

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
        View content = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_bedtime, parent, false);

        adapter = new BedtimeItemAdapter();
        adapter.setAdapterListener(adapterListener);

        list = (RecyclerView) content.findViewById(R.id.recyclerview);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        //list.addItemDecoration(itemDecoration);
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

    protected void loadSettings(Bundle bundle) {
    }

    protected void saveSettings(Bundle bundle) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bedtime_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public RecyclerView getList() {
        return list;
    }

    public BedtimeItemAdapter getAdapter() {
        return adapter;
    }

    public void reloadAdapter() {
        adapter.initItems();
        adapter.notifyDataSetChanged();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected BedtimeItemAdapter.AdapterListener listener;
    protected BedtimeItemAdapter.AdapterListener adapterListener = new BedtimeItemAdapter.AdapterListener() {
    };

    public void setAdapterListener(BedtimeItemAdapter.AdapterListener listener) {
        this.listener = listener;
    }
}
