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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmAddon;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.settings.EditBottomSheetDialog;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooser;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooserView;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;

public class EditEventDialog extends EditBottomSheetDialog
{
    /* EventType */
    protected AlarmEventProvider.EventType type = EventSettings.PREF_DEF_EVENT_TYPE;
    public AlarmEventProvider.EventType getEventType() {
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
        return uri;
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
    public void setEvent(EventSettings.EventAlias event)
    {
        type = event.getType();
        setEventID(event.getID());
        setEventLabel(event.getLabel());
        setEventColor(event.getColor());
        setEventUri(event.getUri());
        updateViews(getActivity(), type);
    }

    protected EditText edit_eventID, edit_label, edit_uri;
    protected ColorChooser choose_color;

    @SuppressLint("SetTextI18n")
    protected void setAngle(int value ) {
        angle = value;
        if (edit_angle != null) {
            edit_angle.setText(Integer.toString(angle));
        }
    }
    private int angle = 0;
    protected EditText edit_angle;

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
        choose_color.setCollapsed(true);

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

        edit_angle = (EditText) dialogContent.findViewById(R.id.edit_event_angle);
        if (edit_angle != null) {
            edit_angle.addTextChangedListener(angleWatcher);
        }

        if (eventID == null) {
            eventID = EventSettings.suggestEventID(context);
            if (label == null || label.trim().isEmpty()) {
                label = eventID;
            }
        }

        super.initViews(context, dialogContent, savedState);
    }

    @Override
    protected void updateViews(Context context)
    {
        edit_eventID.setText(eventID);
        edit_label.setText(label != null ? label : eventID);
        edit_uri.setText(uri != null ? uri : "");
        choose_color.setColor(color);
        updateViews(context, getEventType());
    }

    protected void updateViews(Context context, AlarmEventProvider.EventType type)
    {
        switch (type)
        {
            case SUN_ELEVATION:
                if (edit_angle != null)
                {
                    if (uri != null) {
                        AlarmEventProvider.SunElevationEvent event0 = AlarmEventProvider.SunElevationEvent.valueOf(Uri.parse(uri).getLastPathSegment());
                        setAngle(event0.getAngle());
                    }
                }
                break;

            case DATE:
            case SOLAREVENT:
            default:
                break;
        }
    }

    @Override
    protected boolean validateInput()
    {
        String id = edit_eventID.getText().toString();
        String label = edit_label.getText().toString();

        if (id.trim().isEmpty() || id.contains(" ")) {
            edit_eventID.setError(getContext().getString(R.string.editevent_dialog_id_error));
            return false;
        } else edit_eventID.setError(null);

        if (label.trim().isEmpty()) {
            edit_label.setError(getContext().getString(R.string.editevent_dialog_label_error));
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

    private TextWatcher angleWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int angle = Integer.parseInt(s.toString());
                setEventUri(AlarmAddon.getEventCalcUri(AUTHORITY, AlarmEventProvider.SunElevationEvent.NAME_PREFIX + angle));
            } catch (NumberFormatException e) {
                Log.e("EditEventDialog", "not an angle: " + e);
            }
        }
    };

    @Override
    protected int getLayoutID() {
        return R.layout.layout_dialog_event_edit;
    }

    @Override
    protected void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            ViewUtils.initPeekHeight(getDialog(), R.id.layout_dialog_content0);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

}
