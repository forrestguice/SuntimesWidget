package com.forrestguice.support.widget;

import android.content.Context;
import android.util.AttributeSet;

public class LinearLayoutManager extends androidx.appcompat.widget.LinearLayoutManager
{
    public LinearLayoutManager(Context context) {
        super(context);
    }

    public LinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public LinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}