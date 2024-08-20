package com.forrestguice.suntimeswidget.settings.colors.quadflask;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.Utils;
import com.flask.colorpicker.builder.PaintBuilder;
import com.forrestguice.suntimeswidget.R;

/**
 * this file contains a modified copy of `com.flask.colorpicker.slider.LightnessSlider`
 */

@SuppressWarnings("Convert2Diamond")
public class LightnessSlider extends com.flask.colorpicker.slider.LightnessSlider
{
    private boolean inVerticalOrientation;

    private int color;
    private Paint barPaint = PaintBuilder.newPaint().build();
    private Paint solid = PaintBuilder.newPaint().build();
    private Paint clearingStroke = PaintBuilder.newPaint().color(0xffffffff).xPerMode(PorterDuff.Mode.CLEAR).build();

    private ColorPickerView colorPicker;

    public LightnessSlider(Context context) {
        super(context);
        init(context, null);
    }

    public LightnessSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LightnessSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.AbsCustomSlider, 0, 0);
        try {
            inVerticalOrientation = styledAttrs.getBoolean(
                    R.styleable.AbsCustomSlider_inVerticalOrientation, inVerticalOrientation);
        } finally {
            styledAttrs.recycle();
        }
    }

    @Override
    protected void drawBar(Canvas barCanvas) {
        int width = barCanvas.getWidth();
        int height = barCanvas.getHeight();

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        int l = Math.max(2, width / 256);
        for (int x = 0; x <= width; x += l) {
            hsv[2] = (float) x / (width - 1);
            barPaint.setColor(Color.HSVToColor(hsv));
            barCanvas.drawRect(x, 0, x + l, height, barPaint);
        }
    }

    @Override
    protected void onValueChanged(float value) {
        if (colorPicker != null)
            colorPicker.setLightness(value);
    }

    @Override
    protected void drawHandle(Canvas canvas, float x, float y) {
        solid.setColor(Utils.colorAtLightness(color, value));
        canvas.drawCircle(x, y, handleRadius, clearingStroke);
        canvas.drawCircle(x, y, handleRadius * 0.75f, solid);
    }

    public void setColorPicker(ColorPickerView colorPicker) {
        this.colorPicker = colorPicker;
    }

    public void setColor(int color) {
        this.color = color;
        this.value = Utils.lightnessOfColor(color);
        if (bar != null) {
            updateBar();
            invalidate();
        }
    }

    @Override
    protected void createBitmaps()
    {
        int width;
        int height;

        if (inVerticalOrientation) {
            width = getHeight();
            height = getWidth();
        } else {
            width = getWidth();
            height = getHeight();
        }

        int bW = width - barOffsetX * 2;    // check width and height; createBitmap requires both must be >0
        if (bW <= 0) {
            bW = 1;
        }
        int bH = barHeight;
        if (bH <= 0) {
            bH = 1;
        }

        bar = Bitmap.createBitmap(bW, bH, Bitmap.Config.ARGB_8888);
        barCanvas = new Canvas(bar);

        int w = width;
        if (w <= 0) {
            w = 1;
        }
        int h = height;
        if (h <= 0) {
            h = 1;
        }

        if (bitmap == null || bitmap.getWidth() != w || bitmap.getHeight() != h)
        {
            if (bitmap != null) bitmap.recycle();
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
        }
    }
}
