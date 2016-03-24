package com.wx.wheelview.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.wx.wheelview.common.WheelConstants;
import com.wx.wheelview.widget.WheelView;

/**
 * holo滚轮样式
 *
 * @author fengwx
 */
public class HoloDrawable extends WheelDrawable {

    private Paint mHoloBgPaint, mHoloPaint;

    private int mWheelSize, mItemH;

    public HoloDrawable(int width, int height, WheelView.WheelViewStyle style, int wheelSize, int itemH) {
        super(width, height, style);
        mWheelSize = wheelSize;
        mItemH = itemH;
        init();
    }

    private void init() {
        mHoloBgPaint = new Paint();
        mHoloBgPaint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants
                .WHEEL_SKIN_HOLO_BG);

        mHoloPaint = new Paint();
        mHoloPaint.setStrokeWidth(3);
        mHoloPaint.setColor(mStyle.holoBorderColor != -1 ? mStyle.holoBorderColor : WheelConstants
                .WHEEL_SKIN_HOLO_BORDER_COLOR);
    }


    @Override
    public void draw(Canvas canvas) {
        // draw background
        canvas.drawRect(0, 0, mWidth, mHeight, mHoloBgPaint);

        // draw select border
        if (mItemH != 0) {
            canvas.drawLine(0, mItemH * (mWheelSize / 2), mWidth, mItemH
                    * (mWheelSize / 2), mHoloPaint);
            canvas.drawLine(0, mItemH * (mWheelSize / 2 + 1), mWidth, mItemH
                    * (mWheelSize / 2 + 1), mHoloPaint);
        }
    }
}
