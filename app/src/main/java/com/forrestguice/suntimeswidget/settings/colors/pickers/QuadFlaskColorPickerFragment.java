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

package com.forrestguice.suntimeswidget.settings.colors.pickers;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.AlphaSlider;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.colors.quadflask.LightnessSlider;

/**
 * QuadFlaskColorPickerFragment
 * Flower Mode
 */
public class QuadFlaskColorPickerFragment extends ColorPickerFragment
{
    protected AlphaSlider alphaSlider;
    protected LightnessSlider lightnessSlider;
    protected ColorPickerView colorPicker;

    protected int getLayoutResID() {
        return R.layout.layout_colors_quadflask;
    }

    @Override
    protected void initViews(Context context, View view)
    {
        super.initViews(context, view);
        initViews1(context, view);
        connectQuadFlaskViews();
    }
    protected void initViews1(Context context, View view)
    {
        alphaSlider = (AlphaSlider) view.findViewById(R.id.color_alpha);
        lightnessSlider = (LightnessSlider) view.findViewById(R.id.color_lightness);
        colorPicker = (ColorPickerView) view.findViewById(R.id.color_picker);
    }

    protected void connectQuadFlaskViews()
    {
        if (colorPicker != null) {
            colorPicker.setLightnessSlider(lightnessSlider);
        }
        if (lightnessSlider != null) {
            lightnessSlider.setColorPicker(colorPicker);
        }
        if (alphaSlider != null) {
            alphaSlider.setColorPicker(colorPicker);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(getLayoutResID(), container, false);
        initViews(getContext(), view);
        updateViews(getContext());
        colorPicker.addOnColorChangedListener(onColorChangedListener);
        return view;
    }

    private final OnColorChangedListener onColorChangedListener = new OnColorChangedListener() {
        @Override
        public void onColorChanged(int color) {
            setColor(color, true);
            clearListeners();
            updateViews(getActivity());
            setListeners();
        }
    };

    @Override
    public void updateViews(Context context)
    {
        super.updateViews(context);
        alphaSlider.setVisibility(showAlpha() ? View.VISIBLE : View.GONE);
        lightnessSlider.post(new Runnable() {
            @Override
            public void run() {
                lightnessSlider.setColor(getColor());
                alphaSlider.setColor(getColor());
            }
        });
        colorPicker.setColor(getColor(), false);
    }
}
