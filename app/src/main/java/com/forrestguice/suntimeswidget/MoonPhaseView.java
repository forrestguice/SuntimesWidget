/**
    Copyright (C) 2018 Forrest Guice
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
package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.AppSettings;

@SuppressWarnings("Convert2Diamond")
public class MoonPhaseView extends LinearLayout
{
    private SuntimesUtils utils = new SuntimesUtils();
    private boolean isRtl = false;
    private boolean centered = false;

    private View content;
    private TextView empty;

    public MoonPhaseView(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonPhaseView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        /**TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EquinoxView, 0, 0);
        try {
            setMinimized(a.getBoolean(R.styleable.EquinoxView_minimized, false));
        } finally {
            a.recycle();
        }*/
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        initColors(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonphase, this, true);

        if (attrs != null)
        {
            LinearLayout.LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        content = findViewById(R.id.moonphase_layout);
        empty = (TextView)findViewById(R.id.txt_empty);

        if (isInEditMode())
        {
            updateViews(context, null);
        }
    }

    /**private View.OnTouchListener createButtonListener(final ImageButton button)
    {
        return new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (button != null)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        button.setColorFilter(ContextCompat.getColor(getContext(), R.color.btn_tint_pressed));
                        performClick();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        button.setColorFilter(null);
                    }
                }
                return false;
            }
        };
    }*/

    private int noteColor;
    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary }; //, R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        content.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews( Context context, SuntimesMoonData data )
    {
        if (isInEditMode())
        {
            return;
        }

        if (data == null)
        {
            return;
        }

        if (data.isCalculated())
        {
            // TODO

        } else {
            showEmptyView(true);
        }
    }

    public boolean saveState(Bundle bundle)
    {
        /**boolean cardIsNextYear = (flipper.getDisplayedChild() != 0);
        bundle.putBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, cardIsNextYear);
        bundle.putBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        bundle.putBoolean(EquinoxView.KEY_UI_MINIMIZED, minimized);
        Log.d("DEBUG", "EquinoxView saveState :: nextyear:" + cardIsNextYear + " :: swapped:" + userSwappedCard + " :: minimized:" + minimized);
        return true;*/
        return true;
    }

    public void loadState(Bundle bundle)
    {
        /**boolean cardIsNextYear = bundle.getBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, false);
        flipper.setDisplayedChild((cardIsNextYear ? 1 : 0));
        userSwappedCard = bundle.getBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, false);
        minimized = bundle.getBoolean(EquinoxView.KEY_UI_MINIMIZED, minimized);
        Log.d("DEBUG", "EquinoxView loadState :: nextyear: " + cardIsNextYear + " :: swapped:" + userSwappedCard + " :: minimized:" + minimized);*/
    }

    public void setOnClickListener( View.OnClickListener listener )
    {
        content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( View.OnLongClickListener listener)
    {
        content.setOnLongClickListener(listener);
    }

}
