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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;

public class LightMapDialog extends DialogFragment
{
    private LightMapView lightmap;
    private ImageView ic_night, ic_astro, ic_nautical, ic_civil, ic_day;

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
                lightmap.updateViews(data);
            }
        });

        themeViews(dialog.getContext());
        return dialog;
    }

    public void initViews(View dialogView)
    {
        lightmap = (LightMapView)dialogView.findViewById(R.id.info_time_lightmap);
        ic_night = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_night_icon);
        ic_astro = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_astro_icon);
        ic_nautical = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_nautical_icon);
        ic_civil = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_civil_icon);
        ic_day = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_day_icon);
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
        int def = R.color.color_transparent;
        int colorNight = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        int colorAstro = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        int colorNautical = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        int colorCivil = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        int colorDay = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        typedArray.recycle();

        colorizeImageView(ic_night, colorNight);
        colorizeImageView(ic_astro, colorAstro);
        colorizeImageView(ic_nautical, colorNautical);
        colorizeImageView(ic_civil, colorCivil);
        colorizeImageView(ic_day, colorDay);
    }

    private void colorizeImageView(ImageView view, int color)
    {
        if (view.getBackground() != null)
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
            lightmap.updateViews(data);
            Log.d("DEBUG", "LightMapDialog updated");
        }
    }
}
