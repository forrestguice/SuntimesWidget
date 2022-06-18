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

package com.forrestguice.suntimeswidget.events;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.EditBottomSheetDialog;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooser;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooserView;

public class EditEventDialog extends EditBottomSheetDialog
{
    /* EventType */
    protected EventSettings.EventType type = EventSettings.PREF_DEF_EVENT_TYPE;
    public EventSettings.EventType getEventType() {
        return type;
    }

    /* Event ID */
    protected String eventID = null;
    public String getEventID() {
        return (edit_eventID != null ? edit_eventID.getText().toString() : eventID);
    }
    public void setEventID(String id) {
        eventID = id;
        if (edit_eventID != null) {
            edit_eventID.setText(eventID);
        }
    }

    /* Event Label */
    protected String label = null;
    public String getEventLabel() {
        return (edit_label != null ? edit_label.getText().toString() : label);
    }
    public void setEventLabel(String value) {
        label = value;
        if (edit_label != null) {
            edit_label.setText(label != null ? label : eventID);
        }
    }

    /* Event Color */
    protected Integer color = EventSettings.PREF_DEF_EVENT_COLOR;
    public Integer getEventColor() {
        return (choose_color != null ? choose_color.getColor() : color);
    }
    public void setEventColor(Integer value) {
        color = value;
        if (choose_color != null) {
            choose_color.setColor(color);
        }
    }

    /* Event Uri */
    protected String uri = null;
    public String getEventUri() {
        return (edit_uri != null ? edit_uri.getText().toString() : uri);
    }
    public void setEventUri(String value) {
        uri = value;
        if (edit_uri != null) {
            edit_uri.setText(uri);
        }
    }

    /* Event */
    public EventSettings.EventAlias getEvent() {
        return new EventSettings.EventAlias(type, getEventID(), getEventLabel(), getEventColor(), getEventUri());
    }
    public void setEvent(EventSettings.EventAlias event) {
        setEventID(event.getID());
        setEventLabel(event.getLabel());
        setEventColor(event.getColor());
        setEventUri(event.getUri());
    }

    protected EditText edit_eventID, edit_label, edit_uri;
    protected ColorChooser choose_color;

    @Override
    protected void initViews(Context context, View dialogContent, @Nullable Bundle savedState)
    {
        edit_eventID = (EditText) dialogContent.findViewById(R.id.edit_event_id);
        //edit_eventID.setAdapter(adapter);
        //edit_eventID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //    @Override
        //    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //        setEventID((String)parent.getItemAtPosition(position));
        //    }
        //});
        edit_eventID.addTextChangedListener(idWatcher);

        edit_label = (EditText) dialogContent.findViewById(R.id.edit_label);
        edit_label.addTextChangedListener(labelWatcher);

        edit_uri = (EditText) dialogContent.findViewById(R.id.edit_uri);

        ColorChooserView colorView = (ColorChooserView) dialogContent.findViewById(R.id.chooser_eventColor);
        choose_color = new ColorChooser(context, colorView.getLabel(), colorView.getEdit(), colorView.getButton(), "event");
        choose_color.setFragmentManager(getChildFragmentManager());

        ImageButton cancelButton = (ImageButton) dialogContent.findViewById(R.id.cancel_button);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().cancel();
                }
            });
        }

        ImageButton saveButton = (ImageButton) dialogContent.findViewById(R.id.save_button);
        if (saveButton != null) {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accept();
                }
            });
        }

        if (eventID == null) {
            eventID = EventSettings.suggestEventID(context);
            if (label == null || label.trim().isEmpty()) {
                label = eventID;
            }
        }

        updateViews(context);
        super.initViews(context, dialogContent, savedState);
    }

    @Override
    protected void updateViews(Context context)
    {
        edit_eventID.setText(eventID);
        edit_label.setText(label != null ? label : eventID);
        edit_uri.setText(uri != null ? uri : "");
        choose_color.setColor(color);
    }

    @Override
    protected boolean validateInput()
    {
        String id = edit_eventID.getText().toString();
        String label = edit_label.getText().toString();

        if (id.trim().isEmpty() || id.contains(" ")) {
            edit_eventID.setError(getContext().getString(R.string.addaction_error_id));    // TODO: msg
            return false;
        } else edit_eventID.setError(null);

        if (label.trim().isEmpty()) {
            edit_label.setError(getContext().getString(R.string.addaction_error_title));    // TODO: msg
            return false;
        } else edit_label.setError(null);

        return true;
    }


    private TextWatcher labelWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            checkInput();
        }
    };

    private TextWatcher idWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            checkInput();
        }
    };

    @Override
    protected int getLayoutID() {
        return R.layout.layout_dialog_event_edit;
    }
}
