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
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.tiles.AlarmTileService;
import com.forrestguice.suntimeswidget.widgets.AlarmWidget0;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings;

import java.util.Calendar;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SHOWICONS;

public class AlarmLayout_1x1_0 extends AlarmLayout
{
    public AlarmLayout_1x1_0() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_alarm_1x1_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesClockData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);
    }

    protected int chooseLayout(int position)
    {
        switch (position)
        {
            case 0: return R.layout.layout_widget_alarm_1x1_0_align_fill;
            case 1: return R.layout.layout_widget_alarm_1x1_0_align_float_1;
            case 2: return R.layout.layout_widget_alarm_1x1_0_align_float_2;
            case 3: return R.layout.layout_widget_alarm_1x1_0_align_float_3;
            case 4: return R.layout.layout_widget_alarm_1x1_0_align_float_4;
            case 6: return R.layout.layout_widget_alarm_1x1_0_align_float_6;
            case 7: return R.layout.layout_widget_alarm_1x1_0_align_float_7;
            case 8: return R.layout.layout_widget_alarm_1x1_0_align_float_8;
            case 9: return R.layout.layout_widget_alarm_1x1_0_align_float_9;
            case 5: default: return R.layout.layout_widget_alarm_1x1_0;
        }
    }

    @Override
    public void updateViews(final Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        String displayString = "";

        Long upcomingAlarmId = AlarmSettings.loadUpcomingAlarmId(context);
        if (upcomingAlarmId == null || upcomingAlarmId == -1) {
            displayString = context.getString(R.string.configLabel_alarms_nextAlarm_none);

        } else {
            AlarmClockItem item = AlarmWidget0.loadAlarmClockItem(context, upcomingAlarmId);
            if (item != null)
            {
                Calendar now = data.now();
                Calendar alarmTime = item.getCalendar();
                long millisUntilAlarm = now.getTimeInMillis() - alarmTime.getTimeInMillis();
                alarmTime.setTimeInMillis(item.alarmtime);

                WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
                displayString = (millisUntilAlarm > 1000 * 60 * 60 * 24)
                        ? utils.calendarDateTimeDisplayString(context, alarmTime, true, false, timeFormat).toString()
                        : utils.calendarTimeShortDisplayString(context, alarmTime, false, timeFormat).toString();

                String itemLabel = item.getLabel(item.getLabel(context));
                String eventDisplay = AlarmTileService.formatEventDisplay(context, item);
                views.setTextViewText(android.R.id.text1, itemLabel);
                views.setTextViewText(R.id.text_event, eventDisplay);
                views.setViewVisibility(R.id.text_event, (eventDisplay != null && !eventDisplay.isEmpty() ? View.VISIBLE : View.GONE));

                long timeUntilMs = item.alarmtime - Calendar.getInstance().getTimeInMillis();
                String timeUntilDisplay = utils.timeDeltaLongDisplayString(timeUntilMs, 0, false, true, false,false).getValue();
                //String timeUntilPhrase = context.getString(((timeUntilMs >= 0) ? R.string.hence : R.string.ago), timeUntilDisplay);
                views.setTextViewText(R.id.text_note, "~ " + timeUntilDisplay);  // TODO: i18n

                views.setTextViewText(R.id.text_note1, item.note);   // TODO: substitutions

                boolean showIcon = AlarmWidgetSettings.loadAlarmWidgetBool(context, appWidgetId, PREF_KEY_ALARMWIDGET_SHOWICONS, PREF_DEF_ALARMWIDGET_SHOWICONS);
                Drawable icon = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), item.getIcon(), null), timeColor);
                views.setImageViewBitmap(android.R.id.icon1, SuntimesUtils.drawableToBitmap(context, icon, (int)timeSizeSp, (int)timeSizeSp, false));
                views.setViewVisibility(android.R.id.icon1, (showIcon ? View.VISIBLE : View.GONE));
                views.setViewVisibility(R.id.icon_layout, (showIcon ? View.VISIBLE : View.GONE));

            } else {
                displayString = context.getString(R.string.configLabel_alarms_nextAlarm_error);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] { maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), (maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) };

                String s = (displayString.length() <= 3 ? "0:00" : displayString);
                float adjustedSizeSp = adjustTextSize(context, maxDp, "sans-serif", boldTime, s, timeSizeSp, AlarmLayout.MAX_SP, true);

                if (adjustedSizeSp != timeSizeSp) {
                    views.setTextViewTextSize(android.R.id.text2, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp);
                }
            }
        }

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        views.setViewVisibility(R.id.text_label, (showLabels ? View.VISIBLE : View.GONE));
        views.setTextViewText(android.R.id.text2, (boldTime ? SuntimesUtils.createBoldSpan(null, displayString, displayString) : displayString));
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
