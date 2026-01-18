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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.support.widget.ImageViewCompat;
import com.forrestguice.support.widget.RecyclerView;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import java.util.ArrayList;
import java.util.Calendar;

import static com.forrestguice.suntimeswidget.equinox.EquinoxColorValues.COLOR_SPRING_TEXT;
import static com.forrestguice.suntimeswidget.equinox.EquinoxColorValues.COLOR_SUMMER_TEXT;
import static com.forrestguice.suntimeswidget.equinox.EquinoxColorValues.COLOR_AUTUMN_TEXT;
import static com.forrestguice.suntimeswidget.equinox.EquinoxColorValues.COLOR_WINTER_TEXT;

/**
 * @see EquinoxDatasetAdapter
 */
public class EquinoxDatasetViewHolder extends RecyclerView.ViewHolder
{
    protected static final TimeDateDisplay utils = new TimeDateDisplay();
    protected static final TimeDeltaDisplay delta_utils = new TimeDeltaDisplay();

    public int position = RecyclerView.NO_POSITION;

    public View clickArea;
    public View[] clickAreas = new View[4];
    public SolsticeEquinoxMode selected = null;

    public View container;
    public TextView title;
    public ImageButton btn_flipperNext, btn_flipperPrev;
    public EquinoxNote note_equinox_vernal, note_solstice_summer, note_equinox_autumnal, note_solstice_winter;
    public ArrayList<EquinoxNote> notes = new ArrayList<>();

    public static int getSuggestedLayoutResID() {
        return R.layout.info_time_solsticequinox1;
    }

