package com.wirelesspienetwork.overview.misc;

import android.graphics.Rect;

import com.wirelesspienetwork.overview.BuildConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 辅助类
 */
public class Utilities {

    // Reflection methods for altering shadows
    private static Method sPropertyMethod;
    static {
        try {
            Class<?> c = Class.forName("android.view.GLES20Canvas");
            sPropertyMethod = c.getDeclaredMethod("setProperty", String.class, String.class);
            if (!sPropertyMethod.isAccessible()) sPropertyMethod.setAccessible(true);
        } catch (ClassNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                ex.printStackTrace();
            }
        }
     }

    /**
     * Scales a rect about its centroid
     */
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
