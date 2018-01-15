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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class LightMapDialog extends DialogFragment
{
    private static SuntimesUtils utils = new SuntimesUtils();

    private LightMapView lightmap;
    private ImageView ic_night, ic_astro, ic_nautical, ic_civil, ic_day;
    private TextView txt_night, txt_astro, txt_nautical, txt_civil, txt_day;

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

        ic_night = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_night_icon);
        txt_night = (TextView)dialogView.findViewById(R.id.info_time_lightmap_key_night_duration);

        ic_astro = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_astro_icon);
        txt_astro = (TextView)dialogView.findViewById(R.id.info_time_lightmap_key_astro_duration);

        ic_nautical = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_nautical_icon);
        txt_nautical = (TextView)dialogView.findViewById(R.id.info_time_lightmap_key_nautical_duration);

        ic_civil = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_civil_icon);
        txt_civil = (TextView)dialogView.findViewById(R.id.info_time_lightmap_key_civil_duration);

        ic_day = (ImageView)dialogView.findViewById(R.id.info_time_lightmap_key_day_icon);
        txt_day = (TextView)dialogView.findViewById(R.id.info_time_lightmap_key_day_duration);
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
            Context context = getContext();
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);

            String displayDay = "";
            long lengthDay = data.dayLength();
            if (lengthDay > 0)
            {
                SuntimesUtils.TimeDisplayText txtDay = utils.timeDeltaLongDisplayString(lengthDay, showSeconds);
                displayDay = context.getString(R.string.length_twilight1, txtDay.toString());
            }
            txt_day.setText(displayDay);

            String displayCivil = "";
            long lengthCivil0 = data.civilMorningLength();
            long lengthCivil1 = data.civilEveningLength();
            if (lengthCivil0 > 0 && lengthCivil1 > 0)
            {
                SuntimesUtils.TimeDisplayText txtCivil0 = utils.timeDeltaLongDisplayString(lengthCivil0, showSeconds);
                SuntimesUtils.TimeDisplayText txtCivil1 = utils.timeDeltaLongDisplayString(lengthCivil1, showSeconds);
                displayCivil = context.getString(R.string.length_twilight2, txtCivil0.toString(), txtCivil1.toString());
            }
            txt_civil.setText(displayCivil);

            String displayNautical = "";
            long lengthNautical0 = data.nauticalMorningLength();
            long lengthNautical1 = data.nauticalEveningLength();
            if (lengthNautical0 > 0 && lengthNautical1 > 0)
            {
                SuntimesUtils.TimeDisplayText txtNautical0 = utils.timeDeltaLongDisplayString(lengthNautical0, showSeconds);
                SuntimesUtils.TimeDisplayText txtNautical1 = utils.timeDeltaLongDisplayString(lengthNautical1, showSeconds);
                displayNautical = context.getString(R.string.length_twilight2, txtNautical0.toString(), txtNautical1.toString());
            }
            txt_nautical.setText(displayNautical);

            String displayAstro = "";
            long lengthAstro0 = data.astroMorningLength();
            long lengthAstro1 = data.astroEveningLength();
            if (lengthAstro0 > 0 && lengthAstro1 > 0)
            {
                SuntimesUtils.TimeDisplayText txtAstro0 = utils.timeDeltaLongDisplayString(lengthAstro0, showSeconds);
                SuntimesUtils.TimeDisplayText txtAstro1 = utils.timeDeltaLongDisplayString(lengthAstro1, showSeconds);
                displayAstro = context.getString(R.string.length_twilight2, txtAstro0.toString(), txtAstro1.toString());
            }
            txt_astro.setText(displayAstro);

            String displayNight = "";
            long lengthNight = data.nightLength();
            if (lengthNight > 0)
            {
                SuntimesUtils.TimeDisplayText txtNight = utils.timeDeltaLongDisplayString(lengthNight, showSeconds);
                displayNight = context.getString(R.string.length_twilight1, txtNight.toString());
            }
            txt_night.setText(displayNight);

            lightmap.updateViews(data);
            Log.d("DEBUG", "LightMapDialog updated");
        }
    }

}
