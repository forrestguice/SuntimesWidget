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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.DarkTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public class SuntimesWidgetThemeActivity extends AppCompatActivity
{
    public static final int PICK_THEME_REQUEST = 1;

    public SuntimesWidgetThemeActivity()
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
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            // TODO
        }

        WidgetThemes.initThemes(this);
        initViews(this);
    }

    private void initLocale()
    {
        AppSettings.initLocale(this);
        //WidgetSettings.initDefaults(this);
        //WidgetSettings.initDisplayStrings(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    protected void initViews( Context context )
    {
        // grid of themes
        final GridView gridView = (GridView)findViewById(R.id.themegrid);
        final ThemeGridAdapter adapter = new ThemeGridAdapter(this, WidgetThemes.values());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                SuntimesTheme.ThemeDescriptor theme = (SuntimesTheme.ThemeDescriptor) adapter.getItem(position);
                actionOk(theme.name());
            }
        });

        // select theme button
        Button selectButton = (Button)findViewById(R.id.select_button);
        if (selectButton != null)
        {
            selectButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SuntimesTheme.ThemeDescriptor selected = (SuntimesTheme.ThemeDescriptor)gridView.getSelectedItem();
                    actionOk(selected.name());
                }
            });
        }
    }

    private void actionOk( String themeName )
    {
        Intent intent = new Intent();
        intent.putExtra(SuntimesTheme.THEME_NAME, themeName);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void actionCancel()
    {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    /**
     *
     */
    public class ThemeGridAdapter extends BaseAdapter
    {
        private final Context context;
        private final SuntimesTheme.ThemeDescriptor[] themes;
        private String addText = "+";

        public ThemeGridAdapter(Context context, SuntimesTheme.ThemeDescriptor[] themes)
        {
            this.context = context;
            this.themes = themes;
            addText = context.getString(R.string.configLabel_widgetThemeList_add);
        }

        public int getCount()
        {
            return themes.length+1;
        }

        public Object getItem(int position)
        {
            if (position > 0)
                return themes[position-1];
            return null;
        }

        public long getItemId(int position)
        {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;
            if (convertView != null)
            {
                return convertView;

            } else {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(R.layout.layout_griditem_theme, parent, false);
                LinearLayout layout = (LinearLayout) view.findViewById(R.id.griditem);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                if (position > 0)
                {
                    SuntimesTheme theme = WidgetThemes.loadTheme(context, themes[position - 1].name());
                    textView.setText(theme.themeDisplayString());
                    textView.setTextColor(theme.getTitleColor());
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTitleSizeSp());
                    layout.setBackgroundResource(theme.getBackgroundId());

                } else {
                    layout.setBackgroundColor(Color.BLUE);  // TODO: themed
                    textView.setText(addText);
                }
                return view;
            }
        }
    }

}
