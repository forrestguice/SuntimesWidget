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

import android.app.Activity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.settings.CompareMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.widget.Toolbar;

public class ActionListActivity extends AppCompatActivity
{
    public static final int PICK_ACTION_REQUEST = 1;

    public static final String SELECTED_ACTIONID = "actionID";
    public static final String ADAPTER_MODIFIED = "isModified";
    public static final String PARAM_SELECTED = "selected";
    public static final String PARAM_NOSELECT = "noselect";

    private ActionListHelper helper;
    private String preselectedAction;

    public ActionListActivity() {
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
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(icicle);
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_actionlist);

        Intent intent = getIntent();
        preselectedAction = intent.getStringExtra(PARAM_SELECTED);

        initData(this);

        helper = new ActionListHelper(this, getSupportFragmentManager());
        helper.setData(data);
        helper.initViews(this, findViewById(android.R.id.content), icicle);
        helper.setDisallowSelect(intent.getBooleanExtra(PARAM_NOSELECT, false));

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (preselectedAction != null && !preselectedAction.trim().isEmpty()) {
            helper.setSelected(preselectedAction);
            helper.triggerActionMode();
        }
    }

    private final View.OnClickListener onItemAccepted = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(SELECTED_ACTIONID, helper.getIntentID());
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
        helper.setData(data);
        helper.setOnItemAcceptedListener(onItemAccepted);
        helper.onResume();
    }

    private SuntimesRiseSetData data;
    private void initData(Context context)
    {
        data = new SuntimesRiseSetData(context, AppWidgetManager.INVALID_APPWIDGET_ID);   // use app configuration
        data.setCompareMode(CompareMode.TOMORROW);
        data.setTimeMode(TimeMode.OFFICIAL);
        data.calculate(context);

        SuntimesRiseSetData noonData = new SuntimesRiseSetData(data);
        noonData.setTimeMode(TimeMode.NOON);
        noonData.calculate(context);
        data.linkData(noonData);
    }

    @Override
    public void onBackPressed() {
        onCancelled.onClick(null);
    }
    private final View.OnClickListener onCancelled = new View.OnClickListener() {
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
        inflater.inflate(R.menu.editintent1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.addAction:
                helper.addAction();
                return true;

            case R.id.clearAction:
                helper.clearActions();
                return true;

            case R.id.exportAction:
                helper.exportActions();
                return true;

            case R.id.importAction:
                helper.importActions();
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
        PopupMenuCompat.forceActionBarIcons(menu);
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
