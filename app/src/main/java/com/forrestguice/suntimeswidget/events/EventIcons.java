/**
    Copyright (C) 2020-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

@SuppressWarnings("Convert2Diamond")
public class EventIcons
{
    @SuppressLint("ResourceType")
    public static int getIconResID(Context context, SolarEvents event, boolean northward)
    {
        switch (event)
        {
            case MORNING_ASTRONOMICAL: case MORNING_NAUTICAL: case MORNING_BLUE8: case MORNING_CIVIL:
            case MORNING_BLUE4: case SUNRISE: case MORNING_GOLDEN: case MOONRISE:
                return R.drawable.svg_sunrise;

            case EVENING_GOLDEN: case SUNSET: case EVENING_BLUE4: case EVENING_CIVIL: case EVENING_BLUE8:
            case EVENING_NAUTICAL: case EVENING_ASTRONOMICAL: case MOONSET:
                return R.drawable.svg_sunset;

            case MOONNOON: return R.drawable.svg_moon_noon;
            case MOONNIGHT: return R.drawable.svg_moon_night;

            case FIRSTQUARTER: return (northward) ? R.drawable.svg_moon_q3 : R.drawable.svg_moon_q1;
            case THIRDQUARTER: return (northward) ? R.drawable.svg_moon_q1 : R.drawable.svg_moon_q3;

            case NOON: return getResID(context, R.attr.sunnoonIcon, R.drawable.ic_noon_large);
            case MIDNIGHT: return getResID(context, R.attr.sunnightIcon, R.drawable.ic_midnight_large);
            case NEWMOON: return getResID(context, R.attr.moonPhaseIcon0, R.drawable.ic_moon_new);
            case FULLMOON: return getResID(context, R.attr.moonPhaseIcon2, R.drawable.ic_moon_full);

            case CROSS_SPRING: case CROSS_SUMMER: case CROSS_AUTUMNAL: case CROSS_WINTER:
            case EQUINOX_SPRING: case SOLSTICE_SUMMER: case EQUINOX_AUTUMNAL: case SOLSTICE_WINTER:
                return R.drawable.svg_season;

            default: return 0;
        }
    }
    public static int getIconResID(@Nullable Context context, String tag)
    {
        if (tag != null)
        {
            if (tag.startsWith(TAG_TZ))
            {
                tag = tag.substring(TAG_TZ.length());
                if (tag.equals(WidgetTimezones.ApparentSolarTime.TIMEZONEID) || tag.equals(WidgetTimezones.LocalMeanTime.TIMEZONEID)) {
                    return getResID(context, R.attr.sunnoonIcon, R.drawable.ic_noon_large);

                } else {
                    return getResID(context, R.attr.icActionTime, R.drawable.ic_action_time);
                }

            } else if (tag.startsWith(TAG_ALIAS)) {
                String suffix = null;
                String eventID = tag.substring(TAG_ALIAS.length());
                if (eventID.endsWith(ElevationEvent.SUFFIX_RISING) || eventID.endsWith(ElevationEvent.SUFFIX_SETTING))
                {
                    suffix = eventID.substring(eventID.length()-1);
                    //Log.d("DEBUG", "suffix::" + suffix);
                    eventID = eventID.substring(0, eventID.length()-1);
                }
                if (context != null && EventSettings.hasEvent(context, eventID)) {
                    return (suffix == null ? R.drawable.svg_season
                            : (ElevationEvent.SUFFIX_RISING.equals(suffix) ? R.drawable.svg_sunrise : R.drawable.svg_sunset));
                } else {
                    return getResID(context, R.attr.icActionExtension, R.drawable.ic_action_extension);
                }

            } else {
                return getResID(context, R.attr.icActionTime, R.drawable.ic_action_time);
            }
        } else {
            return getResID(context, R.attr.icActionExtension, R.drawable.ic_action_extension);
        }
    }

    public static float[] getIconScale(SolarEvents event) {
        return new float[] {1f, 1f};
    }
    public static float[] getIconScale(String tag) {
        return new float[] {1f, 1f};
    }

    @SuppressLint("ResourceType")
    public static Integer getIconTint(@Nullable Context context, SolarEvents event, ColorValues colors)
    {
        switch (event)
        {
            case MORNING_ASTRONOMICAL: case MORNING_NAUTICAL:
            case MORNING_BLUE8: case MORNING_CIVIL:
            case MORNING_BLUE4: case SUNRISE: case MORNING_GOLDEN:
                return colors.getColor(AppColorKeys.COLOR_RISING_SUN); //getColor(context, R.attr.sunriseColor, R.color.sunIcon_color_rising_dark);

            case EVENING_GOLDEN: case SUNSET:
            case EVENING_BLUE4: case EVENING_CIVIL:
            case EVENING_BLUE8: case EVENING_NAUTICAL:
            case EVENING_ASTRONOMICAL:
                return colors.getColor(AppColorKeys.COLOR_SETTING_SUN); //getColor(context, R.attr.sunsetColor, R.color.sunIcon_color_setting_dark);

            case EQUINOX_SPRING: case CROSS_SPRING: return colors.getColor(AppColorKeys.COLOR_SPRING); //getColor(context, R.attr.springColor, R.color.springColor_dark);
            case SOLSTICE_SUMMER: case CROSS_SUMMER: return colors.getColor(AppColorKeys.COLOR_SUMMER); //getColor(context, R.attr.summerColor, R.color.summerColor_dark);
            case EQUINOX_AUTUMNAL: case CROSS_AUTUMNAL: colors.getColor(AppColorKeys.COLOR_AUTUMN); //return getColor(context, R.attr.fallColor, R.color.fallColor_dark);
            case SOLSTICE_WINTER: case CROSS_WINTER: return colors.getColor(AppColorKeys.COLOR_WINTER); //getColor(context, R.attr.winterColor, R.color.winterColor_dark);

            case MOONRISE: case MOONNOON: case FIRSTQUARTER: return colors.getColor(AppColorKeys.COLOR_RISING_MOON); //getColor(context, R.attr.moonriseColor, R.color.moonIcon_color_rising_dark);
            case MOONSET: case MOONNIGHT: case THIRDQUARTER: return colors.getColor(AppColorKeys.COLOR_SETTING_MOON); //getColor(context, R.attr.moonsetColor, R.color.moonIcon_color_setting_dark);

            default: return null;
        }
    }
    public static Integer getIconTint(@Nullable Context context, String tag) {
        if (tag != null)
        {
            if (tag.startsWith(TAG_ALIAS)) {
                String eventID = tag.substring(TAG_ALIAS.length());
                if (eventID.endsWith(ElevationEvent.SUFFIX_RISING) || eventID.endsWith(ElevationEvent.SUFFIX_SETTING)) {
                    eventID = eventID.substring(0, eventID.length()-1);
                }
                return ((context != null && EventSettings.hasEvent(context, eventID)) ? EventSettings.getColor(context, eventID) : null);
            }
        }
        return null;
    }

    public static int getIconDrawablePadding(Context context, @NonNull SolarEvents event)
    {
        switch (event)
        {
            case FIRSTQUARTER: case THIRDQUARTER:
            case FULLMOON: case NEWMOON: case NOON: case MIDNIGHT:
                return (int)context.getResources().getDimension(R.dimen.eventIcon_margin1);
            default:
                return (int)context.getResources().getDimension(R.dimen.eventIcon_margin);
        }
    }
    public static int getIconDrawablePadding(Context context, String tag) {
        return (int)context.getResources().getDimension(R.dimen.eventIcon_margin1);
    }

    public static int getIconDrawableInset(Context context, @NonNull SolarEvents event)
    {
        switch (event)
        {
            case FULLMOON: case NEWMOON: case NOON: case MIDNIGHT:
                return (int)context.getResources().getDimension(R.dimen.eventIcon_margin1);
            default:
                return 0;
        }
    }
    public static int getIconDrawableInset(Context context, String tag)
    {
        if (tag != null && (tag.equals(WidgetTimezones.ApparentSolarTime.TIMEZONEID) || tag.equals(WidgetTimezones.LocalMeanTime.TIMEZONEID))) {
            return (int)context.getResources().getDimension(R.dimen.eventIcon_margin1);
        } else {
            return 0;
        }
    }

    public static Drawable getIconDrawable(Context context, String tag, int width, int height) {
        return getIconDrawable(context, EventIcons.getIconResID(context, tag), width, height, getIconScale(tag), getIconDrawableInset(context, tag), EventIcons.getIconTint(context, tag));
    }
    public static Drawable getIconDrawable(Context context, @NonNull SolarEvents event, int width, int height, boolean northward, ColorValues colors) {
        return getIconDrawable(context, EventIcons.getIconResID(context, event, northward), width, height, getIconScale(event), getIconDrawableInset(context, event), EventIcons.getIconTint(context, event, colors));
    }
    public static Drawable getIconDrawable(Context context, int resID, int width, int height, float[] scale, int inset, Integer tint)
    {
        Drawable eventIcon = ContextCompat.getDrawable(context, resID).mutate();
        if (tint != null) {
            tintDrawable(eventIcon, tint);
        }

        if (inset > 0) {
            eventIcon = new InsetDrawable(eventIcon, inset, inset, inset, inset);
        }

        if (width > 0 && height > 0 && scale[0] > 0 && scale[1] > 0) {
            eventIcon.setBounds(0, 0, (int)(scale[0] * width), (int)(scale[1] * height));
        }

        return eventIcon;
    }

    public static int getResID(@Nullable Context context, int attr, int defResID)
    {
        if (context != null)
        {
            int[] attrs = {attr};
            TypedArray a = context.obtainStyledAttributes(attrs);
            int resID = a.getResourceId(0, defResID);
            a.recycle();
            return resID;
        }
        return defResID;
    }

    public static int getColor(@Nullable Context context, int attr, int defColor)
    {
        if (context != null)
        {
            int[] attrs = {attr};
            TypedArray a = context.obtainStyledAttributes(attrs);
            int color = ContextCompat.getColor(context, a.getResourceId(0, defColor));
            a.recycle();
            return color;
        }
        return defColor;
    }

    public static void tintDrawable(Drawable d, int color)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            DrawableCompat.setTint(d, color);
            DrawableCompat.setTintMode(d, PorterDuff.Mode.SRC_IN);
        } else {
            d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    public static final String TAG_ALIAS = "alias_";
    public static final String TAG_TZ = "tz_";

    public static String getIconTag(@Nullable Context context, @Nullable String uriString)
    {
        if (uriString == null) {
            return EventIcons.TAG_TZ + WidgetTimezones.TZID_SYSTEM;
        }
        String tag;
        Uri uri = Uri.parse(uriString);
        if (uri != null)
        {
            String suffix = "";
            String eventID = uri.getLastPathSegment();
            if (eventID != null)
            {
                if (eventID.endsWith(ElevationEvent.SUFFIX_RISING) || eventID.endsWith(ElevationEvent.SUFFIX_SETTING)) {
                    suffix = eventID.substring(eventID.length()-1);
                    eventID = eventID.substring(0, eventID.length()-1);
                }
                if (context != null && EventSettings.hasEvent(context, eventID)) {
                    tag = EventIcons.TAG_ALIAS + eventID + suffix;
                } else tag = null;
            } else tag = EventIcons.TAG_TZ + WidgetTimezones.TZID_SYSTEM;
        } else tag = EventIcons.TAG_TZ + WidgetTimezones.TZID_SYSTEM;

        return tag;
    }

    public static String getIconTag(@Nullable Context context, AlarmClockItem item)
    {
        return (item.timezone != null)
                ? EventIcons.TAG_TZ + item.timezone
                : getIconTag(context, item.getEvent());
    }
}
