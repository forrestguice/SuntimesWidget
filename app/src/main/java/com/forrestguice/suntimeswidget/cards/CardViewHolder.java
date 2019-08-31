/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.cards;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.MoonPhaseView;
import com.forrestguice.suntimeswidget.MoonRiseSetView;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import java.util.ArrayList;
import java.util.HashMap;

public class CardViewHolder extends RecyclerView.ViewHolder
{
    public ImageButton btn_flipperNext;
    public ImageButton btn_flipperPrev;

    public View sunriseHeader;
    public TextView header_sunrise;
    public ImageView icon_sunrise;

    public View sunsetHeader;
    public TextView header_sunset;
    public ImageView icon_sunset;

    public TextView txt_date;

    public ArrayList<TimeFieldRow> rows;
    public TimeFieldRow row_astro, row_nautical, row_civil, row_actual, row_solarnoon;
    public TimeFieldRow row_gold, row_blue8, row_blue4;
    public HashMap<SolarEvents, TextView> timeFields;

    public LinearLayout layout_daylength;
    public TextView txt_daylength;
    public TextView txt_lightlength;

    public TextView moonlabel;
    public MoonPhaseView moonphase;
    public MoonRiseSetView moonrise;
    public View moonClickArea;

    public CardViewHolder(View view)
    {
        super(view);

        txt_date = (TextView) view.findViewById(R.id.text_date);

        sunriseHeader = view.findViewById(R.id.header_time_sunrise);
        header_sunrise = (TextView) view.findViewById(R.id.label_time_sunrise);
        icon_sunrise = (ImageView) view.findViewById(R.id.icon_time_sunrise);

        sunsetHeader = view.findViewById(R.id.header_time_sunset);
        header_sunset = (TextView) view.findViewById(R.id.label_time_sunset);
        icon_sunset = (ImageView) view.findViewById(R.id.icon_time_sunset);

        layout_daylength = (LinearLayout) view.findViewById(R.id.layout_daylength);
        txt_daylength = (TextView) view.findViewById(R.id.text_daylength);
        txt_lightlength = (TextView) view.findViewById(R.id.text_lightlength);

        moonlabel = (TextView) view.findViewById(R.id.text_time_label_moon);
        moonphase = (MoonPhaseView) view.findViewById(R.id.moonphase_view);
        moonClickArea = view.findViewById(R.id.moonphase_clickArea);
        moonrise = (MoonRiseSetView) view.findViewById(R.id.moonriseset_view);
        moonrise.setShowExtraField(false);

        rows = new ArrayList<>();
        rows.add(row_actual = new TimeFieldRow(view, R.id.text_time_label_official, R.id.text_time_sunrise_actual, R.id.text_time_sunset_actual));
        rows.add(row_civil = new TimeFieldRow(view, R.id.text_time_label_civil, R.id.text_time_sunrise_civil, R.id.text_time_sunset_civil));
        rows.add(row_nautical = new TimeFieldRow(view, R.id.text_time_label_nautical, R.id.text_time_sunrise_nautical, R.id.text_time_sunset_nautical));
        rows.add(row_astro = new TimeFieldRow(view, R.id.text_time_label_astro, R.id.text_time_sunrise_astro, R.id.text_time_sunset_astro));
        rows.add(row_solarnoon = new TimeFieldRow(view, R.id.text_time_label_noon, R.id.text_time_noon));
        rows.add(row_gold = new TimeFieldRow(view, R.id.text_time_label_golden, R.id.text_time_golden_morning, R.id.text_time_golden_evening));
        rows.add(row_blue8 = new TimeFieldRow(view, R.id.text_time_label_blue8, R.id.text_time_blue8_morning, R.id.text_time_blue8_evening));
        rows.add(row_blue4 = new TimeFieldRow(view, R.id.text_time_label_blue4, R.id.text_time_blue4_morning, R.id.text_time_blue4_evening));

        timeFields = new HashMap<>();
        timeFields.put(SolarEvents.SUNRISE, row_actual.getField(0));
        timeFields.put(SolarEvents.SUNSET, row_actual.getField(1));
        timeFields.put(SolarEvents.MORNING_CIVIL, row_civil.getField(0));
        timeFields.put(SolarEvents.EVENING_CIVIL, row_civil.getField(1));
        timeFields.put(SolarEvents.MORNING_NAUTICAL, row_nautical.getField(0));
        timeFields.put(SolarEvents.EVENING_NAUTICAL, row_nautical.getField(1));
        timeFields.put(SolarEvents.MORNING_ASTRONOMICAL, row_astro.getField(0));
        timeFields.put(SolarEvents.EVENING_ASTRONOMICAL, row_astro.getField(1));
        timeFields.put(SolarEvents.NOON, row_solarnoon.getField(0));
        timeFields.put(SolarEvents.MORNING_GOLDEN, row_gold.getField(0));
        timeFields.put(SolarEvents.EVENING_GOLDEN, row_gold.getField(1));
        timeFields.put(SolarEvents.MORNING_BLUE8, row_blue8.getField(0));
        timeFields.put(SolarEvents.EVENING_BLUE8, row_blue8.getField(1));
        timeFields.put(SolarEvents.MORNING_BLUE4, row_blue4.getField(0));
        timeFields.put(SolarEvents.EVENING_BLUE4, row_blue4.getField(1));
        timeFields.put(SolarEvents.MOONRISE, moonrise.getTimeViews(SolarEvents.MOONRISE)[0]);
        timeFields.put(SolarEvents.MOONSET, moonrise.getTimeViews(SolarEvents.MOONSET)[0]);

        btn_flipperNext = (ImageButton)view.findViewById(R.id.info_time_nextbtn);
        btn_flipperPrev = (ImageButton)view.findViewById(R.id.info_time_prevbtn);
    }

