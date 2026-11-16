package com.wx.wheelview.widget

import android.app.Activity
import android.graphics.Color
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.wx.wheelview.adapter.ArrayWheelAdapter
import com.wx.wheelview.common.WheelConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Duration

/**
 * WheelView 回归测试：走 Activity 真实的布局流程
 */
@RunWith(RobolectricTestRunner::class)
class WheelViewTest {

    private lateinit var activity: Activity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    }

    private fun createWheel(): WheelView<String> {
        val wheel = WheelView<String>(activity)
        wheel.setWheelAdapter(ArrayWheelAdapter(activity))
        wheel.skin = WheelView.Skin.Holo
        wheel.setWheelData(DATA)
        return wheel
    }

    /**
     * 只推进 100ms，足够完成布局，但不足以触发旧实现中 500ms 的延迟任务
     */
    private fun layout() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))
    }

    private fun show(view: View) {
        activity.setContentView(view, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        layout()
    }

    private fun centerText(wheel: WheelView<String>): TextView {
        val center = wheel.getChildAt(wheel.childCount / 2)
        return center.findViewWithTag(WheelConstants.WHEEL_ITEM_TEXT_TAG)
    }

    // Color.WHITE == -1，旧实现把 -1 当作「未设置」，导致白色无法生效（issue #18 #23 #50）
    @Test
    fun whiteTextColorIsApplied() {
        val wheel = createWheel()
        val style = WheelView.WheelViewStyle()
        style.textColor = Color.WHITE
        style.selectedTextColor = Color.WHITE
        wheel.style = style
        show(wheel)

        assertTrue(wheel.childCount > 0)
        for (i in 0 until wheel.childCount) {
            val text = wheel.getChildAt(i).findViewWithTag<TextView>(WheelConstants.WHEEL_ITEM_TEXT_TAG)
            assertEquals(Color.WHITE, text.currentTextColor)
        }
    }

    // 旧实现先隐藏控件再延迟 500ms 才定位，造成闪烁和延迟（issue #16 #17 #33 #60）
    @Test
    fun setSelectionTakesEffectWithoutDelay() {
        val wheel = createWheel()
        show(wheel)

        wheel.setSelection(3)
        layout()

        assertEquals(View.VISIBLE, wheel.visibility)
        assertEquals(3, wheel.currentPosition)
        assertEquals("d", centerText(wheel).text.toString())
    }

    // 首次布局前调用 setSelection，应在布局完成后生效
    @Test
    fun setSelectionBeforeLayoutIsAppliedAfterLayout() {
        val wheel = createWheel()
        wheel.setSelection(4)
        show(wheel)

        assertEquals(4, wheel.currentPosition)
        assertEquals("e", centerText(wheel).text.toString())
    }

    // 运行时从非循环切到循环：非循环时首尾的占位行被隐藏，这些条目被复用到循环位置后必须重新显示，
    // 否则滚动停下时会空掉一行
    @Test
    fun switchingToLoopShowsRecycledPlaceholderRows() {
        val wheel = createWheel()
        wheel.setWheelSize(5)
        show(wheel)
        // 非循环且选中第一项时，最上面两行是隐藏的占位行
        assertEquals(View.INVISIBLE, wheel.getChildAt(0).visibility)

        wheel.setLoop(true)
        layout()

        assertTrue(wheel.childCount >= 5)
        for (i in 0 until wheel.childCount) {
            assertEquals("第 $i 行应可见", View.VISIBLE, wheel.getChildAt(i).visibility)
        }
    }

    // 循环模式下的 setSelection
    @Test
    fun setSelectionInLoopMode() {
        val wheel = createWheel()
        wheel.setLoop(true)
        show(wheel)
        assertEquals("a", centerText(wheel).text.toString())

        wheel.setSelection(6)
        layout()

        assertEquals(6, wheel.currentPosition)
        assertEquals("g", centerText(wheel).text.toString())
    }

    // 控件自身先 GONE 再 VISIBLE，高度和选中应正常（issue #55）
    @Test
    fun wheelWorksAfterItselfBecomesVisible() {
        val wheel = createWheel()
        wheel.visibility = View.GONE
        show(wheel)

        wheel.visibility = View.VISIBLE
        layout()
        wheel.setSelection(5)
        layout()

        val itemHeight = wheel.getChildAt(0).height
        assertTrue(itemHeight > 0)
        assertEquals(itemHeight * WheelView.WHEEL_SIZE, wheel.layoutParams.height)
        assertEquals(5, wheel.currentPosition)
    }

    // 首次布局时父容器为 GONE，旧实现会永久丢失 item 高度，之后选中和回调都失效（issue #20 #55 #72）
    @Test
    fun wheelWorksAfterParentBecomesVisible() {
        val container = FrameLayout(activity)
        container.visibility = View.GONE
        val wheel = createWheel()
        container.addView(wheel, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        activity.setContentView(container)
        layout()

        container.visibility = View.VISIBLE
        layout()
        wheel.setSelection(2)
        layout()

        val itemHeight = wheel.getChildAt(0).height
        assertTrue(itemHeight > 0)
        assertEquals(itemHeight * WheelView.WHEEL_SIZE, wheel.layoutParams.height)
        assertEquals(2, wheel.currentPosition)
    }

    private companion object {
        val DATA = listOf("a", "b", "c", "d", "e", "f", "g")
    }
}
