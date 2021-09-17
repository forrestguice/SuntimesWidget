/**
   Copyright (C) 2017-2021 Forrest Guice
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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesUtils.TimeDisplayText;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * A 1x1 layout that displays either a solstice or equinox datetime.
 */
public class SolsticeLayout_1x1_0 extends SolsticeLayout
{
    protected WidgetSettings.SolsticeEquinoxMode timeMode = WidgetSettings.SolsticeEquinoxMode.EQUINOX_SPRING;
    protected int timeColor = Color.WHITE;

    public SolsticeLayout_1x1_0()
    {
        super();
    }

    /**public SolsticeLayout_1x1_0(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_solstice_1x1_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesEquinoxSolsticeData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        this.layoutID = (scaleBase ? R.layout.layout_widget_solstice_1x1_0_align_fill : R.layout.layout_widget_solstice_1x1_0);
        timeMode = data.timeMode();
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesEquinoxSolsticeData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
        boolean showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        boolean showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);

        WidgetSettings.TrackingMode trackingMode = WidgetSettings.loadTrackingModePref(context, appWidgetId);
        Calendar event = null;
        if (data != null && data.isCalculated()) {
            event = (trackingMode == WidgetSettings.TrackingMode.SOONEST ? data.eventCalendarUpcoming(Calendar.getInstance())
                    : data.eventCalendarClosest(Calendar.getInstance()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) / (showLabels ? 4 : 3))};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, " September 22, ", timeSizeSp, ClockLayout.CLOCKFACE_MAX_SP, "", suffixSizeSp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setTextViewTextSize(R.id.text_time_event, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_event_note, TypedValue.COMPLEX_UNIT_DIP, textSizeSp * textScale);
                    views.setTextViewTextSize(R.id.text_time_event_label, TypedValue.COMPLEX_UNIT_DIP, textSizeSp * textScale);

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                    views.setViewPadding(R.id.text_time_event_label, (int)(2*scaledPadding), 0, (int)(2*scaledPadding), 0);
                    views.setViewPadding(R.id.text_time_event_note, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding / 2));
                }
            }
        }

        if (event != null)
        {
            Calendar now = Calendar.getInstance();

            views.setTextViewText(R.id.text_time_event_label, data.timeMode().getLongDisplayString());
            views.setViewVisibility(R.id.text_time_event_label, (showLabels ? View.VISIBLE : View.GONE));

            TimeDisplayText eventString = utils.calendarDateTimeDisplayString(context, event, showTimeDate, showSeconds, timeFormat);
            views.setTextViewText(R.id.text_time_event, eventString.getValue());

            int noteStringId = R.string.hence;
            if (event.before(now)) {
                noteStringId = R.string.ago;
            }

            String noteTime = utils.timeDeltaDisplayString(now.getTime(), event.getTime(), showWeeks, showHours).toString();
            String noteString = context.getString(noteStringId, noteTime);
            SpannableString noteSpan = (boldTime ? SuntimesUtils.createBoldColorSpan(null, noteString, noteTime, timeColor) : SuntimesUtils.createColorSpan(null, noteString, noteTime, timeColor));
            views.setTextViewText(R.id.text_time_event_note, noteSpan);

        } else {
            views.setTextViewText(R.id.text_time_event, "");
            views.setTextViewText(R.id.text_time_event_note, context.getString(R.string.feature_not_supported_by_source));
            views.setTextViewText(R.id.text_time_event_label, "");
            views.setViewVisibility(R.id.text_time_event_label, View.GONE);
        }
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        timeColor = theme.getTimeColor();
        int textColor = theme.getTextColor();
        int eventColor = theme.getSeasonColor(timeMode);

        views.setTextColor(R.id.text_time_event_note, textColor);
        views.setTextColor(R.id.text_time_event, eventColor);
        views.setTextColor(R.id.text_time_event_label, eventColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            views.setTextViewTextSize(R.id.text_time_event_label, TypedValue.COMPLEX_UNIT_DIP, theme.getTextSizeSp());
            views.setTextViewTextSize(R.id.text_time_event_note, TypedValue.COMPLEX_UNIT_DIP, theme.getTextSizeSp());
            views.setTextViewTextSize(R.id.text_time_event, TypedValue.COMPLEX_UNIT_DIP, theme.getTimeSizeSp());
        }
    }

}
