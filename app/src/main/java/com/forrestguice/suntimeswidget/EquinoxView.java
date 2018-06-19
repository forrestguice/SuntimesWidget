/**
    Copyright (C) 2017-2018 Forrest Guice
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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class EquinoxView extends LinearLayout
{
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedEquinoxCard";
    public static final String KEY_UI_CARDISNEXTYEAR = "equinoxCardIsNextYear";
    public static final String KEY_UI_MINIMIZED = "equinoxIsMinimized";

    private SuntimesUtils utils = new SuntimesUtils();
    private boolean userSwappedCard = false;
    private boolean isRtl = false;
    private boolean minimized = false;
    private boolean centered = false;
    private WidgetSettings.TrackingMode trackingMode = WidgetSettings.TrackingMode.SOONEST;

    private TextView empty;
    private ViewFlipper flipper;           // flip between thisYear, nextYear
    private Animation anim_card_outNext, anim_card_inNext, anim_card_outPrev, anim_card_inPrev;
    private ImageButton btn_flipperNext_thisYear, btn_flipperPrev_thisYear;
    private ImageButton btn_flipperNext_nextYear, btn_flipperPrev_nextYear;

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

        empty = (TextView)findViewById(R.id.txt_empty);

        flipper = (ViewFlipper)findViewById(R.id.info_equinoxsolstice_flipper);
        flipper.setOnTouchListener(cardTouchListener);

        notes = new ArrayList<EquinoxNote>();

        RelativeLayout thisYear = (RelativeLayout)findViewById(R.id.info_equinoxsolstice_thisyear);
        if (thisYear != null)
        {
            btn_flipperNext_thisYear = (ImageButton)thisYear.findViewById(R.id.info_time_nextbtn);
            btn_flipperNext_thisYear.setOnClickListener(onNextCardClick);
            btn_flipperNext_thisYear.setOnTouchListener(createButtonListener(btn_flipperNext_thisYear));

            btn_flipperPrev_thisYear = (ImageButton)thisYear.findViewById(R.id.info_time_prevbtn);
            btn_flipperPrev_thisYear.setVisibility(View.GONE);

            titleThisYear = (TextView) thisYear.findViewById(R.id.text_title);

            TextView txt_equinox_vernal_label = (TextView) thisYear.findViewById(R.id.text_date_equinox_vernal_label);
            TextView txt_equinox_vernal = (TextView) thisYear.findViewById(R.id.text_date_equinox_vernal);
            TextView txt_equinox_vernal_note = (TextView) thisYear.findViewById(R.id.text_date_equinox_vernal_note);
            note_equinox_vernal = addNote(txt_equinox_vernal_label, txt_equinox_vernal, txt_equinox_vernal_note, 0 );

            TextView txt_solstice_summer_label = (TextView) thisYear.findViewById(R.id.text_date_solstice_summer_label);
            TextView txt_solstice_summer = (TextView) thisYear.findViewById(R.id.text_date_solstice_summer);
            TextView txt_solstice_summer_note = (TextView) thisYear.findViewById(R.id.text_date_solstice_summer_note);
            note_solstice_summer = addNote(txt_solstice_summer_label, txt_solstice_summer, txt_solstice_summer_note, 0);

            TextView txt_equinox_autumnal_label = (TextView) thisYear.findViewById(R.id.text_date_equinox_autumnal_label);
            TextView txt_equinox_autumnal = (TextView) thisYear.findViewById(R.id.text_date_equinox_autumnal);
            TextView txt_equinox_autumnal_note = (TextView) thisYear.findViewById(R.id.text_date_equinox_autumnal_note);
            note_equinox_autumnal = addNote(txt_equinox_autumnal_label, txt_equinox_autumnal, txt_equinox_autumnal_note, 0);

            TextView txt_solstice_winter_label = (TextView) thisYear.findViewById(R.id.text_date_solstice_winter_label);
            TextView txt_solstice_winter = (TextView) thisYear.findViewById(R.id.text_date_solstice_winter);
            TextView txt_solstice_winter_note = (TextView) thisYear.findViewById(R.id.text_date_solstice_winter_note);
            note_solstice_winter = addNote(txt_solstice_winter_label, txt_solstice_winter, txt_solstice_winter_note, 0);

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
            btn_flipperPrev_nextYear.setOnTouchListener(createButtonListener(btn_flipperPrev_nextYear));

            titleNextYear = (TextView) nextYear.findViewById(R.id.text_title);

            TextView txt_equinox_vernal2_label = (TextView) nextYear.findViewById(R.id.text_date_equinox_vernal_label);
            TextView txt_equinox_vernal2 = (TextView) nextYear.findViewById(R.id.text_date_equinox_vernal);
            TextView txt_equinox_vernal2_note = (TextView) nextYear.findViewById(R.id.text_date_equinox_vernal_note);
            note_equinox_vernal2 = addNote(txt_equinox_vernal2_label, txt_equinox_vernal2, txt_equinox_vernal2_note, 1);

            TextView txt_solstice_summer2_label = (TextView) nextYear.findViewById(R.id.text_date_solstice_summer_label);
            TextView txt_solstice_summer2 = (TextView) nextYear.findViewById(R.id.text_date_solstice_summer);
            TextView txt_solstice_summer2_note = (TextView) nextYear.findViewById(R.id.text_date_solstice_summer_note);
            note_solstice_summer2 = addNote(txt_solstice_summer2_label, txt_solstice_summer2, txt_solstice_summer2_note, 1);

            TextView txt_equinox_autumnal2_label = (TextView) nextYear.findViewById(R.id.text_date_equinox_autumnal_label);
            TextView txt_equinox_autumnal2 = (TextView) nextYear.findViewById(R.id.text_date_equinox_autumnal);
            TextView txt_equinox_autumnal2_note = (TextView) nextYear.findViewById(R.id.text_date_equinox_autumnal_note);
            note_equinox_autumnal2 = addNote(txt_equinox_autumnal2_label, txt_equinox_autumnal2, txt_equinox_autumnal2_note, 1);

            TextView txt_solstice_winter2_label = (TextView) nextYear.findViewById(R.id.text_date_solstice_winter_label);
            TextView txt_solstice_winter2 = (TextView) nextYear.findViewById(R.id.text_date_solstice_winter);
            TextView txt_solstice_winter2_note = (TextView) nextYear.findViewById(R.id.text_date_solstice_winter_note);
            note_solstice_winter2 = addNote(txt_solstice_winter2_label, txt_solstice_winter2, txt_solstice_winter2_note, 1);

            if (centered)
            {
                FrameLayout.LayoutParams lpNextYear = (FrameLayout.LayoutParams)nextYear.getLayoutParams();
                lpNextYear.gravity = Gravity.CENTER_HORIZONTAL;
                nextYear.setLayoutParams(lpNextYear);
            }
        }

        if (isInEditMode())
        {
            updateViews(context, null);
        }
    }

    private View.OnTouchListener createButtonListener(final ImageButton button)
    {
        return new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (button != null)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        button.setColorFilter(ContextCompat.getColor(getContext(), R.color.btn_tint_pressed));
                        performClick();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        button.setColorFilter(null);
                    }
                }
                return false;
            }
        };
    }

    private int noteColor; //, springColor, summerColor, fallColor, winterColor;

    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary }; //, R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        //springColor = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        //summerColor = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        //fallColor = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        //winterColor = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        typedArray.recycle();
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
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

    private EquinoxNote addNote(TextView labelView, TextView timeView, TextView noteView, int pageIndex)
    {
        EquinoxNote note = new EquinoxNote(labelView, timeView, noteView, pageIndex);
        notes.add(note);
        return note;
    }

    public void setTrackingMode(WidgetSettings.TrackingMode mode)
    {
        trackingMode = mode;
    }
    public WidgetSettings.TrackingMode getTrackingMode()
    {
        return trackingMode;
    }

    public void setMinimized( boolean value )
    {
        this.minimized = value;
    }
    public boolean isMinimized()
    {
        return minimized;
    }

    private EquinoxNote findSoonestNote(Calendar now)
    {
        return findClosestNote(now, true);
    }
    private EquinoxNote findClosestNote(Calendar now)
    {
        return findClosestNote(now, false);
    }
    private EquinoxNote findClosestNote(Calendar now, boolean upcoming)
    {
        if (notes == null || now == null)
        {
            return null;
        }

        EquinoxNote closest = null;
        long timeDeltaMin = Long.MAX_VALUE;
        for (EquinoxNote note : notes)
        {
            Calendar noteTime = note.getTime();
            if (noteTime != null)
            {
                if (upcoming && !noteTime.after(now))
                    continue;

                long timeDelta = Math.abs(noteTime.getTimeInMillis() - now.getTimeInMillis());
                if (timeDelta < timeDeltaMin)
                {
                    timeDeltaMin = timeDelta;
                    closest = note;
                }
            }
        }
        return closest;
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

    private void showTitle( boolean show )
    {
        titleThisYear.setVisibility(show ? View.VISIBLE : View.GONE);
        titleNextYear.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        flipper.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews( Context context, SuntimesEquinoxSolsticeDataset data )
    {
        showTitle(!minimized);
        showNextPrevButtons(!minimized);
        showEmptyView(false);

        if (isInEditMode())
        {
            if (minimized)
            {
                for (int i = 1; i < notes.size(); i++)
                {
                    EquinoxNote note = notes.get(i);
                    note.setVisible(false);
                }
            }
            return;
        }

        if (data == null)
        {
            for (EquinoxNote note : notes)
            {
                note.setEnabled(false);
                note.updateDate(context, null);
                note.updateNote(context, null);

                if (minimized)
                {
                    note.setVisible(false);
                }
            }
            return;
        }

        if (data.isCalculated() && data.isImplemented())
        {
            SuntimesUtils.TimeDisplayText thisYear = utils.calendarDateYearDisplayString(context, data.dataEquinoxVernal.eventCalendarThisYear());
            titleThisYear.setText(thisYear.toString());

            SuntimesUtils.TimeDisplayText nextYear = utils.calendarDateYearDisplayString(context, data.dataEquinoxVernal.eventCalendarOtherYear());
            titleNextYear.setText(nextYear.toString());

            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);

            note_equinox_vernal.updateDate(context, data.dataEquinoxVernal.eventCalendarThisYear(), showTime, showSeconds);
            note_equinox_autumnal.updateDate(context, data.dataEquinoxAutumnal.eventCalendarThisYear(), showTime, showSeconds);
            note_solstice_summer.updateDate(context, data.dataSolsticeSummer.eventCalendarThisYear(), showTime, showSeconds);
            note_solstice_winter.updateDate(context, data.dataSolsticeWinter.eventCalendarThisYear(), showTime, showSeconds);

            note_equinox_vernal2.updateDate(context, data.dataEquinoxVernal.eventCalendarOtherYear(), showTime, showSeconds);
            note_equinox_autumnal2.updateDate(context, data.dataEquinoxAutumnal.eventCalendarOtherYear(), showTime, showSeconds);
            note_solstice_summer2.updateDate(context, data.dataSolsticeSummer.eventCalendarOtherYear(), showTime, showSeconds);
            note_solstice_winter2.updateDate(context, data.dataSolsticeWinter.eventCalendarOtherYear(), showTime, showSeconds);

            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
            for (EquinoxNote note : notes)
            {
                note.setEnabled();
                note.updateNote(context, data.now(), showWeeks, showHours);
                note.setVisible(!minimized);
            }

            EquinoxNote nextNote = (trackingMode == WidgetSettings.TrackingMode.SOONEST ? findSoonestNote(data.now())
                                                                                        : findClosestNote(data.now()));
            if (nextNote == null)
            {
                nextNote = notes.get(0);
            }

            if (!userSwappedCard)
            {
                flipper.setDisplayedChild(nextNote.pageIndex);
            }
            nextNote.setVisible(true);
            nextNote.setHighlighted(true);

        } else {
            if (minimized)
            {
                for (EquinoxNote note : notes)
                {
                    note.setVisible(false);
                }
            } else {
                showEmptyView(true);
            }
        }
    }

    public boolean saveState(Bundle bundle)
    {
        boolean cardIsNextYear = (flipper.getDisplayedChild() != 0);
        bundle.putBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, cardIsNextYear);
        bundle.putBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        bundle.putBoolean(EquinoxView.KEY_UI_MINIMIZED, minimized);
        Log.d("DEBUG", "EquinoxView saveState :: nextyear:" + cardIsNextYear + " :: swapped:" + userSwappedCard + " :: minimized:" + minimized);
        return true;
    }

    public void loadState(Bundle bundle)
    {
        boolean cardIsNextYear = bundle.getBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, false);
        flipper.setDisplayedChild((cardIsNextYear ? 1 : 0));
        userSwappedCard = bundle.getBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, false);
        minimized = bundle.getBoolean(EquinoxView.KEY_UI_MINIMIZED, minimized);
        Log.d("DEBUG", "EquinoxView loadState :: nextyear: " + cardIsNextYear + " :: swapped:" + userSwappedCard + " :: minimized:" + minimized);
    }

    public boolean showNextCard()
    {
        if (hasNextCard())
        {
            flipper.setOutAnimation(anim_card_outNext);
            flipper.setInAnimation(anim_card_inNext);
            flipper.showNext();
        }
        return true;
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
        }
        return true;
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
                    performClick();
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

                    } //else {
                        // at-a-boundary (the first/last view);
                        // TODO: animate somehow to let user know there aren't additional views
                    //}
                    break;
            }

            return false;
        }
    };

    public void adjustColumnWidth(Context context, int columnWidthPx)
    {
        for (EquinoxNote note : notes)
        {
            note.adjustLabelWidth(columnWidthPx);
        }
    }

    /**
     * EquinoxNote
     */
    protected class EquinoxNote
    {
        protected TextView labelView, timeView, noteView;
        protected Calendar time, now;
        protected boolean highlighted;
        protected int pageIndex = 0;

        public EquinoxNote(TextView labelView, TextView timeView, TextView noteView, int pageIndex)
        {
            this.labelView = labelView;
            this.timeView = timeView;
            this.noteView = noteView;
            this.pageIndex = pageIndex;

            if (this.timeView != null)
            {
                this.timeView.setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (minimized)
                        {
                            flipper.performClick();
                        }
                    }
                });
            }
        }

        public void adjustLabelWidth( int labelWidthPx )
        {
            ViewGroup.LayoutParams layoutParams = labelView.getLayoutParams();
            layoutParams.width = labelWidthPx;
            labelView.setLayoutParams(layoutParams);
        }

        public void updateDate( Context context, Calendar time )
        {
            updateDate(context, time, true, false);
        }
        public void updateDate( Context context, Calendar time, boolean showTime, boolean showSeconds )
        {
            this.time = time;
            if (timeView != null)
            {
                SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, time, showTime, showSeconds);
                timeView.setText(timeText.toString());
            }
        }

        public void updateNote( Context context, Calendar now )
        {
            updateDate(context, now, true, false);
        }
        public void updateNote( Context context, Calendar now, boolean showWeeks, boolean showHours )
        {
            this.now = now;
            if (noteView != null)
            {
                if (now != null && time != null)
                {
                    String noteText = utils.timeDeltaDisplayString(now.getTime(), time.getTime(), showWeeks, showHours).toString();

                    if (time.before(Calendar.getInstance()))
                    {
                        String noteString = context.getString(R.string.ago, noteText);
                        SpannableString noteSpan = (noteView.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, noteColor)
                                                                         : SuntimesUtils.createBoldSpan(null, noteString, noteText));
                        noteView.setText(noteSpan);

                    } else {
                        String noteString = context.getString(R.string.hence, noteText);
                        SpannableString noteSpan = (noteView.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, noteColor)
                                                                         : SuntimesUtils.createBoldSpan(null, noteString, noteText));
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
            setEnabled(true);
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

        public void setEnabled( boolean value)
        {
            labelView.setEnabled(value);
            timeView.setEnabled(value);
            noteView.setEnabled(value);
        }

        public void setEnabled()
        {
            if (time != null)
            {
                setEnabled(time.after(Calendar.getInstance()));

            } else {
                setEnabled(false);
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
