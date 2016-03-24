package com.wx.wheelview.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wx.wheelview.common.WheelConstants;

/**
 * 滚轮Item布局，包含图片和文本
 *
 * @author fengwx
 */
public class WheelItem extends LinearLayout {

    private ImageView mImage;
    private TextView mText;

    public WheelItem(Context context) {
        super(context);
        init();
    }

    public WheelItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WheelItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setPadding(WheelConstants.WHEEL_ITEM_PADDING, WheelConstants.WHEEL_ITEM_PADDING, WheelConstants
                        .WHEEL_ITEM_PADDING,
                WheelConstants.WHEEL_ITEM_PADDING);

        // 图片
        mImage = new ImageView(getContext());
        mImage.setTag(WheelConstants.WHEEL_ITEM_IMAGE_TAG);
        mImage.setVisibility(View.GONE);
        LayoutParams imageParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        imageParams.rightMargin = WheelConstants.WHEEL_ITEM_MARGIN;
        addView(mImage, imageParams);

        // 文本
        mText = new TextView(getContext());
        mText.setTag(WheelConstants.WHEEL_ITEM_TEXT_TAG);
        mText.setSingleLine();
        mText.setTextColor(Color.BLACK);
        LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(mText, textParams);

    }

    /**
     * 设置文本
     *
     * @param text
     */
    public void setText(CharSequence text) {
        mText.setText(text);
    }

    /**
     * 设置图片资源
     *
     * @param resId
     */
    public void setImage(int resId) {
        mImage.setVisibility(View.VISIBLE);
        mImage.setImageResource(resId);
    }


}
