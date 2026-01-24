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
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.welcome.WelcomeAdapter;
import com.forrestguice.suntimeswidget.welcome.WelcomeAlarmsView;
import com.forrestguice.suntimeswidget.welcome.WelcomeLocationView;
import com.forrestguice.suntimeswidget.welcome.WelcomeView;
import com.forrestguice.suntimeswidget.settings.SettingsActivityInterface;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.support.app.ActivityResultLauncherCompat;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.view.ViewPager;

public class WelcomeActivity extends AppCompatActivity
{
    public static final String EXTRA_PAGE = "page";

    protected ActivityResultLauncherCompat startActivityForResult_importPlaces = registerForActivityResultCompat(WelcomeLocationView.IMPORT_REQUEST);
    protected ActivityResultLauncherCompat startActivityForResult_importAlarms = registerForActivityResultCompat(WelcomeAlarmsView.IMPORT_REQUEST);

    private ViewPager pager;
    private WelcomeAdapter pagerAdapter;
    private Button nextButton, prevButton;
    private LinearLayout indicatorLayout;
    private AppSettings.LocaleInfo localeInfo;

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

        pagerAdapter = new WelcomeAdapter(this);
        pager = (ViewPager) findViewById(R.id.container);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(pagerChangeListener);
        pager.setOffscreenPageLimit(pagerAdapter.getCount()-1);

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
    public void onResume()
    {
        super.onResume();
        if (pagerAdapter != null) {
            pagerAdapter.onResume(pager);
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
        WelcomeView page = pagerAdapter.getViewAtPosition(pager, position);
        if (page != null) {
            if (page.validateInput(WelcomeActivity.this)) {
                return page.saveSettings(WelcomeActivity.this);
            } else return false;
        }
        return false;
    }
    private boolean validateInput(AppCompatActivity activity, int position)
    {
        WelcomeView page = pagerAdapter.getViewAtPosition(pager, position);
        if (page != null) {
            return page.validateInput(WelcomeActivity.this);
        }
        return true;
    }
    private void updateViews(AppCompatActivity activity, int position)
    {
        WelcomeView page = pagerAdapter.getViewAtPosition(pager, position);
        if (page != null) {
            page.updateViews(WelcomeActivity.this);
        }
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

    public void onActivityResultCompat(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResultCompat(requestCode, resultCode, data);
        if (pagerAdapter != null) {
            pagerAdapter.onActivityResultCompat(pager, requestCode, resultCode, data);
        }
    }

}