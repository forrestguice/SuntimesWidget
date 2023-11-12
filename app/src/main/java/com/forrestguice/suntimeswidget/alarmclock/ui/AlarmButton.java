/**
    Copyright (C) 2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

/**
 * AlarmButton
 */
public class AlarmButton extends RelativeLayout
{
    public static final String ARG_START = "start";
    public static final int ACTIVATED_START = 10;

    public static final String ARG_END = "end";
    public static final int ACTIVATED_END = 0;

    public AlarmButton(Context context) {
        super(context);
        init(context, null, -1, -1);
    }
    public AlarmButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }
    public AlarmButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }
    public AlarmButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getLayoutResID() {
        return R.layout.layout_view_alarmbutton;
    }

    protected View touchArea;

    protected View thumb;
    protected TextView thumbText;
    protected ImageView thumbIcon;

    protected View dragHint;
    protected View[] allDragHints;
    protected View[] allDragTargets;
    protected View shade;

    protected View frameStart, frameEnd, frameTop, frameBottom;
    protected View[] frameViews;

    protected int color_accent = Color.CYAN;

    protected float x0 = -1, y0 = -1;
    protected long dragStartedAt = -1;
    protected boolean[] dragLock = new boolean[] { false, true };
    protected boolean dragging = false;
    protected boolean settling = false;

    protected int activatedDirection = ACTIVATED_END;
    protected boolean activated = false;
    public boolean isActivated() {
        return activated;
    }

    protected void initLocale(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlarmButton);
        try {
            String directionArg = a.getString(R.styleable.AlarmButton_dragActivatedDirection);
            activatedDirection = (ARG_START.equals(directionArg) ? ACTIVATED_START : ACTIVATED_END);
        } finally {
            a.recycle();
        }

        a = context.obtainStyledAttributes(attrs, new int[] { R.attr.text_accentColor });
        try {
            color_accent = ContextCompat.getColor(context, a.getResourceId(0, R.color.text_accent_dark));
        } finally {
            a.recycle();
        }
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        initLocale(context, attrs);
        LayoutInflater.from(context).inflate(getLayoutResID(), this, true);

        thumb = findViewById(R.id.thumb);
        thumbText = (TextView) findViewById(R.id.thumb_text);
        thumbIcon = (ImageView) findViewById(R.id.thumb_icon);

        allDragTargets = new View[] {
                findViewById(R.id.shade_start),
                findViewById(R.id.shade_end)
        };
        for (View hint : allDragTargets) {
            hint.setVisibility(View.GONE);
        }

        allDragHints = new View[] {
                findViewById(R.id.layout_drag_hint_start),
                findViewById(R.id.layout_drag_hint_top),
                findViewById(R.id.layout_drag_hint_end),
                findViewById(R.id.layout_drag_hint_bottom)
        };
        for (View hint : allDragHints) {
            hint.setVisibility(View.GONE);
        }

        dragHint = (activatedDirection == ACTIVATED_END ? allDragHints[2] : allDragHints[0]);
        dragHint.setVisibility(View.VISIBLE);

        shade = (activatedDirection == ACTIVATED_END ? allDragTargets[1] : allDragTargets[0]);
        shade.setVisibility(View.VISIBLE);
        shade.setAlpha(0);

        frameStart = findViewById(R.id.frame_start);
        frameEnd = findViewById(R.id.frame_end);
        frameTop = findViewById(R.id.frame_top);
        frameBottom = findViewById(R.id.frame_bottom);

        if (activatedDirection == ACTIVATED_END) {
            frameEnd.setBackgroundColor(color_accent);
        } else frameStart.setBackgroundColor(color_accent);

        frameViews = new View[] { dragHint, frameStart, frameEnd, frameTop, frameBottom };
        for (View v : frameViews) {
            v.setAlpha(0);   // hint/frame is initially hidden
        }

        touchArea = findViewById(R.id.touchArea);
        touchArea.setOnTouchListener(new OnTouchListener()
        {
            protected float width, height;
            protected float thumbWidth, thumbHeight;
            protected float thumbWidth2, thumbHeight2;

            private void initPosition()
            {
                width = getWidth();
                height = getHeight();

                thumbWidth = thumb.getWidth();
                thumbHeight = thumb.getHeight();
                thumbWidth2 = thumbWidth / 2f;
                thumbHeight2 = thumbHeight / 2f;

                if (x0 == -1) {
                    x0 = thumb.getX();
                }
                if (y0 == -1) {
                    y0 = thumb.getY();
                }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                initPosition();
                float eventX = event.getX();
                float eventY = event.getY();

                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        if (!settling && eventX >= thumb.getX() && eventX < thumb.getX() + thumbWidth
                                && eventY >= thumb.getY() && eventY < thumb.getY() + thumbHeight)
                        {
                            dragging = true;
                            dragStartedAt = System.currentTimeMillis();
                            triggerRippleAnimation(thumb, eventX - thumb.getX(), eventY - thumb.getY(), true);
                            animateShowFrame(true);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (dragging)
                        {
                            if (!dragLock[0] && eventX - thumbWidth2 >= 0 && eventX + thumbWidth2 < width) {
                                thumb.setX(eventX - thumbWidth2);
                            }
                            if (!dragLock[1] && eventY - thumbHeight2 >= 0 && eventY + thumbHeight2 < height) {
                                thumb.setY(eventY - thumbHeight2);
                            }
                            if (checkActivated()) {
                                touchArea.dispatchTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis() + 100,
                                        MotionEvent.ACTION_UP, eventX, eventY, 0));
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (dragging)
                        {
                            dragging = false;

                            if (activated) {
                                triggerRippleAnimation(thumb, eventX, eventY, false);
                                animateActivated(false, null);

                            } else {
                                activated = checkActivated();
                                triggerRippleAnimation(thumb, eventX, eventY, false);
                                animateActivated(activated, new AnimatorListenerAdapter()
                                {
                                    public void onAnimationEnd(Animator animation) {
                                        if (activated) {
                                            AlarmButton.this.performClick();
                                        }
                                    }
                                });
                            }
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });

        applyAttributes(context, attrs);
    }

    protected void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlarmButton);
        try {
            setThumbText(a.getText(R.styleable.AlarmButton_android_text));
            setThumbImageDrawable(a.getDrawable(R.styleable.AlarmButton_android_src));
        } finally {
            a.recycle();
        }
    }

    protected boolean checkActivated()
    {
        switch (activatedDirection)
        {
            case ACTIVATED_START:
                return ((thumb.getX() <= dragHint.getX() + dragHint.getWidth() / 2f));

            case ACTIVATED_END:
            default:
                return ((thumb.getX() + thumb.getWidth() >= dragHint.getX() + dragHint.getWidth() / 2f));
        }
    }

    protected void animateActivated(boolean value, final AnimatorListenerAdapter listener)
    {
        int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        AnimatorListenerAdapter l = (value ? new AnimatorListenerAdapter()    // to active
        {
            public void onAnimationStart(Animator animation) {
                activated = true;
                shade.setAlpha(1);
                if (listener != null) {
                    listener.onAnimationStart(animation);
                }
            }
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
                animateReturnThumb(x0, y0, null);
            }

        } : new AnimatorListenerAdapter()                                            // to inactive
        {
            public void onAnimationStart(Animator animation) {
                animateReturnThumb(x0, y0, null);
                if (listener != null) {
                    listener.onAnimationStart(animation);
                }
            }
            public void onAnimationEnd(Animator animation) {
                activated = false;
                shade.setAlpha(0);
                animateShowFrame(false);
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
        });
        animateResizeWidth(shade, value ? getWidth() : 1, duration, l);
    }

    protected void animateResizeWidth(final View v, int width, int duration, @Nullable final AnimatorListenerAdapter listener)
    {
        ValueAnimator animation = ValueAnimator.ofInt(v.getWidth(), width);
        animation.setDuration(duration);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().width = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        animation.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationStart(Animator animation) {
                settling = true;
                if (listener != null) {
                    listener.onAnimationStart(animation);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                settling = false;
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
        });
        animation.start();
    }

    protected void animateShowFrame(boolean visible)
    {
        int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        animateShowViews(frameViews, duration, visible);
    }

    protected void animateShowViews(View[] views, int duration, boolean visible)
    {
        ViewPropertyAnimator[] animation = new ViewPropertyAnimator[views.length];
        for (int i=0; i<views.length; i++) {
            animation[i] = views[i].animate().alpha(visible ? 1 : 0).setDuration(duration);
        }
        for (ViewPropertyAnimator a : animation) {
            a.start();
        }
    }

    protected void animateReturnThumb(float x, float y, final AnimatorListenerAdapter listener)
    {
        int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        thumb.animate().x(x).y(y).setDuration(duration).setInterpolator(new OvershootInterpolator()).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationStart(Animator animation) {
                settling = true;
                if (listener != null) {
                    listener.onAnimationStart(animation);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                settling = false;
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
        });
    }

    public static void triggerRippleAnimation(View v, float x, float y, boolean pressed)
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            Drawable d = v.getBackground();
            if (d instanceof RippleDrawable)
            {
                RippleDrawable r = (RippleDrawable) d;
                if (pressed) {
                    r.setHotspot(x, y);
                    r.setState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled });
                } else {
                    r.setState(new int[] { android.R.attr.state_enabled, android.R.attr.state_enabled });
                }
            }
        }
    }

    public void setThumbText(CharSequence text) {
        thumbText.setText(text);
    }

    public void setThumbTextColor(int color) {
        thumbText.setTextColor(color);
    }
    public void setThumbTextColor(ColorStateList color) {
        thumbText.setTextColor(color);
    }

    public void setThumbImageDrawable(Drawable d) {
        thumbIcon.setImageDrawable(d);
    }

    public void setThumbImageTint(int color)
    {
        Drawable d = thumbIcon.getDrawable();
        if (d != null) {
            d.mutate();
            d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        thumbIcon.setImageDrawable(d);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener)
    {
        super.setOnClickListener(listener);
        this.onClickListener = listener;
    }
    protected View.OnClickListener onClickListener = null;

    @Override
    public boolean performClick()
    {
        if (onClickListener != null) {
            onClickListener.onClick(this);
            return true;
        }
        return super.performClick();
    }
}