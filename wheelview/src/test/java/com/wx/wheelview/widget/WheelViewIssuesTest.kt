package com.wx.wheelview.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Looper
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.wx.wheelview.adapter.ArrayWheelAdapter
import com.wx.wheelview.common.WheelConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLog
import java.time.Duration

/**
 * 针对 GitHub issue 的回归测试：走 Activity / Dialog 真实的布局流程
 */
@RunWith(RobolectricTestRunner::class)
class WheelViewIssuesTest {

    private lateinit var activity: Activity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    }

    private fun idle(millis: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))
    }

    private fun <T> createWheel(data: List<T>): WheelView<T> {
        val wheel = WheelView<T>(activity)
        wheel.setWheelAdapter(ArrayWheelAdapter(activity))
        wheel.skin = WheelView.Skin.Holo
        wheel.setWheelData(data)
        return wheel
    }

    private fun show(view: View) {
        activity.setContentView(view, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        idle(100)
    }

    private fun sideBySide(vararg wheels: WheelView<*>): LinearLayout {
        val layout = LinearLayout(activity)
        for (wheel in wheels) {
            layout.addView(wheel, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        return layout
    }

    // 数据少于滚轮个数时（非循环），选中位置应正确（issue #37 #70）
    @Test
    fun selectionWithFewerItemsThanWheelSize() {
        val wheel = createWheel(listOf("a", "b"))
        show(wheel)
        assertEquals(0, wheel.currentPosition)
        assertEquals("a", centerText(wheel).text.toString())

        wheel.setSelection(1)
        idle(100)

        assertEquals(1, wheel.currentPosition)
        assertEquals("b", centerText(wheel).text.toString())
    }

    // 数据少于滚轮个数时（循环），选中位置应正确（issue #37）
    @Test
    fun loopSelectionWithFewerItemsThanWheelSize() {
        val wheel = createWheel(listOf("a", "b"))
        wheel.setLoop(true)
        show(wheel)
        assertEquals("a", centerText(wheel).text.toString())

        wheel.setSelection(1)
        idle(100)

        assertEquals(1, wheel.currentPosition)
        assertEquals("b", centerText(wheel).text.toString())
    }

    private class City(val name: String) {
        override fun toString(): String = name
    }

    // 联动数据的 key 为条目显示文本，数据类型不是 String 时也应能联动（issue #54）
    @Test
    fun joinWorksWithNonStringItems() {
        val subData = hashMapOf(
            "x" to listOf(City("x1"), City("x2")),
            "y" to listOf(City("y1"), City("y2")),
        )
        val main = createWheel(listOf(City("x"), City("y")))
        val sub = createWheel(subData.getValue("x"))
        main.join(sub)
        main.joinDatas(subData)
        show(sideBySide(main, sub))

        main.setSelection(1)
        idle(1000)

        assertEquals("y1", centerText(sub).text.toString())
    }

    // 布局完成后再调用 setStyle，样式应立即生效（issue #73）
    @Test
    fun setStyleAfterLayoutIsApplied() {
        val wheel = createWheel(listOf("a", "b", "c", "d"))
        show(wheel)

        val style = WheelView.WheelViewStyle()
        style.textColor = Color.RED
        style.selectedTextColor = Color.BLUE
        wheel.style = style
        idle(100)

        assertEquals(Color.BLUE, centerText(wheel).currentTextColor)
    }

    // WheelViewDialog.setDialogStyle 设置的边框颜色应生效（issue #23）
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun dialogBorderColorIsApplied() {
        WheelViewDialog<String>(activity)
            .setItems(listOf("a", "b", "c", "d"))
            .setDialogStyle(Color.RED)
            .show()
        idle(100)

        val dialog = ShadowDialog.getLatestDialog()
        val wheel = findWheel(dialog.window!!.decorView)
        assertNotNull(wheel)
        val itemHeight = wheel!!.getChildAt(0).height
        val bitmap = Bitmap.createBitmap(wheel.width, wheel.height, Bitmap.Config.ARGB_8888)
        wheel.background.draw(Canvas(bitmap))

        // holo 皮肤在选中项上下各画一条边框线
        assertEquals(Color.RED, bitmap.getPixel(1, itemHeight * (WheelView.WHEEL_SIZE / 2)))
    }

    // 手动改变副轮选中项后再切换主轮，副轮应回到第一项（issue #69）；主轮未切换时保留副轮选中项
    @Test
    fun joinResetsSubWheelToFirstItem() {
        val subData = hashMapOf(
            "x" to listOf("x1", "x2", "x3"),
            "y" to listOf("y1", "y2", "y3"),
        )
        val main = createWheel(listOf("x", "y"))
        val sub = createWheel(subData.getValue("x"))
        main.join(sub)
        main.joinDatas(subData)
        show(sideBySide(main, sub))
        sub.setSelection(2)
        idle(1000)
        assertEquals("x3", centerText(sub).text.toString())

        main.setSelection(1)
        idle(1000)

        assertEquals(0, sub.currentPosition)
        assertEquals("y1", centerText(sub).text.toString())
    }

    // 低密度大屏（如 mdpi 平板）上，条目上下 20px 的内边距会把 45dp 高的条目挤得只剩几像素，文字被裁掉（issue #28 #36）
    @Test
    @Config(qualifiers = "mdpi")
    fun itemTextIsNotClippedOnLowDensity() {
        val wheel = createWheel(listOf("a", "b", "c", "d"))
        show(wheel)

        val text = centerText(wheel)
        assertTrue("text height ${text.height} < line height ${text.lineHeight}",
            text.height >= text.lineHeight)
    }

    // 鼠标滚轮滚动后，选中项应自动对齐到中间（issue #66）
    @Test
    fun mouseWheelScrollAlignsSelection() {
        val wheel = createWheel(listOf("a", "b", "c", "d", "e", "f", "g", "h"))
        show(wheel)
        val itemH = wheel.getChildAt(0).height

        wheel.dispatchGenericMotionEvent(mouseScroll(-0.4f))
        assertTrue("scroll should leave items unaligned", wheel.getChildAt(0).y % itemH != 0f)
        idle(1000)

        assertEquals(0f, wheel.getChildAt(0).y % itemH, 0f)
    }

    /**
     * 模拟 issue #41 堆栈中 HTC ROM 的行为：smoothScrollBy 内部 endFling 时同步回调 SCROLL_STATE_IDLE。
     * 只替换平台的这一处行为，WheelView 自身逻辑走真实代码。
     */
    private class SyncIdleWheelView<T>(context: Context) : WheelView<T>(context) {
        var scrollListener: OnScrollListener? = null
        var depth = 0
        var maxDepth = 0

        override fun setOnScrollListener(l: OnScrollListener?) {
            super.setOnScrollListener(l)
            scrollListener = l
        }

        override fun smoothScrollBy(distance: Int, duration: Int) {
            depth++
            maxDepth = maxOf(maxDepth, depth)
            try {
                if (depth < 20) {
                    scrollListener!!.onScrollStateChanged(this, AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                }
            } finally {
                depth--
            }
        }
    }

    // smoothScrollBy 同步回调 IDLE 时不应无限递归导致 StackOverflowError（issue #41 #13）
    @Test
    fun idleCallbackDoesNotRecurse() {
        val wheel = SyncIdleWheelView<String>(activity)
        wheel.setWheelAdapter(ArrayWheelAdapter(activity))
        wheel.setWheelData(listOf("a", "b", "c", "d", "e", "f", "g", "h"))
        show(wheel)
        wheel.scrollListBy(wheel.getChildAt(0).height / 3)

        wheel.scrollListener!!.onScrollStateChanged(wheel, AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
        idle(1000)

        assertEquals(1, wheel.maxDepth)
    }

    // 布局过程中修改条目文字大小会触发 requestLayout 警告（issue #53）
    @Test
    fun noRequestLayoutWarningDuringLayout() {
        val wheel = createWheel(listOf("a", "b", "c", "d", "e", "f", "g"))
        val style = WheelView.WheelViewStyle()
        style.selectedTextZoom = 1.5f
        wheel.style = style
        show(wheel)
        ShadowLog.clear()

        wheel.setSelection(4)
        idle(100)

        assertEquals("e", centerText(wheel).text.toString())
        val warnings = ShadowLog.getLogs().mapNotNull { it.msg }.filter { it.contains("improperly called") }
        assertTrue(warnings.toString(), warnings.isEmpty())
    }

    // 弹窗内多个滚轮并排，数据在布局后才设置的滚轮也应有边框和渐变效果（issue #14）
    @Test
    fun sideBySideWheelsWithLateDataAreStyled() {
        val wheels = List(3) { i ->
            val wheel = WheelView<String>(activity)
            wheel.skin = WheelView.Skin.Holo
            val style = wheel.style
            style.selectedTextZoom = 1.3f
            style.textAlpha = 0.6f
            wheel.style = style
            wheel.setWheelAdapter(ArrayWheelAdapter(activity))
            if (i != 1) {
                wheel.setWheelData(listOf("a", "b", "c", "d", "e"))
            }
            wheel
        }
        show(sideBySide(*wheels.toTypedArray()))

        wheels[1].setWheelData(listOf("1", "2", "3", "4", "5"))
        idle(100)

        for (wheel in wheels) {
            assertNotNull(wheel.background)
            assertEquals(wheel.getChildAt(0).height * WheelView.WHEEL_SIZE, wheel.height)
            assertTrue(wheel.getChildAt(0).alpha < 1f)
        }
    }

    // 选中项背景色（issue #38）
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun selectedBackgroundColorIsDrawn() {
        for (skin in listOf(WheelView.Skin.Common, WheelView.Skin.Holo)) {
            val wheel = createWheel(listOf("a", "b", "c", "d", "e"))
            wheel.skin = skin
            val style = WheelView.WheelViewStyle()
            style.selectedBackgroundColor = Color.YELLOW
            wheel.style = style
            show(wheel)

            val itemH = wheel.getChildAt(0).height
            val bitmap = Bitmap.createBitmap(wheel.width, wheel.height, Bitmap.Config.ARGB_8888)
            wheel.background.draw(Canvas(bitmap))
            val centerY = itemH * (WheelView.WHEEL_SIZE / 2) + itemH / 2
            assertEquals(skin.name, Color.YELLOW, bitmap.getPixel(wheel.width / 2, centerY))
        }
    }

    // 未选中项文字按距离逐级缩小（issue #56）
    @Test
    fun textScaleDecreasesWithDistance() {
        val wheel = createWheel(listOf("a", "b", "c", "d", "e", "f", "g"))
        wheel.setWheelSize(5)
        val style = WheelView.WheelViewStyle()
        style.textScale = 0.8f
        wheel.style = style
        show(wheel)
        wheel.setSelection(3)
        idle(100)

        val center = wheel.childCount / 2
        assertEquals(1f, textAt(wheel, center).scaleX, 0.001f)
        assertEquals(0.8f, textAt(wheel, center - 1).scaleX, 0.001f)
        assertEquals(0.8f, textAt(wheel, center + 1).scaleY, 0.001f)
        assertEquals(0.64f, textAt(wheel, center - 2).scaleX, 0.001f)
    }

    // ViewPager 忽略子 View 的 LayoutParams 高度、强制撑满整页，滚轮高度不再是 条目高 × 个数，
    // 数据一屏放得下时无法滚动，setSelection 等都失效（issue #63）
    @Test
    fun wheelWorksInViewPager() {
        val wheels = List(3) {
            createWheel(listOf("a", "b", "c", "d", "e", "f")).apply { setSelection(3) }
        }
        val pager = ViewPager(activity)
        pager.adapter = object : PagerAdapter() {
            override fun getCount(): Int = wheels.size

            override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                container.addView(wheels[position], ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                return wheels[position]
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }
        }
        activity.setContentView(pager)
        idle(100)
        pager.setCurrentItem(2, false)
        idle(100)
        pager.setCurrentItem(0, false)
        idle(100)

        for (wheel in wheels) {
            assertEquals(3, wheel.currentPosition)
            assertEquals("d", centerText(wheel).text.toString())
            assertEquals(wheel.getChildAt(0).height * WheelView.WHEEL_SIZE, wheel.height)
        }
        wheels[1].setSelection(5)
        idle(100)
        assertEquals("f", centerText(wheels[1]).text.toString())
    }

    private companion object {

        fun centerText(wheel: WheelView<*>): TextView = textAt(wheel, wheel.childCount / 2)

        fun textAt(wheel: WheelView<*>, index: Int): TextView =
            wheel.getChildAt(index).findViewWithTag(WheelConstants.WHEEL_ITEM_TEXT_TAG)

        fun mouseScroll(vscroll: Float): MotionEvent {
            val properties = arrayOf(MotionEvent.PointerProperties().apply { toolType = MotionEvent.TOOL_TYPE_MOUSE })
            val coords = arrayOf(MotionEvent.PointerCoords().apply {
                x = 10f
                y = 10f
                setAxisValue(MotionEvent.AXIS_VSCROLL, vscroll)
            })
            return MotionEvent.obtain(0, 0, MotionEvent.ACTION_SCROLL, 1, properties, coords, 0, 0,
                1f, 1f, 0, 0, InputDevice.SOURCE_MOUSE, 0)
        }

        fun findWheel(view: View): WheelView<*>? {
            if (view is WheelView<*>) {
                return view
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    findWheel(view.getChildAt(i))?.let { return it }
                }
            }
            return null
        }
    }
}
