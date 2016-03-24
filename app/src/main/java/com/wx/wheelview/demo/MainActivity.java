package com.wx.wheelview.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.wx.wheelview.ArrayWheelAdapter;
import com.wx.wheelview.SimpleWheelAdapter;
import com.wx.wheelview.WheelData;
import com.wx.wheelview.WheelUtils;
import com.wx.wheelview.WheelView;

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
        ArrayWheelAdapter adapter = new ArrayWheelAdapter(this);
        wheelView1.setWheelAdapter(adapter);
        wheelView1.setWheelSize(3);
        wheelView1.setSkin(WheelView.Skin.Common);
        wheelView1.setWheelData(createArrays());
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textColor = Color.BLACK;
        style.selectedTextColor = Color.RED;
        wheelView1.setStyle(style);
        wheelView1.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
                WheelUtils.log(s);
            }
        });
    }

    /**
     * holo皮肤
     */
    private void initWheel2() {
        wheelView2 = (WheelView) findViewById(R.id.wheelview2);
        ArrayWheelAdapter adapter = new ArrayWheelAdapter(this);
        wheelView2.setWheelAdapter(adapter);
        wheelView2.setWheelSize(5);
        wheelView2.setWheelData(createArrays());
        wheelView2.setLoop(true);
        wheelView2.setExtraText("文本", Color.RED, 40, 120);
        wheelView2.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textColor = Color.BLACK;
        style.selectedTextColor = Color.BLUE;
        wheelView2.setStyle(style);
        wheelView2.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
                WheelUtils.log(s);
            }
        });
    }

    /**
     * 图文混排，无皮肤
     */
    private void initWheel3() {
        wheelView3 = (WheelView) findViewById(R.id.wheelview3);
        SimpleWheelAdapter adapter = new SimpleWheelAdapter(this);
        wheelView3.setWheelAdapter(adapter);
        wheelView3.setWheelSize(5);
        wheelView3.setWheelData(createDatas());
        wheelView3.setSkin(WheelView.Skin.None);
        wheelView3.setSelection(2);
        wheelView3.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<WheelData>() {
            @Override
            public void onItemSelected(int position, WheelData data) {
                WheelUtils.log(data.getName());
            }
        });
    }

    /**
     * 自定义布局
     */
    private void initWheel4() {
        wheelView4 = (WheelView) findViewById(R.id.wheelview4);
        MyWheelAdapter adapter = new MyWheelAdapter(this);
        wheelView4.setWheelAdapter(adapter);
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
                WheelUtils.log(s);
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
