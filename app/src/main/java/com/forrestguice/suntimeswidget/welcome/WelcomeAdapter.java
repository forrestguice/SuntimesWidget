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

package com.forrestguice.suntimeswidget.welcome;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.view.ViewPager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WelcomeAdapter extends PagerAdapter
{
    protected final WeakReference<AppCompatActivity> activityRef;
    protected ArrayList<WelcomeFragmentPage> pages = new ArrayList<>();

    public WelcomeAdapter(final AppCompatActivity activity)
    {
        super();
        activityRef = new WeakReference<>(activity);

        pages.add(new WelcomeFragmentPage() {    // 0; first page
            public WelcomeView newInstance(AppCompatActivity activity) {
                return WelcomeFirstPageView.newInstance(activity);
            }
        });
        pages.add(new WelcomeFragmentPage() {    // 1; appearance
            public WelcomeView newInstance(AppCompatActivity activity) {
                return WelcomeAppearanceView.newInstance(activity);
            }
        });
        pages.add(new WelcomeFragmentPage() {    // 2; ui
            public WelcomeView newInstance(AppCompatActivity activity) {
                return WelcomeUserInterfaceView.newInstance(activity);
            }
        });
        pages.add(new WelcomeFragmentPage() {    // 3; location
            public WelcomeView newInstance(AppCompatActivity activity) {
                return WelcomeLocationView.newInstance(activity);
            }
        });
        pages.add(new WelcomeFragmentPage() {    // 4; time zone
            public WelcomeView newInstance(AppCompatActivity activity) {
                return WelcomeTimeZoneView.newInstance(activity);
            }
        });
        if (AlarmSettings.hasAlarmSupport(activity)) {    // 5; alarms
            pages.add(new WelcomeFragmentPage() {
                public WelcomeView newInstance(AppCompatActivity activity) {
                    return WelcomeAlarmsView.newInstance(activity);
                }
            });
        }

        pages.add(new WelcomeFragmentPage() {
            public WelcomeView newInstance(AppCompatActivity activity) {    // last page
                //return WelcomeActivity.WelcomeFragment.newInstance(R.layout.layout_welcome_legal);
                return WelcomeLegalView.newInstance(activity);
            }
        });
    }

    protected WeakReference<ViewPager> pagerRef;
    public void attachToPager(@NonNull ViewPager pager) {
        pagerRef = new WeakReference<>(pager);
        pager.setAdapter(this);
    }
    @Nullable
    protected ViewPager getViewPager() {
        return pagerRef.get();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup viewGroup, int position)
    {
        WelcomeView view = (position >= 0 && position < getCount())
                ? pages.get(position).newInstance(activityRef.get())
                : WelcomeFirstPageView.newInstance(activityRef.get());

        int viewID = (position >= 0 && position < getCount())
                ? pages.get(position).getViewID() : 0;
        if (viewID != 0) {
            view.setId(viewID);
        }

        view.setTag(getTag(position));
        viewGroup.addView(view, position);
        Log.d("DEBUG", "WelcomeAdapter.onInstantiateItem: " + viewID);
        return view;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    public String getTag(int position) {
        return "welcome_" + position;
    }

    public WelcomeView getViewAtPosition(View viewgroup, int position) {
        return viewgroup.findViewWithTag(getTag(position));
    }

    public void onActivityResultCompat(View view, int requestCode, int resultCode, Intent data)
    {
        for (int i=0; i<getCount(); i++) {
            WelcomeView v = (WelcomeView) view.findViewWithTag(getTag(i));
            if (v != null) {
                v.onActivityResultCompat(requestCode, resultCode, data);
            }
        }
    }

    public void onResume(View view)
    {
        for (int i=0; i<getCount(); i++) {
            WelcomeView v = (WelcomeView) view.findViewWithTag(getTag(i));
            if (v != null) {
                v.onResume();
            }
        }
    }

    protected int[] getViewIDs()
    {
        int[] retValue = new int[pages.size()];
        for (int i=0; i<retValue.length; i++) {
            retValue[i] = pages.get(i).getViewID();
        }
        return retValue;
    }

    public static final String KEY_VIEWIDS = "adapterViewIDs";

    public void onSaveInstanceState(Bundle bundle) {
        Log.d("DEBUG", "WelcomeAdapter.onSaveInstanceState");
        bundle.putIntArray(KEY_VIEWIDS, getViewIDs());
    }
    public void onRestoreInstanceState(Bundle bundle)
    {
        Log.d("DEBUG", "WelcomeAdapter.onRestoreInstanceState");
        int[] viewIDs = bundle.getIntArray(KEY_VIEWIDS);
        for (int i=0; i<pages.size(); i++) {
            pages.get(i).setViewID(viewIDs[i]);
        }
    }

    /**
     * WelcomeFragmentPage
     */
    public abstract static class WelcomeFragmentPage
    {
        public abstract WelcomeView newInstance(AppCompatActivity activity);

        private int viewID = 0;
        protected void setViewID(int id) {
            viewID = id;
        }
        public int getViewID()
        {
            if (Build.VERSION.SDK_INT >= 17) {
                if (viewID == 0) {
                    viewID = View.generateViewId();
                }
            }
            return viewID;
        }
    }
}
