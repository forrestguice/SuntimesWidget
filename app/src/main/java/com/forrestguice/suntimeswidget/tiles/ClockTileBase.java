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

package com.forrestguice.suntimeswidget.tiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import com.forrestguice.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.ContextThemeWrapper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;
import java.util.TimeZone;

public class ClockTileBase extends SuntimesTileBase
{
    public static final boolean DEF_LOCATION_FROM_APP = true;
    public static final WidgetSettings.TimezoneMode DEF_TIMEZONE_MODE = WidgetSettings.TimezoneMode.SOLAR_TIME;
    public static final WidgetSettings.ActionMode DEF_ACTION_MODE = WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY;

    protected SuntimesUtils utils = new SuntimesUtils();

    public ClockTileBase(@Nullable Activity activity) {
        super(activity);
    }

    @Override
    public int appWidgetId() {
        return ClockTileService.CLOCKTILE_APPWIDGET_ID;
    }

    @Override
    public Intent getConfigIntent(Context context) {
        return getConfigIntent(context, appWidgetId(), ClockTileConfigActivity.class);
    }

    @Override
    public Intent getLaunchIntent(Context context) {
        return getLaunchIntent(context, appWidgetId(), initData(context));
    }

    @Override
    @Nullable
    protected Intent getLockScreenIntent(Context context) {
        return new Intent(context, TileLockScreenActivity.class);
    }

    @Override
    protected void initDefaults(Context context)
    {
        super.initDefaults(context);
        WidgetSettings.saveActionModePref(context, appWidgetId(), DEF_ACTION_MODE);
        WidgetSettings.saveTimezoneModePref(context, appWidgetId(), DEF_TIMEZONE_MODE);
        WidgetSettings.saveLocationFromAppPref(context, appWidgetId(), DEF_LOCATION_FROM_APP);
    }

    @Override
    public SpannableStringBuilder formatDialogTitle(Context context)
    {
        Calendar now = now(context);
        WidgetSettings.TimeFormatMode formatMode = WidgetSettings.loadTimeFormatModePref(context, appWidgetId());
        String timeString = utils.calendarTimeShortDisplayString(context, now, false, formatMode).toString();
        SpannableString timeDisplay = SuntimesUtils.createBoldSpan(null, timeString, timeString);
        timeDisplay = SuntimesUtils.createRelativeSpan(timeDisplay, timeString, timeString, 1.25f);

        SpannableStringBuilder title = new SpannableStringBuilder();
        title.append(timeDisplay);
        return title;
    }

    @Override
    public SpannableStringBuilder formatDialogMessage(Context context)
    {
        TimeZone timezone = timezone(context);
        Location location = location(context);
        String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);
        boolean isLocalTime = SuntimesTileBase.isLocalTime(timezone.getID());

        String dateString = utils.calendarDateDisplayString(context, now(context), true).toString();
        SpannableString dateDisplay = SuntimesUtils.createBoldSpan(null, dateString, dateString);
        dateDisplay = SuntimesUtils.createRelativeSpan(dateDisplay, dateString, dateString, 1.25f);

        SpannableStringBuilder message = new SpannableStringBuilder(tzDisplay);
        message.append((isLocalTime ? "\n" + location.getLabel() : ""));
        message.append("\n\n");
        message.append(dateDisplay);
        return message;
    }

    @Override
    public Drawable getDialogIcon(Context context)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));
        TimeZone timezone = timezone(context);
        boolean isLocalTime = SuntimesTileBase.isLocalTime(timezone.getID());

        int[] attrs = { R.attr.icActionTime, R.attr.icActionDst };
        TypedArray a = contextWrapper.obtainStyledAttributes(attrs);
        int icon = a.getResourceId(isLocalTime ? 1 : 0, R.drawable.ic_action_time);
        a.recycle();
        return ContextCompat.getDrawable(context, icon);
    }
}
