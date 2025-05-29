// SPDX-License-Identifier: GPL-3.0-or-later
/*
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

package com.forrestguice.suntimeswidget.colors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.colors.ColorUtils;

public class ColorValuesEditViewHolder extends RecyclerView.ViewHolder
{
    public static int suggestedLayoutResID() {
        return R.layout.layout_listitem_coloredit;
    }

    public TextView text1;

    public ColorValuesEditViewHolder(@NonNull View itemView)
    {
        super(itemView);
        text1 = (TextView) itemView.findViewById(android.R.id.text1);
    }

    public void bindColorToView(Context context, ColorValues values, String key)
    {
        if (key != null)
        {
            boolean bold = true;
            Integer textColor;
            Integer backgroundColor;
            int[] defaultColors = getDefaultColors(context);

            switch (values.getRole(key))
            {
                case ColorValues.ROLE_BACKGROUND_PRIMARY: case ColorValues.ROLE_BACKGROUND:
                case ColorValues.ROLE_BACKGROUND_INVERSE: case ColorValues.ROLE_ACTION:
                bold = false;
                backgroundColor = values.getColor(key);
                textColor = getContrastingColor(values, key, ColorUtils.isTextReadable(defaultColors[0], backgroundColor)
                        ? defaultColors[0] : defaultColors[2]);
                break;

                case ColorValues.ROLE_TEXT: case ColorValues.ROLE_TEXT_PRIMARY:
                case ColorValues.ROLE_TEXT_PRIMARY_INVERSE: case ColorValues.ROLE_TEXT_INVERSE:
                case ColorValues.ROLE_ACCENT: case ColorValues.ROLE_FOREGROUND:
                textColor = values.getColor(key);
                backgroundColor = getContrastingColor(values, key, defaultColors[1]);
                break;

                case ColorValues.ROLE_UNKNOWN:
                default:
                    textColor = values.getColor(key);
                    backgroundColor = null;
                    break;
            }

            SpannableString colorLabel = null;
            String labelText = " " + values.getLabel(key) + " ";

            if (backgroundColor != null && textColor != null)
            {
                //float cornerRadiusPx = context.getResources().getDimension(R.dimen.chip_radius);  // TODO: fix.. looks nice, but it fails to render if the text becomes ellipsized
                //colorLabel = SuntimesUtils.createRoundedBackgroundColorSpan(colorLabel, " " + labelText + " ", labelText, textColor, bold, backgroundColor, cornerRadiusPx, cornerRadiusPx);
                colorLabel = SuntimesUtils.createColorSpan(colorLabel, " " + labelText + " ", labelText, textColor);
                colorLabel = SuntimesUtils.createBackgroundColorSpan(colorLabel, " " + labelText + " ", labelText, backgroundColor);

            } else if (textColor != null) {
                colorLabel = (bold ? SuntimesUtils.createBoldColorSpan(colorLabel, labelText, labelText, textColor)
                        : SuntimesUtils.createColorSpan(colorLabel, labelText, labelText, textColor));

            } else {
                colorLabel = new SpannableString(" ");
            }

            //float textSizePx = getTextSizePx_medium(context);
            //text1.setTextSize(textSizePx);

            text1.setText(colorLabel);
            text1.setVisibility(View.VISIBLE);

        } else {
            text1.setText("");
            text1.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ResourceType")
    public static int[] getDefaultColors(Context context)
    {
        int[] attr = { R.attr.timeCardBackground, R.attr.text_primaryColor, R.attr.text_primaryInverseColor };
        TypedArray typedArray = context.obtainStyledAttributes(attr);
        int backgroundColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.card_bg));
        int textColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_primary));
        int inverseTextColor = ContextCompat.getColor(context, typedArray.getResourceId(2, R.color.text_primary_inverse));
        typedArray.recycle();
        return new int[] { textColor, backgroundColor, inverseTextColor };
    }

    public static float getTextSizePx_medium(Context context)
    {
        int[] attr = { R.attr.text_size_medium };
        TypedArray typedArray = context.obtainStyledAttributes(attr);
        float textSize = typedArray.getDimension(0, context.getResources().getDimension(R.dimen.text_size_medium));
        typedArray.recycle();
        return textSize;
    }

    @Nullable
    public static Integer getContrastingColor(ColorValues colorValues, String key, Integer defaultValue)
    {
        String k;
        switch (colorValues.getRole(key))
        {
            case ColorValues.ROLE_ACTION:
            case ColorValues.ROLE_BACKGROUND: case ColorValues.ROLE_BACKGROUND_PRIMARY:
            k = colorValues.findColorWithRole(ColorValues.ROLE_TEXT_PRIMARY);
            return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            case ColorValues.ROLE_BACKGROUND_INVERSE:
                k = colorValues.findColorWithRole(ColorValues.ROLE_TEXT_PRIMARY_INVERSE);
                return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            case ColorValues.ROLE_TEXT: case ColorValues.ROLE_TEXT_PRIMARY:
            case ColorValues.ROLE_ACCENT: case ColorValues.ROLE_FOREGROUND:
            k = colorValues.findColorWithRole(ColorValues.ROLE_BACKGROUND_PRIMARY);
            return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            case ColorValues.ROLE_TEXT_INVERSE:
            case ColorValues.ROLE_TEXT_PRIMARY_INVERSE:
                k = colorValues.findColorWithRole(ColorValues.ROLE_BACKGROUND_INVERSE);
                return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            default:
                return null;
        }
    }
}
