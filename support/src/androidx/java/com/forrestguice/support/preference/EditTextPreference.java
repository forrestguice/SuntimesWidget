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

package com.forrestguice.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

public class EditTextPreference extends android.preference.EditTextPreference
{
    public EditTextPreference(Context context)
    {
        super(context);
    }

    public EditTextPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @TargetApi(21)
    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * the bug: pref doesn't format %s summary values (pre api 11)
     * @return summary text w/ properly formatted %s values
     */
    @Override
    public CharSequence getSummary()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            return super.getSummary();  // fixed in api 11 (honeycomb); call through to super

        } else {
            CharSequence summary = super.getSummary();
            if (summary != null)
            {
                CharSequence displayValue = getText();
                if (displayValue != null)    // pre api 11; we need to format the summary (%s)
                {
                    return String.format(summary.toString(), displayValue);
                }
            }
            return summary;
        }
    }
}
