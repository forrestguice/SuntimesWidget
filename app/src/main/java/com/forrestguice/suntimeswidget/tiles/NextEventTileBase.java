/**
    Copyright (C) 2022 Forrest Guice
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.ContextThemeWrapper;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.views.SpanUtils;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetDataMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.util.android.AndroidResources;

import java.util.Calendar;
import java.util.TimeZone;

@TargetApi(24)
public class NextEventTileBase extends SuntimesTileBase
{
    protected static final TimeDateDisplay utils = new TimeDateDisplay();
    protected static final TimeDeltaDisplay delta_utils = new TimeDeltaDisplay();

    public NextEventTileBase(@Nullable Activity activity) {
        super(activity);
    }

    @Override
    protected int appWidgetId() {
        return NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID;
    }

    @Override
    protected Intent getConfigIntent(Context context) {
        return getConfigIntent(context, appWidgetId(), NextEventTileConfigActivity.class);
    }

    @Override
    protected Intent getLaunchIntent(Context context) {
        return getLaunchIntent(context, appWidgetId(), initData(context));
    }

    @Override
    @Nullable
    protected Intent getLockScreenIntent(Context context) {
        return new Intent(context, TileLockScreenActivity.class);
    }

    public static final boolean DEF_LOCATION_FROM_APP = true;
    public static final TimezoneMode DEF_TIMEZONE_MODE = TimezoneMode.TIME_STANDARD;
    public static final WidgetSettings.ActionMode DEF_ACTION_MODE = WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY;

    @Override
    protected void initDefaults(Context context)
    {
        super.initDefaults(context);
        WidgetSettings.saveActionModePref(context, appWidgetId(), DEF_ACTION_MODE);
        WidgetSettings.saveTimezoneModePref(context, appWidgetId(), DEF_TIMEZONE_MODE);
        WidgetSettings.saveLocationFromAppPref(context, appWidgetId(), DEF_LOCATION_FROM_APP);
    }

    /**
     * initDataset
     */
    protected SuntimesRiseSetDataset initDataset(Context context)
    {
        if (dataset == null) {
            dataset = new SuntimesRiseSetDataset(context, appWidgetId());
            dataset.calculateData(context);
        }
        return dataset;
    }
    protected SuntimesRiseSetDataset dataset = null;

    /**
     * findNextEvent
     */
    protected SuntimesRiseSetDataset.SearchResult findNextEvent(Context context, boolean reinit)
    {
        if (nextEvent == null || reinit) {
            initDataset(context);
            nextEvent = dataset.findNextEvent();
        }
        return nextEvent;
    }
    private SuntimesRiseSetDataset.SearchResult nextEvent;

    @Override
    protected Dialog createDialog(final Context context)
    {
        findNextEvent(context, true);
        return super.createDialog(context);
    }

    protected SpannableStringBuilder formatDialogTitle(Context context)
    {
        SuntimesRiseSetDataset.SearchResult nextEvent = findNextEvent(context, false);
        Calendar event = Calendar.getInstance(TimeZone.getDefault());
        event.setTimeInMillis(nextEvent.getCalendar().getTimeInMillis());
        String timeString = utils.calendarTimeShortDisplayString(AndroidResources.wrap(context), event, false).toString();
        SpannableString timeDisplay = SpanUtils.createBoldSpan(null, timeString, timeString);
        timeDisplay = SpanUtils.createRelativeSpan(timeDisplay, timeString, timeString, 1.25f);

        SpannableStringBuilder title = new SpannableStringBuilder();
        title.append(timeDisplay);
        return title;
    }

    protected SpannableStringBuilder formatDialogMessage(Context context)
    {
        Calendar now = now(context);
        SuntimesRiseSetDataset.SearchResult nextEvent = findNextEvent(context, false);
        Calendar event = Calendar.getInstance(TimeZone.getDefault());
        event.setTimeInMillis(nextEvent.getCalendar().getTimeInMillis());

        RiseSetDataMode mode = nextEvent.getMode();
        String modeString = (mode != null ? mode.toString() : "null");
        SpannableString modeDisplay = SpanUtils.createBoldSpan(null, modeString, modeString);
        modeDisplay = SpanUtils.createRelativeSpan(modeDisplay, modeString, modeString, 1.25f);

        String noteValue = delta_utils.timeDeltaLongDisplayString(now.getTimeInMillis(), event.getTimeInMillis()).getValue();
        String noteString = context.getString(event.after(now) ? R.string.hence : R.string.ago, noteValue);
        CharSequence noteDisplay = SpanUtils.createBoldSpan(null, noteString, noteValue);

        SpannableStringBuilder message = new SpannableStringBuilder();
        message.append(modeDisplay);
        message.append("\n\n");
        message.append(noteDisplay);
        return message;
    }

    protected Drawable getDialogIcon(Context context)
    {
        SuntimesRiseSetDataset.SearchResult nextEvent = findNextEvent(context, false);
        RiseSetDataMode mode = nextEvent.getMode();
        int icon = (mode != null && mode.getTimeMode() == TimeMode.NOON) ? R.drawable.ic_noon_tile
                : (nextEvent.isRising() ? R.drawable.svg_sunrise : R.drawable.svg_sunset);
        Drawable d = ContextCompat.getDrawable(context, icon);

        if (d != null)
        {
            ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));
            int[] attrs = {R.attr.sunriseColor, R.attr.sunsetColor};
            TypedArray a = contextWrapper.obtainStyledAttributes(attrs);
            int colorId = a.getResourceId(nextEvent.isRising() ? 0 : 1, R.color.text_primary);
            a.recycle();
            ContextCompat.setTint(d, ContextCompat.getColor(context, colorId));
        }
        return d;
    }

}
