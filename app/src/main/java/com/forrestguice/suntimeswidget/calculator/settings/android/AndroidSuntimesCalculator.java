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

package com.forrestguice.suntimeswidget.calculator.settings.android;

import android.content.Context;

import com.forrestguice.colors.Color;
import com.forrestguice.suntimeswidget.calculator.DefaultCalculatorDescriptors;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData0;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.display.AndroidResID_AngleDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.AndroidResID_CardinalDirection;
import com.forrestguice.suntimeswidget.calculator.settings.display.AndroidResID_LengthUnitDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.AndroidResID_MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.AndroidResID_SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.settings.display.AndroidResID_TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.AndroidResID_TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.AngleDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.LengthUnitDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.calendar.AndroidResID_CalendarFormatDisplay;
import com.forrestguice.suntimeswidget.calendar.AndroidResID_CalendarModeDisplay;
import com.forrestguice.suntimeswidget.calendar.AndroidCalendarDisplayFactory;
import com.forrestguice.suntimeswidget.calendar.CalendarFormatDisplay;
import com.forrestguice.suntimeswidget.calendar.CalendarModeDisplay;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.colors.android.AndroidColor;
import com.forrestguice.suntimeswidget.events.BaseEvent;
import com.forrestguice.suntimeswidget.events.DayPercentEvent;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.suntimeswidget.events.MoonElevationEvent;
import com.forrestguice.suntimeswidget.events.MoonIllumEvent;
import com.forrestguice.suntimeswidget.events.ShadowLengthEvent;
import com.forrestguice.suntimeswidget.events.SunElevationEvent;
import com.forrestguice.suntimeswidget.events.android.AndroidEventAliasResolver;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.android.AndroidResID_BaseEvent;
import com.forrestguice.suntimeswidget.events.android.AndroidResID_DayPercentEvent;
import com.forrestguice.suntimeswidget.events.android.AndroidResID_EventSettings;
import com.forrestguice.suntimeswidget.events.android.AndroidResID_MoonElevationEvent;
import com.forrestguice.suntimeswidget.events.android.AndroidResID_MoonIllumEvent;
import com.forrestguice.suntimeswidget.events.android.AndroidResID_ShadowLengthEvent;
import com.forrestguice.suntimeswidget.events.android.AndroidResID_SunElevationEvent;
import com.forrestguice.util.SystemTimeFormat;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.android.AndroidTimeFormat;

import com.forrestguice.suntimeswidget.BuildConfig;

import net.time4j.android.ApplicationStarter;

public class AndroidSuntimesCalculator
{
    public static void init(Context context)
    {
        ApplicationStarter.initialize(context, false);    // time4j

        SystemTimeFormat.init(new AndroidTimeFormat(context.getApplicationContext()));
        SuntimesCalculatorDescriptor.initDefaultDescriptors(new DefaultCalculatorDescriptors());
        SuntimesData.initDataSettingsFactory(new AndroidSuntimesDataSettingsFactory());
        
        AndroidSuntimesDataSettings dataSettings = AndroidSuntimesDataSettings.wrap(context);
        SuntimesMoonData0.setFallbackInfo(new MoonDataFallbackInfo());
        SuntimesRiseSetData.setResIDs(new AndroidResID_SuntimesRiseSetData());

        // settings (display)
        AndroidResources r = AndroidResources.wrap(context);
        TimeDeltaDisplay.initDisplayStrings(r, new AndroidResID_TimeDeltaDisplay());
        AngleDisplay.initDisplayStrings(r, new AndroidResID_AngleDisplay(), new AndroidResID_CardinalDirection());
        LengthUnitDisplay.initDisplayStrings_LengthUnit(r, new AndroidResID_LengthUnitDisplay());
        MoonPhaseDisplay.initDisplayStrings(r, new AndroidResID_MoonPhaseDisplay());
        TimeDateDisplay.initDisplayStrings(dataSettings, new AndroidResID_TimeDateDisplay());

        // events + data
        EventSettings.setResIDs(new AndroidResID_EventSettings());
        BaseEvent.setResIDs(new AndroidResID_BaseEvent());
        EventUri.setBuildConfigInfo(new BuildConfigInfo());
        EventAlias.initItemResolver(new AndroidEventAliasResolver());
        DayPercentEvent.setResIDs(new AndroidResID_DayPercentEvent());
        ShadowLengthEvent.setResIDs(new AndroidResID_ShadowLengthEvent());
        MoonIllumEvent.setResIDs(new AndroidResID_MoonIllumEvent());
        MoonElevationEvent.setResIDs(new AndroidResID_MoonElevationEvent());
        SunElevationEvent.setResIDs(new AndroidResID_SunElevationEvent());

        // calendars
        CalendarModeDisplay.initDisplayStrings(r, new AndroidResID_CalendarModeDisplay());
        CalendarFormatDisplay.initDisplayStrings(r, new AndroidResID_CalendarFormatDisplay(), AndroidCalendarDisplayFactory.create());

        // color
        Color.init(new AndroidColor());
    }

    public static class MoonDataFallbackInfo implements SuntimesMoonData0.FallbackCalculatorInfo
    {
        @Override
        public SuntimesCalculator fallbackCalculator() {
            return new com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator();
        }

        @Override
        public SuntimesCalculatorDescriptor fallbackCalculatorDescriptor() {
            return DefaultCalculatorDescriptors.Time4A_4J();
        }
    }

    public static class BuildConfigInfo implements EventUri.BuildConfigInfo
    {
        @Override
        public String AUTHORITY_ROOT() {
            return BuildConfig.SUNTIMES_AUTHORITY_ROOT;
        }
    }
}
