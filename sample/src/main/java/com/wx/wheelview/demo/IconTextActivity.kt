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

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.wx.wheelview.adapter.SimpleWheelAdapter
import com.wx.wheelview.common.WheelData
import com.wx.wheelview.widget.WheelView
import java.util.Locale

/**
 * 图文混排：SimpleWheelAdapter（图标 + 文本）、循环滚动、选中项点击，并实时打印两个监听器的回调
 *
 * @author venshine
 */
class IconTextActivity : BaseActivity() {

    private class Tag(val name: String, @ColorInt val color: Int, @DrawableRes val dot: Int)

    private val log = ArrayDeque<CharSequence>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_text)
        setToolbar(R.string.demo_icon_text, "SimpleWheelAdapter")

        val wheel = findViewById<WheelView<WheelData>>(R.id.color_wheel)
        DemoWheels.setup(wheel, SimpleWheelAdapter(this), TAGS.map { WheelData(it.dot, it.name) },
            DemoWheels.style(this, colorOf(R.color.text)), skin = WheelView.Skin.None, loop = true,
            selection = INITIAL)
        // 选中项可点击，点击时回调 OnWheelItemClickListener
        wheel.setWheelClickable(true)
        wheel.setOnWheelItemSelectedListener { position, _ ->
            showTag(position)
            addLog("onWheelItemSelected", position, colorOf(R.color.code_string))
        }
        wheel.setOnWheelItemClickListener { position, _ ->
            addLog("onWheelItemClick", position, colorOf(R.color.accent_ink))
        }

        findViewById<View>(R.id.clear_log).setOnClickListener {
            log.clear()
            renderLog()
        }
        findViewById<TextView>(R.id.code_text).text = CodeHighlighter.highlight(this,
            "wheel.setWheelAdapter(SimpleWheelAdapter(this))\n" +
                "wheel.setWheelData(tags.map { WheelData(it.icon, it.name) })\n" +
                "wheel.setWheelClickable(true)\n" +
                "wheel.setOnWheelItemClickListener { position, data -> }",
            listOf("SimpleWheelAdapter", "WheelData", "setWheelClickable", "setOnWheelItemClickListener"))
        showTag(INITIAL)
    }

    private fun showTag(position: Int) {
        val tag = TAGS[position]
        findViewById<TextView>(R.id.tag_chip).apply {
            text = tag.name
            background = GradientDrawable().apply {
                cornerRadius = dp(13f).toFloat()
                setColor(tag.color.withAlpha(0x2E))
            }
            setStartIcon(GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(tag.color)
                val size = dp(8f)
                setSize(size, size)
                setBounds(0, 0, size, size)
            })
        }
        findViewById<TextView>(R.id.tag_hex).text = String.format(Locale.US, "#%06X", tag.color and 0xFFFFFF)
    }

    private fun addLog(name: String, position: Int, @ColorInt nameColor: Int) {
        val tag = TAGS[position]
        log.addFirst(SpannableStringBuilder()
            .appendColored("●  ", tag.color)
            .appendColored(name, nameColor)
            .appendColored("($position, \"${tag.name}\")", colorOf(R.color.text_sub)))
        while (log.size > MAX_LOG) log.removeLast()
        renderLog()
    }

    private fun renderLog() {
        findViewById<View>(R.id.log_empty).visibility = if (log.isEmpty()) View.VISIBLE else View.GONE
        val container = findViewById<LinearLayout>(R.id.log_container)
        container.removeAllViews()
        log.forEachIndexed { i, entry ->
            container.addView(TextView(this).apply {
                text = entry
                textSize = 11.5f
                typeface = font(R.font.jetbrains_mono)
                alpha = 1f - i * 0.15f
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8f) })
        }
    }

    private companion object {
        const val INITIAL = 3
        const val MAX_LOG = 5
        val TAGS = listOf(
            Tag("珊瑚", 0xFFFF6B5A.toInt(), R.drawable.dot_coral),
            Tag("琥珀", 0xFFF5A524.toInt(), R.drawable.dot_amber),
            Tag("柠檬", 0xFFE8D44D.toInt(), R.drawable.dot_lemon),
            Tag("薄荷", 0xFF3DD6A0.toInt(), R.drawable.dot_mint),
            Tag("湖蓝", 0xFF3BB5E8.toInt(), R.drawable.dot_lake),
            Tag("钴蓝", 0xFF5B7CFA.toInt(), R.drawable.dot_cobalt),
            Tag("鸢尾", 0xFF9A7BFF.toInt(), R.drawable.dot_iris),
            Tag("樱粉", 0xFFFF8FB1.toInt(), R.drawable.dot_sakura),
        )
    }
}
