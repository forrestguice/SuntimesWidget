/**
    Copyright (C) 2020 Forrest Guice
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooser;

import java.util.HashMap;

/**
 * QuadFlaskColorPickerFragment1
 * Circle Mode
 */
public class SimpleColorPickerFragment extends ColorPickerFragment
{
    protected EditText edit_r, edit_g, edit_b, edit_a, edit_hex;
    protected SeekBar seek_r, seek_g, seek_b, seek_a;
    protected View layout_a;
    protected HashMap<EditText, TextWatcher> onTextChanged = new HashMap<>();
    protected HashMap<SeekBar, SeekBar.OnSeekBarChangeListener> onSeekBarChanged = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.layout_colors_simple, container, false);
        initViews(getContext(), view);
        updateViews(getContext());
        return view;
    }

    @Override
    protected void initViews(Context context, View view)
    {
        super.initViews(context, view);

        edit_hex = (EditText) view.findViewById(R.id.color_edit_hex);
        edit_r = (EditText) view.findViewById(R.id.color_edit_r);
        edit_g = (EditText) view.findViewById(R.id.color_edit_g);
        edit_b = (EditText) view.findViewById(R.id.color_edit_b);
        edit_a = (EditText) view.findViewById(R.id.color_edit_a);
        seek_r = (SeekBar) view.findViewById(R.id.color_seek_r);
        seek_g = (SeekBar) view.findViewById(R.id.color_seek_g);
        seek_b = (SeekBar) view.findViewById(R.id.color_seek_b);
        seek_a = (SeekBar) view.findViewById(R.id.color_seek_a);
        layout_a = view.findViewById(R.id.color_layout_a);

        seek_r.setMax(255);
        seek_g.setMax(255);
        seek_b.setMax(255);
        seek_a.setMax(255);

        onSeekBarChanged.put(seek_r, onSliderChangedRGB(edit_r));
        onSeekBarChanged.put(seek_g, onSliderChangedRGB(edit_g));
        onSeekBarChanged.put(seek_b, onSliderChangedRGB(edit_b));
        onSeekBarChanged.put(seek_a, onSliderChangedRGB(edit_a));

        onTextChanged.put(edit_r, onValueChangedRGB(edit_r));
        onTextChanged.put(edit_g, onValueChangedRGB(edit_g));
        onTextChanged.put(edit_b, onValueChangedRGB(edit_b));
        onTextChanged.put(edit_a, onValueChangedRGB(edit_a));
        onTextChanged.put(edit_hex, onValueChangedHex(edit_hex));
    }

    @Override
    protected void setListeners()
    {
        edit_r.addTextChangedListener(onTextChanged.get(edit_r));
        edit_g.addTextChangedListener(onTextChanged.get(edit_g));
        edit_b.addTextChangedListener(onTextChanged.get(edit_b));
        edit_a.addTextChangedListener(onTextChanged.get(edit_a));
        edit_hex.addTextChangedListener(onTextChanged.get(edit_hex));
        edit_hex.setOnEditorActionListener(onHexEditAction);

        seek_r.setOnSeekBarChangeListener(onSeekBarChanged.get(seek_r));
        seek_g.setOnSeekBarChangeListener(onSeekBarChanged.get(seek_g));
        seek_b.setOnSeekBarChangeListener(onSeekBarChanged.get(seek_b));
        seek_a.setOnSeekBarChangeListener(onSeekBarChanged.get(seek_a));
    }

    private final TextView.OnEditorActionListener onHexEditAction = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL)
            {
                StringBuilder value = new StringBuilder(edit_hex.getText().toString());
                while (value.length() < (showAlpha() ? 9 : 7)) {
                    value.append("F");
                }
                ColorChooser.HexColorTextWatcher hexWatcher = (ColorChooser.HexColorTextWatcher) onTextChanged.get(edit_hex);
                if (hexWatcher != null) {
                    hexWatcher.onValueChanged(value.toString());
                }
            }
            return true;
        }
    };

    @Override
    protected void clearListeners()
    {
        seek_r.setOnSeekBarChangeListener(null);
        seek_g.setOnSeekBarChangeListener(null);
        seek_b.setOnSeekBarChangeListener(null);
        seek_a.setOnSeekBarChangeListener(null);

        edit_r.removeTextChangedListener(onTextChanged.get(edit_r));
        edit_g.removeTextChangedListener(onTextChanged.get(edit_g));
        edit_b.removeTextChangedListener(onTextChanged.get(edit_b));
        edit_a.removeTextChangedListener(onTextChanged.get(edit_a));
        edit_hex.removeTextChangedListener(onTextChanged.get(edit_hex));
        edit_hex.setOnEditorActionListener(null);
    }

    protected int[] getRGB()
    {
        int[] value = new int[3];

        try {
            value[0] = Integer.parseInt(edit_r.getText().toString());
        } catch (NumberFormatException e) {
            value[0] = 0;
        }
        try {
            value[1] = Integer.parseInt(edit_g.getText().toString());
        } catch (NumberFormatException e) {
            value[1] = 0;
        }
        try {
            value[2] = Integer.parseInt(edit_b.getText().toString());
        } catch (NumberFormatException e) {
            value[2] = 0;
        }

        for (int i=0; i<value.length; i++)
        {
            if (value[i] > 255) {
                value[i] = 255;
            } else if (value[i] < 0) {
                value[i] = 0;
            }
        }
        return value;
    }

    protected int getAlpha()
    {
        try {
            int a = Integer.parseInt(edit_a.getText().toString());
            return a >= 0 && a < 255 ? a : 255;
        } catch (NumberFormatException e) {
             return 255;
        }
    }

    protected SeekBar.OnSeekBarChangeListener onSliderChangedRGB(final EditText edit)
    {
        return new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("DEBUG", "onProgressChanged: " + progress + " (" + fromUser + ")");
                if (fromUser) {
                    edit.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }

    protected TextWatcher onValueChangedRGB(final EditText edit)
    {
        return new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s)
            {
                if (ignoreNextChange) {
                    ignoreNextChange = false;
                    return;
                }

                if (!s.toString().isEmpty())
                {
                    int v = getRGBValue(s.toString());
                    int[] rgb = getRGB();
                    Log.d("DEBUG", "setColor: afterTextChanged: " + v);
                    setColor(Color.argb(getAlpha(), rgb[0], rgb[1], rgb[2]), true);

                    ignoreNextChange = true;
                    edit.setText(Integer.toString(v));
                    edit.setSelection(edit.getText().length());   // TODO: fix frequent IndexOutOfBoundsException here
                }
            }

            private boolean ignoreNextChange = false;
        };
    }

    protected TextWatcher onValueChangedHex(final EditText edit) {
        return new ColorChooser.HexColorTextWatcher(showAlpha()) {
            @Override
            public void onValueChanged(String hexValue) {
                setColor(hexValue);
                Log.d("DEBUG", "setColor: hexChanged: " + hexValue);
                edit.setSelection(edit.getText().length());
            }
        };
    }

    protected int getRGBValue(String s)
    {
        int v;
        try
        {
            v = Integer.parseInt(s);
            if (v < 0) {
                v = 0;
            } else if (v >= 256) {
                v = 255;
            }
        } catch (NumberFormatException e) {
            v = 0;
        }
        return v;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateViews(Context context)
    {
        super.updateViews(context);

        int color = getColor();
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        edit_hex.setText( String.format("#%08X", color) );
        edit_r.setText(Integer.toString(r));
        edit_g.setText(Integer.toString(g));
        edit_b.setText(Integer.toString(b));
        edit_a.setText(showAlpha() ? Integer.toString(a) : "255");
        layout_a.setVisibility(showAlpha() ? View.VISIBLE : View.GONE);

        seek_r.setProgress(r);
        seek_g.setProgress(g);
        seek_b.setProgress(b);
        seek_a.setProgress(a);
    }

}
