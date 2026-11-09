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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.wx.wheelview.widget.WheelView
import java.util.Calendar
import java.util.Locale

/**
 * 首页：年月日实时预览 + 示例入口 + 接入方式
 *
 * @author venshine
 */
class MainActivity : BaseActivity() {

    private lateinit var yearWheel: WheelView<String>
    private lateinit var monthWheel: WheelView<String>
    private lateinit var dayWheel: WheelView<String>

    /** 示例 App 与库同版本发布，版本号只在 sample/build.gradle 的 versionName 维护一处 */
    private val version: String by lazy {
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.source_button).setOnClickListener { openSource() }
        findViewById<TextView>(R.id.version_chip).text = "v$version"
        initDateWheels()
        initCards()
        initInstall()
    }

    /**
     * 年 / 月 / 日：Holo 皮肤 + 附加文本，月和日循环滚动；切换年月时按天数重设「日」的数据
     */
    private fun initDateWheels() {
        val today = Calendar.getInstance()
        val style = DemoWheels.style(this)
        val years = (FIRST_YEAR..LAST_YEAR).map { it.toString() }
        val yearIndex = (today.get(Calendar.YEAR) - FIRST_YEAR).coerceIn(0, years.lastIndex)
        val monthIndex = today.get(Calendar.MONTH)

        yearWheel = findViewById(R.id.year_wheel)
        DemoWheels.setup(yearWheel, DemoWheelAdapter(this, numeric = true), years, style, selection = yearIndex)
        yearWheel.setUnit("年", 32f)

        monthWheel = findViewById(R.id.month_wheel)
        DemoWheels.setup(monthWheel, DemoWheelAdapter(this, numeric = true), (1..12).map(::pad), style,
            loop = true, selection = monthIndex)
        monthWheel.setUnit("月", 20f)

        dayWheel = findViewById(R.id.day_wheel)
        DemoWheels.setup(dayWheel, DemoWheelAdapter(this, numeric = true),
            days(daysIn(FIRST_YEAR + yearIndex, monthIndex + 1)), style,
            loop = true, selection = today.get(Calendar.DAY_OF_MONTH) - 1)
        dayWheel.setUnit("日", 20f)

        yearWheel.setOnWheelItemSelectedListener { _, _ -> onMonthChanged() }
        monthWheel.setOnWheelItemSelectedListener { _, _ -> onMonthChanged() }
        dayWheel.setOnWheelItemSelectedListener { _, _ -> updateDateText() }
    }

    private fun onMonthChanged() {
        val year = FIRST_YEAR + yearWheel.currentPosition
        val month = monthWheel.currentPosition + 1
        if (yearWheel.currentPosition < 0 || month < 1) return
        val count = daysIn(year, month)
        if (count != dayWheel.wheelCount) {
            val keep = dayWheel.currentPosition.coerceIn(0, count - 1)
            // 与库内联动的 resetDataFromTop 一致：换数据后再定位
            dayWheel.post {
                dayWheel.setWheelData(days(count))
                dayWheel.setSelection(keep)
            }
        }
        updateDateText()
    }

    private fun updateDateText() {
        if (yearWheel.currentPosition < 0 || monthWheel.currentPosition < 0 || dayWheel.currentPosition < 0) return
        val year = FIRST_YEAR + yearWheel.currentPosition
        val month = monthWheel.currentPosition + 1
        val day = (dayWheel.currentPosition + 1).coerceAtMost(daysIn(year, month))
        findViewById<TextView>(R.id.date_text).text = "$year.${pad(month)}.${pad(day)}"
        val calendar = Calendar.getInstance().apply { set(year, month - 1, day) }
        findViewById<TextView>(R.id.weekday_text).text =
            getString(R.string.weekday_format, WEEKDAYS[calendar.get(Calendar.DAY_OF_WEEK) - 1])
    }

    private fun initCards() {
        findViewById<View>(R.id.card_styles).setOnClickListener { open(StyleLabActivity::class.java) }
        bindCard(R.id.card_linkage, R.drawable.ic_link, R.string.demo_linkage, R.string.demo_linkage_desc,
            LinkageActivity::class.java)
        bindCard(R.id.card_time, R.drawable.ic_clock, R.string.demo_time, R.string.demo_time_desc,
            TimeActivity::class.java)
        bindCard(R.id.card_icon_text, R.drawable.ic_image_text, R.string.demo_icon_text, R.string.demo_icon_text_desc,
            IconTextActivity::class.java)
        bindCard(R.id.card_dialog, R.drawable.ic_sheet, R.string.demo_dialog, R.string.demo_dialog_desc,
            DialogActivity::class.java)
    }

    private fun bindCard(@IdRes id: Int, @DrawableRes icon: Int, @StringRes title: Int, @StringRes desc: Int,
                         target: Class<out Activity>) {
        val card = findViewById<View>(id)
        card.findViewById<ImageView>(R.id.card_icon).setImageResource(icon)
        card.findViewById<TextView>(R.id.card_title).setText(title)
        card.findViewById<TextView>(R.id.card_desc).setText(desc)
        card.setOnClickListener { open(target) }
    }

    private fun initInstall() {
        val dependency = "com.github.venshine:WheelView:$version"
        val code = "// settings.gradle\nmaven { url = 'https://jitpack.io' }\n\n// build.gradle\nimplementation '$dependency'"
        findViewById<TextView>(R.id.install_code).text =
            CodeHighlighter.highlight(this, code, listOf("implementation", "maven"))
        findViewById<TextView>(R.id.copy_dependency).apply {
            setStartIcon(tintedIcon(R.drawable.ic_copy, colorOf(R.color.text), 16f))
            setOnClickListener {
                copyToClipboard("implementation '$dependency'")
                flashCopied(R.string.home_copied_dependency, R.string.home_copy_dependency)
            }
        }
    }

    private fun open(target: Class<out Activity>) = startActivity(Intent(this, target))

    private fun openSource() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_URL)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, SOURCE_URL, Toast.LENGTH_LONG).show()
        }
    }

    private companion object {
        const val FIRST_YEAR = 2020
        const val LAST_YEAR = 2035
        const val SOURCE_URL = "https://github.com/venshine/WheelView"
        val WEEKDAYS = arrayOf("日", "一", "二", "三", "四", "五", "六")

        fun pad(value: Int) = String.format(Locale.US, "%02d", value)

        fun days(count: Int) = (1..count).map(::pad)

        fun daysIn(year: Int, month: Int): Int =
            Calendar.getInstance().apply { set(year, month - 1, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
