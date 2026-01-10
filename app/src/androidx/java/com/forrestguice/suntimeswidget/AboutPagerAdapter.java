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

import com.forrestguice.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;

/**
 * AboutPagerAdapter
 */
public class AboutPagerAdapter extends FragmentPagerAdapter
{
    public AboutPagerAdapter(AppCompatActivity activity)
    {
        super(activity.getSupportFragmentManager());
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 1:
                return AboutActivity.AboutAppFragment.newInstance( AboutActivity.AboutAppFragment.LAYOUT_CONTRIBUTIONS );
            case 2:
                return AboutActivity.AboutAppFragment.newInstance( AboutActivity.AboutAppFragment.LAYOUT_PRIVACY );
            case 0:
            default:
                return AboutActivity.AboutAppFragment.newInstance( AboutActivity.AboutAppFragment.LAYOUT_APP );
        }
    }

    @Override
    public int getCount()
    {
        return 3;
    }
}
