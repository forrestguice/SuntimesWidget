/**
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

package com.forrestguice.suntimeswidget.calculator.settings;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public interface SuntimesDataSettings
{
    SuntimesCalculatorDescriptor loadCalculatorModePref(int appWidgetId, String calculatorName);

    WidgetSettings.CompareMode loadCompareModePref(int appWidgetId);

    WidgetSettings.DateMode loadDateModePref(int appWidgetId);
    WidgetSettings.DateInfo loadDatePref(int appWidgetId);

    boolean loadLocalizeHemispherePref(int appWidgetId);

    Location loadLocationPref(int appWidgetId);
    WidgetSettings.LocationMode loadLocationModePref(int appWidgetId);

    WidgetSettings.SolarTimeMode loadSolarTimeModePref(int appWidgetID);
    WidgetSettings.RiseSetDataMode loadTimeModePref(int appWidgetId);
    SolsticeEquinoxMode loadTimeMode2Pref(int appWidgetId);

    boolean loadTimeZoneFromAppPref(int appWidgetID);
    String loadTimezonePref(int appWidgetID);
    WidgetSettings.TimezoneMode loadTimezoneModePref(int appWidgetID);
}


