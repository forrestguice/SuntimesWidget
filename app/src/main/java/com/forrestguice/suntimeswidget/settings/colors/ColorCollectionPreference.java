/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.colors;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValuesSheetActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class ColorCollectionPreference extends Preference
{
    public ColorCollectionPreference(Context context) {
        super(context);
    }

    public ColorCollectionPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setParams(context, attrs);
    }

    @TargetApi(21)
    public ColorCollectionPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setParams(context, attrs);
    }

    @TargetApi(21)
    public ColorCollectionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        setParams(context, attrs);
    }

    public void setParams(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorCollectionPreference, 0, 0);
        try {
            colorTag = a.getString(R.styleable.ColorCollectionPreference_colorTag);
            appWidgetID = a.getInt(R.styleable.ColorCollectionPreference_appWidgetID, appWidgetID);
            showAlpha = a.getBoolean(R.styleable.ColorCollectionPreference_showAlpha, showAlpha);
            summaryStringResID = a.getResourceId(R.styleable.ColorCollectionPreference_android_summary, 0);

            int previewArrayID = a.getResourceId(R.styleable.ColorCollectionPreference_previewKeys, 0);
            if (previewArrayID != 0) {
                setPreviewKeys(context.getResources().getStringArray(previewArrayID));
            }

        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        if (collection != null && !previewKeys.isEmpty())
        {
            int[] previewViewIDs = new int[] { R.id.colorPreview0, R.id.colorPreview1, R.id.colorPreview2 };

            ColorValues values = collection.getSelectedColors(getContext(), getAppWidgetID(), getColorTag());
            if (values == null) {
                values = collection.getDefaultColors(getContext());
            }

            for (int i=0; i<previewViewIDs.length; i++)
            {
                View colorPreview = view.findViewById(previewViewIDs[i]);
                if (colorPreview != null)
                {
                    if (i < previewKeys.size()) {
                        colorPreview.setBackgroundColor(values.getColor(previewKeys.get(i)));
                        colorPreview.setVisibility(View.VISIBLE);
                    } else {
                        colorPreview.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    protected ArrayList<String> previewKeys = new ArrayList<>();
    public void setPreviewKeys(String... keys)
    {
        previewKeys.clear();
        previewKeys.addAll(Arrays.asList(keys));
        Log.d("DEBUG", "setPreviewKeys: " + keys[0]);
    }

    protected String colorTag = null;
    public String getColorTag() {
        return colorTag;
    }
    public void setColorTag(String value) {
        colorTag = value;
    }

    protected int appWidgetID = 0;
    public int getAppWidgetID() {
        return appWidgetID;
    }
    public void setAppWidgetID(int value) {
        appWidgetID = value;
    }

    protected ColorValuesCollection<ColorValues> collection = null;
    public ColorValuesCollection<ColorValues> getCollection() {
        return collection;
    }
    public void setCollection(Context context, ColorValuesCollection<ColorValues> value)
    {
        collection = value;
        updateSummary(context);
    }

    protected void updateSummary(Context context)
    {
        String selectedColorsID = getSelectedColorsID(context);
        if (summaryStringResID != 0) {
            setSummary(context.getString(summaryStringResID, (selectedColorsID != null ? selectedColorsID : context.getString(R.string.configLabel_tagDefault))));
        } else {
            setSummary((selectedColorsID != null ? selectedColorsID : context.getString(R.string.configLabel_tagDefault)));
        }
    }
    protected int summaryStringResID = 0;

    protected boolean showAlpha = false;
    public boolean showAlpha() {
        return showAlpha;
    }
    public void setShowAlpha(boolean value) {
        showAlpha = value;
    }

    protected int requestCode = 0;
    public int getRequestCode() {
        return requestCode;
    }
    public void setRequestCode(int value) {
        requestCode = value;
    }

    protected String getSelectedColorsID(Context context) {
        return (collection != null ? collection.getSelectedColorsID(context, getAppWidgetID(), getColorTag()) : null);
    }

    public void initPreferenceOnClickListener(final Activity activity, int requestCode)
    {
        setRequestCode(requestCode);
        setOnPreferenceClickListener(createPreferenceOnClickListener(activity));
    }

    protected Preference.OnPreferenceClickListener createPreferenceOnClickListener(final Activity activity)
    {
        return new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                if (activity != null)
                {
                    Intent intent = new Intent(activity, ColorValuesSheetActivity.class);
                    intent.putExtra(ColorValuesSheetActivity.EXTRA_APPWIDGET_ID, getAppWidgetID());
                    intent.putExtra(ColorValuesSheetActivity.EXTRA_COLORTAG, getColorTag());
                    intent.putExtra(ColorValuesSheetActivity.EXTRA_COLLECTION, getCollection());
                    intent.putExtra(ColorValuesSheetActivity.EXTRA_TITLE, getTitle());
                    intent.putExtra(ColorValuesSheetActivity.EXTRA_SHOW_ALPHA, showAlpha());
                    activity.startActivityForResult(intent, getRequestCode());
                    activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                }
                return false;
            }
        };
    }

}
