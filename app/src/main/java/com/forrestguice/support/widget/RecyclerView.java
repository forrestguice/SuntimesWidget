package com.forrestguice.support.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;

public class RecyclerView extends android.support.v7.widget.RecyclerView
{
    public RecyclerView(@NonNull Context context) {
        super(context);
    }

    public RecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static void setChangeDuration(android.support.v7.widget.RecyclerView recyclerView, long duration)
    {
        SimpleItemAnimator animator = (SimpleItemAnimator) recyclerView.getItemAnimator();
        if (animator != null) {
            animator.setChangeDuration(duration);
        }
    }

    public void addOnScrollListener(@NonNull OnScrollListenerCompat listener) {
        addOnScrollListener(from(listener));
    }
    public void setOnScrollListener(@Nullable final OnScrollListenerCompat listener) {
        setOnScrollListener(from(listener));
    }

    public OnScrollListener from(@Nullable final OnScrollListenerCompat listener)
    {
        return new OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull android.support.v7.widget.RecyclerView recyclerView, int newState) {
                listener.onScrollStateChanged(RecyclerView.this, newState);
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull android.support.v7.widget.RecyclerView recyclerView, int dx, int dy) {
                listener.onScrolled(RecyclerView.this, dx, dy);
                super.onScrolled(recyclerView, dx, dy);
            }
        };
    }

    public static abstract class OnScrollListenerCompat
    {
        public OnScrollListenerCompat() {}
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {}
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {}
    }
}