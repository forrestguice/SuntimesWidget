/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.ui.colors;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;

/**
 * AlarmColorValues
 */
public class AlarmColorValues extends ResourceColorValues
{
    public static final String TAG_ALARMCOLORS = "alarmcolors";

    public static final String COLOR_SOUNDING_PULSE_START = "color_sounding_pulse_start";
    public static final String COLOR_SOUNDING_PULSE_END = "color_sounding_pulse_end";

    public static final String COLOR_SNOOZING_PULSE_START = "color_snoozing_pulse_start";
    public static final String COLOR_SNOOZING_PULSE_END = "color_snoozing_pulse_end";

    public static final String COLOR_BRIGHT_BACKGROUND_START = "color_bright_bg_start";
    public static final String COLOR_BRIGHT_BACKGROUND_END = "color_bright_bg_end";

    public static final String COLOR_TEXT_PRIMARY = "color_text_primary";
    public static final String COLOR_TEXT_PRIMARY_INVERSE = "color_text_primary_inverse";

    public static final String COLOR_TEXT_SECONDARY = "color_text_secondary";
    public static final String COLOR_TEXT_SECONDARY_INVERSE = "color_text_secondary_inverse";

    public static final String COLOR_TEXT_TIME = "color_text_time";
    public static final String COLOR_TEXT_TIME_INVERSE = "color_text_time_inverse";

    public static final String COLOR_CONTROL_ENABLED = "color_control_enabled";
    public static final String COLOR_CONTROL_DISABLED = "color_control_disabled";
    public static final String COLOR_CONTROL_PRESSED = "color_control_pressed";

    public String[] getColorKeys() {
        return new String[] {
                COLOR_BRIGHT_BACKGROUND_END, COLOR_BRIGHT_BACKGROUND_START,
                COLOR_SOUNDING_PULSE_START, COLOR_SOUNDING_PULSE_END,
                COLOR_SNOOZING_PULSE_START, COLOR_SNOOZING_PULSE_END,
                COLOR_TEXT_PRIMARY, COLOR_TEXT_PRIMARY_INVERSE,
                COLOR_TEXT_SECONDARY, COLOR_TEXT_SECONDARY_INVERSE,
                COLOR_TEXT_TIME, COLOR_TEXT_TIME_INVERSE,
                COLOR_CONTROL_ENABLED, COLOR_CONTROL_DISABLED, COLOR_CONTROL_PRESSED
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                0, 0,
                R.attr.sunsetColor, R.attr.sunriseColor,                // sounding pulse
                R.attr.dialogBackgroundAlt, R.attr.text_disabledColor,  // snoozing pulse
                android.R.attr.textColorPrimary, android.R.attr.textColorPrimaryInverse,
                android.R.attr.textColorSecondary,  android.R.attr.textColorSecondaryInverse,
                android.R.attr.textColorPrimary, android.R.attr.textColorPrimaryInverse,
                R.attr.buttonPressColor, R.attr.text_disabledColor, R.attr.buttonPressColor
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_alarms_bg_endColor, R.string.configLabel_alarms_bg_startColor,
                R.string.configLabel_alarms_soundingPulse_startColor, R.string.configLabel_alarms_soundingPulse_endColor,
                R.string.configLabel_alarms_snoozingPulse_startColor, R.string.configLabel_alarms_snoozingPulse_endColor,
                R.string.configLabel_alarms_text_primaryColor, R.string.configLabel_alarms_text_primaryColorInverse,
                R.string.configLabel_alarms_text_secondaryColor, R.string.configLabel_alarms_text_secondaryColorInverse,
                R.string.configLabel_alarms_text_timeColor, R.string.configLabel_alarms_text_timeColorInverse,
                R.string.configLabel_themeColorAccent, R.string.configLabel_alarms_text_disabledColor, R.string.configLabel_themeColorAction,
        };
    }
    public int[] getColorRoles() {
        return new int[] {
                ROLE_BACKGROUND_PRIMARY, ROLE_BACKGROUND_INVERSE,
                ROLE_TEXT, ROLE_TEXT,
                ROLE_TEXT_INVERSE, ROLE_TEXT_INVERSE,
                ROLE_TEXT_PRIMARY, ROLE_TEXT_PRIMARY_INVERSE,
                ROLE_TEXT, ROLE_TEXT_INVERSE,
                ROLE_TEXT_PRIMARY, ROLE_TEXT_PRIMARY_INVERSE,
                ROLE_ACCENT, ROLE_FOREGROUND, ROLE_ACTION
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.white, R.color.black,
                R.color.sunIcon_color_setting_dark, R.color.sunIcon_color_rising_dark,
                R.color.dialog_bg_alt_dark, R.color.text_disabled_dark,
                android.R.color.primary_text_dark, android.R.color.primary_text_light,
                android.R.color.secondary_text_dark, android.R.color.secondary_text_light,
                android.R.color.primary_text_dark, android.R.color.primary_text_light,
                R.color.text_accent_dark, R.color.text_disabled_dark, R.color.text_accent_dark
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.white, R.color.black,
                R.color.sunIcon_color_setting_light, R.color.sunIcon_color_rising_light,
                R.color.dialog_bg_alt_light, R.color.text_disabled_light,
                android.R.color.primary_text_light, android.R.color.primary_text_dark,
                android.R.color.secondary_text_light, android.R.color.secondary_text_dark,
                android.R.color.primary_text_light, android.R.color.primary_text_dark,
                R.color.text_accent_light, R.color.text_disabled_light, R.color.text_accent_light
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.WHITE, Color.BLACK,
                Color.parseColor("#ff9900"), Color.parseColor("#ffd500"),
                Color.parseColor("#ff212121"), Color.parseColor("#ff9e9e9e"),
                Color.WHITE, Color.BLACK,
                Color.WHITE, Color.BLACK,
                Color.WHITE, Color.BLACK,
                Color.CYAN, Color.MAGENTA, Color.CYAN
        };
    }

    public AlarmColorValues(ColorValues other) {
        super(other);
    }
    public AlarmColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    protected AlarmColorValues(Parcel in) {
        super(in);
    }
    public AlarmColorValues() {
        super();
    }

    public AlarmColorValues(Context context, boolean fallbackDarkTheme) {
        super(context, fallbackDarkTheme);
    }

    public AlarmColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<AlarmColorValues> CREATOR = new Creator<AlarmColorValues>()
    {
        public AlarmColorValues createFromParcel(Parcel in) {
            return new AlarmColorValues(in);
        }
        public AlarmColorValues[] newArray(int size) {
            return new AlarmColorValues[size];
        }
    };

    public static AlarmColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new AlarmColorValues(new AlarmColorValues().getDefaultValues(context, darkTheme));
    }
}
