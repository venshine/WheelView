package com.wx.wheelview.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.adapter.BaseWheelAdapter;
import com.wx.wheelview.adapter.SimpleWheelAdapter;
import com.wx.wheelview.common.WheelConstants;
import com.wx.wheelview.common.WheelData;
import com.wx.wheelview.common.WheelViewException;
import com.wx.wheelview.graphics.DrawableFactory;
import com.wx.wheelview.util.WheelUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Java 调用方兼容性：库用 Kotlin 编写，这里按 Java 使用方的常见写法调用公开 API。
 * 能编译即说明 Java 源码兼容（原始类型、子类覆写、常量用于 switch、静态方法等），断言覆盖原有的异常行为。
 */
@RunWith(RobolectricTestRunner.class)
@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public class JavaInteropTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
    }

    /** Java 自定义适配器：覆写 protected bindView，直接读取 protected 字段 mList */
    static class JavaAdapter extends BaseWheelAdapter<String> {
        private final Context context;

        JavaAdapter(Context context) {
            this.context = context;
        }

        @Override
        protected View bindView(int position, View convertView, ViewGroup parent) {
            TextView textView = convertView != null ? (TextView) convertView : new TextView(context);
            textView.setText(mList.get(position));
            return textView;
        }
    }

    /** Java 子类覆写 WheelView 的公开方法，参数签名必须与原 Java 版一致 */
    static class JavaWheelView<T> extends WheelView<T> {
        int dataCalls;

        JavaWheelView(Context context) {
            super(context);
        }

        @Override
        public void setWheelData(List<T> list) {
            dataCalls++;
            super.setWheelData(list);
        }

        @Override
        public void joinDatas(HashMap<String, List<T>> map) {
            super.joinDatas(map);
        }

        @Override
        public void join(WheelView wheelView) {
            super.join(wheelView);
        }

        @Override
        public int getCurrentPosition() {
            return super.getCurrentPosition();
        }

        @Override
        public void setStyle(WheelViewStyle style) {
            super.setStyle(style);
        }
    }

    static class JavaWheelData extends WheelData {
        @Override
        public String getName() {
            return "java:" + super.getName();
        }
    }

    @Test
    public void readmeUsageCompiles() {
        WheelView<String> wheelView = new WheelView<>(activity);
        wheelView.setWheelAdapter(new ArrayWheelAdapter<>(activity));
        wheelView.setSkin(WheelView.Skin.Common);
        wheelView.setWheelData(Arrays.asList("item0", "item1", "item2", "item3", "item4"));
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textColor = Color.WHITE;
        style.selectedTextColor = Color.BLACK;
        style.holoBorderWidth = 2;
        style.textAlpha = 0.5f;
        style.selectedTextBold = true;
        wheelView.setStyle(new WheelView.WheelViewStyle(style));
        wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
            }
        });
        wheelView.setOnWheelItemClickListener((position, s) -> s.length());
        wheelView.setOnWheelItemSelectedListener(null);
        wheelView.setExtraText("cm", Color.RED, 12, 40);

        Integer textColor = wheelView.getStyle().textColor;
        assertEquals(Integer.valueOf(Color.WHITE), textColor);
        assertNull(wheelView.getStyle().backgroundColor);
        assertEquals(WheelView.Skin.Common, wheelView.getSkin());
        assertEquals(5, wheelView.getWheelCount());
        assertEquals(0, wheelView.getSelection());
        assertEquals(-1, wheelView.getCurrentPosition());
        assertEquals("item0", wheelView.getSelectionItem());
        assertEquals(WheelView.Skin.Holo, WheelView.Skin.valueOf("Holo"));
        assertEquals(3, WheelView.Skin.values().length);
    }

    @Test
    public void rawTypesAndSubclassesCompile() {
        HashMap<String, List<String>> subData = new HashMap<>();
        subData.put("x", Arrays.asList("x1", "x2"));
        JavaWheelView<String> main = new JavaWheelView<>(activity);
        main.setWheelAdapter(new JavaAdapter(activity));
        main.setWheelData(Arrays.asList("x", "y"));
        WheelView sub = new WheelView(activity);
        sub.setWheelAdapter(new ArrayWheelAdapter(activity));
        sub.setWheelData(subData.get("x"));
        main.join(sub);
        main.joinDatas(subData);
        assertEquals(1, main.dataCalls);

        BaseWheelAdapter<String> adapter = new JavaAdapter(activity);
        adapter.setData(Arrays.asList("a", "b")).setLoop(true).setWheelSize(5).setClickable(true);
        assertEquals("b", adapter.getItem(3));
        assertEquals(Integer.MAX_VALUE, adapter.getCount());

        SimpleWheelAdapter simpleAdapter = new SimpleWheelAdapter(activity);
        simpleAdapter.setData(Arrays.asList(new WheelData(1, "a")));
        WheelData data = new JavaWheelData();
        data.setName("b");
        data.setId(2);
        assertEquals("java:b", data.getName());
        assertEquals(2, data.getId());

        WheelItem item = new WheelItem(activity);
        item.setText("text");
        new NestedScrollView(activity).addView(main);
    }

    @Test
    public void staticMembersAreJavaStatics() {
        // switch 只接受编译期常量：原 Java 版的常量在 Kotlin 版中仍是 const
        int tag = WheelConstants.WHEEL_ITEM_TEXT_TAG;
        switch (tag) {
            case WheelConstants.WHEEL_ITEM_TEXT_TAG:
            case WheelConstants.WHEEL_ITEM_IMAGE_TAG:
            case WheelView.WHEEL_SIZE:
            case IWheelView.WHEEL_SIZE + 10:
                break;
            default:
                fail();
        }
        assertEquals("com.wx.wheelview", WheelConstants.TAG);
        assertEquals(Color.parseColor("#83cde6"), WheelConstants.DIALOG_WHEEL_COLOR);
        assertEquals(WheelConstants.WHEEL_SKIN_HOLO_BORDER_COLOR, WheelConstants.DIALOG_WHEEL_COLOR);
        assertTrue(!IWheelView.LOOP && !WheelView.CLICKABLE);
        new WheelConstants();
        new WheelUtils();

        assertTrue(WheelUtils.isEmpty(null));
        assertTrue(WheelUtils.isEmpty(Arrays.<String>asList()));
        assertNull(WheelUtils.findTextView(null));
        assertTrue(WheelUtils.dip2px(activity, 10f) > 0);
        WheelUtils.log(null);
        Drawable drawable = DrawableFactory.createDrawable(WheelView.Skin.Holo, 100, 300,
                new WheelView.WheelViewStyle(), 3, 100);
        assertNotNull(drawable);
    }

    @Test
    public void dialogWithRawTypesCompiles() {
        WheelViewDialog dialog = new WheelViewDialog(activity);
        dialog.setTitle("title")
                .setItems(new String[]{"a", "b", "c"})
                .setButtonText("OK")
                .setDialogStyle(Color.RED)
                .setCount(5)
                .setOnDialogItemClickListener(new WheelViewDialog.OnDialogItemClickListener() {
                    @Override
                    public void onItemClick(int position, Object s) {
                    }
                });

        WheelViewDialog<String> typed = new WheelViewDialog<>(activity);
        typed.setItems(Arrays.asList("a", "b"))
                .setOnDialogItemClickListener((position, s) -> String.valueOf(s))
                .setOnDialogItemClickListener(null);
    }

    // null 参数沿用原 Java 版的处理：抛 WheelViewException（而不是 Kotlin 的 NullPointerException），或正常忽略
    @Test
    public void nullArgumentsKeepOriginalBehavior() {
        WheelView<String> wheel = new WheelView<>(activity, (WheelView.WheelViewStyle) null);
        assertNotNull(wheel.getStyle());
        wheel.setWheelAdapter(new ArrayWheelAdapter<>(activity));
        wheel.joinDatas(null);
        wheel.setExtraText(null, 0, 0, 0);
        wheel.setOnWheelItemClickListener(null);

        assertThrowsWheelViewException(() -> wheel.setWheelData(null));
        assertThrowsWheelViewException(() -> wheel.resetDataFromTop(null));
        assertThrowsWheelViewException(() -> wheel.join(null));
        assertThrowsWheelViewException(() -> wheel.setWheelSize(4));
        assertThrowsWheelViewException(() -> wheel.setAdapter(null));
        assertThrowsWheelViewException(() -> new WheelViewDialog<String>(activity).setItems((List<String>) null));
    }

    private static void assertThrowsWheelViewException(Runnable runnable) {
        try {
            runnable.run();
            fail("expected WheelViewException");
        } catch (WheelViewException expected) {
            // 预期
        }
    }
}
