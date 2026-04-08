/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

import com.forrestguice.support.preference.EditTextPreference;

public class LengthPreference extends EditTextPreference
{
    public LengthPreference(Context context)
    {
        super(context);
    }

    public LengthPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @TargetApi(21)
    public LengthPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public LengthPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText( String text )
    {
        if (!isMetric) {
            double doubleValue = Double.parseDouble(text);
            super.setText(Double.toString( doubleValue * (1d / 3.28084d) ));
        } else super.setText(text);
    }

    @Override
    public String getText()
    {
        if (!isMetric) {
            double doubleValue = Double.parseDouble(super.getText());
            return Double.toString(3.28084d * doubleValue);
        } else return super.getText();
    }

    private boolean isMetric = true;
    public boolean isMetric() {
        return isMetric;
    }
    public void setMetric(boolean isMetric) {
        this.isMetric = isMetric;
    }
}
