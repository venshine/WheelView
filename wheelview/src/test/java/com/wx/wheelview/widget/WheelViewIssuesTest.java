package com.wx.wheelview.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.common.WheelConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowDialog;

import java.time.Duration;
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
