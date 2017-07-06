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

package com.forrestguice.suntimeswidget.themes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Locale;

import static com.forrestguice.suntimeswidget.themes.SuntimesTheme.THEME_NAME;

public class WidgetThemeConfigActivity extends AppCompatActivity
{
    public static final int MIN_TITLE_SIZE = 8;

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

    private ActionBar actionBar;
    private EditText editName, editDisplay;
    private EditText editTitleSize;
    private PaddingChooser choosePadding;
    private ColorChooser chooseColorRise, chooseColorSet, chooseColorTitle, chooseColorText, chooseColorTime, chooseColorSuffix;

    public WidgetThemeConfigActivity()
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
        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editName = (EditText)findViewById(R.id.edit_themeName);
        // TODO: validate ID after loss of focus

        editDisplay = (EditText)findViewById(R.id.edit_themeDisplay);
        // TODO: validate name size after loss of focus

        editTitleSize = (EditText)findViewById(R.id.edit_titleSize);
        // TODO: validate title size after loss of focus

        EditText editPadding = (EditText)findViewById(R.id.edit_padding);
        choosePadding = new PaddingChooser(editPadding);
        // TODO: validate/form padding string on the fly

        EditText editColorTitle = (EditText)findViewById(R.id.edit_titleColor);
        ImageButton buttonColorTitle = (ImageButton)findViewById(R.id.editButton_titleColor);
        chooseColorTitle = new ColorChooser(editColorTitle, buttonColorTitle);
        // TODO: validate/form color string on the fly

        EditText editColorText = (EditText)findViewById(R.id.edit_textColor);
        ImageButton buttonColorText = (ImageButton)findViewById(R.id.editButton_textColor);
        chooseColorText = new ColorChooser(editColorText, buttonColorText);
        // TODO: validate/form color string on the fly

        EditText editColorRise = (EditText)findViewById(R.id.edit_sunriseColor);
        ImageButton buttonColorRise = (ImageButton)findViewById(R.id.editButton_sunriseColor);
        chooseColorRise = new ColorChooser(editColorRise, buttonColorRise);
        // TODO: validate/form color string on the fly

        EditText editColorSet = (EditText)findViewById(R.id.edit_sunsetColor);
        ImageButton buttonColorSet = (ImageButton)findViewById(R.id.editButton_sunsetColor);
        chooseColorSet = new ColorChooser(editColorSet, buttonColorSet);
        // TODO: validate/form color string on the fly

        EditText editColorTime = (EditText)findViewById(R.id.edit_timeColor);
        ImageButton buttonColorTime = (ImageButton)findViewById(R.id.editButton_timeColor);
        chooseColorTime = new ColorChooser(editColorTime, buttonColorTime);
        // TODO: validate/form color string on the fly

        EditText editColorSuffix = (EditText)findViewById(R.id.edit_suffixColor);
        ImageButton buttonColorSuffix = (ImageButton)findViewById(R.id.editButton_suffixColor);
        chooseColorSuffix = new ColorChooser(editColorSuffix, buttonColorSuffix);
        // TODO: validate/form color string on the fly

