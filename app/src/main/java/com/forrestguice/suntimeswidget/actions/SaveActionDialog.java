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

package com.forrestguice.suntimeswidget.actions;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetActions;

import java.util.Set;

/**
 * SaveActionDialog
 */
public class SaveActionDialog extends EditActionDialog
{
    @Override
    public String getIntentTitle()
    {
        if (edit.edit_label != null) {
            return edit.edit_label.getText().toString();
        } else return null;
    }
    public void setIntentTitle(String value) {
        intentTitle = value;
    }

    public String getIntentDesc() {
        if (edit.edit_desc != null) {
            return edit.edit_desc.getText().toString();
        } else return null;
    }

    public String getIntentID()
    {
        if (edit_intentID != null) {
            return edit_intentID.getText().toString();
        } else return intentID;
    }
    public void setIntentID(String id) {
        intentID = id;
        if (edit_intentID != null) {
            edit_intentID.setText(intentID);
        }
    }
    public String suggestedIntentID(@Nullable Context context)
    {
        suggested_c = 0;
        String suggested;
        do {
            suggested = ((context != null) ? context.getString(R.string.addaction_custname, Integer.toString(suggested_c)) : suggested_c + "");
            suggested_c++;
        } while (intentIDs != null && intentIDs.contains(suggested));
        return suggested;
    }
    private int suggested_c = 1;

    private String intentID = null, intentTitle = "";
    private Set<String> intentIDs;
    private AutoCompleteTextView edit_intentID;
    private TextView text_note;
    private ImageButton button_suggest;

    private EditActionView edit;
    public EditActionView getEdit() {
        return edit;
    }

    @Override
    protected void updateViews(Context context)
    {
        edit.setIntentTitle(intentTitle);
        edit_intentID.setText(intentID);
        text_note.setVisibility(View.GONE);
        button_suggest.setVisibility(View.GONE);

        if ((intentIDs.contains(intentID)))
        {
            edit.setIntentTitle(WidgetActions.loadActionLaunchPref(context, 0, intentID, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE));
            text_note.setVisibility(View.VISIBLE);
            button_suggest.setVisibility(View.VISIBLE);
            edit_intentID.selectAll();
            edit_intentID.requestFocus();
        }

        if (intentID.trim().isEmpty()) {
            button_suggest.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected boolean validateInput()
    {
        String id = edit_intentID.getText().toString();
        String title = edit.getIntentTitle();

        if (id.trim().isEmpty() || id.contains(" ")) {
            edit_intentID.setError(getContext().getString(R.string.addaction_error_id));
            return false;
        } else edit_intentID.setError(null);

        if (title.trim().isEmpty()) {
            edit.edit_label.setError(getContext().getString(R.string.addaction_error_title));
            return false;
        } else edit.text_label.setError(null);

        return true;
    }

    @Override
    protected void initViews(Context context, View dialogContent, @Nullable Bundle savedState)
    {
        intentIDs = WidgetActions.loadActionLaunchList(context, 0);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, intentIDs.toArray(new String[0]));

        edit = (EditActionView) dialogContent.findViewById(R.id.edit_intent);
        edit.setFragmentManager(getFragmentManager());
        edit.edit_label.addTextChangedListener(titleWatcher);

        text_note = (TextView) dialogContent.findViewById(R.id.text_note);

        edit_intentID = (AutoCompleteTextView) dialogContent.findViewById(R.id.edit_intent_id);
        edit_intentID.setAdapter(adapter);
        edit_intentID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setIntentID((String)parent.getItemAtPosition(position));
            }
        });
        edit_intentID.addTextChangedListener(idWatcher);

        button_suggest = (ImageButton) dialogContent.findViewById(R.id.edit_intent_reset);
        button_suggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIntentID(suggestedIntentID(getContext()));
                updateViews(getContext());
                edit_intentID.selectAll();
                edit_intentID.requestFocus();
            }
        });

        if (intentID == null) {
            intentID = suggestedIntentID(context);
            if (intentTitle == null || intentTitle.trim().isEmpty()) {
                intentTitle = context.getString(R.string.addaction_custtitle, Integer.toString(suggested_c - 1));
            }
        }

        updateViews(context);
        super.initViews(context, dialogContent, savedState);
    }

    private final TextWatcher titleWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            checkInput();
        }
    };

    private final TextWatcher idWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            text_note.setVisibility( (intentIDs.contains(s.toString())) ? View.VISIBLE : View.GONE );
            button_suggest.setVisibility( (intentIDs.contains(s.toString()) || s.toString().trim().isEmpty()) ? View.VISIBLE : View.GONE );
            checkInput();
        }
    };

    public void setValuesFrom(EditActionView view)
    {
        edit.initFromOther(view);
        checkInput();
    }

    @Override
    protected int getLayoutID() {
        return R.layout.layout_dialog_intent_save;
    }
}
