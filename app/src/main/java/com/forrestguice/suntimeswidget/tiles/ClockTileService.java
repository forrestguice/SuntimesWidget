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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.service.quicksettings.Tile;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;
import java.util.TimeZone;

@TargetApi(24)
public class ClockTileService extends SuntimesTileService
{
    public static final int CLOCKTILE_APPWIDGET_ID = -1;

    public static final boolean DEF_LOCATION_FROM_APP = true;

    @Override
    protected int appWidgetId() {
        return CLOCKTILE_APPWIDGET_ID;
    }

    @NonNull
    @SuppressWarnings("rawtypes")
    protected Class getConfigActivityClass(Context context) {
        return ClockTileConfigActivity.class;
    }

    @Override
    protected void initDefaults(Context context)
    {
        super.initDefaults(context);
        WidgetSettings.saveLocationFromAppPref(context, appWidgetId(), DEF_LOCATION_FROM_APP);
    }

    @Override
    protected void updateTile(Context context)
    {
        Tile tile = getQsTile();

        TimeZone timezone = timezone(context);
        String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);
        boolean isSolarTime = WidgetTimezones.LocalMeanTime.TIMEZONEID.equals(timezone.getID()) ||
                WidgetTimezones.ApparentSolarTime.TIMEZONEID.equals(timezone.getID());

        WidgetSettings.TimeFormatMode formatMode = WidgetSettings.loadTimeFormatModePref(context, appWidgetId());
        String timeDisplay = utils.calendarTimeShortDisplayString(context, now(context), false, formatMode).toString() + " " + tzDisplay;
        tile.setLabel(timeDisplay);
        tile.setIcon(Icon.createWithResource(this, isSolarTime ? R.drawable.ic_weather_sunny : R.drawable.ic_action_time));

        updateTileState(context, tile).updateTile();
    }

    @Override
    protected Dialog createDialog(final Context context)
    {
        initLocale(context);
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));

        AlertDialog.Builder dialog = new AlertDialog.Builder(contextWrapper, android.R.style.ThemeOverlay_Material_Dialog);
        dialog.setTitle(formatDialogTitle(context));
        dialog.setMessage(formatDialogMessage(context));
        dialog.setIcon(dialogIcon(context));

        dialog.setNeutralButton(context.getString(R.string.configAction_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityAndCollapse(getConfigIntent(context));
            }
        });
        dialog.setPositiveButton(getLaunchIntentTitle(context), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityAndCollapse(getLaunchIntent(context));
            }
        });

        final Dialog d = dialog.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                startUpdateTask(d);
            }
        });
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopUpdateTask();
            }
        });
        return d;
    }

    protected void startUpdateTask(Dialog dialog)
    {
        //Log.d("DEBUG", "startUpdateTask");
        if (handler == null) {
            handler = new Handler();
        }
        if (updateTask != null) {
            stopUpdateTask();
        }
        updateTask = updateTask(dialog);
        handler.postDelayed(updateTask, updateTaskRateMs());
    }

    protected void stopUpdateTask()
    {
        //Log.d("DEBUG", "stopUpdateTask");
        if (handler != null && updateTask != null) {
            handler.removeCallbacks(updateTask);
            updateTask = null;
        }
    }

    protected void updateDialogViews(Context context, Dialog dialog)
    {
        dialog.setTitle(formatDialogTitle(context));
        TextView message = (TextView) dialog.findViewById(android.R.id.message);
        if (message != null) {
            message.setText(formatDialogMessage(context));
        }
    }

    private Handler handler;
    private Runnable updateTask;
    protected final Runnable updateTask(final Dialog dialog)
    {
        return new Runnable() {
            @Override
            public void run()
            {
                updateDialogViews(getApplicationContext(), dialog);
                handler.postDelayed(this, updateTaskRateMs());
            }
        };
    }
    public static final int UPDATE_RATE = 3000;     // update rate: 3s
    public int updateTaskRateMs() {
        return UPDATE_RATE;
    }

    protected SpannableStringBuilder formatDialogTitle(Context context)
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

    protected SpannableStringBuilder formatDialogMessage(Context context)
    {
        TimeZone timezone = timezone(context);
        Location location = location(context);
        String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);
        boolean isLocalTime = isLocalTime(timezone.getID());

        String dateString = utils.calendarDateDisplayString(context, now(context), true).toString();
        SpannableString dateDisplay = SuntimesUtils.createBoldSpan(null, dateString, dateString);
        dateDisplay = SuntimesUtils.createRelativeSpan(dateDisplay, dateString, dateString, 1.25f);

        SpannableStringBuilder message = new SpannableStringBuilder(tzDisplay);
        message.append((isLocalTime ? "\n" + location.getLabel() : ""));
        message.append("\n\n");
        message.append(dateDisplay);
        return message;

        //String sunriseDisplay = null, sunsetDisplay = null;
        //SuntimesRiseSetData2 data = initData(context);
        //if (data != null)
        //{
        //    Calendar sunrise = data.sunriseCalendarToday();
        //    sunriseDisplay = utils.calendarTimeShortDisplayString(context, sunrise, false, formatMode).toString();
        //    Calendar sunset = data.sunsetCalendarToday();
        //    sunsetDisplay = utils.calendarTimeShortDisplayString(context, sunset, false, formatMode).toString();
        //}

        /**message += "\n\n" + dateDisplay;
         if (sunriseDisplay != null) {
         message += "\n" + sunriseDisplay + " sunrise";   // TODO
         }
         if (sunsetDisplay != null) {
         message += ", " + sunsetDisplay + " sunset" ;    // TODO
         }*/
    }

    protected Drawable dialogIcon(Context context)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));
        TimeZone timezone = timezone(context);
        boolean isLocalTime = isLocalTime(timezone.getID());

        int[] attrs = { R.attr.icActionTime, R.attr.icActionDst };
        TypedArray a = contextWrapper.obtainStyledAttributes(attrs);
        int icon = a.getResourceId(isLocalTime ? 1 : 0, R.drawable.ic_action_time);
        a.recycle();
        return ContextCompat.getDrawable(context, icon);
    }

}
