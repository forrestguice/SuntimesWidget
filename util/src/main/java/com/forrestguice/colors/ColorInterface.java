// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.colors;

public interface ColorInterface
{
    /**
     * @param color hex color string
     * @return int argb color int
     */
    int parseColor(String color);

    /**
     * @return red [0, 255]
     */
    int red(int color);

    /**
     * @return green [0, 255]
     */
    int green(int color);

    /**
     * @return blue [0, 255]
     */
    int blue(int color);

    /**
     * @return alpha [0, 255]
     */
    int alpha(int color);
}