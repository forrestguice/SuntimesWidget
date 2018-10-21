/**
    Copyright (C) 2017 Forrest Guice
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
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

/**
 * A ListPreference where each list item shows two lines; a title and summary.
 */
public class SummaryListPreference extends com.forrestguice.suntimeswidget.settings.ListPreference
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
    public void onPrepareDialogBuilder(AlertDialog.Builder builder )
    {
        int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "1"));
        ListAdapter adapter = new SummaryListAdapter(getContext(), R.layout.layout_listitem_checkedtwoline, getEntries(), summaries, index);
        builder.setAdapter(adapter, this);
        super.onPrepareDialogBuilder(builder);
    }

    /**
     * SummaryListAdapter
     */
    public static class SummaryListAdapter extends ArrayAdapter<CharSequence>
    {
        private CharSequence[] summaries;
        private int layoutID;
        private int index;

        public SummaryListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull CharSequence[] entries, @NonNull CharSequence[] summaries, int i)
        {
            super(context, resource, entries);
            this.summaries = summaries;
            this.layoutID = resource;
            this.index = i;
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View row = convertView;
            if (row == null)
            {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                row = inflater.inflate(this.layoutID, parent, false);
            }

            CheckedTextView checkedText = (CheckedTextView)row.findViewById(android.R.id.text1);
            checkedText.setText(getItem(position));
            checkedText.setChecked((position == index));

            TextView summaryText = (TextView)row.findViewById(android.R.id.text2);
            if (summaryText != null)
            {
                summaryText.setText(getSummary(position));
            }

            return row;
        }

        private CharSequence getSummary(int i)
        {
            if (i >= 0 && i < summaries.length)
                return summaries[i];
            else return "";
        }
    }

}
