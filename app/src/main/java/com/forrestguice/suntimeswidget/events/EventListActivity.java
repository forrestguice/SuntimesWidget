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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;

public class EventListActivity extends AppCompatActivity
{
    public static final int PICK_EVENT_REQUEST = 121;

    public static final String SELECTED_EVENTID = "eventID";
    public static final String SELECTED_EVENTURI = "eventUri";

    public static final String ADAPTER_MODIFIED = EventListFragment.ADAPTER_MODIFIED;
    public static final String EXTRA_SELECTED = EventListFragment.EXTRA_SELECTED;
    public static final String EXTRA_NOSELECT = EventListFragment.EXTRA_NOSELECT;
    public static final String EXTRA_EXPANDED = EventListFragment.EXTRA_EXPANDED;
    public static final String EXTRA_LOCATION = EventListFragment.EXTRA_LOCATION;

    protected EventListFragment list;

    public EventListActivity() {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(savedState);
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_eventlist);
        Intent intent = getIntent();

        list = new EventListFragment();
        list.setDisallowSelect(intent.getBooleanExtra(EXTRA_NOSELECT, false));
        list.setExpanded(intent.getBooleanExtra(EXTRA_EXPANDED, false));
        list.setPreselected(intent.getStringExtra(EXTRA_SELECTED));

        Location location = intent.getParcelableExtra(EXTRA_LOCATION);
        list.setLocation(location);

        FragmentManager fragments = getSupportFragmentManager();
        FragmentTransaction transaction = fragments.beginTransaction();
        transaction.replace(R.id.fragmentContainer, list, "EventList");
        transaction.commit();

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        list.setFragmentListener(listFragmentListener);
    }

    private EventListFragment.FragmentListener listFragmentListener = new EventListFragment.FragmentListener()
    {
        @Override
        public void onItemPicked(String eventID, String eventUri) {
            pickEvent(eventID, eventUri);
        }
    };

    private void pickEvent(String eventID, String eventUri)
    {
        Intent intent = new Intent();
        intent.putExtra(SELECTED_EVENTID, eventID);
        intent.putExtra(SELECTED_EVENTURI, eventUri);
        intent.putExtra(ADAPTER_MODIFIED, list.isModified());
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.transition_ok_in, R.anim.transition_ok_out);
    }

    private void cancelPickEvent()
    {
        Intent intent = new Intent();
        intent.putExtra(ADAPTER_MODIFIED, list.isModified());
        setResult(((list.isModified()) ? Activity.RESULT_OK : Activity.RESULT_CANCELED), intent);
        finish();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }

    @Override
    public void onBackPressed() {
        cancelPickEvent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }
}
