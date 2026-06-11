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
import com.forrestguice.suntimeswidget.calendar.CalendarSettingsInterface;
import com.forrestguice.suntimeswidget.events.EventSettingsInterface;
import com.forrestguice.util.ContextInterface;

public interface SuntimesDataSettings extends ContextInterface
{
    EventSettingsInterface getEventSettings();
    CalendarSettingsInterface getCalendarSettings();

    SuntimesCalculatorDescriptor loadCalculatorModePref(int appWidgetId, String calculatorName);

    CompareMode loadCompareModePref(int appWidgetId);

    DateMode loadDateModePref(int appWidgetId);
    DateInfo loadDatePref(int appWidgetId);

    LengthUnit loadLengthUnitsPref(int appWidgetId);

    boolean loadLocalizeHemispherePref(int appWidgetId);

    Location loadLocationPref(int appWidgetId);
    LocationMode loadLocationModePref(int appWidgetId);

    float loadObserverHeightPref(int appWidgetId);

    RiseSetOrder loadRiseSetOrderPref(int appWidgetId);

    TimeStandardMode loadSolarTimeModePref(int appWidgetID);
    RiseSetDataMode loadTimeModePref(int appWidgetId);
    SolsticeEquinoxMode loadTimeMode2Pref(int appWidgetId);

    TimeFormatMode loadTimeFormatModePref(int appWidgetId);

    boolean loadTimeZoneFromAppPref(int appWidgetID);
    String loadTimezonePref(int appWidgetID);
    TimezoneMode loadTimezoneModePref(int appWidgetID);

    TrackingMode loadTrackingModePref(int appWidgetId);
}


