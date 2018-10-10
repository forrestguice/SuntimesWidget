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
package com.forrestguice.suntimeswidget.lightmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class LightMapWidgetSettings
{
    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_ANALEMMA = "widgetmode_analemma";
    public static final AnalemmaWidgetMode PREF_DEF_APPEARANCE_WIDGETMODE_ANALEMMA = AnalemmaWidgetMode.DEC_EOT;

    /**
     * AnalemmaWidgetMode
     */
    public static enum AnalemmaWidgetMode
    {
        DEC_EOT("Declination & EOT"),
        ALT_EOT("Altitude & EOT"),
        ALT_AZ("Altitude & Azimuth");

        private String displayString;

        private AnalemmaWidgetMode(String displayString)
        {
            this.displayString = displayString;
        }

        public String toString()
        {
            return displayString;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public void setDisplayString( String displayString )
        {
            this.displayString = displayString;
        }

        public static void initDisplayStrings( Context context )
        {
            DEC_EOT.setDisplayString(context.getString(R.string.widgetMode_analemma_dec_eot));
            ALT_EOT.setDisplayString(context.getString(R.string.widgetMode_analemma_alt_eot));
            ALT_AZ.setDisplayString(context.getString(R.string.widgetMode_analemma_alt_az));
        }
    }

    public static void saveAnalemmaModePref(Context context, int appWidgetId, AnalemmaWidgetMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ANALEMMA, mode.name());
        prefs.apply();
    }
    public static AnalemmaWidgetMode loadAnalemmaModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ANALEMMA, PREF_DEF_APPEARANCE_WIDGETMODE_ANALEMMA.name());

        AnalemmaWidgetMode widgetMode;
        try {
            widgetMode = AnalemmaWidgetMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_ANALEMMA;
            Log.w("loadAnalemmaModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_ANALEMMA.name() + "'.");
        }
        return widgetMode;
    }
    public static void deleteAnalemmaModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ANALEMMA);
        prefs.apply();
    }


    /**
     * @param context
     */
    public static void initDisplayStrings( Context context )
    {
        AnalemmaWidgetMode.initDisplayStrings(context);
    }

    /**
     * @param context
     * @param appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId)
    {
        deleteAnalemmaModePref(context, appWidgetId);
    }

}
