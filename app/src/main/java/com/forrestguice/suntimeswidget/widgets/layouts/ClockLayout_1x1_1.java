/**
   Copyright (C) 2025 Forrest Guice
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.ClockWidgetSettings;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockLayout_1x1_1 extends ClockLayout_1x1_0
{
    public ClockLayout_1x1_1() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_clock_1x1_1;
    }

    @Override
    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_clock_1x1_1_align_fill;                       // fill
            case 1: case 2: case 3: return R.layout.layout_widget_clock_1x1_1_align_float_2;    // top
            case 7: case 8: case 9: return R.layout.layout_widget_clock_1x1_1_align_float_8;    // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_clock_1x1_1;         // center
        }
    }

    @Override
    protected float getMaxSp() {
        return 128;
    }

    protected ClockFaceOptions initClockFaceOptions(Context context, int appWidgetId)
    {
        if (options == null) {
            options = new ClockFaceOptions(context, appWidgetId);
        }
        options.textColor = ClockWidgetSettings.loadClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_COLOR, timeColor);
        options.minTextSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, context.getResources().getDisplayMetrics());
        options.textAlign = AppSettings.isLocaleRtl(context) ? Paint.Align.LEFT : Paint.Align.RIGHT;
        return options;
    }
    protected ClockFaceOptions options = null;

    public static String getNowString(Context context, int appWidgetId, Calendar now, ClockFaceOptions options)
    {
        Date time = now.getTime();
        TimeZone tz = now.getTimeZone();
        TimeDateDisplay.applySpecialTimeZone(time, tz);
        Log.d("DEBUG", "getNowString: tz: " + tz);

        switch (options.style)
        {
            case ClockFaceOptions.STYLE_DIGITAL0:
                TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
                TimeDisplayText nowText = time_utils.calendarTimeShortDisplayString(AndroidResources.wrap(context), now, false, timeFormat);
                return nowText.getValue();

            case ClockFaceOptions.STYLE_DIGITAL1:
                SimpleDateFormat hourFormat1 = (is24(context, appWidgetId) ? hourFormat24 : hourFormat12);
                hourFormat1.setTimeZone(tz);
                minuteFormat.setTimeZone(tz);
                return hourFormat1.format(time) + " " + minuteFormat.format(time);

            case ClockFaceOptions.STYLE_VERTICAL:
            default:
                SimpleDateFormat hourFormat = (is24(context, appWidgetId) ? hourFormat24 : hourFormat12);
                hourFormat.setTimeZone(tz);
                minuteFormat.setTimeZone(tz);
                return hourFormat.format(time) + "\n" + minuteFormat.format(time);
        }
    }

    @Override
    protected void updateTimeViews(Context context, int appWidgetId, RemoteViews views, Calendar now)
    {
        ClockFaceOptions options = initClockFaceOptions(context, appWidgetId);
        String nowString = getNowString(context, appWidgetId, now, options);
        views.setTextViewText(R.id.text_time, nowString);
        views.setTextViewText(R.id.text_time_suffix, "");

        int w, h;
        switch (options.style)
        {
            case ClockFaceOptions.STYLE_DIGITAL0:
            case ClockFaceOptions.STYLE_DIGITAL1:
                w = dpWidth;
                h = dpHeight;
                break;

            case ClockFaceOptions.STYLE_VERTICAL:
            default:
                w = h = Math.max(dpWidth, dpHeight);
                break;
        }

        Bitmap b = new ClockFaceBitmap().makeClockBitmap(context, w, h, nowString, options);
        views.setImageViewBitmap(R.id.image_time, b);
        setContentDescription(views, R.id.image_time, nowString);
    }

    protected static void setContentDescription(RemoteViews views, int viewId, CharSequence text) {
        if (Build.VERSION.SDK_INT >= 15) {
            views.setContentDescription(viewId, text);
        }
    }

    public static boolean is24(Context context, int appWidgetId)
    {
        TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        switch (timeFormat)
        {
            case MODE_SUNTIMES: return TimeDateDisplay.is24();
            case MODE_SYSTEM: return android.text.format.DateFormat.is24HourFormat(context);
            case MODE_12HR: return false;
            case MODE_24HR: default: return true;
        }
    }

    protected static SimpleDateFormat hourFormat12 = new SimpleDateFormat("h", Locale.getDefault());
    protected static SimpleDateFormat hourFormat24 = new SimpleDateFormat("HH", Locale.getDefault());
    protected static SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

    /**
     * ClockFaceBitmap
     */
    public static class ClockFaceBitmap
    {
        protected void initPaint(Context context)
        {
            if (pDebug == null)
            {
                pDebug = new Paint();
                pDebug.setStyle(Paint.Style.STROKE);
                pDebug.setStrokeWidth(1);    // debug circles
            }

            if (p == null) {
                p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            }
        }
        private Paint pDebug;
        private TextPaint p;

        public Bitmap makeClockBitmap(Context context, int w, int h, String text, ClockFaceOptions options)
        {
            if (context == null || w <= 0 || h <= 0) {
                return null;
            }

            initPaint(context);
            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);

            //c.drawColor(Color.LTGRAY);
            //c.drawCircle(w/2f, h/2f, w/4f, pDebug);
            //c.drawCircle(w/2f, h/2f, w/2f, pDebug);

            float textSizePx = options.minTextSizePx;
            p.setTextSize(textSizePx);
            p.setTypeface(options.getTypeface());

            String[] lines = text.split("\n");
            StaticLayout layout = getStaticLayout(text, lines, p);

            if (options.scaleText)
            {
                float paddingPx = options.cutout ? context.getResources().getDimension(R.dimen.widget_layout_spacing_small) : 0;
                int[] maxPx = new int[] {(int)(w - 2 * paddingPx), h};
                while (layout.getWidth() < maxPx[0] && layout.getHeight() < maxPx[1])
                {
                    textSizePx += 2;
                    p.setTextSize(textSizePx);
                    layout = getStaticLayout(text, lines, p);
                }
                while (layout.getWidth() > maxPx[0] || layout.getHeight() > maxPx[1])
                {
                    textSizePx -= 1;
                    p.setTextSize(textSizePx);
                    layout = getStaticLayout(text, lines, p);
                }
            }

            p.setStyle(options.outline ? Paint.Style.STROKE : Paint.Style.FILL);
            if (options.outline)
            {
                p.setStrokeWidth(textSizePx * options.outlineRatio);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setStrokeCap(Paint.Cap.ROUND);
                //p.setStrokeMiter(10);
            }

            if (options.glow) {
                p.setShadowLayer(textSizePx * options.glowRatio, options.glowX, options.glowY, options.glowColor);
            } else {
                p.clearShadowLayer();
            }

            if (options.cutout)
            {
                c.drawColor(options.textColor);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
                p.setColor(Color.TRANSPARENT);
            } else {
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                p.setColor(options.textColor);
            }

            p.setTextAlign(options.textAlign);
            float tx, ty;
            switch (options.textAlign)
            {
                case CENTER: tx = w/2f; break;
                case LEFT: tx = w/2f - (layout.getWidth() / 2f); break;
                case RIGHT: default: tx = w/2f + (layout.getWidth() / 2f); break;
            }
            switch (options.style)
            {
                case ClockFaceOptions.STYLE_DIGITAL1:
                case ClockFaceOptions.STYLE_DIGITAL0: ty = h/2f - layout.getHeight() / 2f; break; //h/2f; break;
                case ClockFaceOptions.STYLE_VERTICAL: default: ty = 0;  break;
            }
            c.translate(tx, ty);
            layout.draw(c);
            return b;
        }

        protected static StaticLayout getStaticLayout(String text, String[] lines, TextPaint p)
        {
            if (Build.VERSION.SDK_INT >= 23) {
                return StaticLayout.Builder.obtain(text, 0, text.length(), p, (int) getWidth(lines, p))
                        .setLineSpacing(0, 0.90f).build();
            } else {
                //noinspection deprecation
                return new StaticLayout(text, p, (int) getWidth(lines, p), Layout.Alignment.ALIGN_NORMAL, 0.90f, 0, false);
            }
        }

        protected static float getWidth(String[] lines, TextPaint p)
        {
            float width = 0;
            for (int i=0; i<lines.length; i++)
            {
                float s = p.measureText(lines[i]);
                if (s > width) {
                    width = s;
                }
            }
            return width;
        }
    }

    /**
     * ClockFaceOptions
     */
    public static class ClockFaceOptions
    {
        public static final int STYLE_DIGITAL0 = 0;
        public static final int STYLE_DIGITAL1 = 1;
        public static final int STYLE_VERTICAL = 10;
        public int style = STYLE_VERTICAL;

        public int textColor = Color.WHITE;
        public Paint.Align textAlign = Paint.Align.RIGHT;

        public String fontFamily = "serif-monospace"; //"sans-serif-black";    // casual, cursive, monospace, sans-serif, sans-serif-black, sans-serif-condensed, sans-serif-condensed-light, sans-serif-light, sans-serif-medium, sans-serif-thin, serif, serif-monospace
        public boolean bold = false;
        public boolean italic = false;

        public boolean cutout = false;
        public boolean outline = true;
        public float outlineRatio = 1/48f;

        public boolean glow = false;
        public float glowRatio = 3/48f;
        public int glowX = 1, glowY = 1;
        public int glowColor = Color.WHITE;

        public boolean scaleText = true;
        public float minTextSizePx = 16;

        public ClockFaceOptions() {}

        public ClockFaceOptions(Context context, int appWidgetId)
        {
            scaleText = WidgetSettings.loadScaleTextPref(context, appWidgetId, scaleText);
            textColor = ClockWidgetSettings.loadClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_COLOR, textColor);
            fontFamily = ClockWidgetSettings.loadClockTypefacePref(context, appWidgetId, fontFamily);
            bold = ClockWidgetSettings.loadClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_BOLD, bold);
            italic = ClockWidgetSettings.loadClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_ITALIC, italic);
            outline = ClockWidgetSettings.loadClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE, outline);
        }

        public Typeface getTypeface() {
            return getTypeface(fontFamily, bold, italic);
        }
        public static Typeface getTypeface(String fontFamily, boolean bold, boolean italic)
        {
            return Typeface.create(fontFamily,
                    bold ? italic ? Typeface.BOLD_ITALIC : Typeface.BOLD
                            : italic ? Typeface.ITALIC : Typeface.NORMAL);
        }

        /*
        @Nullable
        public static ClockFaceOptions createClockFaceOptions(String jsonString)
        {
            ClockFaceOptions result = new ClockFaceOptions();
            try {
                JSONObject json = new JSONObject(jsonString);
                result.style = json.has("style") ? json.getInt("style") : result.style;
                result.textColor = json.has("textColor") ? json.getInt("textColor") : result.textColor;
                result.textAlign = json.has("textAlign") ? Paint.Align.values()[json.getInt("textAlign")] : result.textAlign;
                result.fontFamily = json.has("fontFamily") ? json.getString("fontFamily") : result.fontFamily;
                result.bold = json.has("bold") ? json.getBoolean("bold") : result.bold;
                result.italic = json.has("italic") ? json.getBoolean("italic") : result.italic;
                result.outline = json.has("outline") ? json.getBoolean("outline") : result.outline;
                result.outlineRatio = (float) (json.has("outlineRatio") ? json.getDouble("outlineRatio") : result.outlineRatio);
                result.glow = json.has("glow") ? json.getBoolean("glow") : result.glow;
                result.glowX = json.has("glowX") ? json.getInt("glowX") : result.glowX;
                result.glowY = json.has("glowY") ? json.getInt("glowY") : result.glowY;
                result.glowRatio = (float) (json.has("glowRatio") ? json.getDouble("glowRatio") : result.glowRatio);
                result.glowColor = json.has("glowColor") ? json.getInt("glowColor") : result.glowColor;
                result.scaleText = json.has("scaleText") ? json.getBoolean("scaleText") : result.scaleText;
                return result;

            } catch (JSONException e) {
                Log.e("ClockFaceOptions", "fromJSON: " + e);
                return null;
            }
        }
        public String toJSON()
        {
            JSONObject result = new JSONObject();
            try {
                result.put("style", style);
                result.put("textColor", textColor);
                result.put("textAlign", textAlign);
                result.put("fontFamily", fontFamily);
                result.put("bold", bold);
                result.put("italic", italic);
                result.put("outline", outline);
                result.put("outlineRatio", outlineRatio);
                result.put("glow", glow);
                result.put("glowRatio", glowRatio);
                result.put("glowColor", glowColor);
                result.put("glowX", glowX);
                result.put("glowY", glowY);
                result.put("scaleText", scaleText);

            } catch (JSONException e) {
                Log.e("ClockFaceOptions", "toJSON: " + e);
            }
            return result.toString();
        }
        */
    }

    /*
    // this variation uses a TextView and LinearLayout to arrange text onto the canvas
    /*protected Bitmap makeClockBitmap(Context context, int w, int h, String text, String fontFamily, boolean bold, boolean italic, boolean outline, int textColor)
    {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        TextView textView = new TextView(context);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView, 1, 1256, 2, TypedValue.COMPLEX_UNIT_DIP);
        textView.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(textColor);
        if (Build.VERSION.SDK_INT >= 17) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        textView.setTypeface(Typeface.create(fontFamily,
                bold ? italic ? Typeface.BOLD_ITALIC : Typeface.BOLD
                        : italic ? Typeface.ITALIC : Typeface.NORMAL));
        //textView.setText("1\n00");
        textView.setText(text);

        LinearLayout layout = new LinearLayout(context);
        layout.addView(textView);
        layout.measure(c.getWidth(), c.getHeight());
        layout.layout(0, 0, c.getWidth(), c.getHeight());

        initPaint(context);
        pDebug.setStyle(Paint.Style.STROKE);
        pDebug.setStrokeWidth(1);    // debug circles
        c.drawColor(Color.LTGRAY);
        c.drawCircle(w/2f, h/2f, w/4f, pDebug);
        c.drawCircle(w/2f, h/2f, w/2f, pDebug);

        layout.draw(c);

        // this variation draws each line manually...
        //float lineHeight = 0.75f * (-p.ascent() + p.descent());
        //Rect textBounds = new Rect();
        //getTextBounds("00", textSizePx, p, textBounds);
        //String[] lines = text.split("\n");
        //
        //int x = (w / 2) + (int)(lineWidth / 2);
        //int y = h - (int) lineHeight;
        //for (String line : lines)
        //{
        //    c.drawText(line, x, y, p);
        //    y += lineHeight;
        //}

        return b;
    }*/
    /*
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static float adjustTextSize(int[] maxDimensionsPx, String text, String fontFamily, boolean bold, boolean italic, float textSizePx, float textSizeMaxPx)
    {
        float adjustedSizePx = textSizePx;
        Rect textBounds = new Rect();
        Paint textPaint = new Paint();
        textPaint.setTypeface(Typeface.create(fontFamily,
                bold ? italic ? Typeface.BOLD_ITALIC : Typeface.BOLD
                        : italic ? Typeface.ITALIC : Typeface.NORMAL));

        float stepSizePx = 1f;                                      // upscale by stepSize (until maxWidth is filled)
        float maxWidthPx = Math.max(maxDimensionsPx[0], 0);
        float maxHeightPx = Math.max(maxDimensionsPx[1], 0);

        int c = 0;
        int limit = 1000;

        while ((textBounds.width()) < maxWidthPx                         // scale up to fill width
                && (adjustedSizePx < textSizeMaxPx || textSizeMaxPx == -1))
        {
            adjustedSizePx += stepSizePx;
            getTextBounds(text, adjustedSizePx, textPaint, textBounds);

            if (c > limit) {
                Log.w("SuntimesLayout", "adjustTextSize stuck in a loop.. breaking [0]");
                break;
            } else c++;
        }

        c = 0;
        while (textBounds.height() > maxHeightPx)
        {
            adjustedSizePx -= stepSizePx;
            getTextBounds(text, adjustedSizePx, textPaint, textBounds);

            if (c > limit) {
                Log.w("SuntimesLayout", "adjustTextSize stuck in a loop.. breaking [1] .. " + textBounds.height() + "px > " + maxHeightPx + "px [" + maxHeightPx + "px]");
                break;
            } else c++;
        }

        return adjustedSizePx;
    }
    public static void getTextBounds(@NonNull String text, float textSizePx, @NonNull Paint textPaint, @NonNull Rect textBounds)
    {
        textPaint.setTextSize(textSizePx);
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
    }*/

}
