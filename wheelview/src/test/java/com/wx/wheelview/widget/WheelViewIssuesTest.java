package com.wx.wheelview.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Looper;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.common.WheelConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowLog;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 针对 GitHub issue 的回归测试：走 Activity / Dialog 真实的布局流程
 */
@RunWith(RobolectricTestRunner.class)
public class WheelViewIssuesTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
    }

    private void idle(long millis) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis));
    }

    private static TextView centerText(WheelView<?> wheel) {
        View center = wheel.getChildAt(wheel.getChildCount() / 2);
        return center.findViewWithTag(WheelConstants.WHEEL_ITEM_TEXT_TAG);
    }

    private <T> WheelView<T> createWheel(List<T> data) {
        WheelView<T> wheel = new WheelView<>(activity);
        wheel.setWheelAdapter(new ArrayWheelAdapter<T>(activity));
        wheel.setSkin(WheelView.Skin.Holo);
        wheel.setWheelData(data);
        return wheel;
    }

    private void show(View view) {
        activity.setContentView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        idle(100);
    }

    // 数据少于滚轮个数时（非循环），选中位置应正确（issue #37 #70）
    @Test
    public void selectionWithFewerItemsThanWheelSize() {
        WheelView<String> wheel = createWheel(Arrays.asList("a", "b"));
        show(wheel);
        assertEquals(0, wheel.getCurrentPosition());
        assertEquals("a", centerText(wheel).getText().toString());

        wheel.setSelection(1);
        idle(100);

        assertEquals(1, wheel.getCurrentPosition());
        assertEquals("b", centerText(wheel).getText().toString());
    }

    // 数据少于滚轮个数时（循环），选中位置应正确（issue #37）
    @Test
    public void loopSelectionWithFewerItemsThanWheelSize() {
        WheelView<String> wheel = createWheel(Arrays.asList("a", "b"));
        wheel.setLoop(true);
        show(wheel);
        assertEquals("a", centerText(wheel).getText().toString());

        wheel.setSelection(1);
        idle(100);

        assertEquals(1, wheel.getCurrentPosition());
        assertEquals("b", centerText(wheel).getText().toString());
    }

    static class City {
        final String name;

        City(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // 联动数据的 key 为条目显示文本，数据类型不是 String 时也应能联动（issue #54）
    @Test
    public void joinWorksWithNonStringItems() {
        City x = new City("x");
        City y = new City("y");
        HashMap<String, List<City>> subData = new HashMap<>();
        subData.put("x", Arrays.asList(new City("x1"), new City("x2")));
        subData.put("y", Arrays.asList(new City("y1"), new City("y2")));

        WheelView<City> main = createWheel(Arrays.asList(x, y));
        WheelView<City> sub = createWheel(subData.get("x"));
        main.join(sub);
        main.joinDatas(subData);
        LinearLayout layout = new LinearLayout(activity);
        layout.addView(main, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(sub, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        show(layout);

        main.setSelection(1);
        idle(1000);

        assertEquals("y1", centerText(sub).getText().toString());
    }

    // 布局完成后再调用 setStyle，样式应立即生效（issue #73）
    @Test
    public void setStyleAfterLayoutIsApplied() {
        WheelView<String> wheel = createWheel(Arrays.asList("a", "b", "c", "d"));
        show(wheel);

        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textColor = Color.RED;
        style.selectedTextColor = Color.BLUE;
        wheel.setStyle(style);
        idle(100);

        assertEquals(Color.BLUE, centerText(wheel).getCurrentTextColor());
    }

    // WheelViewDialog.setDialogStyle 设置的边框颜色应生效（issue #23）
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    public void dialogBorderColorIsApplied() {
        new WheelViewDialog<String>(activity)
                .setItems(Arrays.asList("a", "b", "c", "d"))
                .setDialogStyle(Color.RED)
                .show();
        idle(100);

        Dialog dialog = ShadowDialog.getLatestDialog();
        WheelView<?> wheel = findWheel(dialog.getWindow().getDecorView());
        assertNotNull(wheel);
        int itemHeight = wheel.getChildAt(0).getHeight();
        Bitmap bitmap = Bitmap.createBitmap(wheel.getWidth(), wheel.getHeight(), Bitmap.Config.ARGB_8888);
        wheel.getBackground().draw(new Canvas(bitmap));

        // holo 皮肤在选中项上下各画一条边框线
        assertEquals(Color.RED, bitmap.getPixel(1, itemHeight * (WheelView.WHEEL_SIZE / 2)));
    }

    // 手动改变副轮选中项后再切换主轮，副轮应回到第一项（issue #69）；主轮未切换时保留副轮选中项
    @Test
    public void joinResetsSubWheelToFirstItem() {
        HashMap<String, List<String>> subData = new HashMap<>();
        subData.put("x", Arrays.asList("x1", "x2", "x3"));
        subData.put("y", Arrays.asList("y1", "y2", "y3"));

        WheelView<String> main = createWheel(Arrays.asList("x", "y"));
        WheelView<String> sub = createWheel(subData.get("x"));
        main.join(sub);
        main.joinDatas(subData);
        LinearLayout layout = new LinearLayout(activity);
        layout.addView(main, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(sub, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        show(layout);
        sub.setSelection(2);
        idle(1000);
        assertEquals("x3", centerText(sub).getText().toString());

        main.setSelection(1);
        idle(1000);

        assertEquals(0, sub.getCurrentPosition());
        assertEquals("y1", centerText(sub).getText().toString());
    }

    // 低密度大屏（如 mdpi 平板）上，条目上下 20px 的内边距会把 45dp 高的条目挤得只剩几像素，文字被裁掉（issue #28 #36）
    @Test
    @Config(qualifiers = "mdpi")
    public void itemTextIsNotClippedOnLowDensity() {
        WheelView<String> wheel = createWheel(Arrays.asList("a", "b", "c", "d"));
        show(wheel);

        TextView text = centerText(wheel);
        assertTrue("text height " + text.getHeight() + " < line height " + text.getLineHeight(),
                text.getHeight() >= text.getLineHeight());
    }

    // 鼠标滚轮滚动后，选中项应自动对齐到中间（issue #66）
    @Test
    public void mouseWheelScrollAlignsSelection() {
        WheelView<String> wheel = createWheel(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"));
        show(wheel);
        int itemH = wheel.getChildAt(0).getHeight();

        wheel.dispatchGenericMotionEvent(mouseScroll(-0.4f));
        assertTrue("scroll should leave items unaligned", wheel.getChildAt(0).getY() % itemH != 0);
        idle(1000);

        assertEquals(0f, wheel.getChildAt(0).getY() % itemH, 0f);
    }

    private static MotionEvent mouseScroll(float vscroll) {
        MotionEvent.PointerProperties[] properties = {new MotionEvent.PointerProperties()};
        properties[0].toolType = MotionEvent.TOOL_TYPE_MOUSE;
        MotionEvent.PointerCoords[] coords = {new MotionEvent.PointerCoords()};
        coords[0].x = 10;
        coords[0].y = 10;
        coords[0].setAxisValue(MotionEvent.AXIS_VSCROLL, vscroll);
        return MotionEvent.obtain(0, 0, MotionEvent.ACTION_SCROLL, 1, properties, coords, 0, 0,
                1f, 1f, 0, 0, InputDevice.SOURCE_MOUSE, 0);
    }

    /**
     * 模拟 issue #41 堆栈中 HTC ROM 的行为：smoothScrollBy 内部 endFling 时同步回调 SCROLL_STATE_IDLE。
     * 只替换平台的这一处行为，WheelView 自身逻辑走真实代码。
     */
    static class SyncIdleWheelView<T> extends WheelView<T> {
        OnScrollListener scrollListener;
        int depth;
        int maxDepth;

        SyncIdleWheelView(Context context) {
            super(context);
        }

        @Override
        public void setOnScrollListener(OnScrollListener l) {
            super.setOnScrollListener(l);
            scrollListener = l;
        }

        @Override
        public void smoothScrollBy(int distance, int duration) {
            depth++;
            maxDepth = Math.max(maxDepth, depth);
            try {
                if (depth < 20) {
                    scrollListener.onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_IDLE);
                }
            } finally {
                depth--;
            }
        }
    }

    // smoothScrollBy 同步回调 IDLE 时不应无限递归导致 StackOverflowError（issue #41 #13）
    @Test
    public void idleCallbackDoesNotRecurse() {
        SyncIdleWheelView<String> wheel = new SyncIdleWheelView<>(activity);
        wheel.setWheelAdapter(new ArrayWheelAdapter<String>(activity));
        wheel.setWheelData(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"));
        show(wheel);
        wheel.scrollListBy(wheel.getChildAt(0).getHeight() / 3);

        wheel.scrollListener.onScrollStateChanged(wheel, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
        idle(1000);

        assertEquals(1, wheel.maxDepth);
    }

    // 布局过程中修改条目文字大小会触发 requestLayout 警告（issue #53）
    @Test
    public void noRequestLayoutWarningDuringLayout() {
        WheelView<String> wheel = createWheel(Arrays.asList("a", "b", "c", "d", "e", "f", "g"));
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.selectedTextZoom = 1.5f;
        wheel.setStyle(style);
        show(wheel);
        ShadowLog.clear();

        wheel.setSelection(4);
        idle(100);

        assertEquals("e", centerText(wheel).getText().toString());
        List<String> warnings = new ArrayList<>();
        for (ShadowLog.LogItem item : ShadowLog.getLogs()) {
            if (item.msg != null && item.msg.contains("improperly called")) {
                warnings.add(item.msg);
            }
        }
        assertTrue(warnings.toString(), warnings.isEmpty());
    }

    // 弹窗内多个滚轮并排，数据在布局后才设置的滚轮也应有边框和渐变效果（issue #14）
    @Test
    public void sideBySideWheelsWithLateDataAreStyled() {
        LinearLayout layout = new LinearLayout(activity);
        List<WheelView<String>> wheels = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            WheelView<String> wheel = new WheelView<>(activity);
            wheel.setSkin(WheelView.Skin.Holo);
            WheelView.WheelViewStyle style = wheel.getStyle();
            style.selectedTextZoom = 1.3f;
            style.textAlpha = 0.6f;
            wheel.setStyle(style);
            wheel.setWheelAdapter(new ArrayWheelAdapter<String>(activity));
            if (i != 1) {
                wheel.setWheelData(Arrays.asList("a", "b", "c", "d", "e"));
            }
            wheels.add(wheel);
            layout.addView(wheel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
        show(layout);

        wheels.get(1).setWheelData(Arrays.asList("1", "2", "3", "4", "5"));
        idle(100);

        for (WheelView<String> wheel : wheels) {
            assertNotNull(wheel.getBackground());
            assertEquals(wheel.getChildAt(0).getHeight() * WheelView.WHEEL_SIZE, wheel.getHeight());
            assertTrue(wheel.getChildAt(0).getAlpha() < 1f);
        }
    }

    private static WheelView<?> findWheel(View view) {
        if (view instanceof WheelView) {
            return (WheelView<?>) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                WheelView<?> found = findWheel(group.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
