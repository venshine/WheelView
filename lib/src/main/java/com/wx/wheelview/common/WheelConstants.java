package com.wx.wheelview.common;

import android.graphics.Color;

/**
 * 滚轮常量类
 *
 * @author fengwx
 */
public class WheelConstants {

    /**
     * 滚轮每一项的内边距
     */
    public static final int WHEEL_ITEM_PADDING = 20;

    /**
     * 滚轮每一项的内部控件外边距
     */
    public static final int WHEEL_ITEM_MARGIN = 20;

    /**
     * 滚轮每一项的图片TAG
     */
    public static final int WHEEL_ITEM_IMAGE_TAG = 100;

    /**
     * 滚轮每一项的文本TAG
     */
    public static final int WHEEL_ITEM_TEXT_TAG = 101;

    /**
     * 平滑滚动持续时间
     */
    public static final int WHEEL_SMOOTH_SCROLL_DURATION = 50;

    /**
     * 默认背景
     */
    public static final int WHEEL_BG = Color.WHITE;

    /**
     * common皮肤默认背景
     */
    public static final int WHEEL_SKIN_COMMON_BG = Color.parseColor("#dddddd");

    /**
     * holo皮肤默认背景
     */
    public static final int WHEEL_SKIN_HOLO_BG = Color.WHITE;

    /**
     * holo皮肤默认选中框颜色
     */
    public static final int WHEEL_SKIN_HOLO_BORDER_COLOR = Color.parseColor("#2196f3");

    /**
     * 滚轮item高度
     */
    public static final int WHEEL_ITEM_HEIGHT = 100;

    /**
     * 滚轮默认文本颜色
     */
    public static final int WHEEL_TEXT_COLOR = Color.BLACK;

    /**
     * 滚轮默认文本大小
     */
    public static final int WHEEL_TEXT_SIZE = 16;

    /**
     * 滚轮默认文本透明度
     */
    public static final float WHEEL_TEXT_ALPHA = 0.7f;

    /**
     * common皮肤默认选中框颜色
     */
    public static final int WHEEL_SKIN_COMMON_COLOR = Color.parseColor("#f0cfcfcf");

    /**
     * common皮肤选中框divider颜色
     */
    public static final int WHEEL_SKIN_COMMON_DIVIDER_COLOR = Color.parseColor("#b5b5b5");

    /**
     * common皮肤左右边框颜色
     */
    public static final int WHEEL_SKIN_COMMON_BORDER_COLOR = Color.parseColor("#666666");

    /**
     * 滚轮滑动时展示选中项
     */
    public static final int WHEEL_SCROLL_HANDLER_WHAT = 0x0100;

    /**
     * 滚轮滑动时展示选中项延迟时间
     */
    public static final int WHEEL_SCROLL_DELAY_DURATION = 600;

}
