/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.colors.pickers;

import android.os.Build;
import android.os.Bundle;
import com.forrestguice.support.design.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.forrestguice.suntimeswidget.settings.colors.ColorChangeListener;

/**
 * ColorPickerPagerAdapter
 */
public class ColorPickerPagerAdapter extends FragmentStatePagerAdapter
{
    public ColorPickerPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public interface AdapterListener extends ColorChangeListener {
        /* EMPTY */
    }
    public void setAdapterListener(AdapterListener listener) {
        adapterListener = listener;
    }
    protected AdapterListener adapterListener = null;

    @Override
    public Fragment getItem(int position)
    {
        ColorPickerFragment item;
        if (Build.VERSION.SDK_INT >= 14)
        {
            switch (position) {
                case 3: item = new QuadFlaskColorPickerFragment1(); break;
                case 2: item = new QuadFlaskColorPickerFragment(); break;
                case 1: item = new MaterialColorPickerFragment(); break;
                case 0: default: item = new SimpleColorPickerFragment(); break;
            }
        } else {
            switch (position) {
                case 1: item = new MaterialColorPickerFragment(); break;
                case 0: default: item = new SimpleColorPickerFragment(); break;
            }
        }
        item.setColorChangeListener(onColorChanged);
        return item;
    }
    private final int numFragments = (Build.VERSION.SDK_INT >= 14) ? 4 : 2;

    @Override
    public int getCount() {
        return numFragments;
    }

    private final ColorChangeListener onColorChanged = new ColorChangeListener()
    {
        @Override
        public void onColorChanged(int color)
        {
            if (adapterListener != null) {
                adapterListener.onColorChanged(color);
            }
        }
    };

    @Override
    public Bundle saveState() {
        return null;
    }
}
