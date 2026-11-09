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

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.wx.wheelview.widget.WheelView
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 样式实验室：切换三种皮肤、调节 WheelViewStyle，实时预览并生成对应的 Kotlin 代码
 *
 * @author venshine
 */
class StyleLabActivity : BaseActivity() {

    private class Swatch(val name: String, @ColorInt val color: Int)

    /** 一个滑块对应 WheelViewStyle 的一个浮点字段 */
    private class Slider(@StringRes val label: Int, val api: String, val min: Float, val max: Float) {
        lateinit var seekBar: SeekBar
        lateinit var valueText: TextView

        /** 每档 0.01，默认值（如 0.55）能精确落在某一档上，不会被截断成 0.54 */
        val steps = ((max - min) * STEPS_PER_UNIT).roundToInt()
        fun valueOf(progress: Int) = min + progress / STEPS_PER_UNIT
        fun progressOf(value: Float) = ((value - min) * STEPS_PER_UNIT).roundToInt()
    }

    private lateinit var wheel: WheelView<String>
    private lateinit var swatches: List<Swatch>
    private val swatchViews = mutableListOf<View>()
    private val textScale = Slider(R.string.lab_text_scale, "textScale", 0.6f, 1f)
    private val textAlpha = Slider(R.string.lab_text_alpha, "textAlpha", 0.2f, 1f)
    private val zoom = Slider(R.string.lab_zoom, "selectedTextZoom", 1f, 1.6f)

