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
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;

public class EquinoxView extends LinearLayout
{
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedCard";
    public static final String KEY_UI_CARDISNEXTYEAR = "cardIsNextYear";

    private SuntimesUtils utils = new SuntimesUtils();
    private boolean userSwappedCard = false;

    private ViewFlipper flipper;           // flip between

    private TextView txt_equinox_vernal;       // this year
    private TextView txt_solstice_summer;
    private TextView txt_equinox_autumnal;
    private TextView txt_solstice_winter;

    private TextView txt_equinox_vernal2;      // and next year
    private TextView txt_solstice_summer2;
    private TextView txt_equinox_autumnal2;
    private TextView txt_solstice_winter2;

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

    private void init(Context context)
    {
        inflate(context, R.layout.layout_view_equinox, this);

        flipper = (ViewFlipper)findViewById(R.id.info_equinoxsolstice_flipper);
        flipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));

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

    protected void updateViews( Context context, SuntimesEquinoxSolsticeDataset data )
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
    }

    @Override
    public Parcelable onSaveInstanceState( )
    {
        boolean cardIsNextYear = (flipper.getDisplayedChild() != 0);
        Log.d("DEBUG", "EquinoxView onSaveInstanceState");

        Bundle bundle = new Bundle();
        bundle.putBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, cardIsNextYear);
        bundle.putBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState( Parcelable state )
    {
        Log.d("DEBUG", "EquinoxView onRestoreInstanceState");
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;

            boolean cardIsNextYear = bundle.getBoolean(EquinoxView.KEY_UI_CARDISNEXTYEAR, false);
            flipper.setDisplayedChild((cardIsNextYear ? 1 : 0));

            userSwappedCard = bundle.getBoolean(KEY_UI_USERSWAPPEDCARD, false);
        }
        super.onRestoreInstanceState(state);
    }

}
