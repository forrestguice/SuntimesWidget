/**
    Copyright (C) 2014-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.content.ContextCompat;

public class SpanUtils
{
    public static final String SPANTAG_DST = "[d]";
    public static final String SPANTAG_WARNING = "[w]";

    public static final int DEF_WARNING_DRAWABLE = R.drawable.ic_action_warning;
    public static final int DEF_ERROR_DRAWABLE = R.drawable.ic_action_error;
    public static final int DEF_DST_DRAWABLE = R.drawable.ic_weather_sunny;

    public SpanUtils() {
    }

    public static SpannableStringBuilder createSpan(Context context, String text, String spanTag, ImageSpan imageSpan)
    {
        return createSpan(context, text, spanTag, imageSpan, ImageSpan.ALIGN_BASELINE);
    }
    public static SpannableStringBuilder createSpan(Context context, String text, String spanTag, ImageSpan imageSpan, int alignment)
    {
        ImageSpanTag[] tags = { new ImageSpanTag(spanTag, imageSpan) };
        return createSpan(context, text, tags, alignment);
    }

    public static SpannableStringBuilder createSpan(Context context, String text, ImageSpanTag[] tags) {
        return createSpan(context, text, tags, ImageSpan.ALIGN_BASELINE);
    }
    public static SpannableStringBuilder createSpan(Context context, String text, ImageSpanTag[] tags, int alignment)
    {
        SpannableStringBuilder span = new SpannableStringBuilder(text);
        ImageSpan blank = createImageSpan(context, R.drawable.ic_transparent, 0, 0, R.color.transparent);

        for (ImageSpanTag tag : tags)
        {
            String spanTag = tag.getTag();
            ImageSpan imageSpan = (tag.getSpan() == null) ? blank : tag.getSpan();

            int tagPos;
            while ((tagPos = text.indexOf(spanTag)) >= 0)
            {
                int tagEnd = tagPos + spanTag.length();
                //Log.d("DEBUG", "tag=" + spanTag + ", tagPos=" + tagPos + ", " + tagEnd + ", text=" + text);

                span.setSpan(createImageSpan(imageSpan), tagPos, tagEnd, alignment);
                text = text.substring(0, tagPos) + tag.getBlank() + text.substring(tagEnd);
            }
        }
        return span;
    }

    public static SpannableStringBuilder createSpan(Context context, CharSequence text, ImageSpanTag[] tags) {
        return createSpan(context, text, tags, ImageSpan.ALIGN_BASELINE);
    }
    public static SpannableStringBuilder createSpan(Context context, CharSequence text, ImageSpanTag[] tags, int alignment)
    {
        SpannableStringBuilder span = new SpannableStringBuilder(text);
        ImageSpan blank = createImageSpan(context, R.drawable.ic_transparent, 0, 0, R.color.transparent);

        for (ImageSpanTag tag : tags)
        {
            String spanTag = tag.getTag();
            ImageSpan imageSpan = (tag.getSpan() == null) ? blank : tag.getSpan();

            int tagPos;
            while ((tagPos = TextUtils.indexOf(text, spanTag )) >= 0)
            {
                int tagEnd = tagPos + spanTag.length();
                //Log.d("DEBUG", "tag=" + spanTag + ", tagPos=" + tagPos + ", " + tagEnd + ", text=" + text);

                span.setSpan(createImageSpan(imageSpan), tagPos, tagEnd, alignment);
                text = text.subSequence(0, tagPos) + tag.getBlank() + text.subSequence(tagEnd, text.length());
            }
        }
        return span;
    }

    public static SpannableString createRoundedBackgroundColorSpan(SpannableString span, String text, String toColorize,
                                                                   final int textColor, final boolean boldText,
                                                                   final int backgroundColor, final float cornerRadiusPx, final float paddingPx)
    {
        ReplacementSpan replacementSpan = new ReplacementSpan()
        {
            @Override
            public int getSize(@NonNull Paint p, CharSequence t, int start, int end, @Nullable Paint.FontMetricsInt fontMetrics) {
                return (int) Math.ceil(p.measureText(t, start, end) + (2 * paddingPx));
            }

            @Override
            public void draw(@NonNull Canvas c, CharSequence t, int start, int end, float x, int top, int y, int bottom, @NonNull Paint p)
            {
                p.setColor(backgroundColor);
                RectF rect = new RectF(x, top, x + p.measureText(t, start, end) + (2 * paddingPx), bottom);
                c.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, p);

                p.setColor(textColor);
                p.setTypeface(boldText ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                c.drawText(t, start, end, x + paddingPx, y, p);
            }
        };

        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toColorize);
        if (start >= 0)
        {
            int end = start + toColorize.length() + 1;  // 1 beyond last character
            span.setSpan(replacementSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createBackgroundColorSpan(SpannableString span, String text, String toColorize, int color)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toColorize);
        if (start >= 0)
        {
            int end = start + toColorize.length();
            span.setSpan(new BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createColorSpan(SpannableString span, String text, String toColorize, int color)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toColorize);
        if (start >= 0)
        {
            int end = start + toColorize.length();
            span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }
    public static SpannableString createColorSpan(SpannableString span, String text, String toColorize, int color, boolean bold)
    {
        if (bold) {
            span = createBoldSpan(span, text, toColorize);
        }
        return createColorSpan(span, text, toColorize, color);
    }

    public static SpannableString createUnderlineSpan(SpannableString span, String text, String toUnderline)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toUnderline);
        if (start >= 0)
        {
            int end = start + toUnderline.length();
            span.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }
    public static SpannableString createUnderlineSpan(SpannableString span, String text, String toUnderline, int color)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toUnderline);
        if (start >= 0)
        {
            UnderlineSpan underline = new UnderlineSpan();
            TextPaint paint = new TextPaint();
            paint.setColor(color);
            underline.updateDrawState(paint);
            int end = start + toUnderline.length();
            span.setSpan(underline, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createBoldSpan(SpannableString span, String text, String toBold)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toBold);
        if (start >= 0)
        {
            int end = start + toBold.length();
            span.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createItalicSpan(SpannableString span, String text, String toBold)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toBold);
        if (start >= 0)
        {
            int end = start + toBold.length();
            span.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createBoldColorSpan(SpannableString span, String text, String toBold, int color)
    {
        return createColorSpan(createBoldSpan(span, text, toBold), text, toBold, color);
    }

    public static SpannableString createRelativeSpan(SpannableString span, String text, String toRelative, float relativeSize)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toRelative);
        if (start >= 0)
        {
            int end = start + toRelative.length();
            span.setSpan(new RelativeSizeSpan(relativeSize), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createAbsoluteSpan(SpannableString span, String text, String toAbsolute, int pointSizePixels)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toAbsolute);
        if (start >= 0)
        {
            int end = start + toAbsolute.length();
            span.setSpan(new AbsoluteSizeSpan(pointSizePixels), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    /**
     * TypefaceSpan
     */

    public static SpannableString createTypefaceSpan(SpannableString span, String text, String toTypeface, String typeface)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toTypeface);
        if (start >= 0)
        {
            int end = start + toTypeface.length();
            span.setSpan(new TypefaceSpan(typeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    /**
     * @param htmlString html markup
     * @return an html span
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

    public static class TypefaceSpan extends MetricAffectingSpan
    {
        protected final Typeface typeface;

        public TypefaceSpan(String typeface) {
            this.typeface = Typeface.create(typeface, Typeface.NORMAL);
        }

        @Override
        public void updateDrawState(TextPaint paint) {
            paint.setTypeface(typeface);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            paint.setTypeface(typeface);
        }
    }

    public static ImageSpan createWarningSpan(Context context, int height)
    {
        //noinspection SuspiciousNameCombination
        return createWarningSpan(context, height, height);
    }

    public static ImageSpan createWarningSpan(Context context, float height)
    {
        return createWarningSpan(context, (int) Math.ceil(height));
    }
    public static ImageSpan createWarningSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionWarning, R.attr.tagColor_warning});
        int drawableID = a.getResourceId(0, DEF_WARNING_DRAWABLE);
        int colorID = a.getResourceId(1, R.color.warningTag_dark);
        a.recycle();
        return createImageSpan(context, drawableID, width, height, ContextCompat.getColor(context, colorID));
    }

    public static ImageSpan createErrorSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionError, R.attr.tagColor_error});
        int drawableID = a.getResourceId(0, DEF_ERROR_DRAWABLE);
        int colorID = a.getResourceId(1, R.color.errorTag_dark);
        a.recycle();
        return createImageSpan(context, drawableID, width, height, ContextCompat.getColor(context, colorID));
    }

    public static ImageSpan createDstSpan(Context context, float height)
    {
        return createDstSpan(context, (int) Math.ceil(height), (int) Math.ceil(height));
    }
    public static ImageSpan createDstSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionDst, R.attr.tagColor_dst});
        int drawableID = a.getResourceId(0, DEF_DST_DRAWABLE);
        int colorID = a.getResourceId(1, R.color.dstTag_dark);
        a.recycle();
        return createImageSpan(context, drawableID, width, height, ContextCompat.getColor(context, colorID));
    }

    public static ImageSpan createImageSpan(Context context, int drawableID, int width, int height, int tint)
    {
        return createImageSpan(context, drawableID, width, height, tint, PorterDuff.Mode.SRC_ATOP);
    }
    public static ImageSpan createImageSpan(Context context, int drawableID, int width, int height, int tint, PorterDuff.Mode tintMode)
    {
        Drawable drawable = null;
        try {
            drawable = ContextCompat.getDrawable(context.getResources(), drawableID, null);
        } catch (Exception e) {
            Log.e("createImageSpan", "invalid drawableID " + drawableID + "! ...set to null.");
        }

        if (drawable != null)
        {
            drawable.mutate();    // don't cache state (or setColorFilter modifies all instances)
            if (width > 0 && height > 0)
            {
                drawable.setBounds(0, 0, width, height);
            }
            drawable.setColorFilter(tint, tintMode);
        }
        return new ImageSpan(drawable);
    }

    public static ImageSpan createImageSpan(ImageSpan other)
    {
        Drawable drawable = null;
        if (other != null)
            drawable = other.getDrawable();

        return new ImageSpan(drawable);
    }

    /**
     * utility class; [Tag, ImageSpan] tuple
     */
    public static class ImageSpanTag
    {
        private final String tag;       // the tag, e.g. [w]
        private final ImageSpan span;   // an ImageSpan that should be substituted for the tag
        private String blank;     // a "blank" string the same length as the tag

        public ImageSpanTag(String tag, ImageSpan span)
        {
            this.tag = tag;
            this.span = span;
            buildBlankTag();
        }

        private void buildBlankTag()
        {
            blank = "";
            for (int i=0; i<tag.length(); i++)
            {   //noinspection StringConcatenationInLoop
                blank += " ";
            }
        }

        public String getTag()
        {
            return tag;
        }

        public ImageSpan getSpan()
        {
            return span;
        }

        public String getBlank()
        {
            return blank;
        }
    }
}
