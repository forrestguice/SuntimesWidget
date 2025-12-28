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

package com.forrestguice.suntimeswidget.graph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;

public class SunSymbolBitmap
{
    public static final String COLOR_SUN_FILL = AppColorKeys.COLOR_SUN_FILL;
    public static final String COLOR_SUN_STROKE = AppColorKeys.COLOR_SUN_STROKE;

    public static final int DRAW_NONE = 0;
    public static final int DRAW_SUN1 = 1;    // (circle) solid stroke
    public static final int DRAW_SUN_CIRCLE_DASHED = 2;    // (circle) dashed stroke
    public static final int DRAW_SUN_LINE_SOLID = 3;
    public static final int DRAW_SUN_LINE_DASHED = 4;
    public static final int DRAW_SUN_CIRCLEDOT_SOLID = 5;
    public static final int DRAW_SUN_CIRCLEDOT_DASHED = 6;
    public static final int DRAW_SUN_CROSS_SOLID = 7;
    public static final int DRAW_SUN_CROSS_DASHED = 8;

    public static int fromSunSymbol(@Nullable SunSymbol symbol)
    {
        if (symbol == null) {
            return DRAW_NONE;
        }
        switch (symbol) {
            case LINE: return DRAW_SUN_LINE_SOLID;
            case DOT: return DRAW_SUN_CIRCLEDOT_SOLID;
            case CROSS: return DRAW_SUN_CROSS_SOLID;
            case CIRCLE: default: return DRAW_SUN1;
        }
    }

    public static void drawSunSymbol(int symbol, int x, int y, int pointRadius, Canvas c, Paint p, ColorValues colors)
    {
        DashPathEffect dashed;
        int pointStroke = (int)Math.ceil(pointRadius / 3d);
        switch (symbol)
        {
            case DRAW_SUN_CIRCLEDOT_DASHED:
                dashed = new DashPathEffect(new float[] {4, 2}, 0);
                drawPoint(x, y, pointRadius, pointStroke, c, p, Color.TRANSPARENT, colors.getColor(COLOR_SUN_STROKE), dashed);
                drawPoint(x, y, pointStroke, 0, c, p, colors.getColor(COLOR_SUN_STROKE), colors.getColor(COLOR_SUN_STROKE), null);
                break;

            case DRAW_SUN_CIRCLEDOT_SOLID:
                drawPoint(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_FILL), colors.getColor(COLOR_SUN_STROKE), null);
                drawPoint(x, y, pointStroke, 0, c, p, colors.getColor(COLOR_SUN_STROKE), colors.getColor(COLOR_SUN_STROKE), null);
                break;

            case DRAW_SUN_CROSS_DASHED:
                dashed = new DashPathEffect(new float[] {4, 2}, 0);
                drawPoint(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_FILL), colors.getColor(COLOR_SUN_STROKE), dashed);
                drawCross(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_STROKE), dashed);
                break;

            case DRAW_SUN_CROSS_SOLID:
                drawPoint(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_FILL), colors.getColor(COLOR_SUN_STROKE), null);
                drawCross(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_STROKE), null);
                break;

            case DRAW_SUN_LINE_DASHED:
                dashed = new DashPathEffect(new float[] {4, 2}, 0);
                drawPoint(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_FILL), colors.getColor(COLOR_SUN_STROKE), dashed);
                drawVerticalLine(x, pointStroke, c, p, colors.getColor(COLOR_SUN_STROKE), dashed);
                break;

            case DRAW_SUN_LINE_SOLID:
                drawPoint(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_FILL), colors.getColor(COLOR_SUN_STROKE), null);
                drawVerticalLine(x, pointStroke, c, p, colors.getColor(COLOR_SUN_STROKE), null);
                break;

            case DRAW_SUN_CIRCLE_DASHED:
                dashed = new DashPathEffect(new float[] {4, 2}, 0);
                drawPoint(x, y, pointRadius, pointStroke, c, p, Color.TRANSPARENT, colors.getColor(COLOR_SUN_STROKE), dashed);
                break;

            case DRAW_SUN1:
            default:
                drawPoint(x, y, pointRadius, pointStroke, c, p, colors.getColor(COLOR_SUN_FILL), colors.getColor(COLOR_SUN_STROKE), null);
                break;
        }
    }

    public static void drawPoint(int x, int y, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
    {
        p.setStyle(Paint.Style.FILL);
        p.setColor(fillColor);
        c.drawCircle(x, y, radius, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeWidth);
        p.setColor(strokeColor);

        if (strokeEffect != null) {
            p.setPathEffect(strokeEffect);
        }

        c.drawCircle(x, y, radius, p);
    }

    public static void drawVerticalLine(int x, int lineWidth, Canvas c, Paint p, int color, @Nullable DashPathEffect effect)
    {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(lineWidth);
        p.setColor(color);

        if (effect != null) {
            p.setPathEffect(effect);
        }

        c.drawLine(x, 0, x, c.getHeight(), p);
    }
    /*protected void drawVerticalLine(Calendar calendar0,  SuntimesRiseSetData data, int lineWidth, Canvas c, Paint p)
    {
        Calendar calendar = Calendar.getInstance(WidgetTimezones.localMeanTime(null, data.location()));
        calendar.setTimeInMillis(calendar0.getTimeInMillis());
        double minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int x = (int) Math.round((minute / MINUTES_IN_DAY) * c.getWidth());
        c.drawRect(x - (lineWidth / 2f), 0, x + (lineWidth / 2f), c.getHeight(), p);
    }*/

    public static void drawCross(int cX, int cY, int radius, int strokeWidth, Canvas c, Paint p, int color, @Nullable DashPathEffect effect)
    {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeWidth);
        p.setColor(color);

        if (effect != null) {
            p.setPathEffect(effect);
        }

        c.drawLine(cX, cY, cX + radius, cY, p);
        c.drawLine(cX, cY, cX - radius, cY, p);
        c.drawLine(cX, cY, cX, cY + radius, p);
        c.drawLine(cX, cY, cX, cY - radius, p);
    }

    public static Drawable makeSunSymbolDrawable(Context context, int symbol, int w, int h, ColorValues colors)
    {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        drawSunSymbol(symbol, w/2, h/2, (w/2) - (w/6), c, makeSunSymbolDrawable_p, colors);
        return new BitmapDrawable(context.getResources(), b);
    }
    private static final Paint makeSunSymbolDrawable_p = new Paint(Paint.ANTI_ALIAS_FLAG);
}
