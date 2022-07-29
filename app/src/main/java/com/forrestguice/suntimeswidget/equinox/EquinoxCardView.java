/**
    Copyright (C) 2022 Forrest Guice
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
package com.forrestguice.suntimeswidget.equinox;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.cards.CardAdapter;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

@SuppressWarnings("Convert2Diamond")
public class EquinoxCardView extends LinearLayout
{
    protected EquinoxViewOptions options = new EquinoxViewOptions();

    protected boolean userSwappedCard = false;

    protected TextView empty;
    private RecyclerView card_view;
    private LinearLayoutManager card_layout;
    private EquinoxDataAdapter card_adapter;
    private CardAdapter.CardViewScroller card_scroller;

    public EquinoxCardView(Context context) {
        super(context);
    }

    public EquinoxCardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EquinoxView, 0, 0);
        try {
            setMinimized(a.getBoolean(R.styleable.EquinoxView_minimized, false));
        } finally {
            a.recycle();
        }
    }

    public int getLayouResID() {
        return R.layout.layout_view_equinox1;
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        options.init(context);
        LayoutInflater.from(context).inflate(getLayouResID(), this, true);

        if (attrs != null)
        {
            LinearLayout.LayoutParams lp = generateLayoutParams(attrs);
            options.centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        empty = (TextView)findViewById(R.id.txt_empty);
        initCardView(context);

        if (isInEditMode()) {
            updateViews(context);
        }
        themeViews(context);
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        options.isRtl = AppSettings.isLocaleRtl(context);
    }

    protected void initCardView(Context context)
    {
        card_view = (RecyclerView)findViewById(R.id.info_equinoxsolstice_flipper1);
        card_view.setHasFixedSize(true);
        card_view.setItemViewCacheSize(7);
        card_view.addItemDecoration(new CardViewDecorator(context));

        card_scroller = new CardAdapter.CardViewScroller(context);
        card_view.setOnScrollListener(onCardScrollListener);
        card_view.setLayoutFrozen(false);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(card_view);

        initAdapter(context);
    }

    public void initAdapter(Context context)
    {
        boolean southernHemisphere = (WidgetSettings.loadLocalizeHemispherePref(context, 0)) && (WidgetSettings.loadLocationPref(context, 0).getLatitudeAsDouble() < 0);
        WidgetSettings.SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context) ? WidgetSettings.SolsticeEquinoxMode.values(southernHemisphere)
                                                                                                   : WidgetSettings.SolsticeEquinoxMode.partialValues(southernHemisphere);
        options.highlightPosition = -1;

        card_adapter = new EquinoxDataAdapter(context, modes, options);
        card_adapter.setAdapterListener(cardListener);
        card_view.setAdapter(card_adapter);

        card_layout = new LinearLayoutManager(context, HORIZONTAL, false);
        card_view.setLayoutManager(card_layout);
        card_view.scrollToPosition(EquinoxDatasetAdapter.CENTER_POSITION + modes.length);

        ViewGroup.LayoutParams params = card_view.getLayoutParams();
        params.height = (int)Math.ceil(context.getResources().getDimension(R.dimen.equinoxItem_height)) + 2;
        card_view.setLayoutParams(params);
    }

    public void updateViews(Context context) {
        SuntimesEquinoxSolsticeData data = card_adapter.initData(context, EquinoxDataAdapter.CENTER_POSITION);
        updateViews(context, data);

        int position = card_adapter.highlightNote(context);
        if (position != -1 && !userSwappedCard) {
            card_view.setLayoutFrozen(false);
            card_view.scrollToPosition(position);
            Log.d("DEBUG", "scroll to " + position);
            card_view.setLayoutFrozen(false);
        }
        Log.d("DEBUG", "EquinoxDialog updated");
    }

    protected void updateViews(Context context, SuntimesEquinoxSolsticeData data) {
        showEmptyView(data == null || !data.isImplemented());
    }

    protected void themeViews(Context context) {}

    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null)
        {
            options.init(theme);
            card_adapter.setThemeOverride(theme);
        }
    }

    public boolean isImplemented(Context context) {
        SuntimesEquinoxSolsticeData data = card_adapter.initData(context, EquinoxDataAdapter.CENTER_POSITION);
        return (data != null && data.isImplemented());
    }

    public boolean saveState(Bundle bundle)
    {
        bundle.putInt("currentCardPosition", currentCardPosition());
        bundle.putBoolean("userSwappedCard", userSwappedCard);
        bundle.putBoolean("minimized", options.minimized);
        return true;
    }

    public void loadState(Bundle bundle)
    {
        userSwappedCard = bundle.getBoolean("userSwappedCard", false);
        options.minimized = bundle.getBoolean("minimized", options.minimized);

        int cardPosition = bundle.getInt("currentCardPosition", EquinoxDataAdapter.CENTER_POSITION);
        if (cardPosition == RecyclerView.NO_POSITION) {
            cardPosition = EquinoxDataAdapter.CENTER_POSITION;
        }
        card_view.scrollToPosition(cardPosition);
        card_view.smoothScrollBy(1, 0);  // triggers a snap
    }

    public int currentCardPosition() {
        return card_layout.findFirstVisibleItemPosition();
    }

    private EquinoxAdapterListener cardListener = new EquinoxAdapterListener()
    {
        @Override
        public void onClick( int position ) {
            if (onClickListener != null) {
                onClickListener.onClick(EquinoxCardView.this);
            }
        }
        @Override public boolean onLongClick( int position ) {
            return false;
        }
        @Override public void onTitleClick( int position ) {}
        @Override public void onNextClick( int position ) {}
        @Override public void onPrevClick( int position ) {}
        @Override public void onMenuClick(View view, int position, WidgetSettings.SolsticeEquinoxMode mode, long datetime) {}
    };

    private RecyclerView.OnScrollListener onCardScrollListener = new RecyclerView.OnScrollListener()
    {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            int position = currentCardPosition();
            if (position >= 0) {
                updateViews(getContext(), card_adapter.initData(getContext(), position));
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState ==  RecyclerView.SCROLL_STATE_DRAGGING) {
                userSwappedCard = true;
            }
        }
    };

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        card_view.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void adjustColumnWidth(int columnWidthPx)
    {
        options.columnWidthPx = columnWidthPx;
        card_adapter.notifyDataSetChanged();
    }

    /**
     * CardViewDecorator
     */
    public static class CardViewDecorator extends RecyclerView.ItemDecoration
    {
        public CardViewDecorator( Context context ) {
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = outRect.right = outRect.top = outRect.bottom = 0;
        }
    }

    /**
     * setTrackingMode
     * @param mode
     */
    public void setTrackingMode(WidgetSettings.TrackingMode mode) {
        options.trackingMode = mode;
    }
    public WidgetSettings.TrackingMode getTrackingMode() {
        return options.trackingMode;
    }

    /**
     * setMinimized
     * @param value
     */
    public void setMinimized( boolean value ) {
        options.minimized = value;
    }
    public boolean isMinimized() {
        return options.minimized;
    }


    @Override
    public void setOnClickListener(View.OnClickListener l) {
        super.setOnClickListener(l);
        onClickListener = l;
    }
    protected View.OnClickListener onClickListener = null;

}
