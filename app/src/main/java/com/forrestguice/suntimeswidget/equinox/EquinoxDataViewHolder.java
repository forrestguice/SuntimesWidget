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
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * @see EquinoxDataAdapter
 */
public class EquinoxDataViewHolder extends RecyclerView.ViewHolder
{
    protected static SuntimesUtils utils = new SuntimesUtils();

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
        text_label.setText(data.timeMode().getLongDisplayString());

        SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, data.eventCalendarThisYear(), WidgetSettings.loadShowTimeDatePref(context, 0), WidgetSettings.loadShowSecondsPref(context, 0));
        text_datetime.setText(timeText.toString());

        focusView.setVisibility(selected ? View.VISIBLE : View.GONE);
        button_menu.setVisibility(selected ? View.VISIBLE : View.GONE);

        updateNote(context, Calendar.getInstance(), data, WidgetSettings.loadShowWeeksPref(context, 0), WidgetSettings.loadShowHoursPref(context, 0), options);
        themeViews(context, data, options);
    }

    public void updateNote( Context context, Calendar now, SuntimesEquinoxSolsticeData data, boolean showWeeks, boolean showHours, EquinoxViewOptions options )
    {
        Calendar calendar = data.eventCalendarThisYear();
        if (now != null && calendar != null)
        {
            String noteText = utils.timeDeltaDisplayString(now.getTime(), calendar.getTime(), showWeeks, showHours).toString();

            if (calendar.before(Calendar.getInstance()))
            {
                String noteString = context.getString(R.string.ago, noteText);
                SpannableString noteSpan = (text_note.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, (options.minimized || highlighted ? options.noteColor : options.disabledColor))
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
    }

    protected void themeViews(Context context, SuntimesEquinoxSolsticeData data, EquinoxViewOptions options)
    {
        if (options.labelColor != null) {
            text_label.setTextColor(options.labelColor);
        }
        if (options.textColor != null) {
            text_note.setTextColor(options.textColor);
        }
        text_datetime.setTextColor(options.getColorForMode(data.timeMode()));

        if (options.timeSizeSp != null)
        {
            text_label.setTextSize(options.timeSizeSp);
            text_note.setTextSize(options.timeSizeSp);
            text_datetime.setTextSize(options.timeSizeSp);
        }
    }

    protected void themeViews(Context context, @NonNull SuntimesTheme theme, EquinoxViewOptions options)
    {
        options.labelColor = options.textColor = theme.getTextColor();

        text_label.setTextColor(theme.getTextColor());
        text_note.setTextColor(theme.getTextColor());

        text_label.setTextSize(theme.getTextSizeSp());
        text_note.setTextSize(theme.getTextSizeSp());
        text_datetime.setTextSize(theme.getTextSizeSp());
    }

    public static final int suggestedLayoutResID() {
        return R.layout.info_time_solsticequinox2;
    }

}