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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

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
            saveSettings(getSupportFragmentManager(), previousPosition);
            setIndicator(position);
            prevButton.setText(getString((position != 0) ? R.string.welcome_action_prev : R.string.welcome_action_skip));
            nextButton.setText(getString((position != pagerAdapter.getCount()-1) ? R.string.welcome_action_next : R.string.welcome_action_done));
            previousPosition = position;
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
        saveSettings(getSupportFragmentManager(), pager.getCurrentItem());
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
    private void saveSettings(FragmentManager fragments, int position)
    {
        // https://stackoverflow.com/questions/54279509/how-to-get-elements-of-fragments-created-by-viewpager-in-mainactivity/54280113#54280113
        WelcomeFragment page = (WelcomeFragment) fragments.findFragmentByTag("android:switcher:" + pager.getId() + ":" + position);
        if (page != null) {
            page.saveSettings(WelcomeActivity.this);
        }
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
                case 3: return WelcomeFragment.newInstance(R.layout.layout_welcome_legal);
                case 2: return WelcomeTimeZoneFragment.newInstance(WelcomeActivity.this);
                case 1: return WelcomeLocationFragment.newInstance();
                case 0: default: return WelcomeAppearanceFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 4;
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

        public void saveSettings(Context context) {
            /* EMPTY */
        }

        public int getLayoutResID() {
            return getArguments().getInt(ARG_LAYOUT_RESID);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * WelcomeLocationFragment
     */
    public static class WelcomeLocationFragment extends WelcomeFragment
    {
        private Button button_addPlaces;
        private ProgressBar progress_addPlaces;

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

            progress_addPlaces = (ProgressBar) view.findViewById(R.id.progress_build_places);
            hideProgress();
        }

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
                showProgress();
            }

            @Override
            public void onFinished(Integer result)
            {
                setRetainInstance(false);
                hideProgress();

                if (result > 0)
                {
                    Context context = getActivity();
                    if (context != null) {
                        button_addPlaces.setText(context.getString(R.string.locationbuild_toast_success, result.toString()));
                        button_addPlaces.setVisibility(View.VISIBLE);
                    }
                }
                reloadLocationList();
            }
        };

        protected void showProgress() {
            if (progress_addPlaces != null) {
                progress_addPlaces.setVisibility(View.VISIBLE);
            }
        }
        protected void hideProgress() {
            if (progress_addPlaces != null) {
                progress_addPlaces.setVisibility(View.INVISIBLE);
            }
        }
        protected void reloadLocationList()
        {
            if (isAdded()) {
                FragmentManager fragments = getChildFragmentManager();
                if (fragments != null) {
                    LocationConfigDialog locationConfig = (LocationConfigDialog) fragments.findFragmentByTag("LocationConfigDialog");
                    if (locationConfig != null) {
                        locationConfig.getDialogContent().populateLocationList();
                        locationConfig.getDialogContent().clickLocationSpinner();
                    }
                }
            }
        }

        @Override
        public void saveSettings(Context context)
        {
            if (isAdded())
            {
                FragmentManager fragments = getChildFragmentManager();
                if (fragments != null)
                {
                    LocationConfigDialog locationConfig = (LocationConfigDialog) fragments.findFragmentByTag("LocationConfigDialog");
                    if (locationConfig != null)
                    {
                        locationConfig.getDialogContent().saveSettings(context);
                        Log.d("DEBUG", "saveSettings: location");
                    }
                }
            }
        }
    }

    /**
     * WelcomeTimeZoneFragment
     */
    public static class WelcomeTimeZoneFragment extends WelcomeFragment
    {
        private Spinner timeFormatSpinner;

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
            fragment.setLongitude(WidgetSettings.loadLocationPref(context, 0).getLongitudeAsDouble());
            return fragment;
        }

        private double getLongitude() {
            return getArguments().getDouble(TimeZoneDialog.KEY_LONGITUDE);
        }
        public void setLongitude(double value) {
            getArguments().putDouble(TimeZoneDialog.KEY_LONGITUDE, value);
        }

        @Override
        public void initViews(Context context, View view)
        {
            super.initViews(context, view);

            FragmentManager fragments = getChildFragmentManager();
            if (fragments != null) {
                TimeZoneDialog tzConfig = (TimeZoneDialog) fragments.findFragmentByTag("TimeZoneDialog");
                if (tzConfig != null) {
                    tzConfig.setTimeFormatMode(WidgetSettings.loadTimeFormatModePref(context, 0));
                }
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
            setLongitude(WidgetSettings.loadLocationPref(context, 0).getLongitudeAsDouble());

            FragmentManager fragments = getChildFragmentManager();
            if (fragments != null) {
                TimeZoneDialog tzConfig = (TimeZoneDialog) fragments.findFragmentByTag("TimeZoneDialog");
                if (tzConfig != null) {
                    tzConfig.setLongitude(getLongitude());
                    tzConfig.updatePreview(context);
                }
            }
        }

        private AdapterView.OnItemSelectedListener onTimeFormatSelected = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                FragmentManager fragments = getChildFragmentManager();
                if (fragments != null) {
                    TimeZoneDialog tzConfig = (TimeZoneDialog) fragments.findFragmentByTag("TimeZoneDialog");
                    Activity context = getActivity();
                    if (tzConfig != null && context != null) {
                        tzConfig.setTimeFormatMode((WidgetSettings.TimeFormatMode) parent.getAdapter().getItem(position));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        @Override
        public void saveSettings(Context context)
        {
            if (isAdded())
            {
                FragmentManager fragments = getChildFragmentManager();
                if (fragments != null) {
                    TimeZoneDialog tzConfig = (TimeZoneDialog) fragments.findFragmentByTag("TimeZoneDialog");
                    if (tzConfig != null) {
                        tzConfig.saveSettings(context);
                    }
                }

                WidgetSettings.TimeFormatMode timeFormat = (WidgetSettings.TimeFormatMode) timeFormatSpinner.getSelectedItem();
                WidgetSettings.saveTimeFormatModePref(context, 0, timeFormat);
                Log.d("DEBUG", "saveSettings: timezone");
            }
        }
    }

    /**
     * WelcomeAlarmsFragment
     */
    public static class WelcomeAlarmsFragment extends WelcomeFragment
    {
        public WelcomeAlarmsFragment() {}

        public static WelcomeAlarmsFragment newInstance()
        {
            WelcomeAlarmsFragment fragment = new WelcomeAlarmsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_alarms);
            fragment.setArguments(args);
            return fragment;
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
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_app);
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