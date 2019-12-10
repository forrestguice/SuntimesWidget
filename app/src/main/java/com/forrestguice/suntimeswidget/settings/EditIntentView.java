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
package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesWidget0;

@SuppressWarnings("Convert2Diamond")
public class EditIntentView extends LinearLayout
{
    public static final String TAG = "EditIntent";

    protected static final String DIALOGTAG_HELP = "help";
    protected static final String HELPTAG_LAUNCH = "action_launch";

    private static String[] ACTION_SUGGESTIONS = new String[] {
            Intent.ACTION_VIEW, Intent.ACTION_EDIT, Intent.ACTION_INSERT, Intent.ACTION_DELETE,
            Intent.ACTION_PICK, Intent.ACTION_RUN, Intent.ACTION_SEARCH, Intent.ACTION_SYNC,
            Intent.ACTION_CHOOSER, Intent.ACTION_GET_CONTENT,
            Intent.ACTION_SEND, Intent.ACTION_SENDTO, Intent.ACTION_ATTACH_DATA,
            Intent.ACTION_WEB_SEARCH, Intent.ACTION_MAIN
    };

    private static String[] MIMETYPE_SUGGESTIONS = new String[] { "text/plain" };

    protected EditText text_launchActivity;
    protected Spinner spinner_launchType;
    protected ImageButton button_launchTest;
    protected ToggleButton button_launchMore;
    protected AutoCompleteTextView text_launchAction;
    protected EditText text_launchData;
    protected AutoCompleteTextView text_launchDataType;
    protected EditText text_launchExtras;

    public EditIntentView(Context context)
    {
        super(context);
        init(context, null);
    }

