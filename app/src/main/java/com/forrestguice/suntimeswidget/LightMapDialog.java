/**
    Copyright (C) 2017-2018 Forrest Guice
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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;

public class LightMapDialog extends DialogFragment
{
    private static SuntimesUtils utils = new SuntimesUtils();

    private LightMapView lightmap;
    private LightMapKey field_night, field_astro, field_nautical, field_civil, field_day;
    private int colorNight, colorAstro, colorNautical, colorCivil, colorDay;
    private boolean showSeconds = false;

    private SuntimesRiseSetDataset data;
    public void setData( SuntimesRiseSetDataset data )
    {
        this.data = data;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();

        View dialogContent = inflater.inflate(R.layout.layout_dialog_lightmap, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        initViews(dialogContent);
        if (savedInstanceState != null)
        {
            Log.d("DEBUG", "LightMapDialog onCreate (restoreState)");
        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface)
            {
                updateViews(data);
            }
        });

        themeViews(dialog.getContext());
        return dialog;
    }

    public void initViews(View dialogView)
    {
        lightmap = (LightMapView)dialogView.findViewById(R.id.info_time_lightmap);
        field_night = new LightMapKey(dialogView, R.id.info_time_lightmap_key_night_icon, R.id.info_time_lightmap_key_night_label, R.id.info_time_lightmap_key_night_duration);
        field_astro = new LightMapKey(dialogView, R.id.info_time_lightmap_key_astro_icon, R.id.info_time_lightmap_key_astro_label, R.id.info_time_lightmap_key_astro_duration);
        field_nautical = new LightMapKey(dialogView, R.id.info_time_lightmap_key_nautical_icon, R.id.info_time_lightmap_key_nautical_label, R.id.info_time_lightmap_key_nautical_duration);
        field_civil = new LightMapKey(dialogView, R.id.info_time_lightmap_key_civil_icon, R.id.info_time_lightmap_key_civil_label, R.id.info_time_lightmap_key_civil_duration);
        field_day = new LightMapKey(dialogView, R.id.info_time_lightmap_key_day_icon, R.id.info_time_lightmap_key_day_label, R.id.info_time_lightmap_key_day_duration);
    }

    @SuppressWarnings("ResourceType")
    public void themeViews(Context context)
    {
        int[] colorAttrs = { R.attr.graphColor_night,   // 0
                R.attr.graphColor_astronomical,         // 1
                R.attr.graphColor_nautical,             // 2
                R.attr.graphColor_civil,                // 3
                R.attr.graphColor_day                   // 4
        };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        colorNight = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        colorAstro = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        colorNautical = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        colorCivil = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        colorDay = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        typedArray.recycle();

        colorizeImageView(field_night.icon, colorNight);
        colorizeImageView(field_astro.icon, colorAstro);
        colorizeImageView(field_nautical.icon, colorNautical);
        colorizeImageView(field_civil.icon, colorCivil);
        colorizeImageView(field_day.icon, colorDay);
    }

    private void colorizeImageView(ImageView view, int color)
    {
        if (view != null && view.getBackground() != null)
        {
            GradientDrawable d = (GradientDrawable) view.getBackground().mutate();
            d.setColor(color);
            d.invalidateSelf();
        }
    }

    public void updateViews( SuntimesRiseSetDataset data )
    {
        if (lightmap != null)
        {
            Context context = getContext();
            showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            field_day.updateInfo(context, new long[] {data.dayLength()});
            field_civil.updateInfo(context, data.civilTwilightLength());
            field_nautical.updateInfo(context, data.nauticalTwilightLength());
            field_astro.updateInfo(context, data.astroTwilightLength());
            field_night.updateInfo(context, new long[] {data.nightLength()});
            lightmap.updateViews(data);
            Log.d("DEBUG", "LightMapDialog updated");
        }
    }

    /**
     * LightMapKey
     */
    private class LightMapKey
    {
        protected ImageView icon;
        protected TextView label;
        protected TextView info;

        public LightMapKey(ImageView icon, TextView label, TextView duration)
        {
            this.icon = icon;
            this.label = label;
            this.info = duration;
        }

        public LightMapKey(@NonNull View parent, int iconRes, int labelRes, int durationRes)
        {
            icon = (ImageView)parent.findViewById(iconRes);
            label = (TextView)parent.findViewById(labelRes);
            info = (TextView)parent.findViewById(durationRes);
        }

        public void setVisible(boolean visible)
        {
            int visibility = (visible ? View.VISIBLE : View.GONE);
            if (label != null) {
                label.setVisibility(visibility);
            }
            if (info != null) {
                info.setVisibility(visibility);
            }
            if (icon != null) {
                icon.setVisibility(visibility);
            }
        }

        public void updateInfo(Context context, long[] durations)
        {
            if (info == null)
                return;

            ArrayList<SuntimesUtils.TimeDisplayText> txt = new ArrayList<>();
            for (int i=0; i<durations.length; i++)
            {
                if (durations[i] > 0)
                    txt.add(utils.timeDeltaLongDisplayString(durations[i], showSeconds));
            }

            if (txt.size() == 1)
            {
                info.setText(new SpannableString(context.getString(R.string.length_twilight1, txt.get(0).toString())));
                setVisible(true);

            } else if (txt.size() >= 2) {
                String s = context.getString(R.string.length_twilight2, txt.get(0).toString(), txt.get(1).toString());
                info.setText(SuntimesUtils.createBoldColorSpan(s, "|", colorDay));
                setVisible(true);

            } else {
                info.setText(new SpannableString(""));
                setVisible(false);
            }
        }
    }

}
