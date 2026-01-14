/**
    Copyright (C) 2022-2025 Forrest Guice
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidEventSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.support.widget.Toolbar;

public class EventListActivity extends AppCompatActivity
{
    public static final int PICK_EVENT_REQUEST = 121;

    public static final String SELECTED_EVENTID = "eventID";
    public static final String SELECTED_EVENTURI = "eventUri";

    public static final String ADAPTER_MODIFIED = EventListFragment.ADAPTER_MODIFIED;
    public static final String EXTRA_SELECTED = EventListFragment.EXTRA_SELECTED;
    public static final String EXTRA_NOSELECT = EventListFragment.EXTRA_NOSELECT;
    public static final String EXTRA_EXPANDED = EventListFragment.EXTRA_EXPANDED;

    public static final String EXTRA_LOCATION = EventListFragment.EXTRA_LOCATION;    // supply a Location (serializable) or ...
    public static final String EXTRA_LOCATION_LABEL = "location_label";                  // provide latitude, longitude, and altitude separately
    public static final String EXTRA_LOCATION_LATITUDE = "location_latitude";
    public static final String EXTRA_LOCATION_LONGITUDE = "location_longitude";
    public static final String EXTRA_LOCATION_ALTITUDE = "location_altitude";

    public static final String EXTRA_TYPEFILTER = EventListFragment.EXTRA_TYPEFILTER;
    public static final String EXTRA_SELECTFILTER = EventListFragment.EXTRA_SELECTFILTER;

    public static final String EXTRA_ADD_ANGLE = "addEventWithAngle";                  // degrees
    public static final String EXTRA_ADD_SHADOWLENGTH = "addEventWithShadowLength";    // meters
    public static final String EXTRA_ADD_OBJECTHEIGHT = "addEventWithObjectHeight";    // meters

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

    @SuppressWarnings("RedundantCast")
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
        list.setTypeFilter(intent.getStringArrayExtra(EXTRA_TYPEFILTER));
        list.setSelectFilter(intent.getStringArrayExtra(EXTRA_SELECTFILTER));
        list.setPreselected(intent.getStringExtra(EXTRA_SELECTED));

        if (intent.hasExtra(EXTRA_LOCATION))
        {
            Location location = (Location) intent.getSerializableExtra(EXTRA_LOCATION);
            list.setLocation(location);

        } else if (intent.hasExtra(EXTRA_LOCATION_LATITUDE) && intent.hasExtra(EXTRA_LOCATION_LONGITUDE)) {
            try {
                String label = intent.getStringExtra(EXTRA_LOCATION_LABEL);
                double latitude = intent.getDoubleExtra(EXTRA_LOCATION_LATITUDE, 0);
                double longitude = intent.getDoubleExtra(EXTRA_LOCATION_LONGITUDE, 0);
                double altitude = intent.getDoubleExtra(EXTRA_LOCATION_ALTITUDE, 0);

                Location.verifyLatitude(latitude);
                Location.verifyLongitude(longitude);
                list.setLocation(new Location(label, "" + latitude, "" + longitude, "" + altitude));

            } catch (Exception e) {
                Log.w("EventListActivity", "Ignoring invalid EXTRA_LOCATION_ values: " + e);
            }
        }
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, list, "EventList")
                .commit();

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final double extra_addEventWithAngle = intent.getDoubleExtra(EXTRA_ADD_ANGLE, -1);
        intent.removeExtra(EXTRA_ADD_ANGLE);

        final double extra_addEventWithShadowLength = intent.getDoubleExtra(EXTRA_ADD_SHADOWLENGTH, -1);
        intent.removeExtra(EXTRA_ADD_SHADOWLENGTH);

        final double extra_addEventWithObjectHeight = intent.getDoubleExtra(EXTRA_ADD_OBJECTHEIGHT, -1);
        intent.removeExtra(EXTRA_ADD_OBJECTHEIGHT);

        menuBar.post(new Runnable() {
            @Override
            public void run()
            {
                if (extra_addEventWithAngle != -1) {
                    list.showAddEventDialog(EventType.SUN_ELEVATION, extra_addEventWithAngle, null, null);

                } else if (extra_addEventWithShadowLength != -1 || extra_addEventWithObjectHeight != -1) {
                    Double shadowLength = ((extra_addEventWithShadowLength != -1) ? extra_addEventWithShadowLength : null);
                    Double objHeight = ((extra_addEventWithObjectHeight != -1) ? extra_addEventWithObjectHeight : null);
                    list.showAddEventDialog(EventType.SHADOWLENGTH, null, shadowLength, objHeight);
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        list.setFragmentListener(listFragmentListener);
    }

    private final EventListFragment.FragmentListener listFragmentListener = new EventListFragment.FragmentListener()
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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, @NonNull Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    /**
     * @return true adapter modified
     */
    public static boolean onEventListActivityResult(@NonNull Context context, int requestCode, int resultCode, @Nullable Intent data)
    {
        boolean adapterModified = ((data != null) && data.getBooleanExtra(EventListActivity.ADAPTER_MODIFIED, false));
        if (resultCode == RESULT_OK)
        {
            String eventID = ((data != null) ? data.getStringExtra(EventListActivity.SELECTED_EVENTID) : null);
            if (eventID != null) {
                EventSettings.setShown(AndroidEventSettings.wrap(context), eventID, true);
                adapterModified = true;
            }
        }
        return adapterModified;
    }
}
