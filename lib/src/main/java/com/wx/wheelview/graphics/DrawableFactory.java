package com.wx.wheelview.graphics;

import android.graphics.drawable.Drawable;

import com.wx.wheelview.widget.WheelView;

/**
 * @author fengwx
 */
public class DrawableFactory {

    public static Drawable createDrawable(WheelView.Skin skin, int width, int height, WheelView.WheelViewStyle
            style, int wheelSize, int itemH) {
        if (skin.equals(WheelView.Skin.Common)) {
            return new CommonDrawable(width, height, style, wheelSize, itemH);
        } else if (skin.equals(WheelView.Skin.Holo)) {
            return new HoloDrawable(width, height, style, wheelSize, itemH);
        } else {
            return new WheelDrawable(width, height, style);
        }
    }

}
