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

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.wx.wheelview.widget.WheelView
import java.util.Calendar
import java.util.Locale

/**
 * 时间与单位：循环滚动、选中行附加文本、选中项背景、setSelection 快捷定位
 *
 * @author venshine
 */
class TimeActivity : BaseActivity() {

    private lateinit var wheels: List<WheelView<String>>
    private lateinit var style: WheelView.WheelViewStyle
    private val presetViews = mutableListOf<TextView>()
    private var loop = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)
        setToolbar(R.string.demo_time, "setExtraText()")

        // Holo 皮肤去掉分割线、只留选中项背景，三列连成一条高亮带
        style = DemoWheels.style(this, colorOf(R.color.accent_ink), textSize = 20).apply {
            holoBorderColor = Color.TRANSPARENT
            selectedBackgroundColor = colorOf(R.color.accent_soft)
        }
        val hours = (0 until 24).map(::pad)
        val sixty = (0 until 60).map(::pad)
        wheels = listOf(R.id.hour_wheel, R.id.minute_wheel, R.id.second_wheel).map { findViewById(it) }
        val data = listOf(hours, sixty, sixty)
        val units = listOf("时", "分", "秒")
        wheels.forEachIndexed { i, wheel ->
            DemoWheels.setup(wheel, DemoWheelAdapter(this, numeric = true), data[i], style,
                loop = loop, selection = INITIAL[i])
            wheel.setUnit(units[i], 22f)
            wheel.setOnWheelItemSelectedListener { _, _ -> render() }
        }

        initPresets()
        initSwitches()
        render()
    }

    private fun initPresets() {
        val row = findViewById<LinearLayout>(R.id.preset_row)
        val textColors = ContextCompat.getColorStateList(this, R.color.preset_text)
        PRESETS.forEachIndexed { i, preset ->
            val view = TextView(this).apply {
                text = "${pad(preset[0])}:${pad(preset[1])}"
                gravity = Gravity.CENTER
                textSize = 15f
                typeface = font(R.font.space_grotesk)
                setTextColor(textColors)
                setBackgroundResource(R.drawable.bg_preset)
                setOnClickListener { wheels.forEachIndexed { index, wheel -> wheel.setSelection(preset[index]) } }
            }
            row.addView(view, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                if (i > 0) marginStart = dp(8f)
            })
            presetViews += view
        }
    }

    private fun initSwitches() {
        findViewById<SwitchCompat>(R.id.loop_switch).apply {
            text = switchLabel(getString(R.string.time_loop), getString(R.string.time_loop_api))
            isChecked = loop
            setOnCheckedChangeListener { _, checked ->
                loop = checked
                wheels.forEach { wheel ->
                    // setLoop 会把位置重置到 0，切换后恢复原来的选中项
                    val current = wheel.currentPosition.coerceAtLeast(0)
                    wheel.setLoop(checked)
                    wheel.setSelection(current)
                }
                render()
            }
        }
        findViewById<SwitchCompat>(R.id.bold_switch).apply {
            text = switchLabel(getString(R.string.time_bold), getString(R.string.time_bold_api))
            isChecked = style.selectedTextBold
            setOnCheckedChangeListener { _, checked ->
                style.selectedTextBold = checked
                wheels.forEach { it.style = style }
            }
        }
    }

    private fun switchLabel(label: String, api: String): CharSequence {
        val builder = SpannableStringBuilder(label).append('\n')
        val start = builder.length
        builder.append(api)
        val end = builder.length
        builder.setSpan(RelativeSizeSpan(0.75f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(colorOf(R.color.text_dim)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        font(R.font.jetbrains_mono)?.let { builder.setSpan(FontSpan(it), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        return builder
    }

    private fun render() {
        findViewById<TextView>(R.id.code_text).text = CodeHighlighter.highlight(this,
            "hourWheel.setLoop($loop)\nhourWheel.setExtraText(\"时\", color, textSizePx, marginPx)\n" +
                "style.selectedBackgroundColor = accentSoft",
            listOf("setLoop", "setExtraText", "selectedBackgroundColor"))

        val time = wheels.map { it.currentPosition }
        if (time.any { it < 0 }) return
        presetViews.forEachIndexed { i, view -> view.isSelected = PRESETS[i].toList() == time }

        val colon = colorOf(R.color.accent_ink)
        findViewById<TextView>(R.id.time_text).text = SpannableStringBuilder()
            .append(pad(time[0])).appendColored(":", colon)
            .append(pad(time[1])).appendColored(":", colon)
            .append(pad(time[2]))

        val now = Calendar.getInstance()
        var diff = time[0] * 60 + time[1] - (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE))
        if (diff <= 0) diff += 24 * 60
        findViewById<TextView>(R.id.time_hint).text = getString(R.string.time_hint_format, diff / 60, diff % 60)
    }

    private companion object {
        val INITIAL = intArrayOf(7, 30, 0)
        val PRESETS = listOf(intArrayOf(6, 30, 0), intArrayOf(7, 30, 0), intArrayOf(12, 0, 0), intArrayOf(22, 45, 0))

        fun pad(value: Int) = String.format(Locale.US, "%02d", value)
    }
}
