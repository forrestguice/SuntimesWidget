/**
 * MIT License
 *
 * Copyright (c) 2023 Forrest Guice
 * Copyright (c) 2021 Kumaraswamy B.G
 *
 * This modified code is derived from the MIUI-autostart library (https://github.com/XomaDev/MIUI-autostart),
 * published by Kumaraswamy B.G under the MIT license.
 *
 * The original code includes the following copyright notice:
 * ----------------------------------------------------------
 * MIT License
 *
 * Copyright (c) 2021 Kumaraswamy B.G
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.forrestguice.suntimeswidget.alarmclock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XiomiAutostartDetect
{
    public static int STATE_UNKNOWN = -2;
    public static int STATE_NO_INFO = -1;
    public static int STATE_ENABLED = 0;
    public static int STATE_DISABLED = 1;

    @SuppressLint("PrivateApi")
    public static int getAutostartState_xiomi(Context context)
    {
        Class<?> clazz;
        try {
            clazz = Class.forName("android.miui.AppOpsUtils");
        } catch (ClassNotFoundException e) {
            Log.e("AutostartDetect", "failed to access class! " + e);
            return STATE_NO_INFO;
        }

        Method method;
        try {
            method = clazz.getDeclaredMethod("getApplicationAutoStart", Context.class, String.class);
            method.setAccessible(true);
        } catch (Exception e) {
            Log.e("AutostartDetect", "failed to access method! " + e);
            method = null;
        }
        if (method == null) {
            return STATE_NO_INFO;
        }

        Object resultObj;
        try {
            resultObj = method.invoke(null, context, context.getPackageName());

        } catch (IllegalAccessException e) {
            Log.e("AutostartDetect", "failed to call method! " + e);
            return STATE_NO_INFO;
        } catch (InvocationTargetException e) {
            Log.e("AutostartDetect", "failed to call method! " + e);
            return STATE_NO_INFO;
        }

        if (!(resultObj instanceof Integer)) {
            return STATE_UNKNOWN;
        }

        int result = (int) resultObj;
        switch (result) {
            case 0: return STATE_ENABLED;
            case 1: return STATE_DISABLED;
            default: return STATE_UNKNOWN;
        }
    }
}
