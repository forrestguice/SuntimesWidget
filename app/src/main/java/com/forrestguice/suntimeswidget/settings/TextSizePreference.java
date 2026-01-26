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

package com.forrestguice.suntimeswidget.settings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.preference.ListPreference;

public class TextSizePreference extends ListPreference
{
    public TextSizePreference(Context context) {
        super(context);
    }

    public TextSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    public TextSizePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public TextSizePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected ListAdapter createListAdapter(int selectedPos) {
        return new ListPrefAdapter(getContext(), listLayoutResId(), getEntries(), selectedPos);
    }

    @Override
    public void setValue(String value)
    {
       super.setValue(value);
       updatePreview();
    }

    protected void updatePreview() {
        // TODO
    }

    /**
     * ListAdapter
     */
    public static class ListPrefAdapter extends ListPreference.ListPrefAdapter
    {
        public ListPrefAdapter(@NonNull Context context, int resource, @NonNull CharSequence[] entries, int i) {
            super(context, resource, entries, i);
            initTextSizes(context);
        }

        public ListPrefAdapter(@NonNull Context context, int resource, @NonNull CharSequence[] entries, @NonNull CharSequence[] summaries, int i) {
            super(context, resource, entries, summaries, i);
            initTextSizes(context);
        }

        protected float[] textSizes = {12, 14, 16, 18};

        @SuppressLint("ResourceType")
        protected void initTextSizes(Context context)
        {
            textSizes[0] = context.getResources().getDimension(R.dimen.smalltext_size_small);
            textSizes[1] = context.getResources().getDimension(R.dimen.text_size_small);
            textSizes[2] = context.getResources().getDimension(R.dimen.largetext_size_medium);
            textSizes[3] = context.getResources().getDimension(R.dimen.xlargetext_size_medium);
        }

        public float getTextSizePx(int position) {
            if (position >= 0 && position < textSizes.length)
                return textSizes[position];
            else return textSizes[1];
        }

        protected void updateView(int position, View view)
        {
            CheckedTextView checkedText = (CheckedTextView) view.findViewById(android.R.id.text1);
            if (checkedText != null) {
                checkedText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSizePx(position));
            }
        }
    }

}
