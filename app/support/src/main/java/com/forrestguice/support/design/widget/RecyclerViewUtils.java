package com.forrestguice.support.design.widget;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

public class RecyclerViewUtils
{
    public static void setChangeDuration(@Nullable android.support.v7.widget.RecyclerView view, long changeDuration)
    {
        if (view != null) {
            SimpleItemAnimator animator = (SimpleItemAnimator) view.getItemAnimator();
            if (animator != null) {
                animator.setChangeDuration(changeDuration);
            }
        }
    }

    /**
     * MarginsItemDecoration
     */
    public static class MarginsItemDecoration extends android.support.v7.widget.RecyclerView.ItemDecoration
    {
        public MarginsItemDecoration() {
            init(0, 0, 0, 0);
        }
        public MarginsItemDecoration(int marginPx) {
            init(marginPx, marginPx, marginPx, marginPx);
        }
        public MarginsItemDecoration(int leftMarginPx, int topMarginPx, int rightMarginPx, int bottomMarginPx) {
            init(leftMarginPx, topMarginPx, rightMarginPx, bottomMarginPx);
        }

        protected final int[] marginPx = new int[] {0, 0, 0, 0};
        protected void init(int leftMarginPx, int topMarginPx, int rightMarginPx, int bottomMarginPx) {
            this.marginPx[0] = leftMarginPx;
            this.marginPx[1] = topMarginPx;
            this.marginPx[2] = rightMarginPx;
            this.marginPx[3] = bottomMarginPx;
        }

        protected int[] getMarginsPx() {
            return marginPx;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull android.support.v7.widget.RecyclerView parent, @NonNull android.support.v7.widget.RecyclerView.State state)
        {
            int[] marginPx = getMarginsPx();
            outRect.left = marginPx[0];
            outRect.top = marginPx[1];
            outRect.right = marginPx[2];
            outRect.bottom = marginPx[3];
        }
    }

    /**
     * PositionMarginsItemDecoration
     */
    public static abstract class PositionMarginsItemDecoration extends MarginsItemDecoration
    {
        protected abstract int getPosition();

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull android.support.v7.widget.RecyclerView parent, @NonNull android.support.v7.widget.RecyclerView.State state) {
            if (getPosition() == parent.getChildAdapterPosition(view)) {
                super.getItemOffsets(outRect, view, parent, state);
            }
        }
    }

}
