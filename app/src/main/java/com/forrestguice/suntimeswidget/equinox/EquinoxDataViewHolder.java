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
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.support.widget.RecyclerView;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import java.util.Calendar;

/**
 * @see EquinoxDataAdapter
 */
public class EquinoxDataViewHolder extends RecyclerView.ViewHolder
{
    protected static final TimeDateDisplay utils = new TimeDateDisplay();
    protected static final TimeDeltaDisplay delta_utils = new TimeDeltaDisplay();

    public View clickArea;
    public View focusView;

    public TextView text_label;
    public TextView text_datetime;
    public TextView text_note;
    public ImageButton button_menu;

    public boolean highlighted = false;
    public boolean selected = false;
    public int position = RecyclerView.NO_POSITION;

    public EquinoxDataViewHolder(View view)
    {
        super(view);
        clickArea = view.findViewById(R.id.clickArea);
        focusView = view.findViewById(R.id.focusView);
        text_label = (TextView) view.findViewById(R.id.text_label);
        text_datetime = (TextView) view.findViewById(R.id.text_datetime);
        text_note = (TextView) view.findViewById(R.id.text_note);
        button_menu = (ImageButton) view.findViewById(R.id.menu_button);
    }

    public void bindDataToPosition(@NonNull Context context, SuntimesEquinoxSolsticeData data, int position, EquinoxViewOptions options)
    {
        this.position = position;
        if (data != null && data.isCalculated())
        {
            Calendar event = data.eventCalendarThisYear();
            if (event != null)
            {
                Calendar now = Calendar.getInstance();
                TimeDisplayText timeText = utils.calendarDateTimeDisplayString(AndroidResources.wrap(context), event, WidgetSettings.loadShowTimeDatePref(context, 0), options.showSeconds);
                text_datetime.setText(timeText.toString());
                text_datetime.setVisibility(options.showDate ? View.VISIBLE : View.GONE);
                text_label.setText(data.timeMode().getLongDisplayString());
                updateNote(context, now, data, WidgetSettings.loadShowWeeksPref(context, 0), WidgetSettings.loadShowHoursPref(context, 0), options);
                themeViews(context, data, options);

                focusView.setVisibility(selected ? View.VISIBLE : View.GONE);
                button_menu.setVisibility(selected ? View.VISIBLE : View.GONE);
                
                if (AppSettings.isTelevision(context)) {
                    button_menu.setFocusableInTouchMode(true);
                    if (selected) {
                        button_menu.requestFocus();
                    }
                }

                boolean enabled = (now.before(event));
                text_label.setEnabled(enabled);
                text_note.setEnabled(enabled);
                text_datetime.setEnabled(enabled);
                return;
            }
        }

        // no data
        focusView.setVisibility(View.GONE);
        button_menu.setVisibility(View.GONE);

        text_label.setText("");
        text_note.setText("");
        text_datetime.setText("");

        text_label.setEnabled(false);
        text_note.setEnabled(false);
        text_datetime.setEnabled(false);
    }

    public void updateNote( Context context, Calendar now, SuntimesEquinoxSolsticeData data, boolean showWeeks, boolean showHours, EquinoxViewOptions options )
    {
        Calendar calendar = data.eventCalendarThisYear();
        if (now != null && calendar != null)
        {
            String noteText = delta_utils.timeDeltaDisplayString(now.getTime(), calendar.getTime(), showWeeks, showHours).toString();

            if (calendar.before(Calendar.getInstance()))
            {
                String noteString = context.getString(R.string.ago, noteText);
                SpannableString noteSpan = (text_note.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, (highlighted ? options.noteColor : options.disabledColor))
                        : SuntimesUtils.createBoldSpan(null, noteString, noteText));
                text_note.setText(noteSpan);

            } else {
                String noteString = context.getString(R.string.hence, noteText);
                SpannableString noteSpan = (text_note.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, options.noteColor)
                        : SuntimesUtils.createBoldSpan(null, noteString, noteText));
                text_note.setText(noteSpan);
            }
        } else {
            text_note.setText("");
        }

        if (highlighted) {
            text_datetime.setTypeface(text_datetime.getTypeface(), Typeface.BOLD);
            text_datetime.setPaintFlags(text_datetime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            text_datetime.setTypeface(text_datetime.getTypeface(), Typeface.NORMAL);
            text_datetime.setPaintFlags(text_datetime.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        }
    }

    protected void themeViews(Context context, SuntimesEquinoxSolsticeData data, EquinoxViewOptions options)
    {
        if (options.labelColor != null) {
            text_label.setTextColor(SuntimesUtils.colorStateList(options.labelColor, (options.minimized && highlighted ? options.labelColor : options.disabledColor)));
        }
        if (options.textColor != null) {
            text_note.setTextColor(SuntimesUtils.colorStateList(options.textColor, (highlighted ? options.textColor : options.disabledColor)));
        }
        int modeColor = options.getColorForMode(data.timeMode());
        text_datetime.setTextColor(SuntimesUtils.colorStateList(modeColor, (highlighted ? modeColor : options.disabledColor)));

        if (options.timeSizeSp != null)
        {
            text_label.setTextSize(options.timeSizeSp);
            text_note.setTextSize(options.timeSizeSp);
            text_datetime.setTextSize(options.timeSizeSp);
        }
    }

    @Deprecated
    protected void themeViews(Context context, @NonNull SuntimesTheme theme, EquinoxViewOptions options)
    {
        options.labelColor = options.textColor = theme.getTextColor();
        options.timeSizeSp = theme.getTimeSizeSp();
    }

    public void adjustLabelWidth( int labelWidthPx )
    {
        ViewGroup.LayoutParams layoutParams = text_label.getLayoutParams();
        layoutParams.width = labelWidthPx;
        text_label.setLayoutParams(layoutParams);
    }

    public static int suggestedLayoutResID() {
        return R.layout.info_time_solsticequinox2;
    }

}