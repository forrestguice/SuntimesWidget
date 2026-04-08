/**
    Copyright (C) 2017-2019 Forrest Guice
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
import android.widget.ListAdapter;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.preference.ListPreference;

/**
 * A ListPreference where each list item shows two lines; a title and summary.
 */
public class SummaryListPreference extends ListPreference
{
    private CharSequence[] summaries;

    public SummaryListPreference(Context context)
    {
        super(context);
    }

    public SummaryListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @TargetApi(21)
    public SummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public SummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setEntrySummaries(CharSequence[] summaries)
    {
        this.summaries = summaries;
    }

    @Override
    protected ListAdapter createListAdapter(int selectedPos)
    {
        return new ListPrefAdapter(getContext(), R.layout.layout_listitem_checkedtwoline, getEntries(), summaries, selectedPos);
    }

}
