package com.wx.wheelview.util;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;

/**
 * 滚轮工具类
 *
 * @author fengwx
 */
public class WheelUtils {

    private static final String TAG = "WheelView";

    /**
     * 打印日志
     *
     * @param msg
     */
    public static void log(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Log.d(TAG, msg);
        }
    }

    /**
     * 判断集合是否为空
     *
     * @param c
     * @param <V>
     * @return
     */
    public static <V> boolean isEmpty(Collection<V> c) {
        return (c == null || c.size() == 0);
    }

    /**
     * 遍历查找TextView
     *
     * @param view
     * @return
     */
    public static TextView findTextView(View view) {
        if (view instanceof TextView) {
            return (TextView) view;
        } else {
            if (view instanceof ViewGroup) {
                return findTextView(((ViewGroup) view).getChildAt(0));
            } else {
                return null;
            }
        }
    }


}
