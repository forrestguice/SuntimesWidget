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

package com.forrestguice.suntimeswidget.settings.colors;

import android.graphics.Color;
import android.util.Log;

public class ColorUtils
{
    /**
     * isTextReadable
     * @param textColor text color
     * @param backgroundColor background color
     * @return contrast ratio between textColor and backgroundColor is greater than 4.5
     */
    public static boolean isTextReadable(int textColor, int backgroundColor) {
        return getContrastRatio(textColor, backgroundColor) > 4.5;    // AA minimum; https://www.w3.org/TR/WCAG21/#contrast-minimum
    }

    /**
     * https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
     */
    public static double getLuminance(int color)
    {
        double r0 = Color.red(color) / 255d;
        double g0 = Color.green(color) / 255d;
        double b0 = Color.blue(color) / 255d;

        double r = ((r0 <= 0.04045) ? (r0 / 12.92) : Math.pow((r0 + 0.055) / 1.055, 2.4));
        double g = ((g0 <= 0.04045) ? (g0 / 12.92) : Math.pow((g0 + 0.055) / 1.055, 2.4));
        double b = ((b0 <= 0.04045) ? (b0 / 12.92) : Math.pow((b0 + 0.055) / 1.055, 2.4));

        return (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
    }
    public static double getContrastRatio(int textColor, int backgroundColor)
    {
        double l_textColor = getLuminance(textColor);
        double l_backgroundColor = getLuminance(backgroundColor);
        double l1 = Math.max(l_textColor, l_backgroundColor);
        double l2 = Math.min(l_textColor, l_backgroundColor);
        return (l1 + 0.05) / (l2 + 0.05);
    }

}
