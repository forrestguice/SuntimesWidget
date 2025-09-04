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

package com.forrestguice.suntimeswidget.alarmclock.ui.colors;

import android.content.Context;
import android.graphics.Color;

import com.forrestguice.suntimeswidget.R;

/**
 * BrightAlarmColorValues
 */
public class BrightAlarmColorValues_BlueSky extends BrightAlarmColorValues
{
    public BrightAlarmColorValues_BlueSky(Context context, boolean fallbackDarkTheme) {
        super(context, fallbackDarkTheme);
    }

    @Override
    public int[] getColorAttrs() {
        return new int[ getColorKeys().length ];    // 0 ... skip attrs
    }
    @Override
    public int[] getColorsResDark() {
        return getColorsResLight();    // dark/light colors are the same
    }
    @Override
    public int[] getColorsResLight() {
        return new int[] {
                R.color.cyan_a100, R.color.blue_grey_999,
                R.color.blue_a200, R.color.white,
                R.color.grey_50, R.color.light_text_disabled,
                R.color.light_text_medium, R.color.dark_text_medium,
                R.color.light_text_medium, R.color.dark_text_medium,
                R.color.light_text_medium, R.color.dark_text_high,
                R.color.indigo_a700, R.color.light_text_disabled, R.color.indigo_a700
        };
    }
    @Override
    public int[] getColorsFallback() {
        return new int[] {
                Color.WHITE, Color.BLACK,
                Color.parseColor("#ff9900"), Color.parseColor("#ffd500"),
                Color.parseColor("#ff212121"), Color.parseColor("#ff9e9e9e"),
                Color.WHITE, Color.BLACK,
                Color.WHITE, Color.BLACK,
                Color.WHITE, Color.BLACK,
                Color.CYAN, Color.MAGENTA, Color.CYAN
        };
    }
}
