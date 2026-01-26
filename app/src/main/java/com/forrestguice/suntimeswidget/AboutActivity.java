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

package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.view.MenuItem;

import com.forrestguice.suntimeswidget.about.AboutPagerAdapter;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.view.ViewPager;
import com.forrestguice.support.widget.TabLayout;
import com.forrestguice.support.widget.Toolbar;

public class AboutActivity extends AppCompatActivity
{
    public static final String EXTRA_ICONID = "iconResourceID";

    private TabLayout tabs;
    private AboutPagerAdapter pagerAdapter;
    private ViewPager viewPager;
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
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int icon = R.drawable.ic_action_suntimes;
        Intent intent = getIntent();
        if (intent != null) {
            icon = intent.getIntExtra(EXTRA_ICONID, icon);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(icon);
        }

        pagerAdapter = new AboutPagerAdapter(this);
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);

        tabs = (TabLayout) findViewById(R.id.tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                AppSettings.checkCustomPermissions(AboutActivity.this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return (super.onOptionsItemSelected(menuItem));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public static String anchor(String url) {
        return anchor(url, url);
    }
    public static String anchor(String url, String text) {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

}
