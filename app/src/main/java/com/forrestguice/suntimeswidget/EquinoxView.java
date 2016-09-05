/**
    Copyright (C) 2014 Forrest Guice
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
package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;

public class EquinoxView extends LinearLayout
{
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedCard";
    public static final String KEY_UI_CARDISNEXTYEAR = "cardIsNextYear";

    private SuntimesUtils utils = new SuntimesUtils();
    private boolean userSwappedCard = false;
    private boolean isRtl = false;

    private ViewFlipper flipper;           // flip between

    private TextView txt_equinox_vernal;       // this year
    private TextView txt_solstice_summer;
    private TextView txt_equinox_autumnal;
    private TextView txt_solstice_winter;

    private TextView txt_equinox_vernal2;      // and next year
    private TextView txt_solstice_summer2;
    private TextView txt_equinox_autumnal2;
    private TextView txt_solstice_winter2;

    private Animation anim_card_outNext, anim_card_inNext, anim_card_outPrev, anim_card_inPrev;

    public EquinoxView(Context context)
    {
        super(context);
        init(context);
    }

    public EquinoxView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    public EquinoxView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        initLocale(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_equinox, this, true);

        flipper = (ViewFlipper)findViewById(R.id.info_equinoxsolstice_flipper);
        flipper.setOnTouchListener(cardTouchListener);

        LinearLayout thisYear = (LinearLayout)findViewById(R.id.info_equinoxsolstice_thisyear);
        if (thisYear != null)
        {
            txt_equinox_vernal = (TextView) thisYear.findViewById(R.id.text_date_equinox_vernal);
            txt_solstice_summer = (TextView) thisYear.findViewById(R.id.text_date_solstice_summer);
            txt_equinox_autumnal = (TextView) thisYear.findViewById(R.id.text_date_equinox_autumnal);
            txt_solstice_winter = (TextView) thisYear.findViewById(R.id.text_date_solstice_winter);
        }

        LinearLayout nextYear = (LinearLayout)findViewById(R.id.info_equinoxsolstice_nextyear);
        if (nextYear != null)
        {
            txt_equinox_vernal2 = (TextView) nextYear.findViewById(R.id.text_date_equinox_vernal);
            txt_solstice_summer2 = (TextView) nextYear.findViewById(R.id.text_date_solstice_summer);
            txt_equinox_autumnal2 = (TextView) nextYear.findViewById(R.id.text_date_equinox_autumnal);
            txt_solstice_winter2 = (TextView) nextYear.findViewById(R.id.text_date_solstice_winter);
        }
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
        initAnimations(context);
    }

    private void initAnimations(Context context)
    {
        anim_card_inNext = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        anim_card_inPrev = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        anim_card_outNext = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        anim_card_outPrev = AnimationUtils.loadAnimation(context, R.anim.fade_out);
    }

    protected void updateViews( Context context, SuntimesEquinoxSolsticeDataset data )
    {
        if (data != null && data.isCalculated())
        {
            SuntimesUtils.TimeDisplayText equinoxString_vernal = utils.calendarDateTimeDisplayString(context, data.dataEquinoxVernal.eventCalendarThisYear());
            SuntimesUtils.TimeDisplayText equinoxString_autumnal = utils.calendarDateTimeDisplayString(context, data.dataEquinoxAutumnal.eventCalendarThisYear());
            SuntimesUtils.TimeDisplayText solsticeString_summer = utils.calendarDateTimeDisplayString(context, data.dataSolsticeSummer.eventCalendarThisYear());
            SuntimesUtils.TimeDisplayText solsticeString_winter = utils.calendarDateTimeDisplayString(context, data.dataSolsticeWinter.eventCalendarThisYear());

            txt_equinox_vernal.setText(equinoxString_vernal.toString());
            txt_solstice_summer.setText(solsticeString_summer.toString());
            txt_equinox_autumnal.setText(equinoxString_autumnal.toString());
            txt_solstice_winter.setText(solsticeString_winter.toString());

            SuntimesUtils.TimeDisplayText equinoxString_vernal2 = utils.calendarDateTimeDisplayString(context, data.dataEquinoxVernal.eventCalendarOtherYear());
            SuntimesUtils.TimeDisplayText equinoxString_autumnal2 = utils.calendarDateTimeDisplayString(context, data.dataEquinoxAutumnal.eventCalendarOtherYear());
            SuntimesUtils.TimeDisplayText solsticeString_summer2 = utils.calendarDateTimeDisplayString(context, data.dataSolsticeSummer.eventCalendarOtherYear());
            SuntimesUtils.TimeDisplayText solsticeString_winter2 = utils.calendarDateTimeDisplayString(context, data.dataSolsticeWinter.eventCalendarOtherYear());

            txt_equinox_vernal2.setText(equinoxString_vernal2.toString());
            txt_solstice_summer2.setText(solsticeString_summer2.toString());
            txt_equinox_autumnal2.setText(equinoxString_autumnal2.toString());
            txt_solstice_winter2.setText(solsticeString_winter2.toString());

        } else {
            String notCalculated = context.getString(R.string.time_loading);
            txt_equinox_vernal.setText(notCalculated);
            txt_solstice_summer.setText(notCalculated);
            txt_equinox_autumnal.setText(notCalculated);
            txt_solstice_winter.setText(notCalculated);

            txt_equinox_vernal2.setText(notCalculated);
            txt_solstice_summer2.setText(notCalculated);
            txt_equinox_autumnal2.setText(notCalculated);
            txt_solstice_winter2.setText(notCalculated);
        }
    }

    public boolean saveState(Bundle bundle)
    {
        boolean cardIsNextYear = (flipper.getDisplayedChild() != 0);
        Log.d("DEBUG", "EquinoxView saveState");
        bundle.putBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, cardIsNextYear);
        bundle.putBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        return true;
    }

    public void loadState(Bundle bundle)
    {
        Log.d("DEBUG", "EquinoxView loadState");
        boolean cardIsNextYear = bundle.getBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, false);
        flipper.setDisplayedChild((cardIsNextYear ? 1 : 0));
        userSwappedCard = bundle.getBoolean(KEY_UI_USERSWAPPEDCARD, false);
    }

    /**
     * @return
     */
    public boolean showNextCard()
    {
        if (hasNextCard())
        {
            flipper.setOutAnimation(anim_card_outNext);
            flipper.setInAnimation(anim_card_inNext);
            flipper.showNext();
            return true;
        }
        return false;
    }

    public boolean hasNextCard()
    {
        int current = flipper.getDisplayedChild();
        return ((current + 1) < flipper.getChildCount());
    }


    /**
     * @return
     */
    public boolean showPreviousCard()
    {
        if (hasPreviousCard())
        {
            flipper.setOutAnimation(anim_card_outPrev);
            flipper.setInAnimation(anim_card_inPrev);
            flipper.showPrevious();
            return true;
        }
        return false;
    }

    public boolean hasPreviousCard()
    {
        int current = flipper.getDisplayedChild();
        int prev = current - 1;
        return (prev >= 0);
    }

    /**
     *
     */
    private View.OnTouchListener cardTouchListener = new View.OnTouchListener()
    {
        public int MOVE_SENSITIVITY = 150;
        public int FLING_SENSITIVITY = 25;
        public float firstTouchX, secondTouchX;

        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    firstTouchX = event.getX();
                    break;

                case MotionEvent.ACTION_UP:
                    secondTouchX = event.getX();
                    if ((secondTouchX - firstTouchX) > FLING_SENSITIVITY)
                    {   // swipe right; back to previous view
                        userSwappedCard = (isRtl ? showNextCard() : showPreviousCard());

                    } else if (firstTouchX - secondTouchX > FLING_SENSITIVITY) {
                        // swipe left; advance to next view
                        userSwappedCard = (isRtl ? showPreviousCard() : showNextCard());

                    } else {
                        // swipe cancel; reset current view
                        final View currentView = flipper.getCurrentView();
                        currentView.layout(0, currentView.getTop(), currentView.getWidth(), currentView.getBottom());
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    float currentTouchX = event.getX();
                    int moveDelta = (int)(currentTouchX - firstTouchX);
                    boolean isSwipeRight = (moveDelta > 0);

                    final View currentView = flipper.getCurrentView();
                    int currentIndex = flipper.getDisplayedChild();

                    int otherIndex;
                    if (isRtl)
                    {
                        otherIndex = (isSwipeRight ? currentIndex + 1 : currentIndex - 1);
                    } else {
                        otherIndex = (isSwipeRight ? currentIndex - 1 : currentIndex + 1);
                    }

                    if (otherIndex >= 0 && otherIndex < flipper.getChildCount())
                    {
                        // in-between child views; flip between them
                        currentView.layout( moveDelta, currentView.getTop(),
                                moveDelta + currentView.getWidth(), currentView.getBottom() );

                        // extended movement; manually trigger swipe/fling
                        if (moveDelta > MOVE_SENSITIVITY || moveDelta < MOVE_SENSITIVITY * -1)
                        {
                            event.setAction(MotionEvent.ACTION_UP);
                            return onTouch(view, event);
                        }

                    } else {
                        // at-a-boundary (the first/last view);
                        // TODO: animate somehow to let user know there aren't additional views
                    }

                    break;
            }

            return true;
        }
    };
}
