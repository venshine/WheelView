package com.wx.wheelview.demo;

import android.app.Activity;
import android.os.Bundle;

import com.wx.wheelview.ArrayWheelAdapter;
import com.wx.wheelview.WheelData;
import com.wx.wheelview.WheelUtils;
import com.wx.wheelview.WheelView;

import java.util.ArrayList;

/**
 * @author fengwx
 */
public class MainActivity extends Activity {

    private WheelView wheelView1, wheelView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWheel1();
        initWheel2();
    }

    private void initWheel1() {
        wheelView1 = (WheelView) findViewById(R.id.wheelview1);
        wheelView1.setWheelData(createArrays());
        wheelView1.setWheelSize(5);
        wheelView1.setLoop(false);
        wheelView1.setSkin(WheelView.Skin.Common);
        ArrayWheelAdapter adapter = new ArrayWheelAdapter(this);
        wheelView1.setWheelAdapter(adapter);
        wheelView1.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
                WheelUtils.log(s);
            }
        });
    }

    private void initWheel2() {
        wheelView2 = (WheelView) findViewById(R.id.wheelview2);
        wheelView2.setWheelData(createArrays());
        wheelView2.setWheelSize(5);
        wheelView2.setLoop(true);
        wheelView2.setSkin(WheelView.Skin.Holo);
        ArrayWheelAdapter adapter = new ArrayWheelAdapter(this);
        wheelView2.setWheelAdapter(adapter);
        wheelView2.setSelection(5 / 2);
        wheelView2.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
                WheelUtils.log(s);
            }
        });
    }

    private ArrayList<String> createArrays() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            list.add("item" + i);
        }
        return list;
    }

    private ArrayList<WheelData> createDatas() {
        ArrayList<WheelData> list = new ArrayList<WheelData>();
        WheelData item;
        for (int i = 0; i < 10; i++) {
            item = new WheelData();
            item.setId(R.mipmap.ic_launcher);
            item.setName("item" + i);
            list.add(item);
        }
        return list;
    }

}
