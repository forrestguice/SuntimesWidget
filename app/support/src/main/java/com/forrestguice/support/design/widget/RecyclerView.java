package com.forrestguice.support.design.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class RecyclerView extends android.support.v7.widget.RecyclerView implements android.support.v4.view.ScrollingView, android.support.v4.view.NestedScrollingChild2
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

    /**
     * OnScrollListener
     */
    public static abstract class OnScrollListener
    {
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {}
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {}
    }

    @java.lang.Deprecated
    public void setOnScrollListener(OnScrollListener listener) {
        setOnScrollListener(createOnScrollListener(this, listener));
    }
    public void addOnScrollListener(OnScrollListener listener) {
        addOnScrollListener(createOnScrollListener(this, listener));
    }

    public static android.support.v7.widget.RecyclerView.OnScrollListener createOnScrollListener(final RecyclerView view, final OnScrollListener listener)
    {
        return new android.support.v7.widget.RecyclerView.OnScrollListener()
        {
            public void onScrollStateChanged(android.support.v7.widget.RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                listener.onScrollStateChanged(view, newState);
            }
            public void onScrolled(android.support.v7.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                listener.onScrolled(view, dx, dy);
            }
        };
    }

}
