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
import com.wx.wheelview.common.WheelConstants
import com.wx.wheelview.widget.WheelView

/**
 * holo滚轮样式
 *
 * @author venshine
 */
open class HoloDrawable internal constructor(
    width: Int,
    height: Int,
    style: WheelView.WheelViewStyle,
    private val mWheelSize: Int,
    private val mItemH: Int,
) : WheelDrawable(width, height, style) {

    private val mHoloBgPaint = Paint().apply {
        color = mStyle.backgroundColor ?: WheelConstants.WHEEL_SKIN_HOLO_BG
    }

    private val mHoloPaint = Paint().apply {
        strokeWidth = (if (mStyle.holoBorderWidth != -1) mStyle.holoBorderWidth else 3).toFloat()
        color = mStyle.holoBorderColor ?: WheelConstants.WHEEL_SKIN_HOLO_BORDER_COLOR
    }

    private val mHoloSelectedPaint: Paint? = mStyle.selectedBackgroundColor?.let { Paint().apply { color = it } }

    override fun draw(canvas: Canvas) {
        val width = mWidth.toFloat()
        // draw background
        canvas.drawRect(0f, 0f, width, mHeight.toFloat(), mHoloBgPaint)

        // draw select border
        if (mItemH != 0) {
            val size = mWheelSize shr 1
            val top = (mItemH * size).toFloat()
            val bottom = (mItemH * (size + 1)).toFloat()
            mHoloSelectedPaint?.let { canvas.drawRect(0f, top, width, bottom, it) }
            canvas.drawLine(0f, top, width, top, mHoloPaint)
            canvas.drawLine(0f, bottom, width, bottom, mHoloPaint)
        }
    }
}
