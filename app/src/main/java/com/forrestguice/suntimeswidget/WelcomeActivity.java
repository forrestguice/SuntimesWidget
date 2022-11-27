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

package com.forrestguice.suntimeswidget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;
import java.util.TimeZone;

public class WelcomeActivity extends AppCompatActivity
{
    private ViewPager pager;
    private WelcomeFragmentAdapter pagerAdapter;
    private Button nextButton, prevButton;
    private LinearLayout indicatorLayout;
    private AppSettings.LocaleInfo localeInfo;

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase, localeInfo = new AppSettings.LocaleInfo());
        super.attachBaseContext(context);
        WidgetSettings.initDefaults(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21)
        {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.layout_activity_welcome);

        pagerAdapter = new WelcomeFragmentAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.container);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(pagerChangeListener);
        pager.setOffscreenPageLimit(pagerAdapter.getCount()-1);   // don't recreate page fragments (retain state)

        prevButton = (Button) findViewById(R.id.button_prev);
        if (prevButton != null) {
            prevButton.setOnClickListener(onPrevPressed);
        }

        nextButton = (Button) findViewById(R.id.button_next);
        if (nextButton != null) {
            nextButton.setOnClickListener(onNextPressed);
        }

        indicatorLayout = (LinearLayout) findViewById(R.id.indicator_layout);
        pagerChangeListener.onPageSelected(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default: return (super.onOptionsItemSelected(menuItem));
        }
    }

    @Override
    public void onBackPressed() {
        onPrevPressed.onClick(prevButton);
    }

    private View.OnClickListener onPrevPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (pager.getCurrentItem() <= 0) {
                onSkip();
            } else {
                onPrev();
            }
        }
    };

    private View.OnClickListener onNextPressed = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if ((pager.getCurrentItem() + 1) < pagerAdapter.getCount()) {
                onNext();
            } else {
                onDone();
            }
        }
    };

    private ViewPager.OnPageChangeListener pagerChangeListener = new ViewPager.OnPageChangeListener()
    {
        private int previousPosition = 0;

        @Override
        public void onPageSelected(int position)
        {
            if (saveSettings(getSupportFragmentManager(), previousPosition))
            {
                updateViews(getSupportFragmentManager(), position);
                setIndicator(position);
                prevButton.setText(getString((position != 0) ? R.string.welcome_action_prev : R.string.welcome_action_skip));
                nextButton.setText(getString((position != pagerAdapter.getCount()-1) ? R.string.welcome_action_next : R.string.welcome_action_done));
                previousPosition = position;

            } else {
                pager.setCurrentItem(previousPosition);
            }
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {}

        @Override
        public void onPageScrollStateChanged(int arg0) {}
    };

    @SuppressLint("ResourceType")
    private void setIndicator(int position)
    {
        int[] colorAttrs = { R.attr.text_accentColor, R.attr.text_disabledColor };
        TypedArray typedArray = obtainStyledAttributes(colorAttrs);
        int activeColor = ContextCompat.getColor(WelcomeActivity.this, typedArray.getResourceId(0, R.color.text_accent_dark));
        int inactiveColor = ContextCompat.getColor(WelcomeActivity.this, typedArray.getResourceId(1, R.color.text_disabled_dark));
        typedArray.recycle();

        TextView[] indicators = new TextView[pagerAdapter.getCount()];
        for (int i=0; i<indicators.length; i++)
        {
            indicators[i] = new TextView(this);
            indicators[i].setTextSize(36);
            indicators[i].setTextColor((i == position) ? activeColor : inactiveColor);
            indicators[i].setText("\u2022");
        }

        indicatorLayout.removeAllViews();
        for (TextView indicator : indicators) {
            indicatorLayout.addView(indicator);
        }
    }

    private void onNext() {
        pager.setCurrentItem(pager.getCurrentItem() + 1, true);
    }

    private void onPrev() {
        pager.setCurrentItem(pager.getCurrentItem() - 1, true);
    }

    private void onSkip()
    {
        AppSettings.setFirstLaunch(WelcomeActivity.this, false);
        finish();
    }

    private void onDone()
    {
        saveSettings(getSupportFragmentManager(), pager.getCurrentItem());
        AppSettings.setFirstLaunch(WelcomeActivity.this, false);
        finish();
    }

    private void saveSettings()
    {
        FragmentManager fragments = getSupportFragmentManager();
        for (int i=0; i<pagerAdapter.getCount(); i++) {
            saveSettings(fragments, i);
        }
    }
    private boolean saveSettings(FragmentManager fragments, int position)
    {
        WelcomeFragment page = getPageFragment(fragments, pager, position);
        if (page != null) {
            if (page.validateInput(WelcomeActivity.this)) {
                return page.saveSettings(WelcomeActivity.this);
            } else return false;
        }
        return false;
    }
    private boolean validateInput(FragmentManager fragments, int position)
    {
        WelcomeFragment page = getPageFragment(fragments, pager, position);
        if (page != null) {
            return page.validateInput(WelcomeActivity.this);
        }
        return true;
    }
    private void updateViews(FragmentManager fragments, int position)
    {
        WelcomeFragment page = getPageFragment(fragments, pager, position);
        if (page != null) {
            page.updateViews(WelcomeActivity.this);
        }
    }

    public static WelcomeFragment getPageFragment(FragmentManager fragments, ViewPager pager, int position) {
        // https://stackoverflow.com/questions/54279509/how-to-get-elements-of-fragments-created-by-viewpager-in-mainactivity/54280113#54280113
        return (WelcomeFragment) fragments.findFragmentByTag("android:switcher:" + pager.getId() + ":" + position);
    }

    public void showAbout( View v )
    {
        startActivity(new Intent(this, AboutActivity.class));
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * WelcomeFragmentAdapter
     */
    private class WelcomeFragmentAdapter extends FragmentPagerAdapter
    {
        public WelcomeFragmentAdapter(FragmentManager fragments)
        {
            super(fragments);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 5: return WelcomeFragment.newInstance(R.layout.layout_welcome_legal);
                case 4: return WelcomeAlarmsFragment.newInstance();
                case 3: return WelcomeTimeZoneFragment.newInstance(WelcomeActivity.this);
                case 2: return WelcomeLocationFragment.newInstance();
                case 1: return WelcomeAppearanceFragment.newInstance();
                case 0: default: return WelcomeFirstPageFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    }

    /**
     * WelcomeFragment
     */
    public static class WelcomeFragment extends Fragment
    {
        public static final String ARG_LAYOUT_RESID = "layoutResID";

        public WelcomeFragment() {}

        public static WelcomeFragment newInstance(int layoutResID)
        {
            WelcomeFragment fragment = new WelcomeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, layoutResID);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = inflater.inflate(getLayoutResID(), container, false);
            initViews(getActivity(), view);
            updateViews(getActivity());
            return view;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            updateViews(getActivity());
        }

        @Override
        public void setUserVisibleHint(boolean value)
        {
            super.setUserVisibleHint(value);
            if (isResumed()) {
                updateViews(getActivity());
            }
        }

        public void initViews(Context context, View view)
        {
            if (view != null)
            {
                int[] textViews = new int[] { R.id.text0, R.id.text1, R.id.text2, R.id.text3 };
                for (int resID : textViews) {
                    TextView text = (TextView) view.findViewById(resID);
                    if (text != null) {
                        text.setText(SuntimesUtils.fromHtml(text.getText().toString()));
                    }
                }

                textViews = new int[] { R.id.link0, R.id.link1, R.id.link2, R.id.link3 };
                for (int resID : textViews) {
                    TextView text = (TextView) view.findViewById(resID);
                    if (text != null) {
                        text.setText(SuntimesUtils.fromHtml(text.getText().toString()));
                        text.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }
            }
        }

        public void updateViews(Context context) {
            /* EMPTY */
        }

        public boolean validateInput(Context context) {
            return true;
        }

        public boolean saveSettings(Context context) {
            return true;
        }

        public int getLayoutResID() {
            return getArguments().getInt(ARG_LAYOUT_RESID);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * WelcomeFirstPageFragment
     */
    public static class WelcomeFirstPageFragment extends WelcomeFragment
    {
        public WelcomeFirstPageFragment() {}

        public static WelcomeFirstPageFragment newInstance()
        {
            WelcomeFirstPageFragment fragment = new WelcomeFirstPageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_app);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void initViews(Context context, View view) {
            super.initViews(context, view);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * WelcomeLocationFragment
     */
    public static class WelcomeLocationFragment extends WelcomeFragment
    {
        private Button button_addPlaces, button_lookupLocation;
        private ProgressBar progress_addPlaces;
        private View layout_permissions;

        public WelcomeLocationFragment() {}

        public static WelcomeLocationFragment newInstance()
        {
            WelcomeLocationFragment fragment = new WelcomeLocationFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_location);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void initViews(Context context, View view)
        {
            super.initViews(context, view);

            button_addPlaces = (Button) view.findViewById(R.id.button_build_places);
            if (button_addPlaces != null) {
                button_addPlaces.setOnClickListener(onAddPlacesClicked);
            }

            button_lookupLocation = (Button) view.findViewById(R.id.button_lookup_location);
            if (button_lookupLocation != null) {
                button_lookupLocation.setOnClickListener(onLookupLocationClicked);
            }

            layout_permissions = view.findViewById(R.id.layout_permissions);
            if (layout_permissions != null) {
                layout_permissions.setVisibility(View.GONE);   // toggled visible by locationConfig
            }

            LocationConfigDialog locationConfig = getLocationConfigDialog();
            if (locationConfig != null) {
                locationConfig.setDialogListener(locationConfigListener);
            }

            progress_addPlaces = (ProgressBar) view.findViewById(R.id.progress_build_places);
            toggleProgress(false);
        }

        private final LocationConfigDialog.LocationConfigDialogListener locationConfigListener = new LocationConfigDialog.LocationConfigDialogListener()
        {
            @Override
            public void onEditModeChanged(LocationConfigView.LocationViewMode mode)
            {
                switch (mode) {
                    case MODE_CUSTOM_ADD:
                    case MODE_CUSTOM_EDIT:
                        togglePermissionsText(true); break;
                    default: togglePermissionsText(false); break;
                }
            }
        };

        protected void togglePermissionsText(boolean value) {
            if (layout_permissions != null) {
                layout_permissions.setVisibility(value ? View.VISIBLE : View.GONE);
            }
        }

        private View.OnClickListener onLookupLocationClicked = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LocationConfigDialog locationConfig = getLocationConfigDialog();
                if (locationConfig != null) {
                    locationConfig.addCurrentLocation(getContext());
                }
                if (button_lookupLocation != null) {
                    button_lookupLocation.setEnabled(false);
                    button_lookupLocation.setVisibility(View.GONE);
                }
            }
        };

        private View.OnClickListener onAddPlacesClicked = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BuildPlacesTask task = new BuildPlacesTask(getActivity());
                task.setTaskListener(buildPlacesListener);
                task.execute();
            }
        };
        private BuildPlacesTask.TaskListener buildPlacesListener = new BuildPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                setRetainInstance(true);
                if (button_addPlaces != null) {
                    button_addPlaces.setEnabled(false);
                    button_addPlaces.setVisibility(View.INVISIBLE);
                }
                if (button_lookupLocation != null) {
                    button_lookupLocation.setEnabled(false);
                    button_lookupLocation.setVisibility(View.INVISIBLE);
                }
                setLocationViewMode(LocationConfigView.LocationViewMode.MODE_DISABLED);
                toggleProgress(true);
            }

            @Override
            public void onFinished(Integer result)
            {
                setRetainInstance(false);
                toggleProgress(false);

                if (result > 0)
                {
                    Context context = getActivity();
                    if (context != null) {
                        button_addPlaces.setText(context.getString(R.string.locationbuild_toast_success, result.toString()));
                        button_addPlaces.setVisibility(View.VISIBLE);
                    }
                }

                if (button_lookupLocation != null) {
                    button_lookupLocation.setEnabled(true);
                    button_lookupLocation.setVisibility(View.VISIBLE);
                }

                setLocationViewMode(LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
                reloadLocationList();
            }
        };

        protected void toggleProgress(boolean visible) {
            if (progress_addPlaces != null) {
                progress_addPlaces.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        @Nullable
        private LocationConfigDialog getLocationConfigDialog()
        {
            if (isAdded()) {
                FragmentManager fragments = getChildFragmentManager();
                if (fragments != null) {
                    return (LocationConfigDialog) fragments.findFragmentByTag("LocationConfigDialog");
                }
            }
            return null;
        }

        protected void reloadLocationList()
        {
            LocationConfigDialog locationConfig = getLocationConfigDialog();
            if (locationConfig != null) {
                locationConfig.getDialogContent().populateLocationList();
                locationConfig.getDialogContent().clickLocationSpinner();
            }
        }

        protected void setLocationViewMode( LocationConfigView.LocationViewMode value)
        {
            LocationConfigDialog locationConfig = getLocationConfigDialog();
            if (locationConfig != null) {
                locationConfig.getDialogContent().setMode(value);
            }
        }

        @Override
        public boolean validateInput(Context context)
        {
            LocationConfigDialog locationConfig = getLocationConfigDialog();
            if (locationConfig != null) {
                return locationConfig.getDialogContent().validateInput();
            }
            return super.validateInput(context);
        }

        @Override
        public boolean saveSettings(Context context)
        {
            LocationConfigDialog locationConfig = getLocationConfigDialog();
            if (locationConfig != null)
            {
                boolean saved = locationConfig.getDialogContent().saveSettings(context);
                Log.d("DEBUG", "saveSettings: location " + saved);
                return saved;
            }
            return false;
        }
    }

    /**
     * WelcomeTimeZoneFragment
     */
    public static class WelcomeTimeZoneFragment extends WelcomeFragment
    {
        private Spinner timeFormatSpinner;
        private TextView timeZoneWarning;
        private Button timeZoneSuggestButton;

        public WelcomeTimeZoneFragment()
        {
            setArguments(new Bundle());
            setLongitude(Double.parseDouble(WidgetSettings.PREF_DEF_LOCATION_LONGITUDE));
        }

        public static WelcomeTimeZoneFragment newInstance(Context context)
        {
            WelcomeTimeZoneFragment fragment = new WelcomeTimeZoneFragment();
            Bundle args = fragment.getArguments();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_timezone);
            fragment.setArguments(args);

            Location location = WidgetSettings.loadLocationPref(context, 0);
            fragment.setLongitude(location.getLongitudeAsDouble());
            fragment.setLongitudeLabel(location.getLabel());
            return fragment;
        }

        public double getLongitude() {
            return getArguments().getDouble(TimeZoneDialog.KEY_LONGITUDE);
        }
        public void setLongitude(double value) {
            getArguments().putDouble(TimeZoneDialog.KEY_LONGITUDE, value);
        }

        public String getLongitudeLabel() {
            return getArguments().getString(LocationConfigView.KEY_LOCATION_LABEL);
        }
        public void setLongitudeLabel( String value ) {
            getArguments().putString(LocationConfigView.KEY_LOCATION_LABEL, value);
        }

        public void toggleWarning(boolean visible)
        {
            if (timeZoneWarning != null) {
                timeZoneWarning.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            if (timeZoneSuggestButton != null) {
                timeZoneSuggestButton.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }

        protected TimeZoneDialog getTimeZoneDialog() {
            FragmentManager fragments = getChildFragmentManager();
            return fragments != null ? (TimeZoneDialog) fragments.findFragmentByTag("TimeZoneDialog") : null;
        }

        @Override
        public void initViews(Context context, View view)
        {
            super.initViews(context, view);

            TimeZoneDialog tzConfig = getTimeZoneDialog();
            if (tzConfig != null) {
                tzConfig.setTimeFormatMode(WidgetSettings.loadTimeFormatModePref(context, 0));
                tzConfig.setDialogListener(timeZoneDialogListener);
            }

            timeZoneWarning = (TextView) view.findViewById(R.id.warning_timezone);
            if (timeZoneWarning != null)
            {
                ImageSpan warningIcon = SuntimesUtils.createWarningSpan(context, context.getResources().getDimension(R.dimen.warningIcon_size));
                timeZoneWarning.setText(SuntimesUtils.createSpan(context, timeZoneWarning.getText().toString(), SuntimesUtils.SPANTAG_WARNING, warningIcon));
            }

            timeZoneSuggestButton = (Button) view.findViewById(R.id.button_suggest_timezone);
            if (timeZoneSuggestButton != null) {
                timeZoneSuggestButton.setOnClickListener(timeZoneSuggestButtonListener);
            }

            timeFormatSpinner = (Spinner) view.findViewById(R.id.appwidget_general_timeformatmode);
            if (timeFormatSpinner != null)
            {
                WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, 0);
                ArrayAdapter<WidgetSettings.TimeFormatMode> adapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline,
                        new WidgetSettings.TimeFormatMode[] {WidgetSettings.TimeFormatMode.MODE_SYSTEM, WidgetSettings.TimeFormatMode.MODE_12HR, WidgetSettings.TimeFormatMode.MODE_24HR});
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeFormatSpinner.setAdapter(adapter);
                timeFormatSpinner.setSelection(adapter.getPosition(timeFormat), false);
                timeFormatSpinner.setOnItemSelectedListener(onTimeFormatSelected);
            }
        }

        @Override
        public void updateViews(Context context)
        {
            Location location = WidgetSettings.loadLocationPref(context, 0);
            setLongitude(location.getLongitudeAsDouble());
            setLongitudeLabel(location.getLabel());

            TimeZoneDialog tzConfig = getTimeZoneDialog();
            if (tzConfig != null) {
                tzConfig.setLongitude(getLongitude());
                tzConfig.updatePreview(context);
            }
        }

        private TimeZoneDialog.TimeZoneDialogListener timeZoneDialogListener = new TimeZoneDialog.TimeZoneDialogListener()
        {
            @Override
            public void onSelectionChanged( TimeZone tz ) {
                toggleWarning(WidgetTimezones.isProbablyNotLocal(tz, getLongitude(), Calendar.getInstance(tz).getTime()));
            }
        };

        private AdapterView.OnItemSelectedListener onTimeFormatSelected = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Activity context = getActivity();
                TimeZoneDialog tzConfig = getTimeZoneDialog();
                if (tzConfig != null && context != null) {
                    tzConfig.setTimeFormatMode((WidgetSettings.TimeFormatMode) parent.getAdapter().getItem(position));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        private View.OnClickListener timeZoneSuggestButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                TimeZoneDialog tzConfig = getTimeZoneDialog();
                if (tzConfig != null)
                {
                    Calendar now = Calendar.getInstance();
                    double longitude = getLongitude();
                    String label = getLongitudeLabel();
                    Log.d("DEBUG", "longitude label: " + label);

                    boolean foundItem = false;
                    String tzID = WidgetSettings.PREF_DEF_TIMEZONE_CUSTOM;
                    WidgetTimezones.TimeZoneItemAdapter adapter = tzConfig.getTimeZoneItemAdapter();
                    WidgetTimezones.TimeZoneItem[] recommendations = null;
                    if (adapter != null)
                    {
                        if (label != null)
                        {
                            WidgetTimezones.TimeZoneItem[] items = adapter.values();
                            for (WidgetTimezones.TimeZoneItem item : items)
                            {
                                if (item.getID().contains(label) || item.getDisplayString().contains(label)) {
                                    tzID = item.getID();
                                    foundItem = true;
                                    break;
                                }
                            }
                        }

                        if (!foundItem) {
                            recommendations = adapter.findItems(longitude);
                        }
                    }

                    if (!foundItem)
                    {
                        tzID = WidgetSettings.PREF_DEF_TIMEZONE_CUSTOM;
                        TimeZone tz = WidgetTimezones.getTimeZone(tzID, longitude);
                        if (WidgetTimezones.isProbablyNotLocal(tz, longitude, now.getTime()))
                        {
                            if (recommendations != null && recommendations[0] != null) {
                                tzID = recommendations[0].getID();
                            }
                        }
                    }
                    tzConfig.setCustomTimeZone(tzID);
                }
            }
        };

        @Override
        public boolean saveSettings(Context context)
        {
            if (isAdded())
            {
                TimeZoneDialog tzConfig = getTimeZoneDialog();
                if (tzConfig != null) {
                    tzConfig.saveSettings(context);
                }

                WidgetSettings.TimeFormatMode timeFormat = (WidgetSettings.TimeFormatMode) timeFormatSpinner.getSelectedItem();
                WidgetSettings.saveTimeFormatModePref(context, 0, timeFormat);
                Log.d("DEBUG", "saveSettings: timezone");
                return true;
            }
            return false;
        }
    }

    /**
     * WelcomeAlarmsFragment
     */
    public static class WelcomeAlarmsFragment extends WelcomeFragment
    {
        public WelcomeAlarmsFragment() {}

        protected TextView batteryOptimizationText;

        public static WelcomeAlarmsFragment newInstance()
        {
            WelcomeAlarmsFragment fragment = new WelcomeAlarmsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_alarms);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void initViews(final Context context, View view)
        {
            CheckBox launcherIconCheck = (CheckBox) view.findViewById(R.id.check_alarms_showlauncher);
            if (launcherIconCheck != null)
            {
                launcherIconCheck.setChecked(AlarmSettings.loadPrefShowLauncher(context));
                launcherIconCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        AlarmSettings.savePrefShowLauncher(context, isChecked);
                    }
                });
            }

            batteryOptimizationText = (TextView) view.findViewById(R.id.text_optWhiteList);

            Button batteryOptimizationButton = (Button) view.findViewById(R.id.button_optWhiteList);
            if (batteryOptimizationButton != null)
            {
                batteryOptimizationButton.setVisibility((Build.VERSION.SDK_INT >= 23) ? View.VISIBLE : View.GONE);
                batteryOptimizationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SuntimesSettingsActivity.createBatteryOptimizationAlertDialog(context).show();
                    }
                });
            }
        }

        @Override
        public void updateViews(Context context)
        {
            if (batteryOptimizationText != null)
            {
                batteryOptimizationText.setVisibility((Build.VERSION.SDK_INT >= 23) ? View.VISIBLE : View.GONE);
                batteryOptimizationText.setText(AlarmSettings.batteryOptimizationMessage(context));
            }
        }

    }

    /**
     * WelcomeAppearanceFragment
     */
    public static class WelcomeAppearanceFragment extends WelcomeFragment
    {
        public WelcomeAppearanceFragment() {}

        public static WelcomeAppearanceFragment newInstance()
        {
            WelcomeAppearanceFragment fragment = new WelcomeAppearanceFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_appearance);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void initViews(Context context, View view)
        {
            super.initViews(context, view);

            Button darkThemeButton = (Button) view.findViewById(R.id.button_theme_dark);
            if (darkThemeButton != null) {
                darkThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_DARK));
            }

            Button lightThemeButton = (Button) view.findViewById(R.id.button_theme_light);
            if (lightThemeButton != null) {
                lightThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_LIGHT));
            }
        }

        private View.OnClickListener onThemeButtonClicked(final String themeID) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppSettings.setThemePref(getActivity(), themeID);
                    recreate(getActivity());
                }
            };
        }

        private static void recreate(Activity activity)
        {
            if (activity != null) {
                activity.finish();
                activity.overridePendingTransition(R.anim.transition_restart_in, R.anim.transition_restart_out);
                activity.startActivity(activity.getIntent());
            }
        }
    }


}