package com.forrestguice.suntimeswidget.calculator.settings.android;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.settings.CompareMode;
import com.forrestguice.suntimeswidget.calculator.settings.DateInfo;
import com.forrestguice.suntimeswidget.calculator.settings.DateMode;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetDataMode;
import com.forrestguice.suntimeswidget.calculator.settings.SolarTimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

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
    public DateMode loadDateModePref(int appWidgetId) {
        return WidgetSettings.loadDateModePref(context, appWidgetId);
    }

    @Override
    public DateInfo loadDatePref(int appWidgetId) {
        return WidgetSettings.loadDatePref(context, appWidgetId);
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
    public SolarTimeMode loadSolarTimeModePref(int appWidgetID) {
        return WidgetSettings.loadSolarTimeModePref(context, appWidgetID);
    }
}
