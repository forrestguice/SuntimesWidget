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
package com.forrestguice.suntimeswidget.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

import java.util.Calendar;

import static com.forrestguice.suntimeswidget.calendar.CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_GREGORIAN;
import static com.forrestguice.suntimeswidget.calendar.CalendarSettings.PREF_KEY_CALENDAR_FORMATPATTERN;
import static com.forrestguice.suntimeswidget.calendar.CalendarSettings.PREF_KEY_CALENDAR_MODE;

public class CalendarFormatDialog extends DialogFragment
{
    public CalendarFormatDialog()
    {
        super();
        Bundle defaultArgs = new Bundle();
        defaultArgs.putString(PREF_KEY_CALENDAR_MODE, CalendarMode.GREGORIAN.name());
        defaultArgs.putString(PREF_KEY_CALENDAR_FORMATPATTERN, PREF_DEF_CALENDAR_FORMATPATTERN_GREGORIAN);
        setArguments(defaultArgs);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        super.onCreate(savedState);
        View dialogContent = inflater.inflate(R.layout.layout_dialog_calendarformat, null);
        initViews(getActivity(), dialogContent);
        updateViews(getContext());
        return dialogContent;
    }

    protected Spinner spinner_calendarFormat;
    protected EditText text_calendarFormatPattern;
    protected ImageButton button_calendarFormatPatternHelp;
    protected ImageButton button_calendarFormatEdit;

