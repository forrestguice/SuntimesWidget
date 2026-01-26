package com.forrestguice.support.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class NestedScrollView extends androidx.core.widget.NestedScrollView
{
    public NestedScrollView(@NonNull Context context) {
        super(context);
    }

    public NestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}