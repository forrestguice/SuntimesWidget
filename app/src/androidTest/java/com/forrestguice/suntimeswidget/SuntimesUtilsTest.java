/**
    Copyright (C) 2017 Forrest Guice
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

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SuntimesUtilsTest
{
    private Context mockContext;

    private int warningSize = 16;
    private int warningColor = Color.RED;
    private int warningDrawableID = R.drawable.ic_action_warning;
    private Drawable warningDrawable;

    @Before
    public void setup()
    {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
        warningDrawable = mockContext.getResources().getDrawable(warningDrawableID);

        SuntimesUtils.initDisplayStrings(mockContext);
    }

    @Test
    public void testImageSpanTag()
    {
        String tag = "[tag]";
        ImageSpan imageSpan = new ImageSpan(warningDrawable);
        SuntimesUtils.ImageSpanTag spanTag = new SuntimesUtils.ImageSpanTag(tag, imageSpan);
        assertTrue("getTag() must return the same tag set by the constructor!", spanTag.getTag().equals(tag));
        assertTrue("getSpan() must return the same span set by the constructor!", spanTag.getSpan().equals(imageSpan) );
        assertTrue("getBlank() must return a string that is the same length as the tag!", spanTag.getBlank().length() == tag.length());
        assertTrue("getBlank() must return a blank string!", spanTag.getBlank().trim().isEmpty());

        SuntimesUtils.ImageSpanTag spanTag2 = new SuntimesUtils.ImageSpanTag(tag, null);
        assertTrue("getSpan() should return null (as set by constructor)", spanTag2.getSpan() == null);
    }

    @Test
    public void testCreateImageSpan()
    {
        ImageSpan span1 = SuntimesUtils.createImageSpan(null);
        assertTrue("createImageSpan(null) must not return null!", span1 != null);

        ImageSpan span2 = SuntimesUtils.createImageSpan(mockContext, -1, warningSize, warningSize, warningColor);
        assertTrue("createImageSpan(context, invalidDrawableId, w, h, tint) must not return null!", span2 != null);

        ImageSpan span3 = SuntimesUtils.createImageSpan(mockContext, warningDrawableID, warningSize, warningSize, warningColor);
        assertTrue("createImageSpan(context, validDrawableId, w, h, tint) must not return null!", span3 != null);

        Drawable drawable3 = span3.getDrawable();
        assertTrue("drawable must not be null", drawable3 != null);
        assertTrue("createImageSpan(context, drawableID, w, h, tint) should set width to w!", drawable3.getBounds().width() == warningSize);
        assertTrue("createImageSpan(context, drawableID, w, h, tint) should set height to h!", drawable3.getBounds().height() == warningSize);

        ImageSpan span4 = SuntimesUtils.createImageSpan(span3);
        assertTrue("createImageSpan(ImageSpan) should create a new object!", span4 != span3);
        assertTrue("createImageSpan(ImageSpan) should have the same drawable!", span4.getDrawable().equals(span3.getDrawable()));
    }
}