    protected void initViews( final Context context, View dialogContent )
    {
        spinner_calendarFormat = (Spinner) dialogContent.findViewById(R.id.appwidget_general_calendarFormat);
        if (spinner_calendarFormat != null)
        {
            final ArrayAdapter<CalendarFormat> adapter = new ArrayAdapter<CalendarFormat>(context, R.layout.layout_listitem_oneline, CalendarFormat.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_calendarFormat.setAdapter(adapter);
            spinner_calendarFormat.setOnItemSelectedListener(onCalendarFormatSelected);
        }

        text_calendarFormatPattern = (EditText) dialogContent.findViewById(R.id.appwidget_general_calendarPattern);
        if (text_calendarFormatPattern != null) {
            text_calendarFormatPattern.setImeOptions(EditorInfo.IME_ACTION_DONE);
            text_calendarFormatPattern.setOnEditorActionListener(onCalendarFormatPatternEdited);
            text_calendarFormatPattern.setOnFocusChangeListener(onCalendarFormatPatternFocus);
        }

        button_calendarFormatEdit = (ImageButton) dialogContent.findViewById(R.id.appwidget_general_calendarFormat_editButton);
        if (button_calendarFormatEdit != null) {
            button_calendarFormatEdit.setOnClickListener(onEditButtonClicked);
        }

        button_calendarFormatPatternHelp = (ImageButton) dialogContent.findViewById(R.id.appwidget_general_calendarPattern_helpButton);
        if (button_calendarFormatPatternHelp != null) {
            button_calendarFormatPatternHelp.setOnClickListener(onHelpButtonClicked);
        }
    }

    protected void updateViews(Context context) {
        setCalendarFormat(getFormatPattern());
    }

    protected int setCalendarFormat(@NonNull CalendarFormat format)
    {
        if (spinner_calendarFormat != null) {
            SpinnerAdapter adapter = spinner_calendarFormat.getAdapter();
            int n = (adapter != null ? adapter.getCount() : 0);
            for (int i=0; i<n; i++) {
                CalendarFormat item = (CalendarFormat) adapter.getItem(i);
                if (format.equals(item)) {
                    spinner_calendarFormat.setSelection(i);
                    return i;
                }
            }
        }
        return -1;
    }

    protected int setCalendarFormat(@NonNull String pattern)
    {
        if (spinner_calendarFormat != null) {
            SpinnerAdapter adapter = spinner_calendarFormat.getAdapter();
            int n = (adapter != null ? adapter.getCount() : 0);
            for (int i=n-1; i>=0; i--) {    // CUSTOM (0) should be considered last
                CalendarFormat item = (CalendarFormat) adapter.getItem(i);
                if (pattern.equals(item.getPattern())) {
                    spinner_calendarFormat.setSelection(i);
                    return i;
                }
            }
            setCalendarFormat(CalendarFormat.CUSTOM);
        }
        return -1;
    }

    public void updateCustomCalendarFormat(String pattern)
    {
        //Log.d("DEBUG", "updateCustomCalendarFormat");
        CalendarMode mode = getCalendarMode();
        CalendarFormat.CUSTOM.setPattern(pattern);
        CalendarFormat.CUSTOM.initDisplayString(getActivity(), mode, Calendar.getInstance());
        notifyDataSetChanged_calendarFormatAdapter();
    }

    public void notifyDataSetChanged_calendarFormatAdapter()
    {
        if (spinner_calendarFormat != null)
        {
            try {
                @SuppressWarnings("unchecked")
                ArrayAdapter<CalendarFormat> adapter = (ArrayAdapter<CalendarFormat>) spinner_calendarFormat.getAdapter();
                adapter.notifyDataSetChanged();
                //Log.d("DEBUG", "notifyDataSetChanged_calendarFormatAdapter");
            } catch (ClassCastException e) {
                Log.e(getClass().getSimpleName(), "Failed to update calendar format adapter: " + e);
            }
        }
    }

    private final AdapterView.OnItemSelectedListener onCalendarFormatSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            CalendarFormat item = (CalendarFormat)parent.getItemAtPosition(position);
            text_calendarFormatPattern.setEnabled(item == CalendarFormat.CUSTOM);
            button_calendarFormatEdit.setVisibility(item == CalendarFormat.CUSTOM ? View.GONE : View.VISIBLE);
            text_calendarFormatPattern.setText(item.getPattern());
            getArguments().putString(PREF_KEY_CALENDAR_FORMATPATTERN, item.getPattern());
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private final TextView.OnEditorActionListener onCalendarFormatPatternEdited = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
            String pattern = v.getText().toString();
            getArguments().putString(PREF_KEY_CALENDAR_FORMATPATTERN, pattern);

            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE: case EditorInfo.IME_ACTION_NEXT: case EditorInfo.IME_ACTION_PREVIOUS:
                case EditorInfo.IME_ACTION_SEARCH: case EditorInfo.IME_ACTION_GO: case EditorInfo.IME_ACTION_SEND:
                    updateCustomCalendarFormat(pattern);
                    break;
            }
            return false;
        }
    };
    private final View.OnFocusChangeListener onCalendarFormatPatternFocus =  new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {
            String pattern = text_calendarFormatPattern.getText().toString();
            getArguments().putString(PREF_KEY_CALENDAR_FORMATPATTERN, pattern);

            if (!hasFocus && v.isEnabled()) {
                updateCustomCalendarFormat(pattern);
            }
        }
    };

    public boolean applyFocusedPattern()
    {
        if (text_calendarFormatPattern.hasFocus())
        {
            String pattern = text_calendarFormatPattern.getText().toString();
            getArguments().putString(PREF_KEY_CALENDAR_FORMATPATTERN, pattern);
            return true;
        } else return false;
    }

    private final View.OnClickListener onEditButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setCalendarFormat(CalendarFormat.CUSTOM);
            String pattern = text_calendarFormatPattern.getText().toString();
            getArguments().putString(PREF_KEY_CALENDAR_FORMATPATTERN, pattern);
            updateCustomCalendarFormat(pattern);
            if (listener != null) {
                listener.onEditClick(CalendarFormatDialog.this);
            }
        }
    };

    private final View.OnClickListener onHelpButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onHelpClick(CalendarFormatDialog.this);
            }
        }
    };

    /**
     * setCalendarMode
     */
    public void setCalendarMode(CalendarMode mode) {
        getArguments().putString(PREF_KEY_CALENDAR_MODE, mode.name());
        updateViews(getActivity());
    }
    public CalendarMode getCalendarMode() {
        try {
            return CalendarMode.valueOf(getArguments().getString(PREF_KEY_CALENDAR_MODE));
        } catch (IllegalArgumentException e) {
            Log.e(getClass().getSimpleName(), "getCalendarMode: " + e);
            return CalendarSettings.PREF_DEF_CALENDAR_MODE;
        }
    }

    /**
     * setFormatPattern
     */
    public void setFormatPattern(String value) {
        getArguments().putString(PREF_KEY_CALENDAR_FORMATPATTERN, value);
        updateViews(getActivity());
    }
    public String getFormatPattern() {
        return getArguments().getString(PREF_KEY_CALENDAR_FORMATPATTERN);
    }

    /**
     * DialogListener
     */
    public interface DialogListener
    {
        void onChanged(CalendarFormatDialog dialog);
        void onEditClick(CalendarFormatDialog dialog);
        void onHelpClick(CalendarFormatDialog dialog);
    }

    private DialogListener listener = null;
    public void setDialogListener( DialogListener listener ) {
        this.listener = listener;
    }

}