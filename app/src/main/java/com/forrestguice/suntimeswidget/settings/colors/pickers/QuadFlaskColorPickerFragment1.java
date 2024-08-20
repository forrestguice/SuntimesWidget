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
import android.view.View;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.slider.AlphaSlider;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.colors.quadflask.LightnessSlider;

/**
 * QuadFlaskColorPickerFragment1
 * Circle Mode
 */
public class QuadFlaskColorPickerFragment1 extends QuadFlaskColorPickerFragment
{
    @Override
    protected int getLayoutResID() {
        return R.layout.layout_colors_quadflask1;
    }

    @Override
    protected void initViews1(Context context, View view)
    {
        alphaSlider = (AlphaSlider) view.findViewById(R.id.color_alpha1);
        lightnessSlider = (LightnessSlider) view.findViewById(R.id.color_lightness1);
        colorPicker = (ColorPickerView) view.findViewById(R.id.color_picker1);
    }
}
