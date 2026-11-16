/*
 * Copyright (C) 2016 venshine.cn@gmail.com
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
package com.wx.wheelview.widget

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wx.wheelview.common.WheelConstants
import com.wx.wheelview.util.WheelUtils

/**
 * 滚轮Item布局，包含图片和文本
 *
 * @author venshine
 */
open class WheelItem : FrameLayout {

    private lateinit var mImage: ImageView
    private lateinit var mText: TextView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * 初始化
     */
    private fun init() {
        val layout = LinearLayout(context)
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT,
            WheelUtils.dip2px(context, WheelConstants.WHEEL_ITEM_HEIGHT.toFloat()))
        layout.orientation = LinearLayout.HORIZONTAL
        // 只留水平内边距：条目高度是 45dp，上下各 20px 在低密度屏上会把文字挤得只剩几像素（issue #28 #36）
        layout.setPadding(WheelConstants.WHEEL_ITEM_PADDING, 0, WheelConstants.WHEEL_ITEM_PADDING, 0)
        layout.gravity = Gravity.CENTER
        addView(layout, layoutParams)

        // 图片
        mImage = ImageView(context)
        mImage.tag = WheelConstants.WHEEL_ITEM_IMAGE_TAG
        mImage.visibility = View.GONE
        val imageParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        imageParams.rightMargin = WheelConstants.WHEEL_ITEM_MARGIN
        layout.addView(mImage, imageParams)

        // 文本
        mText = TextView(context)
        mText.tag = WheelConstants.WHEEL_ITEM_TEXT_TAG
        mText.ellipsize = TextUtils.TruncateAt.END
        mText.setSingleLine()
        mText.includeFontPadding = false
        mText.gravity = Gravity.CENTER
        mText.setTextColor(Color.BLACK)
        val textParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        layout.addView(mText, textParams)
    }

    /**
     * 设置文本
     *
     * @param text
     */
    open fun setText(text: CharSequence?) {
        mText.text = text
    }

    /**
     * 设置图片资源
     *
     * @param resId
     */
    open fun setImage(resId: Int) {
        mImage.visibility = View.VISIBLE
        mImage.setImageResource(resId)
    }
}
