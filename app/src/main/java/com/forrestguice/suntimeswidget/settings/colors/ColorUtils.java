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
    public static double getLuminance(int color) {
        return (0.2126 * Color.red(color) + 0.7152 * Color.green(color) + 0.0722 * Color.blue(color));
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
