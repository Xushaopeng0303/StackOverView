/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License.
 */

package com.wirelesspienetwork.overview.misc;

import android.graphics.Rect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* Common code */
public class Utilities {

    // Reflection methods for altering shadows
    private static Method sPropertyMethod;
    static {
        try {
            Class<?> c = Class.forName("android.view.GLES20Canvas");
            sPropertyMethod = c.getDeclaredMethod("setProperty", String.class, String.class);
            if (!sPropertyMethod.isAccessible()) sPropertyMethod.setAccessible(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /** Scales a rect about its centroid */
    public static void scaleRectAboutCenter(Rect r, float scale) {
        if (scale != 1.0f) {
            int cx = r.centerX();
            int cy = r.centerY();
            r.offset(-cx, -cy);
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
            r.offset(cx, cy);
        }
    }

    /** Sets some private shadow properties. */
    public static void setShadowProperty(String property, String value)
            throws IllegalAccessException, InvocationTargetException {
        if (sPropertyMethod != null) {
            sPropertyMethod.invoke(null, property, value);
        }
    }
}
