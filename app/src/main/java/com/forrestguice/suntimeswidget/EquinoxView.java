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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class EquinoxView extends LinearLayout
{
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedEquinoxCard";
    public static final String KEY_UI_CARDISNEXTYEAR = "equinoxCardIsNextYear";
    public static final String KEY_UI_MINIMIZED = "equinoxIsMinimized";

    private static SuntimesUtils utils = new SuntimesUtils();
    private boolean userSwappedCard = false;


    private TextView empty;
    private ViewFlipper flipper;           // flip between thisYear, nextYear
    private Animation anim_card_outNext, anim_card_inNext, anim_card_outPrev, anim_card_inPrev;

    private EquinoxViewHolder holder_thisYear, holder_nextYear;
    /**private ImageButton btn_flipperNext_thisYear, btn_flipperPrev_thisYear;
    private ImageButton btn_flipperNext_nextYear, btn_flipperPrev_nextYear;

    private TextView titleThisYear, titleNextYear;

    private EquinoxNote note_equinox_vernal, note_solstice_summer, note_equinox_autumnal, note_solstice_winter;  // this year
    private EquinoxNote note_equinox_vernal2, note_solstice_summer2, note_equinox_autumnal2, note_solstice_winter2;  // and next year*/
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
        themeViews(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_equinox, this, true);

        if (attrs != null)
        {
            LinearLayout.LayoutParams lp = generateLayoutParams(attrs);
            options.centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        empty = (TextView)findViewById(R.id.txt_empty);

        flipper = (ViewFlipper)findViewById(R.id.info_equinoxsolstice_flipper);
        flipper.setOnTouchListener(cardTouchListener);

        notes = new ArrayList<EquinoxNote>();

        RelativeLayout thisYear = (RelativeLayout)findViewById(R.id.info_equinoxsolstice_thisyear);
        if (thisYear != null)
        {
            EquinoxViewHolder holder = holder_thisYear = new EquinoxViewHolder(thisYear, options);
            holder.btn_flipperNext.setOnClickListener(onNextCardClick);
            holder.btn_flipperPrev.setVisibility(View.GONE);
        }

        RelativeLayout nextYear = (RelativeLayout)findViewById(R.id.info_equinoxsolstice_nextyear);
        if (nextYear != null)
        {
            EquinoxViewHolder holder = holder_nextYear = new EquinoxViewHolder(nextYear, options);
            holder.btn_flipperPrev.setOnClickListener(onPrevCardClick);
            holder.btn_flipperNext.setVisibility(View.GONE);
        }

        if (isInEditMode()) {
            updateViews(context, null);
        }
    }

    private EquinoxViewOptions options = new EquinoxViewOptions();

    @SuppressLint("ResourceType")
    private void themeViews(Context context) {
        options.init(context);
    }

    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null)
        {
            options.init(theme);
            holder_thisYear.themeViews(theme, options);
            holder_nextYear.themeViews(theme, options);
        }
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        options.isRtl = AppSettings.isLocaleRtl(context);
        initAnimations(context);
    }

    private void initAnimations(Context context)
    {
        anim_card_inNext = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        anim_card_inPrev = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        anim_card_outNext = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        anim_card_outPrev = AnimationUtils.loadAnimation(context, R.anim.fade_out);
    }

    private EquinoxNote addNote(TextView labelView, TextView timeView, TextView noteView, int pageIndex, Integer timeColor)
    {
        EquinoxNote note = new EquinoxNote(labelView, timeView, noteView, pageIndex, options);
        if (timeColor != null) {
            note.themeViews(options.labelColor, timeColor, options.textColor);
        }
        notes.add(note);
        return note;
    }

    public void setTrackingMode(WidgetSettings.TrackingMode mode) {
        options.trackingMode = mode;
    }
    public WidgetSettings.TrackingMode getTrackingMode() {
        return options.trackingMode;
    }

    public void setMinimized( boolean value ) {
        options.minimized = value;
    }
    public boolean isMinimized() {
        return options.minimized;
    }

    private EquinoxNote findSoonestNote(Calendar now) {
        return findClosestNote(now, true);
    }
    private EquinoxNote findClosestNote(Calendar now) {
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
            holder_thisYear.btn_flipperNext.setVisibility(View.VISIBLE);
            holder_thisYear.btn_flipperPrev.setVisibility(View.GONE);
            holder_nextYear.btn_flipperNext.setVisibility(View.GONE);
            holder_nextYear.btn_flipperPrev.setVisibility(View.VISIBLE);

        } else {
            holder_thisYear.btn_flipperNext.setVisibility(View.GONE);
            holder_thisYear.btn_flipperPrev.setVisibility(View.GONE);
            holder_nextYear.btn_flipperNext.setVisibility(View.GONE);
            holder_nextYear.btn_flipperPrev.setVisibility(View.GONE);
        }
    }

    private void showTitle( boolean show )
    {
        holder_thisYear.title.setVisibility(show ? View.VISIBLE : View.GONE);
        holder_nextYear.title.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        flipper.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews( Context context, SuntimesEquinoxSolsticeDataset data )
    {
        showTitle(!options.minimized);
        showNextPrevButtons(!options.minimized);
        showEmptyView(false);

        if (isInEditMode())
        {
            if (options.minimized)
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

                if (options.minimized)
                {
                    note.setVisible(false);
                }
            }
            return;
        }

        if (data.isCalculated() && data.isImplemented())
        {
            SuntimesUtils.TimeDisplayText thisYear = utils.calendarDateYearDisplayString(context, data.dataEquinoxVernal.eventCalendarThisYear());
            holder_thisYear.title.setText(thisYear.toString());

            SuntimesUtils.TimeDisplayText nextYear = utils.calendarDateYearDisplayString(context, data.dataEquinoxVernal.eventCalendarOtherYear());
            holder_nextYear.title.setText(nextYear.toString());

            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);

            holder_thisYear.note_equinox_vernal.updateDate(context, data.dataEquinoxVernal.eventCalendarThisYear(), showTime, showSeconds);
            holder_thisYear.note_equinox_autumnal.updateDate(context, data.dataEquinoxAutumnal.eventCalendarThisYear(), showTime, showSeconds);
            holder_thisYear.note_solstice_summer.updateDate(context, data.dataSolsticeSummer.eventCalendarThisYear(), showTime, showSeconds);
            holder_thisYear.note_solstice_winter.updateDate(context, data.dataSolsticeWinter.eventCalendarThisYear(), showTime, showSeconds);

            holder_nextYear.note_equinox_vernal.updateDate(context, data.dataEquinoxVernal.eventCalendarOtherYear(), showTime, showSeconds);
            holder_nextYear.note_equinox_autumnal.updateDate(context, data.dataEquinoxAutumnal.eventCalendarOtherYear(), showTime, showSeconds);
            holder_nextYear.note_solstice_summer.updateDate(context, data.dataSolsticeSummer.eventCalendarOtherYear(), showTime, showSeconds);
            holder_nextYear.note_solstice_winter.updateDate(context, data.dataSolsticeWinter.eventCalendarOtherYear(), showTime, showSeconds);

            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
            for (EquinoxNote note : notes)
            {
                note.setEnabled();
                note.updateNote(context, data.now(), showWeeks, showHours);
                note.setVisible(!options.minimized);
            }

            EquinoxNote nextNote = (options.trackingMode == WidgetSettings.TrackingMode.SOONEST ? findSoonestNote(data.now())
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
            if (options.minimized)
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
        bundle.putBoolean(EquinoxView.KEY_UI_MINIMIZED, options.minimized);
        Log.d("DEBUG", "EquinoxView saveState :: nextyear:" + cardIsNextYear + " :: swapped:" + userSwappedCard + " :: minimized:" + options.minimized);
        return true;
    }

    public void loadState(Bundle bundle)
    {
        boolean cardIsNextYear = bundle.getBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, false);
        flipper.setDisplayedChild((cardIsNextYear ? 1 : 0));
        userSwappedCard = bundle.getBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, false);
        options.minimized = bundle.getBoolean(EquinoxView.KEY_UI_MINIMIZED, options.minimized);
        Log.d("DEBUG", "EquinoxView loadState :: nextyear: " + cardIsNextYear + " :: swapped:" + userSwappedCard + " :: minimized:" + options.minimized);
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
            if (options.minimized)
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
                        userSwappedCard = (options.isRtl ? showNextCard() : showPreviousCard());

                    } else if (firstTouchX - secondTouchX > FLING_SENSITIVITY) {
                        // swipe left; advance to next view
                        userSwappedCard = (options.isRtl ? showPreviousCard() : showNextCard());

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
                    if (options.isRtl)
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
    public static class EquinoxNote
    {
        protected TextView labelView, timeView, noteView;
        protected Calendar time, now;
        protected boolean highlighted;
        protected int pageIndex = 0;
        private EquinoxViewOptions options;

        public EquinoxNote(TextView labelView, TextView timeView, TextView noteView, int pageIndex, EquinoxViewOptions options)
        {
            this.labelView = labelView;
            this.timeView = timeView;
            this.noteView = noteView;
            this.pageIndex = pageIndex;
            this.options = options;

            if (this.timeView != null)
            {
                this.timeView.setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (EquinoxNote.this.options.minimized)
                        {
                            //flipper.performClick();
                            // TODO: restore
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

        public void themeViews(Integer labelColor, Integer timeColor, Integer textColor)
        {
            if (labelColor != null) {
                labelView.setTextColor(SuntimesUtils.colorStateList(labelColor, options.disabledColor));
            } else Log.e("EquinoxView", "themeViews: null color, ignoring...");

            if (timeColor != null) {
                timeView.setTextColor(SuntimesUtils.colorStateList(timeColor, options.disabledColor));
            } else Log.e("EquinoxView", "themeViews: null color, ignoring...");

            if (textColor != null) {
                noteView.setTextColor(SuntimesUtils.colorStateList(textColor, options.disabledColor));
            } else Log.e("EquinoxView", "themeViews: null color, ignoring...");
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
                        SpannableString noteSpan = (noteView.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, options.noteColor)
                                                                         : SuntimesUtils.createBoldSpan(null, noteString, noteText));
                        noteView.setText(noteSpan);

                    } else {
                        String noteString = context.getString(R.string.hence, noteText);
                        SpannableString noteSpan = (noteView.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, options.noteColor)
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

    /**
     * EquinoxViewHolder
     */
    public static class EquinoxViewHolder extends RecyclerView.ViewHolder
    {
        public int position = RecyclerView.NO_POSITION;

        public TextView title;
        public ImageButton btn_flipperNext, btn_flipperPrev;
        public EquinoxNote note_equinox_vernal, note_solstice_summer, note_equinox_autumnal, note_solstice_winter;
        public ArrayList<EquinoxNote> notes = new ArrayList<>();

        public EquinoxViewHolder(View view, EquinoxViewOptions options)
        {
            super(view);

            title = (TextView)view.findViewById(R.id.text_title);
            btn_flipperNext = (ImageButton)view.findViewById(R.id.info_time_nextbtn);
            btn_flipperPrev = (ImageButton)view.findViewById(R.id.info_time_prevbtn);
            //btn_flipperNext.setOnClickListener(onNextCardClick);

            TextView txt_equinox_vernal_label = (TextView)view.findViewById(R.id.text_date_equinox_vernal_label);
            TextView txt_equinox_vernal = (TextView)view.findViewById(R.id.text_date_equinox_vernal);
            TextView txt_equinox_vernal_note = (TextView)view.findViewById(R.id.text_date_equinox_vernal_note);
            note_equinox_vernal = addNote(txt_equinox_vernal_label, txt_equinox_vernal, txt_equinox_vernal_note, 0, options.seasonColors[0], options);

            TextView txt_solstice_summer_label = (TextView)view.findViewById(R.id.text_date_solstice_summer_label);
            TextView txt_solstice_summer = (TextView)view.findViewById(R.id.text_date_solstice_summer);
            TextView txt_solstice_summer_note = (TextView)view.findViewById(R.id.text_date_solstice_summer_note);
            note_solstice_summer = addNote(txt_solstice_summer_label, txt_solstice_summer, txt_solstice_summer_note, 0, options.seasonColors[1], options);

            TextView txt_equinox_autumnal_label = (TextView)view.findViewById(R.id.text_date_equinox_autumnal_label);
            TextView txt_equinox_autumnal = (TextView)view.findViewById(R.id.text_date_equinox_autumnal);
            TextView txt_equinox_autumnal_note = (TextView)view.findViewById(R.id.text_date_equinox_autumnal_note);
            note_equinox_autumnal = addNote(txt_equinox_autumnal_label, txt_equinox_autumnal, txt_equinox_autumnal_note, 0, options.seasonColors[2], options);

            TextView txt_solstice_winter_label = (TextView)view.findViewById(R.id.text_date_solstice_winter_label);
            TextView txt_solstice_winter = (TextView)view.findViewById(R.id.text_date_solstice_winter);
            TextView txt_solstice_winter_note = (TextView)view.findViewById(R.id.text_date_solstice_winter_note);
            note_solstice_winter = addNote(txt_solstice_winter_label, txt_solstice_winter, txt_solstice_winter_note, 0, options.seasonColors[3], options);

            if (options.centered)
            {
                FrameLayout.LayoutParams lpThisYear = (FrameLayout.LayoutParams)view.getLayoutParams();
                lpThisYear.gravity = Gravity.CENTER_HORIZONTAL;
                view.setLayoutParams(lpThisYear);
            }
        }

        private EquinoxNote addNote(TextView labelView, TextView timeView, TextView noteView, int pageIndex, Integer timeColor, EquinoxViewOptions options)
        {
            EquinoxNote note = new EquinoxNote(labelView, timeView, noteView, pageIndex, options);
            if (timeColor != null) {
                note.themeViews(options.labelColor, timeColor, options.textColor);
            }
            notes.add(note);
            return note;
        }

        public void bindDataToPosition(@NonNull Context context, @NonNull SuntimesEquinoxSolsticeData data, int position, EquinoxViewOptions options)
        {
            this.position = position;
        }

        public void showTitle( boolean show ) {
            title.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        public void showNextPrevButtons( boolean show )
        {
            if (show) {
                btn_flipperNext.setVisibility(View.VISIBLE);
                btn_flipperPrev.setVisibility(View.GONE);
            } else {
                btn_flipperNext.setVisibility(View.GONE);
                btn_flipperPrev.setVisibility(View.GONE);
            }
        }

        public void adjustColumnWidth(Context context, int columnWidthPx)
        {
            for (EquinoxNote note : notes) {
                note.adjustLabelWidth(columnWidthPx);
            }
        }

        public void themeViews(SuntimesTheme theme, EquinoxViewOptions options)
        {
            if (theme != null)
            {
                if (note_equinox_vernal != null)
                {
                    int titleColor = theme.getTitleColor();
                    title.setTextColor(SuntimesUtils.colorStateList(titleColor, options.disabledColor, options.pressedColor));

                    note_equinox_vernal.themeViews(options.labelColor, options.seasonColors[0], options.textColor);
                    note_solstice_summer.themeViews(options.labelColor, options.seasonColors[1], options.textColor);
                    note_equinox_autumnal.themeViews(options.labelColor, options.seasonColors[2], options.textColor);
                    note_solstice_winter.themeViews(options.labelColor, options.seasonColors[3], options.textColor);
                }
            }
        }
    }

    /**
     * EquinoxViewOptions
     */
    public static class EquinoxViewOptions
    {
        public boolean isRtl = false;
        public boolean minimized = false;
        public boolean centered = false;

        public WidgetSettings.TrackingMode trackingMode = WidgetSettings.TrackingMode.SOONEST;

        public int titleColor, noteColor, disabledColor, pressedColor;
        public Integer[] seasonColors = new Integer[4];
        public Integer labelColor, textColor;
        public int resID_buttonPressColor;

        @SuppressLint("ResourceType")
        public void init(Context context)
        {
            int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.text_disabledColor, R.attr.buttonPressColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.transparent));
            titleColor = noteColor;
            disabledColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_disabled_dark));
            resID_buttonPressColor = typedArray.getResourceId(2, R.color.btn_tint_pressed_dark);
            pressedColor = ContextCompat.getColor(context, resID_buttonPressColor);
            labelColor = textColor = seasonColors[0] = seasonColors[1] = seasonColors[2] = seasonColors[3] = null;
            typedArray.recycle();
        }

        public void init(SuntimesTheme theme)
        {
            if (theme != null)
            {
                titleColor = theme.getTitleColor();
                noteColor = theme.getTimeColor();
                labelColor = theme.getTitleColor();
                textColor = theme.getTextColor();
                pressedColor = theme.getActionColor();
                seasonColors[0] = theme.getSpringColor();
                seasonColors[1] = theme.getSummerColor();
                seasonColors[2] = theme.getFallColor();
                seasonColors[3] = theme.getWinterColor();
            }
        }
    }

}
