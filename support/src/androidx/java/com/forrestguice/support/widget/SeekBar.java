package com.forrestguice.support.widget;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.util.AttributeSet;

public class SeekBar extends AppCompatSeekBar
{
    public SeekBar(Context context) {
        super(context);
    }

    public SeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}