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
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.dialog.DialogBase;
import com.forrestguice.suntimeswidget.settings.AppSettings;

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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(icon);
        }

        pagerAdapter = new AboutPagerAdapter(getSupportFragmentManager());
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

    /**
     * AboutPagerAdapter
     */
    public static class AboutPagerAdapter extends FragmentPagerAdapter
    {
        public AboutPagerAdapter(FragmentManager fragments)
        {
            super(fragments);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 1:
                    return AboutAppFragment.newInstance( AboutAppFragment.LAYOUT_CONTRIBUTIONS );
                case 2:
                    return AboutAppFragment.newInstance( AboutAppFragment.LAYOUT_PRIVACY );
                case 0:
                default:
                    return AboutAppFragment.newInstance( AboutAppFragment.LAYOUT_APP );
            }
        }

        @Override
        public int getCount()
        {
            return 3;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static class AboutAppFragment extends DialogBase
    {
        public static final String ARG_LAYOUT_NUMBER = "layoutNumber";

        public static final int LAYOUT_APP = 0;
        public static final int LAYOUT_CONTRIBUTIONS = 1;
        public static final int LAYOUT_PRIVACY = 2;
        //public static final int LAYOUT_BUSKING = 3;

        public AboutAppFragment()
        {
        }

        public static AboutAppFragment newInstance(int layoutNumber)
        {
            AboutAppFragment fragment = new AboutAppFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_NUMBER, layoutNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view;
            int layoutID = getArgs().getInt(ARG_LAYOUT_NUMBER, LAYOUT_APP);
            switch (layoutID)
            {
                case LAYOUT_PRIVACY:
                    view = inflater.inflate(R.layout.layout_about_privacy, container, false);
                    break;

                case LAYOUT_CONTRIBUTIONS:
                    view = inflater.inflate(R.layout.layout_about_contributions, container, false);
                    break;

                //case LAYOUT_BUSKING:
                //    view = inflater.inflate(R.layout.layout_about_busking, container, false);
                //    break;

                case LAYOUT_APP:
                default:
                    view = inflater.inflate(R.layout.layout_about_app, container, false);
                    break;
            }
            updateViews(getContext(), view);
            return view;
        }

        public void updateViews(Context context, View dialogContent)
        {
            TextView nameView = (TextView) dialogContent.findViewById(R.id.txt_about_name);
            if (nameView != null) {
                //nameView.setText(getString(param_appName));   // TODO
                nameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Context context = getActivity();
                        if (context != null) {
                            AboutDialog.openLink(context, getString(R.string.help_app_url));
                        }
                    }
                });
            }

            //ImageView iconView = (ImageView) dialogContent.findViewById(R.id.txt_about_icon);   // TODO
            // if (iconView != null) {
            //    iconView.setImageDrawable(ContextCompat.getDrawable(context, param_iconID));
            //}

            TextView versionView = (TextView) dialogContent.findViewById(R.id.txt_about_version);
            if (versionView != null) {
                versionView.setMovementMethod(LinkMovementMethod.getInstance());
                versionView.setText(SuntimesUtils.fromHtml(htmlVersionString()));
            }

            TextView supportView = (TextView) dialogContent.findViewById(R.id.txt_about_support);
            if (supportView != null) {
                supportView.setMovementMethod(LinkMovementMethod.getInstance());
                supportView.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_support_url, context.getString(R.string.help_support_url))));
            }

            final TextView donateView = (TextView) dialogContent.findViewById(R.id.txt_donate_url);
            if (donateView != null) {
                donateView.setVisibility(View.GONE);
                donateView.setMovementMethod(LinkMovementMethod.getInstance());
                donateView.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_donate_url, context.getString(R.string.app_name), context.getString(R.string.help_donate_url))));
            }

            CheckBox checkDonate = (CheckBox) dialogContent.findViewById(R.id.check_donate);
            if (checkDonate != null)
            {
                checkDonate.setChecked(false);
                checkDonate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (donateView != null) {
                            donateView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                        }
                    }
                });
            }

            TextView legalView1 = (TextView) dialogContent.findViewById(R.id.txt_about_legal1);
            if (legalView1 != null) {
                legalView1.setMovementMethod(LinkMovementMethod.getInstance());
                legalView1.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal1)));
            }

            TextView legalView2 = (TextView) dialogContent.findViewById(R.id.txt_about_legal2);
            if (legalView2 != null) {
                legalView2.setMovementMethod(LinkMovementMethod.getInstance());
                //legalView2.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal2)));
                legalView2.setText(SuntimesUtils.fromHtml(AboutDialog.initTranslationCredits(getActivity())));
            }

            TextView legalView3 = (TextView) dialogContent.findViewById(R.id.txt_about_legal3);
            if (legalView3 != null) {
                legalView3.setMovementMethod(LinkMovementMethod.getInstance());
                legalView3.setText(SuntimesUtils.fromHtml(AboutDialog.initLibraryCredits(getActivity())));
            }

            TextView aboutMediaView = (TextView) dialogContent.findViewById(R.id.txt_about_media);
            if (aboutMediaView != null) {
                aboutMediaView.setMovementMethod(LinkMovementMethod.getInstance());
                aboutMediaView.setText(SuntimesUtils.fromHtml(AboutDialog.initMediaCredits(getActivity())));
            }

            TextView legalView4 = (TextView) dialogContent.findViewById(R.id.txt_about_legal4);
            if (legalView4 != null) {
                String permissionsExplained = context.getString(R.string.privacy_permission_location);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    permissionsExplained += "<br/><br/>" + context.getString(R.string.privacy_permission_storage);
                }
                String privacy = context.getString(R.string.privacy_policy, permissionsExplained);
                legalView4.setText(SuntimesUtils.fromHtml(privacy));
            }

            int[] linkViews = new int[] { R.id.txt_help_url, R.id.txt_about_url, R.id.txt_about_url1, R.id.txt_about_legal5 };
            for (int resID : linkViews)
            {
                TextView text = (TextView) dialogContent.findViewById(resID);
                if (text != null) {
                    text.setText(SuntimesUtils.fromHtml(AboutActivity.anchor(text.getText().toString())));
                    text.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }

        protected static String smallText(String text)
        {
            return "<small>" + text + "</small>";
        }

        public String htmlVersionString()
        {
            String buildString = anchor(getString(R.string.help_commit_url) + BuildConfig.GIT_HASH, BuildConfig.GIT_HASH);
            String versionString = anchor(getString(R.string.help_changelog_url), BuildConfig.VERSION_NAME) + " " + smallText("(" + buildString + ")");
            if (BuildConfig.DEBUG)
            {
                versionString += " " + smallText("[" + BuildConfig.BUILD_TYPE + "]");
            }
            return getString(R.string.app_version, versionString);
        }
    }

    public static String anchor(String url) {
        return anchor(url, url);
    }
    public static String anchor(String url, String text) {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

}
