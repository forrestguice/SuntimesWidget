/**
    Copyright (C) 2014 Forrest Guice
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

package com.forrestguice.suntimeswidget.cards;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * A custom ScrollView that passes its touch events to its parent when unable to scroll (child view is
 * smaller than parent), or when flag allowScroll is set to false.
 */
public class CardScroll extends ScrollView
{
    public CardScroll(Context context)
    {
        super(context);
    }

    public CardScroll(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * property: allow the view to scroll
     */
    private boolean allowScroll = true;
    public boolean getAllowScroll()
    {
        return allowScroll;
    }
    public void setAllowScroll( boolean value )
    {
        allowScroll = value;
    }

    /**
     * Allow scrolling if possible (and flag is set), otherwise allow touchEvent to propagate to parent
     * @param e a MotionEvent
     * @return false touch event not consumed (pass it on), true touch was handled by view
     */
    @Override
    public boolean dispatchTouchEvent( @NonNull MotionEvent e )
    {
        if (allowScroll && ableToScrollVertical())
        {
            return super.dispatchTouchEvent(e);

        } else {
            View childView = getChildAt(0);
            return ((childView != null) && childView.dispatchTouchEvent(e));
        }
    }

    /**
     * see http://stackoverflow.com/questions/18572790/check-if-scrollview-is-higher-than-screen-scrollable/18574328#18574328
     * @return true view can scroll since content is larger than view, false content smaller than view
     */
    private boolean ableToScrollVertical()
    {
        View childView = getChildAt(0);
        if (childView != null)
        {
            int contentHeight = childView.getHeight() + getPaddingTop() + getPaddingBottom();
            return (getHeight() < contentHeight);
        }
        return false;
    }
}
