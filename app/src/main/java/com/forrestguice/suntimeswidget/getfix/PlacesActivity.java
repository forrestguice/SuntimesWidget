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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.support.widget.Toolbar;

public class PlacesActivity extends AppCompatActivity
{
    public static final String EXTRA_ADAPTER_MODIFIED = "isModified";
    public static final String EXTRA_ALLOW_PICK = "allowPick";
    public static final String EXTRA_SELECTED = "selectedRowID";
    public static final String EXTRA_LOCATION = "selectedLocation";

    protected PlacesListFragment list;

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        initLocale();
        setContentView(R.layout.layout_activity_places);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        list = (PlacesListFragment) getSupportFragmentManager().findFragmentById(R.id.placesListFragment);
        if (list != null)
        {
            list.setDialogThemOverride(AppSettings.loadTheme(this));
            list.setFragmentListener(listFragmentListener);
            list.setAllowPick(intent.getBooleanExtra(EXTRA_ALLOW_PICK, false));
            list.setSelectedRowID(intent.getLongExtra(EXTRA_SELECTED, -1));
        }
    }

    protected void initLocale()
    {
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.placesactivity, menu);
        return true;
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            showAbout();
            return true;

        } else if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final PlacesListFragment.FragmentListener listFragmentListener = new PlacesListFragment.FragmentListener()
    {
        @Override
        public boolean onItemEdit(PlaceItem item) {
            //editPlace(item);
            return false;
        }

        @Override
        public void onItemPicked(PlaceItem item) {
            pickPlace(item);
        }

        @Override
        public void onActionModeFinished() {}

        @Override
        public void onItemClicked(PlaceItem item, int position) { /* EMPTY */ }

        @Override
        public boolean onItemLongClicked(PlaceItem item, int position) {
            return false;
        }

        @Override
        public void onFilterChanged(String filterText, Long[] filterExceptions) {}

        @Override
        public void onToggleProgress(boolean value) {
            toggleProgress(value);
        }

        @Override
        public void onLiftAppBar(boolean value) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(value ? SuntimesUtils.dpToPixels(PlacesActivity.this, 50) : 0);
            }
        }
    };

    protected void pickPlace(PlaceItem item)
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SELECTED, item.rowID);
        intent.putExtra(EXTRA_LOCATION, item.location);
        intent.putExtra(EXTRA_ADAPTER_MODIFIED, list.isModified());
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.transition_ok_in, R.anim.transition_ok_out);
    }

    protected void toggleProgress(boolean value)
    {
        View progress = findViewById(R.id.app_progress);
        if (progress != null) {
            progress.setVisibility(value ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ADAPTER_MODIFIED, list.isModified());
        setResult(list.isModified() ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }

    protected void showAbout()
    {
        Intent about = new Intent(this, AboutActivity.class);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }
}