    private var skin = WheelView.Skin.Holo
    private var scaleValue = DEFAULT_SCALE
    private var alphaValue = DEFAULT_ALPHA
    private var zoomValue = DEFAULT_ZOOM
    private var bold = true
    private var loop = false
    private var swatchIndex = 0
    private var code = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style_lab)
        // 顶栏右侧的标签位放「重置」
        setToolbar(R.string.demo_styles, getString(R.string.lab_reset))
        findViewById<TextView>(R.id.toolbar_chip).setOnClickListener { reset() }

        // 深色下用亮色，浅色下用加深色，保证选中文字的对比度
        swatches = if (DemoTheme.isNight(this)) listOf(
            Swatch("强调色", colorOf(R.color.accent_ink)), Swatch("青绿", 0xFF4FD1C5.toInt()),
            Swatch("雾蓝", 0xFF8B9CFF.toInt()), Swatch("象牙白", 0xFFF6F4EE.toInt()),
        ) else listOf(
            Swatch("强调色", colorOf(R.color.accent_ink)), Swatch("青绿", 0xFF0F766E.toInt()),
            Swatch("靛蓝", 0xFF4F46E5.toInt()), Swatch("墨黑", 0xFF16181D.toInt()),
        )

        wheel = findViewById(R.id.preview_wheel)
        DemoWheels.setup(wheel, DemoWheelAdapter(this, 48f), PLANETS, buildStyle(), selection = 2)

        initSkins()
        initSliders()
        initSwatches()
        initToggles()
        findViewById<TextView>(R.id.copy_code).setOnClickListener {
            copyToClipboard(code)
            (it as TextView).flashCopied(R.string.copied, R.string.lab_copy_code)
        }
        render()
    }

    private fun initSkins() {
        mapOf(R.id.skin_common to WheelView.Skin.Common, R.id.skin_holo to WheelView.Skin.Holo,
            R.id.skin_none to WheelView.Skin.None).forEach { (id, value) ->
            findViewById<View>(id).setOnClickListener {
                skin = value
                render()
            }
        }
    }

    private fun initSliders() {
        val container = findViewById<LinearLayout>(R.id.slider_container)
        val tint = ColorStateList.valueOf(colorOf(R.color.accent_ink))
        listOf(textScale, textAlpha, zoom).forEach { slider ->
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            header.addView(TextView(this).apply {
                text = getString(slider.label)
                textSize = 13f
                setTextColor(colorOf(R.color.text))
            })
            header.addView(TextView(this).apply {
                text = slider.api
                textSize = 11f
                typeface = font(R.font.jetbrains_mono)
                setTextColor(colorOf(R.color.text_dim))
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(6f) })
            slider.valueText = TextView(this).apply {
                textSize = 13f
                typeface = font(R.font.jetbrains_mono)
                setTextColor(colorOf(R.color.accent_ink))
            }
            header.addView(slider.valueText)
            container.addView(header, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18f) })

            slider.seekBar = SeekBar(this).apply {
                max = slider.steps
                progressTintList = tint
                thumbTintList = tint
                contentDescription = getString(slider.label)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        val value = slider.valueOf(progress)
                        when (slider) {
                            textScale -> scaleValue = value
                            textAlpha -> alphaValue = value
                            else -> zoomValue = value
                        }
                        render()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
            container.addView(slider.seekBar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                dp(36f)).apply { topMargin = dp(4f) })
        }
        syncSliders()
    }

    private fun syncSliders() {
        textScale.seekBar.progress = textScale.progressOf(scaleValue)
        textAlpha.seekBar.progress = textAlpha.progressOf(alphaValue)
        zoom.seekBar.progress = zoom.progressOf(zoomValue)
    }

    private fun initSwatches() {
        val row = findViewById<LinearLayout>(R.id.swatch_row)
        swatches.forEachIndexed { i, swatch ->
            val view = View(this).apply {
                contentDescription = swatch.name
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    swatchIndex = i
                    render()
                }
            }
            row.addView(view, LinearLayout.LayoutParams(dp(44f), dp(44f)).apply { if (i > 0) marginStart = dp(12f) })
            swatchViews += view
        }
    }

    private fun initToggles() {
        findViewById<View>(R.id.toggle_bold).setOnClickListener {
            bold = !bold
            render()
        }
        findViewById<View>(R.id.toggle_loop).setOnClickListener {
            setLoop(!loop)
            render()
        }
    }

    private fun setLoop(value: Boolean) {
        if (value == loop) return
        loop = value
        // setLoop 会把位置重置到 0，切换后恢复原来的选中项
        val current = wheel.currentPosition.coerceAtLeast(0)
        wheel.setLoop(value)
        wheel.setSelection(current)
    }

    private fun reset() {
        skin = WheelView.Skin.Holo
        scaleValue = DEFAULT_SCALE
        alphaValue = DEFAULT_ALPHA
        zoomValue = DEFAULT_ZOOM
        bold = true
        swatchIndex = 0
        setLoop(false)
        syncSliders()
        render()
    }

    private fun buildStyle(): WheelView.WheelViewStyle {
        val selected = swatches[swatchIndex].color
        return WheelView.WheelViewStyle().apply {
            backgroundColor = colorOf(R.color.surface)
            textColor = colorOf(R.color.wheel_text)
            selectedTextColor = selected
            holoBorderColor = selected
            holoBorderWidth = dp(1.5f)
            // Common 皮肤默认的选中背景是灰色，跟随选中色更协调
            selectedBackgroundColor = if (skin == WheelView.Skin.Common) selected.withAlpha(0x1F) else null
            textSize = 20
            textScale = scaleValue
            textAlpha = alphaValue
            selectedTextZoom = zoomValue
            selectedTextBold = bold
        }
    }

    private fun render() {
        wheel.skin = skin
        wheel.style = buildStyle()

        findViewById<TextView>(R.id.skin_label).text = "Skin.${skin.name}"
        findViewById<View>(R.id.skin_common).isSelected = skin == WheelView.Skin.Common
        findViewById<View>(R.id.skin_holo).isSelected = skin == WheelView.Skin.Holo
        findViewById<View>(R.id.skin_none).isSelected = skin == WheelView.Skin.None
        textScale.valueText.text = format(scaleValue)
        textAlpha.valueText.text = format(alphaValue)
        zoom.valueText.text = format(zoomValue)
        swatchViews.forEachIndexed { i, view -> view.background = swatchDrawable(swatches[i].color, i == swatchIndex) }
        bindToggle(R.id.toggle_bold, R.id.toggle_bold_label, bold)
        bindToggle(R.id.toggle_loop, R.id.toggle_loop_label, loop)

        val colorHex = String.format(Locale.US, "#%06X", swatches[swatchIndex].color and 0xFFFFFF)
        code = """
            |wheel.skin = WheelView.Skin.${skin.name}
            |wheel.setLoop($loop)
            |wheel.style = WheelView.WheelViewStyle().apply {
            |    textScale = ${format(scaleValue)}f
            |    textAlpha = ${format(alphaValue)}f
            |    selectedTextZoom = ${format(zoomValue)}f
            |    selectedTextBold = $bold
            |    selectedTextColor = Color.parseColor("$colorHex")
            |}
        """.trimMargin()
        findViewById<TextView>(R.id.code_text).text =
            CodeHighlighter.highlight(this, code, listOf("WheelViewStyle", "apply", "setLoop", "Skin"))
    }

    /** 选中态设在容器上，文字通过 duplicateParentState 跟随变色；对勾挂在居中的文字上，紧挨文字 */
    private fun bindToggle(id: Int, labelId: Int, on: Boolean) {
        findViewById<View>(id).isSelected = on
        findViewById<TextView>(labelId).setStartIcon(
            if (on) tintedIcon(R.drawable.ic_check, colorOf(R.color.accent_ink), 16f) else null)
    }

    /** 外圈描边表示选中，内圈是颜色本身 */
    private fun swatchDrawable(@ColorInt color: Int, selected: Boolean): Drawable {
        val ring = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(dp(2f), if (selected) color else Color.TRANSPARENT)
        }
        val dot = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        return LayerDrawable(arrayOf(ring, dot)).apply {
            val inset = dp(6f)
            setLayerInset(1, inset, inset, inset, inset)
        }
    }

    private fun format(value: Float) = String.format(Locale.US, "%.2f", value)

    private companion object {
        const val STEPS_PER_UNIT = 100f
        const val DEFAULT_SCALE = 0.9f
        const val DEFAULT_ALPHA = 0.55f
        const val DEFAULT_ZOOM = 1.15f
        val PLANETS = listOf("水星", "金星", "地球", "火星", "木星", "土星", "天王星", "海王星")
    }
}
