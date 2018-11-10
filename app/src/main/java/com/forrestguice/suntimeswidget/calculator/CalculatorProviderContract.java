/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator;

/**
 * CalculatorProviderContract
 * @version 0.1.0
 *
 * Moon Phases
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moon/phases                     .. get upcoming moon phases
 *       content://suntimeswiget.calculator.provider/moon/phases/[millis]            .. get upcoming moon phases after date (timestamp)
 *       content://suntimeswiget.calculator.provider/moon/phases/[millis]-[millis]   .. get upcoming moon phases for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       [COLUMN_MOON_NEW(long), COLUMN_MOON_FIRST(long), COLUMN_MOON_FULL(long), COLUMN_MOON_THIRD(long)]
 *
 *
 * Solstice and Equinox
 *   The following URIs are supported:
 *       content://suntimeswidget.calculator.provider/seasons                         .. get vernal, summer, autumn, and winter dates for this year
 *       content://suntimeswidget.calculator.provider/seasons/[year]                  .. get vernal, summer, autumn, and winter dates for some year
 *       content://suntimeswidget.calculator.provider/seasons/[year]-[year]           .. get vernal, summer, autumn, and winter dates for range
 *
 *   The result will be one or more rows containing:
 *       [COLUMN_YEAR(int), COLUMM_SEASON_VERNAL(long), COLUMN_SEASON_SUMMER(long), COLUMN_SEASON_AUTUMN(long), COLUMN_SEASON_WINTER(long)]
 *
 * ------------------------------------------------------------------------------------------------
 * Example:
 *     String[] projection = new String[] { SuntimesCalculatorProviderContract.COLUMN_MOON_NEW, SuntimesCalculatorProviderContract.COLUMN_MOON_FIRST, SuntimesCalculatorProviderContract.COLUMN_MOON_FULL, SuntimesCalculatorProviderContract.COLUMN_MOON_THIRD };*
 *     Uri uri = Uri.parse("content://" + SuntimesCalculatorProviderContract.AUTHORITY + "/" + SuntimesCalculatorProviderContract.QUERY_MOONPHASE + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());*
 *     Cursor cursor = resolver.query(uri, projection, null, null, null);
 */
public final class CalculatorProviderContract
{
    public static final String AUTHORITY = "suntimeswidget.calculator.provider";

    public static final String QUERY_MOONPHASE = "moon/phases";
    public static final String COLUMN_MOON_NEW = "new";
    public static final String COLUMN_MOON_FIRST = "first";
    public static final String COLUMN_MOON_FULL = "full";
    public static final String COLUMN_MOON_THIRD = "third";

    public static final String QUERY_SEASONS = "seasons";
    public static final String COLUMN_SEASON_YEAR = "year";
    public static final String COLUMN_SEASON_VERNAL = "vernal";
    public static final String COLUMN_SEASON_SUMMER = "summer";
    public static final String COLUMN_SEASON_AUTUMN = "autumn";
    public static final String COLUMN_SEASON_WINTER = "winter";
}
