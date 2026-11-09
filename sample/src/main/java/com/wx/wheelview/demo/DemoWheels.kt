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
package com.wx.wheelview.demo

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.TextView
import androidx.annotation.ColorInt
import com.wx.wheelview.adapter.BaseWheelAdapter
import com.wx.wheelview.widget.WheelView

/**
 * 示例统一的滚轮外观
 *
 * 颜色取自 values / values-night，切换深浅色后 Activity 重建即生效。
 *
 * @author venshine
 */
object DemoWheels {

    /** 每个滚轮显示 5 行 */
    const val WHEEL_SIZE = 5

    fun style(
        context: Context,
        @ColorInt selectedColor: Int = context.colorOf(R.color.wheel_selected),
        textSize: Int = 18,
    ) = WheelView.WheelViewStyle().apply {
        backgroundColor = context.colorOf(R.color.surface)
        textColor = context.colorOf(R.color.wheel_text)
        selectedTextColor = selectedColor
        holoBorderColor = context.colorOf(R.color.accent)
        holoBorderWidth = context.dp(1.5f)
        this.textSize = textSize
        selectedTextZoom = 1.15f
        textAlpha = 0.55f
        textScale = 0.9f
        selectedTextBold = true
    }

    fun <T> setup(
        wheel: WheelView<T>,
        adapter: BaseWheelAdapter<T>,
        data: List<T>,
        style: WheelView.WheelViewStyle,
        skin: WheelView.Skin = WheelView.Skin.Holo,
        loop: Boolean = false,
        selection: Int = 0,
    ) {
        wheel.setWheelAdapter(adapter)
        wheel.setWheelSize(WHEEL_SIZE)
        wheel.skin = skin
        wheel.setWheelData(data)
        wheel.setLoop(loop)
        wheel.style = style
        wheel.setSelection(selection)
    }
}

/** 选中行右侧的单位文字，例如「年」「cm」 */
fun WheelView<*>.setUnit(unit: String, offsetDp: Float) {
    setExtraText(unit, context.colorOf(R.color.accent_ink), context.sp(13f), context.dp(offsetDp))
}

/**
 * 自定义条目布局的适配器
 *
 * 继承 BaseWheelAdapter 即可自定义条目：WheelView 会找到条目里的第一个 TextView 并套用样式。
 * 这里用它控制行高，并让数字使用 Space Grotesk 字体。
 */
class DemoWheelAdapter(
    private val context: Context,
    private val itemHeightDp: Float = 46f,
    numeric: Boolean = false,
) : BaseWheelAdapter<String>() {

    private val typeface = if (numeric) context.font(R.font.space_grotesk) else null

    override fun bindView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = convertView as? TextView ?: TextView(context).apply {
            layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dp(itemHeightDp))
            gravity = Gravity.CENTER
            isSingleLine = true
            includeFontPadding = false
            typeface?.let { this.typeface = it }
        }
        textView.text = mList[position]
        return textView
    }
}
