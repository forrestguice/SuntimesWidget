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
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.colors.ColorChangeListener;
import com.forrestguice.support.app.DialogBase;
import com.forrestguice.colors.ColorUtils;
import com.forrestguice.support.lifecycle.MutableLiveData;
import com.forrestguice.support.lifecycle.Observer;
import com.forrestguice.support.lifecycle.ViewModel;
import com.forrestguice.support.lifecycle.ViewModelProviders;

import java.util.Locale;

/**
 * ColorPickerFragment
 */
public class ColorPickerFragment extends DialogBase
{
    protected ColorPickerModel colorViewModel;

    protected View preview;             // color as solid
    protected TextView preview_text;    // color as text over card color
    protected TextView preview_text1;   // color as background under default text color

    public ColorPickerFragment() {
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        colorViewModel = ViewModelProviders.of(getActivity()).get(ColorPickerModel.class);
        colorViewModel.color.observe(getActivity(), onViewModelColorChanged);
        return null;
    }
    
    protected void initViews(Context context, View view)
    {
        preview = view.findViewById(R.id.preview_color);
        preview_text = (TextView) view.findViewById(R.id.preview_color_text);
        preview_text1 = (TextView) view.findViewById(R.id.preview_color_text1);
    }

    protected ColorChangeListener listener;
    public void setColorChangeListener(ColorChangeListener listener) {
        this.listener = listener;
    }

    public void setColor( String hexValue ) {
        setColor(Color.parseColor(hexValue.trim()), true);
    }

    public void setColor( int color, boolean userTriggered )
    {
        if (colorViewModel != null) {
            colorViewModel.setColor(color);
        }
        if (listener != null && userTriggered) {
            listener.onColorChanged(color);
        }
    }

    public int getColor() {
        return (colorViewModel != null ? colorViewModel.color.getValue() : Color.WHITE);
    }

    public boolean showAlpha() {
        return (colorViewModel != null && colorViewModel.showAlpha());
    }

    private final Observer<Integer> onViewModelColorChanged = new Observer<Integer>()
    {
        @Override
        public void onChanged(@Nullable Integer color)
        {
            if (isAdded() && getView() != null) {
                clearListeners();
                updateViews(getActivity());
                setListeners();
            }
        }
    };

    @CallSuper
    public void updateViews(Context context) {
        updatePreview(context);
    }

    protected void updatePreview(Context context)
    {
        if (preview != null) {
            preview.setBackgroundColor(getColor());
        }

        if (preview_text != null && colorViewModel.hasColorUnder()) {
            preview_text.setTextColor(getColor());
            preview_text.setBackgroundColor(colorViewModel.getColorUnder());
            preview_text.setText(getPreviewText(context, getColor(), colorViewModel.getColorUnder()));

        } else if (preview_text != null) {
            preview_text.setBackgroundColor(getColor());
            preview_text.setTextColor(getColor());
            preview_text.setText(getPreviewText(context, getColor(), colorViewModel.getColorUnder()));
        }

        if (preview_text1 != null && colorViewModel.hasColorOver()) {
            preview_text1.setTextColor(colorViewModel.getColorOver());
            preview_text1.setBackgroundColor(getColor());
            preview_text1.setText(getPreviewText(context, colorViewModel.getColorOver(), getColor()));

        } else if (preview_text1 != null) {
            preview_text1.setBackgroundColor(getColor());
            preview_text1.setTextColor(getColor());
            preview_text1.setText(getPreviewText(context, colorViewModel.getColorOver(), getColor()));
        }
    }

    @Nullable
    protected String getPreviewText(Context context, int textColor, int backgroundColor)
    {
        switch (colorViewModel.getPreviewMode())
        {
            case ColorPickerModel.PREVIEW_CONTRAST_RATIO:
                return String.format(Locale.getDefault(), "%.2f", ColorUtils.getContrastRatio(textColor, backgroundColor));

            case ColorPickerModel.PREVIEW_LUMINANCE:
                return String.format(Locale.getDefault(), "%.2f", 100 * ColorUtils.getLuminance(getColor())) + "%";

            case ColorPickerModel.PREVIEW_TEXT:
            default:
                String text = colorViewModel.getPreviewText();
                return (text != null) ? text : context.getString(R.string.configLabel_themeColorText);
        }
    }

    protected void setListeners() {}
    protected void clearListeners() {}

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null) {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    clearListeners();
                    updateViews(getActivity());
                    setListeners();
                }
            });
        }
    }

    @Override
    public void onViewStateRestored(Bundle bundle)
    {
        super.onViewStateRestored(bundle);
        clearListeners();
        updateViews(getActivity());
        setListeners();
    }

    /**
     * ColorPickerModel
     */
    public static class ColorPickerModel extends ViewModel
    {
        public MutableLiveData<Integer> color = new MutableLiveData<>();
        protected Integer color_over = null;
        protected Integer color_under = null;
        protected boolean showAlpha = false;

        public ColorPickerModel() {
            color.setValue(Color.WHITE);
        }

        public Integer getColorOver() {
            return color_over;
        }
        public void setColorOver(Integer value) {
            color_over = value;
        }
        public boolean hasColorOver() {
            return color_over != null;
        }

        public Integer getColorUnder() {
            return color_under;
        }
        public void setColorUnder(Integer value) {
            color_under = value;
        }
        public boolean hasColorUnder() {
            return color_under != null;
        }

        public void setColor(int value) {
            color.setValue(value);
        }

        public void setShowAlpha(boolean value) {
            showAlpha = value;
        }
        public boolean showAlpha() {
            return showAlpha;
        }

        public static final int PREVIEW_TEXT = 0;
        public static final int PREVIEW_LUMINANCE = 10;
        public static final int PREVIEW_CONTRAST_RATIO = 20;

        protected int previewMode = PREVIEW_CONTRAST_RATIO;
        public int getPreviewMode() {
            return previewMode;
        }
        public void setPreviewMode(int mode) {
            previewMode = mode;
        }

        protected String previewText = null;
        public String getPreviewText() {
            return previewText;
        }
        public void setPreviewText(String value) {
            previewText = value;
        }
    }

}
