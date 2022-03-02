package com.open.thumbseekbar;

import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * ThumbUtils
 *
 * @Description:
 * @Author: xing.tang
 */
public class ThumbUtils {

    private static final String KEY_MIUI_MANE = "ro.miui.ui.version.name";
    private static final Properties PROPERTIES = new Properties();
    private static Boolean miui;


    /**
     * 是否MiUi手机
     *
     * @return boolean【true:是，false:否】
     */
    public static boolean isMiUi() {
        if (miui != null) return miui;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
                PROPERTIES.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            miui = PROPERTIES.containsKey(KEY_MIUI_MANE);
        } else {
            Class<?> clazz;
            try {
                clazz = Class.forName("android.os.SystemProperties");
                Method getMethod = clazz.getDeclaredMethod("get", String.class);
                String name = (String) getMethod.invoke(null, KEY_MIUI_MANE);
                miui = !TextUtils.isEmpty(name);
            } catch (Exception e) {
                e.printStackTrace();
                miui = false;
            }
        }
        return miui;
    }

    /**
     * 将dp转换为px
     *
     * @param dp float
     * @return int
     */
    public static int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 将sp转换为px
     *
     * @param sp int
     * @return int
     */
    public static int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }
}