        switch (mode)
        {
            case EDIT_THEME:
                actionBar.setTitle(getString(R.string.configLabel_widgetThemeEdit));
                //applyButton.setText(getString(R.string.configAction_saveTheme));
                editName.setEnabled(false);
                break;

            case ADD_THEME:
            default:
                actionBar.setTitle(getString(R.string.configLabel_widgetThemeAdd));
                //applyButton.setText(getString(R.string.configAction_addTheme));
                editName.setEnabled(true);
                break;

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.themeconfig, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.saveTheme:
                onSaveClicked();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSaveClicked()
    {
        if (validateInput())
        {
            SuntimesTheme theme = saveTheme();
            if (theme != null)
            {
                Intent intent = new Intent();
                intent.putExtra(SuntimesTheme.THEME_NAME, theme.themeName());
                WidgetThemes.addValue(WidgetThemeConfigActivity.this, theme.themeDescriptor());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
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
                editDisplay.setText((mode == UIMode.ADD_THEME) ? generateThemeDisplayString(theme.themeDisplayString()) : theme.themeDisplayString());
                editTitleSize.setText(String.format("%s", (int)theme.getTitleSizeSp()));
                chooseColorTitle.setColor(theme.getTitleColor());
                chooseColorText.setColor(theme.getTextColor());
                chooseColorRise.setColor(theme.getSunriseTextColor());
                chooseColorSet.setColor(theme.getSunsetTextColor());
                chooseColorTime.setColor(theme.getTimeColor());
                chooseColorSuffix.setColor(theme.getTimeSuffixColor());
                choosePadding.setPadding(theme.getPadding());

            } catch (InvalidParameterException e) {
                Log.e("loadTheme", "unable to load theme: " + e);
            }
        }
    }

    /**
     *
     */
    protected SuntimesTheme saveTheme()
    {
        SuntimesTheme theme = new SuntimesTheme()
        {
            private SuntimesTheme init()
            {
                this.themeName = editName.getText().toString();
                this.themeDisplayString = editDisplay.getText().toString();
                this.themeTitleSize = Float.parseFloat(editTitleSize.getText().toString());
                this.themeTitleColor = chooseColorTitle.getColor();
                this.themeTextColor = chooseColorText.getColor();
                this.themeTimeColor = chooseColorTime.getColor();
                this.themeTimeSuffixColor = chooseColorSuffix.getColor();
                this.themeSunriseTextColor = chooseColorRise.getColor();
                this.themeSunsetTextColor = chooseColorSet.getColor();
                this.themePadding = choosePadding.getPadding();
                return this;
            }
        }.init();

        SharedPreferences themePref = getSharedPreferences(WidgetThemes.PREFS_THEMES, Context.MODE_PRIVATE);
        theme.saveTheme(themePref);
        return theme;
    }

    /**
     * @param suggestedName
     * @return
     */
    protected String generateThemeName( String suggestedName )
    {
        int i = 1;
        String generatedName = suggestedName;
        while (WidgetThemes.valueOf(generatedName) != null)
        {
            generatedName = getString(R.string.addtheme_copyname, suggestedName, i+"");
            i++;
        }
        return generatedName;
    }

    protected String generateThemeDisplayString( String suggestedName )
    {
        return getString(R.string.addtheme_copydisplay, suggestedName);
    }

    /**
     * @return true fields are valid, false one or more fields is invalid
     */
    protected boolean validateInput()
    {
        boolean isValid = true;

        String themeID = editName.getText().toString().trim();
        if (themeID.isEmpty())
        {
            editName.setError("ID must be unique (required)."); // todo: i18n
            isValid = false;
        }

        if (mode == UIMode.ADD_THEME && WidgetThemes.valueOf(editName.getText().toString()) != null)
        {
            editName.setError("ID must be unique (already taken)."); // todo: i18n
            isValid = false;
        }

        if (editDisplay.getText().toString().trim().isEmpty())
        {
            editDisplay.setError("Display string must not be empty."); // todo: i18n
            isValid = false;
        }

        try {
            int titleSize = Integer.parseInt(editTitleSize.getText().toString());
            if (titleSize < MIN_TITLE_SIZE)
            {
                editTitleSize.setError("Title size must be an integer >= " + MIN_TITLE_SIZE + ".");  // todo: i18n
                isValid = false;
            }

        } catch (NumberFormatException e) {
            editTitleSize.setError("Title size must be an integer >= " + MIN_TITLE_SIZE + ".");  // todo: i18n
            isValid = false;
        }

        return isValid;
    }

    /**
     * PaddingChooser
     */
    public static class PaddingChooser implements TextWatcher, View.OnFocusChangeListener
    {
        private int[] padding = new int[4];
        private EditText edit;

        private boolean isRunning = false, isRemoving = false;
        private char[] brackets = {'[',']'};
        private char separator = ',';

        public PaddingChooser( EditText editField )
        {
            this.edit = editField;
            edit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
            this.edit.addTextChangedListener(this);
        }

        public EditText getField()
        {
            return edit;
        }

        public int[] getPadding()
        {
            return padding;
        }
        public void setPadding( int[] padding )
        {
            for (int i=0; i<padding.length && i<this.padding.length; i++)
            {
                this.padding[i] = padding[i];
            }
            updateViews();
        }
        private void setPadding(int i, int value)
        {
            if (i >= 0 && i < padding.length)
            {
                padding[i] = value;
            }
        }
        private void setPadding(int i, String value)
        {
            try {
                setPadding(i, Integer.parseInt(value));

            } catch (NumberFormatException e) {
                setPadding(i, 0);
            }
        }

        private void updateViews()
        {
            edit.setText(toString());
        }

        public String toString()
        {
            return "" + brackets[0] + padding[0] + separator + padding[1] + separator + padding[2] + separator + padding[3] + brackets[1];
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
        {
            isRemoving = count > after;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {}

        @Override
        public void afterTextChanged(Editable editable)
        {
            if (isRunning || isRemoving)
                return;
            isRunning = true;

            insertStartBracket(editable);
            int length = editable.length();
            String text = editable.toString();

            int i0 = -1, i1 = -1, i2 = -1, i3 = -1;
            if ((i0 = text.indexOf(separator, 0)) != -1)
            {
                if ((i1 = text.indexOf(separator, i0 + 1)) != -1)
                {
                    if ((i2 = text.indexOf(separator, i1 + 1)) != -1)
                    {
                        if ((i3 = text.indexOf(separator, i2 + 1)) != -1)
                        {
                            // has four commas (one too many)
                            editable.delete(i3, length);
                        }

                        // has 3 commas (the right amount)
                        appendEndBracket(editable);
                        length = editable.length();
                        text = editable.toString();

                        setPadding(0, text.substring(1, i0));
                        setPadding(1, text.substring(i0+1, i1));
                        setPadding(2, text.substring(i1+1, i2));
                        setPadding(3, text.substring(i2+1, length-1));

                    } else {
                        // has two commas
                        setPadding(0, text.substring(1, i0));
                        setPadding(1, text.substring(i0+1, i1));
                        appendSeparator(editable);
                    }

                } else {
                    // has one comma
                    setPadding(0, text.substring(1, i0));
                    appendSeparator(editable);
                }
            } else {
                // has no commas
                if (length > 1)
                {
                    setPadding(0, text.substring(1, length));
                    appendSeparator(editable);
                }
            }
            isRunning = false;
        }

        private void insertStartBracket(Editable editable)
        {
            if (editable.charAt(0) != brackets[0])
            {
                editable.insert(0, brackets[0]+"");
            }
        }

        private void appendEndBracket(Editable editable)
        {
            int i;
            if ((i = editable.toString().indexOf(brackets[1])) != -1)
            {
                editable.delete(i, i+1);
            }
            editable.append(brackets[1]);
        }

        private void appendSeparator(Editable editable)
        {
            if (editable.charAt(editable.length() - 1) != separator)
            {
                editable.append(separator);
            }
        }

        @Override
        public void onFocusChange(View view, boolean b)
        {
            afterTextChanged(edit.getText());
        }
    }

    /**
     * ColorChooser
     */
    public static class ColorChooser implements TextWatcher, View.OnFocusChangeListener
    {
        public static final int MAX_LENGTH = 8;

        final private ImageButton button;
        final private EditText edit;

        private int color;
        private boolean isRunning = false, isRemoving = false;

        public static final char[] alphabet = {'#', '0', '1', '2', '3', '4', '5', '6', '7','8', '9', 'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F'};
        HashSet<Character> inputSet;

        public ColorChooser( EditText editField, ImageButton button )
        {
            this.edit = editField;
            this.edit.addTextChangedListener(this);
            this.edit.setOnFocusChangeListener(this);

            /**InputFilter[] filters0 = edit.getFilters();
            InputFilter[] filters1 = new InputFilter[filters0.length + 1];
            System.arraycopy(filters0, 0, filters1, 0, filters0.length);
            filters1[filters1.length] = new InputFilter.AllCaps();
            edit.setFilters(filters1);*/

            inputSet = new HashSet<>();
            for (char c : alphabet)
            {
                inputSet.add(c);
            }

            this.button = button;
        }

        public EditText getField()
        {
            return edit;
        }

        public ImageButton getButton()
        {
            return button;
        }

        public void setColor(int color)
        {
            this.color = color;
            updateViews();
        }

        public void setColor(String hexCode)
        {
            this.color = Color.parseColor(hexCode);
            updateViews();
        }

        public int getColor()
        {
            return color;
        }

        private void updateViews()
        {
            edit.setText( String.format("#%08X", color) );
            Drawable d = button.getDrawable();
            if (d != null)
            {
                GradientDrawable g = (GradientDrawable)d.mutate();
                g.setColor(color);
                g.invalidateSelf();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
        {
            isRemoving = count > after;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {}

        @Override
        public void afterTextChanged(Editable editable)
        {
            if (isRunning || isRemoving)
                return;
            isRunning = true;

            String text = editable.toString();
            int i = text.indexOf('#');
            if (i != -1)
            {
                editable.delete(i, i + 1);
            }
            editable.insert(0, "#");

            text = editable.toString();
            if (text.length() > MAX_LENGTH)
            {
                editable.delete(MAX_LENGTH + 1, text.length());
            }

            text = editable.toString();
            for (int j=text.length()-1; j>=0; j--)
            {
                if (!inputSet.contains(text.charAt(j)))
                {
                    editable.delete(j, j+1);
                }
            }

            text = editable.toString();
            String toCaps = text.toUpperCase(Locale.US);
            editable.clear();
            editable.append(toCaps);

            isRunning = false;
        }

        @Override
        public void onFocusChange(View view, boolean b)
        {
            setColor(edit.getText().toString());
        }
    }

}