    public EquinoxDatasetViewHolder(View view, EquinoxViewOptions options)
    {
        super(view);

        container = view.findViewById(R.id.card_content);

        clickArea = view.findViewById(R.id.clickArea);
        if (!options.minimized) {
            clickArea.setVisibility(View.GONE);
        }

        int[] clickResID = new int[] { R.id.click_equinox_vernal, R.id.click_solstice_summer, R.id.click_equinox_autumnal, R.id.click_solstice_winter };
        for (int i=0; i <clickAreas.length; i++)
        {
            clickAreas[i] = view.findViewById(clickResID[i]);
            if (clickAreas[i] != null) {
                clickAreas[i].setVisibility(options.minimized ? View.GONE : View.VISIBLE);
            }
        }

        title = (TextView)view.findViewById(R.id.text_title);
        btn_flipperNext = (ImageButton)view.findViewById(R.id.info_time_nextbtn);
        btn_flipperPrev = (ImageButton)view.findViewById(R.id.info_time_prevbtn);

        note_equinox_vernal = addNote(view, R.id.text_date_equinox_vernal_label, R.id.text_date_equinox_vernal, R.id.text_date_equinox_vernal_note, R.id.menu_equinox_vernal, R.id.focus_equinox_vernal, R.id.text_date_equinox_vernal_layout, 0, options.colors.getColor(COLOR_SPRING_TEXT), options);
        note_solstice_summer = addNote(view, R.id.text_date_solstice_summer_label, R.id.text_date_solstice_summer, R.id.text_date_solstice_summer_note, R.id.menu_solstice_summer, R.id.focus_solstice_summer, R.id.text_date_solstice_summer_layout, 0, options.colors.getColor(COLOR_SUMMER_TEXT), options);
        note_equinox_autumnal = addNote(view, R.id.text_date_equinox_autumnal_label, R.id.text_date_equinox_autumnal, R.id.text_date_equinox_autumnal_note, R.id.menu_equinox_autumnal, R.id.focus_equinox_autumnal, R.id.text_date_equinox_autumnal_layout, 0, options.colors.getColor(COLOR_AUTUMN_TEXT), options);
        note_solstice_winter = addNote(view, R.id.text_date_solstice_winter_label, R.id.text_date_solstice_winter, R.id.text_date_solstice_winter_note, R.id.menu_solstice_winter, R.id.focus_solstice_winter, R.id.text_date_solstice_winter_layout, 0, options.colors.getColor(COLOR_WINTER_TEXT), options);

        if (options.columnWidthPx >= 0) {
            adjustColumnWidth(options.columnWidthPx);
        }

        if (options.centered)
        {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)view.getLayoutParams();
            //params.gravity = Gravity.CENTER_HORIZONTAL;
            view.setLayoutParams(params);
        }
    }

    public void setSelected(SolsticeEquinoxMode mode) {
        this.selected = mode;
        updateItemFocus();
    }
    public SolsticeEquinoxMode getSelected() {
        return selected;
    }

    protected void updateItemFocus()
    {
        int p = (selected != null ? selected.ordinal() : -1);
        for (int i=0; i<notes.size(); i++)
        {
            int visibility = (i == p) ? View.VISIBLE : View.GONE;
            View focusView = notes.get(i).focusView;
            if (focusView != null) {
                focusView.setVisibility(visibility);
            }
            ImageButton menuButton = notes.get(i).contextMenu;
            if (menuButton != null) {
                menuButton.setVisibility(visibility);
            }
        }
    }

    private EquinoxNote addNote(View view, int labelViewResID, int timeViewResID, int noteViewResID, int menuButtonResID, int focusViewResID, int noteLayoutResID, int pageIndex, Integer timeColor, EquinoxViewOptions options)
    {
        TextView txt_label = (TextView)view.findViewById(labelViewResID);
        TextView txt_time = (TextView)view.findViewById(timeViewResID);
        TextView txt_note = (TextView)view.findViewById(noteViewResID);
        ImageButton menu = (ImageButton) view.findViewById(menuButtonResID);
        View focus = view.findViewById(focusViewResID);
        View layout = view.findViewById(noteLayoutResID);
        return addNote(txt_label, txt_time, txt_note, menu, focus, layout, pageIndex, timeColor, options);
    }

    private EquinoxNote addNote(TextView labelView, TextView timeView, TextView noteView, ImageButton menuButton, View focusView, View noteLayout, int pageIndex, Integer timeColor, EquinoxViewOptions options)
    {
        EquinoxNote note = new EquinoxNote(labelView, timeView, noteView, menuButton, focusView, noteLayout, pageIndex, options);
        if (timeColor != null) {
            note.themeViews(options.labelColor, timeColor, options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
        }
        notes.add(note);
        return note;
    }

    public void disableNotes(Context context, EquinoxViewOptions options)
    {
        for (EquinoxNote note : notes)
        {
            note.setHighlighted(false);
            note.setEnabled(false);
            note.updateDate(context, null);
            note.updateNote(context, null, false, false);

            if (options.minimized) {
                note.setVisible(false);
            }
        }
    }
    public void enableNotes(boolean visible)
    {
        for (EquinoxNote note : notes)
        {
            note.setEnabled();
            note.setVisible(visible);
        }
    }

    public void bindDataToPosition(@NonNull Context context, SuntimesEquinoxSolsticeDataset data, int position, EquinoxViewOptions options)
    {
        this.position = position;
        for (EquinoxNote note : notes) {
            note.pageIndex = position;
        }

        if (options.themeOverride != null) {
            //noinspection deprecation
            applyTheme(options.themeOverride, options);
        }
        themeViews(options, position);

        showTitle(false);
        //showTitle(!options.minimized);

        showNextPrevButtons(false);
        //showNextPrevButtons(!options.minimized);

        if (data == null) {
            disableNotes(context, options);
            return;
        }

        if (data.isImplemented() && data.isCalculated())
        {
            TimeDisplayText titleText = utils.calendarDateYearDisplayString(AndroidResources.wrap(context), data.dataEquinoxSpring.eventCalendarThisYear());
            title.setText(titleText.toString());

            boolean showSeconds = !options.minimized || WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);

            note_equinox_vernal.updateDate(context, data.dataEquinoxSpring.eventCalendarThisYear(), showTime, showSeconds);
            note_equinox_autumnal.updateDate(context, data.dataEquinoxAutumnal.eventCalendarThisYear(), showTime, showSeconds);
            note_solstice_summer.updateDate(context, data.dataSolsticeSummer.eventCalendarThisYear(), showTime, showSeconds);
            note_solstice_winter.updateDate(context, data.dataSolsticeWinter.eventCalendarThisYear(), showTime, showSeconds);

            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
            for (EquinoxNote note : notes) {
                note.setHighlighted(false);
                note.updateNote(context, data.now(), showWeeks, showHours);
            }

        } else {
            disableNotes(context, options);
        }

        if (options.columnWidthPx >= 0) {
            adjustColumnWidth(options.columnWidthPx);
        }
        updateItemFocus();
    }

    public void showTitle( boolean show ) {
        title.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showNextPrevButtons( boolean show )
    {
        if (show) {
            btn_flipperNext.setVisibility(View.VISIBLE);
            btn_flipperPrev.setVisibility(View.VISIBLE);
        } else {
            btn_flipperNext.setVisibility(View.GONE);
            btn_flipperPrev.setVisibility(View.GONE);
        }
    }

    public void adjustColumnWidth(int columnWidthPx)
    {
        for (EquinoxNote note : notes) {
            note.adjustLabelWidth(columnWidthPx);
        }
    }

    @Deprecated
    public void applyTheme(SuntimesTheme theme, EquinoxViewOptions options)
    {
        if (theme != null)
        {
            options.titleColor = theme.getTitleColor();
            options.textColor = theme.getTextColor();
            options.pressedColor = theme.getActionColor();
            options.colors.setColor(COLOR_SPRING_TEXT, theme.getSpringColor());
            options.colors.setColor(COLOR_SUMMER_TEXT, theme.getSummerColor());
            options.colors.setColor(COLOR_AUTUMN_TEXT, theme.getFallColor());
            options.colors.setColor(COLOR_WINTER_TEXT, theme.getWinterColor());
        }
    }
    public void themeViews(EquinoxViewOptions options, int position )
    {
        title.setTextColor(SuntimesUtils.colorStateList((position  < EquinoxDatasetAdapter.CENTER_POSITION ? options.disabledColor : options.titleColor), options.disabledColor, options.pressedColor));
        if (options.titleSizeSp != null)
        {
            title.setTextSize(options.titleSizeSp);
            title.setTypeface(title.getTypeface(), (options.titleBold ? Typeface.BOLD : Typeface.NORMAL));
        }

        ImageViewCompat.setImageTintList(btn_flipperNext, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));
        ImageViewCompat.setImageTintList(btn_flipperPrev, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));

        note_equinox_vernal.themeViews(options.labelColor, options.colors.getColor(COLOR_SPRING_TEXT), options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
        note_solstice_summer.themeViews(options.labelColor, options.colors.getColor(COLOR_SUMMER_TEXT), options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
        note_equinox_autumnal.themeViews(options.labelColor, options.colors.getColor(COLOR_AUTUMN_TEXT), options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
        note_solstice_winter.themeViews(options.labelColor, options.colors.getColor(COLOR_WINTER_TEXT), options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
    }

    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////

    /**
     * EquinoxNote
     */
    public static class EquinoxNote
    {
        public TextView labelView, timeView, noteView;
        public ImageButton contextMenu;
        public Calendar time;
        public boolean highlighted;
        public int pageIndex;
        public EquinoxViewOptions options;
        public View focusView, noteLayout;

        public EquinoxNote(TextView labelView, TextView timeView, TextView noteView, ImageButton contextMenu, View focusView, View noteLayout, int pageIndex, EquinoxViewOptions options)
        {
            this.labelView = labelView;
            this.timeView = timeView;
            this.noteView = noteView;
            this.contextMenu = contextMenu;
            this.focusView = focusView;
            this.noteLayout = noteLayout;
            this.pageIndex = pageIndex;
            this.options = options;
        }

        public void adjustLabelWidth( int labelWidthPx )
        {
            ViewGroup.LayoutParams layoutParams = labelView.getLayoutParams();
            layoutParams.width = labelWidthPx;
            labelView.setLayoutParams(layoutParams);
        }

        public void themeViews(@Nullable Integer labelColor, @Nullable Integer timeColor, @Nullable Integer textColor, @Nullable Float textSizeSp, @Nullable Float titleSizeSp, boolean titleBold)
        {
            if (labelColor != null) {
                labelView.setTextColor(SuntimesUtils.colorStateList(labelColor, options.disabledColor));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (timeColor != null) {
                timeView.setTextColor(SuntimesUtils.colorStateList(timeColor, options.disabledColor));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (textColor != null) {
                noteView.setTextColor(SuntimesUtils.colorStateList(textColor, options.disabledColor));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (textSizeSp != null) {
                noteView.setTextSize(textSizeSp);
                timeView.setTextSize(textSizeSp);
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (titleSizeSp != null) {
                labelView.setTextSize(titleSizeSp);
                labelView.setTypeface(labelView.getTypeface(), (titleBold ? Typeface.BOLD : Typeface.NORMAL));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");
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
                TimeDisplayText timeText = new SuntimesUtils().calendarDateTimeDisplayString(context, time, showTime, showSeconds);
                timeView.setText(timeText.toString());
            }
        }

        public void updateNote( Context context, Calendar now, boolean showWeeks, boolean showHours )
        {
            if (noteView != null)
            {
                if (now != null && time != null)
                {
                    String noteText = delta_utils.timeDeltaDisplayString(now.getTime(), time.getTime(), showWeeks, showHours).toString();

                    if (time.before(Calendar.getInstance()))
                    {
                        String noteString = context.getString(R.string.ago, noteText);
                        SpannableString noteSpan = (noteView.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, (options.minimized || highlighted ? options.noteColor : options.disabledColor))
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
            setVisible(true);
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
            labelView.setVisibility( visible ? View.VISIBLE : View.GONE );
            timeView.setVisibility( visible ? View.VISIBLE : View.GONE );
            noteView.setVisibility( visible ? View.VISIBLE : View.GONE );
            noteLayout.setVisibility( visible ? View.VISIBLE : View.GONE );
        }

        public Calendar getTime() {
            return time;
        }
    }

}