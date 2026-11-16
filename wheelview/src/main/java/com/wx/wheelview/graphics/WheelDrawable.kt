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
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.wx.wheelview.common.WheelConstants
import com.wx.wheelview.widget.WheelView

/**
 * 滚轮样式
 *
 * @author venshine
 */
open class WheelDrawable internal constructor(
    @JvmField protected val mWidth: Int, // 控件宽
    @JvmField protected val mHeight: Int, // 控件高
    @JvmField protected val mStyle: WheelView.WheelViewStyle,
) : Drawable() {

    private val mBgPaint = Paint().apply {
        color = mStyle.backgroundColor ?: WheelConstants.WHEEL_BG
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mBgPaint)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.UNKNOWN
}
