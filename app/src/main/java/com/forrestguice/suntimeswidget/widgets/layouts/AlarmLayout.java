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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.tiles.AlarmTileBase;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings;

import java.util.Calendar;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SHOWICONS;

public abstract class AlarmLayout extends SuntimesLayout
{
    protected boolean scaleBase = WidgetSettings.PREF_DEF_APPEARANCE_SCALEBASE;

    public AlarmLayout() {
        initLayoutID();
    }

    public void prepareForUpdate(Context context, int appWidgetId, SuntimesClockData data)
    {
        this.scaleBase = WidgetSettings.loadScaleBasePref(context, appWidgetId);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);
    }

    protected abstract int chooseLayout(int position);

    /**
     * Apply the provided data to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param appWidgetId the android widget ID to update
     * @param views the RemoteViews to apply the data to
     */
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        String titlePattern = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = DataSubstitutions.displayStringForTitlePattern0(context, titlePattern, data);
        CharSequence title = (boldTitle ? SuntimesUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

    protected void updateLabelViews(Context context, RemoteViews views, AlarmClockItem item)
    {
        String itemLabel = item.getLabel(item.getLabel(context));
        String eventDisplay = AlarmTileBase.formatEventDisplay(context, item);
        views.setTextViewText(android.R.id.text1, itemLabel);
        views.setTextViewText(R.id.text_event, eventDisplay);
        views.setViewVisibility(R.id.text_event, (eventDisplay != null && !eventDisplay.isEmpty() ? View.VISIBLE : View.GONE));
    }

    protected void updateTimeUntilView(Context context, RemoteViews views, AlarmClockItem item)
    {
        long timeUntilMs = item.alarmtime - Calendar.getInstance().getTimeInMillis();
        String timeUntilDisplay = utils.timeDeltaLongDisplayString(timeUntilMs, 0, false, true, false,false).getValue();
        //String timeUntilPhrase = context.getString(((timeUntilMs >= 0) ? R.string.hence : R.string.ago), timeUntilDisplay);
        views.setTextViewText(R.id.text_note, "~ " + timeUntilDisplay);  // TODO: i18n
    }

    protected void updateIconView(Context context, RemoteViews views, int appWidgetId, AlarmClockItem item)
    {
        boolean showIcon = AlarmWidgetSettings.loadAlarmWidgetBool(context, appWidgetId, PREF_KEY_ALARMWIDGET_SHOWICONS, PREF_DEF_ALARMWIDGET_SHOWICONS);
        Drawable icon = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), item.getIcon(), null), timeColor);
        views.setImageViewBitmap(android.R.id.icon1, SuntimesUtils.drawableToBitmap(context, icon, (int)timeSizeSp, (int)timeSizeSp, false));
        views.setViewVisibility(android.R.id.icon1, (showIcon ? View.VISIBLE : View.GONE));
        views.setViewVisibility(R.id.icon_layout, (showIcon ? View.VISIBLE : View.GONE));
    }

    protected static String formatTimeDisplayString(Context context, RemoteViews views, int appWidgetId, SuntimesClockData data, AlarmClockItem item)
    {
        Calendar now = data.now();
        Calendar alarmTime = item.getCalendar();
        long millisUntilAlarm = now.getTimeInMillis() - alarmTime.getTimeInMillis();
        alarmTime.setTimeInMillis(item.alarmtime);

        TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        String displayString = (millisUntilAlarm > 1000 * 60 * 60 * 24)
                ? utils.calendarDateTimeDisplayString(context, alarmTime, true, false, timeFormat).toString()
                : utils.calendarTimeShortDisplayString(context, alarmTime, false, timeFormat).toString();

        return displayString;
    }

    protected void updateNoteView(Context context, RemoteViews views, AlarmClockItem item)
    {
        views.setTextViewText(R.id.text_note1, item.note);   // TODO: substitutions
    }

    protected int timeColor = Color.WHITE;
    protected int textColor = Color.WHITE;
    protected int suffixColor = Color.GRAY;
    protected float titleSizeSp = 10;
    protected float timeSizeSp = 12;
    protected float suffixSizeSp = 8;
    protected float textSizeSp = 12;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        timeColor = theme.getTimeColor();
        textColor = theme.getTextColor();
        suffixColor = theme.getTimeSuffixColor();
        boldTime = theme.getTimeBold();
        paddingDp = theme.getPadding();

        views.setTextColor(android.R.id.text2, timeColor);
        views.setTextColor(R.id.text_label, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            textSizeSp = theme.getTextSizeSp();
            timeSizeSp = theme.getTimeSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(android.R.id.text1, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(android.R.id.text2, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
            views.setTextViewTextSize(R.id.text_label, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.text_event, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.text_note, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.text_note1, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
        }
    }

}
