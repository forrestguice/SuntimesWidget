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

package com.forrestguice.suntimeswidget.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import com.forrestguice.support.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.support.widget.SeekBar;

public class WorldMapSeekBar extends SeekBar
{
    private Drawable majorTick, minorTick, centerTick;

    public WorldMapSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public WorldMapSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WorldMapSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        initDrawables(context);
    }

    private int trackColor = Color.WHITE;
    public void setTrackColor(int color ) {
        this.trackColor = color;
        initDrawables(getContext());
    }

    private int majorTickColor = Color.WHITE, minorTickColor = Color.WHITE, centerTickColor = Color.WHITE;
    public void setTickColor( int majorColor, int minorColor, int centerColor )
    {
        this.majorTickColor = majorColor;
        this.minorTickColor = minorColor;
        this.centerTickColor = centerColor;
        initDrawables(getContext());
    }

    private int intervals = (24 / 3);  // 8; 1 tick every 3 hours
    public void setTickIntervals(int n) {
        intervals = n;
    }

    @Override
    public void setThumb(Drawable drawable)
    {
        super.setThumb(drawable);
        thumb = drawable;
    }

    @Override
    public Drawable getThumb() {
        return thumb;
    }
    private Drawable thumb;

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawTickMarks(canvas);
    }

    private void initDrawables(@NonNull Context context)
    {
        majorTick = ContextCompat.getDrawable(context, R.drawable.ic_tick);
        minorTick = ContextCompat.getDrawable(context, R.drawable.ic_tick);
        centerTick = ContextCompat.getDrawable(context, R.drawable.ic_tick_center);
        initTick(majorTick, true);
        initTick(minorTick, false);
        initTick(centerTick, true, centerTickColor);

        Drawable background = ContextCompat.getDrawable(context, R.drawable.seekbar_background);
        SuntimesUtils.tintDrawable(background, trackColor, trackColor, 0);

        Drawable secondaryProgress = new ColorDrawable(Color.TRANSPARENT);
        Drawable primaryProgress = new ScaleDrawable(new ColorDrawable(Color.TRANSPARENT), Gravity.START, 1, -1);

        LayerDrawable progressDrawable = new LayerDrawable(new Drawable[] { background, secondaryProgress, primaryProgress });
        progressDrawable.setId(0, android.R.id.background);
        progressDrawable.setId(1, android.R.id.secondaryProgress);
        progressDrawable.setId(2, android.R.id.progress);

        Rect bounds = getProgressDrawable().getBounds();
        setProgressDrawable(progressDrawable);
        getProgressDrawable().setBounds(bounds);
    }

    private void initTick(Drawable tick, boolean majorTick)
    {
        initTick(tick, majorTick, (majorTick ? majorTickColor : minorTickColor));
    }
    private void initTick(Drawable tick, boolean majorTick, int color)
    {
        int w = tick.getIntrinsicWidth();
        int h = tick.getIntrinsicHeight();
        int wBound = w <= 0 ? 1 : w / 2;
        int hBound = h <= 0 ? 1 : (majorTick ? h : h / 2);
        tick.setBounds(-wBound, -hBound, wBound, hBound);

        GradientDrawable d = (GradientDrawable) tick;
        d.setColor(color);
        d.invalidateSelf();
    }

    protected void drawTickMarks(Canvas canvas)
    {
        if (majorTick != null && minorTick != null && centerTick != null)
        {
            initTick(majorTick, true);
            initTick(minorTick, false);
            initTick(centerTick, true, centerTickColor);
            float tickSpacing = (getWidth() - getPaddingLeft() - getPaddingRight()) / (float) intervals;

            int saveCount = canvas.save();
            canvas.translate(getPaddingLeft(), getHeight() / 2f);
            for (int i = 0; i <= intervals; i++)
            {
                if (i == intervals / 2) {
                    centerTick.draw(canvas);

                } else if (i > 0 && i < intervals) {
                    if (i % 2 == 0)
                        majorTick.draw(canvas);
                    else minorTick.draw(canvas);
                }
                canvas.translate(tickSpacing, 0);
            }
            canvas.restoreToCount(saveCount);
        }
    }
}
