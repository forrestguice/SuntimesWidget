/**
    Copyright (C) 2014-2023 Forrest Guice
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;

import java.security.InvalidParameterException;

/**
 * An OnSharedPreferenceChangeListener that copies changes into widget prefs (com.forrestguice.suntimeswidget.xml).
 */
public abstract class WidgetSettingsPreferenceHelper implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String LOG_TAG = "SuntimesSettings";

    public WidgetSettingsPreferenceHelper() {
    }

    public abstract Context getContext();
    public abstract void updateLocale();
    public abstract void rebuildActivity();
    public abstract void setNeedsRecreateFlag();

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.i(LOG_TAG, "onSharedPreferenceChanged: key: " + key);
        Context context = getContext();

        if (key.endsWith(AppSettings.PREF_KEY_PLUGINS_ENABLESCAN))
        {
            SuntimesCalculatorDescriptor.reinitCalculators();
            rebuildActivity();
            return;
        }

        if (key.endsWith(AlarmSettings.PREF_KEY_ALARM_UPCOMING))
        {
            Log.i(LOG_TAG, "onPreferenceChanged: " + AlarmSettings.PREF_KEY_ALARM_UPCOMING + ", rescheduling alarms..");
            context.sendBroadcast(new Intent(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, null)));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_CALCULATOR))
        {
            try {
                // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
                // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
                String calcName = sharedPreferences.getString(key, null);
                SuntimesCalculatorDescriptor descriptor = SuntimesCalculatorDescriptor.valueOf(calcName);
                WidgetSettings.saveCalculatorModePref(context, 0, descriptor);
                CalculatorProvider.clearCachedConfig(0);
                Log.i(LOG_TAG, "onSharedPreferenceChanged: value: " + calcName + " :: " + descriptor);

            } catch (InvalidParameterException e) {
                Log.e(LOG_TAG, "onPreferenceChanged: Failed to persist sun calculator pref! " + e);
            }
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_CALCULATOR + "_moon"))
        {
            try {
                // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
                // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
                String calcName = sharedPreferences.getString(key, null);
                SuntimesCalculatorDescriptor descriptor = SuntimesCalculatorDescriptor.valueOf(calcName);
                WidgetSettings.saveCalculatorModePref(context, 0, "moon", descriptor);
                CalculatorProvider.clearCachedConfig(0);
                Log.i(LOG_TAG, "onSharedPreferenceChanged: value: " + calcName + " :: " + descriptor);

            } catch (InvalidParameterException e) {
                Log.e(LOG_TAG, "onPreferenceChanged: Failed to persist moon calculator pref! " + e);
            }
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_LOCATION_ALTITUDE_ENABLED))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveLocationAltitudeEnabledPref(context, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_LOCATION_ALTITUDE_ENABLED));
            CalculatorProvider.clearCachedConfig(0);
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveTimeFormatModePref(context, 0, WidgetSettings.TimeFormatMode.valueOf(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_APPEARANCE_TIMEFORMATMODE.name())));
            updateLocale();
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_TRACKINGMODE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveTrackingModePref(context, 0, WidgetSettings.TrackingMode.valueOf(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_GENERAL_TRACKINGMODE.name())));
	        return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWSECONDS))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowSecondsPref(context, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWSECONDS));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWTIMEDATE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowTimeDatePref(context, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWTIMEDATE));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWWEEKS))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowWeeksPref(context, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWWEEKS));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWHOURS))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowHoursPref(context, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWHOURS));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWCOMPARE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowComparePref(context, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWCOMPARE));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_COMPAREMODE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.CompareMode mode = WidgetSettings.CompareMode.valueOf(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_GENERAL_COMPAREMODE.name()));
            WidgetSettings.saveCompareModePref(context, 0, mode);
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveLocalizeHemispherePref(context, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_LOCALIZE_HEMISPHERE));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_OBSERVERHEIGHT))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            try {
                WidgetSettings.saveObserverHeightPref(context, 0,
                        Float.parseFloat(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_GENERAL_OBSERVERHEIGHT + "")));
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "onPreferenceChangeD: Failed to persist observerHeight: bad value!" + e);
            }
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_UNITS_LENGTH))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveLengthUnitsPref(context, 0, WidgetSettings.getLengthUnit(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_GENERAL_UNITS_LENGTH.name())));
            rebuildActivity();
            return;
        }

        if (key.endsWith(AppSettings.PREF_KEY_UI_SHOWCROSSQUARTER))
        {
            // adjust 'tracking level' widget pref whenever 'show cross-quarter days' app pref is toggled
            boolean value = sharedPreferences.getBoolean(key, AppSettings.PREF_DEF_UI_SHOWCROSSQUARTER);
            WidgetSettings.saveTrackingLevelPref(context, 0, (value ? WidgetSettings.TRACKINGLEVEL_MAX : WidgetSettings.TRACKINGLEVEL_MIN));
            return;
        }

        if (key.endsWith(AppSettings.PREF_KEY_UI_EMPHASIZEFIELD))
        {
            setNeedsRecreateFlag();
            return;
        }
    }

}
