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
package com.wx.wheelview.graphics

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import com.wx.wheelview.common.WheelConstants
import com.wx.wheelview.widget.WheelView

/**
 * common滚轮样式
 *
 * @author venshine
 */
open class CommonDrawable internal constructor(
    width: Int,
    height: Int,
    style: WheelView.WheelViewStyle,
    private val mWheelSize: Int,
    private val mItemH: Int,
) : WheelDrawable(width, height, style) {

    private val mTopShadow = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, SHADOWS_COLORS) // 顶部阴影

    private val mBottomShadow = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, SHADOWS_COLORS) // 底部阴影

    private val mCommonBgPaint = Paint().apply {
        color = mStyle.backgroundColor ?: WheelConstants.WHEEL_SKIN_COMMON_BG
    }

    private val mCommonPaint = Paint().apply {
        color = mStyle.selectedBackgroundColor ?: WheelConstants.WHEEL_SKIN_COMMON_COLOR
    }

    private val mCommonDividerPaint = Paint().apply {
        color = WheelConstants.WHEEL_SKIN_COMMON_DIVIDER_COLOR
        strokeWidth = 2f
    }

    private val mCommonBorderPaint = Paint().apply {
        strokeWidth = 6f
        color = WheelConstants.WHEEL_SKIN_COMMON_BORDER_COLOR
    }

    override fun draw(canvas: Canvas) {
        val width = mWidth.toFloat()
        val height = mHeight.toFloat()
        // draw background
        canvas.drawRect(0f, 0f, width, height, mCommonBgPaint)

        // draw select border
        if (mItemH != 0) {
            val size = mWheelSize shr 1
            val top = (mItemH * size).toFloat()
            val bottom = (mItemH * (size + 1)).toFloat()
            canvas.drawRect(0f, top, width, bottom, mCommonPaint)
            canvas.drawLine(0f, top, width, top, mCommonDividerPaint)
            canvas.drawLine(0f, bottom, width, bottom, mCommonDividerPaint)

            // top, bottom
            mTopShadow.setBounds(0, 0, mWidth, mItemH)
            mTopShadow.draw(canvas)

            mBottomShadow.setBounds(0, mHeight - mItemH, mWidth, mHeight)
            mBottomShadow.draw(canvas)

            // left,right
            canvas.drawLine(0f, 0f, 0f, height, mCommonBorderPaint)
            canvas.drawLine(width, 0f, width, height, mCommonBorderPaint)
        }
    }

    private companion object {
        // 阴影色值
        private val SHADOWS_COLORS = intArrayOf(
            0xFF111111.toInt(),
            0x00AAAAAA,
            0x00AAAAAA,
        )
    }
}
