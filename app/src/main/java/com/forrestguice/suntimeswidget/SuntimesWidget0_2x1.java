/**
    Copyright (C) 2017 Forrest Guice
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

/**
 * Widget receiver for resizable widget (that falls back to 2x1 layout).
 */
public class SuntimesWidget0_2x1 extends SuntimesWidget0
{
    public static final int UPDATEALARM_ID = 2;

    @Override
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity.class;
    }

    @Override
    protected int getUpdateAlarmId()
    {
        return SuntimesWidget0_2x1.UPDATEALARM_ID;
    }
}