    public EditIntentView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        /**TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorChooserView, 0, 0);
        try
        {
            String labelText = a.getString(R.styleable.ColorChooserView_labelText);
            if (label != null)
            {
                label.setText(labelText);
                button.setContentDescription(labelText);
            }

            String hintText = a.getString(R.styleable.ColorChooserView_hintText);
            if (hintText != null) {
                edit.setHint(hintText);
            }

        } finally {
            a.recycle();
        }*/
    }

    private int getLayoutID(Context context,AttributeSet attrs)
    {
        int layoutID = R.layout.layout_view_editintent;
        /**TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorChooserView, 0, 0);
        try
        {
            if (a.getBoolean(R.styleable.ColorChooserView_reverse, false)) {
                layoutID = R.layout.layout_view_colorchooser_rev;
            }
        } finally {
            a.recycle();
        }*/
        return layoutID;
    }

    private void init(final Context context, AttributeSet attrs)
    {
        LayoutInflater.from(context).inflate(getLayoutID(context, attrs), this, true);

        text_launchActivity = (EditText) findViewById(R.id.appwidget_action_launch);

        button_launchMore = (ToggleButton) findViewById(R.id.appwidget_action_launch_moreButton);
        button_launchMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View layout = findViewById(R.id.appwidget_action_launch_layout);
                if (layout != null) {
                    int visibility = layout.getVisibility();
                    layout.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
                // TODO
            }
        });

        button_launchTest = (ImageButton) findViewById(R.id.appwidget_action_launch_test);
        button_launchTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                WidgetSettings.LaunchType launchType = (WidgetSettings.LaunchType)spinner_launchType.getSelectedItem();
                String launchClassName = text_launchActivity.getText().toString();
                String launchAction = text_launchAction.getText().toString();
                String launchData = text_launchData.getText().toString();
                String launchDataType = text_launchDataType.getText().toString();
                String launchExtras = text_launchExtras.getText().toString();
                Intent launchIntent;
                Class<?> launchClass;
                try {
                    launchClass = Class.forName(launchClassName);
                    launchIntent = new Intent(context, launchClass);
                    WidgetSettings.applyAction(launchIntent, launchAction.trim().isEmpty() ? null : launchAction);
                    WidgetSettings.applyData(launchIntent, (launchData.trim().isEmpty() ? null : launchData), (launchDataType.trim().isEmpty() ? null : launchDataType));
                    WidgetSettings.applyExtras(launchIntent, launchExtras.trim().isEmpty() ? null : launchExtras);
                    WidgetSettings.startIntent(context, launchIntent, launchType.name());

                } catch (ClassNotFoundException e) {
                    Log.e("LaunchApp", "LaunchApp :: " + launchClassName + " cannot be found! " + e.toString());
                    Toast.makeText(context, "Unable to start intent!", Toast.LENGTH_LONG).show();  // TODO: i18n
                }
            }
        });

        spinner_launchType = (Spinner) findViewById(R.id.appwidget_action_launch_type);
        ArrayAdapter<WidgetSettings.LaunchType> launchTypeAdapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline, WidgetSettings.LaunchType.values());
        launchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_launchType.setAdapter(launchTypeAdapter);

        text_launchAction = (AutoCompleteTextView) findViewById(R.id.appwidget_action_launch_action);
        text_launchAction.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, ACTION_SUGGESTIONS));

        text_launchData = (EditText) findViewById(R.id.appwidget_action_launch_data);
        text_launchDataType = (AutoCompleteTextView) findViewById(R.id.appwidget_action_launch_datatype);
        text_launchDataType.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, MIMETYPE_SUGGESTIONS));

        text_launchExtras = (EditText) findViewById(R.id.appwidget_action_launch_extras);

        ImageButton button_launchAppHelp = (ImageButton) findViewById(R.id.appwidget_action_launch_helpButton);
        if (button_launchAppHelp != null)
        {
            button_launchAppHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(context.getString(R.string.help_action_launch));
                    helpDialog.setShowNeutralButton(context.getString(R.string.configAction_restoreDefaults));
                    helpDialog.setNeutralButtonListener(helpDialogListener_launchApp, HELPTAG_LAUNCH);
                    helpDialog.show(fragmentManager, DIALOGTAG_HELP);
                }
            });
        }

        applyAttributes(context, attrs);
    }

    protected FragmentManager fragmentManager;
    public void setFragmentManager( FragmentManager fragmentManager ) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * getIntentClass
     */
    public String getIntentClass()
    {
        return text_launchActivity.getText().toString();
    }
    public void setIntentClass( String className )
    {
        text_launchActivity.setText(className);
    }

    /**
     * getIntentType
     */
    public WidgetSettings.LaunchType getIntentType()
    {
        return (WidgetSettings.LaunchType)spinner_launchType.getSelectedItem();
    }
    public void setIntentType( WidgetSettings.LaunchType launchType )
    {
        for (int i=0; i < spinner_launchType.getCount(); i++)
        {
            WidgetSettings.LaunchType type = (WidgetSettings.LaunchType)(spinner_launchType.getItemAtPosition(i));
            if (type.equals(launchType))
            {
                spinner_launchType.setSelection(i);
                break;
            }
        }
    }

    /**
     * getIntentAction
     */
    public String getIntentAction()
    {
        return text_launchAction.getText().toString();
    }
    public void setIntentAction(String action)
    {
        text_launchAction.setText(action);
    }

    /**
     * getIntentData
     */
    public String getIntentData()
    {
        return text_launchData.getText().toString();
    }
    public void setIntentData(String data)
    {
        text_launchData.setText(data);
    }

    /**
     * getIntentDataType
     */
    public String getIntentDataType()
    {
        return text_launchDataType.getText().toString();
    }
    public void setIntentDataType( String mimeType )
    {
        text_launchDataType.setText(mimeType);
    }

    /**
     * getIntentExtras
     */
    public String getIntentExtras()
    {
        return text_launchExtras.getText().toString();
    }
    public void setIntentExtras(String extras)
    {
        text_launchExtras.setText(extras);
    }

    /**
     * onResume()
     */
    public void onResume( FragmentManager fragments )
    {
        setFragmentManager(fragments);
        if (fragmentManager != null)
        {
            HelpDialog helpDialog = (HelpDialog) fragmentManager.findFragmentByTag(DIALOGTAG_HELP);
            if (helpDialog != null)
            {
                String tag = helpDialog.getListenerTag();
                if (tag != null && tag.equals(HELPTAG_LAUNCH)) {
                    helpDialog.setNeutralButtonListener(helpDialogListener_launchApp, HELPTAG_LAUNCH);
                }
            }
        }
    }

    /**
     * HelpDialog onShow (launch App)
     */
    private View.OnClickListener helpDialogListener_launchApp = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (text_launchActivity != null)
            {
                spinner_launchType.setSelection(0);
                text_launchAction.setText("");
                text_launchData.setText("");
                text_launchDataType.setText("");
                text_launchExtras.setText("");
                text_launchActivity.setText(WidgetSettings.PREF_DEF_ACTION_LAUNCH);
                text_launchActivity.selectAll();
                text_launchActivity.requestFocus();
            }

            if (fragmentManager != null)
            {
                HelpDialog helpDialog = (HelpDialog) fragmentManager.findFragmentByTag(DIALOGTAG_HELP);
                if (helpDialog != null) {
                    helpDialog.dismiss();
                }
            }
        }
    };

}
