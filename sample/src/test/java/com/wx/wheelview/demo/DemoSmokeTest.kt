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
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.wx.wheelview.widget.WheelView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import java.time.Duration
import java.util.Calendar
import java.util.Locale

/**
 * 示例页冒烟测试：每个页面在浅色、深色下都能启动，关键交互的结果符合预期
 *
 * @author venshine
 */
@RunWith(RobolectricTestRunner::class)
class DemoSmokeTest {

    @After
    fun tearDown() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /** 等待布局完成和滚轮 300ms 的选中回调 */
    private fun idle() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
    }

    private fun <T : Activity> launch(type: Class<T>): T {
        val activity = Robolectric.buildActivity(type).setup().get()
        idle()
        return activity
    }

    private val screens = listOf(
        MainActivity::class.java, LinkageActivity::class.java, TimeActivity::class.java,
        StyleLabActivity::class.java, IconTextActivity::class.java, DialogActivity::class.java,
    )

    @Test
    fun allScreensLaunchInLightMode() {
        screens.forEach { type ->
            val activity = launch(type)
            assertTrue("${type.simpleName} 应为浅色", !DemoTheme.isNight(activity))
        }
    }

    @Test
    fun allScreensLaunchInDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        screens.forEach { type ->
            val activity = launch(type)
            assertTrue("${type.simpleName} 应为深色", DemoTheme.isNight(activity))
        }
    }

    @Test
    fun themeToggleSwitchesToDark() {
        val activity = launch(MainActivity::class.java)
        activity.findViewById<View>(R.id.theme_toggle).performClick()
        idle()
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun homeShowsToday() {
        val activity = launch(MainActivity::class.java)
        val today = Calendar.getInstance()
        val expected = String.format(Locale.US, "%d.%02d.%02d", today.get(Calendar.YEAR),
            today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH))
        assertEquals(expected, activity.findViewById<TextView>(R.id.date_text).text.toString())
    }

    @Test
    fun linkageKeepsInitialSelection() {
        // 主滚轮首次回调时副滚轮数据不变，应保留初始的「深圳」而不是回到第一项
        val activity = launch(LinkageActivity::class.java)
        assertEquals("广东  ›  深圳  ›  南山区", activity.findViewById<TextView>(R.id.result_text).text.toString())
    }

    @Test
    fun timePresetMovesWheels() {
        val activity = launch(TimeActivity::class.java)
        assertEquals("07:30:00", activity.findViewById<TextView>(R.id.time_text).text.toString())
        val presets = activity.findViewById<android.view.ViewGroup>(R.id.preset_row)
        presets.getChildAt(3).performClick()
        idle()
        assertEquals("22:45:00", activity.findViewById<TextView>(R.id.time_text).text.toString())
    }

    @Test
    fun styleLabSwitchesSkinAndCode() {
        val activity = launch(StyleLabActivity::class.java)
        activity.findViewById<View>(R.id.skin_common).performClick()
        val wheel = activity.findViewById<WheelView<String>>(R.id.preview_wheel)
        assertEquals(WheelView.Skin.Common, wheel.skin)
        assertTrue(activity.findViewById<TextView>(R.id.code_text).text.contains("Skin.Common"))
    }

    @Test
    fun iconTextLogsSelection() {
        val activity = launch(IconTextActivity::class.java)
        val log = activity.findViewById<android.view.ViewGroup>(R.id.log_container)
        assertTrue(log.childCount >= 1)
        assertTrue((log.getChildAt(0) as TextView).text.contains("onWheelItemSelected(3, \"薄荷\")"))
    }

    @Test
    fun bottomDialogWritesBackValue() {
        val activity = launch(DialogActivity::class.java)
        val row = activity.findViewById<View>(R.id.row_height)
        assertEquals("175 cm", row.findViewById<TextView>(R.id.row_value).text.toString())
        row.performClick()
        idle()
        val dialog = ShadowDialog.getLatestDialog()
        assertTrue(dialog is BottomWheelDialog && dialog.isShowing)
        dialog.findViewById<View>(R.id.sheet_confirm).performClick()
        idle()
        assertEquals("175 cm", row.findViewById<TextView>(R.id.row_value).text.toString())
    }

    @Test
    fun builtinDialogOpens() {
        val activity = launch(DialogActivity::class.java)
        activity.findViewById<View>(R.id.row_blood).performClick()
        idle()
        val dialog = ShadowDialog.getLatestDialog()
        assertTrue(dialog is android.app.AlertDialog && dialog.isShowing)
    }
}
