package com.forrestguice.support.design.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

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


    /**
     * RecyclerViewInterface
     */
    public interface RecyclerViewInterface {
        android.support.v7.widget.RecyclerView get();
    }
    public static RecyclerViewInterface wrap(final android.support.v7.widget.RecyclerView view) {
        return new RecyclerViewInterface() {
            @Override
            public android.support.v7.widget.RecyclerView get() {
                return view;
            }
        };
    }

    public interface ItemDecorationInterface {
        android.support.v7.widget.RecyclerView.ItemDecoration get();
    }
    public static ItemDecorationInterface wrap(final android.support.v7.widget.RecyclerView.ItemDecoration decoration) {
        return new ItemDecorationInterface() {
            @Override
            public android.support.v7.widget.RecyclerView.ItemDecoration get() {
                return decoration;
            }
        };
    }

    /**
     * ItemDecoration
     */
    public static abstract class ItemDecoration extends android.support.v7.widget.RecyclerView.ItemDecoration
    {
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerViewInterface parent, @NonNull RecyclerView.State state) {}
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerViewInterface parent, @NonNull RecyclerView.State state) {}
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerViewInterface parent, @NonNull RecyclerView.State state) {}

        @Override
        public void onDraw(@NonNull Canvas c, @NonNull android.support.v7.widget.RecyclerView parent, @NonNull RecyclerView.State state) {
            onDraw(c, wrap(parent), state);
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull android.support.v7.widget.RecyclerView parent, @NonNull RecyclerView.State state) {
            onDrawOver(c, wrap(parent), state);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull android.support.v7.widget.RecyclerView parent, @NonNull RecyclerView.State state) {
            getItemOffsets(outRect, view, wrap(parent), state);
        }
    }
}
