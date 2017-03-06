/**
    Copyright (C) 2017 Forrest Guice
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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;

import java.util.ArrayList;
import java.util.Calendar;

public class EquinoxView extends LinearLayout
{
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedCard";
    public static final String KEY_UI_CARDISNEXTYEAR = "cardIsNextYear";
    public static final String KEY_UI_MINIMIZED = "isMinimized";

    private SuntimesUtils utils = new SuntimesUtils();
    private boolean userSwappedCard = false;
    private boolean isRtl = false;
    private boolean minimized = false;
    private boolean centered = false;

    private ViewFlipper flipper;           // flip between
    private Animation anim_card_outNext, anim_card_inNext, anim_card_outPrev, anim_card_inPrev;
    private ImageView btn_flipperNext_thisYear, btn_flipperPrev_thisYear;
    private ImageView btn_flipperNext_nextYear, btn_flipperPrev_nextYear;

    private TextView titleThisYear, titleNextYear;

    private EquinoxNote note_equinox_vernal, note_solstice_summer, note_equinox_autumnal, note_solstice_winter;  // this year
    private EquinoxNote note_equinox_vernal2, note_solstice_summer2, note_equinox_autumnal2, note_solstice_winter2;  // and next year
    private ArrayList<EquinoxNote> notes;

    public EquinoxView(Context context)
    {
        super(context);
        init(context, null);
    }

    public EquinoxView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public EquinoxView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        initColors(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_equinox, this, true);

        if (attrs != null)
        {
            LinearLayout.LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        flipper = (ViewFlipper)findViewById(R.id.info_equinoxsolstice_flipper);
        flipper.setOnTouchListener(cardTouchListener);

        notes = new ArrayList<EquinoxNote>();

        RelativeLayout thisYear = (RelativeLayout)findViewById(R.id.info_equinoxsolstice_thisyear);
        if (thisYear != null)
        {
            btn_flipperNext_thisYear = (ImageButton)thisYear.findViewById(R.id.info_time_nextbtn);
            btn_flipperNext_thisYear.setOnClickListener(onNextCardClick);
            btn_flipperNext_thisYear.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        btn_flipperNext_thisYear.setColorFilter(ContextCompat.getColor(getContext(), R.color.btn_tint_pressed));
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        btn_flipperNext_thisYear.setColorFilter(null);
                    }
                    return false;
                }
            });

            btn_flipperPrev_thisYear = (ImageButton)thisYear.findViewById(R.id.info_time_prevbtn);
            btn_flipperPrev_thisYear.setVisibility(View.GONE);

            titleThisYear = (TextView) thisYear.findViewById(R.id.text_title);

            TextView txt_equinox_vernal_label = (TextView) thisYear.findViewById(R.id.text_date_equinox_vernal_label);
            TextView txt_equinox_vernal = (TextView) thisYear.findViewById(R.id.text_date_equinox_vernal);
            TextView txt_equinox_vernal_note = (TextView) thisYear.findViewById(R.id.text_date_equinox_vernal_note);
            note_equinox_vernal = addNote(txt_equinox_vernal_label, txt_equinox_vernal, txt_equinox_vernal_note);

            TextView txt_solstice_summer_label = (TextView) thisYear.findViewById(R.id.text_date_solstice_summer_label);
            TextView txt_solstice_summer = (TextView) thisYear.findViewById(R.id.text_date_solstice_summer);
            TextView txt_solstice_summer_note = (TextView) thisYear.findViewById(R.id.text_date_solstice_summer_note);
            note_solstice_summer = addNote(txt_solstice_summer_label, txt_solstice_summer, txt_solstice_summer_note);

            TextView txt_equinox_autumnal_label = (TextView) thisYear.findViewById(R.id.text_date_equinox_autumnal_label);
            TextView txt_equinox_autumnal = (TextView) thisYear.findViewById(R.id.text_date_equinox_autumnal);
            TextView txt_equinox_autumnal_note = (TextView) thisYear.findViewById(R.id.text_date_equinox_autumnal_note);
            note_equinox_autumnal = addNote(txt_equinox_autumnal_label, txt_equinox_autumnal, txt_equinox_autumnal_note);

            TextView txt_solstice_winter_label = (TextView) thisYear.findViewById(R.id.text_date_solstice_winter_label);
            TextView txt_solstice_winter = (TextView) thisYear.findViewById(R.id.text_date_solstice_winter);
            TextView txt_solstice_winter_note = (TextView) thisYear.findViewById(R.id.text_date_solstice_winter_note);
            note_solstice_winter = addNote(txt_solstice_winter_label, txt_solstice_winter, txt_solstice_winter_note);

            if (centered)
            {
                FrameLayout.LayoutParams lpThisYear = (FrameLayout.LayoutParams)thisYear.getLayoutParams();
                lpThisYear.gravity = Gravity.CENTER_HORIZONTAL;
                thisYear.setLayoutParams(lpThisYear);
            }
        }

        RelativeLayout nextYear = (RelativeLayout)findViewById(R.id.info_equinoxsolstice_nextyear);
        if (nextYear != null)
        {
            btn_flipperNext_nextYear = (ImageButton)nextYear.findViewById(R.id.info_time_nextbtn);
            btn_flipperNext_nextYear.setVisibility(View.GONE);

            btn_flipperPrev_nextYear = (ImageButton)nextYear.findViewById(R.id.info_time_prevbtn);
            btn_flipperPrev_nextYear.setOnClickListener(onPrevCardClick);
            btn_flipperPrev_nextYear.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        btn_flipperPrev_nextYear.setColorFilter(ContextCompat.getColor(getContext(), R.color.btn_tint_pressed));
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        btn_flipperPrev_nextYear.setColorFilter(null);
                    }
                    return false;
                }
            });

            titleNextYear = (TextView) nextYear.findViewById(R.id.text_title);

            TextView txt_equinox_vernal2_label = (TextView) nextYear.findViewById(R.id.text_date_equinox_vernal_label);
            TextView txt_equinox_vernal2 = (TextView) nextYear.findViewById(R.id.text_date_equinox_vernal);
            TextView txt_equinox_vernal2_note = (TextView) nextYear.findViewById(R.id.text_date_equinox_vernal_note);
            note_equinox_vernal2 = addNote(txt_equinox_vernal2_label, txt_equinox_vernal2, txt_equinox_vernal2_note);

            TextView txt_solstice_summer2_label = (TextView) nextYear.findViewById(R.id.text_date_solstice_summer_label);
            TextView txt_solstice_summer2 = (TextView) nextYear.findViewById(R.id.text_date_solstice_summer);
            TextView txt_solstice_summer2_note = (TextView) nextYear.findViewById(R.id.text_date_solstice_summer_note);
            note_solstice_summer2 = addNote(txt_solstice_summer2_label, txt_solstice_summer2, txt_solstice_summer2_note);

            TextView txt_equinox_autumnal2_label = (TextView) nextYear.findViewById(R.id.text_date_equinox_autumnal_label);
            TextView txt_equinox_autumnal2 = (TextView) nextYear.findViewById(R.id.text_date_equinox_autumnal);
            TextView txt_equinox_autumnal2_note = (TextView) nextYear.findViewById(R.id.text_date_equinox_autumnal_note);
            note_equinox_autumnal2 = addNote(txt_equinox_autumnal2_label, txt_equinox_autumnal2, txt_equinox_autumnal2_note);

            TextView txt_solstice_winter2_label = (TextView) nextYear.findViewById(R.id.text_date_solstice_winter_label);
            TextView txt_solstice_winter2 = (TextView) nextYear.findViewById(R.id.text_date_solstice_winter);
            TextView txt_solstice_winter2_note = (TextView) nextYear.findViewById(R.id.text_date_solstice_winter_note);
            note_solstice_winter2 = addNote(txt_solstice_winter2_label, txt_solstice_winter2, txt_solstice_winter2_note);

            if (centered)
            {
                FrameLayout.LayoutParams lpNextYear = (FrameLayout.LayoutParams)nextYear.getLayoutParams();
                lpNextYear.gravity = Gravity.CENTER_HORIZONTAL;
                nextYear.setLayoutParams(lpNextYear);
            }
        }
    }

    private int noteColor, springColor, summerColor, fallColor, winterColor;

    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.color_transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        springColor = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        summerColor = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        fallColor = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        winterColor = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        typedArray.recycle();
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

    private EquinoxNote addNote(TextView labelView, TextView timeView, TextView noteView)
    {
        EquinoxNote note = new EquinoxNote(labelView, timeView, noteView);
        notes.add(note);
        return note;
    }

    public void setMinimized( boolean value )
    {
        this.minimized = value;
    }

    private EquinoxNote findNextNote(Calendar now)
    {
        if (notes == null || now == null)
        {
            return null;
        }

        EquinoxNote soonest = null;
        long timeDeltaMin = Integer.MAX_VALUE;
        for (EquinoxNote note : notes)
        {
            Calendar noteTime = note.getTime();
            if (noteTime != null)
            {
                long timeDelta = noteTime.getTimeInMillis() - now.getTimeInMillis();
                if (timeDelta < timeDeltaMin)
                {
                    timeDeltaMin = timeDelta;
                    soonest = note;
                }
            }
        }
        return soonest;
    }

    private void showNextPrevButtons( boolean show )
    {
        if (show)
        {
            btn_flipperNext_thisYear.setVisibility(View.VISIBLE);
            btn_flipperPrev_thisYear.setVisibility(View.GONE);
            btn_flipperNext_nextYear.setVisibility(View.GONE);
            btn_flipperPrev_nextYear.setVisibility(View.VISIBLE);

        } else {
            btn_flipperNext_thisYear.setVisibility(View.GONE);
            btn_flipperPrev_thisYear.setVisibility(View.GONE);
            btn_flipperNext_nextYear.setVisibility(View.GONE);
            btn_flipperPrev_nextYear.setVisibility(View.GONE);
        }
    }

    protected void updateViews( Context context, SuntimesEquinoxSolsticeDataset data )
    {
        showNextPrevButtons(!minimized);

        if (data != null && data.isCalculated())
        {
            SuntimesUtils.TimeDisplayText thisYear = utils.calendarDateYearDisplayString(context, data.dataEquinoxVernal.eventCalendarThisYear());
            titleThisYear.setVisibility(minimized ? View.GONE : View.VISIBLE);
            titleThisYear.setText(thisYear.toString());

            SuntimesUtils.TimeDisplayText nextYear = utils.calendarDateYearDisplayString(context, data.dataEquinoxVernal.eventCalendarOtherYear());
            titleNextYear.setVisibility( minimized ? View.GONE : View.VISIBLE);
            titleNextYear.setText(nextYear.toString());

            note_equinox_vernal.updateTime(context, data.dataEquinoxVernal.eventCalendarThisYear());
            note_equinox_autumnal.updateTime(context, data.dataEquinoxAutumnal.eventCalendarThisYear());
            note_solstice_summer.updateTime(context, data.dataSolsticeSummer.eventCalendarThisYear());
            note_solstice_winter.updateTime(context, data.dataSolsticeWinter.eventCalendarThisYear());

            note_equinox_vernal2.updateTime(context, data.dataEquinoxVernal.eventCalendarOtherYear());
            note_equinox_autumnal2.updateTime(context, data.dataEquinoxAutumnal.eventCalendarOtherYear());
            note_solstice_summer2.updateTime(context, data.dataSolsticeSummer.eventCalendarOtherYear());
            note_solstice_winter2.updateTime(context, data.dataSolsticeWinter.eventCalendarOtherYear());

            for (EquinoxNote note : notes)
            {
                note.updateNote(context, data.now());
                note.setVisible(!minimized);
            }

            EquinoxNote nextNote = findNextNote(data.now());
            if (nextNote != null)
            {
                if (minimized)
                {
                    nextNote.setVisible(true);

                } else {
                    nextNote.setHighlighted(true);
                }
            }

        } else {
            // no data
            for (EquinoxNote note : notes)
            {
                note.updateTime(context, null);
                note.updateNote(context, null);

                if (minimized)
                {
                    note.setVisible(false);
                }
            }
        }
    }

    public boolean saveState(Bundle bundle)
    {
        boolean cardIsNextYear = (flipper.getDisplayedChild() != 0);
        Log.d("DEBUG", "EquinoxView saveState");
        bundle.putBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, cardIsNextYear);
        bundle.putBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        bundle.putBoolean(EquinoxView.KEY_UI_MINIMIZED, minimized);
        return true;
    }

    public void loadState(Bundle bundle)
    {
        Log.d("DEBUG", "EquinoxView loadState");
        boolean cardIsNextYear = bundle.getBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, false);
        flipper.setDisplayedChild((cardIsNextYear ? 1 : 0));
        userSwappedCard = bundle.getBoolean(KEY_UI_USERSWAPPEDCARD, false);
        minimized = bundle.getBoolean(KEY_UI_MINIMIZED, minimized);
    }

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

    public void setOnClickListener( View.OnClickListener listener )
    {
        flipper.setOnClickListener(listener);
    }

    public void setOnLongClickListener( View.OnLongClickListener listener)
    {
        flipper.setOnLongClickListener(listener);
    }

    private View.OnClickListener onNextCardClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            userSwappedCard = showNextCard();
        }
    };

    private View.OnClickListener onPrevCardClick = new View.OnClickListener()
    {

        @Override
        public void onClick(View view)
        {
            userSwappedCard = showPreviousCard();
        }
    };

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
            if (minimized)
                return false;

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
                    int moveDelta = (int) (currentTouchX - firstTouchX);
                    boolean isSwipeRight = (moveDelta > 0);

                    final View currentView = flipper.getCurrentView();
                    int currentIndex = flipper.getDisplayedChild();

                    int otherIndex;
                    if (isRtl)
                    {
                        otherIndex = (isSwipeRight ? currentIndex + 1 : currentIndex - 1);
                    } else
                    {
                        otherIndex = (isSwipeRight ? currentIndex - 1 : currentIndex + 1);
                    }

                    if (otherIndex >= 0 && otherIndex < flipper.getChildCount())
                    {
                        // in-between child views; flip between them
                        currentView.layout(moveDelta, currentView.getTop(),
                                moveDelta + currentView.getWidth(), currentView.getBottom());

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

            return false;
        }
    };

    /**
     * EquinoxNote
     */
    private class EquinoxNote
    {
        protected TextView labelView, timeView, noteView;
        protected Calendar time, now;
        protected boolean highlighted;

        public EquinoxNote(TextView labelView, TextView timeView, TextView noteView)
        {
            this.labelView = labelView;
            this.timeView = timeView;
            this.noteView = noteView;
        }

        public void updateTime( Context context, Calendar time )
        {
            this.time = time;
            if (timeView != null)
            {
                SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, time);
                timeView.setText(timeText.toString());

            } else {
                String notCalculated = context.getString(R.string.time_loading);
                timeView.setText(notCalculated);
            }
        }

        public void updateNote( Context context, Calendar now )
        {
            this.now = now;
            if (noteView != null)
            {
                if (now != null && time != null)
                {
                    String noteText = utils.timeDeltaDisplayString(now.getTime(), time.getTime()).toString();

                    if (time.before(Calendar.getInstance()))
                    {
                        String noteString = context.getString(R.string.ago, noteText);
                        SpannableString noteSpan = SuntimesUtils.createBoldColorSpan(noteString, noteText, noteColor);
                        noteView.setText(noteSpan);

                    } else {
                        String noteString = context.getString(R.string.hence, noteText);
                        SpannableString noteSpan = SuntimesUtils.createBoldColorSpan(noteString, noteText, noteColor);
                        noteView.setText(noteSpan);
                    }
                } else {
                    noteView.setText("");
                }
            }
        }

        public void setHighlighted( boolean highlighted )
        {
            this.highlighted = highlighted;
            //highlight(labelView, highlighted);
            highlight(timeView, highlighted);
        }

        private void highlight( TextView view, boolean value )
        {
            if (view != null)
            {
                if (value)
                {
                    view.setTypeface(view.getTypeface(), Typeface.BOLD);
                    view.setPaintFlags(view.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                } else {
                    view.setTypeface(view.getTypeface(), Typeface.NORMAL);
                    view.setPaintFlags(view.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                }
            }
        }

        public void setVisible( boolean visible )
        {
            labelView.setVisibility( visible ? View.VISIBLE : View.GONE);
            timeView.setVisibility( visible ? View.VISIBLE : View.GONE);
            noteView.setVisibility( visible ? View.VISIBLE : View.GONE);
        }

        public Calendar getTime()
        {
            return time;
        }
    }

}
