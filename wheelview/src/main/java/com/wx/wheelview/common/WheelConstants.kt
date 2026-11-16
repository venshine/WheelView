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
package com.wx.wheelview.common

import android.graphics.Color

/**
 * 滚轮常量类
 *
 * 常量放在 companion object 里并标 const / @JvmField，Java 中仍是 WheelConstants 的静态字段；
 * 保持 open 和公开无参构造，与原 Java 类一致
 *
 * @author venshine
 */
open class WheelConstants {

    companion object {
        /**
         * 滚轮tag
         */
        const val TAG = "com.wx.wheelview"

        /**
         * 滚轮每一项的内边距
         */
        const val WHEEL_ITEM_PADDING = 20

        /**
         * 滚轮每一项的内部控件外边距
         */
        const val WHEEL_ITEM_MARGIN = 20

        /**
         * 滚轮每一项的图片TAG
         */
        const val WHEEL_ITEM_IMAGE_TAG = 100

        /**
         * 滚轮每一项的文本TAG
         */
        const val WHEEL_ITEM_TEXT_TAG = 101

        /**
         * 平滑滚动持续时间
         */
        const val WHEEL_SMOOTH_SCROLL_DURATION = 50

        /**
         * 默认背景
         */
        const val WHEEL_BG = Color.WHITE

        /**
         * common皮肤默认背景
         */
        @JvmField
        val WHEEL_SKIN_COMMON_BG = Color.parseColor("#dddddd")

        /**
         * holo皮肤默认背景
         */
        const val WHEEL_SKIN_HOLO_BG = Color.WHITE

        /**
         * holo皮肤默认选中框颜色
         */
        @JvmField
        val WHEEL_SKIN_HOLO_BORDER_COLOR = Color.parseColor("#83cde6")

        /**
         * 滚轮item高度
         */
        const val WHEEL_ITEM_HEIGHT = 45

        /**
         * 滚轮默认文本颜色
         */
        const val WHEEL_TEXT_COLOR = Color.BLACK

        /**
         * 滚轮默认文本大小
         */
        const val WHEEL_TEXT_SIZE = 16

        /**
         * 滚轮默认文本透明度
         */
        const val WHEEL_TEXT_ALPHA = 0.7f

        /**
         * common皮肤默认选中框颜色
         */
        @JvmField
        val WHEEL_SKIN_COMMON_COLOR = Color.parseColor("#f0cfcfcf")

        /**
         * common皮肤选中框divider颜色
         */
        @JvmField
        val WHEEL_SKIN_COMMON_DIVIDER_COLOR = Color.parseColor("#b5b5b5")

        /**
         * common皮肤左右边框颜色
         */
        @JvmField
        val WHEEL_SKIN_COMMON_BORDER_COLOR = Color.parseColor("#666666")

        /**
         * 滚轮滑动时展示选中项
         */
        const val WHEEL_SCROLL_HANDLER_WHAT = 0x0100

        /**
         * 滚轮滑动时展示选中项延迟时间
         */
        const val WHEEL_SCROLL_DELAY_DURATION = 300

        /**
         * 鼠标滚轮停止滚动后对齐选中项的延迟时间
         */
        const val WHEEL_ADJUST_DELAY_DURATION = 100

        /**
         * dialog默认颜色
         */
        @JvmField
        val DIALOG_WHEEL_COLOR = WHEEL_SKIN_HOLO_BORDER_COLOR
    }
}
