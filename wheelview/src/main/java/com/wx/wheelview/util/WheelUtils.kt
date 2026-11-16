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
package com.wx.wheelview.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * 滚轮工具类
 *
 * @author venshine
 */
open class WheelUtils {

    companion object {

        private const val TAG = "WheelView"

        /**
         * 打印日志
         *
         * @param msg
         */
        @JvmStatic
        fun log(msg: String?) {
            if (!TextUtils.isEmpty(msg)) {
                Log.d(TAG, msg!!)
            }
        }

        /**
         * 判断集合是否为空
         *
         * @param c
         * @return
         */
        @JvmStatic
        fun <V> isEmpty(c: @JvmSuppressWildcards Collection<V>?): Boolean = c == null || c.isEmpty()

        /**
         * 遍历查找TextView
         *
         * @param view
         * @return
         */
        @JvmStatic
        fun findTextView(view: View?): TextView? {
            if (view is TextView) {
                return view
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val textView = findTextView(view.getChildAt(i))
                    if (textView != null) {
                        return textView
                    }
                }
            }
            return null
        }

        /**
         * dip转换到px
         *
         * @param context
         * @param dp
         * @return
         */
        @JvmStatic
        fun dip2px(context: Context, dp: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }

        /**
         * sp转换到px
         *
         * @param context
         * @param sp
         * @return
         */
        @JvmStatic
        fun sp2px(context: Context, sp: Float): Int {
            @Suppress("DEPRECATION")
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (sp * fontScale + 0.5f).toInt()
        }
    }
}
