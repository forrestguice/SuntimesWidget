/**
    Copyright (C) 2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.notes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.R;

public class NoteViewFlipper extends ViewFlipper
{
    public static final int MOVE_SENSITIVITY = 25;
    public static final int FLING_SENSITIVITY = 10;

    private boolean isRtl = false;
    public float firstTouchX, secondTouchX;

    public NoteViewFlipper(Context context) {
        super(context);
        init(context);
    }

    public NoteViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context)
    {
        isRtl = context.getResources().getBoolean(R.bool.is_rtl);
        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            { /* DO NOTHING HERE (but we still need this listener) */ }
        });
    }

    @Override
    public boolean performClick()
    {
        super.performClick();
        if (listener != null) {
            return listener.performClick();
        } else return false;
    }

    public boolean performFlingPrev()
    {
        if (listener != null) {
            return listener.performFlingPrev();
        } else return false;
    }

    public boolean performFlingNext()
    {
        if (listener != null) {
            return listener.performFlingNext();
        } else return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                firstTouchX = event.getX();
                return true;

            case MotionEvent.ACTION_UP:
                secondTouchX = event.getX();
                if ((firstTouchX - secondTouchX) >= FLING_SENSITIVITY)
                {
                    if (isRtl)
                        return performFlingPrev();
                    else return performFlingNext();

                } else if ((secondTouchX - firstTouchX) > FLING_SENSITIVITY) {
                    if (isRtl)
                        return performFlingNext();
                    else return performFlingPrev();

                } else {
                    return performClick();
                }

            case MotionEvent.ACTION_MOVE:
                final View currentView = getCurrentView();
                int moveDeltaX = (isRtl ? (int)(firstTouchX - event.getX()) : (int)(event.getX() - firstTouchX));
                if (Math.abs(moveDeltaX) < MOVE_SENSITIVITY) {
                    currentView.layout(moveDeltaX, currentView.getTop(), currentView.getWidth(), currentView.getBottom());
                }
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    /**
     * setViewFlipperListener
     * @param l listener
     */
    public void setViewFlipperListener(ViewFlipperListener l) {
        listener = l;
    }
    protected ViewFlipperListener listener = null;

    /**
     * ViewFlipperListener
     */
    public interface ViewFlipperListener
    {
        boolean performClick();
        boolean performFlingPrev();
        boolean performFlingNext();
    }

}
