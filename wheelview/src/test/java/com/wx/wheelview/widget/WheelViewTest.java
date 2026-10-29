package com.wx.wheelview.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.common.WheelConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * WheelView 回归测试：走 Activity 真实的布局流程
 */
@RunWith(RobolectricTestRunner.class)
public class WheelViewTest {

    private static final List<String> DATA = Arrays.asList("a", "b", "c", "d", "e", "f", "g");

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
    }

    private WheelView<String> createWheel() {
        WheelView<String> wheel = new WheelView<>(activity);
        wheel.setWheelAdapter(new ArrayWheelAdapter<String>(activity));
        wheel.setSkin(WheelView.Skin.Holo);
        wheel.setWheelData(DATA);
        return wheel;
    }

    /**
     * 只推进 100ms，足够完成布局，但不足以触发旧实现中 500ms 的延迟任务
     */
    private void layout() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100));
    }

    private TextView centerText(WheelView<String> wheel) {
        View center = wheel.getChildAt(wheel.getChildCount() / 2);
        return center.findViewWithTag(WheelConstants.WHEEL_ITEM_TEXT_TAG);
    }

    // Color.WHITE == -1，旧实现把 -1 当作「未设置」，导致白色无法生效（issue #18 #23 #50）
    @Test
    public void whiteTextColorIsApplied() {
        WheelView<String> wheel = createWheel();
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textColor = Color.WHITE;
        style.selectedTextColor = Color.WHITE;
        wheel.setStyle(style);
        activity.setContentView(wheel, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout();

        assertTrue(wheel.getChildCount() > 0);
        for (int i = 0; i < wheel.getChildCount(); i++) {
            TextView text = wheel.getChildAt(i).findViewWithTag(WheelConstants.WHEEL_ITEM_TEXT_TAG);
            assertEquals(Color.WHITE, text.getCurrentTextColor());
        }
    }

    // 旧实现先隐藏控件再延迟 500ms 才定位，造成闪烁和延迟（issue #16 #17 #33 #60）
    @Test
    public void setSelectionTakesEffectWithoutDelay() {
        WheelView<String> wheel = createWheel();
        activity.setContentView(wheel, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout();

        wheel.setSelection(3);
        layout();

        assertEquals(View.VISIBLE, wheel.getVisibility());
        assertEquals(3, wheel.getCurrentPosition());
        assertEquals("d", centerText(wheel).getText().toString());
    }

    // 首次布局前调用 setSelection，应在布局完成后生效
    @Test
    public void setSelectionBeforeLayoutIsAppliedAfterLayout() {
        WheelView<String> wheel = createWheel();
        wheel.setSelection(4);
        activity.setContentView(wheel, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout();

        assertEquals(4, wheel.getCurrentPosition());
        assertEquals("e", centerText(wheel).getText().toString());
    }

    // 循环模式下的 setSelection
    @Test
    public void setSelectionInLoopMode() {
        WheelView<String> wheel = createWheel();
        wheel.setLoop(true);
        activity.setContentView(wheel, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout();
        assertEquals("a", centerText(wheel).getText().toString());

        wheel.setSelection(6);
        layout();

        assertEquals(6, wheel.getCurrentPosition());
        assertEquals("g", centerText(wheel).getText().toString());
    }

    // 控件自身先 GONE 再 VISIBLE，高度和选中应正常（issue #55）
    @Test
    public void wheelWorksAfterItselfBecomesVisible() {
        WheelView<String> wheel = createWheel();
        wheel.setVisibility(View.GONE);
        activity.setContentView(wheel, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout();

        wheel.setVisibility(View.VISIBLE);
        layout();
        wheel.setSelection(5);
        layout();

        int itemHeight = wheel.getChildAt(0).getHeight();
        assertTrue(itemHeight > 0);
        assertEquals(itemHeight * WheelView.WHEEL_SIZE, wheel.getLayoutParams().height);
        assertEquals(5, wheel.getCurrentPosition());
    }

    // 首次布局时父容器为 GONE，旧实现会永久丢失 item 高度，之后选中和回调都失效（issue #20 #55 #72）
    @Test
    public void wheelWorksAfterParentBecomesVisible() {
        FrameLayout container = new FrameLayout(activity);
        container.setVisibility(View.GONE);
        WheelView<String> wheel = createWheel();
        container.addView(wheel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        activity.setContentView(container);
        layout();

        container.setVisibility(View.VISIBLE);
        layout();
        wheel.setSelection(2);
        layout();

        int itemHeight = wheel.getChildAt(0).getHeight();
        assertTrue(itemHeight > 0);
        assertEquals(itemHeight * WheelView.WHEEL_SIZE, wheel.getLayoutParams().height);
        assertEquals(2, wheel.getCurrentPosition());
    }
}