    public void highlightField( SolarEvents highlightEvent )
    {
        for (SolarEvents event : timeFields.keySet()) {
            if (event == highlightEvent) {
                TimeFieldRow.highlight(timeFields.get(event));
                break;
            }
        }
    }

    public void resetHighlight()
    {
        for (TimeFieldRow row : rows) {
            row.resetHighlight();
        }
    }

    /**
     * TimeFieldRow
     */
    public static class TimeFieldRow
    {
        protected TextView label;
        private TextView[] fields;

        public TimeFieldRow( TextView label, TextView ...fields )
        {
            this.label = label;
            this.fields = fields;
        }

        public TimeFieldRow(View parent, int labelID, int... fieldIDs)
        {
            if (parent != null)
            {
                this.label = (TextView) parent.findViewById(labelID);
                this.fields = new TextView[fieldIDs.length];

                for (int i=0; i<fieldIDs.length; i++) {
                    this.fields[i] = (TextView) parent.findViewById(fieldIDs[i]);
                }
            }
        }

        public TextView getLabel()
        {
            return label;
        }

        public TextView getField( int i )
        {
            if (i >= 0 && i < fields.length)
                return fields[i];
            else return null;
        }

        public void resetHighlight()
        {
            for (int i=0; i<fields.length; i++) {
                if (fields[i] != null) {
                    resetHighlight(fields[i]);
                }
            }
        }

        public static void highlight(TextView textView)
        {
            if (textView != null && textView.getVisibility() == View.VISIBLE) {
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }

        public static void resetHighlight(TextView textView)
        {
            if (textView != null && textView.getVisibility() == View.VISIBLE) {
                textView.setTypeface(Typeface.create(textView.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            }
        }

        public void updateFields( String ...values )
        {
            for (int i=0; i<values.length; i++)
            {
                if (i >= fields.length)
                    break;

                if (fields[i] != null) {
                    fields[i].setText( values[i] );
                }
            }
        }

        public void setVisible( boolean show )
        {
            int visibility = (show ? View.VISIBLE : View.GONE);

            if (label != null) {
                label.setVisibility(visibility);
            }

            for (int i=0; i<fields.length; i++) {
                if (fields[i] != null) {
                    fields[i].setVisibility(visibility);
                }
            }
        }

    }

}


