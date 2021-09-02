/**
   Copyright (C) 2014-2021 Forrest Guice
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
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public abstract class SuntimesLayout
{
    protected static final SuntimesUtils utils = new SuntimesUtils();

    protected int layoutID;

    public SuntimesLayout()
    {
        initLayoutID();
    }

    protected boolean boldTitle = false;
    protected boolean boldTime = false;

    protected int category = -1;
    public void setCategory(int category)
    {
        this.category = category;
    }

    protected int[] maxDimensionsDp = new int[] {40, 40};
    public void setMaxDimensionsDp(int[] size)
    {
        maxDimensionsDp[0] = size[0];
        maxDimensionsDp[1] = size[1];
    }

    protected int[] paddingDp = new int[] {0, 0, 0, 0};    // left, top, right, bottom

    /**
     * All SuntimesLayout subclasses must implement this method and provide a value for
     * the layoutID. The initLayoutID method should be called from the constructor.
     */
    public abstract void initLayoutID();

    /**
     * @return a layoutID that can be used to create a RemoteViews obj
     */
    public int layoutID()
    {
        return this.layoutID;
    }

    /**
     * @param context the android application context
     * @return a RemoteViews for this layout
     */
    public RemoteViews getViews(Context context)
    {
        return new RemoteViews(context.getPackageName(), layoutID);
    }

    /**
     * Apply a theme (from saved settings for the given appWidgetID) to the RemoteViews this layout
     * knows about.
     * @param context the android application context
     * @param views the RemoteViews to apply the theme to
     * @param appWidgetId the appWidgetID to use when retrieving theme settings
     */
    public void themeViews(Context context, RemoteViews views, int appWidgetId)
    {
        SuntimesTheme theme = WidgetSettings.loadThemePref(context, appWidgetId);
        themeViews(context, views, theme);
    }

    /**
     * Apply the provided theme to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param views the RemoteViews to apply the theme to
     * @param theme the theme object to apply to the views
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        SuntimesUtils.initDisplayStrings(context);

        // theme background
        SuntimesTheme.ThemeBackground background = theme.getBackground();
        if (background.supportsCustomColors())
        {
            views.setInt(R.id.widgetframe_inner, "setBackgroundColor", theme.getBackgroundColor());

        } else {
            views.setInt(R.id.widgetframe_inner, "setBackgroundResource", background.getResID());
            // BUG: setting background screws up padding; pre jellybean versions can't correct for it!
            // either live w/ it, or move this call into if statement below .. however then the background
            // doesn't update for pre jellybean versions, confusing users into thinking themes don't work
            // at all (and they really don't considering the background is 90% of the theme).
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            // fix theme padding (setting background resets padding to 0 for some reason)
            int[] padding = theme.getPaddingPixels(context);
            views.setViewPadding(R.id.widgetframe_inner, padding[0], padding[1], padding[2], padding[3]);

            // theme title text size
            views.setTextViewTextSize(R.id.text_title, TypedValue.COMPLEX_UNIT_DIP, theme.getTitleSizeSp());
        }

        // theme title and text
        int titleColor = theme.getTitleColor();
        views.setTextColor(R.id.text_title, titleColor);
        boldTitle = theme.getTitleBold();
        boldTime = theme.getTimeBold();
        paddingDp = theme.getPadding();
    }

    /**
     * Should call through to WidgetSettings.saveNextSuggestedUpdate (and return true) OR do nothing (and return false).
     * @param context context
     * @param appWidgetId the widgetID
     * @return true if an update time was saved, false otherwise
     */
    public boolean saveNextSuggestedUpdate(Context context, int appWidgetId)
    {
        /* EMPTY */
        return false;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static float[] adjustTextSize(Context context, int[] maxDimensionsDp, int[] paddingDp,
                                         String fontFamily, boolean bold, String timeText, float timeSizeSp, float timeSizeMaxSp, String suffixText, float suffixSizeSp)
    {
        return adjustTextSize(context, maxDimensionsDp, paddingDp, fontFamily, bold, timeText, timeSizeSp, timeSizeMaxSp, suffixText, suffixSizeSp, 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static float[] adjustTextSize(Context context, int[] maxDimensionsDp, int[] paddingDp,
                                         String fontFamily, boolean bold, String timeText, float timeSizeSp, float timeSizeMaxSp, String suffixText, float suffixSizeSp, float iconWidthDp)
    {
        float adjustedTimeSizeSp = timeSizeSp;
        Rect timeBounds = new Rect();
        Paint timePaint = new Paint();
        timePaint.setTypeface(Typeface.create(fontFamily, bold ? Typeface.BOLD : Typeface.NORMAL));

        float adjustedSuffixSizeSp = suffixSizeSp;
        Rect suffixBounds = new Rect();
        Paint suffixPaint = new Paint();
        suffixPaint.setTypeface(Typeface.create(fontFamily, Typeface.BOLD));

        float adjustedIconWidthDp = iconWidthDp;
        float adjustedIconWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, adjustedIconWidthDp, context.getResources().getDisplayMetrics());

        float stepSizeSp = 0.1f;                                      // upscale by stepSize (until maxWidth is filled)
        float suffixRatio = suffixSizeSp / timeSizeSp;                // preserve suffix proportions while scaling
        float iconRatio = iconWidthDp / timeSizeSp;
        //float maxWidthDp = (maxDimensionsDp[0] - paddingDp[0] - 8);   // maxWidth is adjusted for padding and margins
        //float maxHeightDp = (maxDimensionsDp[1] - paddingDp[1] - 8);  // maxHeight is adjusted for padding and margins
        float maxWidthDp = Math.max(maxDimensionsDp[0], 0);
        float maxHeightDp = Math.max(maxDimensionsDp[1], 0);
        float maxWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidthDp, context.getResources().getDisplayMetrics());
        float maxHeightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxHeightDp, context.getResources().getDisplayMetrics());

        int c = 0;
        int limit = 1000;

        while ((timeBounds.width() + suffixBounds.width() + adjustedIconWidthPx) < maxWidthPixels                         // scale up to fill width
                && (adjustedTimeSizeSp < timeSizeMaxSp || timeSizeMaxSp == -1))
        {
            adjustedTimeSizeSp += stepSizeSp;
            adjustedSuffixSizeSp += stepSizeSp * suffixRatio;
            adjustedIconWidthDp += stepSizeSp * iconRatio;
            adjustedIconWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, adjustedIconWidthDp, context.getResources().getDisplayMetrics());
            getTextBounds(context,  timeText, adjustedTimeSizeSp, timePaint, timeBounds);
            getTextBounds(context, suffixText, adjustedSuffixSizeSp, suffixPaint, suffixBounds);

            if (c > limit) {
                Log.w("SuntimesLayout", "adjustTextSize stuck in a loop.. breaking [0]");
                break;
            } else c++;
        }

        c = 0;
        while (timeBounds.height() > maxHeightPixels)
        {
            adjustedTimeSizeSp -= stepSizeSp;
            adjustedSuffixSizeSp -= (stepSizeSp * suffixRatio);
            adjustedIconWidthDp -= (stepSizeSp * iconRatio);
            getTextBounds(context,  timeText, adjustedTimeSizeSp, timePaint, timeBounds);
            getTextBounds(context, suffixText, adjustedSuffixSizeSp, suffixPaint, suffixBounds);

            if (c > limit) {
                Log.w("SuntimesLayout", "adjustTextSize stuck in a loop.. breaking [1] .. " + timeBounds.height() + "px > " + maxHeightPixels + "px [" + maxHeightDp + "dp]");
                break;
            } else c++;
        }

        float[] retValue = new float[3];
        retValue[0] = adjustedTimeSizeSp;
        retValue[1] = adjustedSuffixSizeSp;
        retValue[2] = adjustedIconWidthDp;

        Log.d("ClockLayout", "adjustTextSize: within " + maxDimensionsDp[0] + "," + maxDimensionsDp[1] + " .. baseSp:" + timeSizeSp + ", adjustedSp:" + retValue[0] + ", baseIconDp: " + iconWidthDp +  ", adjustedIconDp: " + retValue[2]);
        return retValue;
    }

    public static void getTextBounds(@NonNull Context context, @NonNull String text, float textSizeSp, @NonNull Paint textPaint, @NonNull Rect textBounds)
    {
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeSp, context.getResources().getDisplayMetrics()));
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
    }

}
