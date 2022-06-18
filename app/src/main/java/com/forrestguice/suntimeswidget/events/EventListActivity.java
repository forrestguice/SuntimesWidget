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
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class EventListActivity extends AppCompatActivity
{
    public static final int PICK_EVENT_REQUEST = 1;

    public static final String SELECTED_EVENTID = "eventID";
    public static final String ADAPTER_MODIFIED = "isModified";
    public static final String PARAM_SELECTED = "selected";
    public static final String PARAM_NOSELECT = "noselect";

    private EventListHelper helper;
    private String preselectedEvent;

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
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_eventlist);

        Intent intent = getIntent();
        preselectedEvent = intent.getStringExtra(PARAM_SELECTED);

        helper = new EventListHelper(this, getSupportFragmentManager());
        helper.initViews(this, findViewById(android.R.id.content), icicle);
        helper.setDisallowSelect(intent.getBooleanExtra(PARAM_NOSELECT, false));

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (preselectedEvent != null && !preselectedEvent.trim().isEmpty()) {
            helper.setSelected(preselectedEvent);
            helper.triggerActionMode();
        }
    }

    private View.OnClickListener onItemAccepted = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(SELECTED_EVENTID, helper.getEventID());
            intent.putExtra(ADAPTER_MODIFIED, helper.isAdapterModified());
            setResult(Activity.RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.transition_ok_in, R.anim.transition_ok_out);
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        helper.setFragmentManager(getSupportFragmentManager());
        helper.setOnItemAcceptedListener(onItemAccepted);
        helper.onResume();
    }

    @Override
    public void onBackPressed() {
        onCancelled.onClick(null);
    }
    private View.OnClickListener onCancelled = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(ADAPTER_MODIFIED, helper.isAdapterModified());
            setResult(((helper.isAdapterModified()) ? Activity.RESULT_OK : Activity.RESULT_CANCELED), intent);
            finish();
            overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.eventlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.addEvent:
                helper.addEvent();
                return true;

            case R.id.clearEvents:
                helper.clearEvents();
                return true;

            case R.id.exportEvents:
                helper.exportEvents();
                return true;

            case R.id.importEvents:
                helper.importEvents();
                return true;

            case R.id.helpEvents:
                helper.showHelp();
                return true;

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
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState(outState);
        helper.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        helper.onRestoreInstanceState(savedState);
    }

}
