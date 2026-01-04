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

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.support.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * WelcomeFragmentAdapter
 */
public class WelcomeFragmentAdapter extends FragmentPagerAdapter
{
    protected ArrayList<WelcomeActivity.WelcomeFragmentPage> pages = new ArrayList<>();

    public WelcomeFragmentAdapter(Context context, final AppCompatActivity activity)
    {
        super(activity.getSupportFragmentManager());
        pages.add(new WelcomeActivity.WelcomeFragmentPage() {    // 0; first page
            public WelcomeActivity.WelcomeFragment newInstance() {
                return WelcomeActivity.WelcomeFirstPageFragment.newInstance();
            }
        });
        pages.add(new WelcomeActivity.WelcomeFragmentPage() {    // 1; appearance
            public WelcomeActivity.WelcomeFragment newInstance() {
                return WelcomeActivity.WelcomeAppearanceFragment.newInstance();
            }
        });
        pages.add(new WelcomeActivity.WelcomeFragmentPage() {    // 2; ui
            public WelcomeActivity.WelcomeFragment newInstance() {
                return WelcomeActivity.WelcomeUserInterfaceFragment.newInstance();
            }
        });
        pages.add(new WelcomeActivity.WelcomeFragmentPage() {    // 3; location
            public WelcomeActivity.WelcomeFragment newInstance() {
                return WelcomeActivity.WelcomeLocationFragment.newInstance();
            }
        });
        pages.add(new WelcomeActivity.WelcomeFragmentPage() {    // 4; time zone
            public WelcomeActivity.WelcomeFragment newInstance() {
                return WelcomeActivity.WelcomeTimeZoneFragment.newInstance(activity);
            }
        });
        if (AlarmSettings.hasAlarmSupport(context)) {    // 5; alarms
            pages.add(new WelcomeActivity.WelcomeFragmentPage() {
                public WelcomeActivity.WelcomeFragment newInstance() {
                    return WelcomeActivity.WelcomeAlarmsFragment.newInstance();
                }
            });
        }

        pages.add(new WelcomeActivity.WelcomeFragmentPage() {
            public WelcomeActivity.WelcomeFragment newInstance() {    // last page
                //return WelcomeActivity.WelcomeFragment.newInstance(R.layout.layout_welcome_legal);
                return WelcomeActivity.WelcomeLegalFragment.newInstance();
            }
        });
    }

    @Override
    public Fragment getItem(int position)
    {
        if (position >= 0 && position < getCount()) {
            return pages.get(position).newInstance();
        } else return WelcomeActivity.WelcomeFirstPageFragment.newInstance();
    }

    @Override
    public int getCount() {
        return pages.size();
    }
}
