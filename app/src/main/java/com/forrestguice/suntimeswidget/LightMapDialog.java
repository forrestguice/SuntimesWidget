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
import android.graphics.Paint;
import android.graphics.Typeface;
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

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;

import java.util.ArrayList;
import java.util.Calendar;

public class LightMapDialog extends DialogFragment
{
    private static SuntimesUtils utils = new SuntimesUtils();

    private View sunLayout;
    private TextView sunAzimuth, sunAzimuthRising, sunAzimuthSetting, sunAzimuthAtNoon;
    private TextView sunElevation, sunElevationAtNoon;

    private LightMapView lightmap;
    private LightMapKey field_night, field_astro, field_nautical, field_civil, field_day;
    private int colorNight, colorAstro, colorNautical, colorCivil, colorDay;
    private boolean showSeconds = true;
    private int decimalPlaces = 1;

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

        dialog.setOnShowListener(onShowDialogListener);
        themeViews(dialog.getContext());
        return dialog;
    }

    private DialogInterface.OnShowListener onShowDialogListener = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            startUpdateTask();
        }
    };

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (sunElevation != null)
            sunElevation.post(updateTask);
    }
    private void stopUpdateTask()
    {
        if (sunElevation != null)
            sunElevation.removeCallbacks(updateTask);
    }

    public static final int UPDATE_RATE = 3000;
    private Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null)
            {
                updateLightmapViews(data);
                updateSunPositionViews(data);
            }
            if (sunElevation != null)
                sunElevation.postDelayed(this, UPDATE_RATE);
        }
    };

    public void initViews(View dialogView)
    {
        lightmap = (LightMapView)dialogView.findViewById(R.id.info_time_lightmap);

        sunLayout = dialogView.findViewById(R.id.info_sun_layout);
        sunElevation = (TextView)dialogView.findViewById(R.id.info_sun_elevation_current);
        sunElevationAtNoon = (TextView)dialogView.findViewById(R.id.info_sun_elevation_atnoon);

        sunAzimuth = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_current);
        sunAzimuthRising = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_rising);
        sunAzimuthAtNoon = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_atnoon);
        sunAzimuthSetting = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_setting);

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

        SuntimesUtils.colorizeImageView(field_night.icon, colorNight);
        SuntimesUtils.colorizeImageView(field_astro.icon, colorAstro);
        SuntimesUtils.colorizeImageView(field_nautical.icon, colorNautical);
        SuntimesUtils.colorizeImageView(field_civil.icon, colorCivil);
        SuntimesUtils.colorizeImageView(field_day.icon, colorDay);
    }

    public void updateViews()
    {
        if (data != null)
            updateViews(data);
    }

    protected void updateViews( @NonNull SuntimesRiseSetDataset data )
    {
        stopUpdateTask();
        updateLightmapViews(data);
        updateSunPositionViews(data);
        startUpdateTask();
    }

    protected void updateLightmapViews(@NonNull SuntimesRiseSetDataset data)
    {
        if (lightmap != null)
        {
            Context context = getContext();
            field_civil.updateInfo(context, createInfoArray(data.civilTwilightLength()));
            field_civil.highlight(false);

            field_nautical.updateInfo(context, createInfoArray(data.nauticalTwilightLength()));
            field_nautical.highlight(false);

            field_astro.updateInfo(context, createInfoArray(data.astroTwilightLength()));
            field_astro.highlight(false);

            field_night.updateInfo(context, createInfoArray(new long[] {data.nightLength()}));
            field_night.highlight(false);

            long dayDelta = data.dayLengthOther() - data.dayLength();
            field_day.updateInfo(context, createInfoArray(data.dayLength(), dayDelta, colorDay));
            field_day.highlight(false);

            lightmap.updateViews(data);
            //Log.d("DEBUG", "LightMapDialog updated");
        }
    }

    private SpannableString styleAzimuthText(double azimuth, Integer color, int places)
    {
        SuntimesUtils.TimeDisplayText azimuthText = utils.formatAsDirection2(azimuth, places);
        String azimuthString = utils.formatAsDirection(azimuthText.getValue(), azimuthText.getSuffix());
        SpannableString azimuthSpan = null;
        if (color != null) {
            azimuthSpan = SuntimesUtils.createColorSpan(azimuthSpan, azimuthString, azimuthText.getSuffix(), color);
        }
        azimuthSpan = SuntimesUtils.createRelativeSpan(azimuthSpan, azimuthString, azimuthText.getSuffix(), 0.7f);
        azimuthSpan = SuntimesUtils.createBoldSpan(azimuthSpan, azimuthString, azimuthText.getSuffix());
        return azimuthSpan;
    }

    private CharSequence styleElevationText(double elevation, int places)
    {
        String elevationString = utils.formatAsDegrees(elevation, places);
        SpannableString span = null;
        if (elevation >= -18) {
            span = SuntimesUtils.createColorSpan(span, elevationString, elevationString, getColorForElevation(elevation));
        }
        return (span != null ? span : elevationString);
    }

    private int getColorForElevation(double elevation)
    {
        if (elevation >= 0)
            return colorDay;

        if (elevation >= -6)
            return colorCivil;

        if (elevation >= -12)  //if (elevation >= -18)   // share color
            return colorAstro;

        return colorNight;
    }

    private void highlightLightmapKey(double elevation)
    {
        if (elevation >= 0)
            field_day.highlight(true);

        else if (elevation >= -6)
            field_civil.highlight(true);

        else if (elevation >= -12)
            field_nautical.highlight(true);

        else if (elevation >= -18)
            field_astro.highlight(true);

        else field_day.highlight(true);
    }

    protected void updateSunPositionViews(@NonNull SuntimesRiseSetDataset data)
    {
        SuntimesCalculator calculator = data.calculator();
        if (sunLayout != null)
        {
            SuntimesCalculator.SunPosition currentPosition = (calculator != null ? calculator.getSunPosition(data.now()) : null);
            if (currentPosition != null)
            {
                sunAzimuth.setText(styleAzimuthText(currentPosition.azimuth, null, 2));
                sunElevation.setText(styleElevationText(currentPosition.elevation, 2));
                highlightLightmapKey(currentPosition.elevation);

            } else {
                sunAzimuth.setText("");
                sunElevation.setText("");
            }

            SuntimesRiseSetData riseSetData = data.dataActual;
            Calendar riseTime = (riseSetData != null ? riseSetData.sunriseCalendarToday() : null);
            SuntimesCalculator.SunPosition positionRising = (riseTime != null && calculator != null ? calculator.getSunPosition(riseTime) : null);
            sunAzimuthRising.setText(positionRising != null ? styleAzimuthText(positionRising.azimuth, null, decimalPlaces) : "");

            Calendar setTime = (riseSetData != null ? riseSetData.sunsetCalendarToday() : null);
            SuntimesCalculator.SunPosition positionSetting = (setTime != null && calculator != null ? calculator.getSunPosition(setTime) : null);
            sunAzimuthSetting.setText(positionSetting != null ? styleAzimuthText(positionSetting.azimuth, null, decimalPlaces) : "");

            SuntimesRiseSetData noonData = data.dataNoon;
            Calendar noonTime = (noonData != null ? noonData.sunriseCalendarToday() : null);
            SuntimesCalculator.SunPosition positionAtNoon = (noonTime != null && calculator != null ? calculator.getSunPosition(noonTime) : null);
            if (positionAtNoon != null)
            {
                sunElevationAtNoon.setText(styleElevationText(positionAtNoon.elevation, decimalPlaces));
                sunAzimuthAtNoon.setText(styleAzimuthText(positionAtNoon.azimuth, null, decimalPlaces));

            } else {
                sunElevationAtNoon.setText("");
                sunAzimuthAtNoon.setText("");
            }

            showSunPosition(currentPosition != null);
        }
    }

    private void showSunPosition(boolean show)
    {
        if (sunLayout != null)
        {
            sunLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * LightMapKey
     */
    private class LightMapKey
    {
        protected ImageView icon;
        protected TextView label;
        protected TextView text;

        public LightMapKey(ImageView icon, TextView label, TextView duration)
        {
            this.icon = icon;
            this.label = label;
            this.text = duration;
        }

        public LightMapKey(@NonNull View parent, int iconRes, int labelRes, int durationRes)
        {
            icon = (ImageView)parent.findViewById(iconRes);
            label = (TextView)parent.findViewById(labelRes);
            text = (TextView)parent.findViewById(durationRes);
        }

        public void setVisible(boolean visible)
        {
            int visibility = (visible ? View.VISIBLE : View.GONE);
            if (label != null) {
                label.setVisibility(visibility);
            }
            if (text != null) {
                text.setVisibility(visibility);
            }
            if (icon != null) {
                icon.setVisibility(visibility);
            }
        }

        public void highlight(boolean highlight)
        {
            if (label != null)
            {
                label.setTypeface(null, (highlight ? Typeface.BOLD : Typeface.NORMAL));
                if (highlight)
                    label.setPaintFlags(label.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                else label.setPaintFlags(label.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            }

            //if (text != null)
                //text.setTypeface(null, (highlight ? Typeface.BOLD : Typeface.NORMAL));
        }

        public void updateInfo(Context context, LightMapKeyInfo[] info)
        {
            if (text == null || info == null || context == null)
                return;

            if (info.length == 1)
            {
                String duration = info[0].durationString(showSeconds);
                if (info[0].delta > 0) {
                    String s = context.getString(R.string.length_twilight1e_pos, duration, info[0].deltaString(showSeconds));
                    if (info[0].durationColor != null)
                        text.setText(SuntimesUtils.createColorSpan(null, s, duration, info[0].durationColor));
                    else text.setText(new SpannableString(s));

                } else if (info[0].delta < 0) {
                    String s = context.getString(R.string.length_twilight1e_neg, duration, info[0].deltaString(showSeconds));
                    if (info[0].durationColor != null)
                        text.setText(SuntimesUtils.createColorSpan(null, s, duration, info[0].durationColor));
                    else text.setText(new SpannableString(s));

                } else {
                    String s = context.getString(R.string.length_twilight1, duration);
                    if (info[0].durationColor != null)
                        text.setText(SuntimesUtils.createColorSpan(null, s, duration, info[0].durationColor));
                    else text.setText(new SpannableString(s));
                }
                setVisible(true);

            } else if (info.length >= 2) {
                String s = context.getString(R.string.length_twilight2, info[0].durationString(showSeconds), info[1].durationString(showSeconds));
                String delimiter = context.getString(R.string.length_delimiter);
                text.setText(SuntimesUtils.createBoldColorSpan(null, s, delimiter, colorDay));
                setVisible(true);

            } else {
                text.setText(new SpannableString(""));
                setVisible(false);
            }
        }
    }

    /**
     * LightMapKeyInfo
     */
    public static class LightMapKeyInfo
    {
        public LightMapKeyInfo(long duration, long delta)
        {
            this.duration = duration;
            this.delta = delta;
        }

        public long duration = 0;
        public Integer durationColor = null;
        public String durationString(boolean showSeconds)
        {
            return utils.timeDeltaLongDisplayString(duration, showSeconds).toString();
        }

        public long delta = 0;
        public Integer deltaColor = null;
        public String deltaString(boolean showSeconds)
        {
            return utils.timeDeltaLongDisplayString(delta, showSeconds).toString();
        }
    }

    public static LightMapKeyInfo[] createInfoArray(long durations, long delta, int color)
    {
        if (durations != 0)
        {
            LightMapKeyInfo[] info = new LightMapKeyInfo[1];
            info[0] = new LightMapKeyInfo(durations, delta);
            info[0].durationColor = color;
            return info;

        } else {
            return new LightMapKeyInfo[0];
        }
    }

    public static LightMapKeyInfo[] createInfoArray(long[] durations)
    {
        ArrayList<LightMapKeyInfo> info = new ArrayList<>();
        for (int i=0; i<durations.length; i++)
        {
            if (durations[i] != 0)
            {
                info.add(new LightMapKeyInfo(durations[i], 0));
            }
        }
        return info.toArray(new LightMapKeyInfo[0]);
    }

}
