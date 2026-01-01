/**
    Copyright (C) 2022-2023 Forrest Guice
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.dialog.DialogBase;
import com.forrestguice.suntimeswidget.getfix.LocationConfigDialog;
import com.forrestguice.suntimeswidget.getfix.LocationConfigView;
import com.forrestguice.suntimeswidget.settings.SettingsActivityInterface;
import com.forrestguice.suntimeswidget.settings.fragments.AlarmPrefsFragment;
import com.forrestguice.suntimeswidget.views.Toast;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemImportTask;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.view.ViewPager;

import java.util.Calendar;
import java.util.TimeZone;

public class WelcomeActivity extends AppCompatActivity
{
    public static final String EXTRA_PAGE = "page";

    private ViewPager pager;
    private WelcomeFragmentAdapter pagerAdapter;
    private Button nextButton, prevButton;
    private LinearLayout indicatorLayout;
    private AppSettings.LocaleInfo localeInfo;

    private static final SuntimesUtils utils = new SuntimesUtils();

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase, localeInfo = new AppSettings.LocaleInfo());
        super.attachBaseContext(context);
        WidgetSettings.initDefaults(context);
        SuntimesUtils.initDisplayStrings(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setResult(RESULT_CANCELED, getResultData());
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_welcome);

        Intent intent = getIntent();
        int page = 0;
        if (intent.hasExtra(EXTRA_PAGE)) {
            page = intent.getIntExtra(EXTRA_PAGE, 0);
            intent.removeExtra(EXTRA_PAGE);
        }

        pagerAdapter = new WelcomeFragmentAdapter(this, this);
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

        if (page != 0)
        {
            final int selectedPage = page;
            pager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pager.setCurrentItem(selectedPage, true);
                }
            }, getResources().getInteger(R.integer.anim_welcome_pause_duration));
        }
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

    private final View.OnClickListener onPrevPressed = new View.OnClickListener() {
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

    private final View.OnClickListener onNextPressed = new View.OnClickListener()
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

    private final ViewPager.OnPageChangeListener pagerChangeListener = new ViewPager.OnPageChangeListener()
    {
        private int previousPosition = 0;

        @Override
        public void onPageSelected(int position)
        {
            //Log.d("DEBUG", "onPageSelected: " + position);
            if (saveSettings(WelcomeActivity.this, previousPosition))
            {
                updateViews(WelcomeActivity.this, position);
                setIndicator(position);
                prevButton.setText(getString((position != 0) ? R.string.welcome_action_prev : R.string.welcome_action_skip));
                nextButton.setText(getString((position != pagerAdapter.getCount()-1) ? R.string.welcome_action_next : R.string.welcome_action_done));
                previousPosition = position;

            } else {
                //Log.d("DEBUG", "onPageSelected: reverting to " + previousPosition);
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
            indicators[i].setTextSize(getResources().getDimensionPixelSize(R.dimen.welcomeIndicator_size));
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
        setResult(RESULT_CANCELED, getResultData());
        finish();
    }

    private void onDone()
    {
        saveSettings(this, pager.getCurrentItem());
        AppSettings.setFirstLaunch(WelcomeActivity.this, false);
        setResult(RESULT_OK, getResultData());
        finish();
    }

    private void saveSettings()
    {
        for (int i=0; i<pagerAdapter.getCount(); i++) {
            saveSettings(this, i);
        }
    }
    private boolean saveSettings(AppCompatActivity activity, int position)
    {
        WelcomeFragment page = getPageFragment(activity, pager, position);
        if (page != null) {
            if (page.validateInput(WelcomeActivity.this)) {
                return page.saveSettings(WelcomeActivity.this);
            } else return false;
        }
        return false;
    }
    private boolean validateInput(AppCompatActivity activity, int position)
    {
        WelcomeFragment page = getPageFragment(activity, pager, position);
        if (page != null) {
            return page.validateInput(WelcomeActivity.this);
        }
        return true;
    }
    private void updateViews(AppCompatActivity activity, int position)
    {
        WelcomeFragment page = getPageFragment(activity, pager, position);
        if (page != null) {
            page.updateViews(WelcomeActivity.this);
        }
    }

    public static WelcomeFragment getPageFragment(AppCompatActivity activity, ViewPager pager, int position) {
        // https://stackoverflow.com/questions/54279509/how-to-get-elements-of-fragments-created-by-viewpager-in-mainactivity/54280113#54280113
        return (WelcomeFragment) activity.getSupportFragmentManager().findFragmentByTag("android:switcher:" + pager.getId() + ":" + position);
    }

    public void showAbout( View v )
    {
        startActivity(new Intent(this, AboutActivity.class));
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    public void setNeedsRecreateFlag() {
        //Log.d("DEBUG", "setNeedsRecreateFlag");
        getIntent().putExtra(SettingsActivityInterface.RECREATE_ACTIVITY, true);
        setResult(RESULT_OK, getResultData());
    }

    public Intent getResultData() {
        boolean value = getIntent().getBooleanExtra(SettingsActivityInterface.RECREATE_ACTIVITY, false);
        //Log.d("DEBUG", "getResultData: needsRecreate? " + value);
        return new Intent().putExtra(SettingsActivityInterface.RECREATE_ACTIVITY, value);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * WelcomeFragmentPage
     */
    public abstract static class WelcomeFragmentPage {
        public abstract WelcomeFragment newInstance();
    }

    /**
     * WelcomeFragment
     */
    public static class WelcomeFragment extends DialogBase
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

                textViews = new int[] { R.id.link0, R.id.link1, R.id.link2, R.id.link3, R.id.link4 };
                for (int resID : textViews) {
                    TextView text = (TextView) view.findViewById(resID);
                    if (text != null) {
                        text.setText(SuntimesUtils.fromHtml(AboutActivity.anchor(text.getText().toString())));
                        text.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }

                final TextView donateLink = (TextView) view.findViewById(R.id.link4);
                if (donateLink != null) {
                    donateLink.setVisibility(View.GONE);
                    donateLink.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_donate_url, context.getString(R.string.app_name), context.getString(R.string.help_donate_url))));
                }

                CheckBox donateCheck = (CheckBox) view.findViewById(R.id.check_donate);
                if (donateCheck != null)
                {
                    donateCheck.setChecked(false);
                    donateCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                    {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (donateLink != null) {
                                donateLink.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                            }
                        }
                    });
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
            return getArgs().getInt(ARG_LAYOUT_RESID);
        }

        public int getPreferredIndex() {
            return 0;
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
        public static final int IMPORT_REQUEST = 1100;

        private Button button_addPlaces, button_importPlaces, button_lookupLocation;
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

            button_importPlaces = (Button) view.findViewById(R.id.button_import_places);
            if (button_importPlaces != null) {
                button_importPlaces.setOnClickListener(onImportPlacesClicked);
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

        private final View.OnClickListener onLookupLocationClicked = new View.OnClickListener()
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

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode)
            {
                case IMPORT_REQUEST:
                    if (resultCode == Activity.RESULT_OK)
                    {
                        Uri uri = (data != null ? data.getData() : null);
                        if (uri != null) {
                            importPlaces(getActivity(), uri);
                        }
                    } else {
                        reloadLocationList();
                    }
                    break;
            }
        }

        private final View.OnClickListener onImportPlacesClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(BuildPlacesTask.buildPlacesOpenFileIntent(), IMPORT_REQUEST);
            }
        };
        protected void importPlaces(Context context, @NonNull Uri uri)
        {
            BuildPlacesTask task = new BuildPlacesTask(context);
            task.setTaskListener(importPlacesListener);
            task.execute(false, uri);
        }
        private final BuildPlacesTask.TaskListener importPlacesListener = new BuildPlacesTask.TaskListener()
        {
            @Override
            public void onStarted() {
                setRetainInstance(true);
                toggleControlsEnabled(false);
                toggleControlsVisible(false);
                setLocationViewMode(LocationConfigView.LocationViewMode.MODE_DISABLED);
                toggleProgress(true);
            }

            @Override
            public void onFinished(Integer result)
            {
                setRetainInstance(false);
                toggleProgress(false);
                toggleControlsEnabled(true);
                toggleControlsVisible(true);
                if (result > 0)
                {
                    Context context = getActivity();
                    if (context != null && button_importPlaces != null) {
                        button_importPlaces.setText(context.getString(R.string.locationbuild_toast_success, result.toString()));
                        button_importPlaces.setEnabled(false);
                    }
                }
                setLocationViewMode(LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
                reloadLocationList();
            }
        };

        private final View.OnClickListener onAddPlacesClicked = new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                BuildPlacesTask.promptAddWorldPlaces(getActivity(), buildPlacesListener);
            }
        };
        private final BuildPlacesTask.TaskListener buildPlacesListener = new BuildPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                setRetainInstance(true);
                toggleControlsEnabled(false);
                toggleControlsVisible(false);
                setLocationViewMode(LocationConfigView.LocationViewMode.MODE_DISABLED);
                toggleProgress(true);
            }

            @Override
            public void onFinished(Integer result)
            {
                setRetainInstance(false);
                toggleProgress(false);
                toggleControlsEnabled(true);
                toggleControlsVisible(true);

                if (result > 0)
                {
                    Context context = getActivity();
                    if (context != null && button_addPlaces != null) {
                        button_addPlaces.setText(context.getString(R.string.locationbuild_toast_success, result.toString()));
                        button_addPlaces.setEnabled(false);
                    }
                }

                setLocationViewMode(LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
                reloadLocationList();
            }
        };

        protected void toggleControlsEnabled(boolean value)
        {
            if (button_addPlaces != null) {
                button_addPlaces.setEnabled(value);
            }
            if (button_importPlaces != null) {
                button_importPlaces.setEnabled(value);
            }
            if (button_lookupLocation != null) {
                button_lookupLocation.setEnabled(value);
            }
        }

        protected void toggleControlsVisible(boolean visible)
        {
            if (button_addPlaces != null) {
                button_addPlaces.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
            if (button_importPlaces != null) {
                button_importPlaces.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
            if (button_lookupLocation != null) {
                button_lookupLocation.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        protected void toggleProgress(boolean visible) {
            if (progress_addPlaces != null) {
                progress_addPlaces.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        @Nullable
        private LocationConfigDialog getLocationConfigDialog()
        {
            if (isAdded()) {
                if (getChildFragmentManager() != null) {
                    return (LocationConfigDialog) getChildFragmentManager().findFragmentByTag("LocationConfigDialog");
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
                //Log.d("DEBUG", "saveSettings: location " + saved);
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
        private TextView timeZoneWarning, timeZoneWarningNote;
        private Button timeZoneSuggestButton;

        public WelcomeTimeZoneFragment()
        {
            setArguments(new Bundle());
            setLongitude(Double.parseDouble(WidgetSettings.PREF_DEF_LOCATION_LONGITUDE));
        }

        public static WelcomeTimeZoneFragment newInstance(Context context)
        {
            WelcomeTimeZoneFragment fragment = new WelcomeTimeZoneFragment();
            Bundle args = fragment.getArgs();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_timezone);
            fragment.setArguments(args);

            Location location = WidgetSettings.loadLocationPref(context, 0);
            fragment.setLongitude(location.getLongitudeAsDouble());
            fragment.setLongitudeLabel(location.getLabel());
            return fragment;
        }

        public double getLongitude() {
            return getArgs().getDouble(TimeZoneDialog.KEY_LONGITUDE);
        }
        public void setLongitude(double value) {
            getArgs().putDouble(TimeZoneDialog.KEY_LONGITUDE, value);
        }

        public String getLongitudeLabel() {
            return getArgs().getString(LocationConfigView.KEY_LOCATION_LABEL);
        }
        public void setLongitudeLabel( String value ) {
            getArgs().putString(LocationConfigView.KEY_LOCATION_LABEL, value);
        }

        public void toggleWarning(boolean visible)
        {
            if (timeZoneWarning != null) {
                timeZoneWarning.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            //if (timeZoneWarningNote != null) {
            //    timeZoneWarningNote.setVisibility(visible ? View.VISIBLE : View.GONE);
            //}
            if (timeZoneSuggestButton != null) {
                timeZoneSuggestButton.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }

        protected TimeZoneDialog getTimeZoneDialog() {
            return ((getChildFragmentManager() != null) ? (TimeZoneDialog) getChildFragmentManager().findFragmentByTag("TimeZoneDialog") : null);
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
            timeZoneWarningNote = (TextView) view.findViewById(R.id.warning_timezone_note);

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
                final TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, 0);
                final ArrayAdapter<TimeFormatMode> adapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline,
                        new TimeFormatMode[] {TimeFormatMode.MODE_SYSTEM, TimeFormatMode.MODE_12HR, TimeFormatMode.MODE_24HR});
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeFormatSpinner.setAdapter(adapter);
                timeFormatSpinner.setOnItemSelectedListener(onTimeFormatSelected);
                timeFormatSpinner.post(new Runnable() {
                    @Override
                    public void run() {
                        timeFormatSpinner.setSelection(adapter.getPosition(timeFormat), false);
                    }
                });
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
                tzConfig.setLongitude(getLongitudeLabel(), getLongitude());
                tzConfig.updatePreview(context);
            }
        }

        protected void updateWarningNote(Context context, TimeZone tz)
        {
            if (timeZoneWarningNote != null)
            {
                long zoneOffsetMillis = tz.getOffset(System.currentTimeMillis());
                long lonOffsetMillis = Math.round(getLongitude() * (24 * 60 * 60 * 1000) / 360d);
                long offset = zoneOffsetMillis - lonOffsetMillis;
                String offsetDisplay = (offset < 0 ? "-" : "+") + utils.timeDeltaLongDisplayString(offset);

                TypedArray typedArray = context.obtainStyledAttributes(new int[] { R.attr.tagColor_warning, R.attr.text_primaryColor });
                int warningColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.warningTag_dark));
                int normalColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_primary_dark));
                typedArray.recycle();

                int highlightColor = normalColor;
                if (Math.abs(offset / 1000 / 60 / 60) >= WidgetTimezones.WARNING_TOLERANCE_HOURS) {
                    highlightColor = warningColor;
                }

                String location = getLongitudeLabel();
                String note = context.getString(R.string.timezoneWarningNote, tz.getID(), offsetDisplay, location);
                SpannableString noteDisplay = SuntimesUtils.createBoldColorSpan(null, note, offsetDisplay, highlightColor);
                noteDisplay = SuntimesUtils.createBoldColorSpan(noteDisplay, note, location, normalColor);
                timeZoneWarningNote.setText(noteDisplay);
            }
        }

        private final TimeZoneDialog.TimeZoneDialogListener timeZoneDialogListener = new TimeZoneDialog.TimeZoneDialogListener()
        {
            @Override
            public void onSelectionChanged( TimeZone tz ) {
                toggleWarning(WidgetTimezones.isProbablyNotLocal(tz, getLongitude(), Calendar.getInstance(tz).getTime()));
                updateWarningNote(getContext(), tz);
            }
        };

        private final AdapterView.OnItemSelectedListener onTimeFormatSelected = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Activity context = getActivity();
                TimeZoneDialog tzConfig = getTimeZoneDialog();
                if (tzConfig != null && context != null) {
                    tzConfig.setTimeFormatMode((TimeFormatMode) parent.getAdapter().getItem(position));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        private final View.OnClickListener timeZoneSuggestButtonListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TimeZoneDialog tzConfig = getTimeZoneDialog();
                if (tzConfig != null) {
                    tzConfig.setCustomTimeZone(tzConfig.timeZoneRecommendation(getLongitudeLabel(), getLongitude()));
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

                TimeFormatMode timeFormat = (TimeFormatMode) timeFormatSpinner.getSelectedItem();
                WidgetSettings.saveTimeFormatModePref(context, 0, timeFormat);
                //Log.d("DEBUG", "saveSettings: timezone");
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
        public static final int IMPORT_REQUEST = 1200;

        public WelcomeAlarmsFragment() {}

        protected TextView autostartText;
        protected TextView batteryOptimizationText;
        protected Button importAlarmsButton;
        private ProgressBar progress_importAlarms;

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

            CheckBox reminderNotificationCheck = (CheckBox) view.findViewById(R.id.check_alarms_showreminders);
            if (reminderNotificationCheck != null)
            {
                long reminderMillis = AlarmSettings.loadPrefAlarmUpcoming(context);
                reminderNotificationCheck.setChecked(reminderMillis > 0);
                reminderNotificationCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        long reminderMillis = (isChecked ? AlarmSettings.PREF_DEF_ALARM_UPCOMING : 0);
                        AlarmSettings.savePrefAlarmUpcomingReminder(context, reminderMillis);
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
                        AlarmPrefsFragment.createBatteryOptimizationAlertDialog(context).show();
                    }
                });
            }

            View layout_autoStart = view.findViewById(R.id.layout_autostart);
            if (layout_autoStart != null) {
                layout_autoStart.setVisibility( AlarmSettings.hasAutostartSettings(context) ? View.VISIBLE : View.GONE );
            }

            autostartText = (TextView) view.findViewById(R.id.text_autostart);
            Button autostartButton = (Button) view.findViewById(R.id.button_autostart);
            if (autostartButton != null) {
                autostartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlarmSettings.openAutostartSettings(context);
                    }
                });
            }

            progress_importAlarms = (ProgressBar) view.findViewById(R.id.progress_import_alarms);
            importAlarmsButton = (Button) view.findViewById(R.id.button_import_alarms);
            if (importAlarmsButton != null) {
                importAlarmsButton.setOnClickListener(onImportAlarmsClicked);
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

            if (autostartText != null) {
                autostartText.setText(AlarmSettings.hasAutostartSettings(context) ? AlarmSettings.autostartMessage(context) : "");
            }
        }

        protected void toggleControlsEnabled(boolean value)
        {
            if (importAlarmsButton != null) {
                importAlarmsButton.setEnabled(value);
            }
        }

        protected void toggleControlsVisible(boolean visible)
        {
            if (importAlarmsButton != null) {
                importAlarmsButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        protected void toggleProgress(boolean visible) {
            if (progress_importAlarms != null) {
                progress_importAlarms.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode)
            {
                case IMPORT_REQUEST:
                    if (resultCode == Activity.RESULT_OK)
                    {
                        Uri uri = (data != null ? data.getData() : null);
                        if (uri != null) {
                            importAlarms(getActivity(), uri);
                        }
                    }
                    break;
            }
        }

        protected AlarmClockItemImportTask importTask = null;
        private final View.OnClickListener onImportAlarmsClicked = new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (importTask != null) {
                    Log.e("ImportAlarms", "Already busy importing/exporting! ignoring request");
                }
                AlarmListDialog.ImportFragment fragment = new AlarmListDialog.ImportFragment() {
                    @Override
                    public void startActivityForResult(Intent intent, int request) {
                        WelcomeAlarmsFragment.this.startActivityForResult(intent, request);
                    }
                };
                AlarmListDialog.importAlarms(fragment, getContext(), getLayoutInflater(), IMPORT_REQUEST);
            }
        };

        protected void importAlarms(final Context context, @NonNull Uri uri)
        {
            if (importTask != null) {
                Log.e("ImportAlarms", "Already busy importing/exporting! ignoring request");
            }
            importTask = new AlarmClockItemImportTask(context);
            importTask.setTaskListener(importAlarmsListener);
            importTask.execute(uri);
        }

        private final AlarmClockItemImportTask.TaskListener importAlarmsListener =  new AlarmClockItemImportTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                setRetainInstance(true);
                toggleProgress(true);
                toggleControlsEnabled(false);
                toggleControlsVisible(false);
            }

            @Override
            public void onFinished(AlarmClockItemImportTask.TaskResult result)
            {
                if (result.getResult())
                {
                    final Context context = getContext();
                    final AlarmClockItem[] items = result.getItems();
                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, true, true);
                    task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
                    {
                        @Override
                        public void onFinished(Boolean result, AlarmClockItem[] items)
                        {
                            setRetainInstance(false);
                            importTask = null;
                            toggleProgress(false);
                            toggleControlsVisible(true);

                            if (result)
                            {
                                String plural = getResources().getQuantityString(R.plurals.alarmPlural, items.length, items.length);
                                importAlarmsButton.setText(getString(R.string.importalarms_toast_success, plural));

                                for (AlarmClockItem item : items) {
                                    if (item.enabled) {
                                        context.sendBroadcast( AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri()) );
                                    }
                                }
                            }
                        }
                    });
                    task.execute(items);

                } else {
                    setRetainInstance(false);
                    importTask = null;
                    toggleProgress(false);
                    toggleControlsEnabled(true);
                    if (isAdded())
                    {
                        Uri uri = result.getUri();   // import failed
                        String path = ((uri != null) ? uri.toString() : "<path>");
                        String failureMessage = getString(R.string.msg_import_failure, path);
                        Toast.makeText(getActivity(), failureMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

    }

    /**
     * WelcomeAppearanceFragment
     */
    public static class WelcomeAppearanceFragment extends WelcomeFragment
    {
        protected Spinner spinner;
        protected ToggleButton[] buttons = null;
        protected TextView previewDate;

        public WelcomeAppearanceFragment() {}

        public static WelcomeAppearanceFragment newInstance()
        {
            WelcomeAppearanceFragment fragment = new WelcomeAppearanceFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_appearance);
            fragment.setArguments(args);
            return fragment;
        }

        protected void setChecked(@Nullable RadioButton button, boolean value) {
            if (button != null) {
                button.setChecked(value);
            }
        }

        protected void setCheckedChangeListener(@Nullable RadioButton button, CompoundButton.OnCheckedChangeListener listener) {
            if (button != null) {
                button.setOnCheckedChangeListener(listener);
            }
        }

        protected String themeID = null, themeID1 = null, darkThemeID = null, lightThemeID = null;
        protected AppSettings.TextSize textSize;

        @Override
        public void initViews(Context context, View view)
        {
            super.initViews(context, view);

            RadioButton smallText = (RadioButton) view.findViewById(R.id.radio_text_small);
            RadioButton normalText = (RadioButton) view.findViewById(R.id.radio_text_normal);
            RadioButton largeText = (RadioButton) view.findViewById(R.id.radio_text_large);
            RadioButton xlargeText = (RadioButton) view.findViewById(R.id.radio_text_xlarge);

            textSize = AppSettings.TextSize.valueOf(AppSettings.loadTextSizePref(context));
            switch (textSize)
            {
                case SMALL: setChecked(smallText, true); break;
                case LARGE: setChecked(largeText, true); break;
                case XLARGE: setChecked(xlargeText, true); break;
                case NORMAL: default: setChecked(normalText, true); break;
            }
            setCheckedChangeListener(smallText, onTextSizeChecked(context, AppSettings.TextSize.SMALL));
            setCheckedChangeListener(normalText, onTextSizeChecked(context, AppSettings.TextSize.NORMAL));
            setCheckedChangeListener(largeText, onTextSizeChecked(context, AppSettings.TextSize.LARGE));
            setCheckedChangeListener(xlargeText, onTextSizeChecked(context, AppSettings.TextSize.XLARGE));

            final AppSettings.AppThemeInfo themeInfo = AppSettings.loadThemeInfo(context);
            themeID = themeInfo.getThemeName();
            themeID1 = AppSettings.getThemeOverride(context, themeInfo);
            final AppSettings.AppThemeInfo themeInfo1 = AppSettings.loadThemeInfo(themeID1);
            darkThemeID = AppSettings.loadThemeDarkPref(context);
            lightThemeID = AppSettings.loadThemeLightPref(context);
            AppSettings.AppThemeInfo darkThemeInfo = AppSettings.loadThemeInfo(darkThemeID);

            previewDate = (TextView) view.findViewById(R.id.text_date);
            updatePreview(context, themeInfo.getDisplayString(context));

            spinner = (Spinner) view.findViewById(R.id.spin_theme);
            if (spinner != null)
            {
                final ArrayAdapter<AppSettings.AppThemeInfo> spinnerAdapter = new AppThemeInfoAdapter(context, R.layout.layout_listitem_welcome);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                int initialPosition = spinnerAdapter.getPosition(themeID1 != null ? themeInfo1 : themeInfo);
                spinner.setSelection(initialPosition, false);
                spinner.setOnItemSelectedListener(onThemeItemSelected(initialPosition));
            }

            ToggleButton systemThemeButton = (ToggleButton) view.findViewById(R.id.button_theme_system);
            ToggleButton systemTheme1Button = (ToggleButton) view.findViewById(R.id.button_theme_system1);
            ToggleButton darkThemeButton = (ToggleButton) view.findViewById(R.id.button_theme_dark);
            ToggleButton lightThemeButton = (ToggleButton) view.findViewById(R.id.button_theme_light);
            buttons = new ToggleButton[] {systemThemeButton, systemTheme1Button, darkThemeButton, lightThemeButton};
            for (ToggleButton button : buttons) {
                if (button != null) {
                    button.setChecked(false);
                }
            }

            if (systemThemeButton != null) {
                if (setChecked(systemThemeButton, AppSettings.THEME_SYSTEM.equals(themeID) && AppSettings.THEME_DEFAULT.equals(darkThemeID))) {
                    updatePreview(context, themeInfo.getDisplayString(context));
                }
                systemThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_SYSTEM, null, null));
            }

            if (systemTheme1Button != null) {
                if (setChecked(systemTheme1Button, AppSettings.THEME_SYSTEM.equals(themeID) && AppSettings.THEME_SYSTEM1.equals(darkThemeID))) {
                    updatePreview(context, darkThemeInfo.getDisplayString(context));
                }
                systemTheme1Button.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_SYSTEM, AppSettings.THEME_SYSTEM1, AppSettings.THEME_SYSTEM1));
            }

            if (darkThemeButton != null) {
                if (setChecked(darkThemeButton, AppSettings.THEME_DARK.equals(themeID))) {
                    updatePreview(context, themeInfo.getDisplayString(context));
                }
                darkThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_DARK, null, null));
            }

            if (lightThemeButton != null) {
                if (setChecked(lightThemeButton, AppSettings.THEME_LIGHT.equals(themeID))) {
                    updatePreview(context, themeInfo.getDisplayString(context));
                }
                lightThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_LIGHT, null, null));
            }
        }

        public static class AppThemeInfoAdapter extends ArrayAdapter<AppSettings.AppThemeInfo>
        {
            protected int layout;

            public AppThemeInfoAdapter(@NonNull Context context, int resource)
            {
                super(context, resource);
                layout = resource;
                for (AppSettings.AppThemeInfo info : AppSettings.appThemeInfo()) {
                    add(info);
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                return createView(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
            }
            @NonNull @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                return createView(position, convertView, parent, layout);
            }

            @SuppressLint("ResourceType")
            private View createView(int position, View convertView, ViewGroup parent, int layoutResID)
            {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(layoutResID, parent, false);
                }

                AppSettings.AppThemeInfo item = getItem(position);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(item != null ? item.getDisplayString(getContext()) : "");
                return view;
            }
        }

        private AdapterView.OnItemSelectedListener onThemeItemSelected(final int initialPosition)
        {
            return new AdapterView.OnItemSelectedListener()
            {
                private int currentPosition = initialPosition;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    if (position == currentPosition) {
                        //Log.d("DEBUG", "spinner position is already at " + position + ", skipping onItemSelected...");
                        return;
                    }
                    currentPosition = position;
                    onThemeItemSelected(parent, view, position, id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
        }
        private void onThemeItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            AppSettings.AppThemeInfo themeInfo = (AppSettings.AppThemeInfo) parent.getAdapter().getItem(position);
            switch (themeInfo.getThemeName())
            {
                case AppSettings.THEME_SYSTEM1:
                    onThemeButtonClicked(AppSettings.THEME_SYSTEM, AppSettings.THEME_SYSTEM1, AppSettings.THEME_SYSTEM1).onClick(view);
                    break;

                case AppSettings.THEME_DARK1:
                    onThemeButtonClicked(AppSettings.THEME_DARK, AppSettings.THEME_LIGHT1, AppSettings.THEME_DARK1).onClick(view);
                    break;

                case AppSettings.THEME_LIGHT1:
                    onThemeButtonClicked(AppSettings.THEME_LIGHT, AppSettings.THEME_LIGHT1, AppSettings.THEME_DARK1).onClick(view);
                    break;

                case AppSettings.THEME_DARK:
                case AppSettings.THEME_LIGHT:
                case AppSettings.THEME_SYSTEM:
                default:
                    onThemeButtonClicked(themeInfo.getThemeName(), null, null).onClick(view);
                    break;
            }
        }

        protected void updatePreview(Context context, String themeDisplay)
        {
            if (previewDate != null) {
                previewDate.setText(themeDisplay.replace(" ", "\n"));
            }
        }

        protected boolean setChecked(ToggleButton button, boolean value)
        {
            if (value) {
                for (ToggleButton b : buttons) {
                    if (b != null) {
                        b.setChecked(b == button);
                    }
                }
                return true;
            } else return false;
        }

        private CompoundButton.OnCheckedChangeListener onTextSizeChecked(final Context context, final AppSettings.TextSize textSize)
        {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        WelcomeAppearanceFragment.this.textSize = textSize;
                        AppSettings.saveTextSizePref(context, textSize);
                        recreate(getActivity());
                    }
                }
            };
        }

        private View.OnClickListener onThemeButtonClicked(final String themeID, final String lightThemeID, final String darkThemeID) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Activity activity = getActivity();
                    WelcomeAppearanceFragment.this.themeID = themeID;
                    WelcomeAppearanceFragment.this.lightThemeID = lightThemeID;
                    WelcomeAppearanceFragment.this.darkThemeID = darkThemeID;
                    AppSettings.saveThemeLightPref(activity, lightThemeID);
                    AppSettings.saveThemeDarkPref(activity, darkThemeID);
                    AppSettings.setThemePref(activity, themeID);
                    AppSettings.setTheme(activity, AppSettings.loadThemePref(activity));
                    recreate(getActivity());
                }
            };
        }

        @Override
        public int getPreferredIndex() {
            return 1;
        }

        private void recreate(Activity activity)
        {
            if (activity != null)
            {
                if (activity instanceof  WelcomeActivity) {
                    WelcomeActivity activity1 = (WelcomeActivity)activity;
                    activity1.setNeedsRecreateFlag();
                }
                activity.finish();
                activity.overridePendingTransition(R.anim.transition_restart_in, R.anim.transition_restart_out);
                activity.startActivity(activity.getIntent()
                        .putExtra(EXTRA_PAGE, getPreferredIndex()));
            }
        }

        @Override
        public boolean saveSettings(Context context)
        {
            AppSettings.saveThemeLightPref(context, lightThemeID);
            AppSettings.saveThemeDarkPref(context, darkThemeID);
            AppSettings.saveTextSizePref(context, textSize);
            AppSettings.setThemePref(context, themeID);
            return true;
        }
    }

    /**
     * WelcomeUserInterfaceFragment
     */
    public static class WelcomeUserInterfaceFragment extends WelcomeFragment
    {
        public WelcomeUserInterfaceFragment() {}

        public static WelcomeUserInterfaceFragment newInstance()
        {
            WelcomeUserInterfaceFragment fragment = new WelcomeUserInterfaceFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_ui);
            fragment.setArguments(args);
            return fragment;
        }

        protected CheckBox check_moon;
        protected CheckBox check_astro, check_nautical, check_civil, check_noon, check_midnight, check_blue, check_gold;
        protected CheckBox check_solstice, check_crossquarter;

        @Override
        public void initViews(Context context, View view)
        {
            super.initViews(context, view);
            check_astro = (CheckBox) view.findViewById(R.id.check_show_astro);
            check_nautical = (CheckBox) view.findViewById(R.id.check_show_nautical);
            check_civil = (CheckBox) view.findViewById(R.id.check_show_civil);
            check_noon = (CheckBox) view.findViewById(R.id.check_show_noon);
            check_midnight = (CheckBox) view.findViewById(R.id.check_show_midnight);
            check_gold = (CheckBox) view.findViewById(R.id.check_show_gold);
            check_blue = (CheckBox) view.findViewById(R.id.check_show_blue);
            check_crossquarter = (CheckBox) view.findViewById(R.id.check_show_crossquarter);
            check_moon = (CheckBox) view.findViewById(R.id.check_show_moon);

            check_solstice = (CheckBox) view.findViewById(R.id.check_show_solstice);
            if (check_solstice != null) {
                check_solstice.setOnCheckedChangeListener(onCheckedChanged_showSolstice);
            }

            Button button_defaults = (Button) view.findViewById(R.id.button_defaults);
            if (button_defaults != null) {
                button_defaults.setOnClickListener(onClick_restoreDefaults);
            }

            loadSettings(context);
        }

        private final View.OnClickListener onClick_restoreDefaults = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDefaults(getActivity());
            }
        };

        private final CompoundButton.OnCheckedChangeListener onCheckedChanged_showSolstice = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (check_crossquarter != null) {
                    check_crossquarter.setEnabled(isChecked);
                }
            }
        };

        protected void loadDefaults(Context context)
        {
            boolean[] fields = AppSettings.loadShowFields(AppSettings.PREF_DEF_UI_SHOWFIELDS);
            if (check_astro != null) {
                check_astro.setChecked(fields[AppSettings.FIELD_ASTRO]);
            }
            if (check_nautical != null) {
                check_nautical.setChecked(fields[AppSettings.FIELD_NAUTICAL]);
            }
            if (check_civil != null) {
                check_civil.setChecked(fields[AppSettings.FIELD_CIVIL]);
            }
            if (check_noon != null) {
                check_noon.setChecked(fields[AppSettings.FIELD_NOON]);
            }
            if (check_midnight != null) {
                check_midnight.setChecked(fields[AppSettings.FIELD_MIDNIGHT]);
            }
            if (check_gold != null) {
                check_gold.setChecked(fields[AppSettings.FIELD_GOLD]);
            }
            if (check_blue != null) {
                check_blue.setChecked(fields[AppSettings.FIELD_BLUE]);
            }
            if (check_solstice != null) {
                check_solstice.setChecked(AppSettings.PREF_DEF_UI_SHOWEQUINOX);
            }
            if (check_crossquarter != null) {
                check_crossquarter.setChecked(AppSettings.PREF_DEF_UI_SHOWCROSSQUARTER);
            }
            if (check_moon != null) {
                check_moon.setChecked(AppSettings.PREF_DEF_UI_SHOWMOON);
            }
        }

        protected void loadSettings(Context context)
        {
            boolean[] fields = AppSettings.loadShowFieldsPref(context);
            if (check_astro != null) {
                check_astro.setChecked(fields[AppSettings.FIELD_ASTRO]);
            }
            if (check_nautical != null) {
                check_nautical.setChecked(fields[AppSettings.FIELD_NAUTICAL]);
            }
            if (check_civil != null) {
                check_civil.setChecked(fields[AppSettings.FIELD_CIVIL]);
            }
            if (check_noon != null) {
                check_noon.setChecked(fields[AppSettings.FIELD_NOON]);
            }
            if (check_midnight != null) {
                check_midnight.setChecked(fields[AppSettings.FIELD_MIDNIGHT]);
            }
            if (check_gold != null) {
                check_gold.setChecked(fields[AppSettings.FIELD_GOLD]);
            }
            if (check_blue != null) {
                check_blue.setChecked(fields[AppSettings.FIELD_BLUE]);
            }
            if (check_solstice != null) {
                check_solstice.setChecked(AppSettings.loadShowEquinoxPref(context));
            }
            if (check_crossquarter != null) {
                check_crossquarter.setChecked(AppSettings.loadShowCrossQuarterPref(context));
            }
            if (check_moon != null) {
                check_moon.setChecked(AppSettings.loadShowMoonPref(context));
            }
        }

        @Override
        public boolean saveSettings(Context context)
        {
            if (check_astro != null) {
                AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_ASTRO, check_astro.isChecked());
            }
            if (check_nautical != null) {
                AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_NAUTICAL, check_nautical.isChecked());
            }
            if (check_civil != null) {
                AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_CIVIL, check_civil.isChecked());
            }
            if (check_noon != null) {
                AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_NOON, check_noon.isChecked());
            }
            if (check_midnight != null) {
                AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_MIDNIGHT, check_midnight.isChecked());
            }
            if (check_gold != null) {
                AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_GOLD, check_gold.isChecked());
            }
            if (check_blue != null) {
                AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_BLUE, check_blue.isChecked());
            }
            if (check_solstice != null) {
                AppSettings.saveShowEquinoxPref(context, check_solstice.isChecked());
            }
            if (check_crossquarter != null) {
                AppSettings.saveShowCrossQuarterPref(context, check_crossquarter.isChecked());
            }
            if (check_moon != null) {
                AppSettings.saveShowMoonPref(context, check_moon.isChecked());
            }
            WidgetSettings.saveLengthUnitsPref(context, 0, WidgetSettings.loadLengthUnitsPref(context, 0));
            return true;
        }
    }

}