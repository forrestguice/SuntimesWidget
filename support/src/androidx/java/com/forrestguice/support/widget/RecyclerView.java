package com.forrestguice.support.widget;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.View;

public class RecyclerView extends androidx.appcompat.widget.RecyclerView
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

    public static void setChangeDuration(androidx.appcompat.widget.RecyclerView recyclerView, long duration)
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
            public void onScrollStateChanged(@NonNull androidx.appcompat.widget.RecyclerView recyclerView, int newState) {
                listener.onScrollStateChanged(RecyclerView.this, newState);
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull androidx.appcompat.widget.RecyclerView recyclerView, int dx, int dy) {
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

    /**
     * MarginDecorator : ItemDecoration
     */
    public static class MarginDecorator extends androidx.appcompat.widget.RecyclerView.ItemDecoration
    {
        private final int[] marginPx = new int[4];

        public MarginDecorator(int px) {
            this(px, px, px, px);
        }
        public MarginDecorator(int leftPx, int topPx, int rightPx, int bottomPx )
        {
            marginPx[0] = leftPx;
            marginPx[1] = topPx;
            marginPx[2] = rightPx;
            marginPx[3] = bottomPx;
        }
        public MarginDecorator( Context context, int dimenResId ) {
            this(context, dimenResId, dimenResId, dimenResId, dimenResId);
        }
        public MarginDecorator( Context context, int dimenResId_left, int dimenResId_top, int dimenResId_right, int dimenResId_bottom )
        {
            marginPx[0] = (context != null && dimenResId_left != 0 ? (int) context.getResources().getDimension(dimenResId_left) : 0);
            marginPx[1] = (context != null && dimenResId_top != 0 ? (int) context.getResources().getDimension(dimenResId_top) : 0);
            marginPx[2] = (context != null && dimenResId_right != 0 ? (int) context.getResources().getDimension(dimenResId_right) : 0);
            marginPx[3] = (context != null && dimenResId_bottom != 0 ? (int) context.getResources().getDimension(dimenResId_bottom) : 0);
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull androidx.appcompat.widget.RecyclerView parent, @NonNull androidx.appcompat.widget.RecyclerView.State state)
        {
            outRect.left = marginPx[0];
            outRect.top = marginPx[1];
            outRect.right = marginPx[2];
            outRect.bottom = marginPx[3];
        }
    }

    /**
     * LastItemBottomMarginDecorator : ItemDecoration
     */
    public static class LastItemBottomMarginDecorator extends androidx.appcompat.widget.RecyclerView.ItemDecoration
    {
        private final androidx.appcompat.widget.RecyclerView.Adapter<?> adapter;
        private final int marginPx;

        public LastItemBottomMarginDecorator(Context context, androidx.appcompat.widget.RecyclerView.Adapter<?> adapter, int dimenResId)
        {
            this.adapter = adapter;
            this.marginPx = ((dimenResId != 0) ? (int) context.getResources().getDimension(dimenResId) : 0);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, androidx.appcompat.widget.RecyclerView parent, @NonNull androidx.appcompat.widget.RecyclerView.State state)
        {
            int position = parent.getChildAdapterPosition(view);
            if (position == adapter.getItemCount() - 1) {
                outRect.bottom = marginPx;
            } else {
                super.getItemOffsets(outRect, view, parent, state);
            }
        }
    }

}