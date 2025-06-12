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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

public class LightMapSeekbar extends android.support.v7.widget.AppCompatSeekBar
{
    public static final String TAG = LightMapSeekbar.class.getSimpleName();

    protected SuntimesRiseSetDataset data = null;
    protected LightMapView.LightMapColors options;
    protected boolean option_drawBackground = false;

    public LightMapSeekbar(Context context)
    {
        super(context, null, R.style.LightMapSeekBar);
        init(context, new LightMapView.LightMapColors(context));
    }

    public LightMapSeekbar(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context, new LightMapView.LightMapColors(context));
    }

    protected void init(Context context, LightMapView.LightMapColors options)
    {
        this.options = options;

        if (isInEditMode()) {
            setBackgroundColor(options.values.getColor(LightMapColorValues.COLOR_NIGHT));
        }
        setPadding(0, 0, 0, 0);
        setMax(24 * 60);    // minutes in a day
        setSplitTrack(true);

        int thumbColor = ColorUtils.setAlphaComponent(options.values.getColor(LightMapColorValues.COLOR_SUN_STROKE), 255/2);
        setThumbTintList(SuntimesUtils.colorStateList(Color.TRANSPARENT, Color.TRANSPARENT, thumbColor));
    }

    public void resetThumb()
    {
        Calendar calendar = LightMapView.LightMapTask.mapTime(data, options);
        int minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        setProgress(minute);
    }

    public void onResume() { /* EMPTY */ }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        updateViews(data);
    }

    public void updateViews(@Nullable SuntimesRiseSetDataset data)
    {
        this.data = data;
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        resetThumb();

        if (data == null && option_drawBackground) {
            setBackgroundColor(options.values.getColor(LightMapColorValues.COLOR_NIGHT));

        } else if (option_drawBackground) {
            LightMapView.LightMapTask draw = new LightMapView.LightMapTask(getContext());
            Bitmap b = draw.makeBitmap(data, getWidth(), getHeight(), options);
            setBackground(new BitmapDrawable(b));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (data != null) {
            updateViews(data);
        }
    }

    public void themeViews( Context context, @NonNull SuntimesTheme theme ) {
        if (options == null) {
            options = new LightMapView.LightMapColors();
        }
        LightMapView.themeViews(context, theme, options);
    }

    protected void loadSettings(Context context, @NonNull Bundle bundle ) {}
    protected boolean saveSettings(Bundle bundle) {
        return true;
    }

    public void setOptions(@NonNull LightMapView.LightMapColors options) {
        init(getContext(), options);
    }

}