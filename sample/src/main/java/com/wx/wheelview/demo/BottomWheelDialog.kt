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
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.wx.wheelview.widget.WheelView

/**
 * 贴底弹出的滚轮选择框：普通 Dialog + WheelView，不依赖 Material Components
 *
 * @author venshine
 */
class BottomWheelDialog(
    context: Context,
    private val title: String,
    private val subtitle: String,
    private val items: List<String>,
    private val unit: String,
    private val unitOffsetDp: Float,
    private val loop: Boolean,
    private val selection: Int,
    private val onConfirm: (Int) -> Unit,
) : AppCompatDialog(context, R.style.BottomWheelDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_bottom_wheel)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setDimAmount(if (DemoTheme.isNight(context)) 0.64f else 0.42f)
            // 窗口铺满全屏并延伸到导航栏下方，面板底色铺到屏幕底边，不露出一截遮罩
            WindowCompat.setDecorFitsSystemWindows(this, false)
            navigationBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(this, decorView).isAppearanceLightNavigationBars = !DemoTheme.isNight(context)
        }

        // 窗口是全屏的，「点外部关闭」由空白区域自己处理
        requireNotNull(findViewById<View>(R.id.dialog_root)).setOnClickListener { cancel() }
        requireNotNull(findViewById<TextView>(R.id.sheet_title)).text = title
        requireNotNull(findViewById<TextView>(R.id.sheet_subtitle)).text = subtitle

        val wheel = requireNotNull(findViewById<WheelView<String>>(R.id.sheet_wheel))
        val numeric = items.first().first().isDigit()
        DemoWheels.setup(wheel, DemoWheelAdapter(context, 48f, numeric), items, DemoWheels.style(context, textSize = 20),
            loop = loop, selection = selection)
        if (unit.isNotEmpty()) wheel.setUnit(unit, unitOffsetDp)

        requireNotNull(findViewById<View>(R.id.sheet_cancel)).setOnClickListener { dismiss() }
        requireNotNull(findViewById<View>(R.id.sheet_confirm)).setOnClickListener {
            onConfirm(wheel.currentPosition.coerceAtLeast(0))
            dismiss()
        }

        // 底部留出导航栏高度，按钮不被手势条挡住
        val sheet = requireNotNull(findViewById<View>(R.id.sheet_root))
        val basePadding = sheet.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(sheet) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, basePadding + bottom)
            insets
        }
    }
}
