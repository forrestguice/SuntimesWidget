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

package com.forrestguice.colors;

public class ColorUtils
{
    /**
     * isTextReadable
     * @param textColor text color
     * @param backgroundColor background color
     * @return contrast ratio between textColor and backgroundColor is greater than 4.5
     */
    public static boolean isTextReadable(int textColor, int backgroundColor) {
        return getContrastRatio(textColor, backgroundColor) > 4.5;    // AA minimum; https://www.w3.org/TR/WCAG21/#contrast-minimum
    }

    /**
     * https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
     */
    public static double getLuminance(int color)
    {
        double r0 = Color.red(color) / 255d;
        double g0 = Color.green(color) / 255d;
        double b0 = Color.blue(color) / 255d;

        double r = ((r0 <= 0.04045) ? (r0 / 12.92) : Math.pow((r0 + 0.055) / 1.055, 2.4));
        double g = ((g0 <= 0.04045) ? (g0 / 12.92) : Math.pow((g0 + 0.055) / 1.055, 2.4));
        double b = ((b0 <= 0.04045) ? (b0 / 12.92) : Math.pow((b0 + 0.055) / 1.055, 2.4));

        return (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
    }
    public static double getContrastRatio(int textColor, int backgroundColor)
    {
        double l_textColor = getLuminance(textColor);
        double l_backgroundColor = getLuminance(backgroundColor);
        double l1 = Math.max(l_textColor, l_backgroundColor);
        double l2 = Math.min(l_textColor, l_backgroundColor);
        return (l1 + 0.05) / (l2 + 0.05);
    }

    /**
     * the following copied from ColorUtils.java
     * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/com/android/internal/graphics/ColorUtils.java
     */

    /*
     * Copyright (C) 2017 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License
     */

    private static final double XYZ_WHITE_REFERENCE_X = 95.047;
    private static final double XYZ_WHITE_REFERENCE_Y = 100;
    private static final double XYZ_WHITE_REFERENCE_Z = 108.883;
    private static final double XYZ_EPSILON = 0.008856;
    private static final double XYZ_KAPPA = 903.3;

    /**
     * android.support.v4.graphics.ColorUtils
     * Set the alpha component of {@code color} to be {@code alpha}.
     */
    public static int setAlphaComponent(int color, int alpha)
    {
        if (alpha < 0 || alpha > 255) {
            throw new IllegalArgumentException("alpha must be between 0 and 255.");
        }
        return (color & 0x00ffffff) | (alpha << 24);
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Composite two potentially translucent colors over each other and returns the result.
     */
    public static int compositeColors(int foreground, int background)
    {
        int bgAlpha = Color.alpha(background);
        int fgAlpha = Color.alpha(foreground);
        int a = compositeAlpha(fgAlpha, bgAlpha);
        int r = compositeComponent(Color.red(foreground), fgAlpha,
                Color.red(background), bgAlpha, a);
        int g = compositeComponent(Color.green(foreground), fgAlpha,
                Color.green(background), bgAlpha, a);
        int b = compositeComponent(Color.blue(foreground), fgAlpha,
                Color.blue(background), bgAlpha, a);
        return argb(a, r, g, b);
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Returns the composite alpha of the given foreground and background alpha.
     */
    public static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
        return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF);
    }
    private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
        if (a == 0) return 0;
        return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF);
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Returns the euclidean distance between two LAB colors.
     */
    public static double distanceEuclidean(double[] labX, double[] labY)
    {
        return Math.sqrt(Math.pow(labX[0] - labY[0], 2)
                + Math.pow(labX[1] - labY[1], 2)
                + Math.pow(labX[2] - labY[2], 2));
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Convert the ARGB color to its CIE Lab representative components.
     *
     * @param color  the ARGB color to convert. The alpha component is ignored
     * @param outLab 3-element array which holds the resulting LAB components
     */
    public static void colorToLAB(int color, double[] outLab) {
        RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), outLab);
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Convert RGB components to its CIE Lab representative components.
     *
     * <ul>
     * <li>outLab[0] is L [0 ...100)</li>
     * <li>outLab[1] is a [-128...127)</li>
     * <li>outLab[2] is b [-128...127)</li>
     * </ul>
     *
     * @param r      red component value [0..255]
     * @param g      green component value [0..255]
     * @param b      blue component value [0..255]
     * @param outLab 3-element array which holds the resulting LAB components
     */
    public static void RGBToLAB(int r, int g, int b, double[] outLab)
    {
        // First we convert RGB to XYZ
        RGBToXYZ(r, g, b, outLab);
        // outLab now contains XYZ
        XYZToLAB(outLab[0], outLab[1], outLab[2], outLab);
        // outLab now contains LAB representation
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Convert the ARGB color to its CIE XYZ representative components.
     *
     * <p>The resulting XYZ representation will use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).</p>
     *
     * <ul>
     * <li>outXyz[0] is X [0 ...95.047)</li>
     * <li>outXyz[1] is Y [0...100)</li>
     * <li>outXyz[2] is Z [0...108.883)</li>
     * </ul>
     *
     * @param color  the ARGB color to convert. The alpha component is ignored
     * @param outXyz 3-element array which holds the resulting LAB components
     */
    public static void colorToXYZ(int color, double[] outXyz) {
        RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Convert RGB components to its CIE XYZ representative components.
     *
     * <p>The resulting XYZ representation will use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).</p>
     *
     * <ul>
     * <li>outXyz[0] is X [0 ...95.047)</li>
     * <li>outXyz[1] is Y [0...100)</li>
     * <li>outXyz[2] is Z [0...108.883)</li>
     * </ul>
     *
     * @param r      red component value [0..255]
     * @param g      green component value [0..255]
     * @param b      blue component value [0..255]
     * @param outXyz 3-element array which holds the resulting XYZ components
     */
    public static void RGBToXYZ(int r, int g, int b, double[] outXyz)
    {
        if (outXyz.length != 3) {
            throw new IllegalArgumentException("outXyz must have a length of 3.");
        }
        double sr = r / 255.0;
        sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
        double sg = g / 255.0;
        sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
        double sb = b / 255.0;
        sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);
        outXyz[0] = 100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805);
        outXyz[1] = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
        outXyz[2] = 100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505);
    }

    /**
     * android.support.v4.graphics.ColorUtils
     * Converts a color from CIE XYZ to CIE Lab representation.
     *
     * <p>This method expects the XYZ representation to use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).</p>
     *
     * <ul>
     * <li>outLab[0] is L [0 ...100)</li>
     * <li>outLab[1] is a [-128...127)</li>
     * <li>outLab[2] is b [-128...127)</li>
     * </ul>
     *
     * @param x      X component value [0...95.047)
     * @param y      Y component value [0...100)
     * @param z      Z component value [0...108.883)
     * @param outLab 3-element array which holds the resulting Lab components
     */
    public static void XYZToLAB(double x, double y, double z, double[] outLab)
    {
        if (outLab.length != 3) {
            throw new IllegalArgumentException("outLab must have a length of 3.");
        }
        x = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X);
        y = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y);
        z = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z);
        outLab[0] = Math.max(0, 116 * y - 16);
        outLab[1] = 500 * (x - y);
        outLab[2] = 200 * (y - z);
    }

    private static double pivotXyzComponent(double component) {
        return component > XYZ_EPSILON
                ? Math.pow(component, 1 / 3.0)
                : (XYZ_KAPPA * component + 16) / 116;
    }

    /**
     * the following copied from:
     * android.graphics.Color
     */

    /*
     * Copyright (C) 2017 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License
     */

    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    public static int rgb(float red, float green, float blue) {
        return 0xff000000 |
                ((int) (red   * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) <<  8) |
                (int) (blue  * 255.0f + 0.5f);
    }

}
