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

package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.security.InvalidParameterException;

import static com.forrestguice.suntimeswidget.themes.SuntimesTheme.THEME_NAME;

public class SuntimesWidgetThemeConfigActivity extends AppCompatActivity
{
    public static final String PARAM_MODE = "mode";

    public static final int ADD_THEME_REQUEST = 0;
    public static final int EDIT_THEME_REQUEST = 1;
    public static enum UIMode { ADD_THEME, EDIT_THEME }

    private static UIMode mode = UIMode.ADD_THEME;
    public UIMode getMode()
    {
        return mode;
    }

    private UIMode param_mode = null;
    private String param_themeName = null;

    private EditText editName, editDisplay;

    public SuntimesWidgetThemeConfigActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        initLocale();
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_themeconfig);

        Intent intent = getIntent();
        param_mode = (UIMode)intent.getSerializableExtra(PARAM_MODE);
        param_themeName = intent.getStringExtra(THEME_NAME);

        mode = (param_mode == null) ? UIMode.ADD_THEME : param_mode;
        initViews(this);
        loadTheme(param_themeName);
    }

    private void initLocale()
    {
        AppSettings.initLocale(this);
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    protected void initViews( Context context )
    {
        TextView titleText = (TextView)findViewById(R.id.text_title);
        editName = (EditText)findViewById(R.id.edit_themeName);
        editDisplay = (EditText)findViewById(R.id.edit_themeDisplay);

        switch (mode)
        {
            case EDIT_THEME:
                titleText.setText(getString(R.string.configLabel_widgetThemeEdit));
                editName.setEnabled(false);
                break;

            case ADD_THEME:
            default:
                titleText.setText(getString(R.string.configLabel_widgetThemeAdd));
                editName.setEnabled(true);
                break;

        }
    }

    /**
     * loads values from an existing theme into ui fields,
     */
    protected void loadTheme( String themeName )
    {
        if (themeName != null)
        {
            editName.setText((mode == UIMode.ADD_THEME) ? generateThemeName(themeName) : themeName);

            try {
                SuntimesTheme theme = WidgetThemes.loadTheme(this, themeName);
                editDisplay.setText(theme.themeDisplayString());

            } catch (InvalidParameterException e) {
                Log.e("loadTheme", "unable to load theme: " + e);
            }
        }
    }

    protected String generateThemeName( String suggestedName )
    {
        int i = 1;
        String generatedName = suggestedName;
        while (WidgetThemes.valueOf(generatedName) != null)
        {
            generatedName = suggestedName + i;
            i++;
        }
        return generatedName;
    }

    protected boolean validateInput()
    {
        if (WidgetThemes.valueOf(editName.getText().toString()) != null)
        {
            editName.setError("ID already taken.");
            return false;
        }

        if (editDisplay.getText().toString().trim().isEmpty())
        {
            editName.setError("Display String must not be empty.");
            return false;
        }

        return true;
    }


}
