/**
    Copyright (C) 2014 Forrest Guice
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.forrestguice.suntimeswidget.R;

public class AppSettings
{
    public static final String PREF_KEY_APPEARANCE_THEME = "app_appearance_theme";
    public static final String PREF_DEF_APPEARANCE_THEME = "dark";

    public static final String PREF_KEY_UI_CLOCKTAPACTION = "app_ui_clocktapaction";
    public static final ClockTapAction PREF_DEF_UI_CLOCKTAPACTION = ClockTapAction.ALARM;


    public static enum ClockTapAction
    {
        NOTHING("Do Nothing"),
        ALARM("Set an Alarm"),
        NEXT_NOTE("Show next note"),
        PREV_NOTE("Show previous note");

        private String displayString;

        private ClockTapAction(String displayString)
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
            String[] labels = context.getResources().getStringArray(R.array.clockTapActions_display);
            NOTHING.setDisplayString(labels[0]);
            ALARM.setDisplayString(labels[1]);
            NEXT_NOTE.setDisplayString(labels[2]);
            PREV_NOTE.setDisplayString(labels[3]);
        }
    }

    /**
     * @param context
     * @return
     */
    public static ClockTapAction loadClockTapActionPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String modeString = pref.getString(PREF_KEY_UI_CLOCKTAPACTION, PREF_DEF_UI_CLOCKTAPACTION.name());

        ClockTapAction actionMode;
        try {
            actionMode = ClockTapAction.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            actionMode = PREF_DEF_UI_CLOCKTAPACTION;
        }
        return actionMode;
    }

    /**
     * @param context
     * @return
     */
    public static String loadThemePref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_APPEARANCE_THEME, PREF_DEF_APPEARANCE_THEME);
    }

    public static int loadTheme(Context context)
    {
        String themeName = loadThemePref(context);
        if (themeName != null)
        {
            return (themeName.equals("light") ? R.style.AppTheme_Light : R.style.AppTheme_Dark);
        }
        return R.style.AppTheme_Dark;
    }

    public static void initDisplayStrings( Context context )
    {
        ClockTapAction.initDisplayStrings(context);
    }
}
