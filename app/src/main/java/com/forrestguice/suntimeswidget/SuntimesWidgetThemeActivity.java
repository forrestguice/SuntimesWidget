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
import android.support.v7.view.ActionMode;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public class SuntimesWidgetThemeActivity extends AppCompatActivity
{
    public static final int PICK_THEME_REQUEST = 1;
    public static final String ADAPTER_MODIFIED = "isModified";

    private boolean adapterModified = false;
    private GridView gridView;

    protected ActionMode actionMode = null;
    private WidgetThemeActionCompat themeActions;

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

        /**Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
        }*/

        WidgetThemes.initThemes(this);
        initViews(this);
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
        gridView = (GridView)findViewById(R.id.themegrid);
        initThemeAdapter(context);

        themeActions = new WidgetThemeActionCompat(context);
    }

    protected void initThemeAdapter(Context contxt)
    {
        final ThemeGridAdapter adapter = new ThemeGridAdapter(this, WidgetThemes.values());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                if (position == 0)
                {
                    addTheme();

                } else {
                    SuntimesTheme.ThemeDescriptor theme = (SuntimesTheme.ThemeDescriptor) adapter.getItem(position);
                    triggerActionMode(v, theme);
                }
            }
        });
    }

    private boolean triggerActionMode(View view, SuntimesTheme.ThemeDescriptor themeDesc)
    {
        if (actionMode == null)
        {
            themeActions.setTheme(this, themeDesc);
            actionMode = startSupportActionMode(themeActions);

            SuntimesTheme theme = WidgetThemes.loadTheme(this, themeDesc.name());
            actionMode.setTitle(theme.themeDisplayString());
            return true;

        } else {
            actionMode.finish();
            triggerActionMode(view, themeDesc);
            return false;
        }
    }

    protected void addTheme()
    {
        if (actionMode != null)
        {
            actionMode.finish();
        }

        // TODO: add theme ui
    }

    protected void editTheme( SuntimesTheme theme )
    {
        if (theme.isDefault())
        {
            // TODO: msg - cant edit default, copy as new

        } else {
            // TODO: edit theme ui
        }
    }

    protected  void deleteTheme(SuntimesTheme theme )
    {
        if (!theme.isDefault())
        {
            // TODO: delete confirm
            // TODO: delete theme
        }
    }

    protected void selectTheme( SuntimesTheme theme )
    {
        Intent intent = new Intent();
        intent.putExtra(SuntimesTheme.THEME_NAME, theme.themeName());
        intent.putExtra(ADAPTER_MODIFIED, adapterModified);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra(ADAPTER_MODIFIED, adapterModified);
        setResult(((adapterModified) ? Activity.RESULT_OK : Activity.RESULT_CANCELED), intent);
        finish();
    }

    /**
     * ThemeGridAdapter
     */
    public static class ThemeGridAdapter extends BaseAdapter
    {
        private final Context context;
        private final SuntimesTheme.ThemeDescriptor[] themes;

        public ThemeGridAdapter(Context context, SuntimesTheme.ThemeDescriptor[] themes)
        {
            this.context = context;
            this.themes = themes;
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
                if (position > 0)
                {
                    view = layoutInflater.inflate(R.layout.layout_griditem_theme, parent, false);
                    View layout = view.findViewById(R.id.griditem);
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);

                    SuntimesTheme theme = WidgetThemes.loadTheme(context, themes[position - 1].name());
                    textView.setText(theme.themeDisplayString());
                    textView.setTextColor(theme.getTitleColor());
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTitleSizeSp());
                    layout.setBackgroundResource(theme.getBackgroundId());

                } else {
                    view = layoutInflater.inflate(R.layout.layout_griditem_addtheme, parent, false);
                }
                return view;
            }
        }
    }

    /**
     * WidgetThemeActionCompat
     */
    private class WidgetThemeActionCompat implements android.support.v7.view.ActionMode.Callback
    {
        private SuntimesTheme theme = null;

        public WidgetThemeActionCompat(Context context) {}

        public void setTheme(Context context, SuntimesTheme.ThemeDescriptor themeDesc )
        {
            this.theme = WidgetThemes.loadTheme(context, themeDesc.name());
        }

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.themeconfig, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            MenuItem deleteItem = menu.findItem(R.id.deleteTheme);
            deleteItem.setVisible( !theme.isDefault() );
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item)
        {
            mode.finish();
            if (theme != null)
            {
                switch (item.getItemId())
                {
                    case R.id.selectTheme:
                        selectTheme(theme);
                        return true;

                    case R.id.editTheme:
                        editTheme(theme);
                        return true;

                    case R.id.deleteTheme:
                        deleteTheme(theme);
                        return true;
                }
            }
            return false;
        }
    }

}
