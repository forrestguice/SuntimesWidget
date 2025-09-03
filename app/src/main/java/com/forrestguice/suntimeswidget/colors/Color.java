// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.colors;

import com.forrestguice.util.Log;

public class Color
{
    public static final int BLACK = 0;
    public static final int WHITE = -1;

    private static ColorInterface utils;
    public static void init(ColorInterface i) {
        utils = i;
    }

    public static int parseColor(String color)
    {
        if (utils != null) {
            return utils.parseColor(color);
        } else {
            Log.e("parseColor", "ColorUtilsInterface is uninitialized!! call `init` first.");
            return 0;
        }
    }
}