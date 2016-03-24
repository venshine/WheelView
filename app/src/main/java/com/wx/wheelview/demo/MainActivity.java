package com.wx.wheelview.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.adapter.SimpleWheelAdapter;
import com.wx.wheelview.common.WheelData;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;

/**
 * Demo
 *
 * @author fengwx
 */
public class MainActivity extends Activity {

    private WheelView wheelView1, wheelView2, wheelView3, wheelView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWheel1();
        initWheel2();
        initWheel3();
        initWheel4();
    }

    /**
     * common皮肤
     */
    private void initWheel1() {
        wheelView1 = (WheelView) findViewById(R.id.wheelview1);
        wheelView1.setWheelAdapter(new ArrayWheelAdapter(this));
        wheelView1.setSkin(WheelView.Skin.Common);
        wheelView1.setWheelData(createArrays());
    }

    /**
     * holo皮肤
     */
    private void initWheel2() {
        wheelView2 = (WheelView) findViewById(R.id.wheelview2);
        wheelView2.setWheelAdapter(new ArrayWheelAdapter(this));
        wheelView2.setWheelSize(5);
        wheelView2.setWheelData(createArrays());
        wheelView2.setLoop(true);
        wheelView2.setExtraText("文本", Color.RED, 40, 120);
        wheelView2.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textColor = Color.BLACK;
        style.selectedTextColor = Color.BLUE;
        wheelView2.setStyle(style);
    }

    /**
     * 图文混排，无皮肤
     */
    private void initWheel3() {
        wheelView3 = (WheelView) findViewById(R.id.wheelview3);
        wheelView3.setWheelAdapter(new SimpleWheelAdapter(this));
        wheelView3.setWheelSize(5);
        wheelView3.setWheelData(createDatas());
        wheelView3.setSkin(WheelView.Skin.None);
        wheelView3.setSelection(2);
        wheelView3.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<WheelData>() {
            @Override
            public void onItemSelected(int position, WheelData data) {
            }
        });
    }

    /**
     * 自定义布局
     */
    private void initWheel4() {
        wheelView4 = (WheelView) findViewById(R.id.wheelview4);
        wheelView4.setWheelAdapter(new MyWheelAdapter(this));
        wheelView4.setWheelSize(5);
        wheelView4.setSkin(WheelView.Skin.Holo);
        wheelView4.setWheelData(createArrays());
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.YELLOW;
        style.textColor = Color.DKGRAY;
        style.selectedTextColor = Color.GREEN;
        wheelView4.setStyle(style);
        wheelView4.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
            }
        });
    }

    private ArrayList<String> createArrays() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            list.add("item" + i);
        }
        return list;
    }

    private ArrayList<WheelData> createDatas() {
        ArrayList<WheelData> list = new ArrayList<WheelData>();
        WheelData item;
        for (int i = 0; i < 20; i++) {
            item = new WheelData();
            item.setId(R.mipmap.ic_launcher);
            item.setName("item" + i);
            list.add(item);
        }
        return list;
    }

}
