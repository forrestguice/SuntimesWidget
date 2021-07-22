/**
   Copyright (C) 2019 Forrest Guice
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

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public abstract class ClockLayout extends SuntimesLayout
{
    public ClockLayout()
    {
        initLayoutID();
    }

    /**
     * Called by widget before themeViews and updateViews to give the layout obj an opportunity to
     * modify its state based on the supplied data.
     */
    public void prepareForUpdate(SuntimesClockData data)
    {
        // EMPTY
    }

    /**
     * Apply the provided data to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param appWidgetId the android widget ID to update
     * @param views the RemoteViews to apply the data to
     */
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        String titlePattern = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = utils.displayStringForTitlePattern(context, titlePattern, data);
        CharSequence title = (boldTitle ? SuntimesUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

    public static final int CLOCKFACE_MAX_SP = 72;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static float[] adjustTextSize(Context context, int[] maxDimensionsDp, int[] paddingDp,
                                         String fontFamily, boolean bold, String timeText, float timeSizeSp, float timeSizeMaxSp, String suffixText, float suffixSizeSp)
    {
        float adjustedTimeSizeSp = timeSizeSp;
        Rect timeBounds = new Rect();
        Paint timePaint = new Paint();
        timePaint.setTypeface(Typeface.create(fontFamily, bold ? Typeface.BOLD : Typeface.NORMAL));

        float adjustedSuffixSizeSp = suffixSizeSp;
        Rect suffixBounds = new Rect();
        Paint suffixPaint = new Paint();
        suffixPaint.setTypeface(Typeface.create(fontFamily, Typeface.BOLD));

        float stepSizeSp = 0.1f;                                      // upscale by stepSize (until maxWidth is filled)
        float suffixRatio = suffixSizeSp / timeSizeSp;                // preserve suffix proportions while scaling
        float maxWidthDp = (maxDimensionsDp[0] - paddingDp[0] - 8);   // maxWidth is adjusted for padding and margins
        float maxWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidthDp, context.getResources().getDisplayMetrics());

        while ((timeBounds.width() + suffixBounds.width()) < maxWidthPixels
                && (adjustedTimeSizeSp < timeSizeMaxSp || timeSizeMaxSp == -1))
        {
            adjustedTimeSizeSp += stepSizeSp;
            adjustedSuffixSizeSp += stepSizeSp * suffixRatio;
            getTextBounds(context,  timeText, adjustedTimeSizeSp, timePaint, timeBounds);
            getTextBounds(context, suffixText, adjustedSuffixSizeSp, suffixPaint, suffixBounds);
        }

        float[] retValue = new float[2];
        retValue[0] = adjustedTimeSizeSp;
        retValue[1] = adjustedSuffixSizeSp;

        Log.d("ClockLayout", "adjustTextSize: within " + maxDimensionsDp[0] + "," + maxDimensionsDp[1] + " .. baseSp:" + timeSizeSp + ", adjustedSp:" + retValue[0]);
        return retValue;
    }

    public static void getTextBounds(@NonNull Context context, @NonNull String text, float textSizeSp, @NonNull Paint textPaint, @NonNull Rect textBounds)
    {
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeSp, context.getResources().getDisplayMetrics()));
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
    }

}
