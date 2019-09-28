/**
    Copyright (C) 2017-2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.AlphaSlider;
import com.forrestguice.suntimeswidget.R;

public class ColorDialog extends BottomSheetDialogFragment
{
    public ColorDialog() {}

    private ColorPickerView colorPicker;
    private AlphaSlider alphaSlider;

    private int color = Color.WHITE;
    public int getColor()
    {
        return color;
    }
    public void setColor( int color )
    {
        this.color = color;
    }

    private boolean showAlpha = false;
    public void setShowAlpha(boolean value)
    {
        this.showAlpha = value;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_colors, parent, false);

        if (savedState != null)
        {
            setColor(savedState.getInt("color", getColor()));
            showAlpha = savedState.getBoolean("showAlpha", showAlpha);
        }
        initViews(getActivity(), dialogContent);

        return dialogContent;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putInt("color", getColor());
        outState.putBoolean("showAlpha", showAlpha);
    }

    private void initViews(Context context, View dialogContent)
    {
        colorPicker = (ColorPickerView)dialogContent.findViewById(R.id.color_picker);
        colorPicker.setColor(color, false);
        colorPicker.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                setColor(color);
            }
        });

        alphaSlider = (AlphaSlider)dialogContent.findViewById(R.id.color_alpha);
        alphaSlider.setVisibility(showAlpha ? View.VISIBLE : View.GONE);

        Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        btn_cancel.setOnClickListener(onDialogCancelClick);

        Button btn_accept = (Button) dialogContent.findViewById(R.id.dialog_button_accept);
        btn_accept.setOnClickListener(onDialogAcceptClick);
    }

    private View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    };

    private View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            dismiss();
            if (colorChangeListener != null) {
                colorChangeListener.onColorChanged(getColor());
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    /**
     * ColorChangeListener
     */
    public static abstract class ColorChangeListener
    {
        public void onColorChanged(int color) {}
    }
    public ColorChangeListener colorChangeListener = null;
    public void setColorChangeListener( ColorChangeListener listener )
    {
        this.colorChangeListener = listener;
    }

}
