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
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.settings.AppSettings;

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
        onDone();
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
        @Override
        public void onPageSelected(int position)
        {
            setIndicator(position);
            prevButton.setText(getString((position != 0) ? R.string.welcome_action_prev : R.string.welcome_action_skip));
            nextButton.setText(getString((position != pagerAdapter.getCount()-1) ? R.string.welcome_action_next : R.string.welcome_action_done));
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
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
        pager.setCurrentItem(pager.getCurrentItem() + 1);
    }

    private void onPrev() {
        pager.setCurrentItem(pager.getCurrentItem() - 1);
    }

    private void onSkip()
    {
        AppSettings.setFirstLaunch(WelcomeActivity.this, false);
        finish();
    }

    private void onDone()
    {
        saveSettings();
        AppSettings.setFirstLaunch(WelcomeActivity.this, false);
        finish();
    }

    private void saveSettings()
    {
        FragmentManager fragments = getSupportFragmentManager();
        for (int i=0; i<pagerAdapter.getCount(); i++)
        {
            // https://stackoverflow.com/questions/54279509/how-to-get-elements-of-fragments-created-by-viewpager-in-mainactivity/54280113#54280113
            WelcomeFragment page = (WelcomeFragment) fragments.findFragmentByTag("android:switcher:" + pager.getId() + ":" + i);
            if (page != null) {
                page.saveSettings(WelcomeActivity.this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * WelcomeFragmentAdapter
     */
    public static class WelcomeFragmentAdapter extends FragmentPagerAdapter
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
                case 2: return WelcomeTimeZoneFragment.newInstance();
                case 1: return WelcomeLocationFragment.newInstance();
                case 0: default: return WelcomeFragment.newInstance(R.layout.layout_welcome_app);
            }
        }

        @Override
        public int getCount() {
            return 3;
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
            initViews(getContext(), view);
            updateViews(getContext(), view);
            return view;
        }

        public void initViews(Context context, View view)
        {
            if (view != null)
            {
                int[] textViews = new int[] { R.id.text0 };
                for (int resID : textViews) {
                    TextView text = (TextView) view.findViewById(resID);
                    if (text != null) {
                        text.setText(SuntimesUtils.fromHtml(text.getText().toString()));
                    }
                }
            }
        }

        public void updateViews(Context context, View view) {
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
        public WelcomeTimeZoneFragment() {}

        public static WelcomeTimeZoneFragment newInstance()
        {
            WelcomeTimeZoneFragment fragment = new WelcomeTimeZoneFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RESID, R.layout.layout_welcome_timezone);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void initViews(Context context, View view) {
            super.initViews(context, view);
            // TODO
        }

        @Override
        public void updateViews(Context context, View view) {
            // TODO
        }

        @Override
        public void saveSettings(Context context)
        {
            if (isAdded())
            {
                FragmentManager fragments = getChildFragmentManager();
                if (fragments != null)
                {
                    TimeZoneDialog tzConfig = (TimeZoneDialog) fragments.findFragmentByTag("TimeZoneDialog");
                    if (tzConfig != null)
                    {
                        tzConfig.saveSettings(context);
                        Log.d("DEBUG", "saveSettings: timezone");
                    }
                }
            }
        }
    }

}