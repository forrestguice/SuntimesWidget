package com.forrestguice.support.design.widget;

import android.content.Context;
import android.util.AttributeSet;

public class GridLayoutManager extends android.support.v7.widget.GridLayoutManager
{
    public GridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public GridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public GridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }
}
