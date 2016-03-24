package com.wx.wheelview.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;

import com.wx.wheelview.common.WheelConstants;
import com.wx.wheelview.widget.WheelView;

/**
 * common滚轮样式
 *
 * @author fengwx
 */
public class CommonDrawable extends WheelDrawable {

    private static final int[] SHADOWS_COLORS =
            {
                    0xFF111111,
                    0x00AAAAAA,
                    0x00AAAAAA
            };  // 阴影色值

    private GradientDrawable mTopShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            SHADOWS_COLORS);    // 顶部阴影

    private GradientDrawable mBottomShadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
            SHADOWS_COLORS);    // 底部阴影

    private Paint mCommonBgPaint, mCommonPaint, mCommonDividerPaint, mCommonBorderPaint;

    private int mWheelSize, mItemH;

    public CommonDrawable(int width, int height, WheelView.WheelViewStyle style, int wheelSize, int itemH) {
        super(width, height, style);
        mWheelSize = wheelSize;
        mItemH = itemH;
        init();
    }

    private void init() {
        mCommonBgPaint = new Paint();
        mCommonBgPaint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants
                .WHEEL_SKIN_COMMON_BG);

        mCommonPaint = new Paint();
        mCommonPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_COLOR);

        mCommonDividerPaint = new Paint();
        mCommonDividerPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_DIVIDER_COLOR);
        mCommonDividerPaint.setStrokeWidth(2);

        mCommonBorderPaint = new Paint();
        mCommonBorderPaint.setStrokeWidth(6);
        mCommonBorderPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_BORDER_COLOR);

//        mTopShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
//                SHADOWS_COLORS);
//        mBottomShadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                SHADOWS_COLORS);
    }

    @Override
    public void draw(Canvas canvas) {
        // draw background
        canvas.drawRect(0, 0, mWidth, mHeight, mCommonBgPaint);

        // draw select border
        if (mItemH != 0) {
            canvas.drawRect(0, mItemH * (mWheelSize / 2), mWidth, mItemH
                    * (mWheelSize / 2 + 1), mCommonPaint);
            canvas.drawLine(0, mItemH * (mWheelSize / 2), mWidth, mItemH
                    * (mWheelSize / 2), mCommonDividerPaint);
            canvas.drawLine(0, mItemH * (mWheelSize / 2 + 1), mWidth, mItemH
                    * (mWheelSize / 2 + 1), mCommonDividerPaint);

            // top, bottom
            mTopShadow.setBounds(0, 0, mWidth, mItemH);
            mTopShadow.draw(canvas);

            mBottomShadow.setBounds(0, mHeight - mItemH, mWidth, mHeight);
            mBottomShadow.draw(canvas);

            // left,right
            canvas.drawLine(0, 0, 0, mHeight, mCommonBorderPaint);
            canvas.drawLine(mWidth, 0, mWidth, mHeight, mCommonBorderPaint);
        }
    }
}
