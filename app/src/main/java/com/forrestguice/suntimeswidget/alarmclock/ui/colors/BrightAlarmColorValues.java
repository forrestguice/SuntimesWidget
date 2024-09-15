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
public class BrightAlarmColorValues extends AlarmColorValues
{
    public BrightAlarmColorValues(Context context, boolean fallbackDarkTheme) {
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
                R.color.white, R.color.black,
                R.color.sunIcon_color_setting_light, R.color.sunIcon_color_rising_light,
                R.color.dialog_bg_alt_light, R.color.text_disabled_light,
                android.R.color.primary_text_light, android.R.color.primary_text_dark,
                android.R.color.secondary_text_light, android.R.color.secondary_text_dark,
                android.R.color.primary_text_light, android.R.color.primary_text_dark,
                R.color.text_accent_light, R.color.text_disabled_light, R.color.text_accent_light
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
