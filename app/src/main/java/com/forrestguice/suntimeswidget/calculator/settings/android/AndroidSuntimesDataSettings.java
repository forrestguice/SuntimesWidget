package com.forrestguice.suntimeswidget.calculator.settings.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.settings.CompareMode;
import com.forrestguice.suntimeswidget.calculator.settings.DateInfo;
import com.forrestguice.suntimeswidget.calculator.settings.DateMode;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetDataMode;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetOrder;
import com.forrestguice.suntimeswidget.calculator.settings.SolarTimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.calculator.settings.TrackingMode;
import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.util.Resources;
import com.forrestguice.util.android.AndroidResources;

import java.util.Set;

public class AndroidSuntimesDataSettings implements SuntimesDataSettings
{
    private final android.content.Context context;
    public AndroidSuntimesDataSettings(android.content.Context context) {
        this.context = context;
    }

    public static AndroidSuntimesDataSettings wrap(android.content.Context context) {
        return new AndroidSuntimesDataSettings(context);
    }

    @Override
    public Resources getResources() {
        if (resources == null) {
            resources = AndroidResources.wrap(context);
        }
        return resources;
    }
    private Resources resources;

    @Override
    public String getString(int id) {
        return getResources().getString(id);
    }
    @Override
    public String getString(int id, Object... formatArgs) {
        return getResources().getString(id, formatArgs);
    }

    @Override
    public SuntimesCalculatorDescriptor loadCalculatorModePref(int appWidgetId, String calculatorName) {
        return WidgetSettings.loadCalculatorModePref(context, appWidgetId, calculatorName);
    }

    @Override
    public Location loadLocationPref(int appWidgetId) {
        return WidgetSettings.loadLocationPref(context, appWidgetId);
    }

    @Override
    public LocationMode loadLocationModePref(int appWidgetId) {
        return WidgetSettings.loadLocationModePref(context, appWidgetId);
    }

    @Override
    public float loadObserverHeightPref(int appWidgetId) {
        return WidgetSettings.loadObserverHeightPref(context, appWidgetId);
    }

    @Override
    public RiseSetOrder loadRiseSetOrderPref(int appWidgetId) {
        return WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
    }

    @Override
    public TrackingMode loadTrackingModePref(int appWidgetId) {
        return WidgetSettings.loadTrackingModePref(context, appWidgetId);
    }

    @Override
    public DateMode loadDateModePref(int appWidgetId) {
        return WidgetSettings.loadDateModePref(context, appWidgetId);
    }

    @Override
    public DateInfo loadDatePref(int appWidgetId) {
        return WidgetSettings.loadDatePref(context, appWidgetId);
    }

    @Override
    public LengthUnit loadLengthUnitsPref(int appWidgetId) {
        return WidgetSettings.loadLengthUnitsPref(context, appWidgetId);
    }

    @Override
    public CompareMode loadCompareModePref(int appWidgetId) {
        return WidgetSettings.loadCompareModePref(context, appWidgetId);
    }

    @Override
    public RiseSetDataMode loadTimeModePref(int appWidgetId) {
        return WidgetSettings.loadTimeModePref(context, appWidgetId);
    }

    @Override
    public SolsticeEquinoxMode loadTimeMode2Pref(int appWidgetId) {
        return WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
    }

    @Override
    public boolean loadLocalizeHemispherePref(int appWidgetId) {
        return WidgetSettings.loadLocalizeHemispherePref(context, appWidgetId);
    }

    @Override
    public boolean loadTimeZoneFromAppPref(int appWidgetID) {
        return WidgetSettings.loadTimeZoneFromAppPref(context, appWidgetID);
    }
    @Override
    public String loadTimezonePref(int appWidgetID) {
        return WidgetSettings.loadTimezonePref(context, appWidgetID);
    }
    @Override
    public TimezoneMode loadTimezoneModePref(int appWidgetID) {
        return WidgetSettings.loadTimezoneModePref(context, appWidgetID);
    }

    @Override
    public CalendarMode loadCalendarModePref(int appWidgetId) {
        return CalendarSettings.loadCalendarModePref(context, appWidgetId);
    }

    @Override
    public Set<String> loadEventList() {
        return EventSettings.loadEventList(context);
    }

    @Override
    public String loadEventValue(String id, String key) {
        return EventSettings.loadEventValue(context, id, key);
    }

    @Override
    public SolarTimeMode loadSolarTimeModePref(int appWidgetID) {
        return WidgetSettings.loadSolarTimeModePref(context, appWidgetID);
    }
}
