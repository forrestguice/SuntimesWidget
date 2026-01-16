/**
    Copyright (C) 2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import com.forrestguice.suntimeswidget.widgets.SuntimesConfigActivity0;

/**
 * A preferences activity for the main app;
 * @see SuntimesConfigActivity0 for widget configuration.
 */
public interface SettingsActivityInterface
{
    String calendarPackage = "com.forrestguice.suntimescalendars";
    String calendarActivity = "com.forrestguice.suntimeswidget.calendar.SuntimesCalendarActivity";

    int REQUEST_HEADER = 10;

    @Deprecated
    int REQUEST_PICKTHEME_LIGHT = 20;      // SuntimesTheme (legacy)
    @Deprecated
    int REQUEST_PICKTHEME_DARK = 30;       // SuntimesTheme (legacy)

    int REQUEST_TAPACTION_CLOCK = 40;
    int REQUEST_TAPACTION_DATE0 = 50;
    int REQUEST_TAPACTION_DATE1 = 60;
    int REQUEST_TAPACTION_NOTE = 70;
    int REQUEST_MANAGE_EVENTS = 80;
    int REQUEST_WELCOME_SCREEN = 90;

    int REQUEST_PICKCOLORS_BRIGHTALARM = 100;    // AlarmColorValues
    int REQUEST_PICKCOLORS_LIGHT = 120;          // AppColorValues
    int REQUEST_PICKCOLORS_DARK = 130;           // AppColorValues

    String RECREATE_ACTIVITY = "recreate_activity";
}
